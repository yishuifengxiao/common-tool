package com.yishuifengxiao.common.tool.utils;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 数字转换与操作比较工具
 * </p>
 * 该工具主要是将给定的数字与0进行比较和将数字转换成boolean以及将字符串解析成数字。该工具的主要功能如下：
 * <ol>
 * <li>判断给定的数字是否小于或等于0</li>
 * <li>判断给定的数字是否小于0</li>
 * <li>将数字转换成boolean值</li>
 * <li>将boolean值转换成数字</li>
 * <li>获取封装数据类型里的数据，防止出现NPE</li>
 * <li>将字符串解析为数字</li>
 * </ol>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class NumberUtil {
	/**
	 * 零值
	 */
	private static final BigDecimal ZERO = BigDecimal.ZERO;

	/**
	 * 判断输入值是否大于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于或等于0返回为true,否则为false
	 */
	public static boolean greaterEqualZero(Integer value) {
		if (null == value) {
			return false;
		}
		return value >= 0;
	}

	/**
	 * 判断输入值是否大于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于或等于0返回为true,否则为false
	 */
	public static boolean greaterEqualZero(Long value) {
		if (null == value) {
			return false;
		}
		return value >= 0;
	}

	/**
	 * 判断输入值是否大于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于或等于0返回为true,否则为false
	 */
	public static boolean greaterEqualZero(Float value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) >= 0;
	}

	/**
	 * 判断输入值是否大于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于或等于0返回为true,否则为false
	 */
	public static boolean greaterEqualZero(Double value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) >= 0;
	}

	/**
	 * 判断输入值是否大于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于或等于0返回为true,否则为false
	 */
	public static boolean greaterEqualZero(BigDecimal value) {
		if (null == value) {
			return false;
		}
		return value.compareTo(ZERO) >= 0;
	}

	/**
	 * 判断输入值是否大于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于0返回为true,否则为false
	 */
	public static boolean greaterZero(Integer value) {
		if (null == value) {
			return false;
		}
		return value > 0;
	}

	/**
	 * 判断输入值是否大于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于0返回为true,否则为false
	 */
	public static boolean greaterZero(Long value) {
		if (null == value) {
			return false;
		}
		return value > 0;
	}

	/**
	 * 判断输入值是否大于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于0返回为true,否则为false
	 */
	public static boolean greaterZero(Float value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) > 0;
	}

	/**
	 * 判断输入值是否大于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于0返回为true,否则为false
	 */
	public static boolean greaterZero(Double value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) > 0;
	}

	/**
	 * 判断输入值是否大于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值大于0返回为true,否则为false
	 */
	public static boolean greaterZero(BigDecimal value) {
		if (null == value) {
			return false;
		}
		return value.compareTo(ZERO) > 0;
	}

	/**
	 * 判断输入值是否小于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于或等于0返回为true,否则为false
	 */
	public static boolean lessEqualZero(Integer value) {
		if (null == value) {
			return false;
		}
		return value <= 0;
	}

	/**
	 * 判断输入值是否小于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于或等于0返回为true,否则为false
	 */
	public static boolean lessEqualZero(Long value) {
		if (null == value) {
			return false;
		}
		return value <= 0;
	}

	/**
	 * 判断输入值是否小于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于或等于0返回为true,否则为false
	 */
	public static boolean lessEqualZero(Float value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) <= 0;
	}

	/**
	 * 判断输入值是否小于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于或等于0返回为true,否则为false
	 */
	public static boolean lessEqualZero(Double value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) <= 0;
	}

	/**
	 * 判断输入值是否小于或等于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于或等于0返回为true,否则为false
	 */
	public static boolean lessEqualZero(BigDecimal value) {
		if (null == value) {
			return false;
		}
		return value.compareTo(ZERO) <= 0;
	}

	/**
	 * 判断输入值是否小于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于0返回为true,否则为false
	 */
	public static boolean lessZero(Integer value) {
		if (null == value) {
			return false;
		}
		return value < 0;
	}

	/**
	 * 判断输入值是否小于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于0返回为true,否则为false
	 */
	public static boolean lessZero(Long value) {
		if (null == value) {
			return false;
		}
		return value < 0;
	}

	/**
	 * 判断输入值是否小于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于0返回为true,否则为false
	 */
	public static boolean lessZero(Float value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) < 0;
	}

	/**
	 * 判断输入值是否小于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于0返回为true,否则为false
	 */
	public static boolean lessZero(Double value) {
		if (null == value) {
			return false;
		}
		return new BigDecimal(value).compareTo(ZERO) < 0;
	}

	/**
	 * 判断输入值是否小于0
	 * 
	 * @param value 需要判断的输入值，若该值为null直接返回false
	 * @return 输入值小于0返回为true,否则为false
	 */
	public static boolean lessZero(BigDecimal value) {
		if (null == value) {
			return false;
		}
		return value.compareTo(ZERO) < 0;
	}

	/**
	 * 判断两个数据是否相等
	 * 
	 * @param originalValue 原始值，如果原始值为null直接返回为 false
	 * @param value         被比较值
	 * @return 如果两个值相等返回为true,否则为false
	 */
	public static boolean equals(Integer originalValue, Integer value) {
		if (null == originalValue || null == value) {
			return false;
		}
		return originalValue.equals(value) || StringUtils.equalsIgnoreCase(originalValue.toString(), value.toString());
	}

	/**
	 * 判断两个数据是否相等
	 * 
	 * @param originalValue 原始值，如果原始值为null直接返回为 false
	 * @param value         被比较值
	 * @return 如果两个值相等返回为true,否则为false
	 */
	public static boolean equals(Long originalValue, Long value) {
		if (null == originalValue || null == value) {
			return false;
		}
		return originalValue.equals(value) || StringUtils.equalsIgnoreCase(originalValue.toString(), value.toString());
	}

	/**
	 * 判断数据里是否有等于目标数据的数字
	 * 
	 * @param originalValue 目标数据，如果为null直接返回为false
	 * @param values        待比较的数据，如果为null直接返回为false
	 * @return 待比较的数据里包含了目标数据就返回为true，否则为false
	 */
	public static boolean contains(Integer originalValue, Integer... values) {
		if (null == originalValue) {
			return false;
		}
		if (null == values) {
			return false;
		}
		for (Integer value : values) {
			if (equals(originalValue, value)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取一个自然数
	 * 
	 * @param value 输入的数据
	 * @return 如果输入的数据为null或负数则返回为0，否则返回为原始值
	 */
	public static Integer getNumber(Integer value) {
		if (null == value || value < 0) {
			return 0;
		}
		return value;
	}

	/**
	 * 获取一个自然数
	 * 
	 * @param value 输入的数据
	 * @return 如果输入的数据为null或负数则返回为0，否则返回为原始值
	 */
	public static Long getNumber(Long value) {
		if (null == value || value < 0) {
			return 0L;
		}
		return value;
	}

	/**
	 * 获取一个自然数
	 * 
	 * @param value 输入的数据
	 * @return 如果输入的数据为数据类型，当转化后的数据为null或负数则返回为0，否则返回为原始值
	 */
	public static Long getNumber(String value) {
		return getNumber(parseLong(value));
	}

	/**
	 * <p>
	 * 将字符串形式的数字转成Boolean值
	 * </p>
	 * <p>
	 * 1 如果数字为null，返回为false
	 * </p>
	 * <p>
	 * 2 数字小于或等于0返回为false
	 * </p>
	 * <p>
	 * 3 数字大于0返回为true
	 * 
	 * @param value 需要转换的数据
	 * @return boolean值
	 */
	public static Boolean num2Bool(String value) {
		int val = parseInt(value, 0);
		if (val <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * 将数字转成Boolean值
	 * </p>
	 * <p>
	 * 1 如果数字为null，返回为false
	 * </p>
	 * <p>
	 * 2 数字小于或等于0返回为false
	 * </p>
	 * <p>
	 * 3 数字大于0返回为true
	 * 
	 * @param value 需要转换的数据
	 * @return boolean值
	 */
	public static Boolean num2Bool(Integer value) {
		if (null == value) {
			return false;
		}
		if (value <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * <p>
	 * 将数字转成Boolean值
	 * </p>
	 * <p>
	 * 1 如果数字为null，返回为false
	 * </p>
	 * <p>
	 * 2 数字小于或等于0返回为false
	 * </p>
	 * <p>
	 * 3 数字大于0返回为true
	 * 
	 * @param value 需要转换的数据
	 * @return boolean值
	 */
	public static Boolean num2Bool(Long value) {
		if (null == value) {
			return false;
		}
		if (value <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 将boolean值转换成数字
	 * 
	 * @param value boolean值
	 * @return value为true时返回1，否则为0
	 */
	public static int bool2Int(boolean value) {
		return value ? 1 : 0;
	}

	/**
	 * <p>
	 * 数据自动补0
	 * </p>
	 * 对于null值的装箱后的数据，自动设置为false
	 * 
	 * @param value 需要转换的数据
	 * @return 数据自动补0, 对于null值的装箱后的数据，自动设置为false
	 */
	public static Boolean get(Boolean value) {
		return null == value ? false : value;
	}

	/**
	 * <p>
	 * 数据自动补0
	 * </p>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value 需要转换的数据
	 * @return 对于null值的装箱后的数据，自动设置为0
	 */
	public static Integer get(Integer value) {
		return null == value ? 0 : value;
	}

	/**
	 * <p>
	 * 数据自动补0
	 * </p>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value 需要转换的数据
	 * @return 对于null值的装箱后的数据，自动设置为0
	 */
	public static Long get(Long value) {
		return null == value ? 0L : value;
	}

	/**
	 * <p>
	 * 数据自动补0
	 * </p>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value 需要转换的数据
	 * @return 对于null值的装箱后的数据，自动设置为0
	 */
	public static Float get(Float value) {
		return null == value ? 0 : value;
	}

	/**
	 * <p>
	 * 数据自动补0
	 * </p>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value 需要转换的数据
	 * @return 对于null值的装箱后的数据，自动设置为0
	 */
	public static Double get(Double value) {
		return null == value ? 0 : value;
	}

	/**
	 * 判断数据是否为null，如果为null则返回默认值，否则返回为输入值
	 * 
	 * @param value        输入值
	 * @param defaultValue 默认值
	 * @return 如果为null则返回默认值，否则返回为输入值
	 */
	public static Boolean get(Boolean value, boolean defaultValue) {
		return null == value ? defaultValue : value;
	}

	/**
	 * 判断数据是否为null，如果为null则返回默认值，否则返回为输入值
	 * 
	 * @param value        输入值
	 * @param defaultValue 默认值
	 * @return 如果为null则返回默认值，否则返回为输入值
	 */
	public static Integer get(Integer value, int defaultValue) {
		return null == value ? defaultValue : value;
	}

	/**
	 * 判断数据是否为null，如果为null则返回默认值，否则返回为输入值
	 * 
	 * @param value        输入值
	 * @param defaultValue 默认值
	 * @return 如果为null则返回默认值，否则返回为输入值
	 */
	public static Long get(Long value, long defaultValue) {
		return null == value ? defaultValue : value;
	}

	/**
	 * 判断数据是否为null，如果为null则返回默认值，否则返回为输入值
	 * 
	 * @param value        输入值
	 * @param defaultValue 默认值
	 * @return 如果为null则返回默认值，否则返回为输入值
	 */
	public static Float get(Float value, float defaultValue) {
		return null == value ? defaultValue : value;
	}

	/**
	 * 判断数据是否为null，如果为null则返回默认值，否则返回为输入值
	 * 
	 * @param value        输入值
	 * @param defaultValue 默认值
	 * @return 如果为null则返回默认值，否则返回为输入值
	 */
	public static Double get(Double value, double defaultValue) {
		return null == value ? defaultValue : value;
	}

	/**
	 * 将字符串转为Double
	 * 
	 * @param str 需要解析的字符串
	 * @return 解析成功则返回Double的数据，否则为null
	 */
	public static Double parseDouble(String str) {

		try {
			if (StringUtils.isNotBlank(str)) {
				return Double.parseDouble(str.trim());
			}
		} catch (Exception e) {
			log.debug("【易水工具】将字符串 {} 解析为 Double 时出现问题，出现问题的原因为 {}", str, e.getMessage());
		}
		return null;
	}

	/**
	 * 将字符串转为Double
	 * 
	 * @param str          需要解析的字符串
	 * @param defaultValue 解析失败时的默认值
	 * @return 解析成功则返回Double的数据，否则为defaultValue
	 */
	public static Double parseDouble(String str, double defaultValue) {
		Double value = parseDouble(str);
		return null == value ? defaultValue : value;
	}

	/**
	 * 将字符串转为Float
	 * 
	 * @param str 需要解析的字符串
	 * @return 解析成功则返回Float的数据，否则为null
	 */
	public static Float parseFloat(String str) {

		try {
			if (StringUtils.isNotBlank(str)) {
				return Float.parseFloat(str.trim());
			}
		} catch (Exception e) {
			log.debug("【易水工具】将字符串 {} 解析为 Float 时出现问题，出现问题的原因为 {}", str, e.getMessage());
		}
		return null;
	}

	/**
	 * 将字符串转为Float
	 * 
	 * @param str          需要解析的字符串
	 * @param defaultValue 解析失败时的默认值
	 * @return 解析成功则返回Float的数据，否则为defaultValue
	 */
	public static Float parseFloat(String str, float defaultValue) {
		Float value = parseFloat(str);
		return null == value ? defaultValue : value;
	}

	/**
	 * 将字符串转为Integer
	 * 
	 * @param str 需要解析的字符串
	 * @return 解析成功则返回Integer的数据，否则为null
	 */
	public static Integer parseInt(String str) {

		try {
			if (StringUtils.isNotBlank(str)) {
				return Integer.parseInt(str.trim());
			}
		} catch (Exception e) {
			log.debug("【易水工具】将字符串 {} 解析为 Integer 时出现问题，出现问题的原因为 {}", str, e.getMessage());
		}
		return null;
	}

	/**
	 * 将字符串转为Integer
	 * 
	 * @param str          需要解析的字符串
	 * @param defaultValue 解析失败时的默认值
	 * @return 解析成功则返回Integer的数据，否则为defaultValue
	 */
	public static Integer parseInt(String str, int defaultValue) {
		Integer value = parseInt(str);
		return null == value ? defaultValue : value;
	}

	/**
	 * 将字符串转为Long
	 * 
	 * @param str 需要解析的字符串
	 * @return 解析成功则返回Long的数据，否则为null
	 */
	public static Long parseLong(String str) {

		try {
			if (StringUtils.isNotBlank(str)) {
				return Long.parseLong(str.trim());
			}
		} catch (Exception e) {
			log.debug("【易水工具】将字符串 {} 解析为 Integer 时出现问题，出现问题的原因为 {}", str, e.getMessage());
		}
		return null;
	}

	/**
	 * 将字符串转为Long
	 * 
	 * @param str          需要解析的字符串
	 * @param defaultValue 解析失败时的默认值
	 * @return 解析成功则返回Long的数据，否则为defaultValue
	 */
	public static Long parseLong(String str, long defaultValue) {
		Integer value = parseInt(str);
		return null == value ? defaultValue : value;
	}
}
