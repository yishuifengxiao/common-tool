/**
 * 
 */
package com.yishuifengxiao.common.tool.exception;

/**
 * 凭证异常<br/>
 * <br/>
 * 主要使用到的场景有:<br/>
 * 1 应该携带访问凭证的地方没有携带<br/>
 * 2 携带了一个非法的访问凭证
 * 
 * @author yishui
 * @date 2018年12月8日
 * @Version 0.0.1
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

	/**
	 * @param message
	 */
	public TokenException(String message) {
		super(message);

	}

	public TokenException(int errorCode, String message) {
		super(errorCode, message);
	}
	
	/**
	 * @param cause
	 */
	public TokenException(Throwable cause) {
		super(cause);

	}

	/**
	 * @param message
	 * @param cause
	 */
	public TokenException(String message, Throwable cause) {
		super(message, cause);

	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public TokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);

	}

}
