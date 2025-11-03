package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HexUtilTest {

    /**
     * 测试正常场景：转换正整数为16进制字符串
     * 预期结果：返回正确的16进制字符串表示
     */
    @Test
    public void testToHexString_PositiveInteger() {
        Number number = 255;
        String result = HexUtil.toHexString(number);
        assertEquals("FF", result);
    }

    /**
     * 测试正常场景：转换负整数为16进制字符串
     * 预期结果：返回正确的16进制字符串表示
     */
    @Test
    public void testToHexString_NegativeInteger() {
        Number number = -255;
        String result = HexUtil.toHexString(number);
        assertEquals("FFFFFF01", result);
    }

    /**
     * 测试正常场景：转换零为16进制字符串
     * 预期结果：返回"00"
     */
    @Test
    public void testToHexString_Zero() {
        Number number = 0;
        String result = HexUtil.toHexString(number);
        assertEquals("00", result);
    }

    /**
     * 测试正常场景：转换大整数为16进制字符串
     * 预期结果：返回正确的16进制字符串表示
     */
    @Test
    public void testToHexString_LargeNumber() {
        Number number = 65535;
        String result = HexUtil.toHexString(number);
        assertEquals("FFFF", result);
    }

    /**
     * 测试边界场景：转换奇数长度16进制字符串
     * 预期结果：自动左补零使字符串长度为偶数
     */
    @Test
    public void testToHexString_OddLength() {
        Number number = 15;
        String result = HexUtil.toHexString(number);
        assertEquals("0F", result);
    }


}
