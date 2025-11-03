package com.yishuifengxiao.common.tool.codec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 3DES工具类
 * <p>使用 3DES/Triple DES 算法（168位密钥）</p>
 * <p>明确使用 CBC模式、使用 PKCS5Padding 填充、使用随机IV、支持动态密钥生成、使用 SecretKeySpec 处理密钥</p>
 *
 * @author yishui
 * @version 1.0.0
 */
public class TripleDES {
    private static final String DESEDE_ALGORITHM = "DESede";
    private static final String DESEDE_TRANSFORMATION = "DESede/CBC/PKCS5Padding";
    private static final int DESEDE_KEY_SIZE = 168; // 3DES密钥长度
    private static final int IV_LENGTH = 8; // DES IV长度

    /**
     * 生成3DES密钥
     */
    public static String generate3DESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(DESEDE_ALGORITHM);
        keyGen.init(DESEDE_KEY_SIZE);
        SecretKey secretKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 3DES加密
     *
     * @param base64Key base64编码的密钥
     * @param plaintext 待加密的明文
     * @return 加密后的base64编码字符串，包含IV和密文
     * @throws Exception 加密过程中可能抛出的异常
     */
    public static String encrypt(String base64Key, String plaintext) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, DESEDE_ALGORITHM);

        // 生成随机IV
        byte[] iv = new byte[IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(DESEDE_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes("UTF-8"));

        // 组合IV和密文
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);

        return Base64.getEncoder().encodeToString(combined);
    }


    /**
     * 3DES解密
     *
     * @param base64Key     base64编码的密钥
     * @param encryptedText base64编码的加密文本，包含IV和密文
     * @return 解密后的明文字符串
     * @throws Exception 解密过程中可能抛出的异常
     */
    public static String decrypt(String base64Key, String encryptedText) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, DESEDE_ALGORITHM);

        byte[] combined = Base64.getDecoder().decode(encryptedText);

        // 分离IV和密文
        byte[] iv = new byte[IV_LENGTH];
        byte[] ciphertext = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, ciphertext, 0, ciphertext.length);

        Cipher cipher = Cipher.getInstance(DESEDE_TRANSFORMATION);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, "UTF-8");
    }

}