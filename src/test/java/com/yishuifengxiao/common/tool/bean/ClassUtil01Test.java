package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * 测试 ClassUtil.extractValue 方法
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassUtil01Test {

    // 定义一个测试用的类
    static class TestObject {
        public String name = "testName";
        private int age = 20;
        public static String staticField = "staticValue";
        public transient String transientField = "transientValue";

        public void throwIllegalAccessException() throws IllegalAccessException {
            throw new IllegalAccessException("模拟非法访问异常");
        }
    }

    /**
     * 测试场景 B1: data == null
     */
    @Test
    public void testExtractValue_DataIsNull() {
        Object result = ClassUtil.extractValue(null, "name");
        assertNull(result);
    }

    /**
     * 测试场景 B2: fieldName 为空或空白
     */
    @Test
    public void testExtractValue_FieldNameIsBlank() {
        TestObject obj = new TestObject();
        assertNull(ClassUtil.extractValue(obj, null));
        assertNull(ClassUtil.extractValue(obj, ""));
        assertNull(ClassUtil.extractValue(obj, " "));
    }

    /**
     * 测试场景 B3: 字段不存在
     */
    @Test
    public void testExtractValue_FieldNotExists() {
        TestObject obj = new TestObject();
        try (MockedStatic<ClassUtil> mocked = Mockito.mockStatic(ClassUtil.class)) {
            mocked.when(() -> ClassUtil.fields(any())).thenReturn(new ArrayList<>());
            Object result = ClassUtil.extractValue(obj, "nonExistentField");
            assertNull(result);
        }
    }

    /**
     * 测试场景 B4 & B5: 字段存在且能正确获取值
     */
    @Test
    public void testExtractValue_NormalField() throws NoSuchFieldException {
        TestObject obj = new TestObject();

        Field nameField = TestObject.class.getDeclaredField("name");
        List<Field> fieldList = new ArrayList<>();
        fieldList.add(nameField);

        try (MockedStatic<ClassUtil> mocked = Mockito.mockStatic(ClassUtil.class)) {
            mocked.when(() -> ClassUtil.fields(TestObject.class)).thenReturn(fieldList);
            Object result = ClassUtil.extractValue(obj, "name");
            assertEquals("testName", result);
        }
    }

    /**
     * 测试场景 B6: IllegalAccessException 异常处理
     */
    @Test
    public void testExtractValue_IllegalAccessExceptionHandled() throws Exception {
        TestObject obj = new TestObject();

        Field illegalAccessField = TestObject.class.getDeclaredField("age");
        List<Field> fieldList = new ArrayList<>();
        fieldList.add(illegalAccessField);

        try (MockedStatic<ClassUtil> mocked = Mockito.mockStatic(ClassUtil.class)) {
            mocked.when(() -> ClassUtil.fields(TestObject.class)).thenReturn(fieldList);

            // 使用反射伪造抛出异常
            doThrow(new IllegalAccessException("模拟异常")).when(illegalAccessField).setAccessible(true);

            Object result = ClassUtil.extractValue(obj, "age");
            assertNull(result); // 应该捕获异常并返回 null
        }
    }

    /**
     * 测试场景 B7: 通用 Exception 处理
     */
    @Test
    public void testExtractValue_GenericExceptionHandled() throws Exception {
        TestObject obj = new TestObject();

        Field genericExceptionField = TestObject.class.getDeclaredField("age");
        List<Field> fieldList = new ArrayList<>();
        fieldList.add(genericExceptionField);

        try (MockedStatic<ClassUtil> mocked = Mockito.mockStatic(ClassUtil.class)) {
            mocked.when(() -> ClassUtil.fields(TestObject.class)).thenReturn(fieldList);

            // 模拟 get 方法抛出异常
            when(genericExceptionField.get(obj)).thenThrow(new RuntimeException("Generic exception"));

            Object result = ClassUtil.extractValue(obj, "age");
            assertNull(result); // 应该捕获异常并返回 null
        }
    }
}
