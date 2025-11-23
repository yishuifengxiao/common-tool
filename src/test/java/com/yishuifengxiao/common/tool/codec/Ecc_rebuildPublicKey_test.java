package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
public class Ecc_rebuildPublicKey_test {


    /**
     * 测试正常场景：使用有效的公钥字节数组重建公钥
     * 目的：验证方法能够正确重建有效的公钥
     */
    @Test
    public void testRebuildPublicKey_ValidKey() throws Exception {
        // 生成新的密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();

        // 调用被测方法
        PublicKey result = ECC.rebuildPublicKey(publicKeyBytes);

        // 验证结果
        assertNotNull(result);
        assertEquals(keyPair.getPublic(), result);
    }

    /**
     * 测试边界场景：传入空的字节数组
     * 目的：验证方法对空输入的处理能力
     */
    @Test(expected = InvalidKeySpecException.class)
    public void testRebuildPublicKey_EmptyBytes() throws Exception {
        byte[] emptyBytes = new byte[0];
        ECC.rebuildPublicKey(emptyBytes);
    }

    /**
     * 测试异常场景：传入无效的公钥字节数组
     * 目的：验证方法对无效输入的处理能力
     */
    @Test(expected = InvalidKeySpecException.class)
    public void testRebuildPublicKey_InvalidKeyBytes() throws Exception {
        byte[] invalidBytes = "invalid_key_bytes".getBytes(StandardCharsets.UTF_8);
        ECC.rebuildPublicKey(invalidBytes);
    }

    /**
     * 测试异常场景：传入null值
     * 目的：验证方法对null输入的处理能力
     */
    @Test(expected = NullPointerException.class)
    public void testRebuildPublicKey_NullInput() throws Exception {
        ECC.rebuildPublicKey(null);
    }

    /**
     * 测试边界场景：传入超大字节数组
     * 目的：验证方法对大输入的处理能力
     */
    @Test(expected = InvalidKeySpecException.class)
    public void testRebuildPublicKey_LargeInput() throws Exception {
        byte[] largeBytes = new byte[1024 * 1024]; // 1MB
        Arrays.fill(largeBytes, (byte) 1);
        ECC.rebuildPublicKey(largeBytes);
    }

}
