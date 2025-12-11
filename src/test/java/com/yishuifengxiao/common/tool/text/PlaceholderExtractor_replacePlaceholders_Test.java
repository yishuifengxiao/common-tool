package com.yishuifengxiao.common.tool.text;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * PlaceholderExtractor工具类replacePlaceholders方法的单元测试
 */
public class PlaceholderExtractor_replacePlaceholders_Test {

    /**
     * 测试正常替换占位符的情况
     */
    @Test
    public void testReplacePlaceholders_NormalCase() {
        String input = "Hello ${name}, welcome to ${place}!";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("name", "Alice");
        replacements.put("place", "Beijing");
        
        String result = PlaceholderExtractor.replacePlaceholders(input, replacements);
        assertEquals("Hello Alice, welcome to Beijing!", result);
    }

    /**
     * 测试没有占位符的情况
     */
    @Test
    public void testReplacePlaceholders_NoPlaceholders() {
        String input = "Hello world, welcome to Beijing!";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("name", "Alice");
        
        String result = PlaceholderExtractor.replacePlaceholders(input, replacements);
        assertEquals("Hello world, welcome to Beijing!", result);
    }

    /**
     * 测试空字符串和null值
     */
    @Test
    public void testReplacePlaceholders_EmptyAndNull() {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("name", "Alice");
        
        // 测试null值
        String result1 = PlaceholderExtractor.replacePlaceholders(null, replacements);
        assertNull(result1);
        
        // 测试空字符串
        String result2 = PlaceholderExtractor.replacePlaceholders("", replacements);
        assertEquals("", result2);
    }

    /**
     * 测试未匹配到的占位符保持原样
     */
    @Test
    public void testReplacePlaceholders_UnmatchedPlaceholders() {
        String input = "Hello ${name}, welcome to ${place}!";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("name", "Alice");
        // 注意: place没有在replacements中定义
        
        String result = PlaceholderExtractor.replacePlaceholders(input, replacements);
        assertEquals("Hello Alice, welcome to ${place}!", result);
    }

    /**
     * 测试空替换值的情况
     */
    @Test
    public void testReplacePlaceholders_EmptyReplacementValues() {
        String input = "Hello ${name}, welcome to ${place}!";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("name", "");
        replacements.put("place", "");
        
        String result = PlaceholderExtractor.replacePlaceholders(input, replacements);
        assertEquals("Hello , welcome to !", result);
    }

    /**
     * 测试特殊字符替换的情况
     */
    @Test
    public void testReplacePlaceholders_SpecialCharacters() {
        String input = "Value: ${var1}, ${var2}, ${var3}";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("var1", "value with spaces");
        replacements.put("var2", "value-with-dashes");
        replacements.put("var3", "value_with_underscores");
        
        String result = PlaceholderExtractor.replacePlaceholders(input, replacements);
        assertEquals("Value: value with spaces, value-with-dashes, value_with_underscores", result);
    }

    /**
     * 测试包含正则表达式特殊字符的替换值
     */
    @Test
    public void testReplacePlaceholders_RegexSpecialChars() {
        String input = "Pattern: ${pattern}";
        Map<String, String> replacements = new HashMap<>();
        replacements.put("pattern", "value with $ and \\ and ?");
        
        String result = PlaceholderExtractor.replacePlaceholders(input, replacements);
        assertEquals("Pattern: value with $ and \\ and ?", result);
    }
}