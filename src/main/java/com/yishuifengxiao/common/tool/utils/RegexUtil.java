package com.yishuifengxiao.common.tool.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 正则工具
 * </p>
 * 该工具主要是利用正则对字符串进行判断，主要功能如下：
 * <ol>
 * <li>判断给定的字符串是否包含中文</li>
 * <li>判断给定的字符串是否符合给定的正则表达式</li>
 * </ol>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RegexUtil {
	/**
	 * 包含中文的正则
	 */
	private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]+");

	/**
	 * 存储正则Pattern的集合 key ：正则表达式 value ：Pattern对象
	 */
	private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();

	/**
	 * 根据正则表达式获取Pattern对象
	 * 
	 * @param regex 正则表达式
	 * @return Pattern对象
	 */
	public static Pattern pattern(String regex) {
		if (StringUtils.isBlank(regex)) {
			throw new RuntimeException("正则表达式不能为空");
		}
		regex = regex.trim();
		Pattern pattern = PATTERN_CACHE.get(regex);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			PATTERN_CACHE.put(regex, pattern);
		}
		return pattern;
	}

	/**
	 * 判断内容是否符合正则表达式
	 * 
	 * @param regex   正则表达式
	 * @param str 待判断的内容
	 * @return 若匹配则返回为true,否则为false
	 */
	public static boolean match(String regex, String str) {
		Pattern pattern = pattern(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	/**
	 * 判断内容是否包正则表达式标识的内容
	 * 
	 * @param regex   正则表达式
	 * @param str 待判断的内容
	 * @return 若匹配则返回为true,否则为false
	 */
	public static boolean find(String regex, String str) {
		Pattern pattern = pattern(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.find();
	}

	/**
	 * 根据正则表达式从内容中提取出一组匹配的内容<br/>
	 * <b>只要匹配到第一组数据就会返回</b>
	 * 
	 * @param regex   正则表达式
	 * @param str 目标内容
	 * @return 提取出一组匹配的内容
	 */
	public static String extract(String regex, String str) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		String result = null;
		Pattern pattern = pattern(regex);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			result = matcher.group();
		}
		return result;
	}

	/**
	 * 根据正则表达式从内容中提取出所有匹配的内容<br/>
	 * <b>返回所有匹配的数据</b>
	 * 
	 * @param regex   正则表达式
	 * @param str 目标内容
	 * @return 取出所有匹配的内容
	 */
	public static List<String> extractAll(String regex, String str) {
		if (StringUtils.isBlank(str)) {
			return new ArrayList<>();
		}
		List<String> list = new ArrayList<>();
		Pattern pattern = pattern(regex);
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			list.add(matcher.group());
		}
		return list;
	}

	/**
	 * 如果包含汉字则返回为true
	 * 
	 * @param str 需要判断的字符串
	 * @return 如果包含汉字则返回为true，否则为false
	 */
	public static boolean containChinese(String str) {
		return CHINESE_PATTERN.matcher(str).matches();
	}
}
