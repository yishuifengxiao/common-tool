/**
 * 
 */
package com.yishuifengxiao.common.tool.bean;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.BeanUtils;

import com.yishuifengxiao.common.tool.constant.ErrorCode;
import com.yishuifengxiao.common.tool.exception.CustomException;
import com.yishuifengxiao.common.tool.exception.ValidateException;

/**
 * 对象转换类
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class BeanUtil {

	/**
	 * 将源对象里属性值复制给目标对象
	 * 
	 * @param <S>    源对象的类型
	 * @param <T>    目标对象的类型
	 * @param source 源对象
	 * @param target 目标对象
	 * @return 复制后的目标对象
	 * @throws ValidateException 复制时发生问题
	 */
	public static synchronized <S, T> T copy(S source, T target) throws ValidateException {
		if (source == null) {
			throw new ValidateException(ErrorCode.PARAM_NULL, "源数据不能为空");
		}
		if (target == null) {
			throw new ValidateException(ErrorCode.PARAM_NULL, "目标填充不能为空");
		}
		try {
			BeanUtils.copyProperties(source, target);
			return target;
		} catch (Exception e) {
			throw new ValidateException(ErrorCode.DATA_CONVERT_ERROR, e.getMessage());
		}

	}

	/**
	 * 去除对象里非空属性之外的属性和制表符
	 * 
	 * @param <T>    源对象的类型
	 * @param source 原始对象
	 * @return 过滤后的对象
	 * @throws IllegalAccessException 过滤时发生问题
	 */
	public static <T> T setNullValue(T source) throws IllegalAccessException {
		Field[] fields = source.getClass().getDeclaredFields();
		for (Field field : fields) {
			if ("class java.lang.String".equals(field.getGenericType().toString())) {
				field.setAccessible(true);
				Object obj = field.get(source);
				if (obj != null && obj.equals("")) {
					field.set(source, null);
				} else if (obj != null) {
					field.set(source,
							StringEscapeUtils.escapeJava(obj.toString()).replace("\\", "\\" + "\\").replace("(", "\\(")
									.replace(")", "\\)").replace("%", "\\%").replace("*", "\\*").replace("[", "\\[")
									.replace("]", "\\]").replace("|", "\\|").replace(".", "\\.").replace("$", "\\$")
									.replace("+", "\\+").trim());
				}
			}
		}
		return source;
	}

	/**
	 * 将Java对象序列化为二进制数据
	 * 
	 * @param obj 需要序列化的的对象
	 * @return 二进制数据
	 * @throws CustomException 转换时发生的异常
	 */
	public static byte[] objectToByte(Object obj) throws CustomException {
		byte[] bytes = null;
		try {
			// object to bytearray
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream oo = new ObjectOutputStream(bo);
			oo.writeObject(obj);
			bytes = bo.toByteArray();
			bo.close();
			oo.close();
		} catch (Exception e) {
			throw new CustomException(ErrorCode.BEAN_EXCEPTION, e.getMessage());
		}
		return bytes;
	}

	/**
	 * 将序列化化后的二进制数据反序列化为对象
	 * 
	 * @param bytes 序列化化后的二进制数据
	 * @return 对象
	 * @throws CustomException 转换时发生的异常
	 */
	public static Object byteToObject(byte[] bytes) throws CustomException {
		Object obj = null;
		try {
			// bytearray to object
			ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
			ObjectInputStream oi = new ObjectInputStream(bi);
			obj = oi.readObject();
			bi.close();
			oi.close();
		} catch (Exception e) {
			throw new CustomException(ErrorCode.BEAN_EXCEPTION, e.getMessage());
		}
		return obj;
	}

	/**
	 * 将序列化化后的二进制数据反序列化为对象
	 * 
	 * @param <T>   目标对象的类型
	 * @param bytes 原始的二进制数据
	 * @param clazz 目标对象
	 * @return 反序列化之后的对象
	 * @throws CustomException 转换时发生的异常
	 */
	@SuppressWarnings("unchecked")
	public static <T> T byteToObject(byte[] bytes, Class<T> clazz) throws CustomException {
		T t = null;
		try {
			// bytearray to object
			ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
			ObjectInputStream oi = new ObjectInputStream(bi);
			t = (T) oi.readObject();
			bi.close();
			oi.close();
		} catch (Exception e) {
			throw new CustomException(ErrorCode.BEAN_EXCEPTION, e.getMessage());
		}
		return t;
	}

}
