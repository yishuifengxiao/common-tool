package com.yishuifengxiao.common.tool.context;

import java.util.Map;
import java.util.WeakHashMap;

import org.springframework.util.Assert;

/**
 * 本地线程工具类
 * 
 * @author yishui
 * @date 2019年10月29日
 * @version 1.0.0
 */
public class LocalStorage {
	/**
	 * 本地线程存储
	 */
	private static final ThreadLocal<Map<String, Object>> CONTEXT_HOLLDER = new ThreadLocal<>();

	/**
	 * 根据键获取存储的值
	 * 
	 * @param key 存储的键
	 * @return
	 */
	public static Object get(String key) {
		Assert.notNull(key, "key值不能为空");
		Map<String, Object> map = get();
		return map.get(key);
	}

	/**
	 * 根据键获取存储的值
	 * 
	 * @param key 存储的键
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> clazz) {
		Assert.notNull(clazz, "key值不能为空");
		return (T) get(clazz.getName());
	}

	/**
	 * 检索出对象并清除当前副本对象
	 * 
	 * @param key 存储的键
	 * @return
	 */
	public synchronized static Object pop(String key) {
		try {
			return get(key);
		} finally {
			clear();
		}
	}

	/**
	 * 检索出对象并清除当前副本对象
	 * 
	 * @param key 存储的键
	 * @return
	 */
	public synchronized static <T> T pop(Class<T> clazz) {
		try {
			return get(clazz);
		} finally {
			clear();
		}
	}

	/**
	 * 将数据存储到本地线程副本中
	 * 
	 * @param <T>
	 * @param t   要存储的数据，不能为null
	 */
	public synchronized static <T> void put(T t) {
		Assert.notNull(t, "存储的值不能为空");
		put(t.getClass().getName(), t);
	}

	/**
	 * 向本地线程副本里存储信息
	 * 
	 * @param key   信息的键
	 * @param value 信息的值
	 */
	public synchronized static void put(String key, Object value) {
		Assert.notNull(key, "key值不能为空");
		Map<String, Object> map = get();
		map.put(key, value);
	}

	/**
	 * 移除本地线程副本里存储信息的某个值
	 * 
	 * @param key 信息的键
	 */
	public synchronized static void remove(String key) {
		Assert.notNull(key, "key值不能为空");
		Map<String, Object> map = get();
		map.put(key, null);
		map.remove(key);
	}

	/**
	 * 清空本地线程副本
	 */
	public synchronized static void clear() {
		CONTEXT_HOLLDER.remove();
	}

	/**
	 * 获取本地线程副本里的键值对集合
	 * 
	 * @return
	 */
	private static Map<String, Object> get() {
		Map<String, Object> map = CONTEXT_HOLLDER.get();
		if (map == null) {
			map = new WeakHashMap<>();
			CONTEXT_HOLLDER.set(map);
		}
		return map;
	}

}
