package com.yishuifengxiao.common.tool.lang;

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
    public static final String TRUE_TEXT = "true";
    /**
     * false值对应的文本
     */
    public static final String FALSE_TEXT = "false";


    /**
     * <p>是否为true值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据为字符串true(不区分大小写且忽略空格)返回为true, 否则返回false
     */
    public static boolean isTrueText(String text) {
        text = null == text ? null : text.trim();
        return StringUtils.equalsIgnoreCase(TRUE_TEXT, text);
    }

    /**
     * <p>是否不为true值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据不为字符串true(不区分大小写且忽略空格)返回为true, 否则返回false
     */
    public static boolean isNotTrueText(String text) {
        return !isTrueText(text);
    }

    /**
     * <p>是否为false值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据为字符串false(不区分大小写且忽略空格)返回为true, 否则返回false
     */
    public static boolean isFalseText(String text) {
        text = null == text ? null : text.trim();
        return StringUtils.equalsIgnoreCase(FALSE_TEXT, text);
    }

    /**
     * <p>是否不为false值对应的文本值</p>
     *
     * @param text 编码
     * @return 若传入的数据不为字符串false(不区分大小写且忽略空格)返回为true, 否则返回false
     */
    public static boolean isNotFalseText(String text) {
        return !isFalseText(text);
    }


    /**
     * 将boolean值转换成数字
     *
     * @param value boolean值
     * @return value为true时返回1，否则为0
     */
    public static int bool2Int(Boolean value) {
        return bool2Int(value, 1, 0).intValue();
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
     * <p>将数据转换为布尔值</p>
     * <p>转换规则如下</p>
     * <ul>
     *     <li>当内容为文本 true(忽略大小写和空格)时返回为true</li>
     *     <li>当内容为文本 false(忽略大小写和空格)时返回为false</li>
     *     <li>文本可以转换为数字，且数字大于0时返回为true</li>
     *     <li>文本可以转换为数字，且数字小于0或等于时返回为false</li>
     *     <li>其他情况下返回为null</li>
     * </ul>
     *
     * @param val 待判断的内容
     * @return 转换后的值
     */
    public static Boolean parse(Object val) {
        if (null == val) {
            return null;
        }
        String text = val.toString().trim();
        if (StringUtils.equalsIgnoreCase(BoolUtil.TRUE_TEXT, text)) {
            return true;
        }
        if (StringUtils.equalsIgnoreCase(BoolUtil.FALSE_TEXT, text)) {
            return false;
        }
        BigDecimal decimal = NumberUtil.parse(text);
        if (null == decimal) {
            return null;
        }
        return NumberUtil.gtZero(decimal) ? true : false;
    }

    /**
     * <p>将数据转换为布尔值</p>
     * <p>转换规则如下</p>
     * <ul>
     *     <li>当内容为文本 true(忽略大小写和空格)时返回为true</li>
     *     <li>文本可以转换为数字，且数字大于0时返回为true</li>
     *     <li>其他情况下返回为false</li>
     * </ul>
     *
     * @param val 待判断的内容
     * @return 转换后的值
     */
    public static boolean twoStateTrue(Object val) {
        return BooleanUtils.isTrue(parse(val));
    }

    /**
     * <p>将数据转换为布尔值</p>
     * <p>转换规则如下</p>
     * <ul>
     *     <li>当内容为文本 false(忽略大小写和空格)时返回为false</li>
     *     <li>文本可以转换为数字，且数字小于0或等于时返回为false</li>
     *     <li>其他情况下返回为false</li>
     * </ul>
     *
     * @param val 待判断的内容
     * @return 转换后的值
     */
    public static boolean twoStateFalse(Object val) {
        return BooleanUtils.isFalse(parse(val));
    }


}
