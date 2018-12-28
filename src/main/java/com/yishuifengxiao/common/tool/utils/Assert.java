/**
 * 
 */
package com.yishuifengxiao.common.tool.utils;

/**
 * 断言工具类
 * 
 * @author yishui
 * @date 2018年12月19日
 * @version 0.0.1
 */
public class Assert {
	/**
	 * 断言判断是否存在空元素
	 * 
	 * @param message
	 *            存在空元素时的信息
	 * @param objects
	 *            需要判断的元素
	 */
	public static void hasNoEmpty(String message, Object... objects) {
		for (Object object : objects) {
			org.springframework.util.Assert.notNull(object, message);
		}
	}

}
