package com.yishuifengxiao.common.tool.format;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.format.Formatter;

/**
 * 字符串与LocalDate转化类
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class LocalDateFormatter implements Formatter<LocalDate> {
	private DateTimeFormatter formatter;
	private String datePattern;

	public LocalDateFormatter(String datePattern) {
		this.datePattern = datePattern;
		formatter = DateTimeFormatter.ofPattern(datePattern);
	}

	@Override
	public synchronized LocalDate parse(String s, Locale locale) throws ParseException {
		return LocalDate.parse(s, DateTimeFormatter.ofPattern(datePattern));
	}

	@Override
	public synchronized String print(LocalDate localDate, Locale locale) {
		if (null == localDate) {
			return null;
		}
		return localDate.format(formatter);
	}
}