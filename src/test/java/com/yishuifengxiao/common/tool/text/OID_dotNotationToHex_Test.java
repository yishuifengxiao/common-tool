package com.yishuifengxiao.common.tool.text;

import com.yishuifengxiao.common.tool.lang.OID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OID_dotNotationToHex_Test {

    /**
     * 测试正常场景：标准OID转换
     * 验证标准点分十进制OID能正确转换为十六进制字符串
     */
    @Test
    public void testDotNotationToHex_StandardOID() {
        String input = "1.2.840.113549";
        String expected = "2A864886F70D";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotationToHex_StandardOID1() {
        String input = "2.999.10";
        String expected = "88370A";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotationToHex_StandardOID2() {
        String input = "2.23.146.1.2.1.0";
        String expected = "67811201020100";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotationToHex_StandardOID3() {
        String input = "2.5.29.19";
        String expected = "551D13";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotationToHex_StandardOID4() {
        String input = "1.2.840.10045.3.1.7";
        String expected = "2A8648CE3D030107";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotationToHex_StandardOID5() {
        String input = "1.2.840.10045.2.1";
        String expected = "2A8648CE3D0201";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotationToHex_StandardOID6() {
        String input = "2.5.4.6";
        String expected = "550406";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    @Test
    public void testDotNotationToHex_StandardOID7() {
        String input = "1.2.840.10045.4.3.2";
        String expected = "2A8648CE3D040302";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：最小有效OID
     * 验证最小有效OID(0.0)能正确转换为十六进制字符串
     */
    @Test
    public void testDotNotationToHex_MinValidOID() {
        String input = "0.0";
        String expected = "00";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    /**
     * 测试边界场景：最大有效OID
     * 验证最大有效OID(2.39)能正确转换为十六进制字符串
     */
    @Test
    public void testDotNotationToHex_MaxValidOID() {
        String input = "2.39";
        String expected = "77";
        String result = OID.dotNotationToHex(input);
        assertEquals(expected, result);
    }

    /**
     * 测试异常场景：空输入
     * 验证当输入为空字符串时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_EmptyInput() {
        OID.dotNotationToHex("");
    }

    /**
     * 测试异常场景：null输入
     * 验证当输入为null时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_NullInput() {
        OID.dotNotationToHex(null);
    }

    /**
     * 测试异常场景：单组件OID
     * 验证当OID只有一个组件时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_SingleComponent() {
        OID.dotNotationToHex("1");
    }

    /**
     * 测试异常场景：空组件
     * 验证当OID包含空组件时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_EmptyComponent() {
        OID.dotNotationToHex("1..2");
    }

    /**
     * 测试异常场景：负数组件
     * 验证当OID包含负数组件时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_NegativeComponent() {
        OID.dotNotationToHex("1.-2");
    }

    /**
     * 测试异常场景：无效数字组件
     * 验证当OID包含非数字组件时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_InvalidNumberComponent() {
        OID.dotNotationToHex("1.abc");
    }




}
