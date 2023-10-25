package com.yishuifengxiao.common.tool.lang;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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
        Number decimal = parse(str);
        return null == decimal ? null : decimal.doubleValue();
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
        Number decimal = parse(str);
        return null == decimal ? null : decimal.floatValue();
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
        Number decimal = parse(str);
        return null == decimal ? null : decimal.intValue();
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
        Number decimal = parse(str);
        return null == decimal ? null : decimal.longValue();
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
    public static Number parse(Object val) {
        try {
            if (null == val || StringUtils.isBlank(val.toString())) {
                return null;
            }
            Number number = NumberUtils.createNumber(val.toString().replaceAll(",", "").trim());
            return number;
        } catch (Throwable e) {
            log.debug("将数据【{}】转换为数值时出现问题 {}", val, e);
        }
        return null;
    }

    /**
     * <p>
     * 将输入值转换为 BigDecimal
     * </p>
     * <p>
     * <strong>线程安全</strong>
     * </p>
     *
     * @param val        输入值
     * @param defaultVal 默认值
     * @return 转换后的 BigDecimal ，若转换失败则返回为 defaultVal
     */
    public static Number parse(Object val, BigDecimal defaultVal) {
        if (null == val) {
            return defaultVal;
        }
        Number decimal = parse(val);
        return null == decimal ? defaultVal : decimal;
    }
}
