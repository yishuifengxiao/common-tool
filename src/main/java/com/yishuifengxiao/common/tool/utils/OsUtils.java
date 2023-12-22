package com.yishuifengxiao.common.tool.utils;

import java.io.File;
import java.time.ZoneId;

/**
 * 系统工具类
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
public final class OsUtils {
    /**
     * 默认的时区,上海
     */
    public final static String DEFAULT_ZONE = "Asia/Shanghai";

    /**
     * 获取北京时间的ZoneId
     *
     * @return 北京时间的ZoneId
     */
    public final static ZoneId ZONEID_OF_CHINA = ZoneId.of(DEFAULT_ZONE);
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
     * 半角逗号
     */
    public final static String COMMA = ",";

    /**
     * 下划线 (英文输入法 _ )
     */
    public final static String SEPARATOR_UNDERLINE = "_";

    /**
     * 问号 (英文输入法 ? )
     */
    public final static String SEPARATOR_QUESTION_MARK = "?";

    /**
     * 本机的IPV4地址
     */
    public final static String LOCAL_IPV4 = "127.0.0.1";
    /**
     * 本机的IPV6地址
     */
    public final static String LOCAL_IPV6 = "0:0:0:0:0:0:0:1";

    /**
     * 本机的地址localhost
     */
    public final static String LOCAL_HOST = "localhost";

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
     * 根据操作系统获取文件路径分隔符
     * </p>
     *
     * <p>
     * windows下返回左斜杠<code>/</code>,其他环境下返回右斜杠<code>\</code>
     * </p>
     *
     * @return 路径分隔符
     */
    public static String pathSeparator() {
        return System.getProperty("file.separator");
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
     * <p>获取系统临时文件目录地址</p>
     * <p>在windows下地址的形式为 <code>C:\Users\yishui\AppData\Local\Temp\</code></p>
     *
     * @return 系统临时文件目录地址
     */
    public static String tmpdir() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * <p>获取用户的当前工作目录地址</p>
     * <p>在linux下地址的形式为 <code>/home/yishui/appname</code></p>
     * <p>在windows下地址的形式为 <code>D:\yishui\appname</code></p>
     *
     * @return 用户的当前工作目录地址
     */
    public static String userDir() {

        return System.getProperty("user.dir");
    }

    /**
     * <p>获取用户的当前home目录地址</p>
     * <p>在linux下地址的形式为 <code>/home/aidocuser</code></p>
     * <p>在windows下地址的形式为 <code>C:\Users\yishui</code></p>
     *
     * @return 用户的当前工作目录地址
     */
    public static String homeDir() {

        return System.getProperty("user.home");
    }

    /**
     * <p>获取用户的当前工作目录地址</p>
     * <p>在linux下地址的形式为 <code>/home/yishui/appname</code></p>
     * <p>在windows下地址的形式为 <code>D:\yishui\appname</code></p>
     *
     * @return 用户的当前工作目录地址
     */
    public static File currentWorkPath() {
        return new File(userDir());
    }

    /**
     * <p>在当前工作目录下创建一个子目录</p>
     *
     * @param name 待创建的子目录的名字
     * @return 在当前工作目录下创建的子目录
     */
    public static File currentWorkSubdirectory(String name) {
        return new File(currentWorkPath(), name);
    }

    /**
     * 获取操作系统的名字
     *
     * @return 操作系统的名字
     */
    public static String osName() {
        return System.getProperty("os.name");
    }

    /**
     * <p>获取JAVA文件的编码</p>
     * <ul>
     *    <li>file.encoding不主动配置的情况下，默认是操作系统的编码；</li>
     *    <li>file.encoding在JVM启动后再修改其值，只会修改配置项值，不会改变默认字符集编码；</li>
     *    <li>运行时配置file.encoding，影响java默认字符集编码：</li>
     * </ul>
     * <p>在java应用启动时可以通过 <code>-Dfile.encoding=GBK</code> 指定编码</p>
     *
     * @return JAVA文件的编码
     */
    public static String fileEncoding() {
        return System.getProperty("file.encoding");
    }


    /**
     * 获取操作系统的默认编码
     *
     * @return 操作系统的默认编码
     */
    public static String osEncoding() {
        return System.getProperty("sun.jnu.encoding");
    }


    /**
     * 获取操作系统类型
     *
     * @return 操作系统类型
     */
    public static Platform getPlatform() {
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


}
