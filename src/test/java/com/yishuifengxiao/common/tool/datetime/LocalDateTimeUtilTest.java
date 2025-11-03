package com.yishuifengxiao.common.tool.datetime;

import org.junit.Test;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

public class LocalDateTimeUtilTest {

    /**
     * 测试正常场景：使用默认格式解析标准日期时间字符串
     * 预期结果：成功解析为LocalDateTime对象
     */
    @Test
    public void testParse_StandardDateTime() {
        String timeStr = "2023-05-15 14:30:45";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    /**
     * 测试正常场景：使用自定义格式解析日期时间字符串
     * 预期结果：成功解析为LocalDateTime对象
     */
    @Test
    public void testParse_CustomFormat() {
        String timeStr = "15/05/2023 14.30.45";
        String pattern = "dd/MM/yyyy HH.mm.ss";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr, pattern);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    /**
     * 测试正常场景：解析仅包含日期的字符串
     * 预期结果：成功解析为当天的00:00:00的LocalDateTime对象
     */
    @Test
    public void testParse_DateOnly() {
        String timeStr = "2023-05-15";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }

    /**
     * 测试边界场景：解析紧凑格式的日期时间字符串
     * 预期结果：成功解析为LocalDateTime对象
     */
    @Test
    public void testParse_CompactDateTime() {
        String timeStr = "20230515143045";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    /**
     * 测试边界场景：解析ISO格式的日期时间字符串
     * 预期结果：成功解析为LocalDateTime对象
     */
    @Test
    public void testParse_ISOFormat() {
        String timeStr = "2023-05-15T14:30:45";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }

    /**
     * 测试边界场景：解析空字符串
     * 预期结果：返回null
     */
    @Test
    public void testParse_EmptyString() {
        String timeStr = "";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNull(result);
    }

    /**
     * 测试边界场景：解析null值
     * 预期结果：返回null
     */
    @Test
    public void testParse_NullInput() {
        LocalDateTime result = LocalDateTimeUtil.parse(null);
        
        assertNull(result);
    }

    /**
     * 测试异常场景：解析无法匹配任何格式的字符串
     * 预期结果：返回null
     */
    @Test
    public void testParse_InvalidFormat() {
        String timeStr = "Invalid Date Format";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNull(result);
    }

    /**
     * 测试边界场景：解析仅包含时间的字符串
     * 预期结果：返回null（因为该方法不支持仅包含时间的格式）
     */
    @Test
    public void testParse_TimeOnly() {
        String timeStr = "14:30:45";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNull(result);
    }

    /**
     * 测试边界场景：解析斜杠分隔的日期时间字符串
     * 预期结果：成功解析为LocalDateTime对象
     */
    @Test
    public void testParse_SlashSeparatedDateTime() {
        String timeStr = "2023/05/15 14:30:45";
        LocalDateTime result = LocalDateTimeUtil.parse(timeStr);
        
        assertNotNull(result);
        assertEquals(2023, result.getYear());
        assertEquals(5, result.getMonthValue());
        assertEquals(15, result.getDayOfMonth());
        assertEquals(14, result.getHour());
        assertEquals(30, result.getMinute());
        assertEquals(45, result.getSecond());
    }
}
