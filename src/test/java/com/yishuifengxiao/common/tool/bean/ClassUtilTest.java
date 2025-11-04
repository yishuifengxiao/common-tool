package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClassUtilTest {

    /**
     * 测试正常场景：提取一个简单类中的所有字段
     * 目的：验证方法能够正确提取类中定义的所有字段
     */
    @Test
    public void testFields_SimpleClass() {
        class SimpleClass {
            private String field1;
            public int field2;
        }

        List<Field> result = ClassUtil.fields(SimpleClass.class);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> f.getName().equals("field1")));
        assertTrue(result.stream().anyMatch(f -> f.getName().equals("field2")));
    }

    /**
     * 测试正常场景：提取一个继承类中的所有字段（包括父类字段）
     * 目的：验证方法能够正确提取类及其父类中定义的所有字段
     */
    @Test
    public void testFields_InheritedClass() {
        class ParentClass {
            private String parentField;
        }
        class ChildClass extends ParentClass {
            private String childField;
        }

        List<Field> result = ClassUtil.fields(ChildClass.class);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(f -> f.getName().equals("parentField")));
        assertTrue(result.stream().anyMatch(f -> f.getName().equals("childField")));
    }

    /**
     * 测试边界场景：提取一个没有字段的空类
     * 目的：验证方法能够正确处理没有字段的类
     */
    @Test
    public void testFields_EmptyClass() {
        class EmptyClass {}
        
        List<Field> result = ClassUtil.fields(EmptyClass.class);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试边界场景：提取Object类的字段
     * 目的：验证方法能够正确处理Object类（没有字段的情况）
     */
    @Test
    public void testFields_ObjectClass() {
        List<Field> result = ClassUtil.fields(Object.class);
        assertTrue(result.isEmpty());
    }

    /**
     * 测试异常场景：传入null参数
     * 目的：验证方法能够正确处理null输入
     */
    @Test(expected = NullPointerException.class)
    public void testFields_NullInput() {
        ClassUtil.fields(null);
    }
}
