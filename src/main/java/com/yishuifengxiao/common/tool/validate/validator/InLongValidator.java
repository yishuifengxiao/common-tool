package com.yishuifengxiao.common.tool.validate.validator;

import com.yishuifengxiao.common.tool.lang.CompareUtil;
import com.yishuifengxiao.common.tool.validate.InLong;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

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

    /**
     * 是否允许目标值为null，默认为true
     */
    private boolean nullable = true;

    @Override
    public void initialize(InLong constraintAnnotation) {
        this.values = constraintAnnotation.value();
        this.nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (!this.nullable) {
            return null != value && Arrays.stream(this.values).anyMatch(v -> CompareUtil.equals(v, value));
        }
        return null == value || Arrays.stream(this.values).anyMatch(v -> CompareUtil.equals(v, value));
    }

}
