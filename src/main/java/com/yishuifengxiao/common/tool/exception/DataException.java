/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * 数据异常<br/>
 * <br/>
 * 主要使用到的场景有:<br/>
 * 1 期待的数据与获取到的数据不一致<br/>
 * 2 获取到的数据里包含有非法数据
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

	public DataException(int errorCode, String message) {
		super(errorCode, message);
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
