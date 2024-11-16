package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 自定义枚举类接口
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface RootEnum extends Serializable {

    /**
     * 枚举值的编码
     *
     * @return 枚举值的编码
     */
    default Object code() {
        return null;
    }


    /**
     * 枚举值的名称
     *
     * @return 枚举值的名称
     */
    default String enumName() {
        return null;
    }


    /**
     * 枚举值的描述
     *
     * @return 枚举值的描述
     */
    default String description() {
        return null;
    }

    /**
     * 判断给定的编码与枚举值的编码是否一致
     *
     * @param code 待判断的给定的编码,若为null则直接返回为false
     * @return 给定的编码与枚举值的编码一致返回为true, 否则为false
     */
    default boolean equalCode(Object code) {
        if (null == code) {
            return false;
        }
        Object val = this.code();
        if (null == val) {
            return false;
        }
        return Objects.equals(val, code);
    }


}
