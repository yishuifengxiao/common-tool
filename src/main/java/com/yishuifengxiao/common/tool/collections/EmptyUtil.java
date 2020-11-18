/**
 * 
 */
package com.yishuifengxiao.common.tool.collections;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.exception.ValidateException;

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
	 * @param pages 分页对象
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
	 * 判断分页对象是否为空，若不为空则抛出异常
	 * 
	 * @param <T>
	 * @param pages 分页对象
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertEmpty(Page<T> page, String msg) throws ValidateException {
		if (!isEmpty(page)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断是否为一个非空的分页对象
	 * 
	 * @param pages 分页对象
	 * @return 如果是空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(Page<T> pages) {
		return !isEmpty(pages);
	}

	/**
	 * 判断分页对象是否不为空，若为空则抛出异常
	 * 
	 * @param <T>
	 * @param pages 分页对象
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNoEmpty(Page<T> page, String msg) throws ValidateException {
		if (!notEmpty(page)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断是否为一个空的分页对象
	 * 
	 * @param pages 分页对象
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
	 * 判断分页对象是否为空，若不为空则抛出异常
	 * 
	 * @param <T>
	 * @param pages 分页对象
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertEmpty(org.springframework.data.domain.Page<T> page, String msg)
			throws ValidateException {
		if (!isEmpty(page)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断是否为一个非空的分页对象
	 * 
	 * @param pages 分页对象
	 * @return 如果是空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(org.springframework.data.domain.Page<T> page) {
		return !isEmpty(page);
	}

	/**
	 * 判断分页对象是否不为空，若为空则抛出异常
	 * 
	 * @param <T>
	 * @param pages 分页对象
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNoEmpty(org.springframework.data.domain.Page<T> page, String msg)
			throws ValidateException {
		if (!notEmpty(page)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断数组是否为空
	 * 
	 * @param data 需要判断的数组
	 * @return 如果为空则返回true
	 */
	public static <T> boolean isEmpty(T[] data) {
		return data == null || data.length == 0;
	}

	/**
	 * 判断数组是否为空，若不为空则抛出异常
	 * 
	 * @param <T>
	 * @param data 数组数据
	 * @param msg  异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertEmpty(T[] data, String msg) throws ValidateException {
		if (!isEmpty(data)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断数组数组不是空
	 * 
	 * @param data 需要判断的数组
	 * @return 如果不为空则返回true
	 */
	public static <T> boolean notEmpty(T[] data) {
		return !isEmpty(data);
	}

	/**
	 * 判断数组是否不为空，若为空则抛出异常
	 * 
	 * @param <T>
	 * @param pages 数组数据
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNoEmpty(T[] data, String msg) throws ValidateException {
		if (!notEmpty(data)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断set是否为空
	 * 
	 * @param data 需要判断的数组
	 * @return 如果为空则返回为true
	 */
	public static <T> boolean isEmpty(Set<T> data) {
		return data == null || data.size() == 0;
	}

	/**
	 * 判断set是否为空，若不为空则抛出异常
	 * 
	 * @param <T>
	 * @param data set数据
	 * @param msg  异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertEmpty(Set<T> data, String msg) throws ValidateException {
		if (!isEmpty(data)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断set是否不为空
	 * 
	 * @param data 需要判断的数组
	 * @return 如果不为空则返回为true
	 */
	public static <T> boolean notEmpty(Set<T> data) {
		return !isEmpty(data);
	}

	/**
	 * 判断Set是否不为空，若为空则抛出异常
	 * 
	 * @param <T>
	 * @param pages Set数据
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNoEmpty(Set<T> data, String msg) throws ValidateException {
		if (!notEmpty(data)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断是否为空的列表
	 * 
	 * @param list 列表
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
	 * 判断List是否为空，若不为空则抛出异常
	 * 
	 * @param <T>
	 * @param data list数据
	 * @param msg  异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertEmpty(List<T> list, String msg) throws ValidateException {
		if (!isEmpty(list)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断是否为空的列表
	 * 
	 * @param list 列表
	 * @return 如果为空返回为false，否则为true
	 */
	public static <T> boolean notEmpty(List<T> list) {
		return !isEmpty(list);
	}

	/**
	 * 判断List是否不为空，若为空则抛出异常
	 * 
	 * @param <T>
	 * @param pages List数据
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNoEmpty(List<T> data, String msg) throws ValidateException {
		if (!notEmpty(data)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断 集合是否只有一个元素
	 * 
	 * @param data 列表
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
	 * 判断集合里是否只有一个元素，若不是只有一个元素则抛出异常
	 * 
	 * @param <T>
	 * @param data 集合
	 * @param msg  异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertOnlyOne(Collection<T> data, String msg) throws ValidateException {
		if (!onlyOneElement(data)) {
			throw new ValidateException(msg);
		}
	}

}
