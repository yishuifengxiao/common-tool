package com.yishuifengxiao.common.tool.sensitive;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>
 * Json序列化脱敏注解
 * </p>
 * <p>
 * 使用方法如下： 在POJO的需要脱密的属性上加上
 *
 * <pre>
 *  &#64;SensitiveInfo(SensitiveEnum.MOBILE_PHONE)
 * </pre>
 * <p>
 * 即可
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