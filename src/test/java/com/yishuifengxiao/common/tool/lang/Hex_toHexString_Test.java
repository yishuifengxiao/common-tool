package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

/**
 * Hex工具类的单元测试
 */
public class Hex_toHexString_Test {

    /**
     * 测试toHexString方法 - 基本ASCII字符
     */
    @Test
    public void testToHexStringWithAscii() {
        // 测试空字符串
        assertEquals("", Hex.toHexString(""));

        // 测试单个字符
        assertEquals("61", Hex.toHexString("a"));
        assertEquals("41", Hex.toHexString("A"));

        // 测试多个字符
        assertEquals("68656c6c6f", Hex.toHexString("hello"));
        assertEquals("48656c6c6f20576f726c64", Hex.toHexString("Hello World"));
    }

    /**
     * 测试toHexString方法 - 中文字符
     */
    @Test
    public void testToHexStringWithChinese() {
        // 测试中文字符
        String chinese = "你好";
        String expected = bytesToHex(chinese.getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, Hex.toHexString(chinese));

        // 测试中英文混合
        String mixed = "Hello你好World";
        String expectedMixed = bytesToHex(mixed.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedMixed, Hex.toHexString(mixed));
    }

    /**
     * 测试toHexString方法 - 特殊字符
     */
    @Test
    public void testToHexStringWithSpecialChars() {
        // 测试特殊字符
        String special = "!@#$%^&*()";
        String expected = bytesToHex(special.getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, Hex.toHexString(special));

        // 测试数字
        String numbers = "123456789";
        String expectedNumbers = bytesToHex(numbers.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedNumbers, Hex.toHexString(numbers));
    }

    /**
     * 辅助方法：将字节数组转换为十六进制字符串，用于验证结果
     */
    private String bytesToHex(byte[] bytes) {
        final String hexString = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            sb.append(hexString.charAt((aByte & 0xf0) >> 4));
            sb.append(hexString.charAt((aByte & 0x0f) >> 0));
        }
        return sb.toString();
    }
}
