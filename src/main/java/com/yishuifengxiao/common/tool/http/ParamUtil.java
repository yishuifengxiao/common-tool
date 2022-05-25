package com.yishuifengxiao.common.tool.http;

import org.apache.commons.lang3.StringUtils;

/**
 * 输入参数处理工具
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ParamUtil {

	/**
	 * 空字符串
	 */
	private static final String EMPTY_STRING = "";

	/**
	 * undefined字符串
	 */
	private static final String UNDEFINED_STRING = "undefined";

	/**
	 * <p>
	 * 对传入的参数进行非空处理
	 * </p>
	 * 当传入的参数为 null 或 "" 或 "undefined" 直接返回为null,否则返回原始值
	 * 
	 * @param <T> 传入的参数的数据类型
	 * @param t   传入的参数
	 * @return 处理后的参数
	 */
	public static <T> T convert(T t) {
		return t == null || EMPTY_STRING.equals(t) || UNDEFINED_STRING.equals(t) ? null : t;
	}


	/**
	 * 对参数进行非空和空格处理，并对undefined值的数据进行过滤
	 * 
	 * @param str 传入的参数
	 * @return 处理后的参数
	 */
	public static String undefined(String str) {
		str = StringUtils.trim(str);
		return StringUtils.equalsIgnoreCase(str, UNDEFINED_STRING) ? null : str;
	}

}
