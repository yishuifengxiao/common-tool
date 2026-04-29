package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TLV工具类
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TLVUtil {
    /**
     * 从TLV字符串中提取指定tag的value
     *
     * @param tag 目标tag
     * @param tlv TLV字符串
     * @return 找到的value，未找到或出错则返回空字符串
     */
    public static String extractValue(String tag, String tlv) {
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
        boolean ok = Hex.isHex(tlv) && Hex.isHex(tag);
        if (!ok) {
            return new TlvResult().setException(new UncheckedException("illegal data"));
        }
        tag = tag.trim().toUpperCase();
        tlv = tlv.trim().toUpperCase();
        if (!StringUtils.startsWithIgnoreCase(tlv, tag)) {
            return new TlvResult().setException(new UncheckedException(String.format("The data does not start with " +
                    "TAG %s", tag)));
        }
        String remain = StringUtils.substringAfter(tlv, tag);
        if (StringUtils.length(remain) < 2) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
        }

        // 解析长度字段，根据前缀确定长度字段的字节数
        String lengthStr;
        int startPos;

        if (StringUtils.startsWithIgnoreCase(remain, "83")) {
            // 长度字段占3字节（6个十六进制字符）
            lengthStr = StringUtils.substring(remain, 2, 8);
            if (StringUtils.length(lengthStr) != 6) {
                return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
            }
            startPos = 8;
        } else if (StringUtils.startsWithIgnoreCase(remain, "82")) {
            // 长度字段占2字节（4个十六进制字符）
            lengthStr = StringUtils.substring(remain, 2, 6);
            if (StringUtils.length(lengthStr) != 4) {
                return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
            }
            startPos = 6;
        } else if (StringUtils.startsWithIgnoreCase(remain, "81")) {
            // 长度字段占1字节（2个十六进制字符）
            lengthStr = StringUtils.substring(remain, 2, 4);
            if (StringUtils.length(lengthStr) != 2) {
                return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
            }
            startPos = 4;
        } else {
            // 长度字段占1字节（2个十六进制字符），无前缀
            lengthStr = StringUtils.substring(remain, 0, 2);
            if (StringUtils.length(lengthStr) != 2) {
                return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
            }
            startPos = 2;
        }

        // 解析长度值并验证数据完整性
        int length;
        try {
            length = Integer.parseInt(lengthStr, 16);
        } catch (NumberFormatException e) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
        }

        if (length <= 0) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
        }

        int valueEndPos = startPos + length * 2;
        if (valueEndPos > StringUtils.length(remain)) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
        }

        String val = StringUtils.substring(remain, startPos, valueEndPos);
        if (StringUtils.length(val) != length * 2) {
            return new TlvResult().setException(new UncheckedException("Not valid TLV data"));
        }
        return new TlvResult().putVal(tag, val).setRemain(StringUtils.substring(remain, valueEndPos));
    }


    /**
     * 在同一层级依次提取多个标签的值
     * 适用于扁平结构的TLV数据，按顺序从剩余数据中提取每个标签
     *
     * @param tlv  TLV格式的十六进制字符串
     * @param tags 要提取的标签列表，按顺序匹配
     * @return TlvResult对象，包含所有成功提取的标签-值对和可能的异常信息
     */
    public static TlvResult extractValOnSameLevel(String tlv, String... tags) {
        tlv = StringUtils.trim(tlv);
        if (!Hex.isHex(tlv)) {
            return new TlvResult().setException(new UncheckedException("illegal data"));
        }
        if (null == tags || tags.length == 0) {
            return new TlvResult().setException(new UncheckedException("no tags provided"));
        }

        // 依次提取每个标签的值，每次从剩余数据中继续解析
        TlvResult tlvResult = new TlvResult();
        String remain = tlv;
        for (String tag : tags) {
            TlvResult result = extract(tag, remain);
            if (null != result.getException()) {
                tlvResult.setException(result.getException());
                break;
            }
            remain = result.getRemain();
            String val = result.getVal(tag);
            tlvResult.putVal(tag, val);
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
    public static TlvResult extractValRecursive(String tlv, String... tags) {
        tlv = StringUtils.trim(tlv);
        if (!Hex.isHex(tlv)) {
            return new TlvResult().setException(new UncheckedException("illegal data"));
        }
        if (null == tags || tags.length == 0) {
            return new TlvResult().setException(new UncheckedException("no tags provided"));
        }

        // 递归提取嵌套标签，每次将当前标签的值作为下一层的输入
        TlvResult tlvResult = new TlvResult();
        String remain = tlv;
        for (String tag : tags) {
            TlvResult result = extract(tag, remain);
            if (null != result.getException()) {
                tlvResult.setException(result.getException());
                break;
            }
            String val = result.getVal(tag);
            remain = val;
            tlvResult.putVal(tag, val);
        }
        return tlvResult;
    }

    /**
     * 循环提取相同标签的所有值
     * 适用于包含多个相同标签的TLV数据，持续解析直到没有更多匹配项或发生错误
     *
     * @param tag 目标标签，应为十六进制字符串
     * @param tlv TLV格式的十六进制字符串
     * @return 包含所有匹配值的列表，如果发生错误则返回空列表【不包含TAG】
     */
    public static List<String> extractValLoop(String tag, String tlv) {
        boolean ok = Hex.isHex(tlv) && Hex.isHex(tag);
        if (!ok) {
            return new ArrayList<>();
        }

        // 循环提取相同标签的值，每次从剩余数据中继续查找
        List<String> list = new ArrayList<>();
        String remain = tlv;
        Exception exception = null;
        while (null == exception && StringUtils.isNotBlank(remain)) {
            TlvResult result = extract(tag, remain);
            exception = result.getException();
            if (null == exception) {
                list.add(result.getVal(tag));
                remain = result.getRemain();
            }
        }
        return null == exception ? list : new ArrayList<>();
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
        tag = StringUtils.isBlank(tag) ? "" : tag.trim();
        if (value == null || !Hex.isHex(value)) {
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

        String hex = Long.toHexString(value).toUpperCase();

        // 如果十六进制字符串长度为奇数，在前面补0
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        return hex;
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
     * @return 计算得到的长度字段十六进制值，超出范围返回空字符串
     */
    public static String lengthHex(String hexData) {
        if (hexData == null || hexData.isEmpty()) {
            return "00";
        }

        // 移除不可见字符（非十六进制字符）
        String cleanHex = hexData.replaceAll("[^0-9A-Fa-f]", "");
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class TlvResult implements Serializable {
        private Map<String, String> map = new HashMap<>();
        private String remain;
        private Exception exception;

        /**
         * 获取所有TLV解析结果的副本
         *
         * @return 包含所有标签-值对的Map副本，不会暴露内部数据结构
         */
        public Map<String, String> getResults() {
            return new HashMap<>(map);
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
            map.put(StringUtils.trim(tag.toUpperCase()), StringUtils.trim(val));
            return this;
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