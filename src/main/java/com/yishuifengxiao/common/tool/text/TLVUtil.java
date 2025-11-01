package com.yishuifengxiao.common.tool.text;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.BitSet;

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
                String lengthStr = tlvData.substring(tagIndex + targetTag.length(),
                        tagIndex + targetTag.length() + 2);
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
                if (isPotentialTlv(value)) {
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
        return (firstChar >= '8' && firstChar <= 'F') ||
                (firstChar >= 'a' && firstChar <= 'f');
    }

    /**
     * 判断一个字符串是否可能是一个TLV结构
     *
     * @param value 要检查的字符串
     * @return 是否可能是TLV结构
     */
    private static boolean isPotentialTlv(String value) {
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
        return tag + sb.toString();
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
     * 将字符串转换为十六进制字符串（使用UTF-8编码）
     *
     * @param str 原始字符串
     * @return 十六进制字符串，每两个字符代表一个字节
     */
    public static String stringToHex(String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        return bytesToHex(bytes);
    }

    /**
     * 将十六进制字符串转换为原始字符串（使用UTF-8编码）
     *
     * @param hex 十六进制字符串
     * @return 原始字符串
     */
    public static String hexToString(String hex) {
        byte[] bytes = hexToBytes(hex);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串，每两个字符代表一个字节
     */
    public static String bytesToHex(byte[] bytes) {
        // 兼容旧版本 Android/Java 的实现
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        // 移除可能存在的空格
        hex = hex.replaceAll("\\s", "");

        // 检查十六进制字符串长度是否为偶数
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("十六进制字符串长度必须为偶数");
        }

        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            // 每两个字符解析为一个字节
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
    }

    /**
     * 便捷方法：将字符串转换为TLV格式（自动转换为十六进制）
     *
     * @param tag  标签
     * @param text 文本字符串
     * @return TLV格式字符串
     */
    public static String stringToTLV(String tag, String text) {
        String hexValue = stringToHex(text);
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
        String hexValue = bytesToHex(bytes);
        return toTLV(tag, hexValue);
    }

    /**
     * 将BitSet转换为byte数组
     *
     * @param bitSet 要转换的BitSet
     * @return 转换后的byte数组
     */
    public static byte[] bitSetToByteArray(BitSet bitSet) {
        if (bitSet == null) {
            return new byte[0];
        }

        // 如果BitSet为空，返回空数组
        if (bitSet.length() == 0) {
            return new byte[0];
        }

        // 计算需要的字节数
        int byteCount = (bitSet.length() + 7) / 8;
        byte[] bytes = new byte[byteCount];

        // 遍历所有设置的位
        for (int bitIndex = bitSet.nextSetBit(0); bitIndex >= 0; bitIndex = bitSet.nextSetBit(bitIndex + 1)) {
            int byteIndex = bitIndex / 8;
            int bitOffset = bitIndex % 8;
            bytes[byteIndex] |= (1 << bitOffset);
        }

        return bytes;
    }

    /**
     * 将byte数组转换为BitSet
     *
     * @param bytes 要转换的byte数组
     * @return 转换后的BitSet
     */
    public static BitSet byteArrayToBitSet(byte[] bytes) {
        if (bytes == null) {
            return new BitSet();
        }

        BitSet bitSet = new BitSet(bytes.length * 8);

        for (int i = 0; i < bytes.length * 8; i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;

            if ((bytes[byteIndex] & (1 << bitIndex)) != 0) {
                bitSet.set(i);
            }
        }

        return bitSet;
    }

    /**
     * 将BitSet转换为byte数组（支持指定字节序）
     *
     * @param bitSet    要转换的BitSet
     * @param bigEndian 是否使用大端序（true: 高位在前, false: 低位在前）
     * @return 转换后的byte数组
     */
    public static byte[] bitSetToByteArray(BitSet bitSet, boolean bigEndian) {
        if (bitSet == null) {
            return new byte[0];
        }

        int byteCount = (bitSet.length() + 7) / 8;
        byte[] bytes = new byte[byteCount];

        for (int i = 0; i < bitSet.length(); i++) {
            if (bitSet.get(i)) {
                int byteIndex = i / 8;
                int bitIndex = bigEndian ? (7 - (i % 8)) : (i % 8);
                bytes[byteIndex] |= (1 << bitIndex);
            }
        }

        return bytes;
    }

    /**
     * 将byte数组转换为BitSet（支持指定字节序）
     *
     * @param bytes     要转换的byte数组
     * @param bigEndian 是否使用大端序
     * @return 转换后的BitSet
     */
    public static BitSet byteArrayToBitSet(byte[] bytes, boolean bigEndian) {
        if (bytes == null) {
            return new BitSet();
        }

        BitSet bitSet = new BitSet(bytes.length * 8);

        for (int i = 0; i < bytes.length * 8; i++) {
            int byteIndex = i / 8;
            int bitIndex = bigEndian ? (7 - (i % 8)) : (i % 8);

            if ((bytes[byteIndex] & (1 << bitIndex)) != 0) {
                bitSet.set(i);
            }
        }

        return bitSet;
    }

    /**
     * 将BitSet转换为十六进制字符串
     *
     * @param bitSet 要转换的BitSet
     * @return 十六进制字符串
     */
    public static String bitSetToHexString(BitSet bitSet) {
        byte[] bytes = bitSetToByteArray(bitSet, false);
        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }

        return hexString.toString();
    }

    /**
     * 从十六进制字符串创建BitSet
     *
     * @param hexString 十六进制字符串
     * @return 创建的BitSet
     */
    public static BitSet hexStringToBitSet(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return new BitSet();
        }

        int len = hexString.length();
        byte[] bytes = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }

        return byteArrayToBitSet(bytes, false);
    }

    /**
     * 获取BitSet的二进制字符串表示
     */
    public static String toBinaryString(BitSet bitSet) {
        if (bitSet == null) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }

    /**
     * 从二进制字符串创建BitSet
     */
    public static BitSet fromBinaryString(String binaryString) {
        if (binaryString == null || binaryString.isEmpty()) {
            return new BitSet();
        }

        BitSet bitSet = new BitSet(binaryString.length());
        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '1') {
                bitSet.set(i);
            }
        }
        return bitSet;
    }


    /**
     * 比较两个BitSet的内容是否相同
     */
    public static boolean contentEquals(BitSet bitSet1, BitSet bitSet2) {
        if (bitSet1 == bitSet2) return true;
        if (bitSet1 == null || bitSet2 == null) return false;

        return bitSet1.equals(bitSet2);
    }

    /**
     * 将十六进制字符串转换为Base64字符串（兼容Java 8+）
     *
     * @param hexString 十六进制字符串
     * @return Base64编码的字符串
     */
    public static String hexToBase64(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return "";
        }

        // 移除可能存在的空格和前缀
        hexString = hexString.replaceAll("\\s", "").replace("0x", "");

        // 确保十六进制字符串长度为偶数
        if (hexString.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string length");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            int firstDigit = Character.digit(hexString.charAt(i), 16);
            int secondDigit = Character.digit(hexString.charAt(i + 1), 16);

            if (firstDigit == -1 || secondDigit == -1) {
                throw new IllegalArgumentException("Invalid hex character");
            }

            bytes[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }

        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将Base64字符串转换为十六进制字符串（兼容Java 8+）
     *
     * @param base64String Base64编码的字符串
     * @return 十六进制字符串
     */
    public static String base64ToHex(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return "";
        }

        byte[] bytes = Base64.getDecoder().decode(base64String);
        return bytesToHex(bytes);
    }
}