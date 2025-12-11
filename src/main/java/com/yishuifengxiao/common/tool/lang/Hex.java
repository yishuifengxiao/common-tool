package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.BitSet;
import java.util.regex.Pattern;

/**
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class Hex {
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
     * 将字符串编码成16进制数字,适用于所有字符（包括中文）
     *
     * @param str 传入字符串
     * @return 将字符串编码成16进制数字
     */
    public static String utf8TextToHexString(String str) {
        if (null == str) {
            return null;
        }
        // 根据默认编码获取字节数组
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        final String hexString = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // 将字节数组中每个字节拆解成2位16进制整数
        for (byte aByte : bytes) {
            sb.append(hexString.charAt((aByte & 0xf0) >> 4));
            sb.append(hexString.charAt((aByte & 0x0f) >> 0));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 将十六进制字符串转换为UTF-8编码的字符串
     *
     * @param hexStr 输入的十六进制字符串，每两个字符表示一个字节
     * @return 转换后的UTF-8编码字符串
     */
    public static String hexStringToUtf8Text(String hexStr) {
        if (null == hexStr) {
            return null;
        }
        byte[] resultBytes = new byte[hexStr.length() / 2];
        // 将十六进制字符串按每两位分割并转换为字节数组
        for (int i = 0; i < hexStr.length(); i += 2) {
            String hexPair = hexStr.substring(i, i + 2);
            resultBytes[i / 2] = (byte) Integer.parseInt(hexPair, 16);
        }

        return new String(resultBytes, StandardCharsets.UTF_8);
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
        if (!isHex(hex)) {
            return null;
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
        return Hex.bytesToHex(bytes);
    }

    /**
     * 解析Base64编码的字符串并返回对应的字节数组
     *
     * @param base64String Base64编码的字符串，可以是标准、URL安全或MIME类型的Base64编码
     * @return 解码后的字节数组，如果输入为空则返回空数组，如果解码失败则返回null
     */
    public static byte[] parseBase64String(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
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
        if (!isHex(hexString)) {
            log.warn("Invalid hex string: {}", hexString);
            return null;
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            int firstDigit = Character.digit(hexString.charAt(i), 16);
            int secondDigit = Character.digit(hexString.charAt(i + 1), 16);

            if (firstDigit == -1 || secondDigit == -1) {
                return null;
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
        if (!isHex(hexString)) {
            log.warn("Invalid hex string: {}", hexString);
            return null;
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
     * 从十六进制字符串创建BitSet
     *
     * @param hexString 十六进制字符串
     * @return 创建的BitSet
     */
    public static BitSet hexToBitSet(String hexString) {
        if (!isHex(hexString)) {
            log.warn("Invalid hex string: {}", hexString);
            return null;
        }

        int len = hexString.length();
        byte[] bytes = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }

        return bytesToBitSet(bytes);
    }

    /**
     * 将BitSet转换为十六进制字符串
     *
     * @param bitSet 要转换的BitSet
     * @return 十六进制字符串表示
     */
    public static String bitSetToHex(BitSet bitSet) {
        if (bitSet == null) {
            return "";
        }

        // 如果BitSet为空，返回空字符串
        if (bitSet.length() == 0) {
            return "";
        }

        // 先将BitSet转换为byte数组
        byte[] bytes = bitSetToBytes(bitSet);

        // 再将byte数组转换为十六进制字符串
        return bytesToHex(bytes);
    }

    /**
     * 将BitSet转换为byte数组
     *
     * @param bitSet 要转换的BitSet
     * @return 转换后的byte数组
     */
    public static byte[] bitSetToBytes(BitSet bitSet) {
        if (bitSet == null) {
            return new byte[0];
        }
        return bitSet.toByteArray();
    }


    /**
     * 将byte数组转换为BitSet
     *
     * @param bytes 要转换的byte数组
     * @return 转换后的BitSet
     */
    public static BitSet bytesToBitSet(byte[] bytes) {
        if (bytes == null) {
            return new BitSet();
        }
        return BitSet.valueOf(bytes);
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

    /**
     * 交换字符串中相邻的字符
     * <p>该方法将字符串中的每对相邻字符进行交换，例如："abcdef" 输出 "bacfde"；"abcde" 输出 "badce"</p>
     *
     * @param s 输入的字符串
     * @return 交换相邻字符后得到的新字符串
     */
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
    public static String numberToHexString(Number number, Integer byteNum) {
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
            throw new IllegalArgumentException("Unsupported number type: " + number.getClass().getSimpleName() + ". " + "Supported types: Integer, Long, Short, Byte.");
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
    public static String numberToHexString(Number number) {
        return numberToHexString(number, null);
    }

    /**
     * 将十六进制字符串转换为二进制字符串
     *
     * @param hexString 输入的十六进制字符串
     * @return 对应的二进制字符串表示
     */
    public static String hexStringToBinaryString(String hexString) {
        if (StringUtils.isBlank(hexString)) {
            return null;
        }
        BigInteger val = new BigInteger(hexString.trim(), 16);
        String valString = val.toString(2);
        return valString.length() % 2 == 0 ? valString : "0" + valString;
    }

    /**
     * 将二进制字符串转换为十六进制字符串
     *
     * @param binaryString 输入的二进制字符串
     * @return 对应的十六进制字符串表示，如果输入为空或空白字符串则返回null
     */
    public static String binaryStringToHexString(String binaryString) {
        if (StringUtils.isBlank(binaryString)) {
            return null;
        }
        BigInteger val = new BigInteger(binaryString.trim(), 2);
        String valString = val.toString(16).toUpperCase();
        return valString.length() % 2 == 0 ? valString : "0" + valString;
    }

    /**
     * 将十进制数转换为十六进制字符串，默认大端表示
     *
     * @param num 10进制数
     * @param len 几个字节表示
     * @return String 16进制字符串
     */
    public static String itoa(int num, int len) {
        String target = Integer.toHexString(num);
        // 检查目标长度是否超过指定字节数能表示的范围或数值是否为负数
        if (target.length() > len * 2 || num < 0) {
            throw new UncheckedException("参数非法");
        }
        StringBuilder builder = new StringBuilder();
        int paddCount = 2 * len - target.length();
        // 在字符串前补0以达到指定的字节长度
        for (int i = 0; i < paddCount; i++) {
            builder.append("0");
        }
        builder.append(target.toUpperCase());
        return builder.toString().toUpperCase();
    }

    /**
     * 将十六进制字符串转换为十进制整数
     *
     * @param s 16进制字符串
     * @return int 10进制数
     */

    public static Integer atoi(String s) {
        if (StringUtils.isBlank(s)) {
            return null;
        }
        return Integer.valueOf(s.trim(), 16);
    }

}