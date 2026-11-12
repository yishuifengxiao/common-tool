package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


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

    /**
     * AES 分组长度（字节）
     */
    private static final int BLOCK_SIZE = 16;

    /**
     * 空初始向量（16字节全零），用于兼容未传入 IV 的历史密文
     */
    private static final byte[] ZERO_IV = new byte[BLOCK_SIZE];

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
        if (null == data) {
            throw new IllegalArgumentException("明文不能为空");
        }
        // 检查数据长度是否为16的倍数
        if (data.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("明文长度必须是16字节的倍数");
        }

        // 检查密钥长度（AES支持128位、192位、256位）
        if (null == key || (key.length != 16 && key.length != 24 && key.length != 32)) {
            throw new IllegalArgumentException("密钥长度必须是16字节（AES-128）、24字节（AES-192）或32字节（AES-256）");
        }
        iv = normalizeIv(iv);

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
        byte[] encrypted = encrypt(Hex.hexToBytes(data), Hex.hexToBytes(key), StringUtils.isNotBlank(iv) ?
                Hex.hexToBytes(iv) : null);
        return Hex.bytesToHex(encrypted);
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
        if (null == encryptedData) {
            throw new IllegalArgumentException("密文不能为空");
        }
        // 检查数据长度是否为16的倍数
        if (encryptedData.length % BLOCK_SIZE != 0) {
            throw new IllegalArgumentException("密文长度必须是16字节的倍数");
        }
        // 检查密钥长度（AES支持128位、192位、256位）
        if (null == key || (key.length != 16 && key.length != 24 && key.length != 32)) {
            throw new IllegalArgumentException("密钥长度必须是16字节（AES-128）、24字节（AES-192）或32字节（AES-256）");
        }

        // 与加密保持一致：空 IV 使用16字节全零 IV，兼容历史密文
        iv = normalizeIv(iv);
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
        byte[] decrypted = decrypt(encryptedData, Hex.hexToBytes(key),
                StringUtils.isNotBlank(iv) ? Hex.hexToBytes(iv) : null);
        return Hex.bytesToHex(decrypted);
    }

    /**
     * 归一化初始向量
     * <p>
     * 为兼容历史密文，null 或空数组统一视为16字节全零 IV；长度不为16字节则拒绝。
     * </p>
     *
     * @param iv 原始初始向量
     * @return 长度为16字节的初始向量
     * @throws IllegalArgumentException 当 IV 长度不为16字节时
     */
    private static byte[] normalizeIv(byte[] iv) {
        if (null == iv || iv.length == 0) {
            return ZERO_IV.clone();
        }
        if (iv.length != BLOCK_SIZE) {
            throw new IllegalArgumentException("初始向量长度必须是16字节");
        }
        return iv;
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
     * 移除填充数据（宽松模式）
     * <p>
     * 填充不合法时按“无填充”处理并返回原数据，适用于不确定输入是否带填充的场景。
     * 若需要校验密文完整性，请使用 {@link #unpadDataStrict(byte[])}。
     * </p>
     *
     * @param data 待去填充的数据
     * @return 去填充后的数据，填充不合法时返回原数据
     */
    public static byte[] unpadData(byte[] data) {
        if (null == data || data.length == 0) {
            return data;
        }
        int paddingSize = data[data.length - 1] & 0xFF;

        // 验证填充是否有效：填充长度必须在1~16之间且不能超过数据长度，否则视为无填充
        if (paddingSize < 1 || paddingSize > BLOCK_SIZE || paddingSize > data.length) {
            return data;
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
     * 移除填充数据（严格模式）
     * <p>
     * 填充不合法时抛出 {@link IllegalArgumentException}，避免把损坏或篡改的密文当作明文返回。
     * </p>
     *
     * @param data 待去填充的数据
     * @return 去填充后的数据，空输入返回原值
     * @throws IllegalArgumentException 填充不合法时抛出
     */
    public static byte[] unpadDataStrict(byte[] data) {
        if (null == data || data.length == 0) {
            return data;
        }
        int paddingSize = data[data.length - 1] & 0xFF;
        if (paddingSize < 1 || paddingSize > BLOCK_SIZE || paddingSize > data.length) {
            throw new IllegalArgumentException("非法的填充数据: 填充长度越界");
        }
        for (int i = data.length - paddingSize; i < data.length; i++) {
            if (data[i] != paddingSize) {
                throw new IllegalArgumentException("非法的填充数据: 填充字节不一致");
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

}