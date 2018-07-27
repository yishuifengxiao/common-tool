/**
 * 
 */
package com.yishui.common.tool.utils;

import java.util.UUID;

/**
 * UUID工具类
 * 
 * @author yishui
 * @version 0.0.1
 * @date 2018年7月27日
 */
public class UID {
	/**
	 * 生成UUID
	 * 
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
