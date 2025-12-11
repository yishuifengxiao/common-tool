package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Hex工具类atoi方法的单元测试
 */
public class Hex_atoi_Test {

    @Test
    public void test_atoi_with_valid_hex_strings() {
        // 测试正常的十六进制字符串
        assertEquals(Integer.valueOf(10), Hex.atoi("A"));
        assertEquals(Integer.valueOf(255), Hex.atoi("FF"));
        assertEquals(Integer.valueOf(10), Hex.atoi("0a")); // 小写
        assertEquals(Integer.valueOf(16), Hex.atoi("10"));
        assertEquals(Integer.valueOf(0), Hex.atoi("0"));
        assertEquals(Integer.valueOf(0), Hex.atoi("00"));
    }

    @Test
    public void test_atoi_with_spaces() {
        // 测试带有前后空格的十六进制字符串
        assertEquals(Integer.valueOf(10), Hex.atoi(" A "));
        assertEquals(Integer.valueOf(255), Hex.atoi(" FF "));
        assertEquals(Integer.valueOf(16), Hex.atoi(" 10 "));
    }

    @Test
    public void test_atoi_with_null_or_blank_input() {
        // 测试null或空白输入
        assertNull(Hex.atoi(null));
        assertNull(Hex.atoi(""));
        assertNull(Hex.atoi(" "));
        assertNull(Hex.atoi("   "));
    }

    @Test(expected = NumberFormatException.class)
    public void test_atoi_with_invalid_hex_string() {
        // 测试无效的十六进制字符串，应该抛出NumberFormatException
        Hex.atoi("GG"); // G不是有效的十六进制字符
    }

    @Test(expected = NumberFormatException.class)
    public void test_atoi_with_empty_trimmed_string() {
        // 测试只有空格的字符串，在trim后变为空字符串，应该抛出NumberFormatException
        Hex.atoi("   ");
    }
}