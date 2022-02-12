/**
 * 
 */
package com.yishuifengxiao.common.tool.lang;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 字符串工具
 * </p>
 * 该工具主要用于将字符串的首字母进行大小写转换
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class StringUtil {

	/**
	 * 将字符串的首字母变为小写的
	 * 
	 * @param s 字符串
	 * @return 转换之后的字符串
	 */
	public static String toLowerCaseFirstOne(String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			return s;
		}
		return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();

	}

	/**
	 * 将字符串的首字母变为大写的
	 * 
	 * @param s 字符串
	 * @return 转换之后的字符串
	 */
	public static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0))) {
			return s;
		}
		return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();

	}

	/**
	 * 判断文本里是否包含指定的字符(不区分大小写)
	 * 
	 * @param text       原始文本
	 * @param searchStrs 待查找的文本
	 * @return 若包含则返回为true,否则为false
	 */
	public static boolean containsAnyIgnoreCase(String text, String... searchStrs) {
		return Arrays.asList(searchStrs).parallelStream().anyMatch(t -> StringUtils.containsIgnoreCase(text, t));
	}

}
