package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Hex工具类的单元测试
 */
public class Hex_hexStringToUtf8Text_Test {

    /**
     * 测试toHexString方法 - 基本ASCII字符
     */
    @Test
    public void testToHexStringWithAscii() {
        // 测试空字符串
        assertEquals("", Hex.utf8TextToHexString(""));

        // 测试单个字符
        assertEquals("61", Hex.utf8TextToHexString("a"));
        assertEquals("41", Hex.utf8TextToHexString("A"));

        // 测试多个字符
        assertEquals("68656c6c6f", Hex.utf8TextToHexString("hello"));
        assertEquals("48656c6c6f20576f726c64", Hex.utf8TextToHexString("Hello World"));
    }

    /**
     * 测试toHexString方法 - 中文字符
     */
    @Test
    public void testToHexStringWithChinese() {
        // 测试中文字符
        String chinese = "你好";
        String expected = bytesToHex(chinese.getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, Hex.utf8TextToHexString(chinese));

        // 测试中英文混合
        String mixed = "Hello你好World";
        String expectedMixed = bytesToHex(mixed.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedMixed, Hex.utf8TextToHexString(mixed));
    }

    /**
     * 测试toHexString方法 - 特殊字符
     */
    @Test
    public void testToHexStringWithSpecialChars() {
        // 测试特殊字符
        String special = "!@#$%^&*()";
        String expected = bytesToHex(special.getBytes(StandardCharsets.UTF_8));
        assertEquals(expected, Hex.utf8TextToHexString(special));

        // 测试数字
        String numbers = "123456789";
        String expectedNumbers = bytesToHex(numbers.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedNumbers, Hex.utf8TextToHexString(numbers));
    }

    /**
     * 使用反射测试私有方法hexStringToUtf8Text
     */
    @Test
    public void testHexStringToUtf8Text() throws Exception {
        // 获取私有方法
        Method method = Hex.class.getDeclaredMethod("hexStringToUtf8Text", String.class);
        method.setAccessible(true);

        // 测试null输入
        assertNull(method.invoke(null, (String) null));

        // 测试空字符串
        assertEquals("", method.invoke(null, ""));

        // 测试基本ASCII字符
        assertEquals("a", method.invoke(null, "61"));
        assertEquals("A", method.invoke(null, "41"));
        assertEquals("hello", method.invoke(null, "68656c6c6f"));
        assertEquals("Hello World", method.invoke(null, "48656c6c6f20576f726c64"));

        // 测试中文字符 (通过先编码再解码的方式)
        String chinese = "你好";
        String hexChinese = Hex.utf8TextToHexString(chinese);
        assertEquals(chinese, method.invoke(null, hexChinese));

        // 测试中英文混合
        String mixed = "Hello你好World";
        String hexMixed = Hex.utf8TextToHexString(mixed);
        assertEquals(mixed, method.invoke(null, hexMixed));
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

    @Test
    public void test_toHexString_hexStringToUtf8Text() {
        String val = "123";
        String hex = Hex.utf8TextToHexString(val);
        String val2 = Hex.hexStringToUtf8Text(hex);
        assertEquals(val, val2);
    }
}
