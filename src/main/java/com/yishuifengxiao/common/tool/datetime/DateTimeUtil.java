/**
 *
 */
package com.yishuifengxiao.common.tool.datetime;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.exception.constant.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * <p>
 * 时间转换解析工具
 * </p>
 * 该工具的主要作用是将LocalDateTime和Date形式的时间互相转换以及将字符串格式的时间和日期时间互相转换。该工具是一个线程安全类的工具，其主要的功能如下：
 * <ol>
 * <li>将LocalDateTime和Date形式的时间互相转换</li>
 * <li>将指定格式的字符串按解析为日期时间</li>
 * <li>将日期时间按照指定的格式转换成字符串</li>
 * </ol>
 *
 * <p>
 * <strong>该工具是一个线程安全类的工具</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class DateTimeUtil {

    /**
     * 默认的时区,上海
     */
    public final static String DEFAULT_ZONE = "Asia/Shanghai";

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
     * @param date 给定的时间
     * @return 转换后的时间
     */
    public static synchronized LocalDateTime date2LocalDateTime(Date date) {
        if (null == date) {
            return null;
        }
        Instant instant = date.toInstant();
        return LocalDateTime.ofInstant(instant, zoneIdOfChina());
    }

    /**
     * 将LocalDateTime转换为 Date
     *
     * @param localDateTime 给定的时间
     * @return 转换后的时间
     */
    public static synchronized Date localDateTime2Date(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 返回自1970年1月1日以来，由此LocalDateTime对应的 Date对象表示的00:00:00 GMT的毫秒 数 。
     *
     * @param localDateTime 给定的时间
     * @return 由此LocalDateTime对应的 Date对象表示的00:00:00 GMT的毫秒 数 。
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
     * @param date 给定的时间
     * @return 由此 Date对象表示的00:00:00 GMT的毫秒 数 。
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
     * @return 从1970-01-01T00：00：00Z的时代开始的毫秒 数获得一个LocalDateTime的实例
     */
    public static synchronized LocalDateTime getLocalDateTime(long milliseconds) {
        return date2LocalDateTime(new Date(milliseconds));
    }

    /**
     * <p>
     * 将字符串解析为LocalDateTime 形式的时间
     * </p>
     *
     * 默认采用yyyy-MM-dd HH:mm:ss 或 yyyy-MM-dd HH:mm 或 yyyy-MM-dd 形式解析
     *
     * @param timeStr 需要解析的字符串
     *
     * @return LocalDateTime形式的时间
     */
    public static synchronized LocalDateTime parse(String timeStr) {
        return parse(timeStr, DEFAULT_DATETIME_FORMAT, DEFAULT_DATE_FORMAT, SIMPLE_DATETIME_FORMAT);
    }

    /**
     * 将字符串解析为LocalDateTime 形式的时间
     *
     * @param timeStr  需要解析的字符串
     * @param patterns 解析规则 当未填写解析规则时
     * @return LocalDateTime形式的时间
     */
    public static synchronized LocalDateTime parse(String timeStr, String... patterns) {

        try {
            return date2LocalDateTime(DateUtils.parseDate(timeStr, patterns));
        } catch (ParseException e) {
            log.info("【易水工具】按照解析规则 {} 从字符串 {} 中解析出时间时出现问题，出现问题的原因为{}", patterns, timeStr, e.getMessage());
            throw new UncheckedException(ErrorCode.PARAM_FORMAT_ERROR, "从字符串中解析时间失败");
        }
    }

    /**
     * 将LocalDateTime形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
     *
     * @param localDateTime 给定的时间
     * @return yyyy-MM-dd HH:mm:ss格式的字符串
     */
    public static synchronized String format(LocalDateTime localDateTime) {
        return format(localDateTime, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将LocalDateTime形式的时间格式化为 格式化为指定形式的字符串
     *
     * @param localDateTime 给定的时间
     * @param pattern       格式化形式，例如yyyy-MM-dd HH:mm:ss
     * @return 指定形式的字符串
     */
    public static synchronized String format(LocalDateTime localDateTime, String pattern) {
        if (null == localDateTime) {
            return null;
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return format.format(localDateTime);
    }

    /**
     * 将Date形式的时间格式化为 格式化为指定形式的字符串
     *
     * @param date    给定的时间
     * @param pattern 格式化形式，例如yyyy-MM-dd HH:mm:ss
     * @return 指定形式的字符串
     */
    public static synchronized String format(Date date, String pattern) {
        if (null == date) {
            return null;
        }
        DateTimeFormatter format = DateTimeFormatter.ofPattern(pattern);
        return format.format(date2LocalDateTime(date));
    }

    /**
     * 将Date形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
     *
     * @param date 给定的时间
     * @return yyyy-MM-dd HH:mm:ss格式的字符串
     */
    public static synchronized String format(Date date) {
        return format(date, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将Date形式的时间格式化为yyyy-MM-dd格式的字符串
     *
     * @param date 给定的时间
     * @return yyyy-MM-dd 格式的字符串
     */
    public static synchronized String formatDate(Date date) {
        return format(date, DEFAULT_DATE_FORMAT);
    }

    /**
     * 将时间戳形式的时间格式化为yyyy-MM-dd HH:mm:ss格式的字符串
     *
     * @param currentTimeMillis 时间戳
     * @return yyyy-MM-dd HH:mm:ss 格式化后的字符串
     */
    public static synchronized String formatDate(long currentTimeMillis) {
        return format(currentTimeMillis, DEFAULT_DATETIME_FORMAT);
    }

    /**
     * 将Date形式的时间格式化为 格式化为指定形式的字符串
     *
     * @param currentTimeMillis 时间戳
     * @param pattern 格式化形式，例如yyyy-MM-dd HH:mm:ss
     * @return 指定形式的字符串
     */
    public static synchronized String format(long currentTimeMillis, String pattern) {
        return format(new Date(currentTimeMillis), pattern);
    }

    /**
     * 将Date形式的时间格式化为yyyy-MM-dd格式的字符串
     *
     * @param localDateTime 给定的时间
     * @return yyyy-MM-dd 格式的字符串
     */
    public static synchronized String formatDate(LocalDateTime localDateTime) {
        return format(localDateTime, DEFAULT_DATE_FORMAT);
    }

}
