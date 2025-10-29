package com.yishuifengxiao.common.tool.utils;

import com.yishuifengxiao.common.tool.entity.RootEnum;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.io.CloseUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
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
public class ValidateUtils {

    /**
     * 判断给定的值是否为true,若为null或false则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isTrue(Boolean val, String msg) {
        validate(val, true, false, msg);
    }

    /**
     * 判断给定的值是否为true,若为null或false则抛出异常
     *
     * @param val      待判断的值
     * @param rootEnum 异常提示信息
     */
    public static void isTrue(Boolean val, RootEnum rootEnum) {
        validate(val, true, false, rootEnum);
    }

    /**
     * 判断给定的值是否为true或null,若为false则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isTrueOrNull(Boolean val, String msg) {
        validate(val, true, true, msg);
    }

    /**
     * 判断给定的值是否为true或null,若为false则抛出异常
     *
     * @param val      待判断的值
     * @param rootEnum 异常提示信息
     */
    public static void isTrueOrNull(Boolean val, RootEnum rootEnum) {
        validate(val, true, true, rootEnum);
    }

    /**
     * 判断给定的值是否为false,若为null或true则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isFalse(Boolean val, String msg) {
        validate(val, false, false, msg);
    }

    /**
     * 判断给定的值是否为false,若为null或true则抛出异常
     *
     * @param val      待判断的值
     * @param rootEnum 异常提示信息
     */
    public static void isFalse(Boolean val, RootEnum rootEnum) {
        validate(val, false, false, rootEnum);
    }

    /**
     * 判断给定的值是否为false或null，若为true则抛出异常
     *
     * @param val 待判断的值
     * @param msg 异常提示信息
     */
    public static void isFalseOrNull(Boolean val, String msg) {
        validate(val, false, true, msg);
    }

    /**
     * 判断给定的值是否为false或null，若为true则抛出异常
     *
     * @param val      待判断的值
     * @param rootEnum 异常提示信息
     */
    public static void isFalseOrNull(Boolean val, RootEnum rootEnum) {
        validate(val, false, true, rootEnum);
    }

    // 私有通用验证方法
    private static void validate(Boolean value, boolean expected, boolean allowNull, Object errorMsg) {
        boolean conditionMet = allowNull ? (value == null || value == expected) : (value != null && value == expected);
        if (!conditionMet) {
            if (errorMsg instanceof RootEnum) {
                throw new UncheckedException((RootEnum) errorMsg);
            } else if (errorMsg instanceof String) {
                throw new UncheckedException((String) errorMsg);
            }
        }
    }

    /**
     * 生成一个 Supplier
     *
     * @param message 提示信息
     * @return Supplier
     */
    public static Supplier<UncheckedException> orElseThrow(String message) {
        return () -> new UncheckedException(message);
    }

    /**
     * 生成一个 Supplier
     *
     * @param rootEnum 参数信息
     * @return Supplier
     */
    public static Supplier<UncheckedException> orElseThrow(RootEnum rootEnum) {
        return () -> new UncheckedException(rootEnum);
    }

    /**
     * 生成一个 Supplier
     *
     * @param code    错误码
     * @param message 提示信息
     * @return Supplier
     */
    public static Supplier<UncheckedException> orElseThrow(int code, String message) {
        return () -> new UncheckedException(code, message);
    }

/**
 * 生成一个Supplier
 *
 * @param exception 异常信息
 * @return Supplier
 */
public static Supplier<UncheckedException> orElseThrow(UncheckedException exception) {
    return () -> {
        UncheckedException newException = new UncheckedException(exception.getCode(), exception.getMessage());
        newException.setContext(exception.getContext());
        return (UncheckedException) newException.initCause(exception.getCause());
    };
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
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        CloseUtil.close(printWriter); // 只需关闭外层 PrintWriter 即可
        return writer.toString();
    }

    /**
     * 抛出一个自定义运行时异常
     *
     * @param rootEnum 异常信息
     */
    public static void throwException(RootEnum rootEnum) {
        throw new UncheckedException(rootEnum);
    }

    /**
     * 抛出一个自定义运行时异常
     *
     * @param exception 异常信息
     */
    public static void throwException(RuntimeException exception) {
        throw exception;
    }

    /**
     * 抛出一个自定义运行时异常
     *
     * @param rootEnum 异常信息
     * @param context  异常原因
     */
    public static void throwException(RootEnum rootEnum, Object context) {
        throw new UncheckedException(rootEnum).setContext(context);
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
