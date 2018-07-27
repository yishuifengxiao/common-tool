package com.yishui.common.tool.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

/**
 * 自定义密码校验器
 * 
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public class PasswordValidator implements ConstraintValidator<Password, String> {

	private String[] forbiddenWords = { "admin" };

	@Override
	public void initialize(Password constraintAnnotation) {
		// 初始化，得到注解数据
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isEmpty(value)) {
			return true;
		}

		for (String word : forbiddenWords) {
			if (value.contains(word)) {
				return false;// 验证失败
			}
		}
		return true;
	}
}