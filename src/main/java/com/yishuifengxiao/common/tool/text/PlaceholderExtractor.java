package com.yishuifengxiao.common.tool.text;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderExtractor {

    /**
     * 提取字符串中所有的占位符，格式为 ${name}
     *
     * @param input 输入字符串
     * @return 占位符名称列表
     */
    public static List<String> extractPlaceholders(String input) {
        if (StringUtils.isBlank(input)) {
            return new ArrayList<>();
        }
        List<String> placeholders = new ArrayList<>();

        // 正则表达式：匹配 ${...} 格式
        // \\$ 转义$字符，\\{ 转义{，.+? 非贪婪匹配任意字符，\\} 转义}
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String placeholderName = matcher.group(1);
            placeholders.add(placeholderName);
        }

        return placeholders;
    }

    /**
     * 提取占位符的详细信息（包括位置和内容）
     *
     * @param input 输入字符串
     * @return 包含占位符详细信息的列表
     */
    public static List<PlaceholderInfo> extractPlaceholdersWithDetails(String input) {
        List<PlaceholderInfo> placeholders = new ArrayList<>();
        if (StringUtils.isBlank(input)) {
            return placeholders;
        }

        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String fullMatch = matcher.group(0);  // 完整的占位符，如 ${name}
            String placeholderName = matcher.group(1);  // 占位符名称，如 name
            int start = matcher.start();  // 起始位置
            int end = matcher.end();      // 结束位置

            placeholders.add(new PlaceholderInfo(placeholderName, fullMatch, start, end));
        }

        return placeholders;
    }

    /**
     * 替换占位符
     *
     * @param input        输入字符串
     * @param replacements 替换映射
     * @return 替换后的字符串
     */
    public static String replacePlaceholders(String input, java.util.Map<String, String> replacements) {
        if (StringUtils.isBlank(input)) {
            return null;
        }
        Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholderName = matcher.group(1);
            String replacement = replacements.getOrDefault(placeholderName, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 占位符信息类
     */

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class PlaceholderInfo implements Serializable {
        /**
         * 占位符名称,如 name
         */
        private String name;
        /**
         * 完整占位符,如 ${name}
         */
        private String fullPlaceholder;
        /**
         * 起始位置
         */
        private int start;
        /**
         * 结束位置
         */
        private int end;
    }

}

