package com.yishuifengxiao.common.tool.utils;

/**
 * 布尔类型工具类
 * 
 * @author yishui
 * @Date 2019年4月26日
 * @version 1.0.0
 */
public class BoolUtil {

	/**
	 * 判断是否为true
	 * 
	 * @param data
	 *            需要判断的对象
	 * @return
	 */
	public final static boolean isTrue(Boolean data) {
		if (data == null) {
			return false;
		}
		return data == true ? true : false;
	}

	/**
	 * 判断是否不是true
	 * 
	 * @param data
	 * @return
	 */
	public final static boolean notTrue(Boolean data) {
		if (data == null) {
			return true;
		}
		return data == false ? true : false;
	}

	/**
	 * 判断是否为false
	 * 
	 * @param data
	 * @return
	 */
	public final static boolean isFalse(Boolean data) {
		if (data == null) {
			return false;
		}
		return data == false ? true : false;
	}

	/**
	 * 判断是否不为false
	 * 
	 * @param data
	 * @return
	 */
	public final static boolean notFalse(Boolean data) {
		if (data == null) {
			return true;
		}
		return data == true ? true : false;
	}

	/**
	 * 将0或1对应的转换为false和true，其他的为null
	 * 
	 * @param data
	 *            需要转换的数据
	 * @return
	 */
	public final static Boolean convert(Integer data) {
		if (data == null) {
			return null;
		}
		if (data == 1) {
			return true;
		}
		if (data == 0) {
			return false;
		}
		return null;

	}
}
