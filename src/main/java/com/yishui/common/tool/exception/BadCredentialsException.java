/**
 * 
 */
package com.yishui.common.tool.exception;

/**
 * 密码不正确异常
 * @author yishui
 * @date 2018年12月10日
 * @Version 0.0.1
 */
public class BadCredentialsException extends UserException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4573358103244712163L;

	/**
	 * 
	 */
	public BadCredentialsException() {
	}

	/**
	 * @param message
	 */
	public BadCredentialsException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public BadCredentialsException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public BadCredentialsException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public BadCredentialsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
