package com.yishuifengxiao.common.tool.validate;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import com.yishuifengxiao.common.tool.validate.validator.InBoolValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * <p>布尔值校验注解</p>
 * <p>校验目标值必须为0或1，适用于数据库布尔字段（tinyint类型）的校验场景。</p>
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
