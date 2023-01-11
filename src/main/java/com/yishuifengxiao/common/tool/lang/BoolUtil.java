package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.entity.BoolStat;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * <p>布尔工具</p>
 * <p>主要功能如下</p>
 * <ul>
 * <li>判断字符串是否为‘true’或‘false’对应的文本以及转换后的布尔值</li>
 * <li>将布尔值转换为数字</li>
 *
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class BoolUtil {
    /**
     * true值对应的文本
     */
    private static final String TRUE_TEXT = "true";
    /**
     * false值对应的文本
     */
    private static final String FALSE_TEXT = "false";


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
    public static BoolStat bool(Object value) {
        if (null == value) {
            return BoolStat.Null;
        }
        if (StringUtils.equalsIgnoreCase(TRUE_TEXT, value.toString())) {
            return BoolStat.True;
        }
        if (StringUtils.equalsIgnoreCase(FALSE_TEXT, value.toString())) {
            return BoolStat.False;
        }
        BigDecimal decimal = NumberUtil.parse(value);
        if (null == decimal) {
            return BoolStat.Null;
        }
        return NumberUtil.gtZero(decimal) ? BoolStat.True : BoolStat.False;
    }

    /**
     * <p>是否为true值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据为字符串true(不区分大小写)返回为true, 否则返回false
     */
    public static boolean isTrue(String text) {
        return StringUtils.equalsIgnoreCase(TRUE_TEXT, text);
    }

    /**
     * <p>是否不为true值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据不为字符串true(不区分大小写)返回为true, 否则返回false
     */
    public static boolean isNotTrue(String text) {
        return !isTrue(text);
    }

    /**
     * <p>是否为false值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据为字符串false(不区分大小写)返回为true, 否则返回false
     */
    public static boolean isFalse(String text) {
        return StringUtils.equalsIgnoreCase(FALSE_TEXT, text);
    }

    /**
     * <p>是否不为false值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据不为字符串false(不区分大小写)返回为true, 否则返回false
     */
    public static boolean isNotFalse(String text) {
        return !isFalse(text);
    }


    /**
     * <p>是否为true值</p>
     *
     * @param stat 布尔对象值
     * @return 若传入的是布尔对象值对应的true则返回为true, 否则为false
     */
    public static boolean isTrue(BoolStat stat) {
        return null != stat && BoolStat.True == stat;
    }

    /**
     * <p>是否不为true值</p>
     *
     * @param stat 布尔对象值
     * @return 若传入的不是布尔对象值对应的true则返回为true, 否则为false
     */
    public static boolean isNotTrue(BoolStat stat) {
        return !isTrue(stat);
    }

    /**
     * <p>是否为false值</p>
     *
     * @param stat 布尔对象值
     * @return 若传入的是布尔对象值对应的false则返回为true, 否则为false
     */
    public static boolean isFalse(BoolStat stat) {
        return null != stat && BoolStat.False == stat;
    }

    /**
     * <p>是否为false值</p>
     *
     * @param stat 布尔对象值
     * @return 若传入的不是布尔对象值对应的false则返回为true, 否则为false
     */
    public static boolean isNotFalse(BoolStat stat) {
        return !isFalse(stat);
    }

    /**
     * <p>将数字转换为布尔值</p>
     * <p>转换规则如下：</p>
     * <ul>
     *     <li>数据大于0时返回为true</li>
     *     <li>数据小于获等于0时返回为true</li>
     *     <li>其他值时返回为null</li>
     * </ul>
     *
     * @param number 待转换的数字
     * @return 转换后的布尔值
     */
    public static BoolStat bool(Number number) {
        if (null == number) {
            return BoolStat.Null;
        }
        return CompareUtil.gt(number, 0) ? BoolStat.True : BoolStat.False;
    }

    /**
     * 将boolean值转换成数字
     *
     * @param value boolean值
     * @return value为true时返回1，否则为0
     */
    public static int bool2Int(Boolean value) {
        return BooleanUtils.isTrue(value) ? 1 : 0;
    }


    /**
     * 将boolean值转换成数字
     *
     * @param bool     待判断的boolean值
     * @param trueVal  boolean值为true时返回的值
     * @param falseVal boolean值不为true时返回的值
     * @return 转换后的值
     */
    public static Number bool2Int(Boolean bool, Number trueVal, Number falseVal) {
        return BooleanUtils.isTrue(bool) ? trueVal : falseVal;
    }

    /**
     * 将boolean值转换成数字
     *
     * @param bool     待判断的boolean值
     * @param trueVal  boolean值为true时返回的值
     * @param falseVal boolean值不为true时返回的值
     * @return 转换后的值
     */
    public static Number bool2Int(BoolStat bool, Number trueVal, Number falseVal) {
        return BoolStat.True == bool ? trueVal : falseVal;
    }
}
