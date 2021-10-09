package com.yishuifengxiao.common.tool.validate.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.yishuifengxiao.common.tool.lang.NumberUtil;
import com.yishuifengxiao.common.tool.validate.InLong;

/**
 * <p>
 * 目标数据必须在指定的数据集合之内的判断逻辑
 * </p>
 * 判断数据必须在指定的长整型数据里面
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
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
