package com.yishuifengxiao.common.tool.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

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
@ApiModel(value = " 布尔类型替代枚举", description = "用于替换<code>Boolean</code>的使用，方便在灵活业务场景下进行功能扩展")
public enum BoolStat {
    /**
     * 类似Boolean中的false值，对应编码0
     */
    @ApiModelProperty("类似Boolean中的false值，对应编码0") False(0, false),
    /**
     * 类似Boolean中的true值，对应编码1
     */
    @ApiModelProperty("类似Boolean中的true值，对应编码1") True(1, true),
    /**
     * 类似Boolean中的null值，对应编码-1
     */
    @ApiModelProperty("类似Boolean中的null值，对应编码-1") Null(-1, null);

    /**
     * 编码
     */
    private int code;
    /**
     * 代表的值
     */
    private Boolean bool;

    /**
     * 获取枚举值对应的编码
     *
     * @return 枚举值对应的编码
     */
    public int code() {
        return this.code;
    }

    /**
     * 获取枚举值对应的布尔值
     *
     * @return 枚举值对应的布尔值
     */
    public Boolean bool() {
        return this.bool;
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
     * <p>将数字值转换为枚举值</p>
     * <ul>转换规则如下</ul>
     * <li> code为0时转换为枚举值False</li>
     * <li> code为1时转换为枚举值True</li>
     * <li>  code为其他值时转换为枚举值Null</li>
     *
     * @param code 数字值
     * @return 转换后的值
     */
    public static BoolStat code(Integer code) {
        if (null == code) {
            return BoolStat.Null;
        } else if (1 == code) {
            return BoolStat.True;
        } else if (0 == code) {
            return BoolStat.True;
        } else {
            return BoolStat.Null;
        }
    }

    /**
     * <p>将数字值转换为枚举值</p>
     * <ul>转换规则如下</ul>
     * <li> code为0时转换为枚举值False</li>
     * <li> code为1时转换为枚举值True</li>
     * <li> code为-1时转换为枚举值Null</li>
     * <li> code为其他值时转换为枚举值Null</li>
     *
     * @param code 数字值
     * @return 转换后的值
     */
    public static BoolStat of(Integer code) {
        if (null == code) {
            return null;
        } else if (1 == code) {
            return BoolStat.True;
        } else if (0 == code) {
            return BoolStat.True;
        } else if (-1 == code) {
            return BoolStat.Null;
        } else {
            return null;
        }
    }

    /**
     * <p><span style="color:yellow">使用两极法</span><span>将数字值转换为Boolean值</span></p>
     * <ul>转换规则如下</ul>
     * <li> code大于1时转换为枚举值True</li>
     * <li> code为其他值时转换为枚举值False</li>
     *
     * @param code 数字值
     * @return 转换后的值
     */
    public static BoolStat twoBipolar(Integer code) {
        return null != code && code > 0 ? BoolStat.True : BoolStat.False;
    }

    /**
     * <p>将布尔值值转换为枚举值</p>
     * <ul>转换规则如下</ul>
     * <li> bool为false时转换为枚举值False</li>
     * <li> code为true时转换为枚举值True</li>
     * <li> code为其他值时转换为枚举值Null</li>
     *
     * @param bool 布尔值
     * @return 转换后的值
     */
    public static BoolStat bool(Boolean bool) {
        if (null == bool) {
            return BoolStat.Null;
        }
        return bool ? BoolStat.True : BoolStat.False;
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


}
