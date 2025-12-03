package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * 自定义枚举类接口
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
public interface RootEnum<T> extends Serializable {

    /**
     * 枚举值的编码
     *
     * @return 枚举值的编码
     */
    default T code() {
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


    static <T, E extends RootEnum<T>> Optional<E> of(Class<E> clazz, T code) {
        if (null == clazz || null == code || !clazz.isEnum()) {
            return Optional.empty();
        }
        try {
            for (E item : clazz.getEnumConstants()) {
                if (item.equalCode(code)) {
                    return Optional.of(item);
                }
            }
        } catch (Exception e) {
        }

        return Optional.empty();
    }

}