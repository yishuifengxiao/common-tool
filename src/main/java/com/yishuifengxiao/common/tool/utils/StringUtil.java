/**
 * 
 */
package com.yishuifengxiao.common.tool.utils;

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
		} else {
			return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
		}
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
		} else {
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
		}
	}

}
