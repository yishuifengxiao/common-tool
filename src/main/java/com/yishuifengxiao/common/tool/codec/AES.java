package com.yishuifengxiao.common.tool.codec;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * <p>AES加密工具类</p>
 * <p>基于AES对称加密算法实现的数据加解密工具。</p>
 * <p>特性：</p>
 * <ul>
 * <li>使用AES/ECB/PKCS5Padding模式</li>
 * <li>支持128位密钥长度</li>
 * <li>加密结果使用Base64编码</li>
 * <li>提供默认密钥和自定义密钥两种方式</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class AES {

    /**
     * 默认加密密钥，生产环境应使用自定义密钥
     */
    private static final String PASSWORD_CRYPT_KEY = "yishui@#";

    /**
     * 使用默认密钥加密数据
     *
     * @param data 需要加密的数据
     * @return 加密后的Base64字符串，数据为空或加密失败返回null
     */
    public static final String encrypt(String data) {
        return encrypt(null, data);
    }

    /**
     * 使用指定密钥加密数据
     *
     * @param key  加密密钥，为空时使用默认密钥
     * @param data 需要加密的数据
     * @return 加密后的Base64字符串，数据为空或加密失败返回null
     */
    public static final String encrypt(String key, String data) {
        if (StringUtils.isBlank(key)) {
            key = PASSWORD_CRYPT_KEY;
        }
        if (StringUtils.isBlank(data)) {
            return null;
        }
        try {
            SecretKey aesKey = deriveKey(key);
            // 显式声明转换方式，避免依赖JCE默认的算法/模式/填充
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return new String(Base64.getEncoder().encode(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8))),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 不记录明文数据，避免敏感信息进入日志
            if (log.isDebugEnabled()) {
                log.debug("数据加密失败，错误: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 使用默认密钥解密数据
     *
     * @param data 待解密的数据（Base64编码）
     * @return 解密后的原始数据，数据为空或解密失败返回null
     */
    public static final String decrypt(String data) {
        return decrypt(null, data);
    }

    /**
     * 使用指定密钥解密数据
     *
     * @param key  解密密钥，为空时使用默认密钥
     * @param data 待解密的数据（Base64编码）
     * @return 解密后的原始数据，数据为空或解密失败返回null
     */
    public static final String decrypt(String key, String data) {
        if (StringUtils.isBlank(key)) {
            key = PASSWORD_CRYPT_KEY;
        }
        if (StringUtils.isBlank(data)) {
            return null;
        }
        try {
            SecretKey aesKey = deriveKey(key);
            // 显式声明转换方式，避免依赖JCE默认的算法/模式/填充
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8))),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 不记录密文数据，避免敏感信息进入日志
            if (log.isDebugEnabled()) {
                log.debug("数据解密失败，错误: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 由字符串密钥确定性地派生128位AES密钥
     * <p>
     * 使用SHA-256对密钥字符串求哈希并取前16字节。不能用 {@code new SecureRandom(key)} 派生：
     * SecureRandom 只是补充熵，不是确定性KDF，同一密钥字符串在不同调用/不同JVM中会得到不同密钥，
     * 导致加密后的数据无法解密。
     * </p>
     *
     * @param key 密钥字符串
     * @return 128位AES密钥
     */
    private static SecretKey deriveKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(Arrays.copyOf(hashed, 16), "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 算法不可用", e);
        }
    }

}