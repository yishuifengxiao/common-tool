/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * 逻辑层通用异常
 * 
 * @author yishui
 * @date 2018年12月27日
 * @Version 0.0.1
 */
public class ServiceException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9137605901063408685L;

	/**
	 * 
	 */
	public ServiceException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public ServiceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ServiceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
