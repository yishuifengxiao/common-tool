/**
 * 
 */
package com.yishui.common.tool.exception;

/**
 * 用户相关的异常
 * @author yishui
 * @date 2018年12月8日
 * @Version 0.0.1
 */
public class UserException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2971034148624563109L;

	/**
	 * 
	 */
	public UserException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public UserException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public UserException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
