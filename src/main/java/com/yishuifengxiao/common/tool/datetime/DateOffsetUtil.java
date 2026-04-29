package com.yishuifengxiao.common.tool.datetime;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import com.yishuifengxiao.common.tool.utils.OsUtils;

/**
 * <p>Date日期时间偏移工具类</p>
 * <p>基于Date类型提供日期时间偏移计算功能，用于获取特定时间点。</p>
 * <p>特性：</p>
 * <ul>
 * <li>获取某天的开始(00:00:00)和结束(23:59:59)时间</li>
 * <li>支持日期偏移计算(昨天、前天、7天前等)</li>
 * <li>支持周偏移计算(本周一、上周一等)</li>
 * <li>支持月偏移计算(本月初、上月初等)</li>
 * <li>支持年偏移计算(年初时间)</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class DateOffsetUtil {

    /**
     * <p>
     * 获取今天0时0分0秒这个时间
     * </p>
     * <p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 00:00:00
     *
     * @return 今天0时0分0秒这个时间
     */
    public static Date todayStart() {
        return getDayStart(DateTimeUtil.localDateTime2Date(LocalDateTime.now(OsUtils.ZONEID_OF_CHINA)));
    }

    /**
     * <p>
     * 获取昨天0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-09 00:00:00
     *
     * @return 昨天0时0分0秒这个时间
     */
    public static Date yesterdayStart() {
        return dayStart(1L);
    }

    /**
     * <p>
     * 获取昨天23时59分59秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-09 23:59:59
     *
     * @return 昨天0时0分0秒这个时间
     */
    public static Date yesterdayEnd() {
        return dayEnd(1L);
    }

    /**
     * <p>
     * 获取前天0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-09 00:00:00
     *
     * @return 前天0时0分0秒这个时间
     */
    public static Date last2DayStart() {
        return dayStart(2L);
    }

    /**
     * <p>
     * 获取7天前那一天0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-03 00:00:00
     *
     * @return 7天前0时0分0秒这个时间
     */
    public static Date last7DayStart() {
        return dayStart(7L);
    }

    /**
     * <p>
     * 获取14天前那一天0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-15 12:12:12 则返回时间为 2020-10-01 00:00:00
     *
     * @return 7天前0时0分0秒这个时间
     */
    public static Date last14DayStart() {
        return dayStart(14L);
    }

    /**
     * <p>
     * 获取本周一周一的开始时间
     * </p>
     * 例如当前时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-16 00:00:00(周一)
     *
     * @return 本周一周一的开始时间
     */
    public static Date mondayStart() {
        return getMondayStart(new Date());
    }

    /**
     * <p>
     * 获取上周的周一的开始时间
     * </p>
     * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-09 00:00:00(周一)
     *
     * @return 上周的周一的开始时间
     */
    public static Date lastMondayStart() {
        return mondayStart(1);
    }

    /**
     * <p>
     * 获取上上周的周一的开始时间
     * </p>
     * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-02 00:00:00(周一)
     *
     * @return 上上周的周一的开始时间
     */
    public static Date last2MondayStart() {
        return mondayStart(2);
    }

    /**
     * <p>
     * 获取上几周的周一的开始时间
     * </p>
     * 例如给定的时间为 2020-11-18 12:12:12 (周三)，offsetWeeks 为1 ，则返回时间的时间为 2020-11-09
     * 00:00:00(周一)
     *
     * @param offsetWeeks 偏移的周数，1表示是上周，2表示是上上周
     * @return 上几周的周一的开始时间
     */
    public static Date mondayStart(int offsetWeeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getMondayStart(new Date()));
        calendar.add(Calendar.DATE, -7 * offsetWeeks);
        return calendar.getTime();
    }

    /**
     * <p>
     * 获取给定时间所在那一周的周一
     * </p>
     * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-16 12:12:12(周一)
     *
     * @param date 给定的时间
     * @return 给定时间所在那一周的周一
     */
    public static Date getMonday(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (1 == dayOfWeek) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        calendar.add(Calendar.DATE, calendar.getFirstDayOfWeek() - currentDayOfWeek);
        return calendar.getTime();
    }

    /**
     * <p>
     * 获取给定时间所在那一周的周一的开始时间
     * </p>
     * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-16 00:00:00(周一)
     *
     * @param date 给定的时间
     * @return 给定时间所在那一周的周一
     */
    public static Date getMondayStart(Date date) {
        return getDayStart(getMonday(date));
    }

    /**
     * <p>
     * 获取过去指定天数的0时0分0秒
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 , offsetDays 为1 , 则返回时间为 2020-10-09 00:00:00
     *
     * @param offsetDays 过去的天数，从1开始计数，1表示是昨天
     * @return 过去指定天数的0时0分0秒
     */
    public static Date dayStart(long offsetDays) {

        return getDayStart(DateTimeUtil.localDateTime2Date(LocalDateTime.now().minusDays(offsetDays)));
    }

    /**
     * <p>
     * 获取过去指定天数的23时59分59秒
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 , offsetDays 为1 , 则返回时间为 2020-10-09 23:59:59
     *
     * @param offsetDays 过去的天数，从1开始计数，1表示是昨天
     * @return 过去指定天数的23时59分59秒
     */
    public static Date dayEnd(long offsetDays) {

        return getDayEnd(DateTimeUtil.localDateTime2Date(LocalDateTime.now().minusDays(offsetDays)));
    }

    /**
     * <p>
     * 获取本月1号0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 , 则返回时间为 2020-10-01 00:00:00
     *
     * @return 本月1号0时0分0秒这个时间
     */
    public static Date monthStart() {

        return getMonthStart(DateTimeUtil.localDateTime2Date(LocalDateTime.now()));
    }

    /**
     * <p>
     * 获取某个月之前的1号0时0分0秒这个时间
     * </p>
     * * 例如当前时间为2020-10-10 12:12:12 , offset 为1 , 则返回时间为 2020-09-01 00:00:00
     *
     * @param offset 与当前月份的偏移量 ,从1开始计数，1表示上个月
     * @return 某个月之前的1号0时0分0秒这个时间
     */
    public static Date monthStart(long offset) {

        return getMonthStart(DateTimeUtil.localDateTime2Date(LocalDateTime.now().minusMonths(offset)));
    }

    /**
     * <p>
     * 获取上个月1号0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-09-01 00:00:00
     *
     * @return 上个月1号0时0分0秒这个时间
     */
    public static Date lastMonthStart() {

        return getMonthStart(DateTimeUtil.localDateTime2Date(LocalDateTime.now().minusMonths(1L)));
    }

    /**
     * <p>
     * 获取上上个月前那一天0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-08-01 00:00:00
     *
     * @return 上上月1号0时0分0秒这个时间
     */
    public static Date last2MonthStart() {

        return getDayStart(DateTimeUtil.localDateTime2Date(LocalDateTime.now().minusMonths(2L).withDayOfMonth(1)));
    }

    /**
     * <p>
     * 获取一个输入日期的0时0分0秒
     * </p>
     * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 00:00:00
     *
     * @param dateTime 输入日期
     * @return 输入日期的0时0分0秒
     */
    public static Date getDayStart(Date dateTime) {
        if (null == dateTime) {
            return null;
        }

        return DateTimeUtil.localDateTime2Date(LocalDateTime.of(dateTime.toInstant().atZone(OsUtils.ZONEID_OF_CHINA).toLocalDate(), LocalTime.MIN));
    }

    /**
     * <p>
     * 获取一个输入日期的23:59:59
     * </p>
     * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 23:59:59
     *
     * @param dateTime 输入日期
     * @return 输入日期的23时59分59秒
     */
    public static Date getDayEnd(Date dateTime) {
        if (null == dateTime) {
            return null;
        }
        return DateTimeUtil.localDateTime2Date(LocalDateTime.of(dateTime.toInstant().atZone(OsUtils.ZONEID_OF_CHINA).toLocalDate(), LocalTime.MAX));
    }

    /**
     * <p>
     * 获取一个输入日期的当月1号0时0分0秒
     * </p>
     * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-10-1 00:00:00
     *
     * @param dateTime 输入日期
     * @return 输入日期的当月1号0时0分0秒
     */
    public static Date getMonthStart(Date dateTime) {
        if (null == dateTime) {
            return null;
        }

        return getDayStart(DateTimeUtil.localDateTime2Date(DateTimeUtil.date2LocalDateTime(dateTime).withDayOfMonth(1)));
    }

    /**
     * <p>
     * 获取一个输入日期的当年1月1号0时0分0秒
     * </p>
     * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-1-1 00:00:00
     *
     * @param dateTime 输入日期
     * @return 输入日期的当年1月1号0时0分0秒
     */
    public static Date getYearStart(Date dateTime) {
        if (null == dateTime) {
            return null;
        }

        return getDayStart(DateTimeUtil.localDateTime2Date(DateTimeUtil.date2LocalDateTime(dateTime).withDayOfMonth(1).withMonth(1)));

    }

}
