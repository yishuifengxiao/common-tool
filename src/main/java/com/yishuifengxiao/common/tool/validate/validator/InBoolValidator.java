package com.yishuifengxiao.common.tool.validate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.yishuifengxiao.common.tool.lang.NumberUtil;
import com.yishuifengxiao.common.tool.validate.InBool;

/**
 * <p>
 * 目标数据必须在指定的数据集合之内的判断逻辑
 * </p>
 * 判断数据必须在指定的整型数据里面,输入的数据必须为0或1，不能为其他的数据
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class InBoolValidator implements ConstraintValidator<InBool, Integer> {

	private boolean nullable = true;

	@Override
	public void initialize(InBool constraintAnnotation) {
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		if (null == value && !this.nullable) {
			return false;
		}

		return NumberUtil.equals(value, 1) || NumberUtil.equals(value, 0);
	}

}
