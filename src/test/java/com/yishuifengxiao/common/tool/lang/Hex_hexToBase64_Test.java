package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Hex工具类中hexToBase64方法的单元测试
 */
public class Hex_hexToBase64_Test {

    /**
     * 测试hexToBase64方法 - 空或null输入
     */
    @Test
    public void testHexToBase64WithNullAndEmptyInput() {
        // 测试null输入
        assertEquals("", Hex.hexToBase64(null));

        // 测试空字符串输入
        assertEquals("", Hex.hexToBase64(""));
    }

    /**
     * 测试hexToBase64方法 - 包含空格和前缀的十六进制字符串
     */
    @Test
    public void testHexToBase64WithSpacesAndPrefix() {
        // 测试包含空格的十六进制字符串
        assertEquals("SGVsbG8=", Hex.hexToBase64("48 65 6c 6c 6f"));

        // 测试包含0x前缀的十六进制字符串
        assertEquals("SGVsbG8=", Hex.hexToBase64("0x48656c6c6f"));

        // 测试同时包含空格和0x前缀的十六进制字符串
        assertEquals("SGVsbG8=", Hex.hexToBase64("0x48 65 6c 6c 6f"));

    }

    /**
     * 测试hexToBase64方法 - 正常的十六进制字符串
     */
    @Test
    public void testHexToBase64WithValidHex() {
        // 测试"Hello"字符串的十六进制表示
        assertEquals("SGVsbG8=", Hex.hexToBase64("48656c6c6f"));

        // 测试"World"字符串的十六进制表示
        assertEquals("V29ybGQ=", Hex.hexToBase64("576f726c64"));

        // 测试包含中文的十六进制字符串 "你好"
        assertEquals("5L2g5aW9", Hex.hexToBase64("e4bda0e5a5bd"));

        // 测试包含数字和特殊字符的十六进制字符串
        assertEquals("MTIzNDU2Nzg5IQ==", Hex.hexToBase64("31323334353637383921"));
    }

    /**
     * 测试hexToBase64方法 - 大小写十六进制字符
     */
    @Test
    public void testHexToBase64WithMixedCase() {
        // 测试小写十六进制字符
        assertEquals("AQID", Hex.hexToBase64("010203"));

        // 测试大写十六进制字符
        assertEquals("AQID", Hex.hexToBase64("010203"));

        // 测试大小写混合的十六进制字符
        assertEquals("AQID", Hex.hexToBase64("010203"));
    }

    /**
     * 测试hexToBase64方法 - 奇数长度的十六进制字符串（应抛出异常）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToBase64WithOddLength() {
        Hex.hexToBase64("123"); // 奇数长度，应该抛出异常
    }

    /**
     * 测试hexToBase64方法 - 包含非法字符的十六进制字符串（应抛出异常）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToBase64WithInvalidCharacters() {
        Hex.hexToBase64("GG"); // 包含非十六进制字符，应该抛出异常
    }

    /**
     * 测试hexToBase64方法 - 与Base64编码的互操作性
     */
    @Test
    public void testHexToBase64Interoperability() {
        // 测试ASCII字符串转换
        String originalStr = "Hello World!";
        byte[] originalBytes = originalStr.getBytes(StandardCharsets.UTF_8);
        String hexStr = Hex.bytesToHex(originalBytes);
        String base64FromHex = Hex.hexToBase64(hexStr);
        String expectedBase64 = Base64.getEncoder().encodeToString(originalBytes);
        assertEquals(expectedBase64, base64FromHex);

        // 测试包含中文的字符串转换
        String chineseStr = "你好世界";
        byte[] chineseBytes = chineseStr.getBytes(StandardCharsets.UTF_8);
        String chineseHexStr = Hex.bytesToHex(chineseBytes);
        String chineseBase64FromHex = Hex.hexToBase64(chineseHexStr);
        String expectedChineseBase64 = Base64.getEncoder().encodeToString(chineseBytes);
        assertEquals(expectedChineseBase64, chineseBase64FromHex);

        // 测试特殊值
        byte[] specialBytes = new byte[]{-1, 0, 127, -128};
        String specialHexStr = Hex.bytesToHex(specialBytes);
        String specialBase64FromHex = Hex.hexToBase64(specialHexStr);
        String expectedSpecialBase64 = Base64.getEncoder().encodeToString(specialBytes);
        assertEquals(expectedSpecialBase64, specialBase64FromHex);
    }

    /**
     * 测试hexToBase64方法 - 边界值
     */
    @Test
    public void testHexToBase64WithBoundaryValues() {
        // 测试最小字节值
        assertEquals("/w==", Hex.hexToBase64("ff"));

        // 测试最大字节值（与最小相同，因为都是-1）
        assertEquals("/w==", Hex.hexToBase64("FF"));

        // 测试零值
        assertEquals("AA==", Hex.hexToBase64("00"));

        // 测试中间值
        assertEquals("fw==", Hex.hexToBase64("7f"));
    }

    /**
     * 测试hexToBase64方法 - 长字符串
     */
    @Test
    public void testHexToBase64WithLongString() {
        // 测试较长的字符串
        String longStr = "This is a long string for testing hex to base64 conversion";
        byte[] longBytes = longStr.getBytes(StandardCharsets.UTF_8);
        String longHexStr = Hex.bytesToHex(longBytes);
        String longBase64FromHex = Hex.hexToBase64(longHexStr);
        String expectedLongBase64 = Base64.getEncoder().encodeToString(longBytes);
        assertEquals(expectedLongBase64, longBase64FromHex);
    }

    /**
     * 测试hexToBase64方法 - 单字节
     */
    @Test
    public void testHexToBase64WithSingleByte() {
        // 测试单个字节的不同值
        assertEquals("AA==", Hex.hexToBase64("00"));
        assertEquals("AQ==", Hex.hexToBase64("01"));
        assertEquals("Ag==", Hex.hexToBase64("02"));
        assertEquals("fw==", Hex.hexToBase64("7f"));
        assertEquals("gA==", Hex.hexToBase64("80"));
        assertEquals("/w==", Hex.hexToBase64("ff"));
    }

    /**
     * 测试hexToBase64方法 - 双字节
     */
    @Test
    public void testHexToBase64WithTwoBytes() {
        // 测试双字节的不同组合
        assertEquals("AAA=", Hex.hexToBase64("0000"));
        assertEquals("AAE=", Hex.hexToBase64("0001"));
        assertEquals("AQAB", Hex.hexToBase64("010001"));
        assertEquals("//8=", Hex.hexToBase64("ffff"));
    }

    @Test
    public void test_hexToBase64_base64ToHex() {
        String hexString = "1234";
        String base64 = Hex.hexToBase64(hexString);
        String toHex = Hex.base64ToHex(base64);
        assertEquals(hexString, toHex);
    }
}
