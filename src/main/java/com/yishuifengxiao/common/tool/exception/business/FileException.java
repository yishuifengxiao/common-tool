/**
 * 
 */
package com.yishuifengxiao.common.tool.exception.business;

import com.yishuifengxiao.common.tool.exception.ServiceException;

/**
 * 文件相关的异常
 * 
 * @author yishui
 * @date 2018年12月12日
 * @Version 0.0.1
 */
public class FileException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7633606666606471596L;

	/**
	 * 
	 */
	public FileException() {

	}

	/**
	 * @param message
	 */
	public FileException(String message) {
		super(message);

	}

	/**
	 * @param cause
	 */
	public FileException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public FileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
