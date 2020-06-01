package com.yishuifengxiao.common.tool.context;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * map工具类
 * 
 * @author yishui
 * @date 2020年6月1日
 * @version 1.0.0
 */
public class MapUtil {

	/**
	 * 存储数据
	 */
	private static final Map<String, Object> MAP = new HashMap<>();

	/**
	 * 根据键获取对应的数据
	 * 
	 * @param key 键
	 * @return 数据
	 */
	public static Object get(String key) {
		return getObject(key);
	}

	/**
	 * 根据键获取对应类型的数据
	 * 
	 * @param <T> 数据类型
	 * @param key 键
	 * @param t   数据类型的实例
	 * @return 若值不存在或值的类型不对应，则返回为null
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(String key, T t) {
		try {
			t = (T) getObject(key);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return t;
	}

	/**
	 * 根据键获取Object数据
	 * 
	 * @param key 键
	 * @return Object数据
	 */
	public static Object getObject(String key) {
		if (StringUtils.isBlank(key)) {
			return null;
		}
		return MAP.get(key);
	}

	/**
	 * 根据键获取String数据
	 * 
	 * @param key 键
	 * @return String数据，若值不存在或值的类型不对应，则返回为null
	 */
	public static String getString(String key) {
		Object obj = getObject(key);
		if (obj instanceof String) {
			return (String) obj;
		}
		return null;
	}

	/**
	 * 根据键获取Integer数据
	 * 
	 * @param key 键
	 * @return Integer数据，若值不存在或值的类型不对应，则返回为null
	 */
	public static Integer getInt(String key) {
		Object obj = getObject(key);
		if (obj instanceof Integer) {
			return (Integer) obj;
		}
		return null;
	}

	/**
	 * 根据键获取Boolean数据
	 * 
	 * @param key 键
	 * @return Boolean数据，若值不存在或值的类型不对应，则返回为null
	 */
	public static Boolean getBoolean(String key) {
		Object obj = getObject(key);
		if (obj instanceof Boolean) {
			return (Boolean) obj;
		}
		return null;
	}

	/**
	 * 根据键获取Float数据
	 * 
	 * @param key 键
	 * @return Float数据，若值不存在或值的类型不对应，则返回为null
	 */
	public static Float getFloat(String key) {
		Object obj = getObject(key);
		if (obj instanceof Float) {
			return (Float) obj;
		}
		return null;
	}

	/**
	 * 根据键获取Double数据
	 * 
	 * @param key 键
	 * @return Double数据，若值不存在或值的类型不对应，则返回为null
	 */
	public static Double getDouble(String key) {
		Object obj = getObject(key);
		if (obj instanceof Double) {
			return (Double) obj;
		}
		return null;
	}

	/**
	 * 存放一个键值对数据
	 * 
	 * @param key   键
	 * @param value 值
	 */
	public synchronized static void put(String key, Object value) {
		MAP.put(key, value);
	}

	/**
	 * 清空所有缓存的数据
	 */
	public synchronized static void clear() {
		MAP.clear();
	}

}
