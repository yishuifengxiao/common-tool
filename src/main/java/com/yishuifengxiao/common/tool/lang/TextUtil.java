/**
 *
 */
package com.yishuifengxiao.common.tool.lang;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * <p>
 * 文本工具
 * </p>
 * 该工具主要用于将字符串的首字母进行大小写转换
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TextUtil {

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
	 * @return 若包含则返回为true, 否则为false
	 */
	public static boolean containsAnyIgnoreCase(String text, String... searchStrs) {
		return Arrays.asList(searchStrs).parallelStream().anyMatch(t -> StringUtils.containsIgnoreCase(text, t));
	}

	/**
	 * 判断文本里是否包含指定的字符(区分大小写)
	 *
	 * @param text       原始文本
	 * @param searchStrs 待查找的文本
	 * @return 若包含则返回为true, 否则为false
	 */
	public static boolean containsAny(String text, String... searchStrs) {
		return Arrays.asList(searchStrs).parallelStream().anyMatch(t -> StringUtils.contains(text, t));
	}

	/**
	 * 目标文本里是否包含指定的字符串(忽略大小写)
	 * 
	 * @param searchStr 待匹配的文本
	 * @param strs      原始文本集
	 * @return 若包含则返回为true, 否则为false
	 */
	public static boolean containsIgnoreCaseInAny(String searchStr, String... strs) {
		return Arrays.asList(strs).parallelStream().anyMatch(t -> StringUtils.containsIgnoreCase(t, searchStr));
	}

	/**
	 * 目标文本里是否包含指定的字符串(区分大小写)
	 * 
	 * @param searchStr 待匹配的文本
	 * @param strs      原始文本集
	 * @return 若包含则返回为true, 否则为false
	 */
	public static boolean containsInAny(String searchStr, String... strs) {
		return Arrays.asList(strs).parallelStream().anyMatch(t -> StringUtils.contains(t, searchStr));
	}

	/**
	 * 转换为下划线,例如:将CallBack转换成 call_back
	 * 
	 * @param camelCaseName 输入的数据
	 * @return 转换后的数据
	 */
	public static synchronized String underscoreName(String camelCaseName) {
		StringBuilder result = new StringBuilder();
		if (camelCaseName != null && camelCaseName.length() > 0) {
			result.append(camelCaseName.substring(0, 1).toLowerCase());
			for (int i = 1; i < camelCaseName.length(); i++) {
				char ch = camelCaseName.charAt(i);
				if (Character.isUpperCase(ch)) {
					result.append("_");
					result.append(Character.toLowerCase(ch));
				} else {
					result.append(ch);
				}
			}
		}
		return result.toString();
	}

	/**
	 * 转换为驼峰,例如:将call_back转换成 CallBack
	 * 
	 * @param underscoreName 输入的数据
	 * @return 转换后的数据
	 */
	public static synchronized String camelCaseName(String underscoreName) {
		StringBuilder result = new StringBuilder();
		if (underscoreName != null && underscoreName.length() > 0) {
			boolean flag = false;
			for (int i = 0; i < underscoreName.length(); i++) {
				char ch = underscoreName.charAt(i);
				if ("_".charAt(0) == ch) {
					flag = true;
				} else {
					if (flag) {
						result.append(Character.toUpperCase(ch));
						flag = false;
					} else {
						result.append(ch);
					}
				}
			}
		}
		return result.toString();
	}

}
