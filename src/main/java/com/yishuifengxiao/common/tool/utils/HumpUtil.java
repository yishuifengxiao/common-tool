package com.yishuifengxiao.common.tool.utils;

/**
 * 下划线与驼峰互转工具
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class HumpUtil {

	/**
	 * 转换为下划线
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
	 * 转换为驼峰
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