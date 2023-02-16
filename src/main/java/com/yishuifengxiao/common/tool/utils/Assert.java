package com.yishuifengxiao.common.tool.utils;

import com.yishuifengxiao.common.tool.collections.SizeUtil;
import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.CompareUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 * <p>
 * 断言工具
 * </p>
 * <p>
 * 该工具主要判断给定的数据是否符合给定的条件，若数据不符合给定的条件就抛出自定义受检查的异常
 * </p>
 * <p>
 * 该主要是为了替换到代码里的各种<code>if</code>判断，从而提升代码的优雅性
 * </p>
 *
 * <strong>注意：本工具中抛出的异常全部为运行时异常
 * <code>UncheckedException</code>,注意在使用时带来的处理问题</strong>
 *
 * @author yishui
 * @version 1.0.0
 * @see UncheckedException
 * @since 1.0.0
 */
public final class Assert {

    /**
     * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void gteZero(String msg, Number value) {
        if (!CompareUtil.gteZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void gteZero(String msg, BigDecimal value) {
        if (!CompareUtil.gteZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void gtZero(String msg, Number value) {
        if (!CompareUtil.gtZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void gtZero(String msg, BigDecimal value) {
        if (!CompareUtil.gtZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void lteZero(String msg, Number value) {
        if (!CompareUtil.lteZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void lteZero(String msg, BigDecimal value) {
        if (!CompareUtil.lteZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void ltZero(String msg, Number value) {
        if (!CompareUtil.ltZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
     *
     * @param msg   异常提示信息
     * @param value 需要比较的值
     */
    public static void ltZero(String msg, BigDecimal value) {
        if (!CompareUtil.ltZero(value)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判读所有给定的数据为true,如果有一个数据不为true就抛出异常
     *
     * @param msg    异常提示信息
     * @param values 需要判断的值
     */
    public static void isTrue(String msg, Boolean... values) {
        if (null != values) {
            for (Boolean value : values) {
                if (null == value || !value) {
                    throw new UncheckedException(msg);
                }
            }
        }

    }

    /**
     * 判读所有给定的数据为false,如果有一个数据不为false就抛出异常
     *
     * @param msg    异常提示信息
     * @param values 需要判断的值
     */
    public static void isFalse(String msg, Boolean... values) {
        if (null != values) {
            for (Boolean value : values) {
                if (null != value && value) {
                    throw new UncheckedException(msg);
                }
            }
        }

    }

    /**
     * 判断传入的参数是否为空，若有传入的参数有一个不为空就抛出异常
     *
     * @param msg    提示信息
     * @param values 需要判断的数据
     */
    public static void isBlank(String msg, Object... values) {
        if (null == values) {
            return;
        }
        if (!Arrays.stream(values).allMatch(v -> null == v || StringUtils.isBlank(v.toString()))) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断传入的参数是否不为空，若有传入的参数有一个为空就抛出异常
     *
     * @param msg    提示信息
     * @param values 需要判断的数据
     */
    public static void isNotBlank(String msg, Object... values) {
        if (null == values) {
            return;
        }
        if (Arrays.stream(values).anyMatch(v -> null == v || StringUtils.isBlank(v.toString()))) {
            throw new UncheckedException(msg);
        }
    }


    /**
     * 判断分页对象是否为空，若不为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param page 分页对象
     */
    public static <T> void isEmpty(String msg, Page<T> page) {
        if (SizeUtil.isNotEmpty(page)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断分页对象是否不为空，若为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param page 分页对象
     */
    public static <T> void isNotEmpty(String msg, Page<T> page) {
        if (SizeUtil.isEmpty(page)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断分页对象是否为空，若不为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param page 分页对象
     */
    public static <T> void isEmpty(String msg, org.springframework.data.domain.Page<T> page) {
        if (SizeUtil.isNotEmpty(page)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断分页对象是否不为空，若为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param page 分页对象
     */
    public static <T> void isNotEmpty(String msg, org.springframework.data.domain.Page<T> page) {
        if (SizeUtil.isEmpty(page)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断数组是否为空，若不为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param data 数组数据
     */
    public static <T> void isEmpty(String msg, T[] data) {
        if (SizeUtil.isNotEmpty(data)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断数组是否不为空，若为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param data 数组数据
     */
    public static <T> void isNotEmpty(String msg, T[] data) {
        if (SizeUtil.isEmpty(data)) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断集合是否为空，若不为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param data 待判定的集合
     */
    public synchronized static <T> void isEmpty(String msg, Collection<T> data) {
        if (null != data && data.size() > 0) {
            throw new UncheckedException(msg);
        }
    }


    /**
     * 判断Set是否不为空，若为空则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param data 待判定的集合
     */
    public static <T> void isNotEmpty(String msg, Collection<T> data) {
        if (null == data || data.size() == 0) {
            throw new UncheckedException(msg);
        }
    }


    /**
     * 判断集合里是否只有一个元素，若不是只有一个元素则抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param data 集合
     */
    public static <T> void isOnlyOne(String msg, Collection<T> data) {
        if (null == data || data.size() != 1) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断集合是否不是只有一个元素，若只有一个元素就抛出异常
     *
     * @param <T>  给定的数据的类型
     * @param msg  异常提示信息
     * @param data 集合
     */
    public static <T> void isNotOnlyOne(String msg, Collection<T> data) {
        if (null != data && data.size() == 1) {
            throw new UncheckedException(msg);
        }
    }

}
