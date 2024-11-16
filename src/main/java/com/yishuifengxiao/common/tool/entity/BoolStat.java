package com.yishuifengxiao.common.tool.entity;

import com.yishuifengxiao.common.tool.lang.BoolUtil;
import com.yishuifengxiao.common.tool.lang.CompareUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Arrays;

/**
 * <p>布尔类型替代枚举</p>
 * <p>用于替换<code>Boolean</code>的使用，方便在灵活业务场景下进行功能扩展</p>
 * <p>在简明意义上的对应关系如下</p>
 * <ul>
 *     <li>数字1  ======= 布尔true </li>
 *     <li>数字0  ======= 布尔false </li>
 *     <li>数字-1 ======= 布尔null </li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Schema(name = " 布尔类型替代枚举", description = "用于替换<code>Boolean</code>的使用，方便在灵活业务场景下进行功能扩展")
public enum BoolStat implements RootEnum {


    /**
     * 类似Boolean中的false值，对应编码0
     */
    @Schema(name = "类似Boolean中的false值，对应编码0") False(0, false),

    /**
     * 类似Boolean中的true值，对应编码1
     */
    @Schema(name = "类似Boolean中的true值，对应编码1") True(1, true),

    /**
     * 类似Boolean中的null值，对应编码-1
     */
    @Schema(name = "类似Boolean中的null值，对应编码-1") Null(-1, null);

    /**
     * 编码
     */
    private Integer code;
    /**
     * 代表的值
     */
    private Boolean bool;

    /**
     * 获取枚举值对应的编码
     *
     * @return 枚举值对应的编码
     */
    public Integer code() {
        return this.code;
    }

    /**
     * 获取枚举值的描述
     *
     * @return 枚举值的描述
     */
    @Override
    public String description() {
        return null;
    }


    /**
     * 枚举状态值是否对应的布尔值true
     *
     * @return 若枚举状态值对应的布尔值true则返回为true, 否则为false
     */
    public boolean isTrue() {
        return BooleanUtils.isTrue(this.bool);
    }

    /**
     * 枚举状态值是否不对应的布尔值true
     *
     * @return 若枚举状态值不对应的布尔值true则返回为true, 否则为false
     */
    public boolean isNotTrue() {
        return BooleanUtils.isNotTrue(this.bool);
    }

    /**
     * 枚举状态值是否对应的布尔值false
     *
     * @return 若枚举状态值对应的布尔值false则返回为true, 否则为false
     */
    public boolean isFalse() {
        return BooleanUtils.isFalse(this.bool);
    }

    /**
     * 枚举状态值是否不对应的布尔值false
     *
     * @return 若枚举状态值不对应的布尔值false则返回为true, 否则为false
     */
    public boolean isNotFalse() {
        return BooleanUtils.isNotFalse(this.bool);
    }


    /**
     * 构造函数
     *
     * @param code 编码
     * @param bool 对应的布尔值
     */
    BoolStat(int code, Boolean bool) {
        this.code = code;
        this.bool = bool;
    }


    /**
     * 是否为true值对应的编码
     *
     * @param code 编码
     * @return 若为true值对应的编码则返回为true, 否则为false
     */
    public static boolean isTrue(Integer code) {
        return null != code && code == 1;
    }

    /**
     * 是否为false值对应的编码
     *
     * @param code 编码
     * @return 若为false值对应的编码则返回为true, 否则为false
     */
    public static boolean isFalse(Integer code) {
        return null != code && code == 0;
    }


    /**
     * 是否为布尔值选项(是否为0或1)
     *
     * @param code 待判断的值
     * @return true表示传入的值是0或1
     */
    public static boolean isBoolVal(Integer code) {
        if (null == code) {
            return false;
        }
        return code == 0 || code == 1;
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
     *     <li>数据大于0时返回为True</li>
     *     <li>数据小于或等于0时返回为False</li>
     *     <li>其他值时返回为null</li>
     * </ul>
     *
     * @param number 待转换的数字
     * @return 转换后的布尔值
     */
    public static BoolStat of(Number number) {
        if (null == number) {
            return BoolStat.Null;
        }
        return CompareUtil.gt(number, 0) ? BoolStat.True : BoolStat.False;
    }

    /**
     * <p>将布尔值值转换为枚举值</p>
     * <p>转换规则如下
     * </p>
     * <ul>
     * <li> bool为false时转换为枚举值False</li>
     * <li> bool为true时转换为枚举值True</li>
     * <li> bool为其他值时转换为枚举值Null</li>
     * </ul>
     *
     * @param bool 布尔值
     * @return 转换后的值
     */
    public static BoolStat of(Boolean bool) {
        if (null == bool) {
            return BoolStat.Null;
        }
        return bool ? BoolStat.True : BoolStat.False;
    }


    /**
     * <p>将数字转换为布尔值</p>
     * <p>转换规则如下：</p>
     * <ul>
     *     <li>数据大于0时返回为True</li>
     *     <li>其他值时返回为False</li>
     * </ul>
     *
     * @param number 待转换的数字
     * @return 转换后的布尔值
     */
    public static BoolStat twoState(Number number) {
        if (null == number || CompareUtil.lte(number, 0)) {
            return BoolStat.False;
        }
        return BoolStat.True;
    }

    /**
     * <p>将数字转换为布尔值</p>
     * <p>转换规则如下：</p>
     * <ul>
     *     <li>数据大于0时返回为True</li>
     *     <li>数据等于0时返回为False</li>
     *     <li>其他值时返回为False</li>
     * </ul>
     *
     * @param number 待转换的数字
     * @return 转换后的布尔值
     */
    public static BoolStat tristate(Number number) {
        if (null == number) {
            return BoolStat.Null;
        }
        if (CompareUtil.gt(number, 0)) {
            return BoolStat.True;
        } else if (CompareUtil.lt(number, 0)) {
            return BoolStat.Null;
        } else {
            return BoolStat.False;
        }

    }


    /**
     * <p>将数据转换为布尔类型替代枚举</p>
     * <p>转换规则如下</p>
     * <ul>
     *     <li>当内容为文本 true(忽略大小写和空格)时返回为枚举值True</li>
     *     <li>当内容为文本 false(忽略大小写和空格)时返回为枚举值False</li>
     *     <li>文本可以转换为数字，且数字大于0时返回为枚举值True</li>
     *     <li>文本可以转换为数字，且数字小于0或等于时返回为枚举值False</li>
     *     <li>其他情况下返回为枚举值NUll</li>
     * </ul>
     *
     * @param value 待判断的内容
     * @return 转换后的值
     */
    public static BoolStat parse(Object value) {
        if (null == value) {
            return BoolStat.Null;
        }
        Boolean val = BoolUtil.parse(value);
        if (null == val) {
            return BoolStat.Null;
        }
        return val ? BoolStat.True : BoolStat.False;
    }

    /**
     * 将状态值转换成数字
     *
     * @param bool     待判断的状态值
     * @param trueVal  状态值为true时返回的值
     * @param falseVal 状态值不为true时返回的值
     * @return 转换后的值
     */
    public static Number bool2Int(BoolStat bool, Number trueVal, Number falseVal) {
        return isTrue(bool) ? trueVal : falseVal;
    }

    /**
     * <p>判断文本是否是符合要求的boolean值替换表达值</p>
     * <p>即判断文本内容是否为"0", "1", "true", "false" 或者null其中的一种 </p>
     *
     * @param val 待判断的文本
     * @return 符合要求为true，否则为false
     */
    public static boolean isLegalVal(Object val) {
        if (null == val) {
            return true;
        }
        return Arrays.asList("0", "1", "true", "false").stream().anyMatch(v -> v.equalsIgnoreCase(val.toString().trim()));
    }
}
