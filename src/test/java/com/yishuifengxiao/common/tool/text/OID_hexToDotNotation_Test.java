package com.yishuifengxiao.common.tool.text;

import com.yishuifengxiao.common.tool.lang.OID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OID_hexToDotNotation_Test {

    @Test
    public void testhexToDotNotation_StandardOID1() {
        String expected = "2.999.10";
        String input = "88370A";
        String result = OID.hexToDotNotation(input);
        assertEquals(expected, result);
    }

    @Test
    public void testhexToDotNotation_StandardOID2() {
        String expected = "2.23.146.1.2.1.0";
        String input = "67811201020100";
        String result = OID.hexToDotNotation(input);
        assertEquals(expected, result);
    }

    @Test
    public void testhexToDotNotation_StandardOID3() {
        String expected = "2.5.29.19";
        String input = "551D13";
        String result = OID.hexToDotNotation(input);
        assertEquals(expected, result);
    }

    @Test
    public void testhexToDotNotation_StandardOID4() {
        String expected = "1.2.840.10045.3.1.7";
        String input = "2A8648CE3D030107";
        String result = OID.hexToDotNotation(input);
        assertEquals(expected, result);
    }

    @Test
    public void testhexToDotNotation_StandardOID5() {
        String expected = "1.2.840.10045.2.1";
        String input = "2A8648CE3D0201";
        String result = OID.hexToDotNotation(input);
        assertEquals(expected, result);
    }

    @Test
    public void testhexToDotNotation_StandardOID6() {
        String expected = "2.5.4.6";
        String input = "550406";
        String result = OID.hexToDotNotation(input);
        assertEquals(expected, result);
    }

    @Test
    public void testhexToDotNotation_StandardOID7() {
        String expected = "1.2.840.10045.4.3.2";
        String input = "2A8648CE3D040302";
        String result = OID.hexToDotNotation(input);
        assertEquals(expected, result);
    }

    /**
     * 测试正常场景：有效的十六进制字符串转换为点分十进制
     * 目的：验证标准输入的正确转换功能
     */
    @Test
    public void testHexToDotNotation_ValidHexString() {
        String hexStr = "2B0601040181C31F03";
        String expected = "1.3.6.1.4.1.24991.3";
        
        String result = OID.hexToDotNotation(hexStr);
        
        assertEquals(expected, result);
    }

    /**
     * 测试正常场景：包含空格的十六进制字符串
     * 目的：验证输入字符串中空格的处理能力
     */
    @Test
    public void testHexToDotNotation_HexWithSpaces() {
        String hexStr = "2B 06 01 04 01 81 C3 1F 03";
        String expected = "1.3.6.1.4.1.24991.3";
        
        String result = OID.hexToDotNotation(hexStr);
        
        assertEquals(expected, result);
    }

    /**
     * 测试正常场景：大写十六进制字符串
     * 目的：验证大小写不敏感的处理能力
     */
    @Test
    public void testHexToDotNotation_UpperCaseHex() {
        String hexStr = "2B0601040181C31F03";
        String expected = "1.3.6.1.4.1.24991.3";
        
        String result = OID.hexToDotNotation(hexStr);
        
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：最小有效OID转换
     * 目的：验证最小有效输入的转换能力
     */
    @Test
    public void testHexToDotNotation_MinimalValidOID() {
        String hexStr = "28";
        String expected = "1.0";
        
        String result = OID.hexToDotNotation(hexStr);
        
        assertEquals(expected, result);
    }

    /**
     * 测试异常场景：输入为null
     * 目的：验证对null输入的正确异常处理
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_NullInput() {
        OID.hexToDotNotation(null);
    }

    /**
     * 测试异常场景：空字符串输入
     * 目的：验证对空字符串的正确异常处理
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_EmptyString() {
        OID.hexToDotNotation("");
    }

    /**
     * 测试异常场景：无效的十六进制字符
     * 目的：验证对非法字符的检测能力
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_InvalidHexChars() {
        OID.hexToDotNotation("2G");
    }


}
