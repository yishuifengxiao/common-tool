/**
 * 
 */
package com.yishuifengxiao.common.tool.collections;

import java.util.Collection;

import com.yishuifengxiao.common.tool.entity.Page;

/**
 * <p>
 * 空集合判断工具
 * </p>
 * 
 * 该工具的主要目标是快速地判断集合是否为空，其具备以下的几项功能
 * <ol>
 * <li>判断集合是否为空或者空元素的集合</li>
 * <li>判断集合是否仅有一个元素</li>
 * </ol>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class EmptyUtil {

	/**
	 * 判断分页对象是否为空
	 * 
	 * @param <T>  分页对象里元素的数据类型
	 * @param page 分页对象
	 * @return 如果是空返回为true，否则为false
	 */
	public static <T> boolean isEmpty(Page<T> page) {

		if (page == null) {
			return true;
		}
		if (page.getData() == null || page.getData().size() == 0) {
			return true;
		}
		return false;

	}

	/**
	 * 判断分页对象是否不为空
	 * 
	 * @param <T>  分页对象里元素的数据类型
	 * @param page 分页对象
	 * @return 如果不是空返回为true，否则为false
	 */
	public static <T> boolean notEmpty(Page<T> page) {
		return !isEmpty(page);
	}

	/**
	 * 判断分页对象是否为空
	 * 
	 * @param <T>  分页对象里元素的数据类型
	 * @param page 分页对象
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
	 * 判断分页对象是否不为空
	 * 
	 * @param <T>  分页对象里元素的数据类型
	 * @param page 分页对象
	 * @return 如果不是空返回为true，否则为false
	 */
	public static <T> boolean notEmpty(org.springframework.data.domain.Page<T> page) {
		return !isEmpty(page);
	}

	/**
	 * 判断数组是否为空
	 * 
	 * @param <T>  数组里元素的类型
	 * @param data 数据源
	 * @return 如果为空则返回true，否则为false
	 */
	public static <T> boolean isEmpty(T[] data) {
		return data == null || data.length == 0;
	}

	/**
	 * 判断数组是否不为空
	 * 
	 * @param <T>  数组里元素的类型
	 * @param data 数据源
	 * @return 如果不为空则返回true，否则为false
	 */
	public static <T> boolean notEmpty(T[] data) {
		return !isEmpty(data);
	}

	/**
	 * 判断集合是否为空
	 * 
	 * @param <T>  集合里元素的类型
	 * @param data 数据源
	 * @return 如果为空则返回true，否则为false
	 */
	public static <T> boolean isEmpty(Collection<T> data) {
		if (data == null) {
			return true;
		}
		if (data.size() == 0) {
			return true;
		}
		if (data.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * 判断集合是否不为空
	 * 
	 * @param <T>  集合里元素的类型
	 * @param data 数据源
	 * @return 如果不为空则返回true，否则为false
	 */
	public static <T> boolean notEmpty(Collection<T> data) {
		return !isEmpty(data);
	}

	/**
	 * 判断 集合是否只有一个元素
	 * 
	 * @param <T>  集合里元素的类型
	 * @param data 数据源
	 * @return 如果只有一个元素返回为true，否则为false
	 */
	public static <T> boolean onlyOneElement(Collection<T> data) {
		if (null == data) {
			return false;
		}
		if (data.isEmpty()) {
			return false;
		}
		return data.size() == 1;
	}

	/**
	 * 判断集合里的元素的数量是否大于或等于一个
	 * 
	 * @param <T>  集合里元素的类型
	 * @param data 数据源
	 * @return 如果元素的数量是否大于或等于一个返回为true，否则为false
	 */
	public static <T> boolean gteOneElement(Collection<T> data) {
		if (null == data) {
			return false;
		}
		return data.size() >= 1;
	}

	/**
	 * 判断集合里元素数量是否大于一个
	 * 
	 * @param <T>  集合里元素的类型
	 * @param data 数据源
	 * @return 若集合里元素数量大于1则返回为true,否则为false
	 */
	public static <T> boolean gtOneElement(Collection<T> data) {
		if (null == data) {
			return false;
		}
		return data.size() > 1;
	}
}
