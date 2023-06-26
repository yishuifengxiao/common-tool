package com.yishuifengxiao.common.tool.collections;

import java.util.Map;
import java.util.TreeMap;

/**
 * <p>
 * map生成工具
 * <p>
 * 该工具的主要目的是采用链式接口快速生成一个map对象
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class MapUtil<K, V> {

    /**
     * 数据存储对象
     */
    private final Map<K, V> map = new TreeMap<>();

    /**
     * 获取一个map工具类实例
     *
     * @return map工具类实例
     */
    @SuppressWarnings("rawtypes")
    public static MapUtil map() {
        return new MapUtil();
    }


    /**
     * 输出最终需要的map数据
     *
     * @param <K1> Key的数据类型
     * @param <V1> 值得数据类型
     * @return 最终需要的map数据
     */
    @SuppressWarnings("rawtypes")
    public <K1 extends K, V1 extends V> Map build() {
        return this.map;
    }

    /**
     * <p>
     * 增加一个键值对数据
     * </p>
     * 如果增加的键值对的键为空，则不会增加该条记录
     *
     * @param key   数据的键
     * @param value 数据的值
     * @return 当前map工具类实例
     */
    public MapUtil<K, V> put(K key, V value) {
        if (null != key && null != value) {
            this.map.put(key, value);
        }
        return this;
    }


    /**
     * 批量添加一组键值对
     *
     * @param map 键值对
     * @return 当前map工具类实例
     */
    public MapUtil<K, V> putAll(Map<K, V> map) {
        if (null != map) {
            map.forEach((k, v) -> put(k, v));
        }
        return this;
    }


}
