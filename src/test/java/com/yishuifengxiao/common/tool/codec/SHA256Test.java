package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;
import org.mockito.MockedStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mockStatic;

/**
 * SHA256.calculateSHA256 单元测试类
 * <p>
 * 通过标准 SHA-256 测试向量验证真实计算结果，不再对 MessageDigest 的成功路径做静态 mock。
 * </p>
 */
public class SHA256Test {

    /**
     * TC01: 输入为 null 时返回 null（与 Javadoc 及工具文档约定一致）
     */
    @Test
    public void testCalculateSHA256_NullInput_ReturnsNull() {
        assertNull(SHA256.calculateSHA256((String) null));
        assertNull(SHA256.calculateSHA256((byte[]) null));
    }

    /**
     * TC02: 空字符串的 SHA-256 标准向量
     */
    @Test
    public void testCalculateSHA256_EmptyString_ReturnsCorrectHash() {
        assertEquals("E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855",
                SHA256.calculateSHA256(""));
    }

    /**
     * TC03: 普通英文字符串的标准向量
     */
    @Test
    public void testCalculateSHA256_NormalString_ReturnsCorrectHash() {
        assertEquals("2CF24DBA5FB0A30E26E83B2AC5B9E29E1B161E5C1FA7425E73043362938B9824",
                SHA256.calculateSHA256("hello"));
    }

    /**
     * TC04: 特殊符号字符串的标准向量
     */
    @Test
    public void testCalculateSHA256_SpecialChars_ReturnsCorrectHash() {
        assertEquals("95CE789C5C9D18490972709838CA3A9719094BCA3AC16332CFEC0652B0236141",
                SHA256.calculateSHA256("!@#$%^&*()"));
    }

    /**
     * TC05: 中文字符串（UTF-8）的标准向量
     */
    @Test
    public void testCalculateSHA256_ChineseString_ReturnsCorrectHash() {
        assertEquals("BECA6335B20FF57CCC47403EF4D9E0B8FCCB4442B3151C2E7D50050673D43172",
                SHA256.calculateSHA256("你好世界"));
    }

    /**
     * TC06: 十六进制输入非法时抛出 IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCalculateSHA256FromHex_InvalidHex_ThrowsException() {
        SHA256.calculateSHA256FromHex("GG");
    }

    /**
     * TC07: MessageDigest.getInstance 抛出 NoSuchAlgorithmException 时转换为 RuntimeException
     */
    @Test(expected = RuntimeException.class)
    public void testCalculateSHA256_NoSuchAlgorithmException_ThrowsRuntimeException() {
        try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
            mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("Algorithm not available"));
            SHA256.calculateSHA256("any input");
        }
    }
}
