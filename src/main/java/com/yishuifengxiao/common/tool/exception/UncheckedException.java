/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <p>
 * 自定义运行时异常基类
 * </p>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class UncheckedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9071897872977285359L;

	/**
	 * 错误码
	 */
	protected int errorCode;

	public UncheckedException() {

	}

	public UncheckedException(int errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public UncheckedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

	public UncheckedException(String message, Throwable cause) {
		super(message, cause);
	}

	public UncheckedException(String message) {
		super(message);
	}

	public UncheckedException(Throwable cause) {
		super(cause);
	}

	public UncheckedException(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * 获取错误码
	 * 
	 * @return 错误码
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * 设置错误码
	 * 
	 * @param errorCode 错误码
	 */
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
