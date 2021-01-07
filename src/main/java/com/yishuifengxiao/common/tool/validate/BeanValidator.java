package com.yishuifengxiao.common.tool.validate;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;
import com.yishuifengxiao.common.tool.exception.ValidateException;

/**
 * 校验工具类
 * 
 * @author qingteng
 * @date 2020年12月4日
 * @version 1.0.0
 */
public class BeanValidator {
	private final static Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	/**
	 * <p>
	 * 使用默认的分组对数据进行校验
	 * </p>
	 * 如果数据不符合条件就抛出异常
	 * 
	 * @param <T> 需要校验的数据的类型
	 * @param t   需要校验的数据
	 * @throws ValidateException
	 */
	public static <T> void validate(T t) throws ValidateException {
		validate(t, null);
	}

	/**
	 * <p>
	 * 使用指定的分组对数据进行校验
	 * </p>
	 * 如果数据不符合条件就抛出异常
	 * 
	 * @param <T>   需要校验的数据的类型
	 * @param <G>   校验的分组
	 * @param t     需要校验的数据
	 * @param clazz 校验的分组，如果为null就是默认的分组
	 * @throws ValidateException
	 */
	public static <T, G> void validate(T t, Class<G> clazz) throws ValidateException {
		Set<ConstraintViolation<T>> constraintViolations = VALIDATOR.validate(t, null == clazz ? Default.class : clazz);
		if (EmptyUtil.isEmpty(constraintViolations)) {
			return;
		}

		for (ConstraintViolation<T> constraintViolation : constraintViolations) {
			throw new ValidateException(constraintViolation.getMessage());
		}

	}

}