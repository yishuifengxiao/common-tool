package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonUtil_isJSONObject_Test {

    /**
     * 测试正常场景：输入为有效的JSON对象字符串
     * 预期结果：返回true
     */
    @Test
    public void testIsJSONObject_ValidJsonObject() {
        String input = "{\"name\":\"John\", \"age\":30}";
        boolean result = JsonUtil.isJSONObject(input);
        assertTrue(result);
    }

    /**
     * 测试边界场景：输入为JSON数组字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONObject_JsonArray() {
        String input = "[{\"name\":\"John\"}, {\"name\":\"Jane\"}]";
        boolean result = JsonUtil.isJSONObject(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：输入为空的JSON对象字符串
     * 预期结果：返回true
     */
    @Test
    public void testIsJSONObject_EmptyJsonObject() {
        String input = "{}";
        boolean result = JsonUtil.isJSONObject(input);
        assertTrue(result);
    }

    /**
     * 测试边界场景：输入为空白字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONObject_BlankString() {
        String input = "   ";
        boolean result = JsonUtil.isJSONObject(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：输入为null
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONObject_NullInput() {
        String input = null;
        boolean result = JsonUtil.isJSONObject(input);
        assertFalse(result);
    }

    /**
     * 测试异常场景：输入为无效的JSON格式字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSONObject_InvalidJson() {
        String input = "{name:John}";
        boolean result = JsonUtil.isJSONObject(input);
        assertFalse(result);
    }

    /**
     * 测试边界场景：输入为嵌套的JSON对象
     * 预期结果：返回true
     */
    @Test
    public void testIsJSONObject_NestedJsonObject() {
        String input = "{\"person\":{\"name\":\"John\",\"age\":30}}";
        boolean result = JsonUtil.isJSONObject(input);
        assertTrue(result);
    }
}
