package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.text.TextUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>TLV（Tag-Length-Value）解析与构建工具类。</p>
 *
 * <p>设计模型：</p>
 * <ul>
 *     <li>{@link TlvNode}：单个 TLV 三元组（不可变），是一等公民</li>
 *     <li>{@link TlvResult}：一次解析/提取操作的只读快照，包含节点列表、剩余数据、失败列表</li>
 *     <li>{@link TlvFailure}：单点失败描述（不阻断整体流程，用于表达“部分成功”）</li>
 * </ul>
 *
 * <p>约定：</p>
 * <ul>
 *     <li>Tag / Value 均以十六进制字符串表示，每两个字符一个字节</li>
 *     <li>输入允许含空白与不可见字符，解析前统一清除</li>
 *     <li>Tag 匹配大小写不敏感；输出统一大写</li>
 * </ul>
 *
 * @author yishui
 * @version 2.0.1
 * @since 1.0.0
 */
public final class TLV {

    /**
     * BER-TLV 长格式允许的最大长度字节数。
     *
     * <p>该常量同时被 {@link #parseLengthField(String)} 与
     * {@link #parseTopLevelBytes(byte[], String)} 使用，保证两条解析路径对
     * 长度字段的支持范围一致，避免长格式长度字节数过多导致的 {@code long} 溢出。</p>
     */
    private static final int MAX_LENGTH_BYTES = 3;

    /**
     * 支持的最大长度值（3 字节，即 0xFFFFFF）。
     */
    private static final long MAX_LENGTH_VALUE = 0xFFFFFFL;

    private TLV() {
    }

    // ==================================================================
    // 对外 API：单 Tag 提取
    // ==================================================================

    /**
     * 提取指定 tag 的 value；失败或未找到返回空字符串。
     *
     * <p>修复说明：原实现通过 {@code r.val(tag)} 查询，而节点中的 tag 已做
     * clean + upper 规范化；当传入 tag 含空白/不可见字符时可能查询不到。
     * 这里直接返回第一个成功节点的 value，语义更直接、更稳健。</p>
     *
     * @param tag 目标 Tag（十六进制串，大小写不敏感）
     * @param tlv 待提取的 TLV 串
     * @return 匹配到的 Value；失败或未找到时返回空串（非 {@code null}）
     */
    public static String extractVal(String tag, String tlv) {
        TlvResult r = extract(tag, tlv);
        if (!r.isSuccess() || r.nodes().isEmpty()) {
            return "";
        }
        return r.nodes().get(0).value();
    }

    /**
     * 提取指定 tag 的完整结果（成功时含 1 个节点）。
     *
     * <p>注意：本方法采用<b>字符串前缀匹配</b>，要求 {@code tlv} 以 {@code tag} 字符串开头。
     * 因此当 tag 是另一个 tag 的前缀时（如 {@code "01"} 与 {@code "0102"}），
     * 可能提取到非预期的节点。需要严格 BER-TLV 语义时请使用 {@link #parseTopLevelTlv(String)}。</p>
     *
     * @param tag 目标 Tag（十六进制串，大小写不敏感）
     * @param tlv 待提取的 TLV 串
     * @return 解析结果快照；成功时 {@link TlvResult#nodes()} 含 1 个节点
     */
    public static TlvResult extract(String tag, String tlv) {
        String cleanTlv = clean(tlv);
        String cleanTag = clean(tag);
        if (cleanTag == null || cleanTag.isEmpty() || cleanTlv == null || cleanTlv.isEmpty()) {
            return TlvResult.failure("illegal data", upper(cleanTlv));
        }

        // 修复：显式要求偶数长度十六进制，避免依赖 Hex.isHex 的实现细节。
        if (!isEvenHex(cleanTlv) || !isEvenHex(cleanTag)) {
            return TlvResult.failure("illegal data", upper(cleanTlv));
        }

        // 修复：预先计算 upperInput，避免多条失败路径中重复 trim + toUpperCase。
        String upperInput = upper(cleanTlv);

        if (!cleanTlv.regionMatches(true, 0, cleanTag, 0, cleanTag.length())) {
            return TlvResult.failure(
                    String.format("The data does not start with TAG %s", cleanTag), upperInput);
        }

        String tail = cleanTlv.substring(cleanTag.length());
        if (tail.length() < 2) {
            // 长度字段最少 1 字节（2 个十六进制字符），不足即视为非法 TLV
            return TlvResult.failure("Not valid TLV data", upperInput);
        }

        LengthParseResult lp = parseLengthField(tail);
        if (!lp.isSuccess()) {
            // 失败时 lp.exception() 恒非 null，直接取 message
            return TlvResult.failure(lp.exception().getMessage(), upperInput);
        }

        // 使用 long 计算尾部位置，避免 length * 2 在 int 范围溢出
        long valueEnd = lp.startPos() + (long) lp.length() * 2;
        if (valueEnd > tail.length()) {
            return TlvResult.failure("Not valid TLV data", upperInput);
        }

        int end = (int) valueEnd;
        TlvNode node = new TlvNode(upper(cleanTag), upper(tail.substring(lp.startPos(), end)));
        // 修复：remain 统一大写，保证输出快照与节点规范一致。
        return TlvResult.single(node, upper(tail.substring(end)));
    }

    // ==================================================================
    // 对外 API：多 Tag 提取
    // ==================================================================

    /**
     * 在同一层级依次提取多个 Tag。
     *
     * <p>关键语义：<b>单个 Tag 提取失败不中断、不推进游标</b>，继续尝试下一个 Tag；
     * 失败点记录在 {@link TlvResult#failures()} 中，成功的节点记录在 {@link TlvResult#nodes()} 中。
     * 当所有 Tag 都成功时 {@link TlvResult#isSuccess()} 为 {@code true}，
     * 部分失败时为 {@code false}（但 {@link TlvResult#isPartial()} 可识别“有成功也有失败”）。</p>
     *
     * @param tlv  待提取的 TLV 串（同一层级）
     * @param tags 需要依次提取的 Tag 列表
     * @return 结果快照；失败不中断，全部失败时 {@code nodes} 为空
     */
    public static TlvResult extractValsOnSameLevel(String tlv, String... tags) {
        String cleanTlv = clean(tlv);
        // 修复：显式拒绝空输入与奇数长度十六进制。
        if (cleanTlv == null || cleanTlv.isEmpty() || !isEvenHex(cleanTlv)) {
            return TlvResult.failure("illegal data", upper(cleanTlv));
        }
        if (tags == null || tags.length == 0) {
            return TlvResult.failure("no tags provided", upper(cleanTlv));
        }

        List<TlvNode> nodes = new ArrayList<>();
        List<TlvFailure> failures = new ArrayList<>();
        // 修复：初始游标与 remain 统一大写，保持输出一致性。
        String input = upper(cleanTlv);
        String remain = input;

        for (String tag : tags) {
            TlvResult r = extract(tag, input);
            if (r.isSuccess()) {
                nodes.addAll(r.nodes());
                input = r.remain();
                remain = input;
            } else {
                // 失败：记录，不推进游标，继续下一个 Tag
                failures.add(new TlvFailure(upper(clean(tag)), r.failureMessage(), r.getException()));
            }
        }
        return TlvResult.of(nodes, remain, failures);
    }

    /**
     * 递归提取嵌套层级中的多个 Tag；每层以上一层提取出的 Value 作为下一层输入。
     * 任一层失败即终止后续递归（因为后续输入已不可得），已成功的节点保留。
     *
     * @param tlv  最外层 TLV 串
     * @param tags 从外到内的 Tag 路径
     * @return 结果快照；失败点记录在 {@code failures} 中，成功节点顺序与 tags 顺序一致
     */
    public static TlvResult extractValsRecursive(String tlv, String... tags) {
        String cleanTlv = clean(tlv);
        // 修复：显式拒绝空输入与奇数长度十六进制。
        if (cleanTlv == null || cleanTlv.isEmpty() || !isEvenHex(cleanTlv)) {
            return TlvResult.failure("illegal data", upper(cleanTlv));
        }
        if (tags == null || tags.length == 0) {
            return TlvResult.failure("no tags provided", upper(cleanTlv));
        }

        List<TlvNode> nodes = new ArrayList<>();
        List<TlvFailure> failures = new ArrayList<>();
        // 修复：初始游标与 remain 统一大写。
        String input = upper(cleanTlv);
        String remain = input;

        for (String tag : tags) {
            TlvResult r = extract(tag, input);
            if (!r.isSuccess()) {
                failures.add(new TlvFailure(upper(clean(tag)), r.failureMessage(), r.getException()));
                break;
            }
            TlvNode node = r.nodes().get(0);
            nodes.add(node);
            // 下一层输入为当前节点的 Value
            input = node.value();
            // remain 记录最后一次成功解析后当前层的剩余数据
            remain = r.remain();
        }
        return TlvResult.of(nodes, remain, failures);
    }

    /**
     * 循环提取相同 Tag 的全部 Value，直到无法继续匹配或出现异常。
     *
     * <p>说明：每次成功提取后输入严格变短（至少消费 tag 与 length 字段），
     * 因此不会出现死循环。</p>
     *
     * <p>修复说明：原实现使用 {@code r.val(cleanTag)} 取值，会走
     * {@code all() → last() → value()} 的完整查询链路并再次 clean+upper；
     * 而 {@link #extract(String, String)} 成功时必然只返回 1 个节点，
     * 直接取 {@code nodes().get(0).value()} 更直接且高效。</p>
     *
     * @param tag 目标 Tag（十六进制串，大小写不敏感）
     * @param tlv 待提取的 TLV 串
     * @return 依次出现的 Value 列表；无匹配时返回空列表
     */
    public static List<String> extractValsLoop(String tag, String tlv) {
        String cleanTlv = clean(tlv);
        String cleanTag = clean(tag);
        // 修复：显式要求偶数长度十六进制。
        if (cleanTlv == null || cleanTag == null
                || !isEvenHex(cleanTlv) || !isEvenHex(cleanTag)) {
            return new ArrayList<>();
        }

        List<String> list = new ArrayList<>();
        // 修复：初始输入统一大写。
        String input = upper(cleanTlv);
        while (StringUtils.isNotBlank(input)) {
            TlvResult r = extract(cleanTag, input);
            if (!r.isSuccess()) {
                break;
            }
            // 修复：直接取首个节点值，避免 all/last 的重复规范路径。
            list.add(r.nodes().get(0).value());
            input = r.remain();
        }
        return list;
    }

    // ==================================================================
    // 对外 API：顶层解析
    // ==================================================================

    /**
     * 解析顶层（单层）TLV，返回该层级下全部节点。语义同 {@link #parseTopLevelTlv(String)}。
     *
     * @param hex 十六进制 TLV 串
     * @return 解析结果快照
     */
    public static TlvResult parse(String hex) {
        return parseTopLevelTlv(hex);
    }

    /**
     * 解析顶层（单层）TLV，不递归。
     *
     * <p>支持 BER-TLV 多字节 Tag 与短/长格式 Length；数据不完整时把未消费部分放入
     * {@link TlvResult#remain()}，不视为失败（{@code failures} 为空）。
     * 输入非法十六进制时返回 {@code failures} 非空的结果。</p>
     *
     * <p>参数说明：</p>
     * <ul>
     *     <li>输入奇数长度时，末尾字符作为“截断残留”并入 {@code remain}</li>
     *     <li>非法字符（非 0-9/a-f/A-F）导致解析失败，失败信息记录在 {@code failures}</li>
     * </ul>
     *
     * @param hexStr 十六进制 TLV 串
     * @return 解析结果快照
     */
    public static TlvResult parseTopLevelTlv(String hexStr) {
        String cleaned = clean(hexStr);
        if (cleaned == null || cleaned.isEmpty()) {
            return TlvResult.empty();
        }

        // 奇数长度：将最后一个字符作为“截断残留”放入 remain
        String truncated = "";
        if (cleaned.length() % 2 != 0) {
            truncated = cleaned.substring(cleaned.length() - 1).toUpperCase(Locale.ROOT);
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        if (cleaned.isEmpty()) {
            return TlvResult.of(Collections.emptyList(), truncated, Collections.emptyList());
        }

        byte[] data;
        try {
            data = HexFormat.of().parseHex(cleaned);
        } catch (IllegalArgumentException e) {
            // 修复：解析失败时 remain 统一大写，保证输出快照一致。
            return TlvResult.failure(e.getMessage(), upper(cleaned) + truncated);
        }
        return parseTopLevelBytes(data, truncated);
    }

    // ==================================================================
    // 对外 API：构建
    // ==================================================================

    /**
     * 构建 TLV 串（Tag + Length + Value）。
     *
     * <p>校验 tag 是否为合法十六进制（偶数长度），并统一转为大写，
     * 保证输出符合“Tag 输出统一大写”的约定。</p>
     *
     * @param tag   Tag（十六进制串，可为空；非空时必须为偶数长度十六进制）
     * @param value Value（十六进制串，可为空；非空时必须为偶数长度十六进制）
     * @return Tag + Length + Value 拼接后的十六进制串
     * @throws IllegalArgumentException tag 非空且不是合法十六进制字符串时抛出
     */
    public static String toTLV(String tag, String value) {
        String cleanTag = upper(clean(tag));
        if (cleanTag == null) {
            cleanTag = "";
        } else if (!cleanTag.isEmpty() && !isEvenHex(cleanTag)) {
            throw new IllegalArgumentException("TLV Tag 必须是长度为偶数的十六进制字符串");
        }
        return cleanTag + buildLengthAndValue(value);
    }

    /**
     * 构建 LV 串（Length + Value，不含 Tag）。
     *
     * @param value Value（十六进制串，可为空；非空时必须为偶数长度十六进制）
     * @return Length + Value 拼接后的十六进制串
     */
    public static String toLV(String value) {
        return buildLengthAndValue(value);
    }

    /**
     * 根据十六进制数据长度计算 BER-TLV 长度字段。
     *
     * <p>校验输入是否为合法十六进制（偶数长度），避免奇数长度时
     * {@code length / 2} 向下取整导致长度字段计算错误。</p>
     *
     * @param hexData 十六进制数据（可为空串，表示长度 0）
     * @return 大写 BER-TLV Length 字段十六进制串
     * @throws IllegalArgumentException 输入非空且不是合法十六进制字符串时抛出
     */
    public static String calculateLengthHex(String hexData) {
        String clean = clean(hexData);
        if (clean == null) {
            clean = "";
        } else if (!clean.isEmpty() && !isEvenHex(clean)) {
            throw new IllegalArgumentException("参数必须是长度为偶数的十六进制字符串");
        }
        StringBuilder sb = new StringBuilder(8);
        appendLengthField(sb, clean.length() / 2L);
        return sb.toString();
    }

    // ==================================================================
    // 对外 API：长度字段解析
    // ==================================================================

    /**
     * 解析 BER-TLV 长度字段。
     *
     * <p>修复说明：原实现使用 {@link Integer#parseInt(String, int)} 解析，
     * 该 API 允许 {@code +} / {@code -} 前缀（如 {@code "+F"} 解析为 15、
     * {@code "-F"} 解析为 -15），导致非合法十六进制输入被错误接受，
     * 甚至产生负长度。现改为逐字节 hex 解析，非法字符直接拒绝，
     * 保证返回值中的 {@code length} 恒为非负。</p>
     *
     * <p>返回值语义：</p>
     * <ul>
     *     <li>{@code startPos}：长度字段结束位置（Value 起始字符索引）</li>
     *     <li>{@code length}：Value 的字节长度（恒非负）</li>
     *     <li>{@code exception}：失败时的异常；成功时为 {@code null}</li>
     * </ul>
     *
     * @param data 以 Length 字段开头的十六进制串（至少 2 个字符）
     * @return 长度解析结果；失败时 {@code exception} 非 {@code null}
     */
    public static LengthParseResult parseLengthField(String data) {
        String clean = clean(data);
        if (clean == null || clean.length() < 2) {
            return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
        }

        int firstByteValue = parseHexByte(clean, 0);
        if (firstByteValue < 0) {
            return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
        }

        // 短格式：单字节长度（最高位为 0），长度即该字节值
        if ((firstByteValue & 0x80) == 0) {
            return new LengthParseResult(2, firstByteValue, null);
        }

        // 长格式：首字节低 7 位为后续长度字节数
        int numBytes = firstByteValue & 0x7F;
        if (numBytes == 0) {
            return new LengthParseResult(0, 0,
                    new UncheckedException("Indefinite length not supported"));
        }
        if (numBytes > MAX_LENGTH_BYTES) {
            return new LengthParseResult(0, 0,
                    new UncheckedException("Length bytes exceed maximum"));
        }

        int required = 2 + numBytes * 2;
        if (clean.length() < required) {
            return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
        }
        int length = 0;
        for (int k = 2; k < required; k += 2) {
            int b = parseHexByte(clean, k);
            if (b < 0) {
                return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
            }
            length = (length << 8) | b;
        }
        return new LengthParseResult(required, length, null);
    }

    // ==================================================================
    // 内部：hex 字节解析
    // ==================================================================

    /**
     * 解析字符串中指定位置的 2 个十六进制字符为一个字节值。
     *
     * <p>仅接受 {@code 0-9}、{@code a-f}、{@code A-F}；不接受 {@code +}、{@code -}、
     * 空格等非十六进制字符，从而规避 {@link Integer#parseInt(String, int)} 的宽松解析问题。</p>
     *
     * <p>修复说明：增加 {@code pos < 0} 边界检查，避免极端情况下越界访问。</p>
     *
     * @param s   源字符串
     * @param pos 起始索引（从 0 开始）
     * @return 字节值（0-255）；输入越界或含非法字符时返回 -1
     */
    private static int parseHexByte(String s, int pos) {
        if (pos < 0 || pos + 2 > s.length()) {
            return -1;
        }
        int hi = Character.digit(s.charAt(pos), 16);
        int lo = Character.digit(s.charAt(pos + 1), 16);
        if (hi < 0 || lo < 0) {
            return -1;
        }
        return (hi << 4) | lo;
    }

    // ==================================================================
    // 内部：构建辅助
    // ==================================================================

    /**
     * 根据 Value 生成 Length + Value（不含 Tag）。
     *
     * <p>Value 会执行 clean + 校验 + 大写化，长度按字节数（hex 字符数 / 2）编码。</p>
     *
     * @param value Value 十六进制串
     * @return Length + Value 拼接后的十六进制串（大写）
     * @throws IllegalArgumentException value 非空且非偶数长度十六进制
     */
    private static String buildLengthAndValue(String value) {
        String clean = clean(value);
        if (clean == null) {
            clean = "";
        } else if (!clean.isEmpty() && !isEvenHex(clean)) {
            throw new IllegalArgumentException("TLV值必须是长度为偶数的十六进制字符串");
        }

        // 修复：Value 输出统一大写，与类级约定及 TlvNode 规范化保持一致。
        clean = clean.toUpperCase(Locale.ROOT);

        StringBuilder sb = new StringBuilder(2 + clean.length());
        appendLengthField(sb, clean.length() / 2L);
        sb.append(clean);
        return sb.toString();
    }

    /**
     * 追加 BER-TLV Length 字段。
     *
     * <p>支持 1/2/3 字节长度字段，超过 {@code MAX_LENGTH_VALUE} 时抛出
     * {@link UncheckedException}，与 {@link #parseLengthField(String)} 的解析上限保持一致。</p>
     *
     * <ul>
     *     <li>{@code byteLen < 0x80}：短格式，1 字节</li>
     *     <li>{@code 0x80 ≤ byteLen ≤ 0xFF}：长格式 {@code 81 XX}</li>
     *     <li>{@code 0x100 ≤ byteLen ≤ 0xFFFF}：长格式 {@code 82 XXXX}</li>
     *     <li>{@code 0x10000 ≤ byteLen ≤ 0xFFFFFF}：长格式 {@code 83 XXXXXX}</li>
     * </ul>
     *
     * @param sb      输出缓冲
     * @param byteLen Value 的字节长度
     */
    private static void appendLengthField(StringBuilder sb, long byteLen) {
        if (byteLen < 0x80) {
            sb.append(toHex(byteLen, 2));
        } else if (byteLen <= 0xFF) {
            sb.append("81").append(toHex(byteLen, 2));
        } else if (byteLen <= 0xFFFF) {
            sb.append("82").append(toHex(byteLen, 4));
        } else if (byteLen <= MAX_LENGTH_VALUE) {
            sb.append("83").append(toHex(byteLen, 6));
        } else {
            throw new UncheckedException("Length exceeds maximum supported range.");
        }
    }

    /**
     * 将 long 值以指定宽度的十六进制格式（大写、左补零）输出。
     *
     * @param value 待格式化数值
     * @param width 输出宽度（字符数）
     * @return 格式化后的十六进制串
     */
    private static String toHex(long value, int width) {
        return String.format("%0" + width + "X", value);
    }

    // ==================================================================
    // 内部：顶层字节流解析
    // ==================================================================

    /**
     * 在已解析为 byte[] 的字节流上按 BER-TLV 规则逐个解析顶层节点。
     *
     * <p>数据不完整（如 Tag/Length/Value 截断）时，未消费部分原样放入
     * {@link TlvResult#remain()}，不视为失败；只有格式不支持（不定长、
     * 长度字节数超上限）才产生 {@code failures}。</p>
     *
     * @param data      已解析的字节流
     * @param truncated 原始输入奇数长度时截断的末尾字符（保持大小写），追加到 remain
     * @return 解析结果快照
     */
    private static TlvResult parseTopLevelBytes(byte[] data, String truncated) {
        List<TlvNode> nodes = new ArrayList<>();
        int i = 0;
        int n = data.length;

        while (i < n) {
            int start = i;

            // --- Tag ---
            byte b = data[i++];
            List<Byte> tagBytes = new ArrayList<>();
            tagBytes.add(b);
            // BER-TLV：首字节低 5 位为 0x1F 表示多字节 Tag
            if ((b & 0x1F) == 0x1F) {
                while (true) {
                    if (i >= n) {
                        // Tag 尚未闭合，剩余数据全部进入 remain
                        return TlvResult.of(nodes, encodeHex(data, start, n) + truncated,
                                Collections.emptyList());
                    }
                    b = data[i++];
                    tagBytes.add(b);
                    // 后续字节最高位为 0 表示 Tag 结束
                    if ((b & 0x80) == 0) {
                        break;
                    }
                }
            }
            byte[] tag = toByteArray(tagBytes);

            // --- Length ---
            if (i >= n) {
                // Length 尚未开始，剩余数据进入 remain
                return TlvResult.of(nodes, encodeHex(data, start, n) + truncated,
                        Collections.emptyList());
            }
            int firstLen = data[i++] & 0xFF;
            long lengthVal;
            if (firstLen < 0x80) {
                lengthVal = firstLen;
            } else {
                int numBytes = firstLen & 0x7F;

                // 修复：区分“不支持的格式”与“数据不完整”。
                // 不定长长度、长度字节数超上限属于格式不支持，应产生 failure；
                // 仅当长度字节数足够但数据不足时，才作为不完整数据放入 remain。
                if (numBytes == 0) {
                    return TlvResult.of(nodes, encodeHex(data, start, n) + truncated,
                            List.of(new TlvFailure(null, "Indefinite length not supported")));
                }
                if (numBytes > MAX_LENGTH_BYTES) {
                    return TlvResult.of(nodes, encodeHex(data, start, n) + truncated,
                            List.of(new TlvFailure(null, "Length bytes exceed maximum")));
                }
                if (numBytes > n - i) {
                    // 长度字节数不足（数据不完整），整体进入 remain
                    return TlvResult.of(nodes, encodeHex(data, start, n) + truncated,
                            Collections.emptyList());
                }
                lengthVal = 0;
                for (int j = 0; j < numBytes; j++) {
                    lengthVal = (lengthVal << 8) | (data[i++] & 0xFF);
                }
            }

            // --- Value ---
            // n - i 为剩余字节数；lengthVal 最大 0xFFFFFF，不会超过 int 范围
            if (lengthVal > n - i) {
                // Value 数据不足，整体进入 remain
                return TlvResult.of(nodes, encodeHex(data, start, n) + truncated,
                        Collections.emptyList());
            }
            int valueLen = (int) lengthVal;
            String tagHex = encodeHex(tag);
            String valHex = encodeHex(data, i, i + valueLen);
            nodes.add(new TlvNode(tagHex, valHex));
            i += valueLen;
        }
        return TlvResult.of(nodes, truncated, Collections.emptyList());
    }

    // ==================================================================
    // 内部：通用辅助
    // ==================================================================

    /**
     * 清除字符串中的空白与不可见字符。
     *
     * @param s 输入串
     * @return 清理后的串；输入 {@code null} 时返回 {@code null}
     */
    private static String clean(String s) {
        return TextUtil.removeWhitespaceAndInvisible(s);
    }

    /**
     * 将字符串 {@code trim} 后转为大写（Locale.ROOT）。
     *
     * @param s 输入串
     * @return 大写串；输入 {@code null} 时返回 {@code null}
     */
    private static String upper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 判断字符串是否为偶数长度的合法十六进制串。
     *
     * <p>空串视为合法（用于允许空 Value / 空 Tag 的构建场景）。
     * 该方法显式检查偶数长度，避免仅依赖 {@code Hex.isHex} 的实现细节。</p>
     *
     * @param s 输入串
     * @return {@code true} 表示合法（偶数长度 hex 或空串）
     */
    private static boolean isEvenHex(String s) {
        if (s == null) {
            return false;
        }
        if (s.isEmpty()) {
            return true;
        }
        return (s.length() & 1) == 0 && Hex.isHex(s);
    }

    /**
     * 将 {@link Byte} 列表转为 {@code byte[]}。
     *
     * @param list Byte 列表
     * @return 对应的原始字节数组
     */
    private static byte[] toByteArray(List<Byte> list) {
        byte[] arr = new byte[list.size()];
        for (int k = 0; k < arr.length; k++) {
            arr[k] = list.get(k);
        }
        return arr;
    }

    /**
     * 将字节数组编码为大写十六进制串。
     */
    private static String encodeHex(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

    /**
     * 将字节数组指定区间编码为大写十六进制串。
     *
     * @param data 字节数组
     * @param from 起始索引（含）
     * @param to   结束索引（不含）
     * @return 大写十六进制串；{@code to <= from} 时返回空串
     */
    private static String encodeHex(byte[] data, int from, int to) {
        if (to <= from) {
            return "";
        }
        return HexFormat.of().withUpperCase().formatHex(data, from, to);
    }

    // ==================================================================
    // 值对象：TlvNode / TlvFailure / LengthParseResult / TlvResult
    // ==================================================================

    /**
     * 单个 TLV 三元组（不可变）。Tag 与 Value 均为大写十六进制串。
     *
     * <p>修复说明：原实现仅做 {@code requireNonNull}，未将 tag/value 规范化为大写。
     * 由于 {@link TlvResult#all(String)} 会将查询 tag 转为大写后严格比较，
     * 用户直接创建小写节点时将无法被匹配。现于 compact constructor 中统一转大写，
     * 与类级约定“输出统一大写”保持一致。</p>
     *
     * <p>进一步修复：构造时同时清除空白与不可见字符，保证手动创建的节点与解析结果
     * 在查询语义上一致。</p>
     *
     * @param tag   Tag 十六进制串（会被 clean + 大写化）
     * @param value Value 十六进制串（会被 clean + 大写化）
     */
    public record TlvNode(String tag, String value) implements Serializable {

        private static final long serialVersionUID = 1L;

        public TlvNode {
            Objects.requireNonNull(tag, "tag");
            Objects.requireNonNull(value, "value");

            // 规范化为大写并清除空白/不可见字符，保证与 TlvResult 查询语义一致
            String normalizedTag = TLV.clean(tag);
            String normalizedValue = TLV.clean(value);
            tag = normalizedTag == null ? "" : normalizedTag.toUpperCase(Locale.ROOT);
            value = normalizedValue == null ? "" : normalizedValue.toUpperCase(Locale.ROOT);
        }

        /**
         * Value 的字节长度。
         *
         * <p>约定 Value 为偶数长度十六进制串；若传入奇数长度（非法），
         * 此处按向下取整返回，调用方应自行保证输入合法性。</p>
         *
         * @return Value 字节长度
         */
        public int byteLength() {
            return value.length() / 2;
        }

        /**
         * 将本节点重新编码为 TLV 串（Tag + Length + Value）。
         *
         * @return 重新编码后的十六进制串
         */
        public String encode() {
            return tag + TLV.calculateLengthHex(value) + value;
        }

        /**
         * Tag 是否为构造类型（BER 首字节 bit 6 为 1）。
         *
         * <p>修复说明：原实现使用 {@link Integer#parseInt(String, int)}，
         * 该 API 会宽松接受 {@code +} / {@code -} 前缀。现改为严格逐字符解析。</p>
         *
         * @return {@code true} 表示构造类型
         */
        public boolean isConstructed() {
            int first = parseHexByte(tag, 0);
            return first >= 0 && (first & 0x20) != 0;
        }

        /**
         * 将 Value 作为下一层 TLV 继续解析。
         *
         * @return 下一层解析结果快照
         */
        public TlvResult children() {
            return TLV.parse(value);
        }
    }

    /**
     * 单点失败描述。用于在“部分成功”场景中表达哪些 Tag 解析失败。
     *
     * @param tag     失败的 Tag（可能为 {@code null}，表示整体数据问题）
     * @param message 失败信息
     * @param cause   原始异常（可空）
     */
    public record TlvFailure(String tag, String message, Exception cause) implements Serializable {

        private static final long serialVersionUID = 1L;

        public TlvFailure(String tag, String message) {
            this(tag, message, null);
        }
    }

    /**
     * BER-TLV 长度字段解析结果。
     *
     * @param startPos  长度字段结束位置（Value 起始索引，以字符为单位）
     * @param length    Value 的字节长度（修复后保证恒为非负）
     * @param exception 解析异常，成功时为 {@code null}
     */
    public record LengthParseResult(int startPos, int length, UncheckedException exception)
            implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 是否解析成功。
         *
         * @return {@code true} 表示成功（{@code exception == null}）
         */
        public boolean isSuccess() {
            return exception == null;
        }
    }

    /**
     * 一次解析/提取操作的只读快照。
     *
     * <p>包含：</p>
     * <ul>
     *     <li>{@link #nodes()}：按出现顺序的节点列表（允许同名 Tag 重复）</li>
     *     <li>{@link #remain()}：未消费的剩余数据</li>
     *     <li>{@link #failures()}：失败点列表（可为空）</li>
     * </ul>
     *
     * <p>状态判断：{@link #isSuccess()} 无失败；{@link #isPartial()} 有成功也有失败；{@link #isEmpty()} 无节点。</p>
     */
    public static final class TlvResult implements Serializable {

        private static final long serialVersionUID = 1L;

        private final List<TlvNode> nodes;
        private final String remain;
        private final List<TlvFailure> failures;

        /**
         * Tag-Value 映射缓存。
         *
         * <p>使用 {@code transient volatile} + 双重检查锁定，保证反序列化后懒加载，
         * 且在并发调用 {@link #getMap()} 时只构建一次。</p>
         */
        private transient volatile LinkedHashMap<String, String> mapCache;

        TlvResult(List<TlvNode> nodes, String remain, List<TlvFailure> failures) {
            this.nodes = nodes == null ? List.of() : List.copyOf(nodes);
            this.remain = remain == null ? "" : remain;
            this.failures = failures == null ? List.of() : List.copyOf(failures);
        }

        // ---- 工厂 ----

        /**
         * 空结果：无节点、无剩余、无失败。
         */
        public static TlvResult empty() {
            return new TlvResult(List.of(), "", List.of());
        }

        /**
         * 通用构造。
         *
         * @param nodes    节点列表（可为 {@code null}，视为空）
         * @param remain   剩余数据（可为 {@code null}，视为空串）
         * @param failures 失败列表（可为 {@code null}，视为空）
         */
        public static TlvResult of(List<TlvNode> nodes, String remain, List<TlvFailure> failures) {
            return new TlvResult(nodes, remain, failures);
        }

        /**
         * 单节点结果（通常用于单 Tag 提取）。
         */
        public static TlvResult single(TlvNode node, String remain) {
            return new TlvResult(List.of(node), remain, List.of());
        }

        /**
         * 失败结果（无节点）。
         *
         * @param message 失败信息
         * @param remain  剩余数据
         */
        public static TlvResult failure(String message, String remain) {
            return new TlvResult(List.of(), remain, List.of(new TlvFailure(null, message)));
        }

        // ---- 节点查询 ----

        /**
         * 节点列表（不可变）。
         */
        public List<TlvNode> nodes() {
            return nodes;
        }

        /**
         * 兼容旧 getter 命名。
         */
        public List<TlvNode> getNodes() {
            return nodes;
        }

        /**
         * 未消费的剩余数据。
         */
        public String remain() {
            return remain;
        }

        /**
         * 兼容旧 getter 命名。
         */
        public String getRemain() {
            return remain;
        }

        /**
         * 失败列表（不可变）。
         */
        public List<TlvFailure> failures() {
            return failures;
        }

        /**
         * 兼容旧 getter 命名。
         */
        public List<TlvFailure> getFailures() {
            return failures;
        }

        /**
         * 返回所有匹配指定 Tag 的节点（保持出现顺序）。
         *
         * <p>修复说明：查询 tag 也需要执行 clean + upper，与 TlvNode 的规范化保持一致；
         * 否则当查询 tag 含空白/不可见字符时无法匹配。</p>
         *
         * @param tag 目标 Tag
         * @return 匹配节点列表；无匹配时返回空列表
         */
        public List<TlvNode> all(String tag) {
            if (StringUtils.isBlank(tag)) {
                return List.of();
            }
            String norm = upper(clean(tag));
            List<TlvNode> result = new ArrayList<>();
            for (TlvNode n : nodes) {
                if (n.tag().equals(norm)) {
                    result.add(n);
                }
            }
            return result;
        }

        /**
         * 首个匹配节点。
         *
         * @param tag 目标 Tag
         * @return 匹配节点；无匹配时返回 {@link Optional#empty()}
         */
        public Optional<TlvNode> first(String tag) {
            List<TlvNode> all = all(tag);
            return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
        }

        /**
         * 最后一个匹配节点。
         *
         * @param tag 目标 Tag
         * @return 匹配节点；无匹配时返回 {@link Optional#empty()}
         */
        public Optional<TlvNode> last(String tag) {
            List<TlvNode> all = all(tag);
            return all.isEmpty() ? Optional.empty() : Optional.of(all.get(all.size() - 1));
        }

        /**
         * 指定 Tag 的最后一个 Value；不存在返回空串。
         *
         * @param tag 目标 Tag
         * @return Value 串；无匹配时返回空串
         */
        public String val(String tag) {
            return last(tag).map(TlvNode::value).orElse("");
        }

        /**
         * 兼容旧 API。
         */
        public String getVal(String tag) {
            return val(tag);
        }

        /**
         * 返回最后一个节点的 Value。
         *
         * @return 最后一个节点的 Value；无节点时返回空串
         */
        public String getVal() {
            return nodes.isEmpty() ? "" : nodes.get(nodes.size() - 1).value();
        }

        /**
         * 是否包含指定 Tag。
         *
         * @param tag 目标 Tag
         * @return {@code true} 表示存在
         */
        public boolean has(String tag) {
            return !StringUtils.isBlank(tag) && !all(tag).isEmpty();
        }

        /**
         * 兼容旧 API。
         */
        public boolean hasTag(String tag) {
            return has(tag);
        }

        /**
         * 进入指定 Tag 的 Value 继续解析（取最后一个匹配节点）。
         *
         * @param tag 目标 Tag
         * @return 下一层解析结果；无匹配时返回 {@link #empty()}
         */
        public TlvResult child(String tag) {
            return last(tag).map(TlvNode::children).orElse(empty());
        }

        /**
         * 返回 Tag-Value 映射视图。
         * 同名 Tag 保留最后一次出现的 Value；返回的是副本。
         *
         * @return 不可变副本的 {@link LinkedHashMap}
         */
        public LinkedHashMap<String, String> getMap() {
            LinkedHashMap<String, String> cache = mapCache;
            if (cache == null) {
                synchronized (this) {
                    cache = mapCache;
                    if (cache == null) {
                        LinkedHashMap<String, String> m = new LinkedHashMap<>();
                        for (TlvNode n : nodes) {
                            m.put(n.tag(), n.value());
                        }
                        cache = m;
                        mapCache = m;
                    }
                }
            }
            return new LinkedHashMap<>(cache);
        }

        /**
         * 兼容旧 API。
         */
        public LinkedHashMap<String, String> getResults() {
            return getMap();
        }

        /**
         * 返回第一个失败包装成的异常；无失败返回 {@code null}。
         *
         * <p>修复说明：原实现直接 {@code new UncheckedException(f.message())}，
         * 当 message 为 null 时异常信息为空。现做 null 兜底，便于问题排查。</p>
         *
         * @return 首个失败的异常；无失败返回 {@code null}
         */
        public Exception getException() {
            if (failures.isEmpty()) {
                return null;
            }
            TlvFailure f = failures.get(0);
            if (f.cause() != null) {
                return f.cause();
            }
            String msg = f.message() != null ? f.message() : "Unknown TLV error";
            return new UncheckedException(msg);
        }

        /**
         * 第一个失败的消息；无失败返回空串。
         *
         * <p>修复说明：失败消息可能为 {@code null}，原实现会直接返回 null，
         * 违反“无失败返回空串”的契约。现统一兜底为空串。</p>
         *
         * @return 首个失败消息；无失败或消息为 {@code null} 时返回空串
         */
        public String failureMessage() {
            if (failures.isEmpty()) {
                return "";
            }
            String msg = failures.get(0).message();
            return msg != null ? msg : "";
        }

        /**
         * 无任何失败。
         */
        public boolean isSuccess() {
            return failures.isEmpty();
        }

        /**
         * 既有成功节点又有失败点。
         */
        public boolean isPartial() {
            return !nodes.isEmpty() && !failures.isEmpty();
        }

        /**
         * 无任何节点。
         */
        public boolean isEmpty() {
            return nodes.isEmpty();
        }

        /**
         * 节点数量。
         */
        public int size() {
            return nodes.size();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TlvResult)) {
                return false;
            }
            TlvResult that = (TlvResult) o;
            return nodes.equals(that.nodes)
                    && remain.equals(that.remain)
                    && failures.equals(that.failures);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodes, remain, failures);
        }

        @Override
        public String toString() {
            return "TlvResult{nodes=" + nodes + ", remain='" + remain + "', failures=" + failures + '}';
        }
    }
}