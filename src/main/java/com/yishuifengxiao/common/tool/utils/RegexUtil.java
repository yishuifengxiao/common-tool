package com.yishuifengxiao.common.tool.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 正则工具
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RegexUtil {
	/**
	 * 包含中文的正则
	 */
	private static final  Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]+");

	/**
	 * 如果包含汉字则返回为true
	 * 
	 * @param str 需要判断的字符串
	 * @return 如果包含汉字则返回为true，否则为false
	 */
	public static boolean containChinese(String str) {
		return CHINESE_PATTERN.matcher(str).matches();
	}

	/**
	 * 根据正则提取字符串
	 *
	 * @param regex  正则表达式
	 * @param source 内容字符串
	 * @return 提取出来的字符串
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
