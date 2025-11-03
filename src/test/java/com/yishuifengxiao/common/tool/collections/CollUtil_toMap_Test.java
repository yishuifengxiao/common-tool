package com.yishuifengxiao.common.tool.collections;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CollUtil_toMap_Test {

    /**
     * 测试正常场景：传入偶数个参数
     * 预期结果：正确转换为包含对应键值对的Map
     */
    @Test
    public void testToMap_EvenNumberOfParams() {
        Object[] params = {"key1", "value1", "key2", "value2"};
        
        Map<Object, Object> result = CollUtil.toMap(params);
        
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    /**
     * 测试边界场景：传入空参数数组
     * 预期结果：返回空Map
     */
    @Test
    public void testToMap_EmptyParams() {
        Object[] params = {};
        
        Map<Object, Object> result = CollUtil.toMap(params);
        
        assertTrue(result.isEmpty());
    }

    /**
     * 测试边界场景：传入null参数
     * 预期结果：返回空Map
     */
    @Test
    public void testToMap_NullParams() {
        Map<Object, Object> result = CollUtil.toMap(null);
        
        assertTrue(result.isEmpty());
    }

    /**
     * 测试异常场景：传入奇数个参数
     * 预期结果：抛出IllegalArgumentException异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToMap_OddNumberOfParams() {
        Object[] params = {"key1", "value1", "key2"};
        
        CollUtil.toMap(params);
    }

    /**
     * 测试正常场景：传入不同类型的键值对
     * 预期结果：正确转换为包含混合类型键值对的Map
     */
    @Test
    public void testToMap_MixedTypes() {
        Object[] params = {"stringKey", "stringValue", 1, 100, true, false};
        
        Map<Object, Object> result = CollUtil.toMap(params);
        
        assertEquals(3, result.size());
        assertEquals("stringValue", result.get("stringKey"));
        assertEquals(100, result.get(1));
        assertEquals(false, result.get(true));
    }

    /**
     * 测试正常场景：传入重复的键
     * 预期结果：后传入的值覆盖先传入的值
     */
    @Test
    public void testToMap_DuplicateKeys() {
        Object[] params = {"key", "value1", "key", "value2"};
        
        Map<Object, Object> result = CollUtil.toMap(params);
        
        assertEquals(1, result.size());
        assertEquals("value2", result.get("key"));
    }
}
