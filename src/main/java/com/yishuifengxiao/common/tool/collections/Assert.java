package com.yishuifengxiao.common.tool.collections;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.exception.ValidateException;

/**
 * 集合断言工具类
 * 
 * @author yishui
 * @date 2018年10月17日
 * @Version 0.0.1
 */
public class Assert {

	/**
	 * 判断传入的参数是否为空，若有传入的参数有一个不为空就抛出异常
	 * 
	 * @param msg    提示信息
	 * @param values 需要判断的数据
	 * @throws ValidateException
	 */
	public static void assertBlank(String msg, Object... values) throws ValidateException {
		if (EmptyUtil.notEmpty(values)) {
			for (Object value : values) {
				if (null != value) {
					if (value instanceof String) {
						if (StringUtils.isNotBlank(value.toString())) {
							new ValidateException(msg);
						}
					} else {
						new ValidateException(msg);
					}
				}
			}
		}
	}

	/**
	 * 判断传入的参数是否不为空，若有传入的参数有一个为空就抛出异常
	 * 
	 * @param msg    提示信息
	 * @param values 需要判断的数据
	 * @throws ValidateException
	 */
	public static void assertNotBlank(String msg, Object... values) throws ValidateException {
		if (EmptyUtil.notEmpty(values)) {
			for (Object value : values) {
				if (null == value) {
					new ValidateException(msg);
				} else {
					if (StringUtils.isBlank(value.toString())) {
						new ValidateException(msg);
					}

				}
			}
		}

	}

	/**
	 * 对象是否为null(或空字符串)，若不为空(或空字符串)则抛出异常
	 * 
	 * @param value 需要判断的数据
	 * @param msg   提示信息
	 * @throws ValidateException
	 */
	public static void assertNull(Object value, String msg) throws ValidateException {
		if (null != value) {
			if (value instanceof String) {
				if (StringUtils.isNotBlank(value.toString())) {
					new ValidateException(msg);
				}
			} else {
				new ValidateException(msg);
			}
		}
	}

	/**
	 * 对象是否不为null(或空字符串)，若为空(或空字符串)则抛出异常
	 * 
	 * @param value 需要判断的数据
	 * @param msg   提示信息
	 * @throws ValidateException
	 */
	public static void assertNotNull(Object value, String msg) throws ValidateException {
		if (null == value || StringUtils.isBlank("" + value)) {
			new ValidateException(msg);
		}
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
		if (EmptyUtil.notEmpty(page)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.isEmpty(page)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.notEmpty(page)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.isEmpty(page)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.notEmpty(data)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.isEmpty(data)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.notEmpty(data)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.isEmpty(data)) {
			throw new ValidateException(msg);
		}
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
		if (EmptyUtil.isEmpty(data)) {
			throw new ValidateException(msg);
		}
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
		if (!EmptyUtil.onlyOneElement(data)) {
			throw new ValidateException(msg);
		}
	}

	/**
	 * 判断集合是否不是只有一个元素，若只有一个元素就抛出异常
	 * 
	 * @param <T>
	 * @param data 集合
	 * @param msg  异常提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNotOnlyOne(Collection<T> data, String msg) throws ValidateException {
		if (!EmptyUtil.notOnlyOneElement(data)) {
			throw new ValidateException(msg);
		}
	}

}
