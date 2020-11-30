/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * 逻辑异常<br/>
 * <br/>
 * 主要使用到的场景有:<br/>
 * 1 在处理业务逻辑时出现异常情况
 * 2 一般在逻辑层使用较多
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

	}

	/**
	 * @param message
	 */
	public ServiceException(String message) {
		super(message);

	}

	public ServiceException(int errorCode, String message) {
		super(errorCode, message);
	}

	/**
	 * @param cause
	 */
	public ServiceException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
