package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonUtil_isJSONArray_Test {

    /**
     * 测试正常场景：有效的JSON数组字符串
     * 预期结果：返回true
     */
    @Test
    public void testIsJSONArray_ValidJSONArray() {
        String input = "[1, 2, 3]";
        boolean result = JsonUtil.isJSONArray(input);
        assertTrue(result);
    }

    /**
     * 测试正常场景：有效的空JSON数组字符串
     * 预期结果：返回true
     */
    @Test
    public void testIsJSONArray_ValidEmptyJSONArray() {
        String input = "[]";
        boolean result = JsonUtil.isJSONArray(input);
        assertTrue(result);
    }

    /**
     * 测试边界场景：字符串为null
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONArray_NullInput() {
        String input = null;
        boolean result = JsonUtil.isJSONArray(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：字符串为空字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONArray_EmptyString() {
        String input = "";
        boolean result = JsonUtil.isJSONArray(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：字符串为空白字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONArray_BlankString() {
        String input = "   ";
        boolean result = JsonUtil.isJSONArray(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：字符串以[开头但不以]结尾
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONArray_StartsWithBracketOnly() {
        String input = "[1, 2, 3";
        boolean result = JsonUtil.isJSONArray(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：字符串以]结尾但不以[开头
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONArray_EndsWithBracketOnly() {
        String input = "1, 2, 3]";
        boolean result = JsonUtil.isJSONArray(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：字符串包含JSON对象而非数组
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONArray_JSONObject() {
        String input = "{\"key\": \"value\"}";
        boolean result = JsonUtil.isJSONArray(input);
        assertFalse(result);
    }

    /**
     * 测试异常场景：无效的JSON格式字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONArray_InvalidJSON() {
        String input = "[1, 2, 3";
        boolean result = JsonUtil.isJSONArray(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：字符串包含JSON数组但前后有空格
     * 预期结果：返回true（方法内部会trim）
     */
    @Test
    public void testIsJSONArray_WithWhitespace() {
        String input = "  [1, 2, 3]  ";
        boolean result = JsonUtil.isJSONArray(input);
        assertTrue(result);
    }
}
