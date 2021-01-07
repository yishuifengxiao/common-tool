package com.yishuifengxiao.common.tool.sensitive;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * <p>Json序列化脱敏注解</p>
 * 
 * 使用方法如下： 在POJO的需要脱密的属性上加上
 *
 * <pre>
 *  &#64;SensitiveInfo(SensitiveEnum.MOBILE_PHONE)
 * </pre>
 * 
 * 即可
 * 
 * @author qingteng
 * @date 2020年12月13日
 * @version 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerialize.class)
public @interface Sensitive {

	SensitiveEnum value();

}