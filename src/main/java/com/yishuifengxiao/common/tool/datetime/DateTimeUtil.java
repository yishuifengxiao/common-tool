/**
 * 
 */
package com.yishuifengxiao.common.tool.datetime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * LocalDateTime与DateTime转换
 * 
 * @author yishui
 * @date 2018年12月11日
 * @Version 0.0.1
 */
public final class DateTimeUtil {

	/**
	 * 默认的时区,上海
	 */
	private final static String DEFAULT_ZONE = "Asia/Shanghai";

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

}
