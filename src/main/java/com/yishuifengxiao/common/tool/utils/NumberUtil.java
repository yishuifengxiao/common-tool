package com.yishuifengxiao.common.tool.utils;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 数字工具类
 * 
 * @author qingteng
 * @date 2020年11月25日
 * @version 1.0.0
 */
@Slf4j
public final class NumberUtil {

	/**
	 * 将字符串形式的数字转成Boolean值<br/>
	 * 1 如果数字为null，返回为false<br/>
	 * 2 数字小于或等于0返回为false<br/>
	 * 3 数字大于0返回为true
	 * 
	 * @param value
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
	 * 将数字转成Boolean值<br/>
	 * 1 如果数字为null，返回为false<br/>
	 * 2 数字小于或等于0返回为false<br/>
	 * 3 数字大于0返回为true
	 * 
	 * @param value
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
	 * 将数字转成Boolean值<br/>
	 * 1 如果数字为null，返回为false<br/>
	 * 2 数字小于或等于0返回为false<br/>
	 * 3 数字大于0返回为true
	 * 
	 * @param value
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
	 * 数据自动补0<br/>
	 * 对于null值的装箱后的数据，自动设置为false
	 * 
	 * @param value
	 * @return
	 */
	public static Boolean get(Boolean value) {
		return null == value ? false : value;
	}

	/**
	 * 数据自动补0<br/>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value
	 * @return
	 */
	public static Integer get(Integer value) {
		return null == value ? 0 : value;
	}

	/**
	 * 数据自动补0<br/>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value
	 * @return
	 */
	public static Long get(Long value) {
		return null == value ? 0L : value;
	}

	/**
	 * 数据自动补0<br/>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value
	 * @return
	 */
	public static Float get(Float value) {
		return null == value ? 0 : value;
	}

	/**
	 * 数据自动补0<br/>
	 * 对于null值的装箱后的数据，自动设置为0
	 * 
	 * @param value
	 * @return
	 */
	public static Double get(Double value) {
		return null == value ? 0 : value;
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
			log.debug("将字符串 {} 解析为 Double 时出现问题，出现问题的原因为 {}", str, e.getMessage());
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
			log.debug("将字符串 {} 解析为 Float 时出现问题，出现问题的原因为 {}", str, e.getMessage());
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
			log.debug("将字符串 {} 解析为 Integer 时出现问题，出现问题的原因为 {}", str, e.getMessage());
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
			log.debug("将字符串 {} 解析为 Integer 时出现问题，出现问题的原因为 {}", str, e.getMessage());
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
