package com.yishuifengxiao.common.tool.validate;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.util.Collections;
import java.util.Set;

/**
 * 校验工具类
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class BeanValidator {
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
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(t, null == clazz ? Default.class : clazz);
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
        Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(t, null == clazz ? Default.class : clazz);
        return (null == constraintViolations || constraintViolations.isEmpty()) ? null : constraintViolations.iterator().next().getMessage();
    }

}