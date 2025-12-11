package com.yishuifengxiao.common.tool.text;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * PlaceholderExtractor工具类extractPlaceholders方法的单元测试
 */
public class PlaceholderExtractor_extractPlaceholders_Test {

    /**
     * 测试正常提取占位符的情况
     */
    @Test
    public void testExtractPlaceholders_NormalCase() {
        String input = "Hello ${name}, welcome to ${place}!";
        List<String> result = PlaceholderExtractor.extractPlaceholders(input);
        
        assertEquals(2, result.size());
        assertEquals("name", result.get(0));
        assertEquals("place", result.get(1));
    }

    /**
     * 测试没有占位符的情况
     */
    @Test
    public void testExtractPlaceholders_NoPlaceholders() {
        String input = "Hello world, welcome to Beijing!";
        List<String> result = PlaceholderExtractor.extractPlaceholders(input);
        
        assertTrue(result.isEmpty());
    }

    /**
     * 测试空字符串和null值
     */
    @Test
    public void testExtractPlaceholders_EmptyAndNull() {
        // 测试null值
        List<String> result1 = PlaceholderExtractor.extractPlaceholders(null);
        assertTrue(result1.isEmpty());
        
        // 测试空字符串
        List<String> result2 = PlaceholderExtractor.extractPlaceholders("");
        assertTrue(result2.isEmpty());
    }

    /**
     * 测试只有占位符的情况
     */
    @Test
    public void testExtractPlaceholders_OnlyPlaceholders() {
        String input = "${first}${second}${third}";
        List<String> result = PlaceholderExtractor.extractPlaceholders(input);
        
        assertEquals(3, result.size());
        assertEquals("first", result.get(0));
        assertEquals("second", result.get(1));
        assertEquals("third", result.get(2));
    }

    /**
     * 测试嵌套占位符的情况（实际上不会被识别为嵌套，而是独立的占位符）
     */
    @Test
    public void testExtractPlaceholders_Nested() {
        String input = "Hello ${user.${name}}!";
        List<String> result = PlaceholderExtractor.extractPlaceholders(input);
        
        assertEquals(1, result.size());
        assertEquals("user.${name", result.get(0)); // 注意这里不是真正的嵌套支持
    }

    /**
     * 测试特殊字符在占位符中的情况
     */
    @Test
    public void testExtractPlaceholders_SpecialCharacters() {
        String input = "Value: ${user-name}, ${user_name}, ${user123}, ${用户}";
        List<String> result = PlaceholderExtractor.extractPlaceholders(input);
        
        assertEquals(4, result.size());
        assertEquals("user-name", result.get(0));
        assertEquals("user_name", result.get(1));
        assertEquals("user123", result.get(2));
        assertEquals("用户", result.get(3));
    }

    /**
     * 测试重复占位符的情况
     */
    @Test
    public void testExtractPlaceholders_DuplicatePlaceholders() {
        String input = "Hello ${name}, ${name} again!";
        List<String> result = PlaceholderExtractor.extractPlaceholders(input);
        
        assertEquals(2, result.size());
        assertEquals("name", result.get(0));
        assertEquals("name", result.get(1));
    }

    /**
     * 测试占位符边界情况
     */
    @Test
    public void testExtractPlaceholders_EdgeCases() {
        // 测试空占位符
        String input1 = "Hello ${}!";
        List<String> result1 = PlaceholderExtractor.extractPlaceholders(input1);
        assertEquals(0, result1.size());

        // 测试只有一个字符的占位符
        String input2 = "Hello ${a}!";
        List<String> result2 = PlaceholderExtractor.extractPlaceholders(input2);
        assertEquals(1, result2.size());
        assertEquals("a", result2.get(0));
    }
}