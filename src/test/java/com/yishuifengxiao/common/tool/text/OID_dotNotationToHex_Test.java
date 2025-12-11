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

    /**
     * 测试异常场景：无效第一个组件
     * 验证当第一个组件大于2时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_InvalidFirstComponent() {
        OID.dotNotationToHex("3.1");
    }

    /**
     * 测试异常场景：无效第二个组件
     * 验证当第二个组件大于等于40时抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDotNotationToHex_InvalidSecondComponent() {
        OID.dotNotationToHex("1.40");
    }
}
