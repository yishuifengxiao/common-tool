package com.yishuifengxiao.common.tool.datetime;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DateTimeUtilTest {

    /**
     * 测试正常场景：使用默认格式解析有效日期字符串
     * 预期结果：成功解析并返回对应的Date对象
     */
    @Test
    public void testParseDate_WithDefaultPatterns() throws Exception {
        String timeStr = "2023-05-15";
        Date result = DateTimeUtil.parseDate(timeStr);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date expected = sdf.parse(timeStr);
        assertEquals(expected, result);
    }

    /**
     * 测试正常场景：使用自定义格式解析有效日期字符串
     * 预期结果：成功解析并返回对应的Date对象
     */
    @Test
    public void testParseDate_WithCustomPattern() throws Exception {
        String timeStr = "15/05/2023";
        String pattern = "dd/MM/yyyy";
        Date result = DateTimeUtil.parseDate(timeStr, pattern);

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date expected = sdf.parse(timeStr);
        assertEquals(expected, result);
    }

    /**
     * 测试正常场景：使用多个自定义格式解析有效日期字符串
     * 预期结果：成功解析并返回对应的Date对象
     */
    @Test
    public void testParseDate_WithMultiplePatterns() throws Exception {
        String timeStr = "20230515";
        Date result = DateTimeUtil.parseDate(timeStr, "yyyyMMdd", "yyyy-MM-dd");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date expected = sdf.parse(timeStr);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：传入空字符串
     * 预期结果：返回null
     */
    @Test
    public void testParseDate_WithEmptyString() {
        String timeStr = "";
        Date result = DateTimeUtil.parseDate(timeStr);
        assertNull(result);
    }

    /**
     * 测试边界场景：传入null字符串
     * 预期结果：返回null
     */
    @Test
    public void testParseDate_WithNullString() {
        String timeStr = null;
        Date result = DateTimeUtil.parseDate(timeStr);
        assertNull(result);
    }

    /**
     * 测试边界场景：传入null格式数组
     * 预期结果：使用默认格式成功解析
     */
    @Test
    public void testParseDate_WithNullPatterns() throws Exception {
        String timeStr = "2023-05-15";
        Date result = DateTimeUtil.parseDate(timeStr, (String[]) null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date expected = sdf.parse(timeStr);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：传入包含null元素的格式数组
     * 预期结果：过滤掉null元素后成功解析
     */
    @Test
    public void testParseDate_WithPatternsContainingNull() throws Exception {
        String timeStr = "15/05/2023";
        Date result = DateTimeUtil.parseDate(timeStr, null, "dd/MM/yyyy", null);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date expected = sdf.parse(timeStr);
        assertEquals(expected, result);
    }

    /**
     * 测试异常场景：传入无法解析的日期字符串
     * 预期结果：抛出UncheckedException
     */
    @Test(expected = UncheckedException.class)
    public void testParseDate_WithInvalidDateString() {
        String timeStr = "invalid-date";
        DateTimeUtil.parseDate(timeStr);
    }

    /**
     * 测试异常场景：传入不匹配的格式
     * 预期结果：抛出UncheckedException
     */
    @Test(expected = UncheckedException.class)
    public void testParseDate_WithUnmatchedPattern() {
        String timeStr = "2023-05-15";
        DateTimeUtil.parseDate(timeStr, "dd/MM/yyyy");
    }
}
