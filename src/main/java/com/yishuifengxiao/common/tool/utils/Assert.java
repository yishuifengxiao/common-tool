package com.yishuifengxiao.common.tool.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;
import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.exception.constant.ErrorCode;
import com.yishuifengxiao.common.tool.lang.BetweenUtil;
import com.yishuifengxiao.common.tool.lang.NumberUtil;

/**
 * <h3>断言工具</h3>
 * <p>
 * 该工具主要判断给定的数据是否符合给定的条件，若数据不符合给定的条件就抛出自定义受检查的异常
 * </p>
 * <p>
 * 该主要是为了替换到代码里的各种<code>if</code>判断，从而提升代码的优雅性
 * </p>
 * 
 * <strong>注意：本工具中抛出的异常全部为运行时异常
 * <code>UncheckedException</code>,注意在使用时带来的处理问题</strong>
 * 
 * @see UncheckedException
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class Assert {

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void assertGreaterEqualZero(String msg, Integer value) {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void assertGreaterEqualZero(String msg, Long value) {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void assertGreaterEqualZero(String msg, Float value) {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void assertGreaterEqualZero(String msg, Double value) {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void assertGreaterEqualZero(String msg, BigDecimal value) {
		if (!NumberUtil.greaterEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertGreaterZero(String msg, Integer value) {
		if (!NumberUtil.greaterZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertGreaterZero(String msg, Long value) {
		if (!NumberUtil.greaterZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertGreaterZero(String msg, Float value) {
		if (!NumberUtil.greaterZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertGreaterZero(String msg, Double value) {
		if (!NumberUtil.greaterZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertGreaterZero(String msg, BigDecimal value) {
		if (!NumberUtil.greaterZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessEqualZero(String msg, Integer value) {
		if (!NumberUtil.lessEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessEqualZero(String msg, Long value) {
		if (!NumberUtil.lessEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessEqualZero(String msg, Float value) {
		if (!NumberUtil.lessEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessEqualZero(String msg, Double value) {
		if (!NumberUtil.lessEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessEqualZero(String msg, BigDecimal value) {
		if (!NumberUtil.lessEqualZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessZero(String msg, Integer value) {
		if (!NumberUtil.lessZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessZero(String msg, Long value) {
		if (!NumberUtil.lessZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessZero(String msg, Float value) {
		if (!NumberUtil.lessZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessZero(String msg, Double value) {
		if (!NumberUtil.lessZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 * 
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 * 
	 */
	public static void assertLessZero(String msg, BigDecimal value) {
		if (!NumberUtil.lessZero(value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * <p>
	 * 判断两个值是否相等，如果不相等就抛出异常
	 * </p>
	 * 任何比较值为null则抛出异常
	 * 
	 * @param msg           异常信息
	 * @param originalValue 原始值
	 * @param value         比较值
	 * 
	 */
	public static void assertEquals(String msg, Integer originalValue, Integer value) {
		if (!NumberUtil.equals(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * <p>
	 * 判断两个值是否相等，如果不相等就抛出异常
	 * </p>
	 * 任何比较值为null则抛出异常
	 * 
	 * @param msg           异常信息
	 * @param originalValue 原始值
	 * @param value         比较值
	 * 
	 * 
	 */
	public static void assertEquals(String msg, Long originalValue, Long value) {
		if (!NumberUtil.equals(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * <p>
	 * 判断两个值是否相等，如果不相等就抛出异常
	 * </p>
	 * 任何比较值为null则抛出异常
	 * 
	 * @param msg           异常信息
	 * @param originalValue 原始值
	 * @param value         比较值
	 * 
	 * 
	 */
	public static void assertEquals(String msg, String originalValue, String value) {
		if (!StringUtils.equals(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 * 
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 * 
	 */
	public static void assertNotEquals(String msg, Integer originalValue, Integer value) {
		if (NumberUtil.equals(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 * 
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 * 
	 */
	public static void assertNotEquals(String msg, Long originalValue, Long value) {
		if (NumberUtil.equals(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 * 
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 * 
	 */
	public static void assertNotEquals(String msg, String originalValue, String value) {
		if (StringUtils.equals(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等(不区分大小写)，如果不相等就抛出异常
	 * 
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 * 
	 */
	public static void assertEqualsIgnoreCase(String msg, String originalValue, String value) {
		if (!StringUtils.equalsIgnoreCase(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等(不区分大小写)，如果相等就抛出异常
	 * 
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 * 
	 */
	public static void assertNotEqualsIgnoreCase(String msg, String originalValue, String value) {
		if (StringUtils.equalsIgnoreCase(originalValue, value)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断数据是否为true,如果不是为true则抛出异常
	 * 
	 * @param msg  提示信息
	 * @param bool 需要判断的数据
	 *
	 */
	public static void assertTrue(String msg, Boolean bool) {
		if (!BooleanUtils.isTrue(bool)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判读所有给定的数据为true,如果有一个数据不为true就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param values 需要判断的值
	 */
	public static void assertTrue(String msg, boolean... values) {
		if (null != values) {
			for (boolean value : values) {
				if (!value) {
					throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
				}
			}
		}

	}

	/**
	 * 判断数据是否为false,如果不是为false则抛出异常
	 * 
	 * @param msg  提示信息
	 * @param bool 需要判断的数据
	 *
	 * 
	 */
	public static void assertFalse(String msg, Boolean bool) {
		if (!BooleanUtils.isFalse(bool)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判读所有给定的数据为false,如果有一个数据不为false就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param values 需要判断的值
	 */
	public static void assertFalse(String msg, boolean... values) {
		if (null != values) {
			for (boolean value : values) {
				if (value) {
					throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
				}
			}
		}

	}

	/**
	 * <p>
	 * 判断给定的值是否不在指定的数据范围的区间内，如果不在指定的范围内疚抛出异常
	 * </p>
	 * <p>
	 * 注意：包含边界
	 * </p>
	 * <p>
	 * 例如 compareValue=2，startValue=2 ，endValue=8时是正常情况
	 * </p>
	 * <p>
	 * 例如 compareValue=1，startValue=2 ，endValue=8时会抛出异常
	 * </p>
	 * 
	 * @param msg          提示信息
	 * @param compareValue 给定的值，如果给定的值为null，则默认替换为0
	 * @param startValue   比较范围的开始值，如果给定的值为null，则默认替换为0
	 * @param endValue     比较范围的结束值，如果给定的值为null，则默认替换为0
	 * 
	 */
	public static void assertBetween(String msg, Integer compareValue, Integer startValue, Integer endValue) {
		if (!BetweenUtil.isBetween(compareValue, startValue, endValue)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * <p>
	 * 判断给定的值是否不在指定的数据范围的区间内，如果不在指定的范围内疚抛出异常
	 * </p>
	 * <p>
	 * 注意：包含边界
	 * </p>
	 * <p>
	 * 例如 compareValue=2，startValue=2 ，endValue=8时是正常情况
	 * </p>
	 * <p>
	 * 例如 compareValue=1，startValue=2 ，endValue=8时会抛出异常
	 * </p>
	 * 
	 * @param msg          提示信息
	 * @param compareValue 给定的值，如果给定的值为null，则默认替换为0
	 * @param startValue   比较范围的开始值，如果给定的值为null，则默认替换为0
	 * @param endValue     比较范围的结束值，如果给定的值为null，则默认替换为0
	 */
	public static void assertBetween(String msg, Long compareValue, Long startValue, Long endValue) {
		if (!BetweenUtil.isBetween(compareValue, startValue, endValue)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否相等，如果不相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 */
	public static void assertObjectEquals(String msg, Object value1, Object value2) {
		if (!equals(value1, value2)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断两个值是否不相等，如果相等就抛出异常
	 * 
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 */
	public static void assertObjectNotEquals(String msg, Object value1, Object value2) {
		if (equals(value1, value2)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
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
	 * 判断传入的参数是否为空，若有传入的参数有一个不为空就抛出异常
	 * 
	 * @param msg    提示信息
	 * @param values 需要判断的数据
	 * 
	 */
	public static void assertBlank(String msg, Object... values) {
		if (EmptyUtil.notEmpty(values)) {
			for (Object value : values) {
				if (null != value) {
					if (value instanceof String) {
						if (StringUtils.isNotBlank(value.toString())) {
							throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
						}
					} else {
						throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
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
	 * 
	 */
	public static void assertNotBlank(String msg, Object... values) {
		if (EmptyUtil.notEmpty(values)) {
			for (Object value : values) {
				if (null == value) {
					throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
				} else {
					if (StringUtils.isBlank(value.toString())) {
						throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
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
	 */
	public static void assertHasBlank(String msg, Object... values) {
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
				throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断给定的数据里是否包含空值，如果包含空值就抛出异常
	 * 
	 * @param msg    提示信息
	 * @param values 需要判断的数据
	 * 
	 */
	public static void assertHasNoBlank(String msg, Object... values) {
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
				throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断Optional里对象是否为空，如果不为空则抛出异常
	 * 
	 * @param <T>      给定的数据的类型
	 * @param msg      提示信息
	 * @param optional 需要判断的数据
	 * 
	 * 
	 */
	public static <T> void assertOptionalNull(String msg, Optional<T> optional) {
		if (null != optional) {
			if (optional.isPresent()) {
				throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断Optional里对象是否不为空，如果为空则抛出异常
	 * 
	 * @param <T>      给定的数据的类型
	 * @param msg      提示信息
	 * @param optional 需要判断的数据
	 *
	 * 
	 */
	public static <T> void assertOptionalNotNull(String msg, Optional<T> optional) {
		if (null == optional) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
		if (!optional.isPresent()) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断data里对象是否为空，如果不为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  提示信息
	 * @param data 需要判断的数据
	 *
	 * 
	 */
	public static <T> void assertNull(String msg, Object data) {
		if (null != data) {
			if (data instanceof String) {
				if (StringUtils.isNotBlank(data.toString())) {
					throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
				}
			} else {
				throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
			}
		}
	}

	/**
	 * 判断data里对象是否不为空，如果为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  提示信息
	 * @param data 需要判断的数据
	 *
	 * 
	 */
	public static <T> void assertNotNull(String msg, Object data) {
		if (null == data) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);

		} else {
			if (data instanceof String) {
				if (StringUtils.isBlank(data.toString())) {
					throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
				}
			}
		}
	}

	/**
	 * 判断分页对象是否为空，若不为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param page 分页对象
	 * 
	 * 
	 */
	public static <T> void assertEmpty(String msg, Page<T> page) {
		if (EmptyUtil.notEmpty(page)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断分页对象是否不为空，若为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param page 分页对象
	 * 
	 * 
	 */
	public static <T> void assertNoEmpty(String msg, Page<T> page) {
		if (EmptyUtil.isEmpty(page)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断分页对象是否为空，若不为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param page 分页对象
	 * 
	 */
	public static <T> void assertEmpty(String msg, org.springframework.data.domain.Page<T> page) {
		if (EmptyUtil.notEmpty(page)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断分页对象是否不为空，若为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param page 分页对象
	 * 
	 * 
	 */
	public static <T> void assertNoEmpty(String msg, org.springframework.data.domain.Page<T> page) {
		if (EmptyUtil.isEmpty(page)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断数组是否为空，若不为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 数组数据
	 *
	 * 
	 */
	public static <T> void assertEmpty(String msg, T[] data) {
		if (EmptyUtil.notEmpty(data)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断数组是否不为空，若为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 数组数据
	 *
	 * 
	 */
	public static <T> void assertNoEmpty(String msg, T[] data) {
		if (EmptyUtil.isEmpty(data)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断List是否为空，若不为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param list list数据
	 * 
	 * 
	 */
	public synchronized static <T> void assertEmpty(String msg, List<T> list) {
		if (EmptyUtil.notEmpty(list)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断set是否为空，若不为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data set数据
	 * 
	 * 
	 */
	public static <T> void assertEmpty(String msg, Set<T> data) {
		if (EmptyUtil.notEmpty(data)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断Set是否不为空，若为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data Set数据
	 *
	 * 
	 */
	public static <T> void assertNoEmpty(String msg, Set<T> data) {
		if (EmptyUtil.isEmpty(data)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断List是否不为空，若为空则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data List数据
	 * 
	 */
	public static <T> void assertNoEmpty(String msg, List<T> data) {
		if (EmptyUtil.isEmpty(data)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断集合里是否只有一个元素，若不是只有一个元素则抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 集合
	 *
	 */
	public static <T> void assertOnlyOne(String msg, Collection<T> data) {
		if (!EmptyUtil.onlyOneElement(data)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

	/**
	 * 判断集合是否不是只有一个元素，若只有一个元素就抛出异常
	 * 
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 集合
	 * 
	 * 
	 */
	public static <T> void assertNotOnlyOne(String msg, Collection<T> data) {
		if (EmptyUtil.onlyOneElement(data)) {
			throw new UncheckedException(ErrorCode.DATA_ERROR, msg);
		}
	}

}
