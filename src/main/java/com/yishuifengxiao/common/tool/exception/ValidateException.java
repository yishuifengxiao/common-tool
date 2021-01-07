/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <p>
 * 验证异常
 * </p>
 * 主要使用到的场景有: 1 在进行业务校验逻辑时出现问题
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ValidateException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7804292048699195317L;

	public ValidateException() {

	}

	public ValidateException(String message) {
		super(message);

	}

	public ValidateException(int errorCode, String message) {
		super(errorCode, message);
	}

	public ValidateException(Throwable cause) {
		super(cause);

	}

	public ValidateException(String message, Throwable cause) {
		super(message, cause);

	}

	public ValidateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
