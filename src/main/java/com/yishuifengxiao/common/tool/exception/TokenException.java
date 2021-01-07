/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * <p>
 * 凭证异常
 * </p>
 * 主要使用到的场景有: 1 应该携带访问凭证的地方没有携带 2 携带了一个非法的访问凭证
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TokenException extends CustomException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3786277841981471973L;

	/**
	 * 
	 */
	public TokenException() {

	}


	public TokenException(String message) {
		super(message);

	}

	public TokenException(int errorCode, String message) {
		super(errorCode, message);
	}


	public TokenException(Throwable cause) {
		super(cause);

	}


	public TokenException(String message, Throwable cause) {
		super(message, cause);

	}


	public TokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
