/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <p>
 * 数据异常
 * <p>
 * 
 * 主要使用到的场景有: 1 期待的数据与获取到的数据不一致 2 获取到的数据里包含有非法数据
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
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

	public DataException(String message) {
		super(message);

	}

	public DataException(int errorCode, String message) {
		super(errorCode, message);
	}

	public DataException(Throwable cause) {
		super(cause);

	}

	public DataException(String message, Throwable cause) {
		super(message, cause);

	}

	public DataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
