package com.yishuifengxiao.common.tool.text;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * PlaceholderExtractor工具类extractPlaceholdersWithDetails方法的单元测试
 */
public class PlaceholderExtractor_extractPlaceholdersWithDetails_Test {

    /**
     * 测试正常提取占位符详细信息的情况
     */
    @Test
    public void testExtractPlaceholdersWithDetails_NormalCase() {
        String input = "Hello ${name}, welcome to ${place}!";
        List<PlaceholderExtractor.PlaceholderInfo> result = PlaceholderExtractor.extractPlaceholdersWithDetails(input);
        
        assertEquals(2, result.size());
        
        PlaceholderExtractor.PlaceholderInfo first = result.get(0);
        assertEquals("name", first.getName());
        assertEquals("${name}", first.getFullPlaceholder());
        assertEquals(6, first.getStart());
        assertEquals(13, first.getEnd());
        
        PlaceholderExtractor.PlaceholderInfo second = result.get(1);
        assertEquals("place", second.getName());
        assertEquals("${place}", second.getFullPlaceholder());
        assertEquals(26, second.getStart());
        assertEquals(34, second.getEnd());
    }

    /**
     * 测试没有占位符的情况
     */
    @Test
    public void testExtractPlaceholdersWithDetails_NoPlaceholders() {
        String input = "Hello world, welcome to Beijing!";
        List<PlaceholderExtractor.PlaceholderInfo> result = PlaceholderExtractor.extractPlaceholdersWithDetails(input);
        
        assertTrue(result.isEmpty());
    }

    /**
     * 测试空字符串和null值
     */
    @Test
    public void testExtractPlaceholdersWithDetails_EmptyAndNull() {
        // 测试null值
        List<PlaceholderExtractor.PlaceholderInfo> result1 = PlaceholderExtractor.extractPlaceholdersWithDetails(null);
        assertTrue(result1.isEmpty());
        
        // 测试空字符串
        List<PlaceholderExtractor.PlaceholderInfo> result2 = PlaceholderExtractor.extractPlaceholdersWithDetails("");
        assertTrue(result2.isEmpty());
    }

    /**
     * 测试只有占位符的情况
     */
    @Test
    public void testExtractPlaceholdersWithDetails_OnlyPlaceholders() {
        String input = "${first}${second}${third}";
        List<PlaceholderExtractor.PlaceholderInfo> result = PlaceholderExtractor.extractPlaceholdersWithDetails(input);
        
        assertEquals(3, result.size());
        
        PlaceholderExtractor.PlaceholderInfo first = result.get(0);
        assertEquals("first", first.getName());
        assertEquals("${first}", first.getFullPlaceholder());
        assertEquals(0, first.getStart());
        assertEquals(8, first.getEnd());
        
        PlaceholderExtractor.PlaceholderInfo second = result.get(1);
        assertEquals("second", second.getName());
        assertEquals("${second}", second.getFullPlaceholder());
        assertEquals(8, second.getStart());
        assertEquals(17, second.getEnd());
        
        PlaceholderExtractor.PlaceholderInfo third = result.get(2);
        assertEquals("third", third.getName());
        assertEquals("${third}", third.getFullPlaceholder());
        assertEquals(17, third.getStart());
        assertEquals(25, third.getEnd());
    }

    /**
     * 测试特殊字符在占位符中的情况
     */
    @Test
    public void testExtractPlaceholdersWithDetails_SpecialCharacters() {
        String input = "Value: ${user-name}, ${user_name}, ${user123}";
        List<PlaceholderExtractor.PlaceholderInfo> result = PlaceholderExtractor.extractPlaceholdersWithDetails(input);
        
        assertEquals(3, result.size());
        
        PlaceholderExtractor.PlaceholderInfo first = result.get(0);
        assertEquals("user-name", first.getName());
        assertEquals("${user-name}", first.getFullPlaceholder());
        
        PlaceholderExtractor.PlaceholderInfo second = result.get(1);
        assertEquals("user_name", second.getName());
        assertEquals("${user_name}", second.getFullPlaceholder());
        
        PlaceholderExtractor.PlaceholderInfo third = result.get(2);
        assertEquals("user123", third.getName());
        assertEquals("${user123}", third.getFullPlaceholder());
    }
}