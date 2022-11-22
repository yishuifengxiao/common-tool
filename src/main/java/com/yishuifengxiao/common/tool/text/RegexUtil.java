package com.yishuifengxiao.common.tool.text;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * 正则工具
 * </p>
 * 该工具主要是利用正则对字符串进行判断，主要功能如下：
 * <ol>
 * <li>判断给定的字符串是否包含中文</li>
 * <li>判断给定的字符串是否符合给定的正则表达式</li>
 * </ol>
 *
 * <p>
 * <strong>该工具是一个线程安全类的工具</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RegexUtil {

    /**
     * 协议和域名的正则表达式
     */
    private final static String REGEX_PROTOCOL_AND_HOST = "http[s]?://[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?";

    /**
     * 域名的正则表达式
     */
    private final static String REGEX_DOMAIN = "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?";

    /**
     * 中文的正则表达式
     */
    private final static String REGEX_CHINESE = "[\\u4e00-\\u9fa5]+";

    /**
     * 日期正则表达式
     */
    private final static String REGEX_DATE = "\\d{4}-\\d{1,2}-\\d{1,2}";

    /**
     * URL正则表达式
     */
    private final static String REGEX_URL = "((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";

    /**
     * IPv4地址正则表达式
     */
    private final static String REGEX_IPV4 = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";

    /**
     * 判断是否符合形如 http://www.yishuifengxiao.com 的正则表达式
     */
    public final static Pattern PATTERN_PROTOCOL_AND_HOST = Pattern.compile(REGEX_PROTOCOL_AND_HOST);

    /**
     * 判断是否符合形如 www.yishuifengxiao.com 的正则表达式
     */
    public final static Pattern PATTERN_DOMAIN = Pattern.compile(REGEX_DOMAIN);

    /**
     * 包含中文的正则
     */
    public static final Pattern PATTERN_CHINESE = Pattern.compile(REGEX_CHINESE);

    /**
     * 日期的正则
     */
    public static final Pattern PATTERN_DATE = Pattern.compile(REGEX_DATE);

    /**
     * IPv4地址的正则
     */
    public static final Pattern PATTERN_IPV4 = Pattern.compile(REGEX_IPV4);

    /**
     * URL地址的正则
     */
    public static final Pattern PATTERN_URL = Pattern.compile(REGEX_URL);

    /**
     * 存储正则Pattern的集合 key ：正则表达式 value ：Pattern对象
     */
    private static final Map<String, Pattern> PATTERN_CACHE = new HashMap<>();

    /**
     * 根据正则表达式获取Pattern对象
     *
     * @param regex 正则表达式
     * @return Pattern对象
     */
    public synchronized static Pattern pattern(String regex) {
        if (StringUtils.isBlank(regex)) {
            throw new RuntimeException("正则表达式不能为空");
        }
        regex = regex.trim();
        Pattern pattern = PATTERN_CACHE.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            PATTERN_CACHE.put(regex, pattern);
        }
        return pattern;
    }

    /**
     * 判断内容是否符合正则表达式
     *
     * @param regex 正则表达式
     * @param str   待判断的内容
     * @return 若匹配则返回为true, 否则为false
     */
    public synchronized static boolean match(String regex, String str) {
        Matcher matcher = pattern(regex).matcher(str);
        return matcher.matches();
    }

    /**
     * 判断内容是否包正则表达式标识的内容
     *
     * @param regex 正则表达式
     * @param str   待判断的内容
     * @return 若匹配则返回为true, 否则为false
     */
    public synchronized static boolean find(String regex, String str) {
        Matcher matcher = pattern(regex).matcher(str);
        return matcher.find();
    }

    /**
     * <p>
     * 根据正则表达式从内容中提取出一组匹配的内容
     * </p>
     * <b>只要匹配到第一组数据就会返回</b>
     *
     * @param regex 正则表达式
     * @param str   目标内容
     * @return 提取出一组匹配的内容
     */
    public synchronized static String extract(String regex, String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        Matcher matcher = pattern(regex).matcher(str);
        while (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * <p>
     * 根据正则表达式从内容中提取出所有匹配的内容
     * </p>
     * <b>返回所有匹配的数据</b>
     *
     * @param regex 正则表达式
     * @param str   目标内容
     * @return 取出所有匹配的内容
     */
    public synchronized static List<String> extractAll(String regex, String str) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        Matcher matcher = pattern(regex).matcher(str);
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * 提取处字符串里所有的中文
     *
     * @param text 待提取的字符串
     * @return 提取出来的中文
     */
    public static List<String> extractAllChinese(String text) {
        return extractAll(REGEX_CHINESE, text);
    }

    /**
     * 如果包含汉字则返回为true
     *
     * @param str 需要判断的字符串
     * @return 如果包含汉字则返回为true，否则为false
     */
    public static boolean containChinese(String str) {
        return PATTERN_CHINESE.matcher(str).find();
    }

    /**
     * 如果都是汉字则返回为true
     *
     * @param str 需要判断的字符串
     * @return 如果都是汉字则返回为true，否则为false
     */
    public static boolean isChinese(String str) {
        return PATTERN_CHINESE.matcher(str).matches();
    }

    /**
     * 如果是完整的URL网址则返回为true
     *
     * @param str 需要判断的字符串
     * @return 如果是完整的URL网址则返回为true，否则为false
     */
    public static boolean isUrl(String str) {
        return PATTERN_URL.matcher(str).matches();
    }

    /**
     * 如果都是汉字则返回为true
     *
     * @param str 需要判断的字符串
     * @return 如果都是汉字则返回为true，否则为false
     */
    public static List<String> extractChinese(String str) {
        return extractAll(REGEX_CHINESE, str);
    }

    /**
     * 从给定的字符串中提取出所有的yyyy-MM-dd格式的日期
     *
     * @param str 需要提取的字符串
     * @return 提取出来的所有的yyyy-MM-dd格式的日期
     */
    public static List<String> extractDate(String str) {
        return extractAll(REGEX_DATE, str);
    }

    /**
     * 从给定的字符串中提取出所有的IPv4地址
     *
     * @param str 需要提取的字符串
     * @return 提取出来的所有的IPv4地址
     */
    public static List<String> extractIpv4(String str) {
        return extractAll(REGEX_IPV4, str);
    }

    /**
     * 从给定的字符串中提取出所有的url地址
     *
     * @param str 需要提取的字符串
     * @return 提取出来的所有的url地址
     */
    public static List<String> extractUrl(String str) {
        return extractAll(REGEX_URL, str);
    }
}
