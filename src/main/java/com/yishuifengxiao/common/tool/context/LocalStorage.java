package com.yishuifengxiao.common.tool.context;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 全局存储工具
 * </p>
 * 该工具主要是一个基于内存的KV键值对存储工具。
 *
 * <p>
 * <strong>该工具是一个线程安全类的工具</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class LocalStorage {
    /**
     * 本地线程存储
     */
    private static final Map<String, Object> LOCAL_HOLDER = new HashMap<>();


    /**
     * <p>存储一个数据</p>
     * <p>存储的时的key值默认为<code>value.getClass().getName()</code></p>
     *
     * @param value 待存储的数据
     */
    public synchronized void put(Object value) {
        if (null == value) {
            return;
        }
        LOCAL_HOLDER.put(value.getClass().getName(), value);
    }

    /**
     * <p>存储一个数据</p>
     *
     * @param key   待存储的数据的key
     * @param value 待存储的数据
     */
    public synchronized void put(String key, Object value) {
        if (StringUtils.isBlank(key) || null == value) {
            return;
        }
        LOCAL_HOLDER.put(key.trim(), value);
    }

    /**
     * 根据数据的key获取数据
     *
     * @param key 待存储的数据的key
     * @return 获取到的存储数据
     */
    public synchronized Object get(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return LOCAL_HOLDER.get(key.trim());
    }


    /**
     * 根据数据的key获取数据
     *
     * @param key   待存储的数据的key
     * @param clazz 数据的类型Class
     * @param <T>   数据的类型
     * @return 获取到的存储数据
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T get(String key, Class<T> clazz) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            return (T) LOCAL_HOLDER.get(key.trim());
        } catch (Exception e) {
        }
        return null;

    }

    /**
     * <p>根据数据的key获取数据</p>
     * <p>此方式下默认key为<code>clazz.getName()</code></p>
     *
     * @param clazz 数据的类型Class
     * @param <T>   数据的类型
     * @return 获取到的存储数据
     */
    public synchronized <T> T get(Class<T> clazz) {
        if (null == clazz) {
            return null;
        }
        return get(clazz.getName(), clazz);
    }

    /**
     * 根据数据的key获取数据，若成功获取到此数据则从缓存中删除此数据
     *
     * @param key   待存储的数据的key
     * @param clazz 数据的类型Class
     * @param <T>   数据的类型
     * @return 获取到的存储数据
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T getAndRemove(String key, Class<T> clazz) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        try {
            T value = (T) LOCAL_HOLDER.get(key.trim());
            if (null != value) {
                remove(key.trim());
            }
            return value;
        } catch (Exception e) {
        }
        return null;

    }

    /**
     * <p>根据数据的key获取数据，若成功获取到此数据则从缓存中删除此数据</p>
     * <p>此方式下默认key为<code>clazz.getName()</code></p>
     *
     * @param clazz 数据的类型Class
     * @param <T>   数据的类型
     * @return 获取到的存储数据
     */
    public synchronized <T> T getAndRemove(Class<T> clazz) {
        if (null == clazz) {
            return null;
        }

        return getAndRemove(clazz.getName(), clazz);
    }

    /**
     * 根据数据的key获取数据，若成功获取到此数据则从缓存中删除此数据
     *
     * @param key 待存储的数据的key
     * @return 获取到的存储数据
     */
    public synchronized Object getAndRemove(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        Object value = LOCAL_HOLDER.get(key.trim());
        if (null != value) {
            remove(key.trim());
        }
        return value;
    }

    /**
     * 移除存储的数据
     *
     * @param key 待移除的数据的key
     */
    public synchronized void remove(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        LOCAL_HOLDER.remove(key.trim());
    }

    /**
     * <p>移除存储的数据</p>
     * <p>此方式下默认key为<code>clazz.getName()</code></p>
     *
     * @param <T>   数据的类型
     * @param clazz 待移除的数据的key
     */
    public synchronized <T> void remove(Class<T> clazz) {
        if (null == clazz) {
            return;
        }
        remove(clazz.getName());
    }

    /**
     * 获取所有存储的数据的key
     *
     * @return 所有存储的数据的key
     */
    public synchronized Set<String> keys() {
        return LOCAL_HOLDER.keySet();
    }

    /**
     * 所有存储的数据的key中是否包含指定的key
     *
     * @param key 指定的key
     * @return 包含返回为true, 否则为false
     */
    public synchronized boolean keys(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return LOCAL_HOLDER.containsKey(key);
    }

    /**
     * 清空所有存储的数据
     */
    public synchronized void clear() {
        LOCAL_HOLDER.clear();
    }
}
