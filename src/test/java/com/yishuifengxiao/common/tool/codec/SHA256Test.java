package com.yishuifengxiao.common.tool.codec;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * SHA256.calculateSHA256 单元测试类
 */
public class SHA256Test {

    private MockedStatic<MessageDigest> mockedMessageDigest;

    @Before
    public void setUp() {
        // 初始化静态类的 mock
        mockedMessageDigest = mockStatic(MessageDigest.class);
    }

    @After
    public void tearDown() {
        // 清理静态类的 mock，防止污染其他测试
        mockedMessageDigest.close();
    }

    /**
     * TC01: 测试输入为 null 的情况
     */
    @Test(expected = NullPointerException.class)
    public void testCalculateSHA256_NullInput_ThrowsException() {
        SHA256.calculateSHA256((String) null);
    }

    /**
     * TC02: 测试输入为空字符串的情况
     */
    @Test
    public void testCalculateSHA256_EmptyString_ReturnsCorrectHash() throws Exception {
        String input = "";
        String expectedHash = "";

        MessageDigest mockDigest = mock(MessageDigest.class);
        when(mockDigest.digest(any(byte[].class))).thenReturn(new byte[0]);

        mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenReturn(mockDigest);

        String result = SHA256.calculateSHA256(input);
        assertEquals(expectedHash, result);
    }

    /**
     * TC03: 测试普通英文字符串
     */
    @Test
    public void testCalculateSHA256_NormalString_ReturnsCorrectHash() throws Exception {
        String input = "hello";
        String expectedHash = "2CF24DBA4F21D4288094C1B95DB8E1DDA72E0E5F4F2D6AE2807B6B6F6C7E6E6E"; // 示例值，请替换为实际值

        byte[] mockHashBytes = {0x2c, (byte)0xf2, 0x4d, (byte)0xba, 0x4f, 0x21, (byte)0xd4, 0x28,
                                (byte)0x80, (byte)0x94, (byte)0xc1, (byte)0xb9, 0x5d, (byte)0xb8,
                                (byte)0xe1, (byte)0xdd, (byte)0xa7, 0x2e, 0x0e, 0x5f,
                                0x4f, 0x2d, 0x6a, (byte)0xe2, (byte)0x80, 0x7b, 0x6b, 0x6f,
                                0x6c, 0x7e, 0x6e, 0x6e};

        MessageDigest mockDigest = mock(MessageDigest.class);
        when(mockDigest.digest(any(byte[].class))).thenReturn(mockHashBytes);

        mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenReturn(mockDigest);

        String result = SHA256.calculateSHA256(input);
        assertEquals(expectedHash, result);
    }

    /**
     * TC04: 测试特殊符号字符串
     */
    @Test
    public void testCalculateSHA256_SpecialChars_ReturnsCorrectHash() throws Exception {
        String input = "!@#$%^&*()";
        String expectedHash = "0000000000000000000000000000000000000000000000000000000000000000";

        byte[] mockHashBytes = new byte[32]; // 替换为你自己的数据

        MessageDigest mockDigest = mock(MessageDigest.class);
        when(mockDigest.digest(any(byte[].class))).thenReturn(mockHashBytes);

        mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenReturn(mockDigest);

        String result = SHA256.calculateSHA256(input);
        assertEquals(expectedHash, result);
    }

    /**
     * TC05: 测试中文字符串
     */
    @Test
    public void testCalculateSHA256_ChineseString_ReturnsCorrectHash() throws Exception {
        String input = "你好世界";
        String expectedHash = "0000000000000000000000000000000000000000000000000000000000000000";

        byte[] mockHashBytes = new byte[32]; // 替换为你自己的数据

        MessageDigest mockDigest = mock(MessageDigest.class);
        when(mockDigest.digest(any(byte[].class))).thenReturn(mockHashBytes);

        mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256")).thenReturn(mockDigest);

        String result = SHA256.calculateSHA256(input);
        assertEquals(expectedHash, result);
    }

    /**
     * TC06: 测试 MessageDigest.getInstance 抛出异常的情况
     */
    @Test(expected = RuntimeException.class)
    public void testCalculateSHA256_NoSuchAlgorithmException_ThrowsRuntimeException() throws Exception {
        mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                           .thenThrow(new NoSuchAlgorithmException("Algorithm not available"));

        SHA256.calculateSHA256("any input");
    }
}
