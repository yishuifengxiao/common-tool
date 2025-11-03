package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.HexUtil;

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
        // 将十六进制字符串转换为字节数组，再转换回十六进制字符串
        return HexUtil.bytesToHex(HexUtil.hexToBytes(hex));
    }


    /**
     * 计算字符串的 SHA-256 哈希值
     *
     * @param input 输入字符串
     * @return 十六进制格式的 SHA-256 哈希值
     */
    public static String calculateSHA256(String input) {
        return calculateSHA256(input.getBytes());
    }

    /**
     * 计算字节数组的 SHA-256 哈希值
     *
     * @param input 输入字节数组
     * @return 十六进制格式的 SHA-256 哈希值
     */
    public static String calculateSHA256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input);
            return HexUtil.bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }
}
