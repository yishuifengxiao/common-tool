package com.yishuifengxiao.common.tool.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类
 * 
 * @author yishui
 * @date 2019年8月6日
 * @version 1.0.0
 */
public final class RegexUtil {

	/**
	 * 根据正则提取字符串
	 *
	 * @param regex  正则表达式
	 * @param source 内容字符串
	 * @return
	 */
	public static String getMatcher(String regex, String source) {
		String result = "";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) {
			result = matcher.group();
		}
		return result;
	}
}
