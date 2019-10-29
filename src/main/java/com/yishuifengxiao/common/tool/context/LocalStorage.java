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
	 * 向本地线程副本里存储信息
	 * 
	 * @param key   信息的键
	 * @param value 信息的值
	 */
	public static void put(String key, Object value) {
		Assert.notNull(key, "key值不能为空");
		Map<String, Object> map = get();
		map.put(key, value);
	}

	/**
	 * 移除本地线程副本里存储信息的某个值
	 * 
	 * @param key 信息的键
	 */
	public static void remove(String key) {
		Assert.notNull(key, "key值不能为空");
		Map<String, Object> map = get();
		map.put(key, null);
		map.remove(key);
	}

	/**
	 * 清空本地线程副本
	 */
	public static void clear() {
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
