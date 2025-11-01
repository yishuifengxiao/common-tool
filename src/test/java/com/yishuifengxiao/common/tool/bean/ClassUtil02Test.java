package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 续写测试 ClassUtil.extractValue 方法
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassUtil02Test {

    // 定义测试使用的嵌套结构类
    static class User {
        public String name = "Alice";
        public Address address;
        public NullObj nullObj; // 用于模拟嵌套字段中途为 null 的情况
    }

    static class Address {
        public String street = "Main St.";
    }

    static class NullObj {
        public String dummy;
    }

    static class TestObject {
        public String name = "testName";
        private int age = 20;
        public User user = new User();
        public static String staticField = "staticValue";
        public transient String transientField = "transientValue";

        public void throwIllegalAccessException() throws IllegalAccessException {
            throw new IllegalAccessException("模拟非法访问异常");
        }
    }

    /**
     * 测试场景 T1: data 为 null
     */
    @Test
    public void testExtractValue_DataIsNull() {
        Object result = ClassUtil.extractValue(null, "name");
        assertNull(result);
    }

    /**
     * 测试场景 T2: fieldName 为空字符串
     */
    @Test
    public void testExtractValue_FieldNameIsEmpty() {
        TestObject obj = new TestObject();
        assertNull(ClassUtil.extractValue(obj, ""));
        assertNull(ClassUtil.extractValue(obj, " "));
        assertNull(ClassUtil.extractValue(obj, null));
    }

    /**
     * 测试场景 T3: fieldName 包含前后空格
     */
    @Test
    public void testExtractValue_FieldNameWithSpaces() {
        TestObject obj = new TestObject();
        Object result = ClassUtil.extractValue(obj, " name ");
        assertEquals("testName", result);
    }

    /**
     * 测试场景 T4: 简单字段存在且可访问
     */
    @Test
    public void testExtractValue_SimpleFieldExists() {
        TestObject obj = new TestObject();
        Object result = ClassUtil.extractValue(obj, "name");
        assertEquals("testName", result);
    }

    /**
     * 测试场景 T5: 嵌套字段路径正常
     */
    @Test
    public void testExtractValue_NestedFieldValidPath() {
        TestObject obj = new TestObject();
        obj.user.address = new Address(); // 初始化嵌套对象
        Object result = ClassUtil.extractValue(obj, "user.address.street");
        assertEquals("Main St.", result);
    }

    /**
     * 测试场景 T6: 嵌套字段中间某一级为 null
     */
    @Test
    public void testExtractValue_NestedFieldNullInBetween() {
        TestObject obj = new TestObject();
        obj.user.nullObj = null; // 中间层级为 null
        Object result = ClassUtil.extractValue(obj, "user.nullObj.dummy");
        assertNull(result);
    }

    /**
     * 测试场景 T7: 字段不存在
     */
    @Test
    public void testExtractValue_FieldDoesNotExist() {
        TestObject obj = new TestObject();
        Object result = ClassUtil.extractValue(obj, "nonExistentField");
        assertNull(result);
    }


}
