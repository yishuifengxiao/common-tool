/**
 * 
 */
package com.yishuifengxiao.common.tool.collections;

import java.util.List;
import java.util.Set;

import com.github.pagehelper.PageInfo;
import com.yishuifengxiao.common.tool.entity.Page;

/**
 * 空集合判断类
 * 
 * @author yishui
 * @date 2018年12月11日
 * @Version 0.0.1
 */
public final class EmptyUtil {


	/**
	 * 判断是否为一个空的分页对象
	 * 
	 * @param pages
	 *            分页对象
	 * @return 如果是空返回为true，否则为false
	 */
	public static <T> boolean isEmpty(PageInfo<T> pages) {
		if (pages == null) {
			return true;
		}
		if (pages.getList() == null || pages.getList().size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为一个非空的分页对象
	 * 
	 * @param pages
	 *            分页对象
	 * @return 如果是空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(PageInfo<T> pages) {
		return !isEmpty(pages);
	}

	/**
	 * 判断是否为一个空的分页对象
	 * 
	 * @param pages
	 *            分页对象
	 * @return 如果是空返回为true，否则为false
	 */
	public static <T> boolean isEmpty(Page<T> pages) {

		if (pages == null) {
			return true;
		}
		if (pages.getData() == null || pages.getData().size() == 0) {
			return true;
		}
		return false;

	}

	/**
	 * 判断是否为一个非空的分页对象
	 * 
	 * @param pages
	 *            分页对象
	 * @return 如果是空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(Page<T> pages) {
		return !isEmpty(pages);
	}

	/**
	 * 判断是否为一个空的分页对象
	 * 
	 * @param pages
	 *            分页对象
	 * @return 如果是空返回为true，否则为false
	 */
	public static <T> boolean isEmpty(org.springframework.data.domain.Page<T> page) {

		if (page == null) {
			return true;
		}
		if (page.getContent() == null || page.getContent().size() == 0) {
			return true;
		}
		return false;

	}

	/**
	 * 判断是否为一个非空的分页对象
	 * 
	 * @param pages
	 *            分页对象
	 * @return 如果是空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(org.springframework.data.domain.Page<T> page) {
		return !isEmpty(page);
	}

	/**
	 * 判断数组是否为空
	 * 
	 * @param data
	 *            需要判断的数组
	 * @return 如果为空则返回true
	 */
	public static <T> boolean isEmpty(T[] data) {
		return data == null || data.length == 0;
	}

	/**
	 * 判断数组数组不是空
	 * 
	 * @param data
	 *            需要判断的数组
	 * @return 如果不为空则返回true
	 */
	public static <T> boolean notEmpty(T[] data) {
		return !isEmpty(data);
	}

	/**
	 * 判断set是否为空
	 * 
	 * @param data
	 *            需要判断的数组
	 * @return 如果为空则返回为true
	 */
	public static <T> boolean isEmpty(Set<T> data) {
		return data == null || data.size() == 0;
	}

	/**
	 * 判断set是否不为空
	 * 
	 * @param data
	 *            需要判断的数组
	 * @return 如果不为空则返回为true
	 */
	public static <T> boolean notEmpty(Set<T> data) {
		return !isEmpty(data);
	}

	/**
	 * 判断是否为空的列表
	 * 
	 * @param list
	 *            列表
	 * @return 如果为空返回为true，否则为false
	 */
	public static <T> boolean isEmpty(List<T> list) {
		if (list == null) {
			return true;
		}
		if (list.size() == 0) {
			return true;
		}
		return false;
	}

	/**
	 * 判断是否为空的列表
	 * 
	 * @param list
	 *            列表
	 * @return 如果为空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(List<T> list) {
		return !isEmpty(list);
	}

	/**
	 * 判断 列表是否只有一个元素
	 * 
	 * @param list
	 *            列表
	 * @return 如果只有一个元素返回为true，否则为false
	 */
	public static <T> boolean onlyOneElement(List<T> list) {
		if (notEmpty(list)) {
			if (list.size() == 1) {
				return true;
			}
		}
		return false;
	}

}
