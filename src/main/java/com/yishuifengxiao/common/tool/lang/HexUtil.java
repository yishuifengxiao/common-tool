package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.exception.UncheckedException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.BitSet;
import java.util.regex.Pattern;

/**
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
public class HexUtil {
    /**
     * 正则表达式模式，用于匹配有效的十六进制字符串
     * 匹配任意长度的十六进制字符（大小写字母和数字）
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    // 检查是否为有效的十六进制字符串
    public static boolean isHex(String str) {
        return str.length() % 2 == 0 && HEX_PATTERN.matcher(str).matches();
    }

    /**
     * 将字符串转换为十六进制格式
     *
     * @param val 待转换的字符串，可以为null
     * @return 转换后的十六进制字符串，如果输入为null则返回null
     */
    public static String toHex(String val) {
        // 如果输入字符串为null，直接返回null
        if (val == null) {
            return val;
        }

        // 如果字符串已经符合十六进制格式，则直接返回，确保长度为偶数
        if (HEX_PATTERN.matcher(val).matches()) {
            return val.length() % 2 == 0 ? val : "0" + val;
        }

        // 将普通字符串转换为十六进制格式
        return stringToHex(val);
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
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }

        return data;
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
        byte[] bytes = parseBase64String(base64String);
        return HexUtil.bytesToHex(bytes);
    }

    /**
     * 解析Base64编码的字符串并返回对应的字节数组
     *
     * @param base64String Base64编码的字符串，可以是标准、URL安全或MIME类型的Base64编码
     * @return 解码后的字节数组，如果输入为空则返回空数组，如果解码失败则返回null
     */
    public static byte[] parseBase64String(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return new byte[0]; // 输入为空返回空数组
        }

        // 提前排除全是空白字符的情况
        if (base64String.trim().isEmpty()) {
            return null; // 视作无效输入
        }

        // 第一次尝试 MIME 解码器（兼容含换行等空白字符）
        try {
            return Base64.getMimeDecoder().decode(base64String);
        } catch (Exception ignored) {
            // 忽略首次失败，继续尝试其他方式
        }

        // 第二次尝试标准 Base64 解码（去除空白后再试）
        try {
            String cleaned = base64String.replaceAll("\\s", "");
            if (cleaned.isEmpty()) {
                return null; // 清除后为空，视作无效输入
            }
            return Base64.getDecoder().decode(cleaned);
        } catch (Exception ignored) {
            // 忽略第二次失败
        }

        // 最后尝试 URL 安全的 Base64 解码
        try {
            return Base64.getUrlDecoder().decode(base64String);
        } catch (Exception ignored) {
            // 所有方式均失败，返回 null 表示无法解析
            return null;
        }
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
     * 对hex字符串进行左侧填充到指定字节数
     *
     * @param hexString   原始hex字符串
     * @param targetBytes 目标字节数
     * @return 填充后的hex字符串（保持原始的前缀格式）
     */
    public static String padHexLeft(String hexString, int targetBytes) {
        if (hexString == null || targetBytes <= 0) {
            throw new IllegalArgumentException("参数不能为空且目标字节数必须大于0");
        }

        // 处理前缀（0x或0X）
        String prefix = "";
        String cleanHex = hexString;

        if (cleanHex.startsWith("0x") || cleanHex.startsWith("0X")) {
            // 保留原始的大小写格式
            prefix = cleanHex.substring(0, 2);
            cleanHex = cleanHex.substring(2);
        }

        // 移除可能存在的空格
        cleanHex = cleanHex.replaceAll("\\s+", "");

        // 验证是否为有效的hex字符串
        if (!cleanHex.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("无效的hex字符串: " + hexString);
        }
        // 每个字节对应2个hex字符
        int targetLength = targetBytes * 2;
        int currentLength = cleanHex.length();

        // 如果已经达到或超过目标长度，直接返回
        if (currentLength >= targetLength) {
            return prefix + cleanHex;
        }

        // 左侧填充0
        int zerosToAdd = targetLength - currentLength;
        StringBuilder padded = new StringBuilder();
        for (int i = 0; i < zerosToAdd; i++) {
            padded.append('0');
        }
        padded.append(cleanHex);

        return prefix + padded.toString();
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
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
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
     * 将十六进制字符串转换为BitSet
     *
     * @param hexString 十六进制字符串，可以为null或空字符串
     * @return 转换后的BitSet对象，如果输入为null或空则返回空的BitSet
     */
    public static BitSet hexToBitSet(String hexString) {
        // 处理空值情况，返回空的BitSet
        if (hexString == null || hexString.isEmpty()) {
            return new BitSet();
        }

        // 将十六进制字符串转换为字节数组，再转换为BitSet
        byte[] bytes = hexTobyte(hexString.getBytes());
        return byteArrayToBitSet(bytes);
    }

    /**
     * 将BitSet转换为十六进制字符串
     *
     * @param bitSet 要转换的BitSet对象，可以为null
     * @return 转换后的十六进制字符串，如果输入为null则返回空字符串
     */
    public static String bitSetToHex(BitSet bitSet) {
        if (bitSet == null) {
            return "";
        }

        // 将BitSet转换为字节数组，然后转换为十六进制字符串
        byte[] bytes = bitSetToByteArray(bitSet);
        return byteTohex(bytes);
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
     * 比较两个BitSet的内容是否相同
     */
    public static boolean contentEquals(BitSet bitSet1, BitSet bitSet2) {
        if (bitSet1 == bitSet2) return true;
        if (bitSet1 == null || bitSet2 == null) return false;

        return bitSet1.equals(bitSet2);
    }

    /**
     * 将十六进制字节数组转换为字节数组
     *
     * @param b 输入的十六进制字节数组，长度必须为偶数
     * @return 转换后的字节数组，长度为输入数组的一半
     * @throws UncheckedException 当输入数组长度不是偶数时抛出异常
     */
    public static byte[] hexTobyte(byte[] b) {
        if ((b.length % 2) != 0) throw new UncheckedException("长度不是偶数");
        byte[] b2 = new byte[b.length / 2];
        // 每两个字符组成一个十六进制数进行转换
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param b 待转换的字节数组
     * @return 转换后的十六进制字符串（大写）
     */
    public static String byteTohex(byte[] b) {
        String hs = "";
        String tmp = "";
        // 遍历字节数组，将每个字节转换为两位十六进制字符串
        for (int n = 0; n < b.length; n++) {
            tmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (tmp.length() == 1) hs = hs + "0" + tmp;
            else hs = hs + tmp;
        }
        return hs.toUpperCase();
    }

    /**
     * 对两个字节数组进行异或运算
     *
     * @param b1 第一个字节数组
     * @param b2 第二个字节数组
     * @return 返回两个数组对应位置字节异或后的结果数组，长度为两个输入数组的最小长度
     */
    public static byte[] xOr(byte[] b1, byte[] b2) {
        // 创建结果数组，长度为两个输入数组的最小长度
        byte[] tXor = new byte[Math.min(b1.length, b2.length)];
        // 对两个数组对应位置的字节进行异或运算
        for (int i = 0; i < tXor.length; i++)
            tXor[i] = (byte) (b1[i] ^ b2[i]);
        return tXor;
    }

    // SwapPairs 交换字符串中相邻的字符
    // abcdef 输出 bacfde ；abcde 输出 badce
    // 参数 s: 输入的字符串
    // 返回值: 交换相邻字符后得到的新字符串
    public static String swapPairs(String s) {
        // 将字符串转换为字符数组，便于操作
        char[] chars = s.toCharArray();

        // 每次步进2，交换i和i+1的位置
        for (int i = 0; i < chars.length - 1; i += 2) {
            char temp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = temp;
        }

        // 将交换后的字符数组转换回字符串
        return new String(chars);
    }

    /**
     * <p>将数字转换指定字节数为16进制字符串</p>
     * <p>注意转换后的字符串为偶数个字符，即为2*byteNum个字符数，若为奇数个字符则自动左补零</p>
     * <p>转换后的字符的长度可能大于2*byteNum个字符数</p>
     *
     * @param number  待转换的数字
     * @param byteNum 转换后的字节数，例如byteNum为2时表示最短的字符数为 2*2
     * @return 16进制字符串
     */
    public static String toHexString(Number number, Integer byteNum) {
        if (number == null) {
            return "";
        }

        // 校验 byteNum 合法性
        if (byteNum != null && byteNum < 0) {
            throw new IllegalArgumentException("byteNum must be non-negative");
        }

        String hexString;

        if (number instanceof Integer i) {
            // 先进行基本转换
            hexString = Integer.toHexString(i);
            // 如果设置了byteNum，只进行补齐操作，不进行截断
            if (byteNum != null && byteNum > 0) {
                // 检查是否需要补齐
                if (hexString.length() < byteNum * 2) {
                    // 如果需要补齐，先进行掩码操作确保值在有效范围内
                    long unsignedValue = i & ((1L << (byteNum * 8)) - 1);
                    hexString = Long.toHexString(unsignedValue);
                }
                // 否则保持原值（不做截断）
            }
        } else if (number instanceof Long l) {
            // 先进行基本转换
            hexString = Long.toHexString(l);
            // 如果设置了byteNum，只进行补齐操作，不进行截断
            if (byteNum != null && byteNum > 0) {
                // 检查是否需要补齐
                if (hexString.length() < byteNum * 2) {
                    // 如果需要补齐，先进行掩码操作确保值在有效范围内
                    long mask = (1L << (byteNum * 8)) - 1;
                    long unsignedValue = l & mask;
                    hexString = Long.toHexString(unsignedValue);
                }
                // 否则保持原值（不做截断）
            }
        } else if (number instanceof Short s) {
            // Short类型按实际值处理
            int unsignedValue = s & 0xFFFF;
            hexString = Integer.toHexString(unsignedValue);
            // 如果设置了byteNum，只进行补齐操作，不进行截断
            if (byteNum != null && byteNum > 0) {
                // 检查是否需要补齐
                if (hexString.length() < byteNum * 2) {
                    // 如果需要补齐，先进行掩码操作确保值在有效范围内
                    long mask = (1L << (byteNum * 8)) - 1;
                    unsignedValue = (int) (unsignedValue & mask);
                    hexString = Integer.toHexString(unsignedValue);
                }
                // 否则保持原值（不做截断）
            }
        } else if (number instanceof Byte b) {
            // Byte类型按实际值处理
            int unsignedValue = b & 0xFF;
            hexString = Integer.toHexString(unsignedValue);
            // 如果设置了byteNum，只进行补齐操作，不进行截断
            if (byteNum != null && byteNum > 0) {
                // 检查是否需要补齐
                if (hexString.length() < byteNum * 2) {
                    // 如果需要补齐，先进行掩码操作确保值在有效范围内
                    long mask = (1L << (byteNum * 8)) - 1;
                    unsignedValue = (int) (unsignedValue & mask);
                    hexString = Integer.toHexString(unsignedValue);
                }
                // 否则保持原值（不做截断）
            }
        } else if (number instanceof Double || number instanceof Float) {
            throw new IllegalArgumentException("Floating point numbers are not supported.");
        } else {
            throw new IllegalArgumentException("Unsupported number type: " + number.getClass().getSimpleName() + ". Supported types: Integer, Long, Short, Byte.");
        }

        // 统一大写
        hexString = hexString.toUpperCase();

        // 若长度为奇数，前面补0使其变为偶数长度
        if (hexString.length() % 2 == 1) {
            hexString = "0" + hexString;
        }

        // 若设置了 byteNum 并且当前长度不足，则补齐至 2 * byteNum
        if (byteNum != null && byteNum > 0 && hexString.length() < byteNum * 2) {
            int padLength = byteNum * 2 - hexString.length();
            String prefix = "0".repeat(padLength); // Java 11+
            hexString = prefix + hexString;
        }

        return hexString;
    }


    /**
     * <p>将数字转换为16进制字符串</p>
     * <p>注意转换后的字符串为偶数个字符，若为奇数个字符则自动左补零</p>
     *
     * @param number 待转换的数字
     * @return 16进制字符串
     */
    public static String toHexString(Number number) {
        return toHexString(number, null);
    }
}