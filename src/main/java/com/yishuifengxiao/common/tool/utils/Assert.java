package com.yishuifengxiao.common.tool.utils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.collections.SizeUtil;
import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.CompareUtil;

/**
 * <p>
 * 断言工具
 * </p>
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
 * @author yishui
 * @version 1.0.0
 * @see UncheckedException
 * @since 1.0.0
 */
public final class Assert {

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gteZero(String msg, Integer value) {
		if (!CompareUtil.gteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gteZero(String msg, Long value) {
		if (!CompareUtil.gteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gteZero(String msg, Float value) {
		if (!CompareUtil.gteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gteZero(String msg, Double value) {
		if (!CompareUtil.gteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于或等于0,如果为null或小于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gteZero(String msg, BigDecimal value) {
		if (!CompareUtil.gteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gtZero(String msg, Integer value) {
		if (!CompareUtil.gtZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gtZero(String msg, Long value) {
		if (!CompareUtil.gtZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gtZero(String msg, Float value) {
		if (!CompareUtil.gtZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gtZero(String msg, Double value) {
		if (!CompareUtil.gtZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否大于0,如果为null或小于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void gtZero(String msg, BigDecimal value) {
		if (!CompareUtil.gtZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void lteZero(String msg, Integer value) {
		if (!CompareUtil.lteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void lteZero(String msg, Long value) {
		if (!CompareUtil.lteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void lteZero(String msg, Float value) {
		if (!CompareUtil.lteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void lteZero(String msg, Double value) {
		if (!CompareUtil.lteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于或等于0,如果为null或大于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void lteZero(String msg, BigDecimal value) {
		if (!CompareUtil.lteZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void ltZero(String msg, Integer value) {
		if (!CompareUtil.ltZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void ltZero(String msg, Long value) {
		if (!CompareUtil.ltZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void ltZero(String msg, Float value) {
		if (!CompareUtil.ltZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void ltZero(String msg, Double value) {
		if (!CompareUtil.ltZero(value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断给定的值是否小于0,如果为null或大于0或等于0就抛出异常
	 *
	 * @param msg   异常提示信息
	 * @param value 需要比较的值
	 */
	public static void ltZero(String msg, BigDecimal value) {
		if (!CompareUtil.ltZero(value)) {
			throw new UncheckedException(msg);
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
	 */
	public static void equal(String msg, Integer originalValue, Integer value) {
		if (!CompareUtil.equals(originalValue, value)) {
			throw new UncheckedException(msg);
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
	 */
	public static void equal(String msg, Long originalValue, Long value) {
		if (!CompareUtil.equals(originalValue, value)) {
			throw new UncheckedException(msg);
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
	 */
	public static void equal(String msg, String originalValue, String value) {
		if (!StringUtils.equals(originalValue, value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 *
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 */
	public static void notEquals(String msg, Integer originalValue, Integer value) {
		if (CompareUtil.equals(originalValue, value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 *
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 */
	public static void notEquals(String msg, Long originalValue, Long value) {
		if (CompareUtil.equals(originalValue, value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否不相等(区分大小写)，如果相等就抛出异常
	 *
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 */
	public static void notEquals(String msg, String originalValue, String value) {
		if (StringUtils.equals(originalValue, value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否相等(不区分大小写)，如果不相等就抛出异常
	 *
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 */
	public static void equalIgnoreCase(String msg, String originalValue, String value) {
		if (!StringUtils.equalsIgnoreCase(originalValue, value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否不相等(不区分大小写)，如果相等就抛出异常
	 *
	 * @param msg           异常提示信息
	 * @param originalValue 需要比较的值
	 * @param value         需要被比较的值
	 */
	public static void notEqualsIgnoreCase(String msg, String originalValue, String value) {
		if (StringUtils.equalsIgnoreCase(originalValue, value)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断数据是否为true,如果不是为true则抛出异常
	 *
	 * @param msg  提示信息
	 * @param bool 需要判断的数据
	 */
	public static void isTrue(String msg, Boolean bool) {
		if (!BooleanUtils.isTrue(bool)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判读所有给定的数据为true,如果有一个数据不为true就抛出异常
	 *
	 * @param msg    异常提示信息
	 * @param values 需要判断的值
	 */
	public static void isTrue(String msg, boolean... values) {
		if (null != values) {
			for (boolean value : values) {
				if (!value) {
					throw new UncheckedException(msg);
				}
			}
		}

	}

	/**
	 * 判断数据是否为false,如果不是为false则抛出异常
	 *
	 * @param msg  提示信息
	 * @param bool 需要判断的数据
	 */
	public static void isFalse(String msg, Boolean bool) {
		if (!BooleanUtils.isFalse(bool)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判读所有给定的数据为false,如果有一个数据不为false就抛出异常
	 *
	 * @param msg    异常提示信息
	 * @param values 需要判断的值
	 */
	public static void isFalse(String msg, boolean... values) {
		if (null != values) {
			for (boolean value : values) {
				if (value) {
					throw new UncheckedException(msg);
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
	 */
	public static void between(String msg, Integer compareValue, Integer startValue, Integer endValue) {
		if (!CompareUtil.isBetween(compareValue, startValue, endValue)) {
			throw new UncheckedException(msg);
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
	public static void between(String msg, Long compareValue, Long startValue, Long endValue) {
		if (!CompareUtil.isBetween(compareValue, startValue, endValue)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否相等，如果不相等就抛出异常
	 *
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 */
	public static void objectEquals(String msg, Object value1, Object value2) {
		if (!equals(value1, value2)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否不相等，如果相等就抛出异常
	 *
	 * @param msg    异常提示信息
	 * @param value1 需要比较的值
	 * @param value2 需要被比较的值
	 */
	public static void objectNotEquals(String msg, Object value1, Object value2) {
		if (equals(value1, value2)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断两个值是否相等
	 *
	 * @param value1 第一个值
	 * @param value2 第二个值
	 * @return 若相等返回为true, 否则为false
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
	 */
	public static void isBlank(String msg, Object... values) {
		if (SizeUtil.notEmpty(values)) {
			for (Object value : values) {
				if (null != value) {
					if (value instanceof String) {
						if (StringUtils.isNotBlank(value.toString())) {
							throw new UncheckedException(msg);
						}
					} else {
						throw new UncheckedException(msg);
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
	 */
	public static void notBlank(String msg, Object... values) {
		if (SizeUtil.notEmpty(values)) {
			for (Object value : values) {
				if (null == value) {
					throw new UncheckedException(msg);
				} else {
					if (StringUtils.isBlank(value.toString())) {
						throw new UncheckedException(msg);
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
	public static void hasBlank(String msg, Object... values) {
		if (SizeUtil.notEmpty(values)) {
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
				throw new UncheckedException(msg);
			}
		}
	}

	/**
	 * 判断给定的数据里是否包含空值，如果包含空值就抛出异常
	 *
	 * @param msg    提示信息
	 * @param values 需要判断的数据
	 */
	public static void hasNoBlank(String msg, Object... values) {
		if (SizeUtil.notEmpty(values)) {
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
				throw new UncheckedException(msg);
			}
		}
	}

	/**
	 * 判断Optional里对象是否为空，如果不为空则抛出异常
	 *
	 * @param <T>      给定的数据的类型
	 * @param msg      提示信息
	 * @param optional 需要判断的数据
	 */
	public static <T> void optionalNull(String msg, Optional<T> optional) {
		if (null != optional) {
			if (optional.isPresent()) {
				throw new UncheckedException(msg);
			}
		}
	}

	/**
	 * 判断Optional里对象是否不为空，如果为空则抛出异常
	 *
	 * @param <T>      给定的数据的类型
	 * @param msg      提示信息
	 * @param optional 需要判断的数据
	 */
	public static <T> void optionalNotNull(String msg, Optional<T> optional) {
		if (null == optional) {
			throw new UncheckedException(msg);
		}
		if (!optional.isPresent()) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断data里对象是否为空，如果不为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  提示信息
	 * @param data 需要判断的数据
	 */
	public static <T> void isNull(String msg, Object data) {
		if (null != data) {
			if (data instanceof String) {
				if (StringUtils.isNotBlank(data.toString())) {
					throw new UncheckedException(msg);
				}
			} else {
				throw new UncheckedException(msg);
			}
		}
	}

	/**
	 * 判断data里对象是否不为空，如果为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  提示信息
	 * @param data 需要判断的数据
	 */
	public static <T> void notNull(String msg, Object data) {
		if (null == data) {
			throw new UncheckedException(msg);

		} else {
			if (data instanceof String) {
				if (StringUtils.isBlank(data.toString())) {
					throw new UncheckedException(msg);
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
	 */
	public static <T> void isEmpty(String msg, Page<T> page) {
		if (SizeUtil.notEmpty(page)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断分页对象是否不为空，若为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param page 分页对象
	 */
	public static <T> void notEmpty(String msg, Page<T> page) {
		if (SizeUtil.isEmpty(page)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断分页对象是否为空，若不为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param page 分页对象
	 */
	public static <T> void isEmpty(String msg, org.springframework.data.domain.Page<T> page) {
		if (SizeUtil.notEmpty(page)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断分页对象是否不为空，若为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param page 分页对象
	 */
	public static <T> void notEmpty(String msg, org.springframework.data.domain.Page<T> page) {
		if (SizeUtil.isEmpty(page)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断数组是否为空，若不为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 数组数据
	 */
	public static <T> void isEmpty(String msg, T[] data) {
		if (SizeUtil.notEmpty(data)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断数组是否不为空，若为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 数组数据
	 */
	public static <T> void notEmpty(String msg, T[] data) {
		if (SizeUtil.isEmpty(data)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断List是否为空，若不为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param list list数据
	 */
	public synchronized static <T> void isEmpty(String msg, List<T> list) {
		if (SizeUtil.notEmpty(list)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断set是否为空，若不为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data set数据
	 */
	public static <T> void isEmpty(String msg, Set<T> data) {
		if (SizeUtil.notEmpty(data)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断Set是否不为空，若为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data Set数据
	 */
	public static <T> void notEmpty(String msg, Set<T> data) {
		if (SizeUtil.isEmpty(data)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断List是否不为空，若为空则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data List数据
	 */
	public static <T> void notEmpty(String msg, List<T> data) {
		if (SizeUtil.isEmpty(data)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断集合里是否只有一个元素，若不是只有一个元素则抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 集合
	 */
	public static <T> void onlyOne(String msg, Collection<T> data) {
		if (!SizeUtil.onlyOneElement(data)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 判断集合是否不是只有一个元素，若只有一个元素就抛出异常
	 *
	 * @param <T>  给定的数据的类型
	 * @param msg  异常提示信息
	 * @param data 集合
	 */
	public static <T> void notOnlyOne(String msg, Collection<T> data) {
		if (SizeUtil.onlyOneElement(data)) {
			throw new UncheckedException(msg);
		}
	}

	/**
	 * 抛出一个运行时异常
	 *
	 * @param msg 异常信息
	 */
	public static void throwException(String msg) {
		throw new UncheckedException(msg);
	}

	/**
	 * 抛出一个运行时异常
	 *
	 * @param msg 异常信息
	 * @param e   异常原因
	 */
	public static void throwException(String msg, Throwable e) {
		throw new UncheckedException(msg, e);
	}

	/**
	 * 抛出一个运行时异常
	 *
	 * @param msg     异常信息
	 * @param e       异常原因
	 * @param context 异常原因
	 */
	public static void throwException(String msg, Throwable e, Object context) {
		throw new UncheckedException(msg, e).setContext(context);
	}

}
