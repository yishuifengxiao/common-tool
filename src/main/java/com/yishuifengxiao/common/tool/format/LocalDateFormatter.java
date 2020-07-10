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
 * @date 2018年7月26日
 * @Version 0.0.1
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
		try {
			return LocalDate.parse(s, DateTimeFormatter.ofPattern(datePattern));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public synchronized String print(LocalDate localDate, Locale locale) {
		return localDate.format(formatter);
	}
}