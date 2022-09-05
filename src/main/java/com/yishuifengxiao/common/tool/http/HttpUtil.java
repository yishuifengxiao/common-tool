package com.yishuifengxiao.common.tool.http;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

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
        String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(queryString, SEPARATOR);
        if (null == tokens) {
            return null;
        }
        Map<String, String> map = new HashMap<>(tokens.length);
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


}
