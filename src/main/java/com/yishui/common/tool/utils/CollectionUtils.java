/**
 * 
 */
package com.yishui.common.tool.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 集合工具类
 * 
 * @author yishui
 * @date 2018年10月17日
 * @Version 0.0.1
 */
public class CollectionUtils {

	/**
	 * 将数组转为list
	 * 
	 * @param objs
	 *            需要转换的数组
	 * @return 转换后的list
	 */
	public static <T> List<T> toList(T[] objs) {
		List<T> list = new ArrayList<>();
		for (T t : objs) {
			list.add(t);
		}
		return list;
	}


}
