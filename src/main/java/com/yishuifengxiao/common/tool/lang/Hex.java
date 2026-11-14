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
 *     <li>十六进制字符串与字节数组互转</li>
 *     <li>十六进制与 UTF-8 字符串互转</li>
 *     <li>十六进制与 Base64 互转</li>
 *     <li>十六进制与 BitSet 互转</li>
 *     <li>数字转十六进制字符串</li>
 * </ul>
 *
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class Hex {

    /**
     * 正则表达式模式，用于匹配有效的十六进制字符串。
     * 匹配任意长度的十六进制字符（大小写字母和数字），要求至少一个字符。
     */
    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9a-fA-F]+$");

    /**
     * 检查是否为有效的十六进制字符串。
     * <p>有效条件：非 null、非空、去除首尾空白后长度为偶数，且仅包含 0-9、a-f、A-F。</p>
     *
     * @param str 待检查的字符串
     * @return 如果字符串是有效的十六进制格式且长度为偶数则返回 true，否则返回 false
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
        // 使用 ISO-8859-1（Latin-1）解码，覆盖 0x00~0xFF 全字节范围（扩展 ASCII）
        return new String(bytes, StandardCharsets.ISO_8859_1);
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
        if (ascii.isEmpty()) {
            return "";
        }

        final char[] hexChars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder(ascii.length() * 2);
        for (char c : ascii.toCharArray()) {
            // 只取低 8 位，确保 0~255 范围
            int val = c & 0xFF;
            sb.append(hexChars[val >>> 4]);
            sb.append(hexChars[val & 0x0F]);
        }
        return sb.toString();
    }

    /**
     * 将 UTF-8 编码的字符串转换为小写十六进制字符串。
     *
     * @param str 传入的 UTF-8 编码字符串
     * @return 小写十六进制字符串表示；若输入为 null 则返回 null
     */
    public static String utf8ToHex(String str) {
        if (null == str) {
            return null;
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        final String hexString = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            sb.append(hexString.charAt((aByte & 0xf0) >> 4));
            sb.append(hexString.charAt((aByte & 0x0f)));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串转换为 UTF-8 编码的字符串。
     *
     * @param hexStr 输入的十六进制字符串，每两个字符表示一个字节
     * @return 转换后的 UTF-8 编码字符串；若输入为 null 则返回 null
     */
    public static String hexToUtf8(String hexStr) {
        if (null == hexStr) {
            return null;
        }
        String clean = TextUtil.removeWhitespaceAndInvisible(hexStr);
        if (clean.isEmpty()) {
            return "";
        }
        if (clean.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even, but got " + clean.length());
        }
        byte[] resultBytes = new byte[clean.length() / 2];
        for (int i = 0; i < clean.length(); i += 2) {
            int high = Character.digit(clean.charAt(i), 16);
            int low = Character.digit(clean.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character at position " + i);
            }
            resultBytes[i / 2] = (byte) ((high << 4) + low);
        }

        return new String(resultBytes, StandardCharsets.UTF_8);
    }

    /**
     * 将字节数组转换为大写十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串，每两个字符代表一个字节；如果输入为 null 或空数组则返回空字符串
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

        return hexString.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组。
     * <p>会自动移除空白和不可见字符。无效输入返回 null。</p>
     *
     * @param hex 十六进制字符串
     * @return 字节数组，如果输入无效则返回 null
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null) {
            return null;
        }

        String clean = TextUtil.removeWhitespaceAndInvisible(hex);
        if (!isHex(clean)) {
            log.warn("非法的十六进制字符串: {}", hex);
            return null;
        }

        int len = clean.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int firstDigit = Character.digit(clean.charAt(i), 16);
            int secondDigit = Character.digit(clean.charAt(i + 1), 16);
            data[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }

        return data;
    }

    /**
     * 将 Base64 字符串转换为十六进制字符串（兼容 Java 8+）。
     *
     * @param base64String Base64 编码的字符串
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
     * 解码 Base64 字符串并返回对应的字节数组。
     * <p>先移除空白和不可见字符，然后优先使用标准 Base64 解码器；
     * 若失败再尝试 URL 安全 Base64 解码器。</p>
     *
     * @param base64String Base64 编码的字符串，可以是标准或 URL 安全格式
     * @return 解码后的字节数组，如果输入为空或解码失败则返回 null
     */
    public static byte[] base64ToBytes(String base64String) {
        if (base64String == null) {
            return null;
        }

        String cleaned = TextUtil.removeWhitespaceAndInvisible(base64String);
        if (cleaned.isEmpty()) {
            return null;
        }

        try {
            return Base64.getDecoder().decode(cleaned);
        } catch (IllegalArgumentException e1) {
            try {
                return Base64.getUrlDecoder().decode(cleaned);
            } catch (IllegalArgumentException e2) {
                log.debug("Base64 解码失败: {}", e2.getMessage());
                return null;
            }
        }
    }

    /**
     * 将十六进制字符串转换为 Base64 字符串（兼容 Java 8+）。
     * <p>仅移除开头的 "0x" 或 "0X" 前缀，不会移除字符串中间出现的 "0x"，
     * 避免掩盖非法输入。</p>
     *
     * @param hexString 十六进制字符串
     * @return Base64 编码的字符串；若输入为空或仅空白返回空字符串；无效十六进制返回 null
     */
    public static String hexToBase64(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return "";
        }

        String cleanHex = TextUtil.removeWhitespaceAndInvisible(hexString);
        if (cleanHex.startsWith("0x") || cleanHex.startsWith("0X")) {
            cleanHex = cleanHex.substring(2);
        }
        if (cleanHex.isEmpty()) {
            return "";
        }
        if (!isHex(cleanHex)) {
            log.warn("Invalid hex string: {}", hexString);
            return null;
        }

        byte[] bytes = new byte[cleanHex.length() / 2];
        for (int i = 0; i < cleanHex.length(); i += 2) {
            int firstDigit = Character.digit(cleanHex.charAt(i), 16);
            int secondDigit = Character.digit(cleanHex.charAt(i + 1), 16);
            if (firstDigit == -1 || secondDigit == -1) {
                return null;
            }
            bytes[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }

        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 对 hex 字符串进行左侧填充到指定字节数。
     * <p>支持可选的 "0x"/"0X" 前缀，填充后保留原始前缀大小写格式。</p>
     *
     * @param hexString   原始 hex 字符串
     * @param targetBytes 目标字节数，必须非负
     * @return 填充后的 hex 字符串（大写），如果输入无效则返回 null
     * @throws IllegalArgumentException 如果 targetBytes 为负数
     */
    public static String padHexLeft(String hexString, int targetBytes) {
        if (hexString == null) {
            log.warn("Invalid hex string: {}", hexString);
            return null;
        }
        if (targetBytes < 0) {
            throw new IllegalArgumentException("targetBytes must be non-negative");
        }

        String prefix = "";
        String cleanHex = hexString.trim();
        if (cleanHex.startsWith("0x") || cleanHex.startsWith("0X")) {
            prefix = cleanHex.substring(0, 2);
            cleanHex = cleanHex.substring(2);
        }
        cleanHex = TextUtil.removeWhitespaceAndInvisible(cleanHex);

        if (!HEX_PATTERN.matcher(cleanHex).matches() || cleanHex.length() % 2 != 0) {
            log.warn("Invalid hex string: {}", hexString);
            return null;
        }

        int targetLength = targetBytes * 2;
        int currentLength = cleanHex.length();

        if (currentLength >= targetLength) {
            return prefix + cleanHex.toUpperCase();
        }

        StringBuilder padded = new StringBuilder(targetLength);
        for (int i = 0; i < targetLength - currentLength; i++) {
            padded.append('0');
        }
        padded.append(cleanHex);

        return prefix + padded.toString().toUpperCase();
    }

    /**
     * 补零辅助方法：在字符串左侧填充零字符，使其达到指定长度。
     * <p>返回结果统一转为大写。</p>
     *
     * @param str 需要补零的原始字符串，允许为 null
     * @param len 目标长度
     * @return 补零后的字符串；如果原字符串长度已达到或超过目标长度则返回原字符串（大写）
     */
    public static String padWithZero(String str, int len) {
        if (str == null) {
            str = "";
        }
        str = str.trim();
        if (str.length() >= len) {
            return str.toUpperCase();
        }

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len - str.length(); i++) {
            sb.append('0');
        }
        sb.append(str);
        return sb.toString().toUpperCase();
    }

    /**
     * 对两个字节数组进行异或运算。
     *
     * @param b1 第一个字节数组
     * @param b2 第二个字节数组
     * @return 两个数组对应位置字节异或后的结果数组，长度为两个输入数组的最小长度；
     *         如果任一输入为 null 或空数组则返回空数组
     */
    public static byte[] xOr(byte[] b1, byte[] b2) {
        if (b1 == null || b2 == null || b1.length == 0 || b2.length == 0) {
            return new byte[0];
        }

        byte[] tXor = new byte[Math.min(b1.length, b2.length)];
        for (int i = 0; i < tXor.length; i++) {
            tXor[i] = (byte) (b1[i] ^ b2[i]);
        }
        return tXor;
    }

    /**
     * 交换字符串中相邻的字符。
     * <p>例如："abcdef" 输出 "bacfde"；"abcde" 输出 "badce"。</p>
     *
     * @param s 输入的字符串
     * @return 交换相邻字符后得到的新字符串；如果输入为 null 则返回 null
     */
    public static String swapPairs(String s) {
        if (s == null) {
            return null;
        }

        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length - 1; i += 2) {
            char temp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = temp;
        }

        return new String(chars);
    }

    /**
     * <p>将数字转换为指定字节数为 16 进制字符串。</p>
     *
     * <p>规则：</p>
     * <ul>
     *   <li>转换结果至少为 2*byteNum 个字符；若原值所需长度更大则不截断；</li>
     *   <li>若原值表示不足目标长度，则左侧补齐。补齐策略分两段：</li>
     *   <li>① 符号扩展段：最多补到原始数值类型的宽度（Byte=1、Short=2、Integer=4、Long=8 字节）。
     *       负数补 'F'（补码符号位），正数补 '0'；</li>
     *   <li>② 零扩展段：若 byteNum 大于原始类型字节数，超过部分补 '0'
     *       （因为 Java 中不存在比原始类型更宽的整型，拓宽等价于零扩展）。</li>
     * </ul>
     *
     * <p>示例：</p>
     * <ul>
     *   <li>Integer 5, byteNum=8 → "0000000000000005"</li>
     *   <li>Integer -1, byteNum=8 → "00000000FFFFFFFF"（Integer 只有 4 字节，超出部分零扩展）</li>
     *   <li>Long -1, byteNum=8 → "FFFFFFFFFFFFFFFF"（Long 本身 8 字节，全部为符号扩展）</li>
     * </ul>
     *
     * @param number  待转换的数字，允许为 null
     * @param byteNum 转换后的最小字节数；为 null 或 0 时只保证偶数长度，不补足到指定字节数
     * @return 十六进制字符串；若 number 为 null 则返回空字符串
     * @throws IllegalArgumentException 如果 byteNum 为负数，或数值类型不支持
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
        boolean negative;
        // 原始数值类型所占的字节数，用于确定"符号扩展"的最大宽度
        int typeBytes;

        if (number instanceof Integer i) {
            hexString = Integer.toHexString(i);
            negative = i < 0;
            typeBytes = Integer.BYTES;
        } else if (number instanceof Long l) {
            hexString = Long.toHexString(l);
            negative = l < 0;
            typeBytes = Long.BYTES;
        } else if (number instanceof Short s) {
            // Short 转无符号 int 后生成 hex，负数得到固定 4 字符（2 字节）补码表示
            int unsignedValue = s & 0xFFFF;
            hexString = Integer.toHexString(unsignedValue);
            negative = s < 0;
            typeBytes = Short.BYTES;
        } else if (number instanceof Byte b) {
            // Byte 转无符号 int 后生成 hex，负数得到固定 2 字符（1 字节）补码表示
            int unsignedValue = b & 0xFF;
            hexString = Integer.toHexString(unsignedValue);
            negative = b < 0;
            typeBytes = Byte.BYTES;
        } else if (number instanceof Double || number instanceof Float) {
            throw new IllegalArgumentException("Floating point numbers are not supported.");
        } else {
            throw new IllegalArgumentException("Unsupported number type: "
                    + number.getClass().getSimpleName()
                    + ". Supported types: Integer, Long, Short, Byte.");
        }

        // 统一大写
        hexString = hexString.toUpperCase();

        // 确保偶数长度。负数补 'F'（保持补码符号位），正数补 '0'
        if (hexString.length() % 2 == 1) {
            hexString = (negative ? "F" : "0") + hexString;
        }

        // 按 byteNum 补齐：
        //   符号扩展段：补到 min(byteNum, typeBytes) 字节
        //   零扩展段  ：从 typeBytes 字节到 byteNum 字节（仅在 byteNum > typeBytes 时存在）
        if (byteNum != null && byteNum > 0) {
            int targetLength = byteNum * 2;
            if (hexString.length() < targetLength) {
                int padLength = targetLength - hexString.length();
                int typeMaxHexLength = typeBytes * 2;

                // 符号扩展段：从当前长度延伸到 min(targetLength, typeBytes*2)
                int signedPadEnd = Math.min(targetLength, typeMaxHexLength);
                int signedPadLength = Math.max(0, signedPadEnd - hexString.length());
                char signChar = negative ? 'F' : '0';

                // 零扩展段：超过原始类型宽度的部分补 '0'
                int zeroPadLength = padLength - signedPadLength;

                StringBuilder padded = new StringBuilder(targetLength);
                for (int i = 0; i < signedPadLength; i++) {
                    padded.append(signChar);
                }
                for (int i = 0; i < zeroPadLength; i++) {
                    padded.append('0');
                }
                padded.append(hexString);
                hexString = padded.toString();
            }
        }

        return hexString;
    }

    /**
     * <p>将数字转换为 16 进制字符串。</p>
     * <p>注意：转换后的字符串为偶数个字符，若为奇数个字符则自动左补零。</p>
     *
     * @param number 待转换的数字，允许为 null
     * @return 十六进制字符串；若 number 为 null 则返回空字符串
     */
    public static String numberToHexString(Number number) {
        return numberToHexString(number, null).toUpperCase();
    }

    /**
     * 将十六进制字符串转换为二进制字符串。
     * <p>返回的二进制字符串长度为偶数，若为奇数则左侧补一个 '0'。</p>
     *
     * @param hexString 输入的十六进制字符串
     * @return 对应的二进制字符串表示；若输入为 null 或空白则返回 null
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
     * 将二进制字符串转换为十六进制字符串。
     *
     * @param binaryString 输入的二进制字符串
     * @return 对应的十六进制字符串表示（大写）；若输入为 null 或空白则返回 null
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
     * <p>说明：</p>
     * <ul>
     *     <li>整数类型（包括 BigInteger）使用补码字节数组表示，可能包含符号前导字节；</li>
     *     <li>Float/Double 使用其 IEEE 754 位表示的十六进制（不补前导零）；</li>
     *     <li>BigDecimal 返回 "unscaledHex@scale" 格式；</li>
     *     <li>不支持的 Number 类型抛出 {@link UncheckedException}。</li>
     * </ul>
     *
     * @param number 待转换的数值
     * @return 十六进制字符串（不含 "0x" 前缀）
     * @throws UncheckedException 若 number 为 null 或类型不支持
     */
    public static String toHexString(Number number) {
        if (number == null) {
            throw new UncheckedException("Number cannot be null");
        }

        if (number instanceof BigInteger) {
            byte[] bytes = ((BigInteger) number).toByteArray();
            return bytesToHex(bytes).toUpperCase();
        }

        if (number instanceof Byte || number instanceof Short
                || number instanceof Integer || number instanceof Long) {
            BigInteger bigInt = BigInteger.valueOf(number.longValue());
            byte[] bytes = bigInt.toByteArray();
            return bytesToHex(bytes).toUpperCase();
        }

        if (number instanceof Float) {
            int bits = Float.floatToIntBits((Float) number);
            return Integer.toHexString(bits).toUpperCase();
        }

        if (number instanceof Double) {
            long bits = Double.doubleToLongBits((Double) number);
            return Long.toHexString(bits).toUpperCase();
        }

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
     * 将十六进制字符串转换为 BigDecimal 数值。
     *
     * @param s 十六进制字符串，支持标准的十六进制格式（如 "FF"、"1A3B" 等）
     * @return 转换后的 BigDecimal 数值的 Optional 包装；如果输入为空或 null 则返回 Optional.empty()
     */
    public static Optional<BigDecimal> hexToNumber(String s) {
        if (StringUtils.isBlank(s)) {
            return Optional.empty();
        }
        return NumberUtil.parseHex(s);
    }

    /**
     * 对字符串进行右侧填充处理，使用字符 'F' 补齐至指定最小长度。
     *
     * <p>处理逻辑：</p>
     * <ul>
     *     <li>若目标长度小于等于 0，直接返回原字符串（转大写）或空字符串；</li>
     *     <li>若字符串为 null 或空，生成由 'F' 字符组成的指定长度字符串；</li>
     *     <li>若字符串长度小于目标长度，保留原内容并在右侧补 'F' 至目标长度；</li>
     *     <li>若字符串长度大于或等于目标长度，直接返回原字符串（转大写）不做修改。</li>
     * </ul>
     *
     * <p>典型应用场景：用于 SIM 卡 Profile 中固定长度字段的填充，如 PIN 码、PUK 码、ADM 密钥等。</p>
     *
     * @param inputString  待处理的原始字符串，允许为 null 或空
     * @param targetLength 目标最小长度，字符串将被补齐至该长度
     * @return 填充后的字符串（大写），长度至少为 targetLength（若原字符串超长则可能更长）
     */
    public static String padRightWithF(String inputString, int targetLength) {
        if (targetLength <= 0) {
            return inputString == null ? "" : inputString.toUpperCase();
        }

        String cleanedInput = inputString == null ? "" : TextUtil.removeWhitespaceAndInvisible(inputString);

        if (cleanedInput.length() >= targetLength) {
            return cleanedInput.toUpperCase();
        }

        StringBuilder paddedResult = new StringBuilder(targetLength);
        paddedResult.append(cleanedInput);

        int remainingPaddingLength = targetLength - cleanedInput.length();
        for (int i = 0; i < remainingPaddingLength; i++) {
            paddedResult.append('F');
        }

        return paddedResult.toString().toUpperCase();
    }

    /**
     * 将字符串调整为固定长度：超出则截取，不足则用字符 'F' 右侧填充。
     *
     * <p>处理逻辑：</p>
     * <ul>
     *     <li>若目标长度小于等于 0，返回空字符串；</li>
     *     <li>若输入字符串为 null，转换为空字符串并清理空白和不可见字符；</li>
     *     <li>若字符串长度超过 targetLength，截取前 targetLength 个字符；</li>
     *     <li>若字符串长度不足 targetLength，在右侧追加 'F' 字符直至达到 targetLength；</li>
     *     <li>若字符串长度恰好等于 targetLength，直接返回原字符串。</li>
     * </ul>
     *
     * <p>典型应用场景：用于 SIM 卡 Profile 中需要严格固定长度的字段，如 SPN、标识符等。</p>
     *
     * @param inputString  待处理的原始字符串，允许为 null
     * @param targetLength 目标固定长度，字符串将被严格调整为该长度
     * @return 调整后的字符串（大写），长度严格等于 targetLength
     */
    public static String fixLengthWithF(String inputString, int targetLength) {
        if (targetLength <= 0) {
            return "";
        }

        String cleanedInput = inputString == null ? "" : TextUtil.removeWhitespaceAndInvisible(inputString);

        if (cleanedInput.length() >= targetLength) {
            return cleanedInput.substring(0, targetLength).toUpperCase();
        }

        StringBuilder fixedLengthResult = new StringBuilder(targetLength);
        fixedLengthResult.append(cleanedInput);

        int remainingPaddingLength = targetLength - cleanedInput.length();
        for (int i = 0; i < remainingPaddingLength; i++) {
            fixedLengthResult.append('F');
        }

        return fixedLengthResult.toString().toUpperCase();
    }
}