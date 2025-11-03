package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonUtil_isJSON_Test {

    /**
     * 测试正常场景：输入为有效的JSON字符串
     * 预期结果：返回true
     */
    @Test
    public void testIsJSON_ValidJson() {
        String json = "{\"name\":\"John\", \"age\":30}";
        boolean result = JsonUtil.isJSON(json);
        assertTrue(result);
    }

    /**
     * 测试正常场景：输入为有效的JSON数组字符串
     * 预期结果：返回true
     */
    @Test
    public void testIsJSON_ValidJsonArray() {
        String json = "[{\"name\":\"John\"}, {\"name\":\"Jane\"}]";
        boolean result = JsonUtil.isJSON(json);
        assertTrue(result);
    }

    /**
     * 测试边界场景：输入为空白字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSON_BlankString() {
        String json = "   ";
        boolean result = JsonUtil.isJSON(json);
        assertFalse(result);
    }

    /**
     * 测试边界场景：输入为空字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSON_EmptyString() {
        String json = "";
        boolean result = JsonUtil.isJSON(json);
        assertFalse(result);
    }

    /**
     * 测试边界场景：输入为null
     * 预期结果：返回false
     */
    @Test
    public void testIsJSON_NullInput() {
        boolean result = JsonUtil.isJSON(null);
        assertFalse(result);
    }

    /**
     * 测试异常场景：输入为无效的JSON字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSON_InvalidJson() {
        String json = "{name:John}";
        boolean result = JsonUtil.isJSON(json);
        assertFalse(result);
    }

    /**
     * 测试异常场景：输入为格式错误的JSON字符串
     * 预期结果：返回false
     */
    @Test
    public void testIsJSON_MalformedJson() {
        String json = "{\"name\":\"John\", \"age\":}";
        boolean result = JsonUtil.isJSON(json);
        assertFalse(result);
    }

    /**
     * 测试异常场景：输入为普通字符串而非JSON
     * 预期结果：返回false
     */
    @Test
    public void testIsJSON_PlainText() {
        String json = "This is not a JSON";
        boolean result = JsonUtil.isJSON(json);
        assertFalse(result);
    }
}
