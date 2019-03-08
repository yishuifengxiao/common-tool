package com.yishuifengxiao.common.tool.validate;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;

@SuppressWarnings("rawtypes")
public class NotNullListConstraintValidator implements ConstraintValidator<NotNullList, List> {

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValid(List value, ConstraintValidatorContext context) {
		if (EmptyUtil.isEmpty(value)) {
			return false;
		}
		List data = (List) value.parallelStream().filter(t -> t != null && StringUtils.isNotBlank(t.toString()))
				.collect(Collectors.toList());
		return EmptyUtil.notEmpty(data);
	}

	@Override
	public void initialize(NotNullList constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

}
