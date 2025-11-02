package com.yishuifengxiao.common.tool.text;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * HexUtil.toHex 方法的单元测试类
 */
public class HexUtilToHexTest {

    /**
     * TC01: 输入为 null，应返回 null
     */
    @Test
    public void testToHex_NullInput() {
        String result = HexUtil.toHex(null);
        assertNull(result);
    }

    /**
     * TC02: 输入为空字符串，应返回 "00"
     */
    @Test
    public void testToHex_EmptyString() {
        String result = HexUtil.toHex(" ");
        assertEquals("20", result);
    }

    /**
     * TC03: 输入为合法十六进制字符串且长度为偶数，应直接返回
     */
    @Test
    public void testToHex_ValidHexEvenLength() {
        String input = "1A2B";
        String result = HexUtil.toHex(input);
        assertEquals("1A2B", result);
    }

    /**
     * TC04: 输入为合法十六进制字符串但长度为奇数，应补前导 '0'
     */
    @Test
    public void testToHex_ValidHexOddLength() {
        String input = "1A2";
        String result = HexUtil.toHex(input);
        assertEquals("01A2", result);
    }

    /**
     * TC05: 输入为普通字符串，应转换为对应的十六进制字符串
     */
    @Test
    public void testToHex_NormalString() {
        String input = "Hello";
        // UTF-8 编码下：H=48, e=65, l=6C, l=6C, o=6F
        String expected = "48656C6C6F";
        String result = HexUtil.toHex(input);
        assertEquals(expected, result);
    }
}
