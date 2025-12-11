package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Hex工具类中base64ToHex和parseBase64String方法的单元测试
 */
public class Hex_base64ToHex_Test {

    /**
     * 测试base64ToHex方法 - 空或null输入
     */
    @Test
    public void testBase64ToHexWithNullAndEmptyInput() {
        // 测试null输入
        assertEquals("", Hex.base64ToHex(null));

        // 测试空字符串输入
        assertEquals("", Hex.base64ToHex(""));
    }

    /**
     * 测试base64ToHex方法 - 正常Base64字符串
     */
    @Test
    public void testBase64ToHexWithValidBase64() {
        // 测试简单字符串"hello"
        String helloStr = "hello";
        String helloBase64 = Base64.getEncoder().encodeToString(helloStr.getBytes(StandardCharsets.UTF_8));
        String expectedHelloHex = Hex.bytesToHex(helloStr.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedHelloHex, Hex.base64ToHex(helloBase64));

        // 测试包含中文的字符串
        String chineseStr = "你好世界";
        String chineseBase64 = Base64.getEncoder().encodeToString(chineseStr.getBytes(StandardCharsets.UTF_8));
        String expectedChineseHex = Hex.bytesToHex(chineseStr.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedChineseHex, Hex.base64ToHex(chineseBase64));

        // 测试复杂字符串
        String complexStr = "Hello World! 你好世界 123";
        String complexBase64 = Base64.getEncoder().encodeToString(complexStr.getBytes(StandardCharsets.UTF_8));
        String expectedComplexHex = Hex.bytesToHex(complexStr.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedComplexHex, Hex.base64ToHex(complexBase64));
    }

    /**
     * 测试base64ToHex方法 - URL安全的Base64字符串
     */
    @Test
    public void testBase64ToHexWithUrlSafeBase64() {
        // 测试包含特殊字符的字符串
        String specialStr = "Hello+World/=";
        String urlSafeBase64 = Base64.getUrlEncoder().encodeToString(specialStr.getBytes(StandardCharsets.UTF_8));
        String expectedHex = Hex.bytesToHex(specialStr.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedHex, Hex.base64ToHex(urlSafeBase64));
    }

    /**
     * 测试parseBase64String方法 - 空或null输入
     */
    @Test
    public void testParseBase64StringWithNullAndEmptyInput() {
        // 测试null输入
        assertArrayEquals(new byte[0], Hex.parseBase64String(null));

        // 测试空字符串输入
        assertArrayEquals(new byte[0], Hex.parseBase64String(""));
    }

    /**
     * 测试parseBase64String方法 - 全空白字符输入
     */
    @Test
    public void testParseBase64StringWithWhitespaceOnly() {
        // 测试只有空格的输入
        assertNull(Hex.parseBase64String("   "));

        // 测试只有制表符的输入
        assertNull(Hex.parseBase64String("\t\t\t"));

        // 测试只有换行符的输入
        assertNull(Hex.parseBase64String("\n\n\n"));
    }

    /**
     * 测试parseBase64String方法 - 包含空格的有效Base64
     */
    @Test
    public void testParseBase64StringWithSpacedBase64() {
        // 测试包含空格的有效Base64字符串
        String originalStr = "Hello World";
        String base64 = Base64.getEncoder().encodeToString(originalStr.getBytes(StandardCharsets.UTF_8));
        // 在Base64字符串中添加空格
        String spacedBase64 = base64.substring(0, 4) + " " + base64.substring(4, 8) + " " + base64.substring(8);

        byte[] expectedBytes = originalStr.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = Hex.parseBase64String(spacedBase64);
        assertArrayEquals(expectedBytes, actualBytes);
    }

    /**
     * 测试parseBase64String方法 - MIME格式的Base64（包含换行符）
     */
    @Test
    public void testParseBase64StringWithMimeFormat() {
        // 测试长字符串生成的MIME格式Base64（包含换行符）
        String longStr = "This is a long string that will generate Base64 with line breaks when using MIME encoder.";
        String mimeBase64 = Base64.getMimeEncoder().encodeToString(longStr.getBytes(StandardCharsets.UTF_8));
        byte[] expectedBytes = longStr.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = Hex.parseBase64String(mimeBase64);
        assertArrayEquals(expectedBytes, actualBytes);
    }

    /**
     * 测试parseBase64String方法 - URL安全的Base64
     */
    @Test
    public void testParseBase64StringWithUrlSafeBase64() {
        // 测试URL安全的Base64字符串
        String urlStr = "Hello+World/Foo=Bar";
        String urlSafeBase64 = Base64.getUrlEncoder().encodeToString(urlStr.getBytes(StandardCharsets.UTF_8));
        byte[] expectedBytes = urlStr.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = Hex.parseBase64String(urlSafeBase64);
        assertArrayEquals(expectedBytes, actualBytes);
    }


    /**
     * 测试base64ToHex和parseBase64String方法的集成
     */
    @Test
    public void testBase64ToHexAndParseBase64StringIntegration() {
        // 测试完整的转换链：字符串 -> Base64 -> 字节数组 -> 十六进制字符串
        String originalStr = "集成测试 Integration Test 123";
        String base64Str = Base64.getEncoder().encodeToString(originalStr.getBytes(StandardCharsets.UTF_8));
        byte[] parsedBytes = Hex.parseBase64String(base64Str);
        String hexFromMethod = Hex.base64ToHex(base64Str);
        String expectedHex = Hex.bytesToHex(parsedBytes);

        assertEquals(expectedHex, hexFromMethod);
        assertArrayEquals(originalStr.getBytes(StandardCharsets.UTF_8), parsedBytes);
    }

    /**
     * 测试边界情况
     */
    @Test
    public void testEdgeCases() {
        // 测试单字符
        String singleChar = "A";
        String singleCharBase64 = Base64.getEncoder().encodeToString(singleChar.getBytes(StandardCharsets.UTF_8));
        String expectedSingleCharHex = Hex.bytesToHex(singleChar.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedSingleCharHex, Hex.base64ToHex(singleCharBase64));

        // 测试特殊字节值
        byte[] specialBytes = new byte[]{0, -1, 127, -128};
        String specialBase64 = Base64.getEncoder().encodeToString(specialBytes);
        String expectedSpecialHex = Hex.bytesToHex(specialBytes);
        assertEquals(expectedSpecialHex, Hex.base64ToHex(specialBase64));
    }
}
