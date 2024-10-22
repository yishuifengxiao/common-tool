package com.yishuifengxiao.common.tool.utils;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.io.CloseUtil;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Supplier;

/**
 * <p>
 * 异常处理工具
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ExceptionUtil {


    /**
     * 判断给定的值是否为true,若为null或false则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isTrue(Boolean val, String msg) {
        if (null == val || !val) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否为true或null,若为false则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isTrueOrNull(Boolean val, String msg) {
        if (null != val && !val) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否为false,若为null或true则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isFalse(Boolean val, String msg) {
        if (null == val || val) {
            throw new UncheckedException(msg);
        }
    }

    /**
     * 判断给定的值是否为false或null，若为true则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isFalseOrNull(Boolean val, String msg) {
        if (null != val && val) {
            throw new UncheckedException(msg);
        }
    }


    /**
     * 生成一个 Supplier
     *
     * @param message 提示信息
     * @return Supplier
     */
    public static final Supplier<UncheckedException> orElseThrow(String message) {
        return () -> new UncheckedException(message);
    }

    /**
     * 生成一个 Supplier
     *
     * @param code    错误码
     * @param message 提示信息
     * @return Supplier
     */
    public static final Supplier<UncheckedException> orElseThrow(int code, String message) {
        return () -> new UncheckedException(code, message);
    }

    /**
     * 生成一个Supplier
     *
     * @param exception 异常信息
     * @return Supplier
     */
    public static final Supplier<UncheckedException> orElseThrow(UncheckedException exception) {
        return () -> exception;
    }

    /**
     * 提取出异常中的所有输出信息
     *
     * @param throwable 异常
     * @return 异常中的所有输出信息
     */
    public static String extractError(Throwable throwable) {
        if (null == throwable) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        throwable.printStackTrace(printStream);
        String result = new String(out.toByteArray());
        CloseUtil.close(printStream, out);
        return result;
    }

    /**
     * 抛出一个自定义运行时异常
     *
     * @param code 异常码
     * @param msg  异常信息
     */
    public static void throwException(Integer code, String msg) {
        throw new UncheckedException(code, msg);
    }

    /**
     * 抛出一个自定义运行时异常
     *
     * @param msg 异常信息
     */
    public static void throwException(String msg) {
        throw new UncheckedException(msg);
    }

    /**
     * 抛出一个自定义运行时异常
     *
     * @param msg 异常信息
     * @param e   异常原因
     */
    public static void throwException(String msg, Throwable e) {
        throw new UncheckedException(msg, e);
    }

    /**
     * 抛出一个自定义运行时异常
     *
     * @param msg     异常信息
     * @param e       异常原因
     * @param context 异常原因
     */
    public static void throwException(String msg, Throwable e, Object context) {
        throw new UncheckedException(msg, e).setContext(context);
    }
}
