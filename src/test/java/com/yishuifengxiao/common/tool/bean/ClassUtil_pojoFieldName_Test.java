package com.yishuifengxiao.common.tool.bean;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@Slf4j
public class ClassUtil_pojoFieldName_Test {

    /**
     * 测试正常场景：获取普通getter方法对应的属性名
     * 预期结果：返回正确的属性名
     */
    @Test
    public void testPojoFieldName_NormalGetter() {
        ClassUtil.SerFunction<TestPojo, String> function = TestPojo::getName;
        String result = ClassUtil.pojoFieldName(function);
        assertEquals("name", result);
    }

    /**
     * 测试正常场景：获取boolean类型isXxx方法对应的属性名
     * 预期结果：返回正确的属性名
     */
    @Test
    public void testPojoFieldName_BooleanGetter() {
        ClassUtil.SerFunction<TestPojo, Boolean> function = TestPojo::isActive;
        String result = ClassUtil.pojoFieldName(function);
        assertEquals("active", result);
    }

    /**
     * 测试边界场景：传入null作为参数
     * 预期结果：返回null
     */
    @Test
    public void testPojoFieldName_NullInput() {
        String result = ClassUtil.pojoFieldName(null);
        assertNull(result);
    }

    /**
     * 测试异常场景：传入非lambda表达式
     * 预期结果：返回null
     */
    @Test
    public void testPojoFieldName_NonLambdaFunction() {
        ClassUtil.SerFunction<TestPojo, String> function = new ClassUtil.SerFunction<TestPojo, String>() {
            @Override
            public String apply(TestPojo testPojo) {
                return testPojo.getName();
            }
        };
        String result = ClassUtil.pojoFieldName(function);
        assertNull(result);
    }

    /**
     * 测试异常场景：传入不符合命名规范的getter方法
     * 预期结果：返回null
     */
    @Test
    public void testPojoFieldName_InvalidGetterName() {
        // 创建一个不符合命名规范的getter方法（不以get或is开头）
        ClassUtil.SerFunction<TestPojo, String> function = TestPojo::invalidGetter;
        String result = ClassUtil.pojoFieldName(function);
        assertNull(result);
    }

    /**
     * 测试内部类用于测试pojoFieldName方法
     */
    private static class TestPojo implements Serializable {
        private String name;
        private boolean active;
        private String invalid;

        public String getName() {
            return name;
        }

        public boolean isActive() {
            return active;
        }

        public String getInvalid() {
            return invalid;
        }

        // 添加一个不符合命名规范的getter方法
        public String invalidGetter() {
            return invalid;
        }
    }
}
