package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.exception.UncheckedException;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p>RSA加密工具类</p>
 * <p>提供RSA非对称加密算法的密钥对生成、加密、解密以及密钥格式转换功能。</p>
 * <p>使用说明：</p>
 * <ul>
 * <li>密钥长度建议使用2048位或更高</li>
 * <li>公钥用于加密，私钥用于解密</li>
 * <li>密钥格式支持X.509（公钥）和PKCS8（私钥）</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class RSA {

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度，通常为1024、2048或4096
     * @return 生成的密钥对
     * @throws UncheckedException 密钥对生成失败时抛出
     */
    public static KeyPair generateKeyPair(int keySize) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(keySize);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            throw new UncheckedException("生成密钥对失败", e);
        }
    }

    /**
     * 使用公钥加密数据
     *
     * @param data      待加密的数据字符串
     * @param publicKey 公钥对象
     * @return 加密后的Base64字符串
     * @throws UncheckedException 加密失败时抛出
     */
    public static String encrypt(String data, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new UncheckedException("加密失败", e);
        }
    }

    /**
     * 使用私钥解密数据
     *
     * @param encryptedData 加密后的Base64字符串
     * @param privateKey    私钥对象
     * @return 解密后的原始数据
     * @throws UncheckedException 解密失败时抛出
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new UncheckedException("解密失败", e);
        }
    }

    /**
     * 将公钥转换为Base64字符串
     *
     * @param publicKey 公钥对象
     * @return Base64编码的公钥字符串
     */
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 将私钥转换为Base64字符串
     *
     * @param privateKey 私钥对象
     * @return Base64编码的私钥字符串
     */
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * 从Base64字符串恢复公钥
     *
     * @param publicKeyStr Base64编码的公钥字符串
     * @return 公钥对象
     * @throws UncheckedException 公钥恢复失败时抛出
     */
    public static PublicKey stringToPublicKey(String publicKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            throw new UncheckedException("恢复公钥失败", e);
        }
    }

    /**
     * 从Base64字符串恢复私钥
     *
     * @param privateKeyStr Base64编码的私钥字符串
     * @return 私钥对象
     * @throws UncheckedException 私钥恢复失败时抛出
     */
    public static PrivateKey stringToPrivateKey(String privateKeyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new UncheckedException("恢复私钥失败", e);
        }
    }

}