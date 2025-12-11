package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Hex工具类中hexToBytes方法的单元测试
 */
public class Hex_hexToBytes_Test {

    /**
     * 测试hexToBytes方法 - 空字符串
     */
    @Test
    public void testHexToBytesWithEmptyString() {
        byte[] result = Hex.hexToBytes("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    /**
     * 测试hexToBytes方法 - 包含空格的字符串（会被移除）
     */
    @Test
    public void testHexToBytesWithSpaces() {
        // 测试包含空格的十六进制字符串
        byte[] result = Hex.hexToBytes("48 65 6C 6C 6F");
        assertArrayEquals(new byte[]{72, 101, 108, 108, 111}, result);
        
        // 测试只有空格的情况
        byte[] emptyResult = Hex.hexToBytes("   ");
        assertNotNull(emptyResult);
        assertEquals(0, emptyResult.length);
    }

    /**
     * 测试hexToBytes方法 - 正常的十六进制字符串
     */
    @Test
    public void testHexToBytesWithValidHex() {
        // 测试单个字节
        byte[] singleByte = Hex.hexToBytes("FF");
        assertArrayEquals(new byte[]{-1}, singleByte);
        
        // 测试多个字节
        byte[] multipleBytes = Hex.hexToBytes("000F10FF7FFF80");
        assertArrayEquals(new byte[]{0, 15, 16, -1, 127, -1, -128}, multipleBytes);
        
        // 测试小写十六进制字符
        byte[] lowerCase = Hex.hexToBytes("48656c6c6f");
        assertArrayEquals(new byte[]{72, 101, 108, 108, 111}, lowerCase);
        
        // 测试大小写混合
        byte[] mixedCase = Hex.hexToBytes("48656C6c6F");
        assertArrayEquals(new byte[]{72, 101, 108, 108, 111}, mixedCase);
    }

    /**
     * 测试hexToBytes方法 - 奇数长度的十六进制字符串（应抛出异常）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToBytesWithOddLength() {
        Hex.hexToBytes("123"); // 奇数长度，应该抛出异常
    }

    /**
     * 测试hexToBytes方法 - 包含非法字符的字符串（应抛出异常）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToBytesWithInvalidCharacters() {
        Hex.hexToBytes("GG"); // 包含非十六进制字符，应该抛出异常
    }

    /**
     * 测试hexToBytes方法 - 与bytesToHex方法的互操作性
     */
    @Test
    public void testHexToBytesAndBytesToHexInteroperability() {
        // 测试ASCII字符串转换
        String originalStr = "Hello";
        byte[] originalBytes = originalStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String hexStr = Hex.bytesToHex(originalBytes);
        byte[] convertedBytes = Hex.hexToBytes(hexStr);
        assertArrayEquals(originalBytes, convertedBytes);
        
        // 测试包含中文的字符串转换
        String chineseStr = "你好世界";
        byte[] chineseBytes = chineseStr.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String chineseHexStr = Hex.bytesToHex(chineseBytes);
        byte[] convertedChineseBytes = Hex.hexToBytes(chineseHexStr);
        assertArrayEquals(chineseBytes, convertedChineseBytes);
        
        // 测试特殊值
        byte[] specialBytes = new byte[]{-1, 0, 127, -128};
        String specialHexStr = Hex.bytesToHex(specialBytes);
        byte[] convertedSpecialBytes = Hex.hexToBytes(specialHexStr);
        assertArrayEquals(specialBytes, convertedSpecialBytes);
    }

    /**
     * 测试hexToBytes方法 - 边界值
     */
    @Test
    public void testHexToBytesWithBoundaryValues() {
        // 测试最小字节值
        byte[] minBytes = Hex.hexToBytes("80");
        assertArrayEquals(new byte[]{-128}, minBytes);
        
        // 测试最大字节值
        byte[] maxBytes = Hex.hexToBytes("FF");
        assertArrayEquals(new byte[]{-1}, maxBytes);
        
        // 测试零值
        byte[] zeroBytes = Hex.hexToBytes("00");
        assertArrayEquals(new byte[]{0}, zeroBytes);
        
        // 测试中间值
        byte[] midBytes = Hex.hexToBytes("7F");
        assertArrayEquals(new byte[]{127}, midBytes);
    }
}
