package com.yishuifengxiao.common.tool.utils;

import java.time.LocalDateTime;

/**
 * 日期时间偏移工具类
 * 
 * @author yishui
 * @date 2020年10月19日
 * @Version 0.0.1
 */
public class DateOffsetUtil {
	/**
	 * 时间的起始值
	 */
	private final static int START_OF_TIME = 0;
	/**
	 * 昨天的偏移量
	 */
	private final static long OFFSET_DAY_FOR_YESTERDAY = 1L;
	/**
	 * 7天的偏移量
	 */
	private final static long OFFSET_DAY_FOR_WEEK = 7L;

	/**
	 * 上个月的偏移量
	 */
	private final static long OFFSET_MONTH_FOR_YEAR = 1;
	/**
	 * 每月第一天的起始时间
	 */
	private final static int START_DAY_OF_MONTH = 1;

	/**
	 * 获取今天0时0分0秒这个时间
	 * 
	 * @return 今天0时0分0秒这个时间
	 */
	public static LocalDateTime todayStart() {
		return getDayStart(LocalDateTime.now());
	}

	/**
	 * 获取昨天0时0分0秒这个时间
	 * 
	 * @return 昨天0时0分0秒这个时间
	 */
	public static LocalDateTime yesterdayStart() {
		return dayStart(OFFSET_DAY_FOR_YESTERDAY);
	}

	/**
	 * 获取7天前那一天0时0分0秒这个时间
	 * 
	 * @return 7天前0时0分0秒这个时间
	 */
	public static LocalDateTime weekStart() {
		return dayStart(OFFSET_DAY_FOR_WEEK);
	}

	/**
	 * 获取过去指定天数的0时0分0秒
	 * 
	 * @param offsetDays 过去的天数
	 * @return 过去指定天数的0时0分0秒
	 */
	public static LocalDateTime dayStart(long offsetDays) {
		return getDayStart(LocalDateTime.now().minusDays(offsetDays));
	}

	/**
	 * 获取本月1号0时0分0秒这个时间
	 * 
	 * @return 本月1号0时0分0秒这个时间
	 */
	public static LocalDateTime monthStart() {
		return getMonthStart(LocalDateTime.now());
	}

	/**
	 * 获取某个月之前的1号0时0分0秒这个时间
	 * 
	 * @param offset 与当前月份的偏移量
	 * @return 某个月之前的1号0时0分0秒这个时间
	 */
	public static LocalDateTime monthStart(long offset) {
		return getMonthStart(LocalDateTime.now().minusMonths(offset));
	}

	/**
	 * 获取上个月1号0时0分0秒这个时间
	 * 
	 * @return 本月1号0时0分0秒这个时间
	 */
	public static LocalDateTime lastMonthStart() {
		return getMonthStart(LocalDateTime.now().minusMonths(OFFSET_MONTH_FOR_YEAR));
	}

	/**
	 * 获取一个输入日期的0时0分0秒
	 * 
	 * @param dateTime 输入日期
	 * @return 输入日期的0时0分0秒
	 */
	public static LocalDateTime getDayStart(LocalDateTime dateTime) {
		return dateTime.withHour(START_OF_TIME).withMinute(START_OF_TIME).withSecond(START_OF_TIME)
				.withNano(START_OF_TIME);
	}

	/**
	 * 获取一个输入日期的当月1号0时0分0秒
	 * 
	 * @param dateTime 输入日期
	 * @return 输入日期的当月1号0时0分0秒
	 */
	public static LocalDateTime getMonthStart(LocalDateTime dateTime) {

		return getDayStart(dateTime.withDayOfMonth(START_DAY_OF_MONTH));

	}

}
