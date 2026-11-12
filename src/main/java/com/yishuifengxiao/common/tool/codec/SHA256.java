package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA256 加密工具类
 *
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
public class SHA256 {
    /**
     * 从十六进制字符串计算SHA256哈希值
     *
     * @param hex 输入的十六进制字符串
     * @return 计算得到的SHA256哈希值的十六进制表示
     */
    public static String calculateSHA256FromHex(String hex) {
        byte[] data = Hex.hexToBytes(hex);
        if (data == null) {
            throw new IllegalArgumentException("无效的十六进制字符串");
        }
        return calculateSHA256(data);
    }


    /**
     * 计算字符串的 SHA-256 哈希值
     *
     * @param input 输入字符串
     * @return 十六进制格式的 SHA-256 哈希值，输入为null时返回null
     */
    public static String calculateSHA256(String input) {
        if (null == input) {
            return null;
        }
        return calculateSHA256(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 计算字节数组的 SHA-256 哈希值
     *
     * @param input 输入字节数组
     * @return 十六进制格式的 SHA-256 哈希值，输入为null时返回null
     */
    public static String calculateSHA256(byte[] input) {
        if (null == input) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(input);
            byte[] hash = digest.digest();
            return Hex.bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
}
