/**
 * 
 */
package com.yishuifengxiao.common.tool.exception.business;

import com.yishuifengxiao.common.tool.exception.ServiceException;

/**
 * 业务类异常
 * 
 * @author yishui
 * @date 2018年12月27日
 * @version 0.0.1
 */
public class BusinessException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1685175094020913929L;

	public BusinessException() {
		super();
	}

	public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public BusinessException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusinessException(String message) {
		super(message);

	}

	public BusinessException(Throwable cause) {
		super(cause);
	}

}
