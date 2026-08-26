package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.text.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * <p>十六进制工具类</p>
 * <p>提供十六进制字符串与各种数据类型之间的转换功能。</p>
 * <p>特性：</p>
 * <ul>
 * <li>十六进制字符串与字节数组互转</li>
 * <li>十六进制与UTF-8字符串互转</li>
 * <li>十六进制与Base64互转</li>
 * <li>十六进制与BitSet互转</li>
 * <li>数字转十六进制字符串</li>
 * </ul>
 *
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

    /**
     * 检查是否为有效的十六进制字符串
     *
     * @param str 待检查的字符串
     * @return 如果字符串是有效的十六进制格式且长度为偶数则返回true，否则返回false
     */
    public static boolean isHex(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        str = str.trim();
        return str.length() % 2 == 0 && HEX_PATTERN.matcher(str).matches();
    }

    /**
     * 将十六进制字符串转换为 ASCII 字符串。
     * 每两个十六进制字符对应一个 ASCII 字符（0x00~0xFF）。
     *
     * @param hex 十六进制字符串，可含空白字符，不区分大小写
     * @return 对应的 ASCII 字符串
     * @throws IllegalArgumentException 如果 hex 为 null，或长度为奇数，或包含非法十六进制字符
     */
    public static String hexToAscii(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("hex must not be null");
        }
        String clean = TextUtil.removeWhitespaceAndInvisible(hex);
        if (clean.isEmpty()) {
            return "";
        }
        if (clean.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even, but got " + clean.length());
        }

        byte[] bytes = new byte[clean.length() / 2];
        for (int i = 0; i < clean.length(); i += 2) {
            int high = Character.digit(clean.charAt(i), 16);
            int low = Character.digit(clean.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character at position " + i);
            }
            bytes[i / 2] = (byte) ((high << 4) + low);
        }
        // 使用 US-ASCII 解码，若字节值超出 0~127 仍保留原值（扩展 ASCII）
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * 将 ASCII 字符串转换为大写十六进制字符串。
     * 每个字符取其低 8 位（0~255）转换为两位十六进制。
     *
     * @param ascii ASCII 字符串（若包含非 ASCII 字符，则只取低字节，可能失真）
     * @return 对应的大写十六进制字符串
     * @throws IllegalArgumentException 如果 ascii 为 null
     */
    public static String asciiToHex(String ascii) {

        if (ascii == null) {
            throw new IllegalArgumentException("ascii must not be null");
        }
        ascii = StringUtils.trim(ascii);
        if (ascii.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(ascii.length() * 2);
        for (char c : ascii.toCharArray()) {
            // 只取低 8 位，确保 0~255 范围
            int val = c & 0xFF;
            sb.append(String.format("%02X", val));
        }
        return sb.toString();
    }

    /**
     * 将UTF-8编码的字符串转换为十六进制字符串
     *
     * @param str 传入的UTF-8编码字符串
     * @return 十六进制字符串表示
     */
    public static String utf8ToHex(String str) {
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
    public static String hexToUtf8(String hexStr) {
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
     * @return 十六进制字符串，每两个字符代表一个字节；如果输入为null则返回空字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

        final char[] hexChars = "0123456789ABCDEF".toCharArray();
        StringBuilder hexString = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            hexString.append(hexChars[(b & 0xf0) >> 4]);
            hexString.append(hexChars[b & 0x0f]);
        }

        return hexString.toString().toUpperCase();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hex 十六进制字符串
     * @return 字节数组，如果输入无效则返回null
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) {
            return null;
        }

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
            int firstDigit = Character.digit(hex.charAt(i), 16);
            int secondDigit = Character.digit(hex.charAt(i + 1), 16);

            if (firstDigit == -1 || secondDigit == -1) {
                return null;
            }

            data[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }

        return data;
    }

    /**
     * 将Base64字符串转换为十六进制字符串（兼容Java 8+）
     *
     * @param base64String Base64编码的字符串
     * @return 十六进制字符串，如果输入无效则返回空字符串
     */
    public static String base64ToHex(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return "";
        }
        byte[] bytes = base64ToBytes(base64String);
        if (bytes == null) {
            return "";
        }
        return Hex.bytesToHex(bytes);
    }

    /**
     * 解码Base64字符串并返回对应的字节数组
     *
     * @param base64String Base64编码的字符串，可以是标准、URL安全或MIME类型的Base64编码
     * @return 解码后的字节数组，如果输入为空则返回null，如果解码失败则返回null
     */
    public static byte[] base64ToBytes(String base64String) {
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
     * @return 填充后的hex字符串（保持原始的前缀格式），如果输入无效则返回null
     */
    public static String padHexLeft(String hexString, int targetBytes) {
        if (hexString == null || !isHex(hexString)) {
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
            return (prefix + cleanHex).toUpperCase();
        }

        // 左侧填充0，使用更高效的字符串拼接方式
        StringBuilder padded = new StringBuilder(targetLength);
        for (int i = 0; i < targetLength - currentLength; i++) {
            padded.append('0');
        }
        padded.append(cleanHex);

        return (prefix + padded).toUpperCase();
    }

    /**
     * 补零辅助方法
     * 在字符串左侧填充零字符，使其达到指定长度
     *
     * @param str 需要补零的原始字符串
     * @param len 目标长度
     * @return 补零后的字符串，如果原字符串长度已达到或超过目标长度则返回原字符串
     */
    public static String padWithZero(String str, int len) {
        if (str == null) {
            str = "";
        }
        str = str.trim();
        if (str.length() >= len) {
            return str.toUpperCase();
        }

        // 使用StringBuilder高效构建补零后的字符串
        StringBuilder sb = new StringBuilder(len);
        // 添加所需数量的零字符
        for (int i = 0; i < len - str.length(); i++) {
            sb.append('0');
        }
        // 追加原始字符串
        sb.append(str);
        return sb.toString().toUpperCase();
    }


    /**
     * 对两个字节数组进行异或运算
     *
     * @param b1 第一个字节数组
     * @param b2 第二个字节数组
     * @return 返回两个数组对应位置字节异或后的结果数组，长度为两个输入数组的最小长度；如果任一输入为null则返回空数组
     */
    public static byte[] xOr(byte[] b1, byte[] b2) {
        if (b1 == null || b2 == null || b1.length == 0 || b2.length == 0) {
            return new byte[0];
        }

        // 创建结果数组，长度为两个输入数组的最小长度
        byte[] tXor = new byte[Math.min(b1.length, b2.length)];
        // 对两个数组对应位置的字节进行异或运算
        for (int i = 0; i < tXor.length; i++) {
            tXor[i] = (byte) (b1[i] ^ b2[i]);
        }
        return tXor;
    }

    /**
     * 交换字符串中相邻的字符
     * <p>该方法将字符串中的每对相邻字符进行交换，例如："abcdef" 输出 "bacfde"；"abcde" 输出 "badce"</p>
     *
     * @param s 输入的字符串
     * @return 交换相邻字符后得到的新字符串，如果输入为null则返回null
     */
    public static String swapPairs(String s) {
        if (s == null) {
            return null;
        }

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
            hexString = convertIntegerToHex(i, byteNum);
        } else if (number instanceof Long l) {
            hexString = convertLongToHex(l, byteNum);
        } else if (number instanceof Short s) {
            hexString = convertShortToHex(s, byteNum);
        } else if (number instanceof Byte b) {
            hexString = convertByteToHex(b, byteNum);
        } else if (number instanceof Double || number instanceof Float) {
            throw new IllegalArgumentException("Floating point numbers are not supported.");
        } else {
            throw new IllegalArgumentException("Unsupported number type: " + number.getClass().getSimpleName() + ". " + "Supported types: Integer, Long, Short, Byte.");
        }

        // 统一大写并确保偶数长度
        hexString = hexString.toUpperCase();
        if (hexString.length() % 2 == 1) {
            hexString = "0" + hexString;
        }

        // 若设置了 byteNum 并且当前长度不足，则补齐至 2 * byteNum
        if (byteNum != null && byteNum > 0 && hexString.length() < byteNum * 2) {
            int padLength = byteNum * 2 - hexString.length();
            String prefix = "0".repeat(padLength);
            hexString = prefix + hexString;
        }

        return hexString;
    }

    /**
     * 将Integer转换为十六进制字符串
     */
    private static String convertIntegerToHex(int value, Integer byteNum) {
        String hexString = Integer.toHexString(value);
        if (byteNum != null && byteNum > 0 && hexString.length() < byteNum * 2) {
            long unsignedValue = value & ((1L << (byteNum * 8)) - 1);
            hexString = Long.toHexString(unsignedValue);
        }
        return hexString.toUpperCase();
    }

    /**
     * 将Long转换为十六进制字符串
     */
    private static String convertLongToHex(long value, Integer byteNum) {
        String hexString = Long.toHexString(value);
        if (byteNum != null && byteNum > 0 && hexString.length() < byteNum * 2) {
            long mask = (1L << (byteNum * 8)) - 1;
            long unsignedValue = value & mask;
            hexString = Long.toHexString(unsignedValue);
        }
        return hexString.toUpperCase();
    }

    /**
     * 将Short转换为十六进制字符串
     */
    private static String convertShortToHex(short value, Integer byteNum) {
        int unsignedValue = value & 0xFFFF;
        String hexString = Integer.toHexString(unsignedValue);
        if (byteNum != null && byteNum > 0 && hexString.length() < byteNum * 2) {
            long mask = (1L << (byteNum * 8)) - 1;
            unsignedValue = (int) (unsignedValue & mask);
            hexString = Integer.toHexString(unsignedValue);
        }
        return hexString.toUpperCase();
    }

    /**
     * 将Byte转换为十六进制字符串
     */
    private static String convertByteToHex(byte value, Integer byteNum) {
        int unsignedValue = value & 0xFF;
        String hexString = Integer.toHexString(unsignedValue);
        if (byteNum != null && byteNum > 0 && hexString.length() < byteNum * 2) {
            long mask = (1L << (byteNum * 8)) - 1;
            unsignedValue = (int) (unsignedValue & mask);
            hexString = Integer.toHexString(unsignedValue);
        }
        return hexString.toUpperCase();
    }


    /**
     * <p>将数字转换为16进制字符串</p>
     * <p>注意转换后的字符串为偶数个字符，若为奇数个字符则自动左补零</p>
     *
     * @param number 待转换的数字
     * @return 16进制字符串
     */
    public static String numberToHexString(Number number) {
        return numberToHexString(number, null).toUpperCase();
    }

    /**
     * 将十六进制字符串转换为二进制字符串
     *
     * @param hexString 输入的十六进制字符串
     * @return 对应的二进制字符串表示
     */
    public static String hexToBinary(String hexString) {
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
    public static String binaryToHex(String binaryString) {
        if (StringUtils.isBlank(binaryString)) {
            return null;
        }
        BigInteger val = new BigInteger(binaryString.trim(), 2);
        String valString = val.toString(16).toUpperCase();
        return valString.length() % 2 == 0 ? valString : "0" + valString;
    }

    /**
     * 将任意 Number 转换为十六进制字符串。
     *
     * @param number 待转换的数值
     * @return 十六进制字符串（不含 "0x" 前缀），对于 BigDecimal 返回 "unscaledHex@scale"
     * @throws IllegalArgumentException 若 number 为 null 或类型不支持
     */
    public static String toHexString(Number number) {
        if (number == null) {
            throw new UncheckedException("Number cannot be null");
        }

        // 整数类型（包括 BigInteger）
        if (number instanceof BigInteger) {
            byte[] bytes = ((BigInteger) number).toByteArray();
            return bytesToHex(bytes).toUpperCase();
        }

        if (number instanceof Byte || number instanceof Short || number instanceof Integer || number instanceof Long) {
            BigInteger bigInt = BigInteger.valueOf(number.longValue());
            byte[] bytes = bigInt.toByteArray();
            return bytesToHex(bytes).toUpperCase();
        }

        // 单精度浮点数
        if (number instanceof Float) {
            int bits = Float.floatToIntBits((Float) number);
            return Integer.toHexString(bits).toUpperCase();
        }

        // 双精度浮点数
        if (number instanceof Double) {
            long bits = Double.doubleToLongBits((Double) number);
            return Long.toHexString(bits).toUpperCase();
        }

        // BigDecimal：返回 unscaledValue 的十六进制和 scale
        if (number instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) number;
            BigInteger unscaled = bd.unscaledValue();
            int scale = bd.scale();
            String unscaledHex = bytesToHex(unscaled.toByteArray());
            return unscaledHex + "@" + scale;
        }

        throw new UncheckedException("Unsupported Number type: " + number.getClass());
    }


    /**
     * 将十六进制字符串转换为BigDecimal数值
     *
     * @param s 十六进制字符串，支持标准的十六进制格式（如"FF"、"1A3B"等）
     * @return 转换后的BigDecimal数值的Optional包装，如果输入为空或null则返回Optional.empty()
     */
    public static Optional<BigDecimal> hexToNumber(String s) {
        if (StringUtils.isBlank(s)) {
            return Optional.empty();
        }
        return NumberUtil.parseHex(s);
    }

    /**
     * 对字符串进行右侧填充处理,使用字符'F'补齐至指定最小长度
     *
     * <p>处理逻辑:
     * <ul>
     *   <li>若目标长度小于等于0,直接返回原字符串或空字符串</li>
     *   <li>若字符串为null或空,生成由'F'字符组成的指定长度字符串</li>
     *   <li>若字符串长度小于目标长度,保留原内容并在右侧补'F'至目标长度</li>
     *   <li>若字符串长度大于或等于目标长度,直接返回原字符串不做修改</li>
     * </ul>
     *
     * <p>典型应用场景:
     * 用于SIM卡Profile中固定长度字段的填充,如PIN码、PUK码、ADM密钥等需要统一长度的场合
     *
     * <p>示例:
     * <ul>
     *   <li>输入 null, 4 → 输出 "FFFF"</li>
     *   <li>输入 "", 4 → 输出 "FFFF"</li>
     *   <li>输入 "123", 6 → 输出 "123FFF"</li>
     *   <li>输入 "12345", 3 → 输出 "12345"(超长不截断)</li>
     * </ul>
     *
     * @param inputString  待处理的原始字符串,允许为null或空
     * @param targetLength 目标最小长度,字符串将被补齐至该长度
     * @return 填充后的字符串, 长度至少为targetLength(若原字符串超长则可能更长)
     */
    public static String padRightWithF(String inputString, int targetLength) {
        // 校验目标长度参数的合法性
        if (targetLength <= 0) {
            return inputString == null ? "" : inputString;
        }

        // 清理输入字符串中的空白和不可见字符
        String cleanedInput = inputString == null ? "" : TextUtil.removeWhitespaceAndInvisible(inputString);

        // 若清理后的字符串长度已满足要求,直接返回
        if (cleanedInput.length() >= targetLength) {
            return cleanedInput;
        }

        // 构建填充结果:先添加原始内容,再在右侧补充'F'字符
        StringBuilder paddedResult = new StringBuilder(targetLength);
        paddedResult.append(cleanedInput);

        int remainingPaddingLength = targetLength - cleanedInput.length();
        for (int i = 0; i < remainingPaddingLength; i++) {
            paddedResult.append('F');
        }

        return paddedResult.toString().toUpperCase();
    }

    /**
     * 将字符串调整为固定长度：超出则截取，不足则用字符'F'右侧填充
     *
     * <p>处理逻辑：
     * <ul>
     *   <li>若目标长度小于等于0，返回空字符串</li>
     *   <li>若输入字符串为null，转换为空字符串并清理空白和不可见字符</li>
     *   <li>若字符串长度超过targetLength，截取前targetLength个字符</li>
     *   <li>若字符串长度不足targetLength，在右侧追加'F'字符直至达到targetLength</li>
     *   <li>若字符串长度恰好等于targetLength，直接返回原字符串</li>
     * </ul>
     *
     * <p>典型应用场景：
     * 用于SIM卡Profile中需要严格固定长度的字段，如SPN（服务提供者名称）、标识符等
     * 确保数据既不超过最大长度限制，也不因过短而导致格式错误
     *
     * <p>示例：
     * <ul>
     *   <li>输入 null, 8 → 输出 "FFFFFFFF"</li>
     *   <li>输入 "123", 8 → 输出 "123FFFFF"</li>
     *   <li>输入 "12345678", 8 → 输出 "12345678"</li>
     *   <li>输入 "123456789ABC", 8 → 输出 "12345678"(截断)</li>
     * </ul>
     *
     * @param inputString  待处理的原始字符串，允许为null
     * @param targetLength 目标固定长度，字符串将被严格调整为该长度
     * @return 调整后的字符串，长度严格等于targetLength
     */
    public static String fixLengthWithF(String inputString, int targetLength) {
        // 校验目标长度参数的合法性
        if (targetLength <= 0) {
            return "";
        }

        // 清理输入字符串中的空白和不可见字符
        String cleanedInput = inputString == null ? "" : TextUtil.removeWhitespaceAndInvisible(inputString);

        // 若清理后的字符串长度已达到或超过目标长度，进行截断处理
        if (cleanedInput.length() >= targetLength) {
            return cleanedInput.substring(0, targetLength);
        }

        // 构建固定长度结果：先添加原始内容，再在右侧补充'F'字符至目标长度
        StringBuilder fixedLengthResult = new StringBuilder(targetLength);
        fixedLengthResult.append(cleanedInput);

        int remainingPaddingLength = targetLength - cleanedInput.length();
        for (int i = 0; i < remainingPaddingLength; i++) {
            fixedLengthResult.append('F');
        }

        return fixedLengthResult.toString().toUpperCase();
    }
}