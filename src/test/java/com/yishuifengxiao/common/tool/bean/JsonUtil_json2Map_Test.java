package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonUtil_json2Map_Test {

    /**
     * 测试正常场景：有效的JSON字符串转换为Map
     * 预期结果：成功转换为Map对象
     */
    @Test
    public void testJson2Map_ValidJson() {
        String json = "{\"name\":\"John\", \"age\":30}";

        Map<String, Object> result = JsonUtil.json2Map(json);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    /**
     * 测试边界场景：空字符串输入
     * 预期结果：返回null
     */
    @Test
    public void testJson2Map_EmptyString() {
        String json = "";

        Map<String, Object> result = JsonUtil.json2Map(json);

        assertNull(result);
    }

    /**
     * 测试边界场景：空白字符串输入
     * 预期结果：返回null
     */
    @Test
    public void testJson2Map_BlankString() {
        String json = "   ";

        Map<String, Object> result = JsonUtil.json2Map(json);

        assertNull(result);
    }


    /**
     * 测试异常场景：无效的JSON格式字符串
     * 预期结果：返回null
     */
    @Test
    public void testJson2Map_InvalidJson() {
        String json = "{invalid json}";

        Map<String, Object> result = JsonUtil.json2Map(json);

        assertNull(result);
    }

    /**
     * 测试边界场景：null输入
     * 预期结果：返回null
     */
    @Test
    public void testJson2Map_NullInput() {
        Map<String, Object> result = JsonUtil.json2Map(null);

        assertNull(result);
    }

    /**
     * 测试正常场景：嵌套JSON对象转换
     * 预期结果：成功转换为包含嵌套Map的Map对象
     */
    @Test
    public void testJson2Map_NestedJson() {
        String json = "{\"person\":{\"name\":\"John\", \"age\":30}}";

        Map<String, Object> result = JsonUtil.json2Map(json);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get("person") instanceof Map);
        Map<?, ?> person = (Map<?, ?>) result.get("person");
        assertEquals("John", person.get("name"));
        assertEquals(30, person.get("age"));
    }

    /**
     * 测试正常场景：JSON数组转换
     * 预期结果：成功转换为包含List的Map对象
     */
    @Test
    public void testJson2Map_JsonWithArray() {
        String json = "{\"names\":[\"John\", \"Jane\"]}";

        Map<String, Object> result = JsonUtil.json2Map(json);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get("names") instanceof List);
        List<?> names = (List<?>) result.get("names");
        assertEquals(2, names.size());
        assertTrue(names.contains("John"));
        assertTrue(names.contains("Jane"));
    }
}
