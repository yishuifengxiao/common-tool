package com.yishuifengxiao.common.tool.http;

import com.yishuifengxiao.common.tool.text.RegexUtil;
import com.yishuifengxiao.common.tool.utils.OsUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
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

        /**
     * 从URL字符串中提取协议部分
     *
     * @param url 输入的URL字符串
     * @return 返回URL中的协议部分，如果URL为空或解析失败则返回null
     */
    public static String extractProtocol(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }

        try {
            // 使用正则表达式匹配URL中的协议和主机部分
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

        // 判断是否已经是完整URL
        if (RegexUtil.isUrl(url)) {
            return url;
        }

        // 校验 referrer 必须是完整 URL 才能提取基础路径
        if (!RegexUtil.isUrl(referrer)) {
            return null;
        }

        try {
            if (StringUtils.startsWith(url, OsUtils.LEFT_SLASH)) {
                int lastSlashIndex = referrer.lastIndexOf(OsUtils.LEFT_SLASH);
                if (lastSlashIndex <= 8) { // 协议头长度 "http://" or "https://"
                    return null;
                }
                return referrer.substring(0, lastSlashIndex) + url;
            }

            if (StringUtils.startsWith(url, RELATIVE_ADDR)) {
                long count = StringUtils.countMatches(url, RELATIVE_ADDR);
                String baseReferrer = referrer;

                for (int i = 0; i < count; i++) {
                    int lastSlashIndex = baseReferrer.lastIndexOf(OsUtils.LEFT_SLASH);
                    if (lastSlashIndex <= 8) {
                        return null;
                    }
                    baseReferrer = baseReferrer.substring(0, lastSlashIndex);
                }

                // 安全地移除所有 "../" 并规范化路径
                String normalizedRelativePath = Paths.get("", url).normalize().toString().replace('\\', '/');
                if (normalizedRelativePath.startsWith("../")) {
                    return null; // 路径越界
                }

                return baseReferrer + OsUtils.LEFT_SLASH + normalizedRelativePath;
            }

            int lastSlashIndex = referrer.lastIndexOf(OsUtils.LEFT_SLASH);
            if (lastSlashIndex <= 8) {
                return null;
            }
            return referrer.substring(0, lastSlashIndex) + OsUtils.LEFT_SLASH + url;

        } catch (Exception e) {
            return null;
        }
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

        String[] tokens = queryString.split("&");
        if (tokens.length == 0) {
            return null;
        }

        Map<String, String> map = new HashMap<>(tokens.length);
        for (String token : tokens) {
            if (StringUtils.isBlank(token)) {
                continue;
            }

            String[] strings = token.split("=", 2);
            if (strings.length < 1 || StringUtils.isBlank(strings[0])) {
                continue;
            }

            String key;
            String value;

            try {
                key = java.net.URLDecoder.decode(strings[0].trim(), "UTF-8");
                // 对于值部分，如果包含不完整的转义序列，直接使用原始值
                if (strings.length > 1) {
                    try {
                        value = java.net.URLDecoder.decode(strings[1], "UTF-8");
                    } catch (IllegalArgumentException e) {
                        // 如果解码失败（如不完整的转义序列），使用原始值
                        value = strings[1];
                    }
                } else {
                    value = "";
                }
            } catch (java.io.UnsupportedEncodingException e) {
                // 正常不会发生，UTF-8 必定支持
                throw new RuntimeException("Unsupported encoding", e);
            }

            map.put(key, value);
        }
        return map;
    }


}
