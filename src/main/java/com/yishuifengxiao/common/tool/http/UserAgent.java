/**
 *
 */
package com.yishuifengxiao.common.tool.http;

import com.yishuifengxiao.common.tool.entity.RootEnum;
import org.apache.commons.lang3.RandomUtils;

import java.util.Arrays;

/**
 * 浏览器标识集合
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
public enum UserAgent implements RootEnum {
    /**
     * EDGE浏览器标识，默认为 110.0
     */
    USER_AGENT_EDGE_VERSION_110_0(1, "EDGE110.0", "Mozilla/5.0 (Windows NT 10.0; " + "Win64; x64)"
            + " AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 " + "Edg"
            + "/110.0.1587.41"),
    /**
     * 谷歌浏览器标识，默认为 谷歌78.0
     */
    USER_AGENT_GOOGLE_VERSION_78_0(2, "谷歌78.0", "Mozilla/5.0 (Windows NT 10.0; " + "Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.97 Safari/537" + ".36"),

    /**
     * 谷歌浏览器标识，默认为 谷歌75.0
     */
    USER_AGENT_GOOGLE_VERSION_75_0(3, "谷歌75.0", "Mozilla/5.0 (Windows NT 10.0; " + "Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537" + ".36"),
    /**
     * 火狐浏览器标识，windows 火狐70.0.1
     */
    USER_AGENT_FIREFOX_VERSION_70_0(4, "火狐70.0.1", "Mozilla/5.0 (Windows NT 10.0; " + "Win64; "
            + "x64; rv:70.0) Gecko/20100101 Firefox/70.0"),
    /**
     * 火狐浏览器标识，windows 火狐97.0
     */
    USER_AGENT_FIREFOX_VERSION_97_0(5, "windows 火狐97.0", "Mozilla/5.0 (Windows NT 10.0; " +
            "Win64; x64; rv:97.0) Gecko/20100101 Firefox/97.0"),

    /**
     * 火狐浏览器标识，Mac版 火狐
     */
    USER_AGENT_FIREFOX_VERSION_MAC(6, "Mac版 火狐", "Mozilla/5.0 (Macintosh; Intel " + "Mac OS X 10"
            + ".6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"),
    /**
     * IE浏览器标识， IE 11.476
     */
    USER_AGENT_IE_VERSION_11_476(7, "IE 11.476", "Mozilla/5.0 (Windows NT 10.0; " + "WOW64; "
            + "Trident/7.0; rv:11.0) like Gecko"),

    /**
     * IE浏览器标识， IE 9.0
     */
    USER_AGENT_IE_VERSION_9_0(8, "IE 9.0", "Mozilla/5.0 (compatible; MSIE 9.0; " + "Windows NT 6"
            + ".1; Trident/5.0"),
    /**
     * EDAG浏览器
     */
    USER_AGENT_EDAG_VERSION_11_476(9, "EDAG 70.0", "Mozilla/5.0 (Windows NT 10.0; " + "Win64; "
            + "x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537" +
            ".36 Edge/18.18362"),
    /**
     * EDAG浏览器 99.0.1150.25
     */
    USER_AGENT_EDAG_VERSION_99(10, "EDAG 99.0.1150.25", "Mozilla/5.0 (Windows NT 10.0; Win64;" +
            " x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.45 Safari/537.36 " +
            "Edg/99.0.1150.25"),

    /**
     * safari 浏览器 Mac 版
     */
    USER_AGENT_SAFARI_VERSION_MAC(11, "safari 浏览器 Mac 版", "Mozilla/5.0 (Macintosh; U; Intel " +
            "Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 " +
            "Safari/534.50"),

    /**
     * safari 浏览器 Windows 版
     */
    USER_AGENT_SAFARI_VERSION_WINDOWS(12, "safari 浏览器 Windows 版", "Mozilla/5.0 (Windows; U; " +
            "Windows NT 6.1; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 " +
            "Safari/534.50"),

    /**
     * Opera浏览器 Windows 版
     */
    USER_AGENT_OPERA_VERSION_WINDOWS(13, "Opera浏览器 Windows 版",
            "Opera/9.80 (Windows NT 6.1; U;" + " en) Presto/2.8.131 Version/11.11"),

    /**
     * Opera浏览器 mac 版
     */
    USER_AGENT_OPERA_VERSION_MAC(14, "Opera浏览器 mac 版", "Opera/9.80 (Macintosh; Intel Mac " + "OS "
            + "X 10.6.8; U; en) Presto/2.8.131 Version/11.11"),

    /**
     * 世界之窗浏览器 windows 版
     */
    USER_AGENT_WORLD_VERSION_WINDOWS(15, "世界之窗浏览器 windows 版", "Mozilla/4.0 (compatible; MSIE " +
            "7.0; Windows NT 5.1)"),

    /**
     * 360浏览器 windows 版
     */
    USER_AGENT_360_VERSION_WINDOWS(16, "360浏览器 windows 版", "Mozilla/4.0 (compatible; MSIE 7" +
            ".0; Windows NT 5.1; 360SE)"),

    /**
     * 猎豹浏览器 windows 版
     */
    USER_AGENT_LBBROWSER_VERSION_WINDOWS(17, "猎豹浏览器 windows 版", "Mozilla/5.0 (Windows NT 6" +
            ".1; WOW64) AppleWebKit/537.1 (KHTML, like " + "Gecko) Chrome/21.0.1180.71 Safari/537"
            + ".1 " + "LBBROWSER"),

    /**
     * Avant浏览器 windows 版
     */
    USER_AGENT_AVANT_VERSION_WINDOWS(18, "Avant浏览器 windows 版", "User-Agent, Mozilla/4.0 " +
            "(compatible; MSIE 7.0; Windows NT 5.1; Avant " + "Browser)"),

    /**
     * Green Browser浏览器 windows 版
     */
    USER_AGENT_GREEN_VERSION_WINDOWS(19, "Green Browser浏览器 windows 版",
            "User-Agent, Mozilla/4.0 " + "(compatible; MSIE 7.0; Windows NT 5.1)"),

    /**
     * 腾讯TT浏览器 windows 版
     */
    USER_AGENT_QQTT_VERSION_WINDOWS(20, "腾讯TT浏览器 windows 版", "User-Agent, Mozilla/4.0 " +
            "(compatible; MSIE 7.0; Windows NT 5.1; " + "TencentTraveler 4.0)"),

    /**
     * QQ浏览器 windows 版
     */
    USER_AGENT_QQ_VERSION_WINDOWS(21, "QQ浏览器 windows 版", "Mozilla/5.0 (compatible; MSIE 9" + ".0;"
            + " Windows NT 6.1; WOW64; Trident/5.0; SLCC2;" + " .NET CLR 2.0.50727; .NET CLR 3.5" + ".30729; .NET CLR" +
            " 3.0.30729; Media Center PC 6" + ".0; .NET4.0C; .NET4.0E; QQBrowser/7.0" + ".3698.400)"),

    /**
     * sogou浏览器 windows 版
     */
    USER_AGENT_SOUGOU_VERSION_WINDOWS(22, "sogou浏览器 windows 版",
            "Mozilla/5.0 (Windows NT 5.1) " + "AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17" + ".0.963.84 " +
                    "Safari/535.11 SE 2.X " + "MetaSr 1.0"),

    /**
     * 傲游（maxthon）浏览器
     */
    USER_AGENT_AOYOU_VERSION_WINDOWS(23, "傲游（maxthon）浏览器", "Mozilla/5.0 (Windows NT 6.1; " +
            "WOW64) AppleWebKit/537.36 (KHTML, like " + "Gecko) Maxthon/4.4.3.4000 Chrome/30.0"
            + ".1599" + ".101 Safari/537.36"),

    /**
     * UC浏览器 windows 版
     */
    USER_AGENT_UC_VERSION_WINDOWS(24, "UC浏览器 windows 版", "Mozilla/5.0 (Windows NT 6.1; " +
            "WOW64) AppleWebKit/537.36 (KHTML, like Gecko) " + "Chrome/38.0.2125.122 UBrowser/4"
            + ".0" + ".3214.0 Safari/537.36"),

    /**
     * Microsoft Edge 127
     */
    USER_AGENT_MICROSOFT_EDGE_127(25, "Microsoft Edge 127", "Mozilla/5.0 (Windows NT 10.0; " +
            "Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 " + "Edg/127.0.0.0"),
    /**
     * QQ浏览器12.3
     */
    USER_AGENT_QQ_VERSION_12_3(26, "QQ浏览器12." + "3", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like " + "Gecko) Chrome/94.0.4606.71 Safari/537.36 "
            + "Core/1.94.232.400 QQBrowser/12.3.5573.400"),
    /**
     * deepin 浏览器6.4.6
     */
    USER_AGENT_DEEPIN_BROWSER_6(27, "deepin 浏览器6.4.6", "Mozilla/5.0 (X11; Linux x86_64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/93.0.4577.63 Safari/537.36 UOS " +
            "Community");

    /**
     * 随机获取一个浏览器UserAgent
     *
     * @return 浏览器UserAgent
     */
    public static String autoUserAgent() {
        int randomVal = RandomUtils.nextInt(0, values().length);
        return values()[randomVal].description;
    }

    UserAgent(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 编码
     */
    private Integer code;

    /**
     * 名称
     */
    private String name;
    /**
     * 描述
     */
    private String description;

    /**
     * 获取编码
     *
     * @return 编码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取编码
     *
     * @return 编码
     */
    @Override
    public Integer code() {
        return this.code;
    }

    @Override
    public String enumName() {
        return this.name;
    }

    /**
     * 获取描述
     *
     * @return 描述
     */
    @Override
    public java.lang.String description() {
        return this.description;
    }

    /**
     * 根据代码值查找对应的枚举实例
     * @param code 要查找的代码值，可以是任意类型
     * @return 返回与代码值匹配的枚举实例，如果未找到匹配项或代码值为null则返回null
     */
    @Override
    public RootEnum of(Object code) {
        if (null == code) {
            return null;
        }
        // 使用Stream API遍历所有枚举值，查找第一个代码值匹配的枚举实例
        return Arrays.stream(values()).filter(e -> e.equalCode(code)).findFirst().orElse(null);
    }

}
