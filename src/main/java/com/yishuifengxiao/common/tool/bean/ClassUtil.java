/**
 *
 */
package com.yishuifengxiao.common.tool.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.persistence.Transient;

import com.yishuifengxiao.common.tool.collections.DataUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * class 工具
 * </p>
 *
 * <p>
 * 主要功能为
 * </p>
 * <ul>
 * <li>获取类的所有属性字段</li>
 * <li>根据属性的名字获取对象的属性的值</li>
 * </ul>
 * 
 * <p>
 * <strong>当前工具是一个线程安全类工具</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class ClassUtil {

	/**
	 * 提取出一个类里所有的属性字段(包括父类里的属性字段)
	 *
	 * @param <T>   对象类型
	 * @param clazz 待处理的类
	 * @return 所有提取的属性字段
	 */
	public synchronized static <T> List<Field> fields(Class<T> clazz) {
		return fields(null, clazz);
	}

	/**
	 * 提取出一个类里所有的属性字段(包括父类里的属性字段)
	 *
	 * @param <T>               对象类型
	 * @param clazz             待处理的类
	 * @param noSpecialModifier 是否过滤掉特殊修饰的字段
	 * @return 所有提取的属性字段
	 */
	public synchronized static <T> List<Field> fields(Class<T> clazz, boolean noSpecialModifier) {
		List<Field> fields = fields(null, clazz);

		return noSpecialModifier
				? fields.parallelStream().filter(v -> !isSpecialModifier(v)).collect(Collectors.toList())
				: fields;
	}

	/**
	 * 提取出类的所有属性
	 *
	 * @param <T>   对象类型
	 * @param list  提取后的属性字段
	 * @param clazz 待处理的类
	 * @return 所有提取的属性字段
	 */
	private static <T> List<Field> fields(List<Field> list, Class<T> clazz) {
		if (null == list) {
			list = new ArrayList<>();
		}
		DataUtil.stream(clazz.getDeclaredFields()).filter(Objects::nonNull).forEach(list::add);
		Class<? super T> superclass = clazz.getSuperclass();
		if (null == superclass || superclass.isAssignableFrom(Object.class)) {
			return list.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
		}
		return fields(list, superclass);
	}

	/**
	 * <p>
	 * 该字段是否被特殊修饰
	 * </p>
	 *
	 * <p>
	 * 特殊修饰的关键字如：<code>@Transient</code>、<code>final</code>、<code>static</code>、<code>native</code>、<code>abstract</code>
	 * </p>
	 *
	 * @param field 字段
	 * @return true表示被特殊修饰
	 */
	public synchronized static boolean isSpecialModifier(Field field) {
		Transient sient = field.getAnnotation(Transient.class);
		// 如果被@Transient修饰了就不处理
		if (null != sient) {
			return true;
		}
		// 被Transient 修饰的不处理
		if (Modifier.isTransient(field.getModifiers())) {
			return true;
		}

		// 被final修饰的不处理
		if (Modifier.isFinal(field.getModifiers())) {
			return true;
		}

		// 被static 修饰的不处理
		if (Modifier.isStatic(field.getModifiers())) {
			return true;
		}

		// 被native 修饰的不处理
		if (Modifier.isNative(field.getModifiers())) {
			return true;
		}

		// 被abstract 修饰的不处理
		if (Modifier.isAbstract(field.getModifiers())) {
			return true;
		}

		// 属性为接口
		if (Modifier.isInterface(field.getModifiers())) {
			return true;
		}

		return false;
	}

	/**
	 * 根据属性名字获取对象里对应属性的值
	 *
	 * @param data      待处理的对象
	 * @param fieldName 属性名字
	 * @return 该属性对应的值
	 */
	public synchronized static Object extractValue(Object data, String fieldName) {
		if (null == data || null == fieldName) {
			return null;
		}
		try {
			Field field = fields(data.getClass()).parallelStream().filter(v -> v.getName().equals(fieldName.trim()))
					.findFirst().orElse(null);
			if (null == field) {
				return null;
			}
			field.setAccessible(true);
			return field.get(data);
		} catch (Exception e) {
			log.warn("根据属性名获取属性值时出现问题，出现问题的原因为 {}", e.getMessage());
		}
		return null;
	}

	/**
	 * 遍历对象所有的属性和值
	 *
	 * @param data   待处理的对象
	 * @param action 遍历操作
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized static void forEach(Object data, BiConsumer action) {
		fields(data.getClass()).forEach(t -> action.accept(t, extractValue(data, t.getName())));
	}

}
