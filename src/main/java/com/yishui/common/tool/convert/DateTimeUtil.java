/**
 * 
 */
package com.yishui.common.tool.convert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocalDateTime与DateTime转换
 * 
 * @author yishui
 * @date 2018年12月11日
 * @Version 0.0.1
 */
public final class DateTimeUtil {
	private final static Logger log=LoggerFactory.getLogger(DateTimeUtil.class);
	/**
	 * 将Date转换为 LocalDateTime
	 * 
	 * @param date
	 * @return
	 */
	public static synchronized LocalDateTime date2LocalDateTime(Date date) {
		Instant instant = date.toInstant();
		ZoneId zone = ZoneId.systemDefault();
		log.debug("当前时区为 {}",zone);
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
		return localDateTime;
	}

	/**
	 * 将LocalDateTime转换为 Date
	 * 
	 * @param localDateTime
	 * @return
	 */
	public static synchronized Date localDateTime2Date(LocalDateTime localDateTime) {
		ZoneId zone = ZoneId.systemDefault();
		log.debug("当前时区为 {}",zone);
		Instant instant = localDateTime.atZone(zone).toInstant();
		java.util.Date date = Date.from(instant);
		return date;
	}

}
