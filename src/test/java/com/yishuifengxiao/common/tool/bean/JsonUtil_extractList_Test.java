package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class JsonUtil_extractList_Test {

    private static final String TEST_JSON = "{\"users\":[{\"name\":\"Alice\",\"age\":25},{\"name\":\"Bob\",\"age\":30}]}";
    private static final String TEST_JSON_PATH = "$.users";
    private static final String INVALID_JSON_PATH = "$.invalid";

    /**
     * 测试正常场景：从有效JSON字符串中提取并转换对象列表
     * 验证方法能够正确解析JSON字符串并根据jsonPath提取出对应的对象列表
     */
    @Test
    public void testExtractList_NormalCase() {
        List<HashMap> result = JsonUtil.extractList(TEST_JSON, TEST_JSON_PATH, HashMap.class);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).get("name"));
        assertEquals(25, result.get(0).get("age"));
        assertEquals("Bob", result.get(1).get("name"));
        assertEquals(30, result.get(1).get("age"));
    }

    /**
     * 测试边界场景：JSON字符串为null
     * 验证方法对null输入的处理，预期返回null
     */
    @Test
    public void testExtractList_NullJson() {
        List<HashMap> result = JsonUtil.extractList(null, TEST_JSON_PATH, HashMap.class);

        assertNull(result);
    }

    /**
     * 测试边界场景：jsonPath为null
     * 验证方法对null jsonPath的处理，预期返回null
     */
    @Test
    public void testExtractList_NullJsonPath() {
        List<HashMap> result = JsonUtil.extractList(TEST_JSON, null, HashMap.class);

        assertNull(result);
    }

    /**
     * 测试边界场景：目标类为null
     * 验证方法对null目标类的处理，预期返回null
     */
    @Test
    public void testExtractList_NullClazz() {
        List<HashMap> result = JsonUtil.extractList(TEST_JSON, TEST_JSON_PATH, null);

        assertNull(result);
    }

    /**
     * 测试边界场景：无效的jsonPath
     * 验证方法对无效jsonPath的处理，预期返回null
     */
    @Test
    public void testExtractList_InvalidJsonPath() {
        List<HashMap> result = JsonUtil.extractList(TEST_JSON, INVALID_JSON_PATH, HashMap.class);

        assertNull(result);
    }

    /**
     * 测试边界场景：空JSON字符串
     * 验证方法对空字符串的处理，预期返回null
     */
    @Test
    public void testExtractList_EmptyJson() {
        List<HashMap> result = JsonUtil.extractList("", TEST_JSON_PATH, HashMap.class);

        assertNull(result);
    }

    /**
     * 测试边界场景：空jsonPath
     * 验证方法对空jsonPath的处理，预期返回null
     */
    @Test
    public void testExtractList_EmptyJsonPath() {
        List<HashMap> result = JsonUtil.extractList(TEST_JSON, "", HashMap.class);

        assertNull(result);
    }

    // 添加静态内部类定义
    private static class User {
        private String name;
        private int age;

        // 默认构造函数（Jackson需要）
        public User() {
        }

        // getters and setters
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
     * 测试正常场景：提取并转换为自定义对象列表
     * 验证方法能够正确解析JSON字符串并转换为自定义对象列表
     */
    @Test
    public void testExtractList_CustomObject() {
        List<User> result = JsonUtil.extractList(TEST_JSON, TEST_JSON_PATH, User.class);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals(25, result.get(0).getAge());
        assertEquals("Bob", result.get(1).getName());
        assertEquals(30, result.get(1).getAge());
    }
}
