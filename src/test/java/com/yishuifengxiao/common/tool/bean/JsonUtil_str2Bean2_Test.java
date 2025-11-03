package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import static org.junit.Assert.*;

public class JsonUtil_str2Bean2_Test {

    static class TestBean {
        private String name;
        private int age;

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
     * 测试正常场景：将有效的JSON字符串转换为Java对象
     * 预期结果：成功转换并返回正确的Java对象
     */
    @Test
    public void testStr2Bean_ValidJson() throws Exception {
        String json = "{\"name\":\"John\",\"age\":30}";
        TestBean result = JsonUtil.str2Bean(json, TestBean.class, false);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }

    /**
     * 测试边界场景：输入为空白字符串
     * 预期结果：返回null
     */
    @Test
    public void testStr2Bean_BlankString() {
        String json = "   ";
        TestBean result = JsonUtil.str2Bean(json, TestBean.class, false);

        assertNull(result);
    }

    /**
     * 测试边界场景：输入为null
     * 预期结果：返回null
     */
    @Test
    public void testStr2Bean_NullInput() {
        TestBean result = JsonUtil.str2Bean(null, TestBean.class, false);

        assertNull(result);
    }

    /**
     * 测试异常场景：JSON字符串格式错误
     * 预期结果：返回null
     */
    @Test
    public void testStr2Bean_InvalidJson() {
        String json = "{invalid json}";
        TestBean result = JsonUtil.str2Bean(json, TestBean.class, false);

        assertNull(result);
    }

    /**
     * 测试配置场景：failOnUnknownProperties为true时遇到未知属性
     * 预期结果：返回null
     */
    @Test
    public void testStr2Bean_FailOnUnknownPropertiesTrue() {
        String json = "{\"name\":\"John\",\"age\":30,\"unknown\":\"value\"}";
        TestBean result = JsonUtil.str2Bean(json, TestBean.class, true);

        assertNull(result);
    }

    /**
     * 测试配置场景：failOnUnknownProperties为false时遇到未知属性
     * 预期结果：成功转换并忽略未知属性
     */
    @Test
    public void testStr2Bean_FailOnUnknownPropertiesFalse() {
        String json = "{\"name\":\"John\",\"age\":30,\"unknown\":\"value\"}";
        TestBean result = JsonUtil.str2Bean(json, TestBean.class, false);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }

    /**
     * 测试边界场景：JSON字符串包含额外空格
     * 预期结果：成功转换并返回正确的Java对象
     */
    @Test
    public void testStr2Bean_JsonWithWhitespace() throws Exception {
        String json = "  { \"name\" : \"John\" , \"age\" : 30 }  ";
        TestBean result = JsonUtil.str2Bean(json, TestBean.class, false);

        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
    }
}
