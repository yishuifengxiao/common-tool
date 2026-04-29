package com.yishuifengxiao.common.tool.context;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>本地缓存工具类</p>
 * <p>基于内存的线程安全KV键值对存储工具，提供全局数据共享能力。</p>
 * <p>特性：</p>
 * <ul>
 * <li>线程安全的并发访问</li>
 * <li>支持懒加载缓存机制</li>
 * <li>支持按类型自动生成key</li>
 * <li>提供完整的CRUD操作</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class LocalCache {
    /**
     * 本地线程存储
     */
    private static final ConcurrentHashMap<String, Object> LOCAL_HOLDER = new ConcurrentHashMap<>();


    /**
     * <p>存储一个数据</p>
     * <p>存储的时的key值默认为<code>value.getClass().getName()</code></p>
     *
     * @param value 待存储的数据
     */
    public static void put(Object value) {
        if (null == value) {
            return;
        }
        put(value.getClass().getName(), value);
    }

    /**
     * <p>存储一个数据</p>
     *
     * @param key   待存储的数据的key
     * @param value 待存储的数据
     */
    public synchronized static void put(String key, Object value) {
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
    public synchronized static Object get(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return LOCAL_HOLDER.get(key.trim());
    }

    /**
     * 根据指定的key获取对应的值，若key不存在时则调用supplier
     *
     * @param key      指定的key
     * @param supplier Supplier
     * @param <T>      数据类型
     * @return 获取的值
     */
    @SuppressWarnings("unchecked")
    public synchronized static <T> T get(String key, Supplier<T> supplier) {
        Object value = get(key.trim());
        if (null != value) {
            try {
                return (T) value;
            } catch (Exception e) {
            }
        }
        T val = null == supplier ? null : supplier.get();
        if (null == val) {
            return null;
        }
        put(key.trim(), val);
        return val;
    }


    /**
     * <p>根据数据的key获取数据</p>
     * <p>此方式下默认key为<code>clazz.getName()</code></p>
     *
     * @param clazz 数据的类型Class
     * @param <T>   数据的类型
     * @return 获取到的存储数据
     */
    public static <T> T get(Class<T> clazz) {
        if (null == clazz) {
            return null;
        }
        try {
            return (T) get(clazz.getName());
        } catch (Exception e) {
        }
        return null;

    }


    /**
     * 移除存储的数据
     *
     * @param key 待移除的数据的key
     */
    public synchronized static void remove(String key) {
        if (StringUtils.isBlank(key)) {
            return;
        }
        LOCAL_HOLDER.remove(key);
    }

    /**
     * <p>移除存储的数据</p>
     * <p>此方式下默认key为<code>clazz.getName()</code></p>
     *
     * @param <T>   数据的类型
     * @param clazz 待移除的数据的key
     */
    public static <T> void remove(Class<T> clazz) {
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
    public static Set<String> keys() {
        return LOCAL_HOLDER.keySet();
    }

    /**
     * 所有存储的数据的key中是否包含指定的key
     *
     * @param key 指定的key
     * @return 包含返回为true, 否则为false
     */
    public static boolean containsKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }
        return LOCAL_HOLDER.containsKey(key);
    }

    /**
     * 清空所有存储的数据
     */
    public synchronized static void clear() {
        LOCAL_HOLDER.clear();
    }
}
