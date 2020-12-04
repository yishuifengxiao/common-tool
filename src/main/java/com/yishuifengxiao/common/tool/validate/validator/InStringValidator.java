package com.yishuifengxiao.common.tool.validate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.validate.InString;

/**
 * 目标数据必须在指定的数据集合之内的判断逻辑<br/>
 * 判断数据必须在指定的整型数据里面
 * 
 * @author qingteng
 * @date 2020年12月4日
 * @version 1.0.0
 */
public class InStringValidator implements ConstraintValidator<InString, String> {

	private String[] values;

	@Override
	public void initialize(InString constraintAnnotation) {
		this.values = constraintAnnotation.value();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (null == value) {
			return false;
		}
		boolean contain = false;
		if (null != values) {
			for (String val : values) {
				if (StringUtils.equals(value, val)) {
					contain = true;
					break;
				}
			}
		}

		return contain;
	}

}
