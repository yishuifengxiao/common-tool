/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <p>
 * 用户异常
 * </p>
 * 
 * 主要使用到的场景有: 1 获取用户信息失败 2 根据用户信息进行校验时发生了异常
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class UserException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2971034148624563109L;

	public UserException() {

	}

	public UserException(String message) {
		super(message);

	}

	public UserException(int errorCode, String message) {
		super(errorCode, message);
	}

	public UserException(Throwable cause) {
		super(cause);

	}

	public UserException(String message, Throwable cause) {
		super(message, cause);

	}

	public UserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
