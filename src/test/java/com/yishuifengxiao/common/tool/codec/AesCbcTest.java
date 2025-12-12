package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * AesCbc工具类的单元测试
 */
public class AesCbcTest {

    // 测试数据
    private static final String TEST_DATA = "1234567890ABCDEF"; // 16字节数据
    private static final String KEY = "0123456789ABCDEF"; // 16字节密钥
    private static final String IV = "1234567890ABCDEF"; // 16字节IV

    /**
     * 测试encrypt(byte[], byte[], byte[])方法 - 正常情况
     */
    @Test
    public void testEncryptByteArray_Normal() throws Exception {
        byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        byte[] key = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] iv = IV.getBytes(StandardCharsets.UTF_8);

        byte[] encrypted = AesCbc.encrypt(data, key, iv);

        assertNotNull("加密结果不应为null", encrypted);
        assertTrue("加密结果应不为空", encrypted.length > 0);
        // 验证长度是16的倍数
        assertEquals("加密结果长度应该是16的倍数", 0, encrypted.length % 16);
    }

    /**
     * 测试encrypt(byte[], byte[], byte[])方法 - 数据长度不是16的倍数时抛出异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEncryptByteArray_IllegalLength() throws Exception {
        byte[] data = "12345".getBytes(StandardCharsets.UTF_8); // 5字节，不是16的倍数
        byte[] key = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] iv = IV.getBytes(StandardCharsets.UTF_8);

        AesCbc.encrypt(data, key, iv);
    }

    /**
     * 测试encrypt(String, String, String)方法 - 正常情况
     */
    @Test
    public void testEncryptString_Normal() throws Exception {
        String dataHex = Hex.bytesToHex(TEST_DATA.getBytes(StandardCharsets.UTF_8));
        String keyHex = Hex.bytesToHex(KEY.getBytes(StandardCharsets.UTF_8));
        String ivHex = Hex.bytesToHex(IV.getBytes(StandardCharsets.UTF_8));

        String encryptedHex = AesCbc.encrypt(dataHex, keyHex, ivHex);

        assertNotNull("加密结果不应为null", encryptedHex);
        assertFalse("加密结果不应为空", encryptedHex.isEmpty());
        // 验证是有效的十六进制字符串
        assertTrue("加密结果应是有效的十六进制字符串", encryptedHex.matches("[0-9A-Fa-f]+"));
    }

    /**
     * 测试encryptToBase64方法 - 正常情况
     */
    @Test
    public void testEncryptToBase64_Normal() throws Exception {
        String encryptedBase64 = AesCbc.encryptToBase64(TEST_DATA, KEY, IV);

        assertNotNull("加密结果不应为null", encryptedBase64);
        assertFalse("加密结果不应为空", encryptedBase64.isEmpty());
        // 验证是有效的Base64字符串
        assertTrue("加密结果应是有效的Base64字符串", encryptedBase64.matches("[A-Za-z0-9+/=]+"));
    }

    /**
     * 测试decrypt(byte[], byte[], byte[])方法 - 正常情况
     */
    @Test
    public void testDecryptByteArray_Normal() throws Exception {
        byte[] data = TEST_DATA.getBytes(StandardCharsets.UTF_8);
        byte[] key = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] iv = IV.getBytes(StandardCharsets.UTF_8);

        // 先加密再解密
        byte[] encrypted = AesCbc.encrypt(data, key, iv);
        byte[] decrypted = AesCbc.decrypt(encrypted, key, iv);

        assertArrayEquals("解密后的数据应与原始数据相同", data, decrypted);
    }

    /**
     * 测试decrypt(String, String, String)方法 - 正常情况
     */
    @Test
    public void testDecryptString_Normal() throws Exception {
        String dataHex = Hex.bytesToHex(TEST_DATA.getBytes(StandardCharsets.UTF_8));
        String keyHex = Hex.bytesToHex(KEY.getBytes(StandardCharsets.UTF_8));
        String ivHex = Hex.bytesToHex(IV.getBytes(StandardCharsets.UTF_8));

        // 先加密再解密
        String encryptedHex = AesCbc.encrypt(dataHex, keyHex, ivHex);
        String decryptedHex = AesCbc.decrypt(encryptedHex, keyHex, ivHex);

        assertEquals("解密后的数据应与原始数据相同", dataHex, decryptedHex);
    }

    /**
     * 测试decryptFromBase64方法 - 正常情况
     */
    @Test
    public void testDecryptFromBase64_Normal() throws Exception {
        // 先加密再解密
        String encryptedBase64 = AesCbc.encryptToBase64(TEST_DATA, KEY, IV);
        String decrypted = AesCbc.decryptFromBase64(encryptedBase64, KEY, IV);

        assertEquals("解密后的数据应与原始数据相同", TEST_DATA, decrypted);
    }

    /**
     * 测试decrypt(byte[], byte[], byte[])方法 - 密文长度不是16的倍数时抛出异常
     */
    @Test(expected = IllegalArgumentException.class)
    public void testDecryptByteArray_IllegalLength() throws Exception {
        byte[] encryptedData = "12345".getBytes(StandardCharsets.UTF_8); // 5字节，不是16的倍数
        byte[] key = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] iv = IV.getBytes(StandardCharsets.UTF_8);

        AesCbc.decrypt(encryptedData, key, iv);
    }

    /**
     * 测试padData和unpadData方法 - 正常情况
     */
    @Test
    public void testPadAndUnpadData_Normal() {
        String shortText = "Hello World!"; // 12字节
        byte[] data = shortText.getBytes(StandardCharsets.UTF_8);

        // 填充数据
        byte[] paddedData = AesCbc.padData(data);
        assertNotNull("填充后的数据不应为null", paddedData);
        assertEquals("填充后的数据长度应为16", 16, paddedData.length);

        // 去除填充
        byte[] unpaddedData = AesCbc.unpadData(paddedData);
        assertArrayEquals("去除填充后的数据应与原始数据相同", data, unpaddedData);
    }

    /**
     * 测试generateIV方法 - 正常情况
     */
    @Test
    public void testGenerateIV_Normal() {
        byte[] iv = AesCbc.generateIV();

        assertNotNull("生成的IV不应为null", iv);
        assertEquals("生成的IV长度应为16", 16, iv.length);
    }

    @Test
    public void test_normal() throws Exception {
        String encrypt = AesCbc.encrypt(TEST_DATA + TEST_DATA, KEY + KEY, IV + IV);
        System.out.println(encrypt);
        String decrypt = AesCbc.decrypt(encrypt, KEY + KEY, IV + IV);
        assertEquals(TEST_DATA + TEST_DATA, decrypt);
    }
}