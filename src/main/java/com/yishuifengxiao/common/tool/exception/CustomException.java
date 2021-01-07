/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <p>
 * 自定义异常基类
 * </p>
 * 所有程序中自定义异常的基类
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
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

	public CustomException() {

	}

	public CustomException(String message) {
		this(0, message);

	}

	public CustomException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public CustomException(Throwable cause) {
		super(cause);
	}

	public CustomException(String message, Throwable cause) {
		super(message, cause);
	}

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
