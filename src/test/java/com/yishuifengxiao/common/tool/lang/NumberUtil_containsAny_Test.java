package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NumberUtil_containsAny_Test {

    /**
     * 测试正常场景：目标数据存在于待比较数据中
     * 验证当目标数据存在于待比较数据中时，方法返回true
     */
    @Test
    public void testContainsAny_ValueExists() {
        Number originalValue = 5;
        Number[] values = {1, 2, 3, 4, 5};
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertTrue(result);
    }

    /**
     * 测试正常场景：目标数据不存在于待比较数据中
     * 验证当目标数据不存在于待比较数据中时，方法返回false
     */
    @Test
    public void testContainsAny_ValueNotExists() {
        Number originalValue = 6;
        Number[] values = {1, 2, 3, 4, 5};
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertFalse(result);
    }

    /**
     * 测试边界场景：目标数据为null
     * 验证当目标数据为null时，方法返回false
     */
    @Test
    public void testContainsAny_NullOriginalValue() {
        Number originalValue = null;
        Number[] values = {1, 2, 3, 4, 5};
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertFalse(result);
    }

    /**
     * 测试边界场景：待比较数据为null
     * 验证当待比较数据为null时，方法返回false
     */
    @Test
    public void testContainsAny_NullValues() {
        Number originalValue = 5;
        Number[] values = null;
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertFalse(result);
    }

    /**
     * 测试边界场景：待比较数据中包含null值
     * 验证当待比较数据中包含null值时，方法能正确处理并返回正确结果
     */
    @Test
    public void testContainsAny_ValuesContainsNull() {
        Number originalValue = 5;
        Number[] values = {1, null, 3, 4, 5};
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertTrue(result);
    }

    /**
     * 测试边界场景：待比较数据全部为null
     * 验证当待比较数据全部为null时，方法返回false
     */
    @Test
    public void testContainsAny_AllValuesNull() {
        Number originalValue = 5;
        Number[] values = {null, null, null};
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertFalse(result);
    }

    /**
     * 测试边界场景：待比较数据为空数组
     * 验证当待比较数据为空数组时，方法返回false
     */
    @Test
    public void testContainsAny_EmptyValues() {
        Number originalValue = 5;
        Number[] values = {};
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertFalse(result);
    }

    /**
     * 测试正常场景：比较不同类型但数值相等的Number对象
     * 验证方法能正确比较不同类型但数值相等的Number对象
     */
    @Test
    public void testContainsAny_DifferentNumberTypes() {
        Number originalValue = 5;
        Number[] values = {5L, 5.0, new BigDecimal("5")};
        
        boolean result = NumberUtil.containsAny(originalValue, values);
        
        assertTrue(result);
    }
}
