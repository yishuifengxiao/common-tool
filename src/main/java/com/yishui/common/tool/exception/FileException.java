/**
 * 
 */
package com.yishui.common.tool.exception;

/**
 * 文件相关的异常
 * @author yishui
 * @date 2018年12月12日
 * @Version 0.0.1
 */
public class FileException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7633606666606471596L;

	/**
	 * 
	 */
	public FileException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public FileException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public FileException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public FileException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public FileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
