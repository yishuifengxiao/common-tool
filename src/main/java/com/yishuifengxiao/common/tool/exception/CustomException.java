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
	protected int code;

	/**
	 * 携带的附加信息
	 */
	protected transient Object context;

	/**
	 * Constructs a new runtime exception with {@code null} as its detail message.
	 * The cause is not initialized, and may subsequently be initialized by a call
	 * to {@link #initCause}.
	 */
	public CustomException() {

	}

	/**
	 * Constructs a new runtime exception with the specified detail message. The
	 * cause is not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 *
	 * @param message the detail message. The detail message is saved for later
	 *                retrieval by the {@link #getMessage()} method.
	 */
	public CustomException(String message) {
		this(0, message);

	}

	/**
	 * Constructs a new runtime exception with the specified detail message. The
	 * cause is not initialized, and may subsequently be initialized by a call to
	 * {@link #initCause}.
	 *
	 * @param code 异常码
	 * @param message   the detail message. The detail message is saved for later
	 *                  retrieval by the {@link #getMessage()} method.
	 */
	public CustomException(int code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * Constructs a new runtime exception with the specified cause and a detail
	 * message of {@code (cause==null ? null : cause.toString())} (which typically
	 * contains the class and detail message of {@code cause}). This constructor is
	 * useful for runtime exceptions that are little more than wrappers for other
	 * throwables.
	 *
	 * @param cause the cause (which is saved for later retrieval by the
	 *              {@link #getCause()} method). (A {@code null} value is permitted,
	 *              and indicates that the cause is nonexistent or unknown.)
	 * @since 1.4
	 */
	public CustomException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message and
	 * cause.
	 * <p>
	 * Note that the detail message associated with {@code cause} is <i>not</i>
	 * automatically incorporated in this runtime exception's detail message.
	 *
	 * @param message the detail message (which is saved for later retrieval by the
	 *                {@link #getMessage()} method).
	 * @param cause   the cause (which is saved for later retrieval by the
	 *                {@link #getCause()} method). (A {@code null} value is
	 *                permitted, and indicates that the cause is nonexistent or
	 *                unknown.)
	 * @since 1.4
	 */
	public CustomException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new runtime exception with the specified detail message, cause,
	 * suppression enabled or disabled, and writable stack trace enabled or
	 * disabled.
	 *
	 * @param message            the detail message.
	 * @param cause              the cause. (A {@code null} value is permitted, and
	 *                           indicates that the cause is nonexistent or
	 *                           unknown.)
	 * @param enableSuppression  whether or not suppression is enabled or disabled
	 * @param writableStackTrace whether or not the stack trace should be writable
	 *
	 * @since 1.7
	 */
	public CustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * 获取错误码
	 *
	 * @return 错误码
	 */
	public int getCode() {
		return code;
	}

	/**
	 * 设置错误码
	 * 
	 * @param code 错误码
	 * @return 当前对象
	 */
	public CustomException setCode(int code) {
		this.code = code;
		return this;
	}

	/**
	 * 获取携带的附加信息
	 * 
	 * @return 携带的附加信息
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * 设置携带的附加信息
	 * 
	 * @param context 附加信息
	 * @return 当前对象
	 */
	public CustomException setContext(Object context) {
		this.context = context;
		return this;
	}

	/**
	 * 构建一个CustomException实例对象
	 * 
	 * @param msg 异常信息
	 * @return CustomException实例对象
	 */
	public static CustomException of(String msg) {
		return new CustomException(msg);
	}

	/**
	 * 构建一个CustomException实例对象
	 * 
	 * @param msg     异常信息
	 * @param context 附带的信息
	 * @return CustomException实例对象
	 */
	public static CustomException of(String msg, Object context) {
		CustomException exception = new CustomException(msg);
		exception.setContext(context);
		return exception;
	}
}
