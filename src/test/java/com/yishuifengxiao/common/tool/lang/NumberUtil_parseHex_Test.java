package com.yishuifengxiao.common.tool.lang;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.junit.Assert.*;

@Slf4j
public class NumberUtil_parseHex_Test {

    /**
     * 测试正常场景：有效的16进制字符串
     * 预期结果：正确转换为对应的10进制数字
     */
    @Test
    public void testParseHex_ValidHexString() {
        String hexString = "1A";
        Optional<BigDecimal> result = NumberUtil.parseHex(hexString);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal(new BigInteger("1A", 16)), result.get());
    }

    /**
     * 测试正常场景：包含逗号的16进制字符串
     * 预期结果：正确去除逗号并转换为对应的10进制数字
     */
    @Test
    public void testParseHex_HexStringWithCommas() {
        String hexString = "1,A,2,B";
        Optional<BigDecimal> result = NumberUtil.parseHex(hexString);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal(new BigInteger("1A2B", 16)), result.get());
    }

    /**
     * 测试边界场景：空字符串
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParseHex_EmptyString() {
        String hexString = "";
        Optional<BigDecimal> result = NumberUtil.parseHex(hexString);
        assertFalse(result.isPresent());
    }

    /**
     * 测试边界场景：空白字符串
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParseHex_BlankString() {
        String hexString = "   ";
        Optional<BigDecimal> result = NumberUtil.parseHex(hexString);
        assertFalse(result.isPresent());
    }

    /**
     * 测试边界场景：null输入
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParseHex_NullInput() {
        Optional<BigDecimal> result = NumberUtil.parseHex(null);
        assertFalse(result.isPresent());
    }

    /**
     * 测试异常场景：无效的16进制字符串
     * 预期结果：返回空的Optional
     */
    @Test
    public void testParseHex_InvalidHexString() {
        String hexString = "XYZ";
        Optional<BigDecimal> result = NumberUtil.parseHex(hexString);
        assertFalse(result.isPresent());
    }

    /**
     * 测试边界场景：最大值的16进制字符串
     * 预期结果：正确转换为对应的10进制数字
     */
    @Test
    public void testParseHex_MaxValueHexString() {
        String hexString = "FFFFFFFFFFFFFFFF";
        Optional<BigDecimal> result = NumberUtil.parseHex(hexString);
        assertTrue(result.isPresent());
        assertEquals(new BigDecimal(new BigInteger("FFFFFFFFFFFFFFFF", 16)), result.get());
    }

    /**
     * 测试边界场景：最小值的16进制字符串
     * 预期结果：正确转换为对应的10进制数字
     */
    @Test
    public void testParseHex_MinValueHexString() {
        String hexString = "0";
        Optional<BigDecimal> result = NumberUtil.parseHex(hexString);
        assertTrue(result.isPresent());
        assertEquals(BigDecimal.ZERO, result.get());
    }
}
