/**
 * 
 */
package com.yishuifengxiao.common.tool.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 集合工具类
 * 
 * @author yishui
 * @date 2018年10月17日
 * @Version 0.0.1
 */
public final class CollectionUtil {

	/**
	 * 将数组转为list
	 * 
	 * @param objs 需要转换的数组
	 * @return 转换后的list
	 */
	public static <T> List<T> toList(T[] objs) {
		if (EmptyUtil.isEmpty(objs)) {
			return new ArrayList<>();
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
			return new HashSet<>();
		}
		Set<T> set = new HashSet<>();
		for (T t : objs) {
			set.add(t);
		}
		return set;
	}

	/**
	 * 取出链表的第一个元素
	 * 
	 * @param list
	 * @return
	 */
	public static <T> T first(List<T> data) {
		if (data == null || data.size() == 0) {
			return null;
		}
		return data.get(0);
	}

	/**
	 * 获取set集合的第一个元素
	 * 
	 * @param data
	 * @return
	 */
	public static <T> T first(Set<T> data) {
		if (data == null || data.size() == 0) {
			return null;
		}
		Iterator<T> it = data.iterator();
		return it.next();
	}

	/**
	 * 取出数组的第一个元素
	 * 
	 * @param data
	 * @return
	 */
	public static <T> T first(T[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		return data[0];
	}

	/**
	 * 取出集合的第一个元素
	 * 
	 * @param data
	 * @return
	 */
	public static <T> T first(Collection<T> data) {
		if (data == null || data.size() == 0) {
			return null;
		}
		Iterator<T> it = data.iterator();
		return it.next();
	}

}
