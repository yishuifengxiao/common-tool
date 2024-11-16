package com.yishuifengxiao.common.tool.validate;

import com.yishuifengxiao.common.tool.validate.validator.InBoolValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * 目标数据必须为0或1
 * </p>
 * 该注解针对整型数据，不适用其他数据类型
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
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
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    @Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        /**
         * 校验值
         *
         * @return 校验值
         */
        InBool[] value();
    }
}
