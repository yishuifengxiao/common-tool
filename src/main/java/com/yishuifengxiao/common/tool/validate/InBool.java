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

import com.yishuifengxiao.common.tool.validate.validator.InBoolValidator;

/**
 * <p>
 * 目标数据必须为0或1
 * </p>
 * 针对整型数据
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = InBoolValidator.class)
public @interface InBool {

	/**
	 * 默认错误消息
	 * 
	 * @return 默认错误消息
	 */
	String message() default "非法的数据";

	/**
	 * 是否允许目标值为null，默认为true
	 * 
	 * @return 是否允许目标值为null，默认为true
	 */
	boolean nullable() default true;

	/**
	 * 分组
	 * 
	 * @return 校验分组
	 */
	Class<?>[] groups() default {};

	/**
	 * 负载
	 * 
	 * @return 负载
	 */
	Class<? extends Payload>[] payload() default {};

	/**
	 * 指定多个时使用
	 * @author yishui
	 * @version 1.0.0
	 * @since 1.0.0
	 */
	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		InBool[] value();
	}
}
