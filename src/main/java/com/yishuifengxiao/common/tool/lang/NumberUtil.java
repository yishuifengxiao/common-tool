package com.yishuifengxiao.common.tool.lang;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

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
    public static final BigDecimal THOUNSAND = BigDecimal.valueOf(1000);
    /**
     * 万值(10000)
     */
    public static final BigDecimal TEN_HUNDRED = BigDecimal.valueOf(10000);
    /**
     * 亿值(100000000)
     */
    public static final BigDecimal HUNDRED_MILLION = BigDecimal.valueOf(100000000);

    /**
     * <p>国际单位制(SI)存储单位阶段值(1024)</p>
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
     * <h3>判断输入值是否大于或等于0</h3>
     * <p style="color:yellow">若输入的值为null或非数字类型直接返回为false</p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值大于或等于0返回为true, 否则为false
     */
    public static boolean gteZero(Number value) {
        if (null == value) {
            return false;
        }
        return BigDecimal.valueOf(value.doubleValue()).compareTo(ZERO) >= 0;
    }


    /**
     * <h3>判断输入值是否大于0</h3>
     * <p style="color:yellow">若输入的值为null或非数字类型直接返回为false</p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值大于0返回为true, 否则为false
     */
    public static boolean gtZero(Number value) {
        if (null == value) {
            return false;
        }
        return BigDecimal.valueOf(value.doubleValue()).compareTo(ZERO) > 0;
    }

    /**
     * <h3>判断输入值是否小于或等于0</h3>
     * <p style="color:yellow">若输入的值为null或非数字类型直接返回为false</p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值小于或等于0返回为true, 否则为false
     */
    public static boolean lteZero(Number value) {
        if (null == value) {
            return false;
        }
        return BigDecimal.valueOf(value.doubleValue()).compareTo(ZERO) <= 0;
    }


    /**
     * <h3>判断输入值是否小于0</h3>
     * <p style="color:yellow">若输入的值为null或非数字类型直接返回为false</p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值小于0返回为true, 否则为false
     */
    public static boolean ltZero(Number value) {
        if (null == value) {
            return false;
        }
        return BigDecimal.valueOf(value.doubleValue()).compareTo(ZERO) < 0;
    }


    /**
     * <h3>判断两个数据是否相等</h3>
     * <p style="color:yellow">若输入的值为null或非数字类型直接返回为false</p>
     *
     * @param originalValue 原始值，如果原始值为null直接返回为 false
     * @param value         被比较值
     * @return 如果两个值相等返回为true, 否则为false
     */
    public static boolean equals(Number originalValue, Number value) {
        if (null == originalValue || null == value) {
            return false;
        }

        return BigDecimal.valueOf(originalValue.doubleValue()).compareTo(BigDecimal.valueOf(value.doubleValue())) == 0;
    }


    /**
     * 判断数据里是否有等于目标数据的数字
     *
     * @param originalValue 目标数据，如果为null直接返回为false
     * @param values        待比较的数据，如果为null直接返回为false
     * @return 待比较的数据里包含了目标数据就返回为true，否则为false
     */
    public static boolean contains(Number originalValue, Number... values) {
        if (null == originalValue) {
            return false;
        }
        if (null == values) {
            return false;
        }
        for (Number value : values) {
            if (null == value) {
                continue;
            }
            if (equals(originalValue, value)) {
                return true;
            }
        }
        return false;
    }


    /**
     * <p>
     * 将字符串形式的数字转成Boolean值
     * </p>
     * <p>
     * 1 如果数字为null，返回为false
     * </p>
     * <p>
     * 2 数字小于或等于0返回为false
     * </p>
     * <p>
     * 3 数字大于0返回为true
     *
     * @param value 需要转换的数据
     * @return boolean值
     */
    public static Boolean bool(Object value) {
        if (null == value) {
            return null;
        }
        if (StringUtils.equalsIgnoreCase("true", value.toString())) {
            return true;
        }
        if (StringUtils.equalsIgnoreCase("false", value.toString())) {
            return false;
        }
        BigDecimal decimal = parse(value);
        if (null == decimal) {
            return null;
        }
        return gtZero(decimal);
    }


    /**
     * 将boolean值转换成数字
     *
     * @param value boolean值
     * @return value为true时返回1，否则为0
     */
    public static int bool2Int(boolean value) {
        return value ? 1 : 0;
    }

    /**
     * <p>
     * 数据自动补0
     * </p>
     * 对于null值的装箱后的数据，自动设置为false
     *
     * @param value 需要转换的数据
     * @return 数据自动补0, 对于null值的装箱后的数据，自动设置为false
     */
    public static Boolean get(Boolean value) {
        return null == value ? false : value;
    }

    /**
     * <p>
     * 数据自动补0
     * </p>
     * 对于null值的装箱后的数据，自动设置为0
     *
     * @param value 需要转换的数据
     * @return 对于null值的装箱后的数据，自动设置为0
     */
    public static Number get(Number value) {
        return get(value, 0);
    }

    /**
     * 判断数据是否为null，如果为null则返回默认值，否则返回为输入值
     *
     * @param value        输入值
     * @param defaultValue 默认值
     * @return 如果为null则返回默认值，否则返回为输入值
     */
    public static Number get(Number value, Number defaultValue) {
        return null == value ? defaultValue : value;
    }


    /**
     * 将字符串转为Double
     *
     * @param str 需要解析的字符串
     * @return 解析成功则返回Double的数据，否则为null
     */
    public static Double parseDouble(String str) {

        try {
            if (StringUtils.isNotBlank(str)) {
                return parse(str).doubleValue();
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 将字符串转为Double
     *
     * @param str          需要解析的字符串
     * @param defaultValue 解析失败时的默认值
     * @return 解析成功则返回Double的数据，否则为defaultValue
     */
    public static Double parseDouble(String str, double defaultValue) {
        Double value = parseDouble(str);
        return null == value ? defaultValue : value;
    }

    /**
     * 将字符串转为Float
     *
     * @param str 需要解析的字符串
     * @return 解析成功则返回Float的数据，否则为null
     */
    public static Float parseFloat(String str) {

        try {
            if (StringUtils.isNotBlank(str)) {
                return parse(str).floatValue();
            }
        } catch (Exception e) {
        }
        return null;
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

        try {
            if (StringUtils.isNotBlank(str)) {
                return parse(str).intValue();
            }
        } catch (Exception e) {
        }
        return null;
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

        try {
            if (StringUtils.isNotBlank(str)) {
                return parse(str).longValue();
            }
        } catch (Exception e) {
        }
        return null;
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
    public static BigDecimal parse(Object val) {
        return parse(val, null);
    }

    /**
     * 将输入值转换为 BigDecimal
     *
     * @param val        输入值
     * @param defaultVal 默认值
     * @return 转换后的 BigDecimal ，若转换失败则返回为 defaultVal
     */
    public static BigDecimal parse(Object val, BigDecimal defaultVal) {
        if (null == val) {
            return defaultVal;
        }
        try {
            return new BigDecimal(val.toString().trim());
        } catch (Throwable e) {
            log.debug("将数据【{}】转换为数值时出现问题 {}", val, e);
            return defaultVal;
        }
    }
}
