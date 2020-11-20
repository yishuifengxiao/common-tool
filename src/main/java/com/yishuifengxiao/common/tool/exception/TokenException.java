/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * 与token相关的异常
 * @author yishui
 * @date 2018年12月8日
 * @Version 0.0.1
 */
public class TokenException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3786277841981471973L;

	/**
	 * 
	 */
	public TokenException() {

	}

	/**
	 * @param message
	 */
	public TokenException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public TokenException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public TokenException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
