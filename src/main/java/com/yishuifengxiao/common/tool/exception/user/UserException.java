/**
 * 
 */
package com.yishuifengxiao.common.tool.exception.user;

import com.yishuifengxiao.common.tool.exception.CustomException;

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

	}

	/**
	 * @param message
	 */
	public UserException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public UserException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public UserException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public UserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
