package com.yishuifengxiao.common.tool.validate.validator;

import com.yishuifengxiao.common.tool.validate.InString;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * <p>
 * 目标数据必须在指定的数据集合之内的判断逻辑
 * </p>
 * 判断数据必须在指定的字符串数据里面
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class InStringValidator implements ConstraintValidator<InString, String> {

    private String[] values;

    /**
     * 是否允许目标值为null，默认为true
     */
    private boolean nullable = true;

    /**
     * 是否为大小写敏感，默认为true,表示大小写敏感
     */
    private boolean sensitive = true;

    @Override
    public void initialize(InString constraintAnnotation) {
        this.values = constraintAnnotation.value();
        this.nullable = constraintAnnotation.nullable();
        this.sensitive = constraintAnnotation.sensitive();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (null == value && !this.nullable) {
            return false;
        }
        // @formatter:off
        return this.sensitive ? Arrays.stream(values).anyMatch(v -> StringUtils.equals(v, value)) :
                Arrays.stream(values).anyMatch(v -> StringUtils.equalsIgnoreCase(v, value));
        // @formatter:on
    }

}
