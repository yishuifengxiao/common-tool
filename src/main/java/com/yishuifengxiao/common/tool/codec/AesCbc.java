package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;


/**
 * AES CBC 无填充模式加解密工具类
 * 注意：由于无填充，明文长度必须是16字节的倍数
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class AesCbc {

    // 算法/模式/填充
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/NoPadding";
    public static String key = "0123456789ABCDEF"; // 16字节密钥（AES-128）
    public static String iv = "1234567890ABCDEF";  // 16字节IV

    /**
     * AES CBC无填充加密
     *
     * @param data 明文（必须是16字节的倍数）
     * @param key  密钥（16字节：AES-128, 24字节：AES-192, 32字节：AES-256）
     * @param iv   初始向量（16字节）
     * @return 加密后的字节数组（密文）
     * @throws IllegalArgumentException 当参数不符合要求时
     * @throws RuntimeException         当加密过程中发生错误时
     */
    public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) {
        // 检查参数有效性
        // 检查数据长度是否为16的倍数
        if (data.length % 16 != 0) {
            throw new IllegalArgumentException("明文长度必须是16字节的倍数");
        }

        // 检查密钥长度（AES支持128位、192位、256位）
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("密钥长度必须是16字节（AES-128）、24字节（AES-192）或32字节（AES-256）");
        }

        // 检查初始向量长度（必须是16字节）
        if (iv.length != 16) {
            throw new IllegalArgumentException("初始向量长度必须是16字节");
        }

        try {
            // 创建密钥规范
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

            // 创建IV规范
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // 创建密码器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            // 加密
            byte[] encrypted = cipher.doFinal(data);

            return encrypted;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("加密过程中发生错误: " + e.getMessage(), e);
        }
    }

    /**
     * AES CBC无填充加密
     *
     * @param data 明文（十六进制字符串格式，长度必须是16字节的倍数）
     * @param key  密钥（十六进制字符串格式）
     * @param iv   初始向量（十六进制字符串格式）
     * @return 加密后的十六进制字符串
     * @throws Exception 加密过程中可能抛出的异常
     */
    public static String encrypt(String data, String key, String iv) throws Exception {
        byte[] encrypted = encrypt(Hex.hexToBytes(data), Hex.hexToBytes(key), Hex.hexToBytes(iv));
        return Hex.bytesToHex(encrypted);
    }

    /**
     * AES CBC无填充加密（返回Base64字符串）
     *
     * @param data 明文（必须是16字节的倍数）
     * @param key  密钥
     * @param iv   初始向量
     * @return Base64编码的加密字符串
     */
    public static String encryptToBase64(String data, String key, String iv) throws Exception {
        byte[] encrypted = encrypt(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES CBC无填充解密
     *
     * @param encryptedData 加密数据
     * @param key           密钥
     * @param iv            初始向量
     * @return 解密后的字节数组
     */
    public static byte[] decrypt(byte[] encryptedData, byte[] key, byte[] iv) throws Exception {
        // 检查数据长度是否为16的倍数
        if (encryptedData.length % 16 != 0) {
            throw new IllegalArgumentException("密文长度必须是16字节的倍数");
        }
        // 检查密钥长度（AES支持128位、192位、256位）
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("密钥长度必须是16字节（AES-128）、24字节（AES-192）或32字节（AES-256）");
        }

        // 检查初始向量长度（必须是16字节）
        if (iv.length != 16) {
            throw new IllegalArgumentException("初始向量长度必须是16字节");
        }
        // 创建密钥规范
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ALGORITHM);

        // 创建IV规范
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        // 创建密码器
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        // 解密
        byte[] decrypted = cipher.doFinal(encryptedData);

        return decrypted;
    }

    /**
     * AES CBC无填充解密（从Base64字符串）
     *
     * @param base64Data Base64编码的加密字符串
     * @param key        密钥
     * @param iv         初始向量
     * @return 解密后的字符串
     */
    public static String decryptFromBase64(String base64Data, String key, String iv) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(base64Data);
        byte[] decrypted = decrypt(encryptedData, key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8));
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * AES CBC无填充解密
     *
     * @param key              密钥（十六进制字符串格式）
     * @param iv               初始向量（十六进制字符串格式）
     * @param encryptedHexData 加密数据（十六进制字符串格式）
     * @return 解密后的字符串
     * @throws Exception 解密过程中可能抛出的异常
     */
    public static String decrypt(String encryptedHexData, String key, String iv) throws Exception {
        byte[] encryptedData = Hex.hexToBytes(encryptedHexData);
        byte[] decrypted = decrypt(encryptedData, Hex.hexToBytes(key), Hex.hexToBytes(iv));
        return Hex.bytesToHex(decrypted);
    }

    /**
     * 填充数据到16字节的倍数（PKCS7填充）
     * 用于处理需要填充的情况
     */
    public static byte[] padData(byte[] data) {
        int blockSize = 16;
        int paddingSize = blockSize - (data.length % blockSize);
        byte[] paddedData = new byte[data.length + paddingSize];
        System.arraycopy(data, 0, paddedData, 0, data.length);

        // 填充字节的值等于填充的长度
        for (int i = data.length; i < paddedData.length; i++) {
            paddedData[i] = (byte) paddingSize;
        }

        return paddedData;
    }

    /**
     * 移除填充数据
     */
    public static byte[] unpadData(byte[] data) {
        int paddingSize = data[data.length - 1] & 0xFF;

        // 验证填充是否有效
        if (paddingSize > 16 || paddingSize < 1) {
            return data; // 没有有效的填充，返回原数据
        }

        // 检查填充字节是否正确
        for (int i = data.length - paddingSize; i < data.length; i++) {
            if (data[i] != paddingSize) {
                return data; // 填充不正确，返回原数据
            }
        }

        byte[] unpaddedData = new byte[data.length - paddingSize];
        System.arraycopy(data, 0, unpaddedData, 0, unpaddedData.length);
        return unpaddedData;
    }

    /**
     * 生成随机IV（16字节）
     */
    public static byte[] generateIV() {
        byte[] iv = new byte[16];
        new java.security.SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) {
        try {
            // 测试数据（正好16字节）
            String originalText = "1234567890ABCDEF"; // 16个字符


            System.out.println("原始文本: " + originalText);
            System.out.println("密钥: " + key);
            System.out.println("IV: " + iv);
            System.out.println("原始文本字节长度: " + originalText.getBytes(StandardCharsets.UTF_8).length);

            // 加密
            String encryptedBase64 = encryptToBase64(originalText, key, iv);
            System.out.println("\n加密后(Base64): " + encryptedBase64);

            // 解密
            String decryptedText = decryptFromBase64(encryptedBase64, key, iv);
            System.out.println("解密后: " + decryptedText);

            // 测试需要填充的情况
            System.out.println("\n=== 测试需要填充的情况 ===");
            String shortText = "Hello World!"; // 12个字符，需要填充
            System.out.println("短文本: " + shortText);

            // 先填充再加密
            byte[] paddedData = padData(shortText.getBytes(StandardCharsets.UTF_8));
            byte[] encrypted = encrypt(paddedData, key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8));

            // 解密
            byte[] decrypted = decrypt(encrypted, key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8));
            byte[] unpaddedData = unpadData(decrypted);
            System.out.println("解密并去除填充后: " + new String(unpaddedData, StandardCharsets.UTF_8));

            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}