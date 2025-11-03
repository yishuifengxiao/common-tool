package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HexUtil_toHexString_01_Test {

    /**
     * 测试正常场景：将正整数转换为16进制字符串
     * 验证整数转换后的16进制字符串是否正确
     */
    @Test
    public void testToHexString_PositiveInteger() {
        Integer number = 255;
        Integer byteNum = 2;
        String result = HexUtil.toHexString(number, byteNum);
        assertEquals("00FF", result);
    }


    /**
     * 测试正常场景：将长整型转换为16进制字符串
     * 验证长整型转换后的16进制字符串是否正确
     */
    @Test
    public void testToHexString_Long() {
        Long number = 123456789L;
        Integer byteNum = 4;
        String result = HexUtil.toHexString(number, byteNum);
        assertEquals("075BCD15", result);
    }

    /**
     * 测试正常场景：将短整型转换为16进制字符串
     * 验证短整型转换后的16进制字符串是否正确
     */
    @Test
    public void testToHexString_Short() {
        Short number = 255;
        Integer byteNum = 1;
        String result = HexUtil.toHexString(number, byteNum);
        assertEquals("FF", result);
    }

    /**
     * 测试正常场景：将字节转换为16进制字符串
     * 验证字节转换后的16进制字符串是否正确
     */
    @Test
    public void testToHexString_Byte() {
        Byte number = 15;
        Integer byteNum = 1;
        String result = HexUtil.toHexString(number, byteNum);
        assertEquals("0F", result);
    }

    /**
     * 测试边界场景：输入为null
     * 验证当输入为null时返回空字符串
     */
    @Test
    public void testToHexString_NullInput() {
        String result = HexUtil.toHexString(null, 2);
        assertEquals("", result);
    }

    /**
     * 测试边界场景：byteNum为null
     * 验证当byteNum为null时只进行基本的16进制转换
     */
    @Test
    public void testToHexString_NullByteNum() {
        Integer number = 255;
        String result = HexUtil.toHexString(number, null);
        assertEquals("FF", result);
    }

    /**
     * 测试边界场景：byteNum为0
     * 验证当byteNum为0时只进行基本的16进制转换
     */
    @Test
    public void testToHexString_ZeroByteNum() {
        Integer number = 255;
        String result = HexUtil.toHexString(number, 0);
        assertEquals("FF", result);
    }

    /**
     * 测试边界场景：byteNum为负数
     * 验证当byteNum为负数时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToHexString_NegativeByteNum() {
        Integer number = 255;
        HexUtil.toHexString(number, -1);
    }

    /**
     * 测试异常场景：输入为浮点数
     * 验证当输入为浮点数时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToHexString_FloatInput() {
        Float number = 3.14f;
        HexUtil.toHexString(number, 2);
    }


    /**
     * 测试边界场景：转换后的长度大于指定字节数
     * 验证当转换后的长度大于指定字节数时不做截断处理
     */
    @Test
    public void testToHexString_ResultLongerThanByteNum() {
        Integer number = 65535;
        Integer byteNum = 1;
        String result = HexUtil.toHexString(number, byteNum);
        assertEquals("FFFF", result);
    }
}
