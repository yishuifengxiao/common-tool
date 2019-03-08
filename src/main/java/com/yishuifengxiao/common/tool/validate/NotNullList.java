/**
 * 
 */
package com.yishuifengxiao.common.tool.validate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.yishuifengxiao.common.tool.validate.Password;

/**
 * 校验不是一个空的list，且不是一个只包含null的list
 * 
 * @author yishui
 * @date 2019年3月8日
 * @version 1.0.0
 */
@Target(value = { ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR,
		ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
// 指定验证器
@Constraint(validatedBy = NotNullListConstraintValidator.class)
@Documented
public @interface NotNullList {
	// 默认错误消息
	String message() default "集合不能为空";

	// 分组
	Class<?>[] groups() default {};

	// 负载
	Class<? extends Payload>[] payload() default {};

	// 指定多个时使用
	@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR,
			ElementType.ANNOTATION_TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	@interface List {
		Password[] value();
	}
}
