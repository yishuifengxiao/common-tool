package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BoolUtil_parse_Test {

    /**
     * 测试正常场景：输入为字符串"true"(忽略大小写)
     * 预期结果：返回true
     */
    @Test
    public void testParse_TrueTextIgnoreCase() {
        assertEquals(Boolean.TRUE, BoolUtil.parse("true"));
        assertEquals(Boolean.TRUE, BoolUtil.parse("TRUE"));
        assertEquals(Boolean.TRUE, BoolUtil.parse("True"));
        assertEquals(Boolean.TRUE, BoolUtil.parse(" tRuE "));
    }

    /**
     * 测试正常场景：输入为字符串"false"(忽略大小写)
     * 预期结果：返回false
     */
    @Test
    public void testParse_FalseTextIgnoreCase() {
        assertEquals(Boolean.FALSE, BoolUtil.parse("false"));
        assertEquals(Boolean.FALSE, BoolUtil.parse("FALSE"));
        assertEquals(Boolean.FALSE, BoolUtil.parse("False"));
        assertEquals(Boolean.FALSE, BoolUtil.parse(" fAlSe "));
    }

    /**
     * 测试正常场景：输入为数字且大于0
     * 预期结果：返回true
     */
    @Test
    public void testParse_PositiveNumber() {
        assertEquals(Boolean.TRUE, BoolUtil.parse("1"));
        assertEquals(Boolean.TRUE, BoolUtil.parse("100"));
        assertEquals(Boolean.TRUE, BoolUtil.parse("0.1"));
    }

    /**
     * 测试正常场景：输入为数字且小于等于0
     * 预期结果：返回false
     */
    @Test
    public void testParse_NonPositiveNumber() {
        assertEquals(Boolean.FALSE, BoolUtil.parse("0"));
        assertEquals(Boolean.FALSE, BoolUtil.parse("-1"));
        assertEquals(Boolean.FALSE, BoolUtil.parse("-0.1"));
    }

    /**
     * 测试边界场景：输入为null
     * 预期结果：返回null
     */
    @Test
    public void testParse_NullInput() {
        assertNull(BoolUtil.parse(null));
    }

    /**
     * 测试边界场景：输入为空字符串
     * 预期结果：返回null
     */
    @Test
    public void testParse_EmptyString() {
        assertNull(BoolUtil.parse(""));
        assertNull(BoolUtil.parse("   "));
    }

    /**
     * 测试异常场景：输入为无法解析为布尔或数字的字符串
     * 预期结果：返回null
     */
    @Test
    public void testParse_InvalidString() {
        assertNull(BoolUtil.parse("invalid"));
        assertNull(BoolUtil.parse("abc123"));
        assertNull(BoolUtil.parse("truefalse"));
    }

    /**
     * 测试正常场景：输入为Boolean对象
     * 预期结果：返回对应的布尔值
     */
    @Test
    public void testParse_BooleanObject() {
        assertEquals(Boolean.TRUE, BoolUtil.parse(Boolean.TRUE));
        assertEquals(Boolean.FALSE, BoolUtil.parse(Boolean.FALSE));
    }

    /**
     * 测试正常场景：输入为Number对象
     * 预期结果：根据数值返回对应的布尔值
     */
    @Test
    public void testParse_NumberObject() {
        assertEquals(Boolean.TRUE, BoolUtil.parse(1));
        assertEquals(Boolean.TRUE, BoolUtil.parse(1.5));
        assertEquals(Boolean.FALSE, BoolUtil.parse(0));
        assertEquals(Boolean.FALSE, BoolUtil.parse(-1));
    }
}
