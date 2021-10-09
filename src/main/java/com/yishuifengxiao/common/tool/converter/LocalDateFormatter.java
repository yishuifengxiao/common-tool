package com.yishuifengxiao.common.tool.converter;

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

	/**
	 * 构造函数
	 * 
	 * @param datePattern 时间格式化模式
	 */
	public LocalDateFormatter(String datePattern) {
		this.datePattern = datePattern;
		formatter = DateTimeFormatter.ofPattern(datePattern);
	}

	/**
	 * Parse a text String to produce a T.
	 * 
	 * @param text   the text string
	 * @param locale the current user locale
	 * @return an instance of T
	 * @throws ParseException           when a parse exception occurs in a java.text
	 *                                  parsing library
	 * @throws IllegalArgumentException when a parse exception occurs
	 */
	@Override
	public synchronized LocalDate parse(String text, Locale locale) throws ParseException {
		return LocalDate.parse(text, DateTimeFormatter.ofPattern(datePattern));
	}

	/**
	 * Print the object of type T for display.
	 * 
	 * @param localDate the instance to print
	 * @param locale    the current user locale
	 * @return the printed text string
	 */
	@Override
	public synchronized String print(LocalDate localDate, Locale locale) {
		if (null == localDate) {
			return null;
		}
		return localDate.format(formatter);
	}
}