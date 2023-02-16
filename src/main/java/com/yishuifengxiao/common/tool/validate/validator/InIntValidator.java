package com.yishuifengxiao.common.tool.validate.validator;

import com.yishuifengxiao.common.tool.lang.CompareUtil;
import com.yishuifengxiao.common.tool.validate.InInt;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * <p>
 * 目标数据必须在指定的数据集合之内的判断逻辑
 * </p>
 * 判断数据必须在指定的整型数据里面
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class InIntValidator implements ConstraintValidator<InInt, Integer> {

    private int[] values;

    /**
     * 是否允许目标值为null，默认为true
     */
    private boolean nullable = true;

    @Override
    public void initialize(InInt constraintAnnotation) {
        this.values = constraintAnnotation.value();
        this.nullable = constraintAnnotation.nullable();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (null == value && !this.nullable) {
            return false;
        }
        return Arrays.stream(values).anyMatch(v -> CompareUtil.equals(v, value));
    }

}
