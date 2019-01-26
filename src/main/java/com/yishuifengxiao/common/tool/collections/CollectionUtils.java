/**
 * 
 */
package com.yishuifengxiao.common.tool.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集合工具类
 * 
 * @author yishui
 * @date 2018年10月17日
 * @Version 0.0.1
 */
public final class CollectionUtils {

	/**
	 * 将数组转为list
	 * 
	 * @param objs 需要转换的数组
	 * @return 转换后的list
	 */
	public static <T> List<T> toList(T[] objs) {
		if (EmptyUtil.isEmpty(objs)) {
			return null;
		}
		List<T> list = new ArrayList<>();
		for (T t : objs) {
			list.add(t);
		}
		return list;
	}

	/**
	 * 将数组转换为set
	 * 
	 * @param objs 需要转换的数组
	 * @return 转换后的set
	 */
	public static <T> Set<T> toSet(T[] objs) {
		if (EmptyUtil.isEmpty(objs)) {
			return null;
		}
		Set<T> set = new HashSet<>();
		for (T t : objs) {
			set.add(t);
		}
		return set;
	}

}
