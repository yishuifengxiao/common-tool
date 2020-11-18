package com.yishuifengxiao.common.tool.datetime;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期时间偏移工具类
 * 
 * @author yishui
 * @date 2020年10月19日
 * @Version 0.0.1
 */
public class DateOffsetUtil {

	/**
	 * 获取今天0时0分0秒这个时间<br/>
	 * 
	 * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 00:00:00
	 * 
	 * @return 今天0时0分0秒这个时间
	 */
	public synchronized static LocalDateTime todayStart() {
		return getDayStart(LocalDateTime.now());
	}

	/**
	 * 获取昨天0时0分0秒这个时间 <br/>
	 * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-09 00:00:00
	 * 
	 * @return 昨天0时0分0秒这个时间
	 */
	public synchronized static LocalDateTime yesterdayStart() {
		return dayStart(1L);
	}

	/**
	 * 获取昨天23时59分59秒这个时间 <br/>
	 * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-09 23:59:59
	 * 
	 * @return 昨天0时0分0秒这个时间
	 */
	public synchronized static LocalDateTime yesterdayEnd() {
		return dayEnd(1L);
	}

	/**
	 * 获取前天0时0分0秒这个时间<br/>
	 * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-09 00:00:00
	 * 
	 * @return 前天0时0分0秒这个时间
	 */
	public static LocalDateTime last2DayStart() {
		return dayStart(2L);
	}

	/**
	 * 获取7天前那一天0时0分0秒这个时间<br/>
	 * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-03 00:00:00
	 * 
	 * @return 7天前0时0分0秒这个时间
	 */
	public static LocalDateTime last7DayStart() {
		return dayStart(7L);
	}

	/**
	 * 获取14天前那一天0时0分0秒这个时间<br/>
	 * 例如当前时间为2020-10-15 12:12:12 则返回时间为 2020-10-01 00:00:00
	 * 
	 * @return 7天前0时0分0秒这个时间
	 */
	public static LocalDateTime last14DayStart() {
		return dayStart(14L);
	}

	/**
	 * 获取本周一周一的开始时间<br/>
	 * 例如当前时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-16 00:00:00(周一)
	 * 
	 * @return
	 */
	public static LocalDateTime mondayStart() {
		return getMondayStart(new Date());
	}

	/**
	 * 获取上周的周一的开始时间<br/>
	 * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-09 00:00:00(周一)
	 * 
	 * @return 上周的周一的开始时间
	 */
	public static LocalDateTime lastMondayStart() {
		return mondayStart(1);
	}
	

	/**
	 * 获取上上周的周一的开始时间<br/>
	 * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-02 00:00:00(周一)
	 * 
	 * @return 上上周的周一的开始时间
	 */
	public static LocalDateTime last2MondayStart() {
		return mondayStart(1);
	}

	/**
	 * 获取上几周的周一的开始时间<br/>
	 * 例如给定的时间为 2020-11-18 12:12:12 (周三)，offsetWeeks 为1 ，则返回时间的时间为 2020-11-09 00:00:00(周一)
	 * 
	 * @param offsetWeeks 偏移的周数，1表示是上周，2表示是上上周
	 * @return 上几周的周一的开始时间
	 */
	public static LocalDateTime mondayStart(int offsetWeeks) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(DateTimeUtil.localDateTime2Date(getMondayStart(new Date())));
		cal.add(Calendar.DATE, -7 * offsetWeeks);
		return DateTimeUtil.date2LocalDateTime(cal.getTime());
	}

	/**
	 * 获取给定时间所在那一周的周一<br/>
	 * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-16 12:12:12(周一)
	 * 
	 * @param date
	 * @return 给定时间所在那一周的周一
	 */
	public synchronized static LocalDateTime getMonday(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		// 获得当前日期是一个星期的第几天
		int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (1 == dayWeek) {
			cal.add(Calendar.DAY_OF_MONTH, -1);
		}
		// 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
		cal.setFirstDayOfWeek(Calendar.MONDAY);
		// 获得当前日期是一个星期的第几天
		int day = cal.get(Calendar.DAY_OF_WEEK);
		// 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
		cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
		return DateTimeUtil.date2LocalDateTime(cal.getTime());
	}

	/**
	 * 获取给定时间所在那一周的周一的开始时间<br/>
	 * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-16 00:00:00(周一)
	 * 
	 * @param date
	 * @return 给定时间所在那一周的周一
	 */
	public synchronized static LocalDateTime getMondayStart(Date date) {
		return getDayStart(getMonday(date));
	}

	/**
	 * 获取过去指定天数的0时0分0秒<br/>
	 * 例如当前时间为2020-10-10 12:12:12 , offsetDays 为1 , 则返回时间为 2020-10-09 00:00:00
	 * 
	 * @param offsetDays 过去的天数，从1开始计数，1表示是昨天
	 * @return 过去指定天数的0时0分0秒
	 */
	public synchronized static LocalDateTime dayStart(long offsetDays) {
		return getDayStart(LocalDateTime.now().minusDays(offsetDays));
	}

	/**
	 * 获取过去指定天数的23时59分59秒<br/>
	 * 例如当前时间为2020-10-10 12:12:12 , offsetDays 为1 , 则返回时间为 2020-10-09 23:59:59
	 * 
	 * @param offsetDays 过去的天数，从1开始计数，1表示是昨天
	 * @return 过去指定天数的0时0分0秒
	 */
	public synchronized static LocalDateTime dayEnd(long offsetDays) {
		return getDayEnd(LocalDateTime.now().minusDays(offsetDays));
	}

	/**
	 * 获取本月1号0时0分0秒这个时间<br/>
	 * 例如当前时间为2020-10-10 12:12:12 , 则返回时间为 2020-10-01 00:00:00
	 * 
	 * @return 本月1号0时0分0秒这个时间
	 */
	public static LocalDateTime monthStart() {
		return getMonthStart(LocalDateTime.now());
	}

	/**
	 * 获取某个月之前的1号0时0分0秒这个时间<br/>
	 * * 例如当前时间为2020-10-10 12:12:12 , offset 为1 , 则返回时间为 2020-09-01 00:00:00
	 * 
	 * @param offset 与当前月份的偏移量 ,从1开始计数，1表示上个月
	 * @return 某个月之前的1号0时0分0秒这个时间
	 */
	public synchronized static LocalDateTime monthStart(long offset) {
		return getMonthStart(LocalDateTime.now().minusMonths(offset));
	}

	/**
	 * 获取上个月1号0时0分0秒这个时间<br/>
	 * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-09-01 00:00:00
	 * 
	 * @return 本月1号0时0分0秒这个时间
	 */
	public static LocalDateTime lastMonthStart() {
		return getMonthStart(LocalDateTime.now().minusMonths(1L));
	}

	/**
	 * 获取上上个月前那一天0时0分0秒这个时间<br/>
	 * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-08-01 00:00:00
	 * 
	 * @return 7天前0时0分0秒这个时间
	 */
	public static LocalDateTime last2MonthStart() {
		return getDayStart(LocalDateTime.now().minusMonths(2L).withDayOfMonth(1));
	}

	/**
	 * 获取一个输入日期的0时0分0秒<br/>
	 * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 00:00:00
	 * 
	 * @param dateTime 输入日期
	 * @return 输入日期的0时0分0秒
	 */
	public synchronized static LocalDateTime getDayStart(LocalDateTime dateTime) {
		if (null == dateTime) {
			return null;
		}
		return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
	}

	/**
	 * 获取一个输入日期的23:59:59<br/>
	 * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 23:59:59
	 * 
	 * @param dateTime 输入日期
	 * @return 输入日期的0时0分0秒
	 */
	public synchronized static LocalDateTime getDayEnd(LocalDateTime dateTime) {
		if (null == dateTime) {
			return null;
		}
		return dateTime.withHour(23).withMinute(59).withSecond(59).withNano(0);
	}

	/**
	 * 获取一个输入日期的当月1号0时0分0秒<br/>
	 * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-10-1 00:00:00
	 * 
	 * @param dateTime 输入日期
	 * @return 输入日期的当月1号0时0分0秒
	 */
	public synchronized static LocalDateTime getMonthStart(LocalDateTime dateTime) {
		if (null == dateTime) {
			return null;
		}
		return getDayStart(dateTime.withDayOfMonth(1));
	}

	/**
	 * 获取一个输入日期的当年1月1号0时0分0秒 <br/>
	 * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-1-1 00:00:00
	 * 
	 * @param dateTime 输入日期
	 * @return 输入日期的当月1号0时0分0秒
	 */
	public synchronized static LocalDateTime getYearStart(LocalDateTime dateTime) {
		if (null == dateTime) {
			return null;
		}
		return getDayStart(dateTime.withDayOfMonth(1).withMonth(1));

	}

}
