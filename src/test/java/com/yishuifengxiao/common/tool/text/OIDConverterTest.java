package com.yishuifengxiao.common.tool.text;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * OIDConverter 类中 hexToDotNotation 方法的单元测试
 */
public class OIDConverterTest {

    /**
     * 测试输入为 null 的情况
     * 应当抛出 IllegalArgumentException 异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_NullInput_ThrowsException() {
        OIDConverter.hexToDotNotation(null);
    }

    /**
     * 测试输入为空字符串的情况
     * 应当抛出 IllegalArgumentException 异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_EmptyInput_ThrowsException() {
        OIDConverter.hexToDotNotation("");
    }

    /**
     * 测试输入只包含空格的情况
     * 应当抛出 IllegalArgumentException 异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_OnlySpaces_ThrowsException() {
        OIDConverter.hexToDotNotation("   ");
    }

    /**
     * 测试输入包含无效十六进制字符的情况
     * 应当抛出 IllegalArgumentException 异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_InvalidHexChars_ThrowsException() {
        OIDConverter.hexToDotNotation("GG");
    }

    /**
     * 测试输入长度为奇数的情况
     * 应当抛出 IllegalArgumentException 异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_OddLength_ThrowsException() {
        OIDConverter.hexToDotNotation("123"); // 奇数长度
    }

    /**
     * 测试正常有效的十六进制输入
     * 应返回正确的 OID 表示
     */
    @Test
    public void testHexToDotNotation_ValidInput_ReturnsCorrectOid() {
        String result = OIDConverter.hexToDotNotation("2B0601040182371514");
        assertEquals("1.3.6.1.4.1.59477.20", result);
    }

    /**
     * 测试带有空格的有效输入
     * 应成功忽略空格并正确解析
     */
    @Test
    public void testHexToDotNotation_ValidWithSpaces_IgnoresSpaces() {
        String result = OIDConverter.hexToDotNotation("2B 06 01 04 01 82 37 15 14");
        assertEquals("1.3.6.1.4.1.59477.20", result);
    }

    /**
     * 测试大小写混合的输入
     * 应能自动统一为小写并正确解析
     */
    @Test
    public void testHexToDotNotation_CaseInsensitive_HandlesMixedCase() {
        String result = OIDConverter.hexToDotNotation("2b0601040182371514");
        assertEquals("1.3.6.1.4.1.59477.20", result);
    }

    /**
     * 测试 base128 编码最后一个数据未结束的情况
     * 应抛出 IllegalArgumentException 异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHexToDotNotation_IncompleteBase128Encoding_ThrowsException() {
        // 构造一个 base128 编码未终止的数据（最后一位高位仍为1）
        OIDConverter.hexToDotNotation("2B80"); // 第二字节最高位为1，没有结束标志
    }
}
