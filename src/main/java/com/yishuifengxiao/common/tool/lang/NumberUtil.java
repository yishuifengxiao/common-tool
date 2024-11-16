package com.yishuifengxiao.common.tool.lang;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <p>
 * 数字转换与操作比较工具
 * </p>
 * 该工具主要是将给定的数字与0进行比较和将数字转换成boolean以及将字符串解析成数字。该工具的主要功能如下：
 * <ol>
 * <li>判断给定的数字是否小于或等于0</li>
 * <li>判断给定的数字是否小于0</li>
 * <li>将数字转换成boolean值</li>
 * <li>将boolean值转换成数字</li>
 * <li>获取封装数据类型里的数据，防止出现NPE</li>
 * <li>将字符串解析为数字</li>
 * </ol>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class NumberUtil {
    /**
     * 零值
     */
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    /**
     * 一值(1)
     */
    public static final BigDecimal ONE = BigDecimal.valueOf(1);
    /**
     * 十值(10)
     */
    public static final BigDecimal TEN = BigDecimal.valueOf(10);
    /**
     * 百值(100)
     */
    public static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    /**
     * 千值(1000)
     */
    public static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);
    /**
     * 万值(10000)
     */
    public static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10000);
    /**
     * 亿值(100000000)
     */
    public static final BigDecimal HUNDRED_MILLION = BigDecimal.valueOf(100000000);

    /**
     * <p>
     * 国际单位制(SI)存储单位阶段值(1024)
     * </p>
     * <ul>
     * <li>1KB=1024B；1MB=1024KB=1024×1024B。</li>
     * <li>1B（byte，字节）= 8 bit；</li>
     * <li>1KB（Kilobyte，千字节）=1024B= 2^10 B；</li>
     * <li>1MB（Megabyte，兆字节，百万字节，简称“兆”）=1024KB= 2^20 B；</li>
     * <li>1GB（Gigabyte，吉字节，十亿字节，又称“千兆”）=1024MB= 2^30 B；</li>
     * </ul>
     */
    public static final BigDecimal RATE_1024 = BigDecimal.valueOf(1024);


    /**
     * 将字符串转为Double
     *
     * @param str 需要解析的字符串
     * @return 解析成功则返回Double的数据，否则为null
     */
    public static Double parseDouble(String str) {
        return parse(str).map(BigDecimal::doubleValue).orElse(null);
    }

    /**
     * 将字符串转为Double
     *
     * @param str          需要解析的字符串
     * @param defaultValue 解析失败时的默认值
     * @return 解析成功则返回Double的数据，否则为defaultValue
     */
    public static Double parseDouble(String str, double defaultValue) {
        Double val = parseDouble(str);
        return null == val ? defaultValue : val;
    }

    /**
     * 将字符串转为Float
     *
     * @param str 需要解析的字符串
     * @return 解析成功则返回Float的数据，否则为null
     */
    public static Float parseFloat(String str) {
        return parse(str).map(BigDecimal::floatValue).orElse(null);
    }

    /**
     * 将字符串转为Float
     *
     * @param str          需要解析的字符串
     * @param defaultValue 解析失败时的默认值
     * @return 解析成功则返回Float的数据，否则为defaultValue
     */
    public static Float parseFloat(String str, float defaultValue) {
        Float value = parseFloat(str);
        return null == value ? defaultValue : value.floatValue();
    }

    /**
     * 将字符串转为Integer
     *
     * @param str 需要解析的字符串
     * @return 解析成功则返回Integer的数据，否则为null
     */
    public static Integer parseInt(String str) {
        return parse(str).map(BigDecimal::intValue).orElse(null);
    }

    /**
     * 将字符串转为Integer
     *
     * @param str          需要解析的字符串
     * @param defaultValue 解析失败时的默认值
     * @return 解析成功则返回Integer的数据，否则为defaultValue
     */
    public static Integer parseInt(String str, int defaultValue) {
        Integer value = parseInt(str);
        return null == value ? defaultValue : value;
    }

    /**
     * 将字符串转为Long
     *
     * @param str 需要解析的字符串
     * @return 解析成功则返回Long的数据，否则为null
     */
    public static Long parseLong(String str) {
        return parse(str).map(BigDecimal::longValue).orElse(null);
    }

    /**
     * 将字符串转为Long
     *
     * @param str          需要解析的字符串
     * @param defaultValue 解析失败时的默认值
     * @return 解析成功则返回Long的数据，否则为defaultValue
     */
    public static Long parseLong(String str, long defaultValue) {
        Long value = parseLong(str);
        return null == value ? defaultValue : value;
    }

    /**
     * 将输入值转换为 BigDecimal
     *
     * @param val 输入值
     * @return 转换后的 BigDecimal ，若转换失败则返回为null
     */
    public static Optional<BigDecimal> parse(Object val) {
        try {
            if (null == val || StringUtils.isBlank(val.toString())) {
                return Optional.empty();
            }
            BigDecimal number = new BigDecimal(val.toString().replaceAll(",", "").trim());
            return Optional.ofNullable(number);
        } catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("There was a problem converting data [{}] to numerical values, and the reason for the " +
                        "problem is" +
                        " {}", val, e);
            }
        }

        return Optional.empty();
    }

    /**
     * 将16进制字符串数据转为10进制数字
     *
     * @param hexString 待转换的字符串
     * @return 转换后的10进制数字
     */
    public static Optional<BigDecimal> parseHex(String hexString) {
        try {
            if (null == hexString || StringUtils.isBlank(hexString.toString())) {
                return Optional.empty();
            }
            BigDecimal number = new BigDecimal(new BigInteger(hexString.toString().replaceAll(",", "").trim(), 16));
            return Optional.ofNullable(number);
        } catch (Throwable e) {
            if (log.isInfoEnabled()) {
                log.info("There was a problem converting data [{}] to numerical values, and the reason for the " +
                        "problem is" +
                        " {}", hexString, e);
            }
        }
        return Optional.empty();
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

        String hexString = "";
        if (null != number) {
            if (number instanceof Integer) {
                hexString = Integer.toHexString(number.intValue()).toUpperCase(); // 转为16进制并大写
            } else if (number instanceof Long) {
                hexString = Long.toHexString(number.longValue()).toUpperCase(); // 转为16进制并大写
            } else if (number instanceof Short) {
                hexString = Integer.toHexString(number.shortValue()).toUpperCase(); // 转为16进制并大写
            } else if (number instanceof Byte) {
                hexString = Integer.toHexString(number.byteValue()).toUpperCase(); // 转为16进制并大写
            } else if (number instanceof Double || number instanceof Float) {
                // 如果是浮点数，通常我们只处理其整数部分
                hexString = Long.toHexString(number.longValue()).toUpperCase(); // 转为16进制并大写
            } else {
                throw new IllegalArgumentException("Unsupported number type.");
            }
        }
        if (hexString.length() % 2 == 1) {
            hexString = "0" + hexString;
        }
        if (null != byteNum && byteNum > 0 && hexString.length() < byteNum * 2) {
            String prefix =
                    IntStream.range(0, byteNum * 2 - hexString.length()).mapToObj(v -> "0").collect(Collectors.joining());
            hexString = prefix + hexString;
        }
        return hexString;
    }

    /**
     * <p>将数字转换指定字节数为16进制字符串</p>
     * <p>注意转换后的字符串为偶数个字符，即为2*byteNum个字符数，若为奇数个字符则自动左补零</p>
     * <p>转换后的字符的长度恰好2*byteNum个字符数，故转换后的</p>
     *
     * @param number  待转换的数字
     * @param byteNum 转换后的字节数，例如byteNum为2时表示最短的字符数为 2*2
     * @return 16进制字符串
     */
    public static String toHex(Number number, Integer byteNum) {
        String hexString = toHexString(number, byteNum);
        if (null == byteNum || byteNum <= 0) {
            return hexString;
        }
        if (hexString.length() > (byteNum * 2)) {
            hexString = hexString.substring(0, byteNum * 2);
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
