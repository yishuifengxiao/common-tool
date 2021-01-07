package com.yishuifengxiao.common.tool.sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.Objects;


/**
 * Json脱敏序列化
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class SensitiveSerialize extends JsonSerializer<Object> implements ContextualSerializer {

	private SensitiveEnum type;

	public SensitiveSerialize() {
	}

	public SensitiveSerialize(final SensitiveEnum type) {
		this.type = type;
	}

	@Override
	public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializers)
			throws IOException {
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
	public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
			throws JsonMappingException {

		if (beanProperty != null) {

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
			return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
		}
		return serializerProvider.findNullValueSerializer(beanProperty);

	}
}
