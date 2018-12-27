/**
 * 
 */
package com.yishui.common.tool.exception;

/**
 * 与token相关的异常
 * @author yishui
 * @date 2018年12月8日
 * @Version 0.0.1
 */
public class TokenException extends UserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3786277841981471973L;

	/**
	 * 
	 */
	public TokenException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public TokenException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public TokenException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public TokenException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
