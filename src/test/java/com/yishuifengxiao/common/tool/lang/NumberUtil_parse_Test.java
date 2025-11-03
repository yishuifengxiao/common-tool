package com.yishuifengxiao.common.tool.lang;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;

@Slf4j
public class NumberUtil_parse_Test {

    /**
     * 测试正常场景：输入为null值
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_NullInput() {
        Optional<BigDecimal> result = NumberUtil.parse(null);
        assertFalse(result.isPresent());
    }

    /**
     * 测试正常场景：输入为空字符串
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_EmptyString() {
        Optional<BigDecimal> result = NumberUtil.parse("");
        assertFalse(result.isPresent());
    }

    /**
     * 测试正常场景：输入为空白字符串
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_BlankString() {
        Optional<BigDecimal> result = NumberUtil.parse("   ");
        assertFalse(result.isPresent());
    }

    /**
     * 测试正常场景：输入为BigDecimal类型
     * 预期结果：返回包含相同值的Optional
     */
    @Test
    public void testParse_BigDecimalInput() {
        BigDecimal input = new BigDecimal("123.456");
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertTrue(result.isPresent());
        assertEquals(input, result.get());
    }

    /**
     * 测试正常场景：输入为Integer类型
     * 预期结果：返回包含正确BigDecimal值的Optional
     */
    @Test
    public void testParse_IntegerInput() {
        Integer input = 123;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("123"), result.get());
    }

    /**
     * 测试正常场景：输入为Long类型
     * 预期结果：返回包含正确BigDecimal值的Optional
     */
    @Test
    public void testParse_LongInput() {
        Long input = 123456789L;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("123456789"), result.get());
    }

    /**
     * 测试正常场景：输入为有效的Double类型
     * 预期结果：返回包含正确BigDecimal值的Optional
     */
    @Test
    public void testParse_ValidDoubleInput() {
        Double input = 123.456;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(123.456), result.get());
    }

    /**
     * 测试边界场景：输入为NaN的Double类型
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_NaN_DoubleInput() {
        Double input = Double.NaN;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertFalse(result.isPresent());
    }

    /**
     * 测试边界场景：输入为无限大的Double类型
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_InfiniteDoubleInput() {
        Double input = Double.POSITIVE_INFINITY;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertFalse(result.isPresent());
    }

    /**
     * 测试正常场景：输入为有效的Float类型
     * 预期结果：返回包含正确BigDecimal值的Optional
     */
    @Test
    public void testParse_ValidFloatInput() {
        Float input = 123.456f;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.valueOf(123.456f), result.get());
    }

    /**
     * 测试边界场景：输入为NaN的Float类型
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_NaN_FloatInput() {
        Float input = Float.NaN;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertFalse(result.isPresent());
    }

    /**
     * 测试边界场景：输入为无限大的Float类型
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_InfiniteFloatInput() {
        Float input = Float.POSITIVE_INFINITY;
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertFalse(result.isPresent());
    }

    /**
     * 测试正常场景：输入为有效的数字字符串
     * 预期结果：返回包含正确BigDecimal值的Optional
     */
    @Test
    public void testParse_ValidNumberString() {
        String input = "123.456";
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("123.456"), result.get());
    }

    /**
     * 测试正常场景：输入为包含逗号的数字字符串
     * 预期结果：返回包含正确BigDecimal值的Optional（逗号被移除）
     */
    @Test
    public void testParse_NumberStringWithCommas() {
        String input = "1,234,567.89";
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("1234567.89"), result.get());
    }

    /**
     * 测试边界场景：输入为无效的数字字符串
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_InvalidNumberString() {
        String input = "abc123";
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertFalse(result.isPresent());
    }

    /**
     * 测试边界场景：输入为其他类型的对象
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParse_OtherTypeInput() {
        Object input = new Object();
        Optional<BigDecimal> result = NumberUtil.parse(input);
        assertFalse(result.isPresent());
    }
}
