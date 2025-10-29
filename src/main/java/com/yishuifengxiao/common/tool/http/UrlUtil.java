package com.yishuifengxiao.common.tool.http;

import com.yishuifengxiao.common.tool.text.RegexUtil;
import com.yishuifengxiao.common.tool.utils.OsUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * url数据工具
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class UrlUtil {

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
     * 相对地址
     */
    private final static String RELATIVE_ADDR = "../";

    /**
     * X-Forwarded-For：Squid 服务代理
     * Proxy-Client-IP：apache 服务代理
     * WL-Proxy-Client-IP：weblogic 服务代理
     * HTTP_CLIENT_IP：一些代理服务器
     * X-Real-IP：nginx服务代理
     */
    public final static List<String> IP_HEAD_LIST = List.of(
            "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA",
            "REMOTE_ADDR", "X-Real-IP"
    );


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
     * <p>
     * 从url里提取出简短域名信息
     * </p>
     * <p>
     * 例如www.yishuifengxiao.com 的提取值为 yishuifengxiao
     * </p>
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

        if (position >= 0 && position < tokens.length) {
            return tokens[position];
        } else {
            return null;
        }
    }

    public static String extractProtocol(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        try {
            Matcher matcher = RegexUtil.PATTERN_PROTOCOL_AND_HOST.matcher(url);
            return matcher.find() ? matcher.group(1) : null;
        } catch (IllegalStateException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 根据url的来源地址将此url补全为网络地址
     *
     * @param referrer 来源地址
     * @param url      待补全的url
     * @return 补全后网络地址形式的url
     */
    public static String urlComplete(String referrer, String url) {
        if (StringUtils.isBlank(referrer) || StringUtils.isBlank(url)) {
            return null;
        }
        url = url.trim();
        referrer = referrer.trim();

        if (RegexUtil.isUrl(url)) {
            return url;
        }

        if (StringUtils.startsWith(url, OsUtils.LEFT_SLASH)) {
            return StringUtils.substringBeforeLast(referrer, OsUtils.LEFT_SLASH) + url;
        }

        if (StringUtils.startsWith(url, RELATIVE_ADDR)) {
            long count = StringUtils.countMatches(url, RELATIVE_ADDR);
            String baseReferrer = referrer;
            for (int i = 0; i < count; i++) {
                baseReferrer = StringUtils.substringBeforeLast(baseReferrer, OsUtils.LEFT_SLASH);
            }
            url = url.replaceFirst("(\\.\\./)+", ""); // 更安全的方式去除 ../
            return baseReferrer + OsUtils.LEFT_SLASH + url;
        }

        return StringUtils.substringBeforeLast(referrer, OsUtils.LEFT_SLASH) + OsUtils.LEFT_SLASH + url;
    }

    /**
     * <p>
     * 将查询字符串转为map结构
     * </p>
     * <p>
     * 字符串形式为
     * <p>
     * timeOption=0&#38;page=1&#38;pageSize=10&#38;keyPlace=1&#38;sort=dateDesc&#38;qt=*
     * </p>
     *
     * @param queryString 待转换的查询参数字符串
     * @return 转换后的map数据
     */
    public static Map<String, String> queryString2Map(String queryString) {
        if (StringUtils.isBlank(queryString)) {
            return null;
        }

        String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(queryString, OsUtils.SEPARATOR_AND);
        if (null == tokens) {
            return null;
        }

        Map<String, String> map = new HashMap<>(tokens.length);
        for (String token : tokens) {
            String[] strings = token.split("=", 2); // 最多分割成两段
            if (strings.length < 2 || StringUtils.isBlank(strings[0])) {
                continue;
            }
            String key = strings[0].trim();
            String value = strings.length > 1 ? strings[1] : "";
            map.put(key, value);
        }
        return map;
    }
}
