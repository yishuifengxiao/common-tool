/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <strong>自定义异常基类 </strong>
 * 所有程序中自定义异常的基类
 * 
 * @author yishui
 * @date 2018年12月27日
 * @Version 0.0.1
 */
public class CustomException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1825226802361294349L;

	/**
	 * 错误码
	 */
	protected int errorCode;

	/**
	 * 
	 */
	public CustomException() {

	}

	/**
	 * @param message
	 */
	public CustomException(String message) {
		this(0, message);

	}

	public CustomException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * @param cause
	 */
	public CustomException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CustomException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public CustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
