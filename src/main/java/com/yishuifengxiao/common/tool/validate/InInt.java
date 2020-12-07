package com.yishuifengxiao.common.tool.validate;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.yishuifengxiao.common.tool.validate.validator.InIntValidator;

/**
 * 目标数据必须在指定的数据集合之内<br/>
 * 针对整型数据
 * 
 * @author qingteng
 * @date 2020年12月4日
 * @version 1.0.0
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = InIntValidator.class)
public @interface InInt {

	/**
	 * 默认错误消息
	 * 
	 * @return
	 */
	String message() default "非法的数据";

	/**
	 * 指定的数据集合
	 * 
	 * @return
	 */
	int[] value() default {};

	/**
	 * 是否允许目标值为null，默认为true
	 * 
	 * @return
	 */
	boolean nullable() default true;

	/**
	 * 分组
	 * 
	 * @return
	 */
	Class<?>[] groups() default {};

	/**
	 * 负载
	 * 
	 * @return
	 */
	Class<? extends Payload>[] payload() default {};

	// 指定多个时使用
	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		InInt[] value();
	}
}
