/**
 * 
 */
package com.yishuifengxiao.common.tool.exception.data;

/**
 * 与存储记录相关的异常
 * @author yishui
 * @date 2018年12月8日
 * @Version 0.0.1
 */
public class RecordException extends DataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7647265601515104500L;

	/**
	 * 
	 */
	public RecordException() {

	}

	/**
	 * @param message
	 */
	public RecordException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public RecordException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public RecordException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public RecordException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
