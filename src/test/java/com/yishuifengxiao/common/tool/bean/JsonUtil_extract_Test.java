package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsonUtil_extract_Test {

    private static class TestObject {
        private String name;
        private int age;

        public TestObject() {
        }

        public TestObject(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    /**
     * 测试正常场景：从JSON字符串中提取并转换为指定对象
     * 预期结果：成功提取并转换为TestObject对象
     */
    @Test
    public void testExtract_NormalCase() {
        String json = "{\"person\": {\"name\": \"John\", \"age\": 30}}";
        String jsonPath = "$.person";
        TestObject result = JsonUtil.extract(json, jsonPath, TestObject.class);
        
        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }

    /**
     * 测试边界场景：JSON路径不存在
     * 预期结果：返回null
     */
    @Test
    public void testExtract_JsonPathNotExist() {
        String json = "{\"person\": {\"name\": \"John\", \"age\": 30}}";
        String jsonPath = "$.notExist";
        TestObject result = JsonUtil.extract(json, jsonPath, TestObject.class);
        
        assertNull(result);
    }

    /**
     * 测试边界场景：提取结果为null
     * 预期结果：返回null
     */
    @Test
    public void testExtract_ExtractedDataIsNull() {
        String json = "{\"person\": null}";
        String jsonPath = "$.person";
        TestObject result = JsonUtil.extract(json, jsonPath, TestObject.class);
        
        assertNull(result);
    }

    /**
     * 测试边界场景：提取结果toString为null
     * 预期结果：返回null
     */
    @Test
    public void testExtract_ExtractedDataToStringIsNull() {
        String json = "{\"person\": {}}";
        String jsonPath = "$.person";
        TestObject result = JsonUtil.extract(json, jsonPath, TestObject.class);
        
        assertNull(result);
    }

    /**
     * 测试异常场景：JSON字符串为null
     * 预期结果：抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExtract_JsonIsNull() {
        String json = null;
        String jsonPath = "$.person";
        JsonUtil.extract(json, jsonPath, TestObject.class);
    }

    /**
     * 测试异常场景：JSON路径为null
     * 预期结果：抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExtract_JsonPathIsNull() {
        String json = "{\"person\": {\"name\": \"John\", \"age\": 30}}";
        String jsonPath = null;
        JsonUtil.extract(json, jsonPath, TestObject.class);
    }

    /**
     * 测试异常场景：目标类为null
     * 预期结果：抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExtract_ClassIsNull() {
        String json = "{\"person\": {\"name\": \"John\", \"age\": 30}}";
        String jsonPath = "$.person";
        JsonUtil.extract(json, jsonPath, null);
    }

    /**
     * 测试边界场景：JSON字符串为空
     * 预期结果：抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExtract_JsonIsEmpty() {
        String json = "";
        String jsonPath = "$.person";
        JsonUtil.extract(json, jsonPath, TestObject.class);
    }

    /**
     * 测试边界场景：JSON路径为空
     * 预期结果：抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testExtract_JsonPathIsEmpty() {
        String json = "{\"person\": {\"name\": \"John\", \"age\": 30}}";
        String jsonPath = "";
        JsonUtil.extract(json, jsonPath, TestObject.class);
    }
}
