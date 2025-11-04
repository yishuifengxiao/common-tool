package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClassUtil_extractValue_Test {

    /**
     * 测试正常场景：获取简单属性的值
     * 场景描述：当传入的对象包含简单属性时，应能正确获取该属性的值
     * 预期结果：返回属性对应的值
     */
    @Test
    public void testExtractValue_SimpleField() {
        TestObject testObj = new TestObject();
        testObj.setName("testName");

        Object result = ClassUtil.extractValue(testObj, "name");

        assertEquals("testName", result);
    }

    /**
     * 测试正常场景：获取嵌套属性的值
     * 场景描述：当传入的对象包含嵌套属性时，应能正确获取嵌套属性的值
     * 预期结果：返回嵌套属性对应的值
     */
    @Test
    public void testExtractValue_NestedField() {
        TestObject testObj = new TestObject();
        TestObject.NestedObject nested = new TestObject.NestedObject();
        nested.setValue("nestedValue");
        testObj.setNested(nested);

        Object result = ClassUtil.extractValue(testObj, "nested.value");

        assertEquals("nestedValue", result);
    }

    /**
     * 测试边界场景：传入null对象
     * 场景描述：当传入的对象为null时，应返回null
     * 预期结果：返回null
     */
    @Test
    public void testExtractValue_NullObject() {
        Object result = ClassUtil.extractValue(null, "name");

        assertNull(result);
    }

    /**
     * 测试边界场景：传入空属性名
     * 场景描述：当传入的属性名为空字符串时，应返回null
     * 预期结果：返回null
     */
    @Test
    public void testExtractValue_EmptyFieldName() {
        TestObject testObj = new TestObject();

        Object result = ClassUtil.extractValue(testObj, "");

        assertNull(result);
    }

    /**
     * 测试边界场景：传入空白属性名
     * 场景描述：当传入的属性名为空白字符串时，应返回null
     * 预期结果：返回null
     */
    @Test
    public void testExtractValue_BlankFieldName() {
        TestObject testObj = new TestObject();

        Object result = ClassUtil.extractValue(testObj, "   ");

        assertNull(result);
    }

    /**
     * 测试边界场景：属性不存在
     * 场景描述：当传入的属性名在对象中不存在时，应返回null
     * 预期结果：返回null
     */
    @Test
    public void testExtractValue_NonExistentField() {
        TestObject testObj = new TestObject();

        Object result = ClassUtil.extractValue(testObj, "nonExistentField");

        assertNull(result);
    }

    /**
     * 测试边界场景：嵌套属性中间节点为null
     * 场景描述：当嵌套属性的中间节点为null时，应返回null
     * 预期结果：返回null
     */
    @Test
    public void testExtractValue_NullMiddleNode() {
        TestObject testObj = new TestObject();
        testObj.setNested(null);

        Object result = ClassUtil.extractValue(testObj, "nested.value");

        assertNull(result);
    }

    /**
     * 测试边界场景：嵌套属性不存在
     * 场景描述：当嵌套属性的某个节点不存在时，应返回null
     * 预期结果：返回null
     */
    @Test
    public void testExtractValue_NonExistentNestedField() {
        TestObject testObj = new TestObject();
        TestObject.NestedObject nested = new TestObject.NestedObject();
        testObj.setNested(nested);

        Object result = ClassUtil.extractValue(testObj, "nested.nonExistentField");

        assertNull(result);
    }

    /**
     * 测试边界场景：Map类型对象获取值
     * 场景描述：当传入的对象是Map类型时，应能正确获取指定key的值
     * 预期结果：返回Map中对应key的值
     */
    @Test
    public void testExtractValue_MapObject() {
        Map<String, Object> map = Collections.singletonMap("key", "value");

        Object result = ClassUtil.extractValue(map, "key");

        assertEquals("value", result);
    }

    /**
     * 测试边界场景：Map类型对象获取嵌套值
     * 场景描述：当传入的对象是Map类型且包含嵌套Map时，应能正确获取嵌套key的值
     * 预期结果：返回嵌套Map中对应key的值
     */
    @Test
    public void testExtractValue_NestedMapObject() {
        Map<String, Object> innerMap = Collections.singletonMap("innerKey", "innerValue");
        Map<String, Object> outerMap = Collections.singletonMap("outerKey", innerMap);

        Object result = ClassUtil.extractValue(outerMap, "outerKey.innerKey");

        assertEquals("innerValue", result);
    }

    private static class TestObject {
        private String name;
        private NestedObject nested;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public NestedObject getNested() {
            return nested;
        }

        public void setNested(NestedObject nested) {
            this.nested = nested;
        }

        public static class NestedObject {
            private String value;

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }
}
