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
 *     <li>{@link TlvFailure}：单点失败描述（不阻断整体流程，用于表达"部分成功"）</li>
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


    /**
     * 提取指定 tag 的 value；失败或未找到返回空字符串。
     */
    public static String extractVal(String tag, String tlv) {
        TlvResult r = extract(tag, tlv);
        return r.isSuccess() ? r.val(tag) : "";
    }

    /**
     * 提取指定 tag 的完整结果（成功时含 1 个节点）。
     *
     * <p>注意：本方法采用<b>字符串前缀匹配</b>，要求 {@code tlv} 以 {@code tag} 字符串开头。
     * 因此当 tag 是另一个 tag 的前缀时（如 {@code "01"} 与 {@code "0102"}），
     * 可能提取到非预期的节点。需要严格 BER-TLV 语义时请使用 {@link #parseTopLevelTlv(String)}。</p>
     */
    public static TlvResult extract(String tag, String tlv) {
        String cleanTlv = clean(tlv);
        String cleanTag = clean(tag);
        if (cleanTag == null || cleanTag.isEmpty() || cleanTlv == null || cleanTlv.isEmpty()) {
            return TlvResult.failure("illegal data", cleanTlv);
        }
        if (!Hex.isHex(cleanTlv) || !Hex.isHex(cleanTag)) {
            return TlvResult.failure("illegal data", cleanTlv);
        }
        if (!cleanTlv.regionMatches(true, 0, cleanTag, 0, cleanTag.length())) {
            return TlvResult.failure(
                    String.format("The data does not start with TAG %s", cleanTag), cleanTlv);
        }

        String tail = cleanTlv.substring(cleanTag.length());
        if (tail.length() < 2) {
            return TlvResult.failure("Not valid TLV data", cleanTlv);
        }

        LengthParseResult lp = parseLengthField(tail);
        if (!lp.isSuccess()) {
            return TlvResult.failure(lp.exception().getMessage(), cleanTlv);
        }

        // 使用 long 计算尾部位置，避免 length * 2 在 int 范围溢出
        long valueEnd = lp.startPos() + (long) lp.length() * 2;
        if (valueEnd > tail.length()) {
            return TlvResult.failure("Not valid TLV data", cleanTlv);
        }

        int end = (int) valueEnd;
        TlvNode node = new TlvNode(upper(cleanTag), upper(tail.substring(lp.startPos(), end)));
        return TlvResult.single(node, tail.substring(end));
    }


    /**
     * 在同一层级依次提取多个 Tag。
     *
     * <p>关键语义：<b>单个 Tag 提取失败不中断、不推进游标</b>，继续尝试下一个 Tag；
     * 失败点记录在 {@link TlvResult#failures()} 中，成功的节点记录在 {@link TlvResult#nodes()} 中。
     * 当所有 Tag 都成功时 {@link TlvResult#isSuccess()} 为 {@code true}，
     * 部分失败时为 {@code false}（但 {@link TlvResult#isPartial()} 可识别"有成功也有失败"）。</p>
     */
    public static TlvResult extractValsOnSameLevel(String tlv, String... tags) {
        String cleanTlv = clean(tlv);
        if (cleanTlv == null || !Hex.isHex(cleanTlv)) {
            return TlvResult.failure("illegal data", cleanTlv);
        }
        if (tags == null || tags.length == 0) {
            return TlvResult.failure("no tags provided", cleanTlv);
        }

        List<TlvNode> nodes = new ArrayList<>();
        List<TlvFailure> failures = new ArrayList<>();
        String input = cleanTlv;
        String remain = cleanTlv;

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
     */
    public static TlvResult extractValsRecursive(String tlv, String... tags) {
        String cleanTlv = clean(tlv);
        if (cleanTlv == null || !Hex.isHex(cleanTlv)) {
            return TlvResult.failure("illegal data", cleanTlv);
        }
        if (tags == null || tags.length == 0) {
            return TlvResult.failure("no tags provided", cleanTlv);
        }

        List<TlvNode> nodes = new ArrayList<>();
        List<TlvFailure> failures = new ArrayList<>();
        String input = cleanTlv;
        String remain = cleanTlv;

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
     */
    public static List<String> extractValsLoop(String tag, String tlv) {
        String cleanTlv = clean(tlv);
        String cleanTag = clean(tag);
        if (cleanTlv == null || cleanTag == null
                || !Hex.isHex(cleanTlv) || !Hex.isHex(cleanTag)) {
            return new ArrayList<>();
        }

        List<String> list = new ArrayList<>();
        String input = cleanTlv;
        while (StringUtils.isNotBlank(input)) {
            TlvResult r = extract(cleanTag, input);
            if (!r.isSuccess()) {
                break;
            }
            list.add(r.val(cleanTag));
            input = r.remain();
        }
        return list;
    }


    /**
     * 解析顶层（单层）TLV，返回该层级下全部节点。语义同 {@link #parseTopLevelTlv(String)}。
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
     */
    public static TlvResult parseTopLevelTlv(String hexStr) {
        String clean = clean(hexStr);
        if (clean == null || clean.isEmpty()) {
            return TlvResult.empty();
        }

        // 奇数长度：将最后一个字符作为"截断残留"放入 remain
        String truncated = "";
        if (clean.length() % 2 != 0) {
            truncated = clean.substring(clean.length() - 1).toUpperCase(Locale.ROOT);
            clean = clean.substring(0, clean.length() - 1);
        }
        if (clean.isEmpty()) {
            return TlvResult.of(Collections.emptyList(), truncated, Collections.emptyList());
        }

        byte[] data;
        try {
            data = HexFormat.of().parseHex(clean);
        } catch (IllegalArgumentException e) {
            return TlvResult.failure(e.getMessage(), truncated);
        }
        return parseTopLevelBytes(data, truncated);
    }


    /**
     * 构建 TLV 串（Tag + Length + Value）。
     *
     * <p>校验 tag 是否为合法十六进制（偶数长度），并统一转为大写，
     * 保证输出符合"Tag 输出统一大写"的约定。</p>
     *
     * @throws IllegalArgumentException tag 非空且不是合法十六进制字符串时抛出
     */
    public static String toTLV(String tag, String value) {
        String cleanTag = upper(clean(tag));
        if (cleanTag == null) {
            cleanTag = "";
        } else if (!cleanTag.isEmpty() && !Hex.isHex(cleanTag)) {
            throw new IllegalArgumentException("TLV Tag 必须是长度为偶数的十六进制字符串");
        }
        return cleanTag + buildLengthAndValue(value);
    }

    /**
     * 构建 LV 串（Length + Value，不含 Tag）。
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
     * @throws IllegalArgumentException 输入非空且不是合法十六进制字符串时抛出
     */
    public static String calculateLengthHex(String hexData) {
        String clean = clean(hexData);
        if (clean == null) {
            clean = "";
        } else if (!clean.isEmpty() && !Hex.isHex(clean)) {
            throw new IllegalArgumentException("参数必须是长度为偶数的十六进制字符串");
        }
        StringBuilder sb = new StringBuilder(8);
        appendLengthField(sb, clean.length() / 2L);
        return sb.toString();
    }


    /**
     * 解析 BER-TLV 长度字段。
     *
     * <p>修复说明：原实现使用 {@link Integer#parseInt(String, int)} 解析，
     * 该 API 允许 {@code +} / {@code -} 前缀（如 {@code "+F"} 解析为 15、
     * {@code "-F"} 解析为 -15），导致非合法十六进制输入被错误接受，
     * 甚至产生负长度。现改为逐字节 hex 解析，非法字符直接拒绝，
     * 保证返回值中的 {@code length} 恒为非负。</p>
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

    /**
     * 解析字符串中指定位置的 2 个十六进制字符为一个字节值。
     *
     * <p>仅接受 {@code 0-9}、{@code a-f}、{@code A-F}；不接受 {@code +}、{@code -}、
     * 空格等非十六进制字符，从而规避 {@link Integer#parseInt(String, int)} 的宽松解析问题。</p>
     *
     * @param s   源字符串
     * @param pos 起始索引（从 0 开始）
     * @return 字节值（0-255）；输入越界或含非法字符时返回 -1
     */
    private static int parseHexByte(String s, int pos) {
        if (pos + 2 > s.length()) {
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

    private static String buildLengthAndValue(String value) {
        String clean = clean(value);
        if (clean == null) {
            clean = "";
        } else if (!clean.isEmpty() && !Hex.isHex(clean)) {
            throw new IllegalArgumentException("TLV值必须是长度为偶数的十六进制字符串");
        }

        StringBuilder sb = new StringBuilder(2 + clean.length());
        appendLengthField(sb, clean.length() / 2L);
        sb.append(clean);
        return sb.toString();
    }

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

    private static String toHex(long value, int width) {
        return String.format("%0" + width + "X", value);
    }

    // ==================================================================
    // 内部：顶层字节流解析
    // ==================================================================

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
                return TlvResult.of(nodes, encodeHex(data, start, n) + truncated,
                        Collections.emptyList());
            }
            int firstLen = data[i++] & 0xFF;
            long lengthVal;
            if (firstLen < 0x80) {
                lengthVal = firstLen;
            } else {
                int numBytes = firstLen & 0x7F;
                // 修复说明：
                // 原实现使用 i + numBytes > n 判断越界，当 i 接近 Integer.MAX_VALUE 时
                // i + numBytes 可能溢出为负数，绕过越界检查。改为 numBytes > n - i，
                // 同时保留 numBytes == 0（不定长）与 numBytes > MAX_LENGTH_BYTES 上限检查，
                // 与 parseLengthField 保持一致，防止 lengthVal 累加溢出。
                if (numBytes == 0 || numBytes > MAX_LENGTH_BYTES || numBytes > n - i) {
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

    private static String clean(String s) {
        return TextUtil.removeWhitespaceAndInvisible(s);
    }

    private static String upper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static byte[] toByteArray(List<Byte> list) {
        byte[] arr = new byte[list.size()];
        for (int k = 0; k < arr.length; k++) {
            arr[k] = list.get(k);
        }
        return arr;
    }

    private static String encodeHex(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

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
     * 与类级约定"输出统一大写"保持一致。</p>
     */
    public record TlvNode(String tag, String value) implements Serializable {

        private static final long serialVersionUID = 1L;

        public TlvNode {
            Objects.requireNonNull(tag, "tag");
            Objects.requireNonNull(value, "value");
            // 规范化为大写，保证与 TlvResult 查询语义一致
            tag = tag.toUpperCase(Locale.ROOT);
            value = value.toUpperCase(Locale.ROOT);
        }

        /**
         * Value 的字节长度。
         *
         * <p>约定 Value 为偶数长度十六进制串；若传入奇数长度（非法），
         * 此处按向下取整返回，调用方应自行保证输入合法性。</p>
         */
        public int byteLength() {
            return value.length() / 2;
        }

        /**
         * 将本节点重新编码为 TLV 串（Tag + Length + Value）。
         */
        public String encode() {
            return tag + TLV.calculateLengthHex(value) + value;
        }

        /**
         * Tag 是否为构造类型（BER 首字节 bit 6 为 1）。
         */
        public boolean isConstructed() {
            if (tag.length() < 2) {
                return false;
            }
            try {
                return (Integer.parseInt(tag.substring(0, 2), 16) & 0x20) != 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        /**
         * 将 Value 作为下一层 TLV 继续解析。
         */
        public TlvResult children() {
            return TLV.parse(value);
        }
    }

    /**
     * 单点失败描述。用于在"部分成功"场景中表达哪些 Tag 解析失败。
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

        public static TlvResult empty() {
            return new TlvResult(List.of(), "", List.of());
        }

        public static TlvResult of(List<TlvNode> nodes, String remain, List<TlvFailure> failures) {
            return new TlvResult(nodes, remain, failures);
        }

        public static TlvResult single(TlvNode node, String remain) {
            return new TlvResult(List.of(node), remain, List.of());
        }

        public static TlvResult failure(String message, String remain) {
            return new TlvResult(List.of(), remain, List.of(new TlvFailure(null, message)));
        }

        // ---- 节点查询 ----

        public List<TlvNode> nodes() {
            return nodes;
        }

        /**
         * 兼容旧 getter 命名。
         */
        public List<TlvNode> getNodes() {
            return nodes;
        }

        public String remain() {
            return remain;
        }

        public String getRemain() {
            return remain;
        }

        public List<TlvFailure> failures() {
            return failures;
        }

        public List<TlvFailure> getFailures() {
            return failures;
        }

        /**
         * 返回所有匹配指定 Tag 的节点（保持出现顺序）。
         */
        public List<TlvNode> all(String tag) {
            if (StringUtils.isBlank(tag)) {
                return List.of();
            }
            String norm = upper(tag);
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
         */
        public Optional<TlvNode> first(String tag) {
            List<TlvNode> all = all(tag);
            return all.isEmpty() ? Optional.empty() : Optional.of(all.get(0));
        }

        /**
         * 最后一个匹配节点。
         */
        public Optional<TlvNode> last(String tag) {
            List<TlvNode> all = all(tag);
            return all.isEmpty() ? Optional.empty() : Optional.of(all.get(all.size() - 1));
        }

        /**
         * 指定 Tag 的最后一个 Value；不存在返回空串。
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
         */
        public String getVal() {
            return nodes.isEmpty() ? "" : nodes.get(nodes.size() - 1).value();
        }

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
         */
        public TlvResult child(String tag) {
            return last(tag).map(TlvNode::children).orElse(empty());
        }

        /**
         * 返回 Tag-Value 映射视图。
         * 同名 Tag 保留最后一次出现的 Value；返回的是副本。
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
         */
        public String failureMessage() {
            return failures.isEmpty() ? "" : failures.get(0).message();
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