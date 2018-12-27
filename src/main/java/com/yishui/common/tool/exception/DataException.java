/**
 * 
 */
package com.yishui.common.tool.exception;

/**
 * 数据类异常
 * 
 * @author yishui
 * @date 2018年12月27日
 * @version 0.0.1
 */
public class DataException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -812063449072139564L;

	/**
	 * 
	 */
	public DataException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public DataException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public DataException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DataException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
