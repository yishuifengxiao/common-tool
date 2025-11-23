package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
public class ECC_rebuildPrivateKey_test {

    /**
     * 测试正常场景：使用有效的PKCS8格式私钥字节数组重建私钥
     * 目的：验证方法能够正确解析有效的PKCS8格式私钥字节数组并返回PrivateKey对象
     */
    @Test
    public void testRebuildPrivateKey_ValidPKCS8Key() throws Exception {
        // 生成新的EC密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();

        // 调用被测方法
        PrivateKey result = ECC.rebuildPrivateKey(privateKeyBytes);

        // 验证结果
        assertNotNull(result);
        assertEquals("PKCS#8", result.getFormat());
    }

    /**
     * 测试边界场景：使用空字节数组重建私钥
     * 目的：验证方法对空字节数组输入的处理，预期抛出异常
     */
    @Test(expected = InvalidKeySpecException.class)
    public void testRebuildPrivateKey_EmptyByteArray() throws Exception {
        byte[] emptyBytes = new byte[0];
        ECC.rebuildPrivateKey(emptyBytes);
    }

    /**
     * 测试异常场景：使用无效的PKCS8格式字节数组重建私钥
     * 目的：验证方法对无效PKCS8格式数据的处理，预期抛出异常
     */
    @Test(expected = InvalidKeySpecException.class)
    public void testRebuildPrivateKey_InvalidPKCS8Format() throws Exception {
        byte[] invalidBytes = "invalid_key_format".getBytes(StandardCharsets.UTF_8);
        ECC.rebuildPrivateKey(invalidBytes);
    }

    /**
     * 测试异常场景：使用非EC算法的PKCS8格式密钥
     * 目的：验证方法对非EC算法密钥的处理，预期抛出异常
     */
    @Test(expected = InvalidKeySpecException.class)
    public void testRebuildPrivateKey_NonECAlgorithmKey() throws Exception {
        // 生成RSA密钥对
        KeyPairGenerator rsaKeyPairGenerator = KeyPairGenerator.getInstance("RSA");
        rsaKeyPairGenerator.initialize(2048);
        KeyPair rsaKeyPair = rsaKeyPairGenerator.generateKeyPair();
        byte[] rsaPrivateKeyBytes = rsaKeyPair.getPrivate().getEncoded();

        ECC.rebuildPrivateKey(rsaPrivateKeyBytes);
    }

    /**
     * 测试异常场景：输入为null
     * 目的：验证方法对null输入的处理，预期抛出NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testRebuildPrivateKey_NullInput() throws Exception {
        ECC.rebuildPrivateKey(null);
    }
}
