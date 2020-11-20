/**
 * 
 */
package com.yishuifengxiao.common.tool.exception.data;

import com.yishuifengxiao.common.tool.exception.CustomException;

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

	}

	/**
	 * @param message
	 */
	public DataException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public DataException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public DataException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
