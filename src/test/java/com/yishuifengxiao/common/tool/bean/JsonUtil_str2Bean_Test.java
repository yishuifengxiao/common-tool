package com.yishuifengxiao.common.tool.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@Slf4j
public class JsonUtil_str2Bean_Test {

    /**
     * 测试正常场景：将有效的JSON字符串转换为Java对象
     * 预期结果：成功转换并返回正确的Java对象
     */
    @Test
    public void testStr2Bean_ValidJson() {
        String json = "{\"name\":\"John\",\"age\":30}";
        Person result = JsonUtil.str2Bean(json, Person.class);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }

    /**
     * 测试边界场景：输入为null的JSON字符串
     * 预期结果：返回null
     */
    @Test
    public void testStr2Bean_NullInput() {
        Person result = JsonUtil.str2Bean(null, Person.class);
        assertNull(result);
    }

    /**
     * 测试边界场景：输入为空白字符串
     * 预期结果：返回null
     */
    @Test
    public void testStr2Bean_EmptyString() {
        Person result = JsonUtil.str2Bean("", Person.class);
        assertNull(result);
    }

    /**
     * 测试边界场景：输入为空白字符串（仅包含空格）
     * 预期结果：返回null
     */
    @Test
    public void testStr2Bean_WhitespaceString() {
        Person result = JsonUtil.str2Bean("   ", Person.class);
        assertNull(result);
    }

    /**
     * 测试异常场景：输入为无效的JSON字符串
     * 预期结果：返回null，并记录警告日志
     */
    @Test
    public void testStr2Bean_InvalidJson() {
        String invalidJson = "{invalid json}";
        Person result = JsonUtil.str2Bean(invalidJson, Person.class);
        assertNull(result);
    }

    /**
     * 测试异常场景：JSON字符串与目标类不匹配
     * 预期结果：返回null，并记录警告日志
     */
    @Test
    public void testStr2Bean_MismatchedJson() {
        String json = "{\"name\":\"John\",\"age\":30}";
        Address result = JsonUtil.str2Bean(json, Address.class);
        assertNull(result);
    }

    /**
     * 测试正常场景：将JSON字符串转换为Map对象
     * 预期结果：成功转换并返回正确的Map对象
     */
    @Test
    public void testStr2Bean_ToMap() {
        String json = "{\"name\":\"John\",\"age\":30}";
        Map<String, Object> result = JsonUtil.str2Bean(json, Map.class);

        assertNotNull(result);
        assertEquals("John", result.get("name"));
        assertEquals(30, result.get("age"));
    }

    /**
     * 测试正常场景：将JSON字符串转换为List对象
     * 预期结果：成功转换并返回正确的List对象
     */
    @Test
    public void testStr2Bean_ToList() {
        String json = "[{\"name\":\"John\"},{\"name\":\"Jane\"}]";
        List<Person> result = JsonUtil.str2Bean(json, List.class);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // 辅助类用于测试
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class Person {
        private String name;
        private int age;

    }

    private static class Address {
        private String street;
        private String city;

        // getters and setters
        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }
}
