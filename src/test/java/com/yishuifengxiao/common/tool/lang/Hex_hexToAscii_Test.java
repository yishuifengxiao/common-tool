package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Hex工具类中hexToAscii方法的单元测试
 */
public class Hex_hexToAscii_Test {

    /**
     * 测试hexToAscii方法 - 空字符串
     */
    @Test
    public void testHexToAsciiWithEmptyString() {
        assertEquals("", Hex.hexToAscii(""));
    }

    /**
     * 测试hexToAscii方法 - 纯空白字符串（清理后为空）
     */
    @Test
    public void testHexToAsciiWithBlankString() {
        assertEquals("", Hex.hexToAscii("   "));
        assertEquals("", Hex.hexToAscii("\t\n"));
    }

    /**
     * 测试hexToAscii方法 - 单个字符
     */
    @Test
    public void testHexToAsciiWithSingleChar() {
        assertEquals("H", Hex.hexToAscii("48"));
        assertEquals("A", Hex.hexToAscii("41"));
        assertEquals("a", Hex.hexToAscii("61"));
        assertEquals("0", Hex.hexToAscii("30"));
    }

    /**
     * 测试hexToAscii方法 - 基本ASCII字符串
     */
    @Test
    public void testHexToAsciiWithAsciiString() {
        assertEquals("Hello", Hex.hexToAscii("48656C6C6F"));
        assertEquals("Hello World", Hex.hexToAscii("48656C6C6F20576F726C64"));
        assertEquals("Hello123", Hex.hexToAscii("48656C6C6F313233"));
    }

    /**
     * 测试hexToAscii方法 - 小写十六进制输入（不区分大小写）
     */
    @Test
    public void testHexToAsciiWithLowerCase() {
        assertEquals("Hello", Hex.hexToAscii("48656c6c6f"));
    }

    /**
     * 测试hexToAscii方法 - 含空白字符的十六进制输入（解析前会被清除）
     */
    @Test
    public void testHexToAsciiWithWhitespace() {
        assertEquals("Hello", Hex.hexToAscii("48 65 6C 6C 6F"));
        assertEquals("Hello", Hex.hexToAscii("48\t65\n6C 6C 6F"));
    }

    /**
     * 测试hexToAscii方法 - null输入
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToAsciiWithNull() {
        Hex.hexToAscii(null);
    }

    /**
     * 测试hexToAscii方法 - 奇数长度输入
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToAsciiWithOddLength() {
        Hex.hexToAscii("ABC");
    }

    /**
     * 测试hexToAscii方法 - 非法十六进制字符
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToAsciiWithInvalidChar() {
        Hex.hexToAscii("4G");
    }

    /**
     * 测试hexToAscii方法 - 控制字符
     */
    @Test
    public void testHexToAsciiWithControlChar() {
        assertEquals("\0", Hex.hexToAscii("00"));
        assertEquals("\n", Hex.hexToAscii("0A"));
    }

    /**
     * 测试hexToAscii方法 - 高位字节（超出0~127，解码为替换字符U+FFFD）
     */
    @Test
    public void testHexToAsciiWithHighByte() {
        assertEquals("\uFFFD", Hex.hexToAscii("E4"));
        assertEquals("\uFFFD\uFFFD\uFFFD", Hex.hexToAscii("E4BDA0"));
    }
}
