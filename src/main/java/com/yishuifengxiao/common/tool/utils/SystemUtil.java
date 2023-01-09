package com.yishuifengxiao.common.tool.utils;

import com.yishuifengxiao.common.tool.exception.UncheckedException;

/**
 * 系统工具类
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
public final class SystemUtil {

    /**
     * 获取操作系统的名字
     */
    private static String OS = System.getProperty("os.name").toLowerCase();

    /**
     * 左斜杠 (英文输入法 / )
     */
    public final static String LEFT_SLASH = "/";

    /**
     * 右斜杠 (英文输入法 \ )
     */
    public final static String RIGHT_SLASH = "\\";

    /**
     * 点 (英文输入法 . )
     */
    public final static String SPOT = ".";
    /**
     * 冒号 (英文输入法 : )
     */
    public final static String COLON = ":";

    /**
     * 分隔符 &#38;
     */
    public final static String SEPARATOR_AND = "&";

    /**
     * 下划线 (英文输入法 _ )
     */
    public final static String SEPARATOR_UNDERLINE = "_";

    /**
     * 问号 (英文输入法 ? )
     */
    public final static String SEPARATOR_QUESTION_MARK = "?";

    /**
     * 休眠指定的时间
     *
     * @param millis 休眠时间，单位毫秒
     * @return 是否执行成功
     */
    public static boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * <p>
     * 根据操作系统获取路径分隔符
     * </p>
     *
     * <p>
     * windows下返回左斜杠<code>/</code>,其他环境下返回右斜杠<code>\</code>
     * </p>
     *
     * @return 路径分隔符
     */
    public static String pathSeparator() {
        return isWindows() ? RIGHT_SLASH : LEFT_SLASH;
    }

    /**
     * 是否为linux操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isLinux() {
        return OS.indexOf("linux") >= 0;
    }

    /**
     * 是否为MacOS操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isMacOs() {
        return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") < 0;
    }

    /**
     * 是否为MacOSX操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isMacOsX() {
        return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
    }

    /**
     * 是否为windows操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isWindows() {
        return OS.indexOf("windows") >= 0;
    }

    /**
     * 是否为OS2操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isOs2() {
        return OS.indexOf("os/2") >= 0;
    }

    /**
     * 是否为solaris操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isSolaris() {
        return OS.indexOf("solaris") >= 0;
    }

    /**
     * 是否为sunos操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isSunOs() {
        return OS.indexOf("sunos") >= 0;
    }

    /**
     * 是否为MPEiX操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isMpeIx() {
        return OS.indexOf("mpe/ix") >= 0;
    }

    /**
     * 是否为HPUX操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isHpUx() {
        return OS.indexOf("hp-ux") >= 0;
    }

    /**
     * 是否为aix操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isAix() {
        return OS.indexOf("aix") >= 0;
    }

    /**
     * 是否为OS390操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isOs390() {
        return OS.indexOf("os/390") >= 0;
    }

    /**
     * 是否为freebsd操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isFreeBsd() {
        return OS.indexOf("freebsd") >= 0;
    }

    /**
     * 是否为irix操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isIrix() {
        return OS.indexOf("irix") >= 0;
    }

    /**
     * 是否为DigitalUnix操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isDigitalUnix() {
        return OS.indexOf("digital") >= 0 && OS.indexOf("unix") > 0;
    }

    /**
     * 是否为netware操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isNetWare() {
        return OS.indexOf("netware") >= 0;
    }

    /**
     * 是否为osf1操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isOsf1() {
        return OS.indexOf("osf1") >= 0;
    }

    /**
     * 是否为openvms操作系统
     *
     * @return 若是返回为true, 否则为false
     */
    public static boolean isOpenVms() {
        return OS.indexOf("openvms") >= 0;
    }

    /**
     * 获取操作系统类型
     *
     * @return 操作系统类型
     */
    public static Platform getOsName() {
        Platform platform = null;
        if (isAix()) {
            platform = Platform.AIX;
        } else if (isDigitalUnix()) {
            platform = Platform.Digital_Unix;
        } else if (isFreeBsd()) {
            platform = Platform.FreeBSD;
        } else if (isHpUx()) {
            platform = Platform.HP_UX;
        } else if (isIrix()) {
            platform = Platform.Irix;
        } else if (isLinux()) {
            platform = Platform.Linux;
        } else if (isMacOs()) {
            platform = Platform.Mac_OS;
        } else if (isMacOsX()) {
            platform = Platform.Mac_OS_X;
        } else if (isMpeIx()) {
            platform = Platform.MPEiX;
        } else if (isNetWare()) {
            platform = Platform.NetWare_411;
        } else if (isOpenVms()) {
            platform = Platform.OpenVMS;
        } else if (isOs2()) {
            platform = Platform.OS2;
        } else if (isOs390()) {
            platform = Platform.OS390;
        } else if (isOsf1()) {
            platform = Platform.OSF1;
        } else if (isSolaris()) {
            platform = Platform.Solaris;
        } else if (isSunOs()) {
            platform = Platform.SunOS;
        } else if (isWindows()) {
            platform = Platform.Windows;
        } else {
            platform = Platform.Others;
        }
        return platform;
    }

    /**
     * 操作系统类型
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    public enum Platform {
        /**
         * Linux操作系统
         */
        Linux("Linux"),
        /**
         * Mac_OS操作系统
         */
        Mac_OS("Mac OS"),
        /**
         * Mac_OS_X操作系统
         */
        Mac_OS_X("Mac OS X"),
        /**
         * Windows操作系统
         */
        Windows("Windows"),
        /**
         * OS2操作系统
         */
        OS2("OS/2"),
        /**
         * Solaris操作系统
         */
        Solaris("Solaris"),
        /**
         * SunOS操作系统
         */
        SunOS("SunOS"),
        /**
         * MPEiX操作系统
         */
        MPEiX("MPE/iX"),
        /**
         * HP_UX操作系统
         */
        HP_UX("HP-UX"),
        /**
         * AIX操作系统
         */
        AIX("AIX"),
        /**
         * OS390操作系统
         */
        OS390("OS/390"),
        /**
         * FreeBSD操作系统
         */
        FreeBSD("FreeBSD"),
        /**
         * Irix操作系统
         */
        Irix("Irix"),
        /**
         * Digital_Unix操作系统
         */
        Digital_Unix("Digital Unix"),
        /**
         * NetWare_411操作系统
         */
        NetWare_411("NetWare"),
        /**
         * OSF1操作系统
         */
        OSF1("OSF1"),
        /**
         * OpenVMS操作系统
         */
        OpenVMS("OpenVMS"),
        /**
         * Others操作系统
         */
        Others("Others");

        Platform(String name) {
            this.name = name;
        }

        /**
         * 操作系统名字
         */
        private String name;

        /**
         * 获取操作系统名字
         *
         * @return 操作系统名字
         */
        public String getName() {
            return name;
        }


    }

    /**
     * 抛出一个运行时异常
     *
     * @param msg 异常信息
     */
    public static void throwException(String msg) {
        throw new UncheckedException(msg);
    }

    /**
     * 抛出一个运行时异常
     *
     * @param msg 异常信息
     * @param e   异常原因
     */
    public static void throwException(String msg, Throwable e) {
        throw new UncheckedException(msg, e);
    }

    /**
     * 抛出一个运行时异常
     *
     * @param msg     异常信息
     * @param e       异常原因
     * @param context 异常原因
     */
    public static void throwException(String msg, Throwable e, Object context) {
        throw new UncheckedException(msg, e).setContext(context);
    }
}
