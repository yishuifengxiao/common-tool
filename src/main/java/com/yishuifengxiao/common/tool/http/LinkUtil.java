package com.yishuifengxiao.common.tool.http;

import java.util.regex.Matcher;

import com.yishuifengxiao.common.tool.text.RegexUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 链接处理工具类
 *
 * @author yishui
 * @version 1.0.0
 */
public final class LinkUtil {

    /**
     * com.cn域名表达式
     */
    public final static String COM_CN_DOMAIN = "com.cn";
    /**
     * gov.cn域名表达式
     */
    public final static String GOV_CN_DOMAIN = "gov.cn";

    /**
     * edu.cn域名表达式
     */
    public final static String EDU_CN_DOMAIN = "edu.cn";


    /**
     * 左斜杠
     */
    private final static String LEFT_SLASH = "/";

    /**
     * 相对地址
     */
    private final static String RELATIVE_ADDR = "../";


    /**
     * 从url中提取出来协议和域名
     *
     * @param url 待处理的url
     * @return 返回协议和域名，形如 http://www.yishuifengxiao.com
     */
    public static String extractProtocolAndHost(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        Matcher matcher = RegexUtil.PATTERN_PROTOCOL_AND_HOST.matcher(url);
        return matcher.find() ? matcher.group() : null;

    }

    /**
     * 判断是否符合网络请求的地址形式
     *
     * @param url 需要判断的url
     * @return 符合要求为true, 否则为false
     */
    public static boolean matchHttpRequest(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        Matcher matcher = RegexUtil.PATTERN_PROTOCOL_AND_HOST.matcher(url);
        return matcher.find();
    }

    /**
     * 从url中提取出来完整的域名
     *
     * @param url 待处理的url
     * @return 返回完整的域名，形如 www.yishuifengxiao.com
     */
    public static String extractDomain(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        Matcher matcher = RegexUtil.PATTERN_DOMAIN.matcher(url);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * <p>从url里提取出简短域名信息</p>
     * <p>例如www.yishuifengxiao.com 的提取值为 yishuifengxiao</p>
     *
     * @param url url 待处理的url
     * @return url里提取出简短域名信息
     */
    public static String keyword(String url) {

        String domain = extractDomain(url);

        if (StringUtils.isBlank(domain)) {
            return null;
        }
        domain = domain.toLowerCase();
        String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(domain, ".");
        int position = tokens.length - 2;
        if (StringUtils.containsAny(domain, COM_CN_DOMAIN, GOV_CN_DOMAIN, EDU_CN_DOMAIN)) {
            position -= 1;
        }

        return tokens[position];
    }

    /**
     * 从url中提取出协议
     *
     * @param url 待提取的url
     * @return url中提取出协议
     */
    public static String extractProtocol(String url) {
        Matcher matcher = RegexUtil.PATTERN_PROTOCOL_AND_HOST.matcher(url);
        if (matcher.find()) {
            String protocolAndDomian = matcher.group();
            return StringUtils.substringBefore(protocolAndDomian, ":");
        }
        return null;
    }

    /**
     * 根据url的来源地址将此url补全为网络地址
     *
     * @param referrer 来源地址
     * @param url      待补全的url
     * @return 补全后网络地址形式的url
     */
    public static String urlComplete(String referrer, String url) {
        if (!StringUtils.isNoneBlank(referrer, url)) {
            return null;
        }
        url = url.trim();
        referrer = referrer.trim();
        if (RegexUtil.isUrl(url)) {
            // 已经是网络地址
            return url;
        }
        if (StringUtils.startsWith(url, LEFT_SLASH)) {
            // 左斜杠开头，绝对地址
            return StringUtils.substringBeforeLast(referrer, LEFT_SLASH) + url;
        }
        if (StringUtils.startsWith(url, RELATIVE_ADDR)) {
            // 以../开头的地址
            long count = StringUtils.countMatches(url, RELATIVE_ADDR);
            for (int i = 0; i <= count; i++) {
                referrer = StringUtils.substringBeforeLast(referrer, LEFT_SLASH);
            }
            url = url.replaceAll("\\.\\./", "");
            return referrer + LEFT_SLASH + url;
        }
        // 相对地址
        return StringUtils.substringBeforeLast(referrer, LEFT_SLASH) + LEFT_SLASH + url;
    }
}
