package com.yishuifengxiao.common.tool.lang;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 比较工具工具类
 * </p>
 * 用于比较一个给定的值是否在一个指定的范围内，其主要作用如下：
 * <ol>
 * <li>判断给定的数字是否在指定的数字范围之内</li>
 * <li>判断给定的时间是否在指定的时间范围内</li>
 * </ol>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class CompareUtil {


    /**
     * <p>
     * 判断给定的值是否在指定的数据范围的区间内
     * </p>
     *
     * <p>
     * 注意：包含边界
     * </p>
     * 例如 compareValue=2，startValue=2 ，endValue=8时的返回值为true
     *
     * @param compareValue 给定的值，如果给定的值为null，则直接返回为false
     * @param startValue   比较范围的开始值(包含该值)，如果给定的值为null，则默认替换为0
     * @param endValue     比较范围的结束值(包含该值)，如果给定的值为null，则默认替换为0
     * @return 给定的值是否在指定的数据范围的区间内, 如果在则返回为true, 否则为false
     */
    public static boolean isBetween(Number compareValue, Number startValue, Number endValue) {
        if (null == compareValue) {
            return false;
        }
        if (null == startValue) {
            startValue = 0;
        }
        if (null == endValue) {
            endValue = 0;
        }
        return lte(compareValue, startValue) && lte(compareValue, endValue);
    }

    /**
     * <p>
     * 判断给定的值是否不在指定的数据范围的区间内
     * </p>
     *
     * <p>
     * 注意：包含边界
     * </p>
     * <p>
     * 例如 compareValue=2，startValue=2 ，endValue=8时的返回值为false
     * </p>
     * <p>
     * 例如 compareValue=1，startValue=2 ，endValue=8时的返回值为true
     * </p>
     *
     * @param compareValue 给定的值，如果给定的值为null，则直接返回为false
     * @param startValue   比较范围的开始值(包含该值)，如果给定的值为null，则默认替换为0
     * @param endValue     比较范围的结束值(包含该值)，如果给定的值为null，则默认替换为0
     * @return 给定的值是否不在指定的数据范围的区间内, 如果在则返回为true, 否则为false
     */
    public static boolean isNotBetween(Number compareValue, Number startValue, Number endValue) {
        if (null == compareValue) {
            return false;
        }
        return !isBetween(compareValue, startValue, endValue);
    }


    /**
     * <p>
     * 判断给定的值是否在指定的数据范围的区间内
     * </p>
     *
     * <p>
     * 注意：
     * </p>
     * <p>
     * 1. 不包含边界
     * </p>
     * <p>
     * 2. 任意一个值为null则返回值为false
     * </p>
     *
     * <p>
     * 例如 compareValue=2020-12-12 12:12:12，startValue=2020-12-12 12:12:12
     * ，endValue=2021-12-12 12:12:12时的返回值为true
     * </p>
     * <p>
     * 例如 compareValue=2020-12-11 12:12:12，startValue=2020-12-12 12:12:12
     * ，endValue=2021-12-12 12:12:12时的返回值为false
     * </p>
     *
     * @param compareValue 给定的值
     * @param startValue   比较范围的开始值(包含该值)
     * @param endValue     比较范围的结束值(包含该值)
     * @return 给定的值是否在指定的数据范围的区间内, 如果在则返回为true, 否则为false
     */
    public static boolean isBetween(Date compareValue, Date startValue, Date endValue) {
        if (null == compareValue || null == startValue || null == endValue) {
            return false;
        }

        return compareValue.getTime() >= startValue.getTime() && endValue.getTime() >= compareValue.getTime();
    }

    /**
     * <p>
     * 判断给定的值是否不在指定的数据范围的区间内
     * </p>
     *
     * <p>
     * 1. 包含边界
     * </p>
     * 2. 任意一个值为null则返回值为true
     *
     * @param compareValue 给定的值
     * @param startValue   比较范围的开始值
     * @param endValue     比较范围的结束值
     * @return 给定的值是否不在指定的数据范围的区间内, 如果在则返回为true, 否则为false
     */
    public static boolean isNotBetween(Date compareValue, Date startValue, Date endValue) {
        return !isBetween(compareValue, startValue, endValue);
    }

    /**
     * <p>
     * 判断给定的值是否在指定的数据范围的区间内
     * </p>
     *
     * <p>
     * 注意：
     * </p>
     * <p>
     * 1. 包含边界
     * </p>
     * 2. 任意一个值为null则返回值为false
     *
     * <p>
     * 例如 compareValue=2020-12-12 12:12:12，startValue=2020-12-12 12:12:12
     * ，endValue=2021-12-12 12:12:12时的返回值为true
     * </p>
     * <p>
     * 例如 compareValue=2020-12-11 12:12:12，startValue=2020-12-12 12:12:12
     * ，endValue=2021-12-12 12:12:12时的返回值为false
     * </p>
     *
     * @param compareValue 给定的值
     * @param startValue   比较范围的开始值
     * @param endValue     比较范围的结束值
     * @return 给定的值是否在指定的数据范围的区间内, 如果在则返回为true, 否则为false
     */
    public static boolean isBetween(LocalDateTime compareValue, LocalDateTime startValue, LocalDateTime endValue) {
        if (null == compareValue || null == startValue || null == endValue) {
            return false;
        }
        return (!compareValue.isBefore(startValue)) && (!compareValue.isAfter(endValue));
    }

    /**
     * <p>
     * 判断给定的值是否不在指定的数据范围的区间内
     * </p>
     *
     * <p>
     * 1. 包含边界
     * </p>
     * 2. 任意一个值为null则返回值为false
     *
     * @param compareValue 给定的值
     * @param startValue   比较范围的开始值
     * @param endValue     比较范围的结束值
     * @return 给定的值是否不在指定的数据范围的区间内, 如果在则返回为true, 否则为false
     */
    public static boolean isNotBetween(LocalDateTime compareValue, LocalDateTime startValue, LocalDateTime endValue) {
        if (null == compareValue || null == startValue || null == endValue) {
            return false;
        }
        return !isBetween(compareValue, startValue, endValue);
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
     * <p>判断两个数据是否相等</p>
     * <p style="color:yellow">
     * 若输入的值为null或非数字类型直接返回为false
     * </p>
     *
     * @param originalValue 原始值，如果原始值为null直接返回为 false
     * @param value         被比较值
     * @return 如果两个值相等返回为true, 否则为false
     */
    public static boolean equals(Number originalValue, Number value) {
        if (null == originalValue || null == value) {
            return false;
        }

        return new BigDecimal(originalValue.toString()).compareTo(new BigDecimal(value.toString())) == 0;
    }

    /**
     * 第一个数是否大于第二个数
     *
     * @param v1 第一个数，若为null直接返回为false
     * @param v2 第二个数，若为null直接返回为true
     * @return 第一个数大于第二个数返回为true, 否则为false
     */
    public static boolean gt(BigDecimal v1, BigDecimal v2) {
        if (null == v1) {
            return false;
        }
        if (null == v2) {
            return true;
        }
        return v1.compareTo(v2) > 0;
    }

    /**
     * 第一个数是否大于或等于第二个数
     *
     * @param v1 第一个数，若为null直接返回为false
     * @param v2 第二个数，若为null直接返回为true
     * @return 第一个数大于或等于第二个数返回为true, 否则为false
     */
    public static boolean gte(BigDecimal v1, BigDecimal v2) {
        if (null == v1) {
            return false;
        }
        if (null == v2) {
            return true;
        }
        return v1.compareTo(v2) >= 0;
    }

    /**
     * 第一个数是否小于第二个数
     *
     * @param v1 第一个数，若为null直接返回为true
     * @param v2 第二个数，若为null直接返回为false
     * @return 第一个数小于第二个数返回为true, 否则为false
     */
    public static boolean lt(BigDecimal v1, BigDecimal v2) {
        if (null == v1) {
            return true;
        }
        if (null == v2) {
            return false;
        }
        return v1.compareTo(v2) < 0;
    }

    /**
     * 第一个数是否小于或等于第二个数
     *
     * @param v1 第一个数，若为null直接返回为true
     * @param v2 第二个数，若为null直接返回为false
     * @return 第一个数小于或等于第二个数返回为true, 否则为false
     */
    public static boolean lte(BigDecimal v1, BigDecimal v2) {
        if (null == v1) {
            return true;
        }
        if (null == v2) {
            return false;
        }
        return v1.compareTo(v2) <= 0;
    }

    /**
     * <p>判断两个数据是否相等</p>
     * <p style="color:yellow">
     * 若输入的值为null或非数字类型直接返回为false
     * </p>
     *
     * @param originalValue 原始值，如果原始值为null直接返回为 false
     * @param value         被比较值
     * @return 如果两个值相等返回为true, 否则为false
     */
    public static boolean equals(BigDecimal originalValue, BigDecimal value) {
        if (null == originalValue || null == value) {
            return false;
        }

        return originalValue.compareTo(value) == 0;
    }


    /**
     * <p>判断输入值是否大于或等于0</p>
     * <p style="color:yellow">
     * 若输入的值为null或非数字类型直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值大于或等于0返回为true, 否则为false
     */
    public static boolean gteZero(Number value) {
        if (null == value) {
            return false;
        }
        return new BigDecimal(value.toString()).compareTo(NumberUtil.ZERO) >= 0;
    }

    /**
     * <p>判断输入值是否大于0</p>
     * <p style="color:yellow">
     * 若输入的值为null或小于0直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值大于0返回为true, 否则为false
     */
    public static boolean gtZero(Number value) {
        if (null == value) {
            return false;
        }
        return new BigDecimal(value.toString()).compareTo(NumberUtil.ZERO) > 0;
    }

    /**
     * <p>判断输入值是否小于或等于0</p>
     * <p style="color:yellow">
     * 若输入的值为null或非数字类型直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值小于或等于0返回为true, 否则为false
     */
    public static boolean lteZero(Number value) {
        if (null == value) {
            return false;
        }
        return new BigDecimal(value.toString()).compareTo(NumberUtil.ZERO) <= 0;
    }

    /**
     * <p>判断输入值是否小于0</p>
     * <p style="color:yellow">
     * 若输入的值为null或非数字类型直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值小于0返回为true, 否则为false
     */
    public static boolean ltZero(Number value) {
        if (null == value) {
            return false;
        }
        return new BigDecimal(value.toString()).compareTo(NumberUtil.ZERO) < 0;
    }

    /**
     * <p>判断输入值是否等于0</p>
     * <p style="color:yellow">
     * 若输入的值为null或非数字类型直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为0直接返回true
     * @return 输入值等于0返回为true, 否则为false
     */
    public static boolean eqZero(Number value) {
        if (null == value) {
            return false;
        }
        return new BigDecimal(value.toString()).compareTo(NumberUtil.ZERO) == 0;
    }

    /**
     * <p>判断输入值是否大于或等于0</p>
     * <p style="color:yellow">
     * 若输入的值为null直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值大于或等于0返回为true, 否则为false
     */
    public static boolean gteZero(BigDecimal value) {
        if (null == value) {
            return false;
        }
        return value.compareTo(NumberUtil.ZERO) >= 0;
    }

    /**
     * <p>判断输入值是否大于0</p>
     * <p style="color:yellow">
     * 若输入的值为null直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值大于0返回为true, 否则为false
     */
    public static boolean gtZero(BigDecimal value) {
        if (null == value) {
            return false;
        }
        return value.compareTo(NumberUtil.ZERO) > 0;
    }

    /**
     * <p>判断输入值是否小于或等于0</p>
     * <p style="color:yellow">
     * 若输入的值为null直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值小于或等于0返回为true, 否则为false
     */
    public static boolean lteZero(BigDecimal value) {
        if (null == value) {
            return false;
        }
        return value.compareTo(NumberUtil.ZERO) <= 0;
    }

    /**
     * <p>判断输入值是否小于0</p>
     * <p style="color:yellow">
     * 若输入的值为null直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为null直接返回false
     * @return 输入值小于0返回为true, 否则为false
     */
    public static boolean ltZero(BigDecimal value) {
        if (null == value) {
            return false;
        }
        return new BigDecimal(value.toString()).compareTo(NumberUtil.ZERO) < 0;
    }

    /**
     * <p>判断输入值是否等于0</p>
     * <p style="color:yellow">
     * 若输入的值为null直接返回为false
     * </p>
     *
     * @param value 需要判断的输入值，若该值为0直接返回true
     * @return 输入值等于0返回为true, 否则为false
     */
    public static boolean eqZero(BigDecimal value) {
        if (null == value) {
            return false;
        }
        return value.compareTo(NumberUtil.ZERO) == 0;
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

}
