/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <p>
 * 逻辑异常
 * </p>
 * 主要使用到的场景有: 1 在处理业务逻辑时出现异常情况 2 一般在逻辑层使用较多
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
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


	public ServiceException(String message) {
		super(message);

	}

	public ServiceException(int errorCode, String message) {
		super(errorCode, message);
	}


	public ServiceException(Throwable cause) {
		super(cause);

	}


	public ServiceException(String message, Throwable cause) {
		super(message, cause);

	}


	public ServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
