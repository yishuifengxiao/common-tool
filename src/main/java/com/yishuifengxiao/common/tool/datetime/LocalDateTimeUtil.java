package com.yishuifengxiao.common.tool.datetime;

import com.yishuifengxiao.common.tool.utils.OsUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * <p>
 * 基于LocalDateTime数据类型的日期时间偏移工具
 * </p>
 * 该工具的主要目的是计算距离当前日期、时间指定偏移量的时间点，主要功能如下：
 * <ol>
 * <li>获取今天的开始时间点(00:00:00)</li>
 * <li>获取昨天的开始时间点(00:00:00)和结束时间点(23:59:59)</li>
 * <li>获取前天的开始时间点(00:00:00)</li>
 * <li>获取7天前的那个时间的开始时间点(00:00:00)</li>
 * <li>获取14天前的那个时间的开始时间点(00:00:00)</li>
 * <li>获取本周一的那个时间的开始时间点(00:00:00)</li>
 * <li>获取上周一的那个时间的开始时间点(00:00:00)</li>
 * <li>获取过去指定时间的那个时间的开始时间点(00:00:00)</li>
 * <li>获取本月1号的那个时间的开始时间点(00:00:00)</li>
 * <li>获取过去指定月份的那个月份的1号的开始时间点(00:00:00)</li>
 * <li>获取过去指定年份的那个时间的1月1号的那个时间的开始时间点(00:00:00)</li>
 * </ol>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class LocalDateTimeUtil {

    /**
     * <p>
     * 获取今天0时0分0秒这个时间
     * </p>
     * <p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 00:00:00
     *
     * @return 今天0时0分0秒这个时间
     */
    public static LocalDateTime todayStart() {
        return getDayStart(LocalDateTime.now(OsUtils.ZONEID_OF_CHINA));
    }

    /**
     * <p>
     * 获取昨天0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-10-09 00:00:00
     *
     * @return 昨天0时0分0秒这个时间
     */
    public static LocalDateTime yesterdayStart() {
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
    public static LocalDateTime yesterdayEnd() {
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
    public static LocalDateTime last2DayStart() {
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
    public static LocalDateTime last7DayStart() {
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
    public static LocalDateTime last14DayStart() {
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
    public static LocalDateTime mondayStart() {
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
    public static LocalDateTime lastMondayStart() {
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
    public static LocalDateTime last2MondayStart() {
        return mondayStart(1);
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
    public static LocalDateTime mondayStart(int offsetWeeks) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(DateTimeUtil.localDateTime2Date(getMondayStart(new Date())));
        cal.add(Calendar.DATE, -7 * offsetWeeks);
        return DateTimeUtil.date2LocalDateTime(cal.getTime());
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
    public static LocalDateTime getMonday(Date date) {
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
     * <p>
     * 获取给定时间所在那一周的周一的开始时间
     * </p>
     * 例如给定的时间为 2020-11-18 12:12:12 (周三)，则返回时间的时间为 2020-11-16 00:00:00(周一)
     *
     * @param date 给定时间
     * @return 给定时间所在那一周的周一
     */
    public static LocalDateTime getMondayStart(Date date) {
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
    public static LocalDateTime dayStart(long offsetDays) {
        return getDayStart(LocalDateTime.now().minusDays(offsetDays));
    }

    /**
     * <p>
     * 获取过去指定天数的23时59分59秒
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 , offsetDays 为1 , 则返回时间为 2020-10-09 23:59:59
     *
     * @param offsetDays 过去的天数，从1开始计数，1表示是昨天
     * @return 过去指定天数的0时0分0秒
     */
    public static LocalDateTime dayEnd(long offsetDays) {
        return getDayEnd(LocalDateTime.now().minusDays(offsetDays));
    }

    /**
     * <p>
     * 获取本月1号0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 , 则返回时间为 2020-10-01 00:00:00
     *
     * @return 本月1号0时0分0秒这个时间
     */
    public static LocalDateTime monthStart() {
        return getMonthStart(LocalDateTime.now());
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
    public static LocalDateTime monthStart(long offset) {
        return getMonthStart(LocalDateTime.now().minusMonths(offset));
    }

    /**
     * <p>
     * 获取上个月1号0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-09-01 00:00:00
     *
     * @return 本月1号0时0分0秒这个时间
     */
    public static LocalDateTime lastMonthStart() {
        return getMonthStart(LocalDateTime.now().minusMonths(1L));
    }

    /**
     * <p>
     * 获取上上个月前那一天0时0分0秒这个时间
     * </p>
     * 例如当前时间为2020-10-10 12:12:12 则返回时间为 2020-08-01 00:00:00
     *
     * @return 7天前0时0分0秒这个时间
     */
    public static LocalDateTime last2MonthStart() {
        return getDayStart(LocalDateTime.now().minusMonths(2L).withDayOfMonth(1));
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
    public static LocalDateTime getDayStart(LocalDateTime dateTime) {
        if (null == dateTime) {
            return null;
        }
        return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MIN).truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * <p>
     * 获取一个输入日期的23:59:59
     * </p>
     * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-10-10 23:59:59
     *
     * @param dateTime 输入日期
     * @return 输入日期的0时0分0秒
     */
    public static LocalDateTime getDayEnd(LocalDateTime dateTime) {
        if (null == dateTime) {
            return null;
        }

        return LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MAX).truncatedTo(ChronoUnit.SECONDS);
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
    public static LocalDateTime getMonthStart(LocalDateTime dateTime) {
        if (null == dateTime) {
            return null;
        }
        return getDayStart(dateTime.withDayOfMonth(1));
    }

    /**
     * <p>
     * 获取一个输入日期的当年1月1号0时0分0秒
     * </p>
     * 例如输入时间为2020-10-10 12:12:12 则返回时间为 2020-1-1 00:00:00
     *
     * @param dateTime 输入日期
     * @return 输入日期的当月1号0时0分0秒
     */
    public static LocalDateTime getYearStart(LocalDateTime dateTime) {
        if (null == dateTime) {
            return null;
        }
        return getDayStart(dateTime.withDayOfMonth(1).withMonth(1));

    }

    /**
     * 将字符串解析为LocalDateTime形式的时间
     *
     * @param timeStr  需要解析的字符串
     * @param patterns 解析规则，当未填写解析规则时使用默认的解析规则
     * @return LocalDateTime形式的时间
     */
    public static LocalDateTime parse(String timeStr, String... patterns) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }

        String trimmedTimeStr = timeStr.trim();

        // 默认支持的日期时间格式
        String[] defaultPatterns = {
                "yyyy-MM-dd HH:mm:ss",  // 标准日期时间格式
                "yyyy-MM-dd HH:mm",     // 简化的日期时间格式
                "yyyy-MM-dd",           // 标准日期格式
                "yyyy/MM/dd HH:mm:ss",  // 斜杠分隔的日期时间格式
                "yyyy/MM/dd HH:mm",     // 斜杠分隔的简化日期时间格式
                "yyyy/MM/dd",           // 斜杠分隔的日期格式
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",  // ISO格式
                "yyyy-MM-dd'T'HH:mm:ss",      // 简化的ISO格式
                "yyyyMMddHHmmss",       // 紧凑格式
                "yyyyMMdd"              // 紧凑日期格式
        };

        // 如果用户提供了自定义格式，则使用用户提供的格式
        String[] formatPatterns = (patterns != null && patterns.length > 0) ?
                patterns : defaultPatterns;

        // 尝试使用每种格式进行解析
        for (String pattern : formatPatterns) {
            try {
                java.time.format.DateTimeFormatter formatter =
                        java.time.format.DateTimeFormatter.ofPattern(pattern);

                // 根据格式类型决定解析方式
                if (pattern.contains("HH") || pattern.contains("mm") || pattern.contains("ss")) {
                    // 包含时间部分的格式，解析为LocalDateTime
                    return LocalDateTime.parse(trimmedTimeStr, formatter);
                } else {
                    // 只包含日期部分的格式，解析为LocalDate然后转换为LocalDateTime
                    java.time.LocalDate date = java.time.LocalDate.parse(trimmedTimeStr, formatter);
                    return date.atStartOfDay();
                }
            } catch (java.time.format.DateTimeParseException e) {
                // 当前格式解析失败，尝试下一种格式
                continue;
            }
        }

        // 所有格式都尝试失败，返回null
        return null;
    }
}
