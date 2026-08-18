package com.yishuifengxiao.common.tool.sensitive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * <p>JSON序列化脱敏注解</p>
 * <p>用于标记需要脱敏的字段，在JSON序列化时自动进行脱敏处理。</p>
 * <p>使用示例：</p>
 * <pre>
 * &#64;Sensitive(SensitiveEnum.MOBILE_PHONE)
 * private String phone;
 * </pre>
 * <p>配合{@link SensitiveSerialize}序列化器使用。</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerialize.class)
public @interface Sensitive {

    /**
     * 获取敏感信息枚举类型
     *
     * @return 敏感信息枚举类型
     */
    SensitiveEnum value();

}