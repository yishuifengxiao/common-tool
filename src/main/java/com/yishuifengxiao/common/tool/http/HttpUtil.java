package com.yishuifengxiao.common.tool.http;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.yishuifengxiao.common.tool.text.RegexUtil;
import org.apache.commons.lang3.StringUtils;

import javax.print.DocFlavor;

/**
 * http数据工具
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public final class HttpUtil {

    /**
     * 分隔符
     */
    private final static String SEPARATOR = "&";


    /**
     * 左斜杠
     */
    private final static String LEFT_SLASH = "/";

    /**
     * 相对地址
     */
    private final static String RELATIVE_ADDR = "../";


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
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(queryString)) {
            return null;
        }
        String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(queryString, SEPARATOR);
        if (null == tokens) {
            return null;
        }
        for (String token : tokens) {
            String[] strings = token.split("=");
            if (null == strings || strings.length < 2) {
                continue;
            }
            if (StringUtils.isBlank(strings[0])) {
                continue;
            }
            map.put(strings[0].trim(), strings[1]);
        }
        return map;
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
