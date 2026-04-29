package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.io.CloseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * <p>MD5消息摘要工具类</p>
 * <p>基于MD5算法实现数据的消息摘要计算。</p>
 * <p>特性：</p>
 * <ul>
 * <li>支持字符串的MD5计算（32位和16位）</li>
 * <li>支持文件的MD5计算</li>
 * <li>返回小写十六进制字符串</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class Md5 {

    /**
     * 十六进制字符映射表
     */
    private static final String[] HEX_DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f"};

    /**
     * 计算字符串的MD5值（32位小写）
     *
     * @param str 待计算的字符串
     * @return MD5摘要值（32位小写十六进制），计算失败返回null
     */
    public static String md5(String str) {
        str = null == str ? "" : str;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(byteToHexString(bytes[i]));
            }
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("MD5计算失败，数据: {}, 错误: {}", str, e.getMessage());
            }
            return null;
        }
    }

    /**
     * 将单个字节转换为两位十六进制字符串
     *
     * @param b 待转换的字节
     * @return 两位十六进制字符串
     */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return HEX_DIGITS[d1] + HEX_DIGITS[d2];
    }

    /**
     * 计算字符串的MD5值（16位小写）
     *
     * @param str 待计算的字符串
     * @return MD5摘要值（16位小写十六进制），计算失败返回null
     */
    public static String md5Short(String str) {
        return StringUtils.substring(md5(str), 8, 24);
    }

    /**
     * 计算文件的MD5值（32位小写）
     *
     * @param file 待计算的文件
     * @return MD5摘要值（32位小写十六进制），计算失败返回null
     */
    public synchronized static String md5(File file) {
        FileInputStream inputStream = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : md5.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("文件MD5计算失败，文件: {}, 错误: {}", file, e.getMessage());
            }
            return null;
        } finally {
            CloseUtil.close(inputStream);
        }
    }

    /**
     * 计算文件的MD5值（16位小写）
     *
     * @param file 待计算的文件
     * @return MD5摘要值（16位小写十六进制），计算失败返回null
     */
    public static String md5Short(File file) {
        return StringUtils.substring(md5(file), 8, 24);
    }

}