package com.yishuifengxiao.common.tool.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cache.annotation.Cacheable;

/**
 * 缓存注解<br/>
 * <br/>
 * 其实就是
 * 
 * <pre>
 * &#64;Cacheable(unless = "#result==null")
 * </pre>
 * 
 * 的快捷方式
 * 
 * @author yishui
 * @date 2020年12月6日
 * @Version 0.0.1
 */
@Cacheable(unless = "#result==null")
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Cache {

}
