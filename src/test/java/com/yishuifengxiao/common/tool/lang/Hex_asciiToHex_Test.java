package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Hex工具类中asciiToHex方法的单元测试
 */
public class Hex_asciiToHex_Test {

    /**
     * 测试asciiToHex方法 - 空字符串
     */
    @Test
    public void testAsciiToHexWithEmptyString() {
        assertEquals("", Hex.asciiToHex(""));
    }

    /**
     * 测试asciiToHex方法 - 纯空白字符串（清理后为空）
     */
    @Test
    public void testAsciiToHexWithBlankString() {
        assertEquals("", Hex.asciiToHex("   "));
        assertEquals("", Hex.asciiToHex("\t\n"));
    }

    /**
     * 测试asciiToHex方法 - 单个字符
     */
    @Test
    public void testAsciiToHexWithSingleChar() {
        assertEquals("48", Hex.asciiToHex("H"));
        assertEquals("41", Hex.asciiToHex("A"));
        assertEquals("61", Hex.asciiToHex("a"));
        assertEquals("30", Hex.asciiToHex("0"));
    }

    /**
     * 测试asciiToHex方法 - 基本ASCII字符串
     */
    @Test
    public void testAsciiToHexWithAsciiString() {
        assertEquals("48656C6C6F", Hex.asciiToHex("Hello"));
        assertEquals("48656C6C6F313233", Hex.asciiToHex("Hello123"));
    }

    /**
     * 测试asciiToHex方法 - 输出为大写十六进制
     */
    @Test
    public void testAsciiToHexOutputUpperCase() {
        assertEquals("616263", Hex.asciiToHex("abc"));
        assertEquals("0F", Hex.asciiToHex("\u000F"));
    }

    /**
     * 测试asciiToHex方法 - 含空白字符的输入（解析前会被清除）
     */
    @Test
    public void testAsciiToHexWithWhitespace() {
        assertEquals("48656C6C6F", Hex.asciiToHex("  Hello  "));
        assertEquals("48656C6C6F576F726C64", Hex.asciiToHex("Hello\tWorld"));
    }

    /**
     * 测试asciiToHex方法 - null输入
     */
    @Test(expected = IllegalArgumentException.class)
    public void testAsciiToHexWithNull() {
        Hex.asciiToHex(null);
    }

    /**
     * 测试asciiToHex方法 - 非ASCII字符（只取低8位，可能失真）
     */
    @Test
    public void testAsciiToHexWithNonAsciiChar() {
        // '你' = U+4F60，低8位为0x60
        assertEquals("60", Hex.asciiToHex("你"));
        // '你'=U+4F60（低8位60），'好'=U+597D（低8位7D）
        assertEquals("607D", Hex.asciiToHex("你好"));
    }

    /**
     * 测试asciiToHex与hexToAscii的往返转换
     */
    @Test
    public void testAsciiToHexWithRoundTrip() {
        String original = "HelloWorld123";
        assertEquals(original, Hex.hexToAscii(Hex.asciiToHex(original)));
    }
}
