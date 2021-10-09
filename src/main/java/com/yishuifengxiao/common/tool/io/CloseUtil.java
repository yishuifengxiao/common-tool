package com.yishuifengxiao.common.tool.io;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * IO流关闭工具
 * </p>
 * 该工具主要目的是优雅地关闭掉各种IO流，从而屏蔽掉因为关闭IO时强制异常捕获代码造成代码优雅性的降低
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
public class CloseUtil {

	/**
	 * 批量关闭IO流
	 * 
	 * @param closeables 需要关闭的IO流
	 */
	public synchronized static void close(Closeable... closeables) {
		if (null == closeables) {
			return;
		}
		for (Closeable closeable : closeables) {
			if (null == closeable) {
				continue;
			}
			try {
				closeable.close();
				closeable = null;
			} catch (Exception e) {
				log.debug("【易水工具】关闭流时出现问题，出现问题的原因为 {}", e.getMessage());
			}
		}
	}

	/**
	 * 关闭输入流
	 * 
	 * @param inputStream 输入流
	 */
	public synchronized static void close(InputStream inputStream) {
		if (null != inputStream) {
			try {
				inputStream.close();
				inputStream = null;
			} catch (Exception e) {
				log.debug("【易水工具】关闭输入流时出现问题，出现问题的原因为 {}", e.getMessage());
			}
		}
	}

	/**
	 * 关闭输出流
	 * 
	 * @param outputStream 输出流
	 */
	public synchronized static void close(OutputStream outputStream) {
		if (null != outputStream) {
			try {
				outputStream.close();
				outputStream = null;
			} catch (Exception e) {
				log.debug("【易水工具】关闭输出流时出现问题，出现问题的原因为 {}", e.getMessage());
			}
		}
	}

	/**
	 * 关闭一个字符流读取器
	 * 
	 * @param reader 字符流读取器
	 */
	public synchronized static void close(Reader reader) {
		if (null != reader) {
			try {
				reader.close();
				reader = null;
			} catch (Exception e) {
				log.debug("【易水工具】关闭字符流读取器时出现问题，出现问题的原因为 {}", e.getMessage());
			}
		}
	}

	/**
	 * 关闭一个字符流写入器
	 * 
	 * @param writer 字符流写入器
	 */
	public synchronized static void close(Writer writer) {
		if (null != writer) {
			try {
				writer.close();
				writer = null;
			} catch (Exception e) {
				log.debug("【易水工具】关闭字符流写入器时出现问题，出现问题的原因为 {}", e.getMessage());
			}
		}
	}

}
