package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;
import org.junit.Assert;
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

    @Test
    public void test_normal_hex() throws Exception {
        String r1 = AesCbc.encrypt("00000000000000000000000000000001", "A3A2B056FA38B2D461DFD2C4794EF3E1",
                "00000000000000000000000000000000");
        Assert.assertEquals("0A9707B4AA79E9B451850CC2326DB582", r1);
        r1 = AesCbc.encrypt("00000000000000000000000000000001", "A3A2B056FA38B2D461DFD2C4794EF3E1",
                null);
        Assert.assertEquals("0A9707B4AA79E9B451850CC2326DB582", r1);
        String r2 = AesCbc.encrypt("BF2407B805800388370A800000000000", "A3A2B056FA38B2D461DFD2C4794EF3E1",
                "0A9707B4AA79E9B451850CC2326DB582");
        Assert.assertEquals("3E625BAC9B572C30CB0FBD36CB239D07", r2);

        String r3 = AesCbc.encrypt("00000000000000000000000000000002", "A3A2B056FA38B2D461DFD2C4794EF3E1",
                "00000000000000000000000000000000");
        Assert.assertEquals("BA975F05026770690FEC301163F35009", r3);

        String r4 = AesCbc.encrypt(
                "BF253F5A0A986811415220491600719104636d6363920870726f66696c6531950102B6173015800204F0810F31302e382e33302e37353a38303930B705800344F4448000000000000000000000000000", "A3A2B056FA38B2D461DFD2C4794EF3E1",
                "BA975F05026770690FEC301163F35009");
        Assert.assertEquals(
                "C42284A9B74959541432BA3E70F73D76D3585577ADCD7ABF923DDDDC8A79A2F903F715FD52E6ED75575A5A22AF6EE616408117E0E761B5EB39B75F21A1C27314E7D1F1741D5CEC3E56480197D78AFF1A", r4);

        String r5 = AesCbc.encrypt(
                "BF26368010105288ba317e46fd9eabddb8b0a347a3811021c18be721f548feb7bf49e716da68648210ad027d2b289c465d8aa99c43d8142a4c80000000000000", "A3A2B056FA38B2D461DFD2C4794EF3E1",
                "0EB8778AC4B5EBA99FCEE92A7D05281D");
        Assert.assertEquals(
                "7645C492AC7EE3CC25835243A7D3DC8B5F942EE11641B608D1D919D727291819715ED199D996BE6A61FB021C88C65599F3FE46BF3A3F05F339A758FBFE55210A", r5);
    }
}