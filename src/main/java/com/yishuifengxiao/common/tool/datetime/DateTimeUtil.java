/**
 * 
 */
package com.yishuifengxiao.common.tool.datetime;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

import com.yishuifengxiao.common.tool.exception.CustomException;

import lombok.extern.slf4j.Slf4j;

/**
 * LocalDateTime与DateTime转换
 * 
 * @author yishui
 * @date 2018年12月11日
 * @Version 0.0.1
 */
@Slf4j
public final class DateTimeUtil {

	/**
	 * 默认的时区,上海
	 */
	private final static String DEFAULT_ZONE = "Asia/Shanghai";

	/**
	 * 默认的日期时间的字符串形式
	 */
	private final static String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 默认的日期字符串形式
	 */
	private final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	/**
	 * 获取北京时间的ZoneId
	 * 
	 * @return 北京时间的ZoneId
	 */
	public static synchronized ZoneId zoneIdOfChina() {
		return ZoneId.of(DEFAULT_ZONE);
	}

	/**
	 * 将Date转换为 LocalDateTime
	 * 
	 * @param date
	 * @return
	 */
	public static synchronized LocalDateTime date2LocalDateTime(Date date) {
		if (null == date) {
			return null;
		}
		Instant instant = date.toInstant();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneIdOfChina());
		return localDateTime;
	}

	/**
	 * 将LocalDateTime转换为 Date
	 * 
	 * @param localDateTime
	 * @return
	 */
	public static synchronized Date localDateTime2Date(LocalDateTime localDateTime) {
		if (null == localDateTime) {
			return null;
		}
		Instant instant = localDateTime.atZone(zoneIdOfChina()).toInstant();
		java.util.Date date = Date.from(instant);
		return date;
	}

	/**
	 * 返回自1970年1月1日以来，由此LocalDateTime对应的 Date对象表示的00:00:00 GMT的毫秒 数 。
	 * 
	 * @param localDateTime
	 * @return
	 */
	public static synchronized Long getTime(LocalDateTime localDateTime) {
		if (null == localDateTime) {
			return null;
		}
		return localDateTime2Date(localDateTime).getTime();
	}

	/**
	 * 返回自1970年1月1日以来，由此 Date对象表示的00:00:00 GMT的毫秒 数 。
	 * 
	 * @param date
	 * @return
	 */
	public static synchronized Long getTime(Date date) {
		if (null == date) {
			return null;
		}
		return date.getTime();
	}

	/**
	 * 使用从1970-01-01T00：00：00Z的时代开始的毫秒 数获得一个LocalDateTime的实例。
	 * 
	 * @param milliseconds 从1970-01-01T00：00：00Z的时代开始的毫秒 数
	 * @return
	 */
	public static synchronized LocalDateTime getLocalDateTime(long milliseconds) {
		return date2LocalDateTime(new Date(milliseconds));
	}

	/**
	 * 将字符串解析为LocalDateTime 形式的时间<br/>
	 * 
	 * 默认采用yyyy-MM-dd HH:mm:ss 和 yyyy-MM-dd 形式解析
	 * 
	 * @param timeStr 需要解析的字符串
	 * @param pattern 解析规则 当未填写解析规则时，
	 * @return LocalDateTime形式的时间
	 * @throws CustomException
	 */
	public static synchronized LocalDateTime parse(String timeStr) throws CustomException {
		return parse(timeStr, DEFAULT_DATETIME_FORMAT, DEFAULT_DATE_FORMAT);
	}

	/**
	 * 将字符串解析为LocalDateTime 形式的时间
	 * 
	 * @param timeStr 需要解析的字符串
	 * @param pattern 解析规则 当未填写解析规则时
	 * @return LocalDateTime形式的时间
	 * @throws CustomException
	 */
	public static synchronized LocalDateTime parse(String timeStr, String... patterns) throws CustomException {

		try {
			return date2LocalDateTime(DateUtils.parseDate(timeStr, patterns));
		} catch (ParseException e) {
			log.info("按照解析规则 {} 从字符串 {} 中解析出时间时出现问题，出现问题的原因为{}", patterns, timeStr, e.getMessage());
			throw new CustomException("从字符串中解析时间失败");
		}
	}

	/**
	 * 将LocalDateTime形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
	 * 
	 * @param localDateTime
	 * @return yyyy-MM-dd HH:mm:ss格式的字符串
	 */
	public static synchronized String format(LocalDateTime localDateTime) {
		return format(localDateTime, DEFAULT_DATETIME_FORMAT);
	}

	/**
	 * 将LocalDateTime形式的时间格式化为 格式化为指定形式的字符串
	 * 
	 * @param localDateTime
	 * @param pattern       格式化形式，例如yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static synchronized String format(LocalDateTime localDateTime, String pattern) {
		if (null == localDateTime) {
			return null;
		}
		DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
		return format.format(localDateTime);
	}

	/**
	 * 将Date形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
	 * 
	 * @param localDateTime
	 * @return yyyy-MM-dd HH:mm:ss格式的字符串
	 */
	public static synchronized String format(Date date) {
		return format(date, DEFAULT_DATETIME_FORMAT);
	}

	/**
	 * 将Date形式的时间格式化为 格式化为指定形式的字符串
	 * 
	 * @param localDateTime
	 * @param pattern       格式化形式，例如yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static synchronized String format(Date date, String pattern) {
		if (null == date) {
			return null;
		}
		DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
		return format.format(date2LocalDateTime(date));
	}

}