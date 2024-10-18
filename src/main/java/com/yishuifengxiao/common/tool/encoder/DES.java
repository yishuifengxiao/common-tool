package com.yishuifengxiao.common.tool.encoder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;

/**
 * <p>
 * DES加密工具
 * </p>
 * 基于DES加解密实现的加密工具，该工具可以进行可逆加密，加密时的秘钥很重要，一定要自己改秘钥，打死也不要告诉其他人。
 * <strong>该工具是一个线程安全类的工具。</strong>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class DES {

    /**
     * 密钥，是加密解密的凭据，长度为8的倍数
     */
    private static final String PASSWORD_CRYPT_KEY = "yishui@#";
    /**
     * 加密方式
     */
    private static final String TYPE = "DES";

    /**
     * 密钥长度必须为8的倍数
     */
    private static final int LENGTH = 8;

    /**
     * 偶数的标志
     */
    private static final int EVEN_FLAG = 2;

    /**
     * 对输入的数据进行加密
     *
     * @param key  加密的密钥，如果为空则以默认密码进行加密，如果密钥长度不是8的倍数，系统会自动补0
     * @param data 需要加密的数据
     * @return 加密后的数据, null表示加密失败
     */
    public static final String encrypt(String key, String data) {
        try {
            return byte2hex(encrypt(data.getBytes("utf-8"), keyValidate(key).getBytes("utf-8")));
        } catch (Exception e) {
            log.info("【易水工具】使用密钥加密数据 {} 时出现问题，出现问题的原因为 {}", data, e.getMessage());
        }
        return null;
    }

    /**
     * 用默认的密码进行数据加密
     *
     * @param data 需要加密的数据
     * @return 加密后的数据, null表示加密失败
     */
    public static final String encrypt(String data) {
        return encrypt(null, data);
    }

    /**
     * 对输入的数据进行解密
     *
     * @param key  解密的密钥，如果为空则以默认密码进行加密，如果密钥长度不是8的倍数，系统会自动补0
     * @param data 需要解密的数据
     * @return 解密后的数据, null表示解密失败
     */
    public static final String decrypt(String key, String data) {
        try {
            return new String(decrypt(hex2byte(data.getBytes("utf-8")), keyValidate(key).getBytes("utf-8")));
        } catch (Exception e) {
            log.info("【易水工具】使用密钥解密数据 {} 时出现问题，出现问题的原因为 {}", data, e.getMessage());
        }
        return null;
    }

    /**
     * 用默认的密码进行数据解密
     *
     * @param data 需要解密的数据
     * @return 解密后的数据, null表示解密失败
     */
    public static final String decrypt(String data) {
        return decrypt(null, data);
    }

    /**
     * 加密
     *
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回加密后的数据
     * @throws Exception
     */
    private static byte[] encrypt(byte[] src, byte[] key) throws Exception {

        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密匙数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密匙工厂，然后用它把DESKeySpec转换成
        // 一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(TYPE);

        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(TYPE);

        // 用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        // 现在，获取数据并加密
        // 正式执行加密操作
        return cipher.doFinal(src);

    }

    /**
     * 解密
     *
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回解密后的原始数据
     * @throws Exception
     */
    private static byte[] decrypt(byte[] src, byte[] key) throws Exception {

        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密匙数据创建一个DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
        // 一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(TYPE);

        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(TYPE);

        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        // 现在，获取数据并解密
        // 正式执行解密操作
        return cipher.doFinal(src);

    }

    /**
     * 对key进行校验
     *
     * @param key 原始的key
     * @return 校验之后的可以
     */
    private static String keyValidate(String key) {
        if (StringUtils.isBlank(key)) {
            return PASSWORD_CRYPT_KEY;
        }
        StringBuilder sb = new StringBuilder(key.trim());
        if (key.trim().length() % LENGTH != 0) {
            for (int i = 0; i < (LENGTH - key.trim().length() % LENGTH); i++) {
                sb.append("0");
            }
        }
        return sb.toString();
    }

    /**
     * 二行制转字符串
     *
     * @param b
     * @return
     */
    private static String byte2hex(byte[] b) {

        StringBuilder hs = new StringBuilder();

        StringBuilder stmp = new StringBuilder();

        for (int n = 0; n < b.length; n++) {
            stmp = new StringBuilder((java.lang.Integer.toHexString(b[n] & 0XFF)));
            if (stmp.length() == 1) {
                hs = hs.append("0").append(stmp);
            } else {
                hs = hs.append(stmp);
            }
        }
        return hs.toString().toUpperCase();
    }

    private static byte[] hex2byte(byte[] b) {

        if ((b.length % EVEN_FLAG) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += EVEN_FLAG) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

}
