package com.yishuifengxiao.common.tool.codec;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 编解码兼容性回归测试
 * <p>
 * 覆盖 AES/CBC/NoPadding 的历史密文兼容语义（空 IV 视为16字节全零）以及
 * ECC 密钥十六进制表示按曲线字段长度计算的行为。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
class CodecCompatibilityTest {

    private static final byte[] KEY = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DATA = "0123456789ABCDEF".getBytes(StandardCharsets.UTF_8);

    /**
     * 空 IV 加密后可使用空 IV 解密，保持历史兼容
     *
     * @throws Exception 加解密异常
     */
    @Test
    void aesCbcNullIvRoundTrip() throws Exception {
        byte[] encrypted = AesCbc.encrypt(DATA, KEY, null);
        assertArrayEquals(DATA, AesCbc.decrypt(encrypted, KEY, null));
    }

    /**
     * 空数组 IV 与 null IV 语义一致
     *
     * @throws Exception 加解密异常
     */
    @Test
    void aesCbcEmptyIvRoundTrip() throws Exception {
        byte[] encrypted = AesCbc.encrypt(DATA, KEY, new byte[0]);
        assertArrayEquals(DATA, AesCbc.decrypt(encrypted, KEY, new byte[0]));
        assertArrayEquals(AesCbc.encrypt(DATA, KEY, null), encrypted);
    }

    /**
     * 非16字节 IV 必须被拒绝，而不是静默降级
     */
    @Test
    void aesCbcInvalidIvLengthRejected() {
        assertThrows(IllegalArgumentException.class, () -> AesCbc.encrypt(DATA, KEY, new byte[8]));
        assertThrows(IllegalArgumentException.class, () -> AesCbc.decrypt(DATA, KEY, new byte[8]));
    }

    /**
     * 空数据与 null 数据不应触发空指针
     */
    @Test
    void unpadDataHandlesEmptyInput() {
        assertNull(AesCbc.unpadData(null));
        assertEquals(0, AesCbc.unpadData(new byte[0]).length);
        // 填充值大于数据长度时视为无填充，返回原数组
        byte[] shortData = new byte[]{10};
        assertSame(shortData, AesCbc.unpadData(shortData));
    }

    /**
     * P-256 曲线公钥为 04 + 32字节X + 32字节Y，私钥D为32字节
     */
    @Test
    void ecP256KeyHexLength() {
        KeyPair keyPair = KeyPairHelper.generateECCKeyPair("secp256r1");
        assertEquals(2 + 64 + 64, KeyPairHelper.getPublicKeyHex(keyPair).length());
        assertEquals(64, KeyPairHelper.getPrivateKeyDHex(keyPair).length());
    }

    /**
     * P-384 曲线公钥为 04 + 48字节X + 48字节Y，私钥D为48字节
     */
    @Test
    void ecP384KeyHexLength() {
        KeyPair keyPair = KeyPairHelper.generateECCKeyPair("secp384r1");
        assertEquals(2 + 96 + 96, KeyPairHelper.getPublicKeyHex(keyPair).length());
        assertEquals(96, KeyPairHelper.getPrivateKeyDHex(keyPair).length());
    }
}
