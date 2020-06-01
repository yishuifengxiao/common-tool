/**
 * 
 */
package com.yishuifengxiao.common.tool.utils;

/**
 * 字符工具类
 * 
 * @author yishui
 * @Date 2019年3月8日
 * @version 1.0.0
 */
public class StringUtil {

	/**
	 * 将字符串的首字母变为小写的
	 * 
	 * @param s
	 *            字符串
	 * @return
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
	 * @param s
	 *            字符串
	 * @return
	 */
	public static String toUpperCaseFirstOne(String s) {
		if (Character.isUpperCase(s.charAt(0))) {
			return s;
		} else {
			return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
		}
	}

}
