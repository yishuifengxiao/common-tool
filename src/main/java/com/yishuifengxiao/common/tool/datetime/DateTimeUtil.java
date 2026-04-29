package com.yishuifengxiao.common.tool.datetime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.utils.OsUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>日期时间转换解析工具类</p>
 * <p>提供Date与LocalDateTime之间的转换、日期时间格式化与解析功能。</p>
 * <p>特性：</p>
 * <ul>
 * <li>Date与LocalDateTime双向转换</li>
 * <li>支持多种日期时间格式的解析</li>
 * <li>日期时间格式化输出</li>
 * <li>时间戳与日期时间互转</li>
 * <li>支持中国时区(UTC+8)</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class DateTimeUtil {
    /**
     * 默认的中国时区UTC+8
     */
    public final static ZoneId ZONEID_OF_CHINA = OsUtils.ZONEID_OF_CHINA;


    /**
     * 默认的日期时间的字符串形式 yyyy-MM-dd HH:mm:ss形式
     */
    public final static String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 默认的日期时间的字符串形式 yyyy-MM-dd HH:mm
     */
    public final static String SIMPLE_DATETIME_FORMAT = "yyyy-MM-dd HH:mm";

    /**
     * 默认的日期字符串形式 yyyy-MM-dd
     */
    public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 默认的日期字符串形式 yyyy/MM/dd
     */
    public final static String DEFAULT_SLASH_DATE_FORMAT = "yyyy/MM/dd";

    /**
     * 默认的完全日期字符串形式 yyyy-MM-dd'T'HH:mm:ss.SSSZ ,例如日期时间格式为 2001-07-04T12:08:56.235-0700
     */
    public final static String DEFAULT_FULL_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";


    /**
     * 获取北京时区下的当前时间
     *
     * @return 北京时区下的当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(OsUtils.ZONEID_OF_CHINA);
    }


    /**
     * 将Date转换为 LocalDateTime
     *
     * @param date 给定的时间
     * @return 转换后的时间
     */
    public static LocalDateTime date2LocalDateTime(Date date) {
        if (null == date) {
            return null;
        }
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, OsUtils.ZONEID_OF_CHINA);
    }

    /**
     * 将LocalDateTime转换为 Date
     *
     * @param localDateTime 给定的时间
     * @return 转换后的时间
     */
    public static Date localDateTime2Date(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 返回自1970年1月1日以来,由此LocalDateTime对应的 Date对象表示的00:00:00 GMT的毫秒数。
     *
     * @param localDateTime 给定的时间
     * @return 由此LocalDateTime对应的 Date对象表示的00:00:00 GMT的毫秒数。
     */
    public static Long getTime(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }

        return localDateTime2Date(localDateTime).getTime();
    }

    /**
     * 返回自1970年1月1日以来,由此 Date对象表示的00:00:00 GMT的毫秒数。
     *
     * @param date 给定的时间
     * @return 由此 Date对象表示的00:00:00 GMT的毫秒数。
     */
    public static Long getTime(Date date) {
        if (null == date) {
            return null;
        }
        return date.getTime();
    }

    /**
     * 使用从1970-01-01T00:00:00Z的时代开始的毫秒数获得一个LocalDateTime的实例。
     *
     * @param milliseconds 从1970-01-01T00:00:00Z的时代开始的毫秒数
     * @return 从1970-01-01T00:00:00Z的时代开始的毫秒数获得一个LocalDateTime的实例
     */
    public static LocalDateTime parse(long milliseconds) {
        return date2LocalDateTime(new Date(milliseconds));
    }


    /**
     * 将字符串解析为LocalDateTime形式的时间
     *
     * @param timeStr  需要解析的字符串
     * @param patterns 解析规则,当未填写解析规则时使用默认的解析规则
     * @return LocalDateTime形式的时间
     */
    public static LocalDateTime parse(String timeStr, String... patterns) {
        return date2LocalDateTime(parseDate(timeStr, patterns));
    }

    /**
     * 将字符串解析为Date形式的时间
     *
     * @param timeStr  需要解析的字符串
     * @param patterns 解析规则,当未填写解析规则时使用默认的解析规则
     * @return Date形式的时间
     */
    public static Date parseDate(String timeStr, String... patterns) {
        if (StringUtils.isBlank(timeStr)) {
            return null;
        }
        String[] formatPatterns;
        if (null == patterns || patterns.length == 0) {
            formatPatterns = new String[]{DEFAULT_DATETIME_FORMAT, DEFAULT_DATE_FORMAT,
                    SIMPLE_DATETIME_FORMAT, DEFAULT_FULL_DATE_FORMAT,
                    DEFAULT_SLASH_DATE_FORMAT};
        } else {
            // 过滤掉 null 元素
            formatPatterns = Arrays.stream(patterns).filter(Objects::nonNull).toArray(String[]::new);
        }
        try {
            return DateUtils.parseDate(timeStr.trim(), formatPatterns);
        } catch (ParseException e) {
            if (log.isDebugEnabled()) {
                log.debug("There was a problem parsing the time from the string {} according to " +
                                "the parsing rule {}, and the reason for the problem is {}",
                        timeStr, Arrays.toString(formatPatterns), e);
            }
            throw new UncheckedException(e);
        }
    }


    /**
     * 将LocalDateTime形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
     *
     * @param localDateTime 给定的时间
     * @return yyyy-MM-dd HH:mm:ss格式的字符串
     */
    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将LocalDateTime形式的时间格式化为指定形式的字符串
     *
     * @param localDateTime 给定的时间
     * @param pattern       格式化形式,例如yyyy-MM-dd HH:mm:ss
     * @return 指定形式的字符串
     */
    public static synchronized String format(LocalDateTime localDateTime, String pattern) {
        if (null == localDateTime) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return formatter.format(localDateTime);
    }

    /**
     * 将Date形式的时间格式化为指定形式的字符串
     *
     * @param date    给定的时间
     * @param pattern 格式化形式,例如yyyy-MM-dd HH:mm:ss
     * @return 指定形式的字符串
     */
    public static synchronized String format(Date date, String pattern) {
        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

    /**
     * 将Date形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
     *
     * @param date 给定的时间
     * @return yyyy-MM-dd HH:mm:ss格式的字符串
     */
    public static String format(Date date) {
        return format(date, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将Date形式的时间格式化为yyyy-MM-dd格式的字符串
     *
     * @param date 给定的时间
     * @return yyyy-MM-dd 格式的字符串
     */
    public static String formatDate(Date date) {
        return format(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 将时间戳形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
     *
     * @param currentTimeMillis 时间戳
     * @return yyyy-MM-dd HH:mm:ss 格式化后的字符串
     */
    public static String formatDate(long currentTimeMillis) {
        return format(currentTimeMillis, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将时间戳形式的时间格式化为指定形式的字符串
     *
     * @param currentTimeMillis 时间戳
     * @param pattern           格式化形式,例如yyyy-MM-dd HH:mm:ss
     * @return 指定形式的字符串
     */
    public static String format(long currentTimeMillis, String pattern) {
        return format(new Date(currentTimeMillis), pattern);
    }

    /**
     * 将LocalDateTime形式的时间格式化为yyyy-MM-dd格式的字符串
     *
     * @param localDateTime 给定的时间
     * @return yyyy-MM-dd 格式的字符串
     */
    public static String formatDate(LocalDateTime localDateTime) {
        return format(localDateTime, DEFAULT_DATE_FORMAT);
    }

}
