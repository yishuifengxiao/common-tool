package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * <p>CMAC消息认证码工具类</p>
 * <p>基于RFC 4493标准实现的CMAC算法，用于消息认证和完整性校验。</p>
 * <p>特性：</p>
 * <ul>
 * <li>支持AES-CMAC算法</li>
 * <li>遵循RFC 4493标准</li>
 * <li>输出结果为十六进制字符串</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class CMAC {

    /**
     * CMAC算法名称
     */
    private static final String ALGORITHM = "AES";

    /**
     * 常量Rb，用于CMAC子密钥生成
     */
    private static final byte[] Rb = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, (byte) 0x87};

    /**
     * 零向量，用于CMAC计算初始化
     */
    private static final byte[] Zero = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00};


    /**
     * 计算十六进制数据的CMAC值
     *
     * @param keyHex  十六进制格式的密钥字符串
     * @param dataHex 十六进制格式的数据字符串
     * @return CMAC值（十六进制字符串），计算失败返回null
     */
    public static String calculate(String keyHex, String dataHex) {
        byte[] result = calculate(Hex.hexToBytes(keyHex), Hex.hexToBytes(dataHex));
        return bytesToHex(result).toUpperCase();
    }


    /**
     * 计算数据的CMAC值
     *
     * @param key  加密密钥（16字节，对应AES-128）
     * @param data 待计算的数据
     * @return CMAC值（十六进制字符串），计算失败返回null
     */
    public static byte[] calculate(byte[] key, byte[] data) {
        try {
            byte[] subKey1 = generateSubKey(key, true);
            byte[] subKey2 = generateSubKey(key, false);
            int n = (data.length + 15) / 16;
            byte[] lastBlock = new byte[16];
            int paddingLen = 16 - (data.length % 16);
            if (paddingLen == 0) {
                paddingLen = 16;
            }
            if (data.length == 0) {
                System.arraycopy(Zero, 0, lastBlock, 0, 16);
            } else {
                System.arraycopy(data, (n - 1) * 16, lastBlock, 0, Math.min(16, data.length - (n - 1) * 16));
            }
            for (int i = data.length % 16; i < 16; i++) {
                if (i == data.length % 16) {
                    lastBlock[i] = (byte) 0x80;
                } else {
                    lastBlock[i] = 0x00;
                }
            }
            byte[] xorKey = (data.length % 16 == 0 && data.length != 0) ? subKey1 : subKey2;
            for (int i = 0; i < 16; i++) {
                lastBlock[i] ^= xorKey[i];
            }
            byte[] iv = new byte[16];
            byte[] result = lastBlock;
            for (int i = 0; i < n - 1; i++) {
                byte[] block = Arrays.copyOfRange(data, i * 16, (i + 1) * 16);
                for (int j = 0; j < 16; j++) {
                    block[j] ^= iv[j];
                }
                iv = aesEncrypt(key, block);
            }
            for (int j = 0; j < 16; j++) {
                result[j] ^= iv[j];
            }
            result = aesEncrypt(key, result);
            return result;
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("CMAC计算失败，错误: {}", e.getMessage());
            }
            return null;
        }
    }


    /**
     * 生成CMAC子密钥
     *
     * @param key     原始密钥
     * @param isFirst true表示生成第一子密钥K1，false表示生成第二子密钥K2
     * @return 生成的子密钥
     * @throws Exception 子密钥生成失败时抛出
     */
    private static byte[] generateSubKey(byte[] key, boolean isFirst) throws Exception {
        byte[] l = aesEncrypt(key, Zero);
        byte[] subKey = new byte[16];
        System.arraycopy(l, 0, subKey, 0, 16);
        if ((subKey[0] & 0x80) == 0x80) {
            subKey = shiftLeftAndXor(subKey);
        } else {
            subKey = shiftLeft(subKey);
        }
        if (!isFirst) {
            if ((subKey[0] & 0x80) == 0x80) {
                subKey = shiftLeftAndXor(subKey);
            } else {
                subKey = shiftLeft(subKey);
            }
        }
        return subKey;
    }

    /**
     * 字节数组左移一位
     *
     * @param data 待左移的数据
     * @return 左移后的结果
     */
    private static byte[] shiftLeft(byte[] data) {
        byte[] result = new byte[data.length];
        byte carry = 0;
        for (int i = data.length - 1; i >= 0; i--) {
            result[i] = (byte) ((data[i] << 1) | carry);
            carry = (byte) ((data[i] >> 7) & 1);
        }
        return result;
    }

    /**
     * 字节数组左移一位并与Rb进行异或
     *
     * @param data 待处理的数据
     * @return 处理后的结果
     */
    private static byte[] shiftLeftAndXor(byte[] data) {
        byte[] shifted = shiftLeft(data);
        for (int i = 0; i < Rb.length; i++) {
            shifted[i] ^= Rb[i];
        }
        return shifted;
    }

    /**
     * AES加密
     *
     * @param key  加密密钥
     * @param data 待加密的数据
     * @return 加密后的结果
     * @throws Exception 加密失败时抛出
     */
    private static byte[] aesEncrypt(byte[] key, byte[] data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(new byte[16]);
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(data);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 待转换的字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString().toUpperCase();
    }

}