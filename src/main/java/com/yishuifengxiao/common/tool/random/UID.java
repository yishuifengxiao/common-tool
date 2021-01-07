/**
 * 
 */
package com.yishuifengxiao.common.tool.random;

import java.util.UUID;

/**
 * <p>
 * UUID生成工具
 * </p>
 * 根据UUID生成一个不包含-的随机字符串
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class UID {
	/**
	 * 生成UUID
	 * 
	 * @return UUID
	 */
	public static synchronized String uuid() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
