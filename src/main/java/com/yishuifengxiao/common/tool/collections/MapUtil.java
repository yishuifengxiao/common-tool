package com.yishuifengxiao.common.tool.collections;

import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

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
public final class MapUtil {

    /**
     * 数据存储对象
     */
    private final Map<String, Object> map = new HashMap<>();

    /**
     * 获取一个map工具类实例
     *
     * @return map工具类实例
     */
    public synchronized static MapUtil map() {
        return new MapUtil();
    }

    /**
     * 输出最终需要的map数据
     *
     * @return 最终需要的map数据
     */
    public Map<String, Object> build() {

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
    public MapUtil put(String key, Object value) {
        if (StringUtils.isNotBlank(key)) {
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
    public MapUtil putAll(Map<String, Object> map) {
        if (null != map) {
            map.forEach((k, v) -> put(k, v));
        }
        return this;
    }

    /**
     * <p>将源map转换为目标map</p>
     * <p>在转换时map的key保持不变，但是key对应的值发生变化(或者不变)</p>
     *
     * @param map       待转换的map
     * @param converter 数据转换器
     * @return 转换后的目标map
     */
    public static Map convert(@NotNull Map map, @NotNull Converter converter) {
        Map result = new HashMap(map.size());
        map.forEach((k, v) -> result.put(k, converter.convert(v)));
        return result;
    }

    /**
     * 数据转换器
     *
     * @param <S> 输入数据类型
     * @param <R> 输出数据类型
     */
    public interface Converter<S, R> {
        /**
         * 将输入数据转换为输出数据
         *
         * @param s 输入数据
         * @return 转换后的输出数据
         */
        R convert(S s);
    }

}
