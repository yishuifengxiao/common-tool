package com.yishuifengxiao.common.tool.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.lang.NumberUtil;

/**
 * <p>正则表达式工具类</p>
 * <p>提供正则表达式匹配、提取等功能，支持Pattern缓存以提升性能。</p>
 * <p>特性：</p>
 * <ul>
 * <li>正则表达式匹配和查找</li>
 * <li>从文本中提取匹配内容</li>
 * <li>中文检测和提取</li>
 * <li>数字提取（支持实数）</li>
 * <li>URL、日期、IPv4地址提取</li>
 * <li>Pattern缓存机制</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class RegexUtil {

    /**
     * 协议和域名的正则表达式
     */
    private final static String REGEX_PROTOCOL_AND_HOST =
            "http[s]?://[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?";

    /**
     * 域名的正则表达式
     */
    private final static String REGEX_DOMAIN = "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?";

    /**
     * 中文的正则表达式
     */
    private final static String REGEX_CHINESE = "[\\u4e00-\\u9fa5]+";

    /**
     * <p>匹配所有实数的正则表达式</p>
     * <p style="color:red">注意此表达式的结果中可能会出现多个小数点，应注意排除</p>
     * <ul>
     *     <li><code>[-+]? </code>匹配可选的正负号</li>
     *     <li>[0-9]* 匹配可选的整数部分</li>
     *     <li>\.? 匹配可选的小数点</li>
     *     <li>[0-9]+ 匹配必选的小数部分</li>
     *     <li>([eE][-+]?[0-9]+)? 匹配可选的指数部分，其中 ([eE]) 匹配指数符号，([-+]?[0-9]+) 匹配指数值</li>
     * </ul>
     */
    public final static String REGEX_NUMBER = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    /**
     * 非法的数字形式表达式，即一个小数里包含多个小数点的正则表达
     */
    public final static String REGEX_ILLEGAL_NUMBER = "\\d+\\.\\d+(\\.\\d+)+";

    /**
     * 日期正则表达式
     */
    private final static String REGEX_DATE = "\\d{4}-\\d{1,2}-\\d{1,2}";

    /**
     * URL正则表达式
     */
    private final static String REGEX_URL =
            "((http|ftp|https)://)(([a-zA-Z0-9\\._-]+\\.[a-zA-Z]{2,6})|([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}))(:[0-9]{1,4})*(/[a-zA-Z0-9\\&%_\\./-~-]*)?";

    /**
     * IPv4地址正则表达式
     */
    private final static String REGEX_IPV4 =
            "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))" + "|[0" + "-1]?\\d{1,2})){3}";

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
     * URL地址的正则
     */
    public static final Pattern PATTERN_URL = Pattern.compile(REGEX_URL);


    /**
     * Pattern缓存的最大容量，超过后按LRU淘汰，防止动态传入不同正则导致缓存无限增长
     */
    private static final int PATTERN_CACHE_SIZE = 256;

    /**
     * 存储正则Pattern的集合 key ：正则表达式 value ：Pattern对象，采用有界LRU策略
     */
    private static final Map<String, Pattern> PATTERN_CACHE =
            new LinkedHashMap<String, Pattern>(16, 0.75f, true) {
                private static final long serialVersionUID = 1L;

                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Pattern> eldest) {
                    return size() > PATTERN_CACHE_SIZE;
                }
            };

    /**
     * 根据正则表达式获取Pattern对象
     *
     * @param regex 正则表达式
     * @return Pattern对象
     * @throws IllegalArgumentException 当正则表达式为空或语法非法时抛出
     */
    public static Pattern pattern(String regex) {
        if (StringUtils.isBlank(regex)) {
            throw new IllegalArgumentException("正则表达式不能为空");
        }
        String key = regex.trim();
        // LinkedHashMap 非线程安全，读写统一加锁；Pattern 本身不可变，可安全共享
        synchronized (PATTERN_CACHE) {
            Pattern pattern = PATTERN_CACHE.get(key);
            if (pattern == null) {
                pattern = Pattern.compile(key);
                PATTERN_CACHE.put(key, pattern);
            }
            return pattern;
        }
    }

    /**
     * 判断内容是否符合正则表达式
     *
     * @param regex 正则表达式
     * @param str   待判断的内容
     * @return 若匹配则返回为true, 否则为false
     */
    public static boolean match(String regex, String str) {
        return pattern(regex).matcher(str).matches();
    }

    /**
     * 判断内容是否包正则表达式标识的内容
     *
     * @param regex 正则表达式
     * @param str   待判断的内容
     * @return 若匹配则返回为true, 否则为false
     */
    public static boolean find(String regex, String str) {
        return pattern(regex).matcher(str).find();
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
    public static String extract(String regex, String str) {
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
    public static List<String> extractAll(String regex, String str) {
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
     * 数字token的字符范围，用于先按完整数字片段切分，避免把1.2.3拆成1.2和.3
     */
    private final static String REGEX_NUMBER_TOKEN = "[0-9.eE+-]+";

    /**
     * <p>提取字符中包含的所有实数数</p>
     *
     * @param text 待提取的字符串
     * @return 提取字符中包含的所有整数
     */
    public static List<Number> extractAllNumber(String text) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }

        // 先按完整数字token切分，再只保留符合数字语法且可解析的token；多数值点token会被整体丢弃
        return extractAll(REGEX_NUMBER_TOKEN, text).stream()
                .filter(candidate -> candidate.matches(REGEX_NUMBER))
                .map(NumberUtil::parseToBigDecimal)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * 如果包含汉字则返回为true
     *
     * @param str 需要判断的字符串
     * @return 如果包含汉字则返回为true，否则为false
     */
    public static boolean containsChinese(String str) {
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
     * 从给定的字符串中提取出所有的中文
     *
     * @param str 需要提取的字符串
     * @return 提取出来的所有的中文
     */
    public static List<String> extractAllChineses(String str) {
        return extractAll(REGEX_CHINESE, str);
    }

    /**
     * 从给定的字符串中提取出所有的yyyy-MM-dd格式的日期
     *
     * @param str 需要提取的字符串
     * @return 提取出来的所有的yyyy-MM-dd格式的日期
     */
    public static List<String> extractAllDates(String str) {
        return extractAll(REGEX_DATE, str);
    }

    /**
     * 从给定的字符串中提取出所有的IPv4地址
     *
     * @param str 需要提取的字符串
     * @return 提取出来的所有的IPv4地址
     */
    public static List<String> extractAllIpv4s(String str) {
        return extractAll(REGEX_IPV4, str);
    }

    /**
     * 从给定的字符串中提取出所有的url地址
     *
     * @param str 需要提取的字符串
     * @return 提取出来的所有的url地址
     */
    public static List<String> extractAllUrls(String str) {
        return extractAll(REGEX_URL, str);
    }
}
