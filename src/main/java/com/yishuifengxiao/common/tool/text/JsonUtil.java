/**
 * 
 */
package com.yishuifengxiao.common.tool.text;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

/**
 * json转换工具类
 * 
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class JsonUtil {

	/**
	 * 将java对象转换成json对象
	 * 
	 * @param data 待转换的数据
	 * @return 转换后的json对象，若转换失败则返回null
	 */
	public final static JSONObject toJSON(Object data) {
		if (null == data) {
			return null;
		}
		try {
			return JSONObject.parseObject(JSONObject.toJSONString(data));
		} catch (Exception e) {
			log.info("将对象 {} 转换成json对象时出现问题 {} ", data, e.getMessage());
		}
		return null;
	}

	/**
	 * 将json格式的字符串转换为json对象
	 * 
	 * @param jsonStr 待转换的数据
	 * @return 转换后的json对象，若转换失败则返回null
	 */
	public final static JSONObject str2JSONObject(String jsonStr) {
		if (StringUtils.isBlank(jsonStr)) {
			return null;
		}
		try {
			return JSONObject.parseObject(jsonStr.trim());
		} catch (Exception e) {
			log.info("将字符串 {} 转换成json对象时出现问题 {} ", jsonStr, e.getMessage());
		}
		return null;
	}

	/**
	 * 将json格式的字符串转换为json数组
	 * 
	 * @param jsonStr 待转换的数据
	 * @return 转换后的json数组，若转换失败则返回null
	 */
	public final static JSONArray str2JSONArray(String jsonStr) {
		if (StringUtils.isBlank(jsonStr)) {
			return null;
		}
		try {
			return JSONArray.parseArray(jsonStr.trim());
		} catch (Exception e) {
			log.info("将字符串 {} 转换成json数组时出现问题 {} ", jsonStr, e.getMessage());
		}
		return null;
	}

	/**
	 * 将json格式的字符串转换为map对象
	 * 
	 * @param text 待转换的数据
	 * @return 转换后的map对象，若转换失败则返回null
	 */
	public final static Map<String, Object> json2Map(String text) {
		if (StringUtils.isBlank(text)) {
			return null;
		}
		try {
			return JSONObject.parseObject(text.trim()).getInnerMap();
		} catch (Exception e) {
			log.info("将字符串 {} 转换成 map 时出现问题 {} ", text, e.getMessage());
		}
		return null;
	}

}
