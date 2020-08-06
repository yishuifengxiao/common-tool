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

import com.yishuifengxiao.common.tool.exception.CustomException;

/**
 * 对象转换类
 * 
 * @author yishui
 * @date 2018年12月6日
 * @Version 0.0.1
 */
public final class BeanUtil {

	/**
	 * 将源对象里属性值复制给目标对象
	 * 
	 * @param source    源对象
	 * @param target    目标对象
	 * @param converter 目标对象
	 * @return
	 */
	public static <S, T> T copy(S source, T target) {
		if (source == null || target == null) {
			return null;
		}
		BeanUtils.copyProperties(source, target);
		return target;
	}

	/**
	 * 去除对象里非空属性之外的属性和制表符
	 * 
	 * @param source 原始对象
	 * @return 过滤后的对象
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 */
	public static <T> T setNullValue(T source)
			throws IllegalArgumentException, IllegalAccessException, SecurityException {
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
	 * @throws CustomException
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
			throw new CustomException(e.getMessage());
		}
		return bytes;
	}

	/**
	 * 将序列化化后的二进制数据反序列化为对象
	 * 
	 * @param bytes 序列化化后的二进制数据
	 * @return 对象
	 * @throws CustomException
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
			throw new CustomException(e.getMessage());
		}
		return obj;
	}

	/**
	 * 将序列化化后的二进制数据反序列化为对象
	 * 
	 * @param t     希望序列化成的数据类型，不能为空
	 * 
	 * @param bytes 序列化化后的二进制数据
	 * @return 对象
	 * @throws CustomException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T byteToObject(T t, byte[] bytes) throws CustomException {

		try {
			// bytearray to object
			ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
			ObjectInputStream oi = new ObjectInputStream(bi);
			t = (T) oi.readObject();
			bi.close();
			oi.close();
		} catch (Exception e) {
			throw new CustomException(e.getMessage());
		}
		return t;
	}

}
