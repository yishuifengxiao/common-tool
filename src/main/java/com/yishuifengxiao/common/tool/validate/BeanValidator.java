package com.yishuifengxiao.common.tool.validate;


import java.util.Collections;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

/**
 * <p>Bean校验工具类</p>
 * <p>基于Jakarta Bean Validation提供对象校验功能，支持分组校验。</p>
 * <p>特性：</p>
 * <ul>
 * <li>使用默认分组或指定分组校验</li>
 * <li>返回完整校验结果或仅第一条错误信息</li>
 * <li>Validator实例线程安全，静态初始化</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class BeanValidator {
    // Validator 是线程安全的，因此静态初始化是安全的做法
    private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    /**
     * 使用默认的分组对数据进行校验，如果数据不符合条件就抛出异常
     *
     * @param <T> 需要校验的数据的类型
     * @param t   需要校验的数据
     * @return 校验结果，若数据合法则输出结果为空
     */
    public static <T> Set<ConstraintViolation<T>> validate(T t) {
        return validate(t, null);
    }

    /**
     * 使用默认的分组对数据进行校验，如果数据不符合条件就抛出异常
     *
     * @param <T> 需要校验的数据的类型
     * @param t   需要校验的数据
     * @return 校验结果，若数据不符合要求则提取出第一条提示信息
     */
    public static <T> String validateResult(T t) {
        return validateResult(t, null);
    }

    /**
     * 使用指定的分组对数据进行校验
     *
     * @param <T>   需要校验的数据的类型
     * @param <G>   校验的分组
     * @param t     需要校验的数据
     * @param clazz 校验的分组，如果为null就是默认的分组
     * @return 校验结果，若数据合法则输出结果为空
     */
    public static <T, G> Set<ConstraintViolation<T>> validate(T t, Class<G> clazz) {
        Set<ConstraintViolation<T>> constraintViolations = performValidation(t, clazz);
        return null == constraintViolations ? Collections.emptySet() : constraintViolations;
    }

    /**
     * 使用指定的分组对数据进行校验，若数据不符合要求则提取出第一条提示信息
     *
     * @param <T>   需要校验的数据的类型
     * @param <G>   校验的分组
     * @param t     需要校验的数据
     * @param clazz 校验的分组，如果为null就是默认的分组
     * @return 校验结果，若数据不符合要求则提取出第一条提示信息
     */
    public static <T, G> String validateResult(T t, Class<G> clazz) {
        Set<ConstraintViolation<T>> constraintViolations = performValidation(t, clazz);
        return (null == constraintViolations || constraintViolations.isEmpty()) ? null : constraintViolations.iterator().next().getMessage();
    }

    /**
     * 执行实际的校验操作
     *
     * @param t     待校验对象
     * @param clazz 分组类
     * @param <T>   对象泛型
     * @param <G>   分组泛型
     * @return 约束违规集合
     */
    private static <T, G> Set<ConstraintViolation<T>> performValidation(T t, Class<G> clazz) {
        return VALIDATOR.validate(t, null == clazz ? Default.class : clazz);
    }
}
