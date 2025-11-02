package com.yishuifengxiao.common.tool.text;

import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * HexUtil.hexToBase64 方法的单元测试类
 */
public class HexUtilHexToBase64Test {

    /**
     * TC01: 输入为 null，应返回空字符串
     */
    @Test
    public void testHexToBase64_NullInput() {
        String result = HexUtil.hexToBase64(null);
        assertEquals("", result);
    }

    /**
     * TC02: 输入为空字符串，应返回空字符串
     */
    @Test
    public void testHexToBase64_EmptyString() {
        String result = HexUtil.hexToBase64("");
        assertEquals("", result);
    }

    /**
     * TC03: 输入长度为奇数，应抛出 IllegalArgumentException
     */
    @Test
    public void testHexToBase64_OddLength() {
        assertThrows(IllegalArgumentException.class, () -> HexUtil.hexToBase64("123"));
    }

    /**
     * TC04: 包含非法字符，应抛出 IllegalArgumentException
     */
    @Test
    public void testHexToBase64_InvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> HexUtil.hexToBase64("12GZ"));
    }

    /**
     * TC05: 正常十六进制字符串 "48656c6c6f" -> "Hello"
     * Base64("Hello") = "SGVsbG8="
     */
    @Test
    public void testHexToBase64_NormalHex() {
        String result = HexUtil.hexToBase64("48656c6c6f");
        assertEquals(Base64.getEncoder().encodeToString("Hello".getBytes()), result);
    }

    /**
     * TC06: 包含空格和前缀 "0x" 的十六进制字符串
     * "0x48 65 6c 6c 6f" -> "SGVsbG8="
     */
    @Test
    public void testHexToBase64_WithSpacesAndPrefix() {
        String result = HexUtil.hexToBase64("0x48 65 6c 6c 6f");
        assertEquals(Base64.getEncoder().encodeToString("Hello".getBytes()), result);
    }

    /**
     * TC07: 全为空白字符的输入，应返回空字符串
     */
    @Test
    public void testHexToBase64_BlankInput() {
        String result = HexUtil.hexToBase64("   ");
        assertEquals("", result);
    }
}
