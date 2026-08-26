package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.text.TextUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>TLV解析工具类</p>
 * <p>提供TLV（Tag-Length-Value）格式数据的解析和构建工具方法。</p>
 * <p>特性：</p>
 * <ul>
 * <li>从TLV字符串中提取指定标签的值</li>
 * <li>支持同一层级多个标签的提取</li>
 * <li>支持嵌套层级的递归提取</li>
 * <li>支持循环提取相同标签的所有值</li>
 * <li>提供TLV格式构建方法</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TLV {
    /**
     * 从TLV字符串中提取指定tag的value
     *
     * @param tag 目标tag
     * @param tlv TLV字符串
     * @return 找到的value，未找到或出错则返回空字符串
     */
    public static String extractVal(String tag, String tlv) {
        TlvResult result = extract(tag, tlv);
        if (null != result.getException()) {
            return "";
        }
        String val = result.getVal(tag);
        return val != null ? val : "";
    }

    /**
     * 从TLV字符串中提取指定标签的数据
     * 支持BER-TLV编码格式，包括短格式和长格式的长度字段
     *
     * @param tag 目标标签，应为十六进制字符串
     * @param tlv TLV格式的十六进制字符串
     * @return TlvResult对象，包含提取的值、剩余数据和可能的异常信息
     */
    public static TlvResult extract(String tag, String tlv) {
        if (tag == null || tlv == null || tlv.isEmpty()) {
            return new TlvResult().setException(new UncheckedException("illegal data"));
        }

        tag = TextUtil.removeWhitespaceAndInvisible(tag);
        tlv = TextUtil.removeWhitespaceAndInvisible(tlv);

        if (tag.isEmpty() || !Hex.isHex(tlv) || !Hex.isHex(tag)) {
            return new TlvResult().setException(new UncheckedException("illegal data")).setRemain(tlv);
        }
        if (!tlv.startsWith(tag)) {
            return new TlvResult().setException(new UncheckedException(
                    String.format("The data does not start with TAG %s", tag))).setRemain(tlv);
        }

        String remain = tlv.substring(tag.length());
        if (remain.length() < 2) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data")).setRemain(tlv);
        }

        // 解析长度字段
        LengthParseResult lengthResult = parseLengthField(remain);
        if (lengthResult.exception != null) {
            return new TlvResult().setException(lengthResult.exception).setRemain(tlv);
        }

        int startPos = lengthResult.startPos;
        int length = lengthResult.length;

        // 允许长度为0（空值）
        if (length < 0) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data")).setRemain(tlv);
        }

        int valueEndPos = startPos + length * 2;
        if (valueEndPos > remain.length()) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data")).setRemain(tlv);
        }

        String val = remain.substring(startPos, valueEndPos);
        return new TlvResult().putVal(tag, val).setRemain(remain.substring(valueEndPos));
    }

    /**
     * 解析BER-TLV编码格式的长度字段
     * <p>根据BER-TLV编码标准，长度字段分为两种格式：</p>
     * <ul>
     * <li>短格式：最高位为0，长度值直接由该字节表示（0-127字节），长度字段占1字节</li>
     * <li>长格式：最高位为1，低7位表示后续长度字节数，实际长度由后续字节共同表示</li>
     * </ul>
     * <p>支持的最大长度字节数为3字节，不支持不定长格式。</p>
     *
     * @param data 待解析的十六进制字符串数据，从长度字段起始位置开始
     * @return LengthParseResult对象，包含：
     * <ul>
     * <li>startPos: 长度字段结束位置（即值字段开始位置的索引，以字符为单位）</li>
     * <li>length: 解析得到的实际数据长度（以字节为单位）</li>
     * <li>exception: 解析过程中产生的异常，成功时为null</li>
     * </ul>
     */
    public static LengthParseResult parseLengthField(String data) {
        if (data == null || data.length() < 2) {
            return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
        }

        String firstByte = data.substring(0, 2);
        int firstByteValue;

        try {
            firstByteValue = Integer.parseInt(firstByte, 16);
        } catch (NumberFormatException e) {
            return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
        }

        // BER-TLV标准：最高位为0表示短格式，为1表示长格式
        if ((firstByteValue & 0x80) == 0) {
            // 短格式：直接表示长度（0-127），长度字段占1字节（2个十六进制字符）
            return new LengthParseResult(2, firstByteValue, null);
        } else {
            // 长格式：低7位表示后续长度字节数
            int numLengthBytes = firstByteValue & 0x7F;

            if (numLengthBytes == 0) {
                //  indefinite length，不支持
                return new LengthParseResult(0, 0, new UncheckedException("Indefinite length not supported"));
            }

            if (numLengthBytes > 3) {
                return new LengthParseResult(0, 0, new UncheckedException("Length bytes exceed maximum"));
            }

            int requiredLength = 2 + numLengthBytes * 2;
            if (data.length() < requiredLength) {
                return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
            }

            String lengthStr = data.substring(2, requiredLength);
            int length;

            try {
                length = Integer.parseInt(lengthStr, 16);
            } catch (NumberFormatException e) {
                return new LengthParseResult(0, 0, new UncheckedException("Not valid TLV data"));
            }

            // startPos是长度字段的总字节数（包括前缀字节）
            return new LengthParseResult(requiredLength, length, null);
        }
    }

    /**
     * 长度解析结果内部类
     */
    private static class LengthParseResult {
        final int startPos;  // 长度字段结束位置（即值开始位置）
        final int length;    // 值的长度（字节数）
        final UncheckedException exception;

        LengthParseResult(int startPos, int length, UncheckedException exception) {
            this.startPos = startPos;
            this.length = length;
            this.exception = exception;
        }
    }


    /**
     * 在同一层级依次提取多个标签的值
     * 适用于扁平结构的TLV数据，按顺序从剩余数据中提取每个标签
     *
     * @param tlv  TLV格式的十六进制字符串
     * @param tags 要提取的标签列表，按顺序匹配
     * @return TlvResult对象，包含所有成功提取的标签-值对和可能的异常信息
     */
    public static TlvResult extractValsOnSameLevel(String tlv, String... tags) {
        tlv = TextUtil.removeWhitespaceAndInvisible(tlv);
        if (!Hex.isHex(tlv)) {
            return new TlvResult().setException(new UncheckedException("illegal data")).setRemain(tlv);
        }
        if (null == tags || tags.length == 0) {
            return new TlvResult().setException(new UncheckedException("no tags provided")).setRemain(tlv);
        }

        // 依次提取每个标签的值，每次从剩余数据中继续解析
        TlvResult tlvResult = new TlvResult().setRemain(tlv);
        String input = tlv;
        for (String tag : tags) {
            TlvResult result = extract(tag, input);
            if (result.isSuccess()) {
                tlvResult.putVal(tag, result.getVal(tag));
            }
            input = result.getRemain();
            tlvResult.setRemain(result.getRemain());
        }
        return tlvResult;
    }

    /**
     * 递归提取嵌套层级中的多个标签的值
     * 适用于嵌套结构的TLV数据，每个标签的值作为下一层级的输入继续解析
     *
     * @param tlv  TLV格式的十六进制字符串
     * @param tags 要递归提取的标签列表，按层级顺序匹配
     * @return TlvResult对象，包含所有层级提取的标签-值对和可能的异常信息
     */
    public static TlvResult extractValsRecursive(String tlv, String... tags) {
        tlv = TextUtil.removeWhitespaceAndInvisible(tlv);
        if (!Hex.isHex(tlv)) {
            return new TlvResult().setException(new UncheckedException("illegal data"));
        }
        if (null == tags || tags.length == 0) {
            return new TlvResult().setException(new UncheckedException("no tags provided"));
        }

        // 递归提取嵌套标签，每次将当前标签的值作为下一层的输入
        TlvResult tlvResult = new TlvResult().setRemain(tlv);
        String input = tlv;
        for (String tag : tags) {
            TlvResult result = extract(tag, input);
            if (null != result.getException()) {
                tlvResult.setException(result.getException());
                break;
            }
            String val = result.getVal(tag);
            input = val;
            tlvResult.putVal(tag, val).setRemain(result.getRemain());
        }
        return tlvResult;
    }

    /**
     * 循环提取相同标签的所有值
     * 适用于包含多个相同标签的TLV数据，持续解析直到没有更多匹配项或发生错误
     *
     * @param tag 目标标签，应为十六进制字符串
     * @param tlv TLV格式的十六进制字符串
     * @return 包含所有匹配值的列表，如果发生错误则返回已收集的列表【不包含TAG】
     */
    public static List<String> extractValsLoop(String tag, String tlv) {
        tlv = TextUtil.removeWhitespaceAndInvisible(tlv);
        if (!Hex.isHex(tlv) || !Hex.isHex(tag)) {
            return new ArrayList<>();
        }

        List<String> list = new ArrayList<>();
        String input = tlv;

        while (StringUtils.isNotBlank(input)) {
            TlvResult result = extract(tag, input);
            if (result.getException() != null) {
                // 发生错误时返回已收集的结果
                break;
            }
            list.add(result.getVal(tag));
            input = result.getRemain();
        }

        return list;
    }

    /**
     * 将给定的标签和输入字符串转换为标签-长度-值（TLV）格式的字符串。
     * 该函数根据输入字符串的长度（以字节为单位）选择合适的长度编码方式。
     *
     * @param tag   表示 TLV 格式中的标签部分，为字符串类型
     * @param value 表示 TLV 格式中的值部分，应为十六进制字符串，每两个字符代表一个字节
     * @return 返回一个符合 TLV 格式的字符串
     */
    public static String toTLV(String tag, String value) {
        tag = TextUtil.removeWhitespaceAndInvisible(tag);
        value = TextUtil.removeWhitespaceAndInvisible(value);
        if (!Hex.isHex(value) && StringUtils.isNotBlank(value)) {
            return tag;
        }

        // 计算输入字符串的字节长度，由于输入是十六进制字符串，每两个字符代表一个字节
        int inputLen = value.length() / 2;
        StringBuilder sb = new StringBuilder();

        // 将输入长度转换为十六进制字符串
        String strInputLenString = toHex(String.valueOf(inputLen));

        if (inputLen > 65535) {
            // 当输入长度大于 65535 时，使用标签 83 表示长度为 3 字节
            sb.append("83");
            sb.append(strInputLenString);
            sb.append(value);
        } else if (inputLen > 255) {
            // 当输入长度大于 255 且不超过 65535 时，使用标签 82 表示长度为 2 字节
            sb.append("82");
            sb.append(strInputLenString);
            sb.append(value);
        } else if (inputLen > 127) {
            // 当输入长度大于 127 且不超过 255 时，使用标签 81 表示长度为 1 字节
            sb.append("81");
            sb.append(strInputLenString);
            sb.append(value);
        } else {
            // 当输入长度不超过 127 时，直接使用长度值
            sb.append(strInputLenString);
            sb.append(value);
        }

        // 将标签与构建好的长度和值部分拼接起来
        return tag + sb;
    }

    /**
     * 将数字字符串转换为十六进制表示
     *
     * @param num 数字字符串
     * @return 十六进制表示的字符串
     */
    private static String toHex(String num) {
        num = TextUtil.removeWhitespaceAndInvisible(num);
        if (num == null || num.isEmpty()) {
            return "00";
        }

        long value;
        try {
            value = Long.parseLong(num);
        } catch (NumberFormatException e) {
            return "00";
        }

        if (value < 0) {
            return "00";
        }

        // 直接使用数值计算，避免字符串操作
        if (value <= 0xFF) {
            return String.format("%02X", value);
        } else if (value <= 0xFFFF) {
            return String.format("%04X", value);
        } else if (value <= 0xFFFFFF) {
            return String.format("%06X", value);
        } else {
            return String.format("%08X", value);
        }
    }


    /**
     * 根据输入的十六进制数据计算其TLV长度字段值
     * 长度字段格式遵循BER-TLV编码规则：
     * - 长度 < 128: 单字节表示 (00-7F)
     * - 128 ≤ 长度 ≤ 255: 81前缀 + 1字节长度
     * - 256 ≤ 长度 ≤ 65535: 82前缀 + 2字节长度
     * - 65536 ≤ 长度 ≤ 16777215: 83前缀 + 3字节长度
     *
     * @param hexData 输入的十六进制数据字符串（可包含不可见字符，如空格、换行等）
     * @return 计算得到的长度字段十六进制值，超出范围抛出异常
     */
    public static String calculateLengthHex(String hexData) {
        if (hexData == null || hexData.isEmpty()) {
            return "00";
        }

        // 移除不可见字符（非十六进制字符）
        String cleanHex = TextUtil.removeWhitespaceAndInvisible(hexData);
        // 计算数据长度（以字节为单位）
        int byteLength = cleanHex.length() / 2;

        if (byteLength < 128) {
            // 短格式：单字节直接表示长度
            return toHexString(byteLength);
        } else if (byteLength <= 255) {
            // 长格式1：81前缀 + 1字节长度
            return "81" + toHexString(byteLength);
        } else if (byteLength <= 65535) {
            // 长格式2：82前缀 + 2字节长度
            return "82" + toHexStringWithPadding(byteLength, 4);
        } else if (byteLength <= 16777215) {
            // 长格式3：83前缀 + 3字节长度
            return "83" + toHexStringWithPadding(byteLength, 6);
        } else {
            // 长度超过最大支持范围
            throw new UncheckedException("Length exceeds maximum supported range.");
        }
    }

    /**
     * 将整数转换为两位十六进制字符串
     *
     * @param n 整数（0-255）
     * @return 两位大写十六进制字符串
     */
    private static String toHexString(int n) {
        return String.format("%02X", n);
    }

    /**
     * 将整数转换为指定长度的十六进制字符串，不足左侧补0
     *
     * @param n      要转换的整数
     * @param length 目标字符串长度（字符数）
     * @return 指定长度的十六进制字符串
     */
    private static String toHexStringWithPadding(int n, int length) {
        String format = "%0" + length + "X";
        return String.format(format, n);
    }

    /**
     * <p>解析顶层（单层）TLV数据，返回该层级下所有标签-值对</p>
     * <p>本方法不会递归解析嵌套的TLV，仅解析当前层级的标签-值对。</p>
     * <p>解析规则：</p>
     * <ul>
     * <li>输入为十六进制字符串，可包含空白字符（解析前会被清除）</li>
     * <li>支持BER-TLV编码：Tag可为多字节（首字节低5位为0x1F时继续读取后续字节），Length支持短格式（&lt;0x80）和长格式（0x80|numBytes）</li>
     * <li>输入长度为奇数时，最后一个字符会被截断并放入remain中</li>
     * <li>解析遇到数据不完整（Tag/Length/Value缺失或长度超出范围）时，将未解析的完整数据放入remain</li>
     * <li>输入不是合法十六进制时，结果中将携带异常信息（exception）</li>
     * </ul>
     *
     * @param hexStr TLV格式的十六进制字符串，允许包含空白字符
     * @return TlvResult对象，包含：
     * <ul>
     * <li>map：解析出的顶层标签-值对（键为Tag的十六进制，值为Value的十六进制）</li>
     * <li>remain：未解析的剩余数据（含奇数长度截断的字符）</li>
     * <li>exception：解析异常，无异常时为null</li>
     * </ul>
     */
    public static TlvResult parseTopLevelTlv(String hexStr) {
        TlvResult result = new TlvResult();

        // 1. 清洗空白字符
        String clean = TextUtil.removeWhitespaceAndInvisible(hexStr);
        if (clean.isEmpty()) {
            return result; // 空输入，remain 为空
        }

        // 2. 处理奇数长度：截断最后一个字符，保存到 truncated
        String truncated = "";
        if (clean.length() % 2 != 0) {
            truncated = clean.substring(clean.length() - 1).toUpperCase(); // 转为大写
            clean = clean.substring(0, clean.length() - 1);                // 保留偶数部分
        }

        // 如果偶数部分也为空（即原始输入只有一个字符），直接返回剩余
        if (clean.isEmpty()) {
            result.setRemain(truncated);
            return result;
        }

        // 3. 解码为字节数组
        byte[] data;
        try {
            data = HexFormat.of().parseHex(clean);
        } catch (IllegalArgumentException e) {
            result.setException(e);
            result.setRemain(truncated);
            return result;
        }

        LinkedHashMap<String, String> map = result.getMap();
        int i = 0;
        int n = data.length;

        while (i < n) {
            int start = i;

            // --- 解析 Tag ---
            if (i >= n) {
                // 剩余数据 = 未解析的字节 + 截断字符
                String remainHex = encodeHexUpper(data, start, n);
                result.setRemain(remainHex + truncated);
                return result;
            }
            byte b = data[i++];
            java.util.List<Byte> tagList = new java.util.ArrayList<>();
            tagList.add(b);
            if ((b & 0x1F) == 0x1F) {
                while (true) {
                    if (i >= n) {
                        String remainHex = encodeHexUpper(data, start, n);
                        result.setRemain(remainHex + truncated);
                        return result;
                    }
                    b = data[i++];
                    tagList.add(b);
                    if ((b & 0x80) == 0) break;
                }
            }
            byte[] tag = new byte[tagList.size()];
            for (int idx = 0; idx < tag.length; idx++) {
                tag[idx] = tagList.get(idx);
            }

            // --- 解析 Length ---
            if (i >= n) {
                String remainHex = encodeHexUpper(data, start, n);
                result.setRemain(remainHex + truncated);
                return result;
            }
            int firstLen = data[i] & 0xFF;
            i++;
            long lengthVal;
            if (firstLen < 0x80) {
                lengthVal = firstLen;
            } else {
                int numBytes = firstLen & 0x7F;
                if (numBytes == 0) {
                    String remainHex = encodeHexUpper(data, start, n);
                    result.setRemain(remainHex + truncated);
                    return result;
                }
                if (i + numBytes > n) {
                    String remainHex = encodeHexUpper(data, start, n);
                    result.setRemain(remainHex + truncated);
                    return result;
                }
                lengthVal = 0;
                for (int j = 0; j < numBytes; j++) {
                    lengthVal = (lengthVal << 8) | (data[i] & 0xFF);
                    i++;
                }
            }

            // --- 检查 Value 完整性 ---
            if (lengthVal > n - i) {
                String remainHex = encodeHexUpper(data, start, n);
                result.setRemain(remainHex + truncated);
                return result;
            }

            // --- 读取 Value ---
            int valueLen = (int) lengthVal;
            byte[] value = new byte[valueLen];
            System.arraycopy(data, i, value, 0, valueLen);
            i += valueLen;

            // 存入 map
            String tagHex = encodeHexUpper(tag);
            String valHex = encodeHexUpper(value);
            map.put(tagHex, valHex);
        }

        // 全部解析完成，剩余数据仅为截断的字符（如果有）
        result.setRemain(truncated);
        return result;
    }

    /**
     * 将字节数组编码为大写十六进制字符串
     */
    private static String encodeHexUpper(byte[] bytes) {
        return HexFormat.of().withUpperCase().formatHex(bytes);
    }

    /**
     * 将字节数组的指定范围编码为大写十六进制字符串
     */
    private static String encodeHexUpper(byte[] data, int from, int to) {
        int len = to - from;
        if (len <= 0) return "";
        byte[] slice = new byte[len];
        System.arraycopy(data, from, slice, 0, len);
        return encodeHexUpper(slice);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @Accessors(chain = true)
    public static class TlvResult implements Serializable {
        private LinkedHashMap<String, String> map = new LinkedHashMap<>();
        private String remain = "";
        private Exception exception;


        /**
         * 获取所有TLV解析结果的副本
         *
         * @return 包含所有标签-值对的Map副本，不会暴露内部数据结构
         */
        public LinkedHashMap<String, String> getResults() {
            return new LinkedHashMap<>(map);
        }

        /**
         * 根据标签获取对应的TLV值
         *
         * @param tag TLV标签，会被去除空格并转换为大写后查找
         * @return 标签对应的值，如果标签为空或不存在则返回空字符串【不包含TAG】
         */
        public String getVal(String tag) {
            if (StringUtils.isBlank(tag)) {
                return "";
            }
            return map.getOrDefault(tag.trim().toUpperCase(), "");
        }

        /**
         * 检查是否包含指定的标签
         *
         * @param tag 要检查的标签字符串
         * @return 如果标签存在且不为空则返回true，否则返回false
         */
        public boolean hasTag(String tag) {
            // 检查标签是否为空或空白字符串
            if (StringUtils.isBlank(tag)) {
                return false;
            }
            // 将标签去除前后空格并转换为大写后检查是否存在于map中
            return map.containsKey(tag.trim().toUpperCase());
        }


        /**
         * 将标签和值存储到结果映射中
         *
         * @param tag TLV标签，会被转换为大写并去除空格
         * @param val TLV值，会被去除空格
         * @return 当前TlvResult实例，支持链式调用
         */
        private TlvResult putVal(String tag, String val) {
            if (StringUtils.isBlank(tag)) {
                return this;
            }
            String trimmedTag = tag.trim().toUpperCase();
            String trimmedVal = val != null ? val.trim().toUpperCase() : "";
            map.put(trimmedTag, trimmedVal);
            return this;
        }


        /**
         * 获取TLV解析结果中的最后一个值
         * <p>当map中存在多个标签-值对时，返回最后插入的那个值。</p>
         * <p>如果map为空或null，则返回空字符串。</p>
         *
         * @return 最后一个标签对应的值，如果map为空或null则返回空字符串
         */
        public String getVal() {
            if (null == map || map.isEmpty()) {
                return "";
            }
            String lastValue = map.values().stream().reduce((a, b) -> b).orElse("");
            return lastValue;
        }


        /**
         * 判断TLV解析操作是否成功
         *
         * @return 如果没有异常则返回true，否则返回false
         */
        public boolean isSuccess() {
            return this.exception == null;
        }


    }

}