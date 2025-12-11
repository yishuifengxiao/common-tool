package com.yishuifengxiao.common.tool.text;

import com.yishuifengxiao.common.tool.lang.Hex;

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
     * @param targetTag 目标tag
     * @param tlvData   TLV字符串
     * @return 找到的value，未找到或出错则返回空字符串
     */
    public static String fetchValueFromTlv(String targetTag, String tlvData) {
        if (targetTag == null || tlvData == null || tlvData.isEmpty()) {
            return "";
        }

        try {
            return parseTlvRecursive(targetTag, tlvData);
        } catch (Exception e) {
            return "";
        }
    }

    private static String parseTlvRecursive(String targetTag, String tlvData) {
        if (targetTag == null || tlvData == null || tlvData.isEmpty()) {
            return "";
        }

        // 简单直接的方法：在字符串中查找目标标签
        int tagIndex = 0;
        while ((tagIndex = tlvData.indexOf(targetTag, tagIndex)) != -1) {
            try {
                // 确保标签后面有足够的字符用于长度和值
                if (tagIndex + targetTag.length() + 2 > tlvData.length()) {
                    tagIndex += 1;
                    continue;
                }

                // 提取长度字段
                String lengthStr = tlvData.substring(tagIndex + targetTag.length(), tagIndex + targetTag.length() + 2);
                int valueLength = Integer.parseInt(lengthStr, 16);

                // 提取值字段
                int valueStartIndex = tagIndex + targetTag.length() + 2;
                int valueEndIndex = valueStartIndex + valueLength * 2;

                if (valueEndIndex > tlvData.length()) {
                    tagIndex += 1;
                    continue;
                }

                return tlvData.substring(valueStartIndex, valueEndIndex);
            } catch (Exception e) {
                tagIndex += 1;
                continue;
            }
        }

        // 如果没有直接找到，尝试在每个可能的TLV结构的值部分递归查找
        for (int i = 0; i <= tlvData.length() - 4; i++) {
            try {
                // 尝试解析一个TLV结构
                String currentTag = tlvData.substring(i, i + 2);
                int tagLength = 2;

                // 检查是否是扩展标签
                if (isExtendedTag(currentTag)) {
                    if (i + 4 > tlvData.length()) {
                        i++;
                        continue;
                    }
                    currentTag = tlvData.substring(i, i + 4);
                    tagLength = 4;
                }

                // 解析长度
                int lengthIndex = i + tagLength;
                if (lengthIndex + 2 > tlvData.length()) {
                    i++;
                    continue;
                }

                String lengthStr = tlvData.substring(lengthIndex, lengthIndex + 2);
                int valueLength = Integer.parseInt(lengthStr, 16);

                // 解析值
                int valueStartIndex = lengthIndex + 2;
                int valueEndIndex = valueStartIndex + valueLength * 2;

                if (valueEndIndex > tlvData.length()) {
                    i++;
                    continue;
                }

                String value = tlvData.substring(valueStartIndex, valueEndIndex);

                // 在值部分递归查找
                if (isTlv(value)) {
                    String nestedResult = parseTlvRecursive(targetTag, value);
                    if (!nestedResult.isEmpty()) {
                        return nestedResult;
                    }
                }

                // 跳过整个TLV结构
                i = valueEndIndex - 1; // -1因为外层循环会i++

            } catch (Exception e) {
                i++;
                continue;
            }
        }

        return "";
    }

    /**
     * 判断是否为扩展标签（需要额外字节）
     * 通常以8-F开头的标签需要额外字节
     */
    private static boolean isExtendedTag(String tag) {
        if (tag == null || tag.length() != 2) {
            return false;
        }
        char firstChar = tag.charAt(0);
        return (firstChar >= '8' && firstChar <= 'F') || (firstChar >= 'a' && firstChar <= 'f');
    }

    /**
     * 判断一个字符串是否可能是一个TLV结构
     *
     * @param value 要检查的字符串
     * @return 是否可能是TLV结构
     */
    private static boolean isTlv(String value) {
        // 简单检查：长度是否为偶数，并且至少有6个字符（Tag2字节+Length2字节+Value至少2字节）
        if (value == null || value.length() < 6 || value.length() % 2 != 0) {
            return false;
        }

        // 更严格的检查：验证Tag是否为有效的十六进制字符
        for (int i = 0; i < 4; i++) { // 检查前4个字符（双字节Tag）
            char c = value.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 将给定的标签和输入字符串转换为标签-长度-值（TLV）格式的字符串。
     * 该函数根据输入字符串的长度（以字节为单位）选择合适的长度编码方式。
     *
     * @param tag   表示 TLV 格式中的标签部分，为字符串类型
     * @param input 表示 TLV 格式中的值部分，应为十六进制字符串，每两个字符代表一个字节
     * @return 返回一个符合 TLV 格式的字符串
     */
    public static String toTLV(String tag, String input) {
        tag = null == tag ? "" : tag.trim();
        // 计算输入字符串的字节长度，由于输入是十六进制字符串，每两个字符代表一个字节
        int inputLen = input.length() / 2;
        StringBuilder sb = new StringBuilder();

        // 将输入长度转换为十六进制字符串
        String strInputLenString = toHex(String.valueOf(inputLen));

        if (inputLen > 65535) {
            // 当输入长度大于 65535 时，使用标签 83 表示长度为 3 字节
            sb.append("83");
            sb.append(strInputLenString);
            sb.append(input);
        } else if (inputLen > 255) {
            // 当输入长度大于 255 且不超过 65535 时，使用标签 82 表示长度为 2 字节
            sb.append("82");
            sb.append(strInputLenString);
            sb.append(input);
        } else if (inputLen > 127) {
            // 当输入长度大于 127 且不超过 255 时，使用标签 81 表示长度为 1 字节
            sb.append("81");
            sb.append(strInputLenString);
            sb.append(input);
        } else {
            // 当输入长度不超过 127 时，直接使用长度值
            sb.append(strInputLenString);
            sb.append(input);
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
        long value = Long.parseLong(num);
        String hex = Long.toHexString(value).toUpperCase();

        // 如果十六进制字符串长度为奇数，在前面补0
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        return hex;
    }


    /**
     * 便捷方法：将字符串转换为TLV格式（自动转换为十六进制）
     *
     * @param tag  标签
     * @param text 文本字符串
     * @return TLV格式字符串
     */
    public static String stringToTLV(String tag, String text) {
        String hexValue = Hex.isHex(text) ? text : Hex.toHexString(text);
        return toTLV(tag, hexValue);
    }

    /**
     * 便捷方法：将字节数组转换为TLV格式
     *
     * @param tag   标签
     * @param bytes 字节数组
     * @return TLV格式字符串
     */
    public static String bytesToTLV(String tag, byte[] bytes) {
        String hexValue = Hex.bytesToHex(bytes);
        return toTLV(tag, hexValue);
    }


}