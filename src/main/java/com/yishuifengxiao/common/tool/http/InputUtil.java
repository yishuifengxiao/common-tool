package com.yishuifengxiao.common.tool.http;

import org.apache.commons.lang3.StringUtils;

/**
 * 输入参数处理工具类
 * 
 * @author yishui
 * @date 2019年11月13日
 * @version 1.0.0
 */
public final class InputUtil {

	/**
	 * 空字符串
	 */
	private static final String EMPTY_STRING = "";

	/**
	 * undefined字符串
	 */
	private static final String UNDEFINED_STRING = "undefined";

	/**
	 * 对传入的参数进行非空处理 <br/>
	 * 当传入的参数为 null 或 "" 或 "undefined" 直接返回为null,否则返回原始值
	 * 
	 * @param t 传入的参数
	 * @return 处理后的参数
	 */
	public static <T> T convert(T t) {
		return t == null || EMPTY_STRING.equals(t) || UNDEFINED_STRING.equals(t) ? null : t;
	}

	/**
	 * 对字符串进行非空和空格处理
	 * 
	 * @param str 传入的参数
	 * @return 处理后的参数
	 */
	public static String trim(String str) {
		return StringUtils.isNotBlank(str) ? str.trim() : null;
	}

	/**
	 * 对参数进行非空和空格处理，并对undefined值的数据进行过滤
	 * 
	 * @param str
	 * @return
	 */
	public static String undefined(String str) {
		str = trim(str);
		return StringUtils.equalsIgnoreCase(str, UNDEFINED_STRING) ? null : str;
	}




}
