/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * 与验证相关的异常类
 * @author yishui
 * @date 2018年12月8日
 * @Version 0.0.1
 */
public class ValidateException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7804292048699195317L;

	/**
	 * 
	 */
	public ValidateException() {

	}

	/**
	 * @param message
	 */
	public ValidateException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public ValidateException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public ValidateException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ValidateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
