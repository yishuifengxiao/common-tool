package com.yishuifengxiao.common.tool.utils;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.io.CloseUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * <p>
 * 异常处理工具
 * </p>
 *
 *
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ExceptionUtil {
	/**
	 * 提取出异常中的所有输出信息
	 *
	 * @param throwable 异常
	 * @return 异常中的所有输出信息
	 */
	public static synchronized String extractError(Throwable throwable) {
		if (null == throwable) {
			return null;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PrintStream printStream = new PrintStream(out);
		throwable.printStackTrace(printStream);
		String result = new String(out.toByteArray());
		CloseUtil.close(printStream, out);
		return result;
	}

	/**
	 * 抛出一个自定义运行时异常
	 *
	 * @param msg 异常信息
	 */
	public static void throwException(String msg) {
		throw new UncheckedException(msg);
	}

	/**
	 * 抛出一个自定义运行时异常
	 *
	 * @param msg 异常信息
	 * @param e   异常原因
	 */
	public static void throwException(String msg, Throwable e) {
		throw new UncheckedException(msg, e);
	}

	/**
	 * 抛出一个自定义运行时异常
	 *
	 * @param msg     异常信息
	 * @param e       异常原因
	 * @param context 异常原因
	 */
	public static void throwException(String msg, Throwable e, Object context) {
		throw new UncheckedException(msg, e).setContext(context);
	}
}
