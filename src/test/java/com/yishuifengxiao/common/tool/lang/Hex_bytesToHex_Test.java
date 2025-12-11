package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Hex工具类中bytesToHex方法的单元测试
 */
public class Hex_bytesToHex_Test {

    /**
     * 测试bytesToHex方法 - 空数组
     */
    @Test
    public void testBytesToHexWithEmptyArray() {
        byte[] emptyBytes = new byte[0];
        assertEquals("", Hex.bytesToHex(emptyBytes));
    }

    /**
     * 测试bytesToHex方法 - 单个字节
     */
    @Test
    public void testBytesToHexWithSingleByte() {
        // 测试值为0的字节
        byte[] zeroByte = new byte[]{0};
        assertEquals("00", Hex.bytesToHex(zeroByte));

        // 测试值小于16的字节
        byte[] smallByte = new byte[]{15};
        assertEquals("0F", Hex.bytesToHex(smallByte));

        // 测试值大于等于16的字节
        byte[] largeByte = new byte[]{(byte) 255};
        assertEquals("FF", Hex.bytesToHex(largeByte));
    }

    /**
     * 测试bytesToHex方法 - 多个字节
     */
    @Test
    public void testBytesToHexWithMultipleBytes() {
        // 测试包含各种值的字节数组
        byte[] bytes = new byte[]{0, 15, 16, (byte) 255, 127, -1, -128};
        assertEquals("000F10FF7FFF80", Hex.bytesToHex(bytes));
    }

    /**
     * 测试bytesToHex方法 - 字符串转字节再转十六进制
     */
    @Test
    public void testBytesToHexWithStringConversion() {
        // 测试ASCII字符串
        String asciiStr = "Hello";
        byte[] asciiBytes = asciiStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("48656C6C6F", Hex.bytesToHex(asciiBytes));

        // 测试包含中文的字符串
        String chineseStr = "你好";
        byte[] chineseBytes = chineseStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("E4BDA0E5A5BD", Hex.bytesToHex(chineseBytes));

        // 测试混合字符串
        String mixedStr = "Hello你好123";
        byte[] mixedBytes = mixedStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("48656C6C6FE4BDA0E5A5BD313233", Hex.bytesToHex(mixedBytes));
    }

    /**
     * 测试bytesToHex方法 - 特殊值
     */
    @Test
    public void testBytesToHexWithSpecialValues() {
        // 测试最大字节值
        byte[] maxBytes = new byte[]{-1}; // -1 在byte中表示为 FF
        assertEquals("FF", Hex.bytesToHex(maxBytes));

        // 测试最小字节值
        byte[] minBytes = new byte[]{-128}; // -128 在byte中表示为 80
        assertEquals("80", Hex.bytesToHex(minBytes));

        // 测试中间值
        byte[] midBytes = new byte[]{127}; // 127 在byte中表示为 7F
        assertEquals("7F", Hex.bytesToHex(midBytes));
    }

    @Test
    public void test_bytesToHex_hexToBytes() {
        String hexString = "1234";
        byte[] bytes = Hex.hexToBytes(hexString);
        String toHex = Hex.bytesToHex(bytes);
        assertEquals(hexString, toHex);
    }
}
