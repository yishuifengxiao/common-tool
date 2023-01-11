package com.yishuifengxiao.common.tool.entity;


import io.swagger.annotations.ApiModel;

/**
 * <p>布尔类型替代枚举</p>
 * <p>用于替换<code>Boolean</code>的使用，方便在灵活业务场景下进行功能扩展，该枚举类为三态状态，即未初始化、关闭、开启三中状态</p>
 * <p>在简明意义上的对应关系如下</p>
 * <ul>
 *     <li>数字1  ======= 开启 </li>
 *     <li>数字0  ======= 关闭 </li>
 *     <li>数字-1 ======= 未初始化 </li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@ApiModel(value = " 布尔类型三态类型替代枚举", description = "用于替换<code>Boolean</code>的使用，方便在灵活业务场景下进行功能扩展，该枚举类为三态状态，即未初始化、关闭、开启三中状态")
public enum Switch {
    /**
     * 未初始化,对应编码值-1
     */
    INIT(-1, "未初始化"),
    /**
     * 关闭,对应编码值0
     */
    CLOSE(0, "关闭"),
    /**
     * 开启,对应编码值1
     */
    ACTIVE(1, "开启");

    /**
     * 编码
     */
    private int code;

    /**
     * 名称
     */
    private String name;

    /**
     * 构造函数
     *
     * @param code 编码
     * @param name 名称
     */
    Switch(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 获取状态对应的编码值
     *
     * @return 状态对应的编码值
     */
    public int code() {
        return this.code;
    }

    /**
     * <p>将编码值转换为开关枚举值</p>
     * <p>转换关系如下</p>
     * <ul>
     *     <li>code为null ====》 INIT ;即未初始化</li>
     *     <li>code 大于0 ====》 ACTIVE ;即开启</li>
     *     <li>code 等于0 ====》 CLOSE ;即关闭</li>
     *     <li>code 小于0 ====》 INIT ;即未初始化</li>
     * </ul>
     *
     * @param code 编码值
     * @return 转换后的枚举值
     */
    public static Switch of(Integer code) {
        if (null == code) {
            return Switch.INIT;
        }
        if (code > 0) {
            return Switch.ACTIVE;
        } else if (code == 0) {
            return Switch.CLOSE;
        } else {
            return Switch.INIT;
        }
    }

    /**
     * 是否为激活状态
     *
     * @param val 待判定枚举值
     * @return 待判定枚举值为激活状态则返回为true, 否则为false
     */
    public static boolean isActive(Switch val) {
        return null != val && val == Switch.ACTIVE;
    }

    /**
     * 是否为关闭状态
     *
     * @param val 待判定枚举值
     * @return 待判定枚举值为关闭状态则返回为true, 否则为false
     */
    public static boolean isClose(Switch val) {
        return null != val && val == Switch.CLOSE;
    }

    /**
     * <p>是否为初始化状态</p>
     * <p>若待判定枚举值为null或初始化状态时，均会返回true</p>
     *
     * @param val 待判定枚举值
     * @return 待判定枚举值为初始化状态则返回为true, 否则为false
     */
    public static boolean isInit(Switch val) {
        return null == val || val == Switch.INIT;
    }

    /**
     * 是否不为激活状态
     *
     * @param val 待判定枚举值
     * @return 待判定枚举值为激活状态则返回为false, 否则为true
     */
    public static boolean isNotActive(Switch val) {
        return !isActive(val);
    }

    /**
     * 是否不为关闭状态
     *
     * @param val 待判定枚举值
     * @return 待判定枚举值为关闭状态则返回为false, 否则为true
     */
    public static boolean isNotClose(Switch val) {
        return !isClose(val);
    }

    /**
     * <p>是否不为初始化状态</p>
     * <p>若待判定枚举值为null或初始化状态时，均会返回false,其他情况返回为true</p>
     *
     * @param val 待判定枚举值
     * @return 待判定枚举值为初始化状态则返回为true, 否则为false
     */
    public static boolean isNotInit(Switch val) {
        return !isInit(val);
    }
}
