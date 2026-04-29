package com.yishuifengxiao.common.tool.codec;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * <p>AES加密工具类</p>
 * <p>基于AES对称加密算法实现的数据加解密工具。</p>
 * <p>特性：</p>
 * <ul>
 * <li>使用AES/ECB/NoPadding模式</li>
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
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128, new SecureRandom(key.getBytes(StandardCharsets.UTF_8)));
            SecretKey aesKey = new SecretKeySpec(keygen.generateKey().getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return new String(Base64.getEncoder().encode(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8))),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("数据加密失败，数据: {}, 错误: {}", data, e.getMessage());
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
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128, new SecureRandom(key.getBytes(StandardCharsets.UTF_8)));
            SecretKey aesKey = new SecretKeySpec(keygen.generateKey().getEncoded(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes(StandardCharsets.UTF_8))),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("数据解密失败，数据: {}, 错误: {}", data, e.getMessage());
            }
        }
        return null;
    }

}