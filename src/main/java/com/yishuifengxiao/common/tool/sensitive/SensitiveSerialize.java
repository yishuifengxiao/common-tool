package com.yishuifengxiao.common.tool.sensitive;


import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Objects;


/**
 * Json脱敏序列化
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class SensitiveSerialize extends ValueSerializer<Object> {

    /**
     * 敏感信息枚举类型
     */
    private SensitiveEnum type;

    /**
     * 构造函数
     */
    public SensitiveSerialize() {
    }

    /**
     * 构造函数
     *
     * @param type 敏感信息枚举类型
     */
    public SensitiveSerialize(final SensitiveEnum type) {
        this.type = type;
    }

    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializationContext serializers)
            throws JacksonException {
        if (this.type == null) {
            jsonGenerator.writeString(String.valueOf(value));
            return;
        }
        switch (this.type) {
            case ID_CARD: {
                jsonGenerator.writeString(SensitiveUtil.idCard(String.valueOf(value)));
                break;
            }
            case MOBILE_PHONE: {
                jsonGenerator.writeString(SensitiveUtil.phone(String.valueOf(value)));
                break;
            }
            case PASSWORD: {
                jsonGenerator.writeString(SensitiveUtil.password(String.valueOf(value)));
                break;
            }
            case NAME: {
                jsonGenerator.writeString(SensitiveUtil.name(String.valueOf(value)));
                break;
            }
            default:
                jsonGenerator.writeString(String.valueOf(value));

        }

    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext serializers, BeanProperty beanProperty) {

        if (beanProperty == null) {
            return this;
        }

        // 非 String 类直接跳过
        if (Objects.equals(beanProperty.getType().getRawClass(), String.class)) {
            Sensitive sensitiveInfo = beanProperty.getAnnotation(Sensitive.class);
            if (sensitiveInfo == null) {
                sensitiveInfo = beanProperty.getContextAnnotation(Sensitive.class);
            }
            // 如果能得到注解，就将注解的 value 传入 SensitiveInfoSerialize
            if (sensitiveInfo != null) {
                return new SensitiveSerialize(sensitiveInfo.value());
            }
        }
        return serializers.findValueSerializer(beanProperty.getType());

    }
}
