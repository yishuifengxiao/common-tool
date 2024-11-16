package com.yishuifengxiao.common.tool.validate;

import com.yishuifengxiao.common.tool.validate.validator.InStringValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * 目标数据必须在指定的数据集合之内
 * </p>
 * 针对字符串数据，不适用其他数据类型
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = InStringValidator.class)
public @interface InString {

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
     * 是否为大小写敏感，默认为true,表示大小写敏感
     *
     * @return 默认为true, 表示大小写敏感
     */
    boolean sensitive() default true;

    /**
     * 指定的数据
     *
     * @return 指定的数据
     */
    String[] value() default {};

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
        InString[] value();
    }
}
