/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * dao层统一封装的异常类
 * @author yishui
 * @date 2018年12月8日
 * @Version 0.0.1
 */
public class DaoException extends BusinessException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4353278980810247543L;

	/**
	 * 
	 */
	public DaoException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public DaoException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public DaoException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DaoException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public DaoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

}
