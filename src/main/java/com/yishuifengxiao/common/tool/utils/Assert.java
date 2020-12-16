package com.yishuifengxiao.common.tool.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;
import com.yishuifengxiao.common.tool.constant.ErrorCode;
import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.exception.ValidateException;

/**
 * 集合断言工具类
 * 
 * @author yishui
 * @date 2018年10月17日
 * @Version 0.0.1
 */
public final class Assert {

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterEqualZero(Integer value, String msg) throws ValidateException {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterEqualZero(Long value, String msg) throws ValidateException {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterEqualZero(Float value, String msg) throws ValidateException {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterEqualZero(Double value, String msg) throws ValidateException {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterEqualZero(BigDecimal value, String msg) throws ValidateException {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterZero(Integer value, String msg) throws ValidateException {
		if (!NumberUtil.greaterZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterZero(Long value, String msg) throws ValidateException {
		if (!NumberUtil.greaterZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterZero(Float value, String msg) throws ValidateException {
		if (!NumberUtil.greaterZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterZero(Double value, String msg) throws ValidateException {
		if (!NumberUtil.greaterZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param value 需要比较的值
	 * @param msg   异常提示信息
	 * @throws ValidateException
	 */
	public static void assertGreaterZero(BigDecimal value, String msg) throws ValidateException {
		if (!NumberUtil.greaterZero(value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等，如果不相等就抛出异常<br/>
	 * 任何比较值为null则抛出异常
	 * 
	 * @param originalValue 原始值
	 * @param value         比较值
	 * @param msg           异常信息
	 * @throws ValidateException
	 */
	public static void assertEquals(Integer originalValue, Integer value, String msg) throws ValidateException {
		if (!NumberUtil.equals(originalValue, value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等，如果不相等就抛出异常<br/>
	 * 任何比较值为null则抛出异常
	 * 
	 * @param originalValue 原始值
	 * @param value         比较值
	 * @param msg           异常信息
	 * @throws ValidateException
	 */
	public static void assertEquals(Long originalValue, Long value, String msg) throws ValidateException {
		if (!NumberUtil.equals(originalValue, value)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断数据是否为true,如果不是为true则抛出异常
	 * 
	 * @param bool 需要判断的数据
	 * @param msg  提示信息
	 * @throws ValidateException
	 */
	public static void assertTrue(Boolean bool, String msg) throws ValidateException {
		if (!BooleanUtils.isTrue(bool)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判读所有给定的数据为true,如果有一个数据不为true就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param values 需要判断的值
	 * @throws ValidateException
	 */
	public static void assertTrue(String msg, boolean... values) throws ValidateException {
		if (null != values) {
			for (boolean value : values) {
				if (!value) {
					throw new ValidateException(ErrorCode.DATA_ERROR, msg);
				}
			}
		}

	}

	/**
	 * 判读所有给定的数据为false,如果有一个数据不为false就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param values 需要判断的值
	 * @throws ValidateException
	 */
	public static void assertFalse(String msg, boolean... values) throws ValidateException {
		if (null != values) {
			for (boolean value : values) {
				if (value) {
					throw new ValidateException(ErrorCode.DATA_ERROR, msg);
				}
			}
		}

	}

	/**
	 * 判断数据是否为false,如果不是为false则抛出异常
	 * 
	 * @param bool 需要判断的数据
	 * @param msg  提示信息
	 * @throws ValidateException
	 */
	public static void assertFalse(Boolean bool, String msg) throws ValidateException {
		if (!BooleanUtils.isFalse(bool)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否不在指定的数据范围的区间内，如果不在指定的范围内疚抛出异常<br/>
	 * <br/>
	 * 注意：包含边界<br/>
	 * 例如 compareValue=2，startValue=2 ，endValue=8时是正常情况<br/>
	 * 例如 compareValue=1，startValue=2 ，endValue=8时会抛出异常<br/>
	 * 
	 * @param msg          提示信息
	 * @param compareValue 给定的值，如果给定的值为null，则默认替换为0
	 * @param startValue   比较范围的开始值，如果给定的值为null，则默认替换为0
	 * @param endValue     比较范围的结束值，如果给定的值为null，则默认替换为0
	 * @throws ValidateException
	 */
	public static void assertBetween(String msg, Integer compareValue, Integer startValue, Integer endValue)
			throws ValidateException {
		if (!BetweenUtil.isBetween(compareValue, startValue, endValue)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否不在指定的数据范围的区间内，如果不在指定的范围内疚抛出异常<br/>
	 * <br/>
	 * 注意：包含边界<br/>
	 * 例如 compareValue=2，startValue=2 ，endValue=8时是正常情况<br/>
	 * 例如 compareValue=1，startValue=2 ，endValue=8时会抛出异常<br/>
	 * 
	 * @param msg          提示信息
	 * @param compareValue 给定的值，如果给定的值为null，则默认替换为0
	 * @param startValue   比较范围的开始值，如果给定的值为null，则默认替换为0
	 * @param endValue     比较范围的结束值，如果给定的值为null，则默认替换为0
	 * @throws ValidateException
	 */
	public static void assertBetween(String msg, Long compareValue, Long startValue, Long endValue)
			throws ValidateException {
		if (!BetweenUtil.isBetween(compareValue, startValue, endValue)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等，如果不相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertObjectEquals(String msg, Object value1, Object value2) throws ValidateException {
		if (!equals(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等，如果相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertObjectNotEquals(String msg, Object value1, Object value2) throws ValidateException {
		if (equals(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等
	 * 
	 * @param value1 第一个值
	 * @param value2 第二个值
	 * @return 若相等返回为true,否则为false
	 */
	private static boolean equals(Object value1, Object value2) {
		if (null == value1 && null == value2) {
			return true;
		} else if (null == value1 && null != value2) {
			return false;
		} else if (null != value1 && null == value2) {
			return false;
		}
		return value1.equals(value2);
	}

	/**
	 * 判断两个数据的toString()值是否相等，如果不相等就抛出异常<br/>
	 * <br/>
	 * 适用于以下场景:<br/>
	 * 1 比较Boolean类型的数据<br/>
	 * 2 比较Integer类型的数据<br/>
	 * 3 比较Long类型的数据<br/>
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertEquals2String(String msg, Object value1, Object value2) throws ValidateException {
		if (!equals2String(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个数据的toString()值是否不相等，如果相等就抛出异常 <br/>
	 * 适用于以下场景:<br/>
	 * 1 比较Boolean类型的数据<br/>
	 * 2 比较Integer类型的数据<br/>
	 * 3 比较Long类型的数据<br/>
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertNotEquals2String(String msg, Object value1, Object value2) throws ValidateException {
		if (equals2String(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等
	 * 
	 * @param value1 第一个值
	 * @param value2 第二个值
	 * @return 若相等返回为true,否则为false
	 */
	private static boolean equals2String(Object value1, Object value2) {
		if (null == value1 && null == value2) {
			return true;
		} else if (null == value1 && null != value2) {
			return false;
		} else if (null != value1 && null == value2) {
			return false;
		}
		return value1.toString().equals(value2.toString());
	}

	/**
	 * 判断两个值是否相等(区分大小写)，如果不相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertEquals(String msg, String value1, String value2) throws ValidateException {
		if (!StringUtils.equals(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertNotEquals(String msg, String value1, String value2) throws ValidateException {
		if (StringUtils.equals(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等(区分大小写)，如果不相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertEquals(String msg, Integer value1, Integer value2) throws ValidateException {
		if (!equals(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertNotEquals(String msg, Integer value1, Integer value2) throws ValidateException {
		if (equals(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等(不区分大小写)，如果不相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertEqualsIgnoreCase(String msg, String value1, String value2) throws ValidateException {
		if (!StringUtils.equalsIgnoreCase(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等(不区分大小写)，如果相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 * @throws ValidateException
	 */
	public static void assertNotEqualsIgnoreCase(String msg, String value1, String value2) throws ValidateException {
		if (StringUtils.equalsIgnoreCase(value1, value2)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

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
							throw new ValidateException(ErrorCode.DATA_ERROR, msg);
						}
					} else {
						throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
					throw new ValidateException(ErrorCode.DATA_ERROR, msg);
				} else {
					if (StringUtils.isBlank(value.toString())) {
						throw new ValidateException(ErrorCode.DATA_ERROR, msg);
					}

				}
			}
		}
	}

	/**
	 * 判断给定的数据里是否包含空值，如果不包含空值就抛出异常
	 * 
	 * @param msg    提示信息
	 * @param values 需要判断的数据
	 * @throws ValidateException
	 */
	public static void assertHasBlank(String msg, Object... values) throws ValidateException {
		if (EmptyUtil.notEmpty(values)) {
			// 包含空值判断标志
			boolean contain = false;
			for (Object value : values) {
				if (null == value) {
					contain = true;
					break;
				}
				if (StringUtils.isBlank(value.toString())) {
					contain = true;
					break;
				}
			}
			if (!contain) {
				throw new ValidateException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断给定的数据里是否包含空值，如果包含空值就抛出异常
	 * 
	 * @param msg    提示信息
	 * @param values 需要判断的数据
	 * @throws ValidateException
	 */
	public static void assertHasNoBlank(String msg, Object... values) throws ValidateException {
		if (EmptyUtil.notEmpty(values)) {
			// 包含空值判断标志
			boolean contain = false;
			for (Object value : values) {
				if (null == value) {
					contain = true;
					break;
				}
				if (StringUtils.isBlank(value.toString())) {
					contain = true;
					break;
				}
			}
			if (contain) {
				throw new ValidateException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断Optional里对象是否为空，如果不为空则抛出异常
	 * 
	 * @param <T>
	 * @param optional
	 * @param msg      提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertOptionalNull(Optional<T> optional, String msg) throws ValidateException {
		if (null != optional) {
			if (optional.isPresent()) {
				throw new ValidateException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断Optional里对象是否不为空，如果为空则抛出异常
	 * 
	 * @param <T>
	 * @param optional
	 * @param msg      提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertOptionalNotNull(Optional<T> optional, String msg) throws ValidateException {
		if (null == optional) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
		if (!optional.isPresent()) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断data里对象是否为空，如果不为空则抛出异常
	 * 
	 * @param <T>
	 * @param optional
	 * @param msg      提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNull(Object data, String msg) throws ValidateException {
		if (null != data) {
			if (data instanceof String) {
				if (StringUtils.isNotBlank(data.toString())) {
					throw new ValidateException(ErrorCode.DATA_ERROR, msg);
				}
			} else {
				throw new ValidateException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断data里对象是否不为空，如果为空则抛出异常
	 * 
	 * @param <T>
	 * @param optional
	 * @param msg      提示信息
	 * @throws ValidateException
	 */
	public static <T> void assertNotNull(Object data, String msg) throws ValidateException {
		if (null == data) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);

		} else {
			if (data instanceof String) {
				if (StringUtils.isBlank(data.toString())) {
					throw new ValidateException(ErrorCode.DATA_ERROR, msg);
				}
			}
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断List是否为空，若不为空则抛出异常
	 * 
	 * @param <T>
	 * @param data list数据
	 * @param msg  异常提示信息
	 * @throws ValidateException
	 */
	public synchronized static <T> void assertEmpty(List<T> list, String msg) throws ValidateException {
		if (EmptyUtil.notEmpty(list)) {
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
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
			throw new ValidateException(ErrorCode.DATA_ERROR, msg);
		}
	}

}
