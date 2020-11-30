package com.yishuifengxiao.common.tool.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

/**
 * 全局存储工具类
 * 
 * @author yishui
 * @date 2019年10月29日
 * @version 1.0.0
 */
@Slf4j
public final class LocalStorage {
	/**
	 * 本地线程存储
	 */
	private static final Map<String, Object> LOCAL_HOLLDER = new HashMap<>();

	/**
	 * 根据键获取存储的值
	 * 
	 * @param key 存储的键
	 * @return
	 */
	public synchronized static Object get(String key) {
		Assert.notNull(key, "key值不能为空");
		return LOCAL_HOLLDER.get(key);
	}

	/**
	 * 根据键获取存储的值
	 * 
	 * @param key 存储的键
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public synchronized static <T> T get(Class<T> clazz) {
		Assert.notNull(clazz, "key值不能为空");
		try {
			return (T) get(clazz.getName());
		} catch (Exception e) {
			log.info("【易水工具】获取存储的数据时出现问题，出现问题的原因为 {}", e.getMessage());
		}
		return null;

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
		LOCAL_HOLLDER.put(key, value);
	}

	/**
	 * 移除本地线程副本里存储信息的某个值
	 * 
	 * @param key 信息的键
	 */
	public synchronized static void remove(String key) {
		Assert.notNull(key, "key值不能为空");
		LOCAL_HOLLDER.put(key, null);
		LOCAL_HOLLDER.remove(key);
	}

	/**
	 * 清空本地线程副本
	 */
	public synchronized static void clear() {
		LOCAL_HOLLDER.clear();
	}

	/**
	 * 获取本地线程副本里的键值对集合
	 * 
	 * @return
	 */
	public static Map<String, Object> getAll() {

		return LOCAL_HOLLDER;
	}
}
