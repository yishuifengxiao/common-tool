/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * 自定义异常
 * @author yishui
 * @date 2018年12月27日
 * @Version 0.0.1
 */
public class CustomException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1825226802361294349L;

	/**
	 * 
	 */
	public CustomException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public CustomException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public CustomException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CustomException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}