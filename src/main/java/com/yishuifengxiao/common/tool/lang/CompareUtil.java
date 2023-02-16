package com.yishuifengxiao.common.tool.lang;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>
 * 比较工具工具类
 * </p>
 * <p>用于比较两个数值的大小</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CompareUtil {

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
        if (null == originalValue) {
            return false;
        }
        if (null == values) {
            return false;
        }
        return Arrays.stream(values).filter(Objects::nonNull).anyMatch(v -> equals(originalValue, v));
    }

}
