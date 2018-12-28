package com.yishuifengxiao.common.tool.validate;
 import javax.validation.Constraint;  
import javax.validation.Payload;  
import java.lang.annotation.Documented;  
import java.lang.annotation.Retention;  
import java.lang.annotation.Target;  
import static java.lang.annotation.ElementType.*;  
import static java.lang.annotation.RetentionPolicy.*;  
/**
 * 自定义密码校验器
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
//指定验证器  
@Constraint(validatedBy = PasswordValidator.class)  
@Documented  
public @interface Password {  
  
    //默认错误消息  
    String message() default "{forbidden.word}";  
  
    //分组  
    Class<?>[] groups() default { };  
  
    //负载  
    Class<? extends Payload>[] payload() default { };  
  
    //指定多个时使用  
    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })  
    @Retention(RUNTIME)  
    @Documented  
    @interface List {  
        Password[] value();  
    }  
}  