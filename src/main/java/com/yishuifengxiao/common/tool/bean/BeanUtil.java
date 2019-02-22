/**
 * 
 */
package com.yishuifengxiao.common.tool.bean;

import java.lang.reflect.Field;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.cglib.beans.BeanCopier;

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
	 * @param source
	 *            源对象
	 * @param target
	 *            目标对象
	 * @param converter
	 *            目标对象
	 * @return
	 */
	public static <S, T> T copy(S source, T target) {
		BeanCopier.create(source.getClass(), target.getClass(), false).copy(source, target, null);
		return target;

	}

	/**
	 * 去除对象里非空属性之外的属性和制表符
	 * 
	 * @param source
	 *            原始对象
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

}
