package com.yishuifengxiao.common.tool.validate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.yishuifengxiao.common.tool.utils.NumberUtil;
import com.yishuifengxiao.common.tool.validate.InLong;

/**
 * 目标数据必须在指定的数据集合之内的判断的验证其
 * 
 * @author qingteng
 * @date 2020年12月4日
 * @version 1.0.0
 */
public class InLongValidator implements ConstraintValidator<InLong, Long> {

	private long[] values;
	private boolean nullable = true;

	@Override
	public void initialize(InLong constraintAnnotation) {
		this.values = constraintAnnotation.value();
		this.nullable = constraintAnnotation.nullable();
	}

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		if (null == value && !this.nullable) {
			return false;
		}
		boolean contain = false;
		if (null != values) {
			for (long val : values) {
				if (NumberUtil.equals(value, val)) {
					contain = true;
					break;
				}
			}
		}

		return contain;
	}

}
