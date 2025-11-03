package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonUtil_strList_Test {

    private static class TestClass {
        private String name;
        private int age;

        public TestClass() {
        }

        public TestClass(String name, int age) {
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
     * 测试正常场景：有效的JSON字符串和类类型
     * 预期结果：成功转换为List集合
     */
    @Test
    public void testStr2List_NormalCase() {
        String json = "[{\"name\":\"Alice\",\"age\":25},{\"name\":\"Bob\",\"age\":30}]";
        List<TestClass> result = JsonUtil.str2List(json, TestClass.class, false);

        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals(25, result.get(0).getAge());
        assertEquals("Bob", result.get(1).getName());
        assertEquals(30, result.get(1).getAge());
    }

    /**
     * 测试边界场景：输入JSON字符串为null
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_NullJson() {
        List<TestClass> result = JsonUtil.str2List(null, TestClass.class, false);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试边界场景：输入类类型为null
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_NullClazz() {
        String json = "[{\"name\":\"Alice\",\"age\":25}]";
        List<TestClass> result = JsonUtil.str2List(json, null, false);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试边界场景：输入JSON字符串为空字符串
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_EmptyString() {
        List<TestClass> result = JsonUtil.str2List("", TestClass.class, false);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试边界场景：输入JSON字符串为空白字符串
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_BlankString() {
        List<TestClass> result = JsonUtil.str2List("   ", TestClass.class, false);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试边界场景：输入JSON字符串格式错误
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_InvalidJson() {
        String json = "[{\"name\":\"Alice\",\"age\":25}";
        List<TestClass> result = JsonUtil.str2List(json, TestClass.class, false);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试异常场景：failOnUnknownProperties为true时遇到未知属性
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_FailOnUnknownProperties() {
        String json = "[{\"name\":\"Alice\",\"age\":25,\"unknown\":\"value\"}]";
        List<TestClass> result = JsonUtil.str2List(json, TestClass.class, true);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试正常场景：failOnUnknownProperties为false时遇到未知属性
     * 预期结果：成功转换并忽略未知属性
     */
    @Test
    public void testStr2List_IgnoreUnknownProperties() {
        String json = "[{\"name\":\"Alice\",\"age\":25,\"unknown\":\"value\"}]";
        List<TestClass> result = JsonUtil.str2List(json, TestClass.class, false);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals(25, result.get(0).getAge());
    }

    /**
     * 测试边界场景：输入JSON字符串为空的JSON数组
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_EmptyJsonArray() {
        String json = "[]";
        List<TestClass> result = JsonUtil.str2List(json, TestClass.class, false);
        assertTrue(result.isEmpty());
    }
}
