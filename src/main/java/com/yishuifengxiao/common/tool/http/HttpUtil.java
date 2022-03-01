package com.yishuifengxiao.common.tool.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * http数据工具
 * 
 * @author yishui
 * @version 1.0.0
 * @date 2022/2/27
 */
public class HttpUtil {

	/**
	 * 分隔符
	 */
	private final static String SEPARATOR = "&";

	/**
	 * <p>
	 * 将查询字符串转为map结构
	 * </p>
	 * <p>
	 * 字符串形式为 timeOption=0&page=1&pageSize=10&keyPlace=1&sort=dateDesc&qt=*
	 * </p>
	 *
	 * @param queryString 待转换的查询参数字符串
	 * @return 转换后的map数据
	 */
	public static Map<String, Object> queryString2Map(String queryString) {
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.isBlank(queryString)) {
			return null;
		}
		String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(queryString, SEPARATOR);
		if (null == tokens) {
			return null;
		}
		for (String token : tokens) {
			String[] strings = token.split("=");
			if (null == strings || strings.length < 2) {
				continue;
			}
			if (StringUtils.isBlank(strings[0])) {
				continue;
			}
			map.put(strings[0].trim(), strings[1]);
		}
		return map;
	}
}
