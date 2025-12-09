package com.yishuifengxiao.common.tool.text;

import java.util.ArrayList;
import java.util.List;

/**
 * 十六进制字符串转换为点分十进制表示法（OID格式）
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class OIDConverter {

    /**
     * 将十六进制字符串转换为点分十进制表示法（OID格式）
     *
     * @param hexStr 输入的十六进制字符串，可以包含空格，大小写不敏感
     * @return 转换后的点分十进制字符串表示
     * @throws IllegalArgumentException 转换过程中可能出现的错误，如无效的十六进制字符串或空字符串
     */
    public static String hexToDotNotation(String hexStr) {
        // 清理输入字符串
        if (hexStr == null) {
            throw new IllegalArgumentException("hex string cannot be null");
        }
        hexStr = hexStr.toLowerCase().replaceAll("\\s+", "");

        if (hexStr.isEmpty()) {
            throw new IllegalArgumentException("empty hex string");
        }

        // 解码十六进制字符串
        byte[] data;
        try {
            data = hexStringToByteArray(hexStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid hex string: " + e.getMessage());
        }

        if (data.length == 0) {
            throw new IllegalArgumentException("empty hex string");
        }

        // 解析OID组件
        List<String> components = new ArrayList<>();

        // 第一个字节的特殊处理：X * 40 + Y
        byte firstByte = data[0];
        components.add(String.valueOf((firstByte & 0xFF) / 40));
        components.add(String.valueOf((firstByte & 0xFF) % 40));

        // 处理剩余的字节（base128编码）
        long currentValue = 0;
        boolean expectingMoreBytes = false; // 新增：标记是否期望更多字节

        for (int i = 1; i < data.length; i++) {
            byte b = data[i];
            currentValue = (currentValue << 7) | (b & 0x7F);

            // 如果最高位为1，表示还有更多字节
            if ((b & 0x80) != 0) {
                expectingMoreBytes = true;
            } else {
                // 最高位为0，表示这个数字结束
                components.add(String.valueOf(currentValue));
                currentValue = 0;
                expectingMoreBytes = false;
            }
        }

        // 检查是否有未结束的数字或不完整的base128编码
        if (currentValue != 0 || expectingMoreBytes) {
            throw new IllegalArgumentException("incomplete base128 encoding");
        }

        return String.join(".", components);
    }

    /**
     * 将点分十进制格式的OID字符串转换为大写十六进制字符串。
     * <p>节点值在允许范围内：OID标准规定，每个节点值可以是 0 到 2^32-1（约43亿）之间的整数。2、999 和 10 都在这个范围内。
     * <p>
     * 前两个节点的规则：
     * <p>
     * 第一个节点只能取 0、1、2 三个值，分别代表：
     * <p>
     * 0： ITU-T
     * <p>
     * 1： ISO
     * <p>
     * 2： 联合体（ISO/ITU-T）
     * <p>
     * 第二个节点的值取决于第一个节点：
     * <p>
     * 如果第一节点是 0 或 1，则第二节点必须在 0 到 39 之间。
     * <p>
     * 如果第一节点是 2，则第二节点可以是 0 到 2^32-1 之间的任何值，没有39的限制。</p>
     *
     * @param dotStr 点分十进制格式的OID字符串，例如 "1.2.3.4"
     * @return 转换后的大写十六进制字符串
     * @throws IllegalArgumentException 转换过程中可能出现的错误，如输入为空、格式不正确等
     */
    public static String dotNotationToHex(String dotStr) {
        if (dotStr == null || dotStr.isEmpty()) {
            throw new IllegalArgumentException("empty input");
        }

        String[] parts = dotStr.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("OID must have at least 2 components");
        }

        // 解析点分十进制为数字
        List<Long> components = new ArrayList<>();
        for (String part : parts) {
            // 检查空部分（如 ".1.2" 或 "1..2"）
            String trimmedPart = part.trim();
            if (trimmedPart.isEmpty()) {
                throw new IllegalArgumentException("incomplete format: empty component");
            }

            // 检查负数
            if (trimmedPart.startsWith("-")) {
                throw new IllegalArgumentException("negative number not allowed");
            }

            // 解析数字
            try {
                long val = Long.parseLong(trimmedPart);
                if (val < 0) {
                    throw new IllegalArgumentException("negative number not allowed");
                }
                components.add(val);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid OID component: " + part);
            }
        }

        // 编码为十六进制
        List<Byte> resultBytes = new ArrayList<>();

        // 编码前两个数字（特殊处理）
        if (components.get(0) > 2) {
//            throw new IllegalArgumentException("invalid OID: first component must be 0-2");
        }

        byte firstByte = (byte) (components.get(0) * 40 + components.get(1));
        resultBytes.add(firstByte);

        // 编码剩余的数字（使用base128编码）
        for (int i = 2; i < components.size(); i++) {
            byte[] encoded = encodeBase128(components.get(i));
            for (byte b : encoded) {
                resultBytes.add(b);
            }
        }

        // 转换为字节数组
        byte[] resultArray = new byte[resultBytes.size()];
        for (int i = 0; i < resultBytes.size(); i++) {
            resultArray[i] = resultBytes.get(i);
        }

        return bytesToHex(resultArray).toUpperCase();
    }


    /**
     * 将数字编码为base128格式（用于OID编码）
     *
     * @param n 要编码的数字
     * @return 编码后的字节数组
     */
    private static byte[] encodeBase128(long n) {
        if (n == 0) {
            return new byte[]{0};
        }

        // 计算需要的字节数
        int byteCount = 0;
        long temp = n;
        while (temp > 0) {
            byteCount++;
            temp >>= 7;
        }

        byte[] result = new byte[byteCount];

        // 从低位到高位编码
        for (int i = byteCount - 1; i >= 0; i--) {
            result[i] = (byte) (n & 0x7F);
            if (i != byteCount - 1) {
                result[i] |= 0x80; // 设置最高位，表示还有更多字节（除了最后一个字节）
            }
            n >>= 7;
        }

        return result;
    }


    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param s 十六进制字符串
     * @return 对应的字节数组
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(s.charAt(i), 16);
            int low = Character.digit(s.charAt(i + 1), 16);

            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character");
            }

            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }


}