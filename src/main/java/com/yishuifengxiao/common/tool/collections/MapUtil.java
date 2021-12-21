package com.yishuifengxiao.common.tool.collections;

import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;

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
	private final Map<String, Object> map = new WeakHashMap<>();

	/**
	 * 获取一个map工具类实例
	 * 
	 * @return map工具类实例
	 */
	public synchronized static MapUtil instance() {
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
			this.map.putAll(map);
		}
		return this;
	}

}
