package com.yishuifengxiao.common.tool.lang;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

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
        return null == value ? defaultValue : value;
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
        if (val == null || StringUtils.isBlank(val.toString())) {
            return Optional.empty();
        }

        try {
            // 直接处理常见的数字类型，避免精度丢失
            if (val instanceof BigDecimal) {
                return Optional.of((BigDecimal) val);
            } else if (val instanceof Integer) {
                return Optional.of(new BigDecimal((Integer) val));
            } else if (val instanceof Long) {
                return Optional.of(new BigDecimal((Long) val));
            } else if (val instanceof Double) {
                Double doubleVal = (Double) val;
                if (doubleVal.isNaN() || doubleVal.isInfinite()) {
                    return Optional.empty();
                }
                return Optional.of(BigDecimal.valueOf(doubleVal));
            } else if (val instanceof Float) {
                Float floatVal = (Float) val;
                if (floatVal.isNaN() || floatVal.isInfinite()) {
                    return Optional.empty();
                }
                return Optional.of(BigDecimal.valueOf(floatVal));
            }

            // 处理字符串类型
            String cleanStr = val.toString().replaceAll(",", "").trim();
            return Optional.of(new BigDecimal(cleanStr));
        } catch (NumberFormatException e) {
            log.warn("Failed to convert [{}] to BigDecimal", val, e);
        } catch (Exception e) {
            log.warn("Unexpected error when converting [{}] to BigDecimal", val, e);
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
        if (hexString == null || StringUtils.isBlank(hexString)) {
            return Optional.empty();
        }
        try {
            String cleanHex = hexString.replaceAll(",", "").trim();
            return Optional.of(new BigDecimal(new BigInteger(cleanHex, 16)));
        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to convert hex string [{}] to BigDecimal", hexString, e);
            }
        }
        return Optional.empty();
    }

    /**
     * 第一个数是否大于第二个数
     *
     * @param v1 第一个数，若为null直接返回为false
     * @param v2 第二个数，若为null直接返回为true
     * @return 第一个数大于第二个数返回为true, 否则为false
     */
    public static boolean gt(Number v1, Number v2) {
        if (null == v1) {
            return false;
        }
        if (null == v2) {
            return true;
        }
        return new BigDecimal(v1.toString()).compareTo(new BigDecimal(v2.toString())) > 0;
    }

    /**
     * 第一个数是否大于或等于第二个数
     *
     * @param v1 第一个数，若为null直接返回为false
     * @param v2 第二个数，若为null直接返回为true
     * @return 第一个数大于或等于第二个数返回为true, 否则为false
     */
    public static boolean gte(Number v1, Number v2) {
        if (null == v1) {
            return false;
        }
        if (null == v2) {
            return true;
        }
        return new BigDecimal(v1.toString()).compareTo(new BigDecimal(v2.toString())) >= 0;
    }

    /**
     * 第一个数是否小于第二个数
     *
     * @param v1 第一个数，若为null直接返回为true
     * @param v2 第二个数，若为null直接返回为false
     * @return 第一个数小于第二个数返回为true, 否则为false
     */
    public static boolean lt(Number v1, Number v2) {
        if (null == v1) {
            return true;
        }
        if (null == v2) {
            return false;
        }
        return new BigDecimal(v1.toString()).compareTo(new BigDecimal(v2.toString())) < 0;
    }

    /**
     * 第一个数是否小于或等于第二个数
     *
     * @param v1 第一个数，若为null直接返回为true
     * @param v2 第二个数，若为null直接返回为false
     * @return 第一个数小于或等于第二个数返回为true, 否则为false
     */
    public static boolean lte(Number v1, Number v2) {
        if (null == v1) {
            return true;
        }
        if (null == v2) {
            return false;
        }
        return new BigDecimal(v1.toString()).compareTo(new BigDecimal(v2.toString())) <= 0;
    }

    /**
     * <p>
     * 判断两个数据是否相等
     * </p>
     * <p style="color:yellow">
     * 若输入的值为null直接返回为false
     * </p>
     *
     * @param originalValue 原始值，如果原始值为null直接返回为 false
     * @param value         被比较值,若为null直接返回false
     * @return 如果两个值相等返回为true, 否则为false
     */
    public static boolean equals(Number originalValue, Number value) {
        if (null == originalValue || null == value) {
            return false;
        }

        return new BigDecimal(originalValue.toString()).compareTo(new BigDecimal(value.toString())) == 0;
    }

    /**
     * <p>
     * 判断输入值是否大于或等于0
     * </p>
     * <p>输入值若为null直接返回为false</p>
     *
     * @param value 需要判断的输入值，若为null直接返回为false
     * @return 输入值大于或等于0返回为true, 否则为false
     */
    public static boolean gteZero(Number value) {
        return gte(value, NumberUtil.ZERO);
    }

    /**
     * <p>
     * 判断输入值是否大于0
     * </p>
     * <p>输入值若为null直接返回为false</p>
     *
     * @param value 需要判断的输入值，若为null直接返回为false
     * @return 输入值大于0返回为true, 否则为false
     */
    public static boolean gtZero(Number value) {
        return gt(value, NumberUtil.ZERO);
    }

    /**
     * <p>
     * 判断输入值是否小于或等于0
     * </p>
     * <p>输入值若为null直接返回为true</p>
     *
     * @param value 需要判断的输入值，若为null直接返回为true
     * @return 输入值小于或等于0返回为true, 否则为false
     */
    public static boolean lteZero(Number value) {
        return lte(value, NumberUtil.ZERO);
    }

    /**
     * <p>
     * 判断输入值是否小于0
     * </p>
     * <p>输入值若为null直接返回为true</p>
     *
     * @param value 需要判断的输入值，若为null直接返回为true
     * @return 输入值小于0返回为true, 否则为false
     */
    public static boolean ltZero(Number value) {
        return lt(value, NumberUtil.ZERO);
    }

    /**
     * <p>
     * 判断输入值是否等于0
     * </p>
     *
     * @param value 需要判断的输入值，若为null直接返回为false
     * @return 输入值等于0返回为true, 否则为false
     */
    public static boolean isZero(Number value) {
        return equals(value, NumberUtil.ZERO);
    }

    /**
     * 判断数据里是否有等于目标数据的数字
     *
     * @param originalValue 目标数据，如果为null直接返回为false
     * @param values        待比较的数据，如果为null直接返回为false
     * @return 待比较的数据里包含了目标数据就返回为true，否则为false
     */
    public static boolean containsAny(Number originalValue, Number... values) {
        if (null == originalValue || null == values) {
            return false;
        }
        for (Number value : values) {
            if (value != null && equals(originalValue, value)) {
                return true;
            }
        }
        return false;
    }


}
