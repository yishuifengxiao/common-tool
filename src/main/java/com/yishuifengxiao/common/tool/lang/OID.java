package com.yishuifengxiao.common.tool.lang;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>OID（对象标识符）工具类</p>
 * <p>提供OID的十六进制与点分十进制格式之间的互相转换功能。</p>
 * <p>特性：</p>
 * <ul>
 * <li>十六进制字符串转点分十进制OID</li>
 * <li>点分十进制OID转十六进制字符串</li>
 * <li>支持Base128编码/解码</li>
 * <li>OID格式验证</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class OID {

    /**
     * 将十六进制字符串转换为点分十进制表示法（OID格式）
     *
     * @param hexStr 输入的十六进制字符串，可以包含空格，大小写不敏感
     * @return 转换后的点分十进制字符串表示
     * @throws IllegalArgumentException 转换过程中可能出现的错误，如无效的十六进制字符串或空字符串
     */
    public static String hexToDotNotation(String hexStr) {
        if (hexStr == null) {
            throw new IllegalArgumentException("hex string cannot be null");
        }
        hexStr = hexStr.toLowerCase().replaceAll("\\s+", "");

        if (hexStr.isEmpty()) {
            throw new IllegalArgumentException("empty hex string");
        }

        byte[] data;
        try {
            data = hexStringToByteArray(hexStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid hex string: " + e.getMessage());
        }

        if (data.length == 0) {
            throw new IllegalArgumentException("empty hex string");
        }

        List<String> components = new ArrayList<>();

        long firstValue = 0;
        int firstByteIndex = 0;
        
        while (firstByteIndex < data.length) {
            byte b = data[firstByteIndex];
            firstValue = (firstValue << 7) | (b & 0x7F);
            firstByteIndex++;
            
            if ((b & 0x80) == 0) {
                break;
            }
        }
        
        if (firstByteIndex > data.length) {
            throw new IllegalArgumentException("incomplete base128 encoding for first component");
        }
        
        long firstComponent;
        long secondComponent;
        
        if (firstValue < 40) {
            firstComponent = 0;
            secondComponent = firstValue;
        } else if (firstValue < 80) {
            firstComponent = 1;
            secondComponent = firstValue - 40;
        } else {
            firstComponent = 2;
            secondComponent = firstValue - 80;
        }
        
        components.add(String.valueOf(firstComponent));
        components.add(String.valueOf(secondComponent));

        long currentValue = 0;
        boolean expectingMoreBytes = false;

        for (int i = firstByteIndex; i < data.length; i++) {
            byte b = data[i];
            currentValue = (currentValue << 7) | (b & 0x7F);

            if ((b & 0x80) != 0) {
                expectingMoreBytes = true;
            } else {
                components.add(String.valueOf(currentValue));
                currentValue = 0;
                expectingMoreBytes = false;
            }
        }

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

        List<Long> components = new ArrayList<>();
        for (String part : parts) {
            String trimmedPart = part.trim();
            if (trimmedPart.isEmpty()) {
                throw new IllegalArgumentException("incomplete format: empty component");
            }

            if (trimmedPart.startsWith("-")) {
                throw new IllegalArgumentException("negative number not allowed");
            }

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

        List<Byte> resultBytes = new ArrayList<>();

        // 编码前两个数字（特殊处理）
        long firstComponent = components.get(0);
        long secondComponent = components.get(1);
        
        if (firstComponent > 2) {
            throw new IllegalArgumentException("invalid OID: first component must be 0-2");
        }
        
        if (firstComponent < 2 && secondComponent > 39) {
            throw new IllegalArgumentException("invalid OID: when first component is 0 or 1, second component must be 0-39");
        }

        byte[] encoded = encodeBase128(firstComponent * 40 + secondComponent);
        for (byte b : encoded) {
            resultBytes.add(b);
        }

        for (int i = 2; i < components.size(); i++) {
            byte[] encodedComponent = encodeBase128(components.get(i));
            for (byte b : encodedComponent) {
                resultBytes.add(b);
            }
        }

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