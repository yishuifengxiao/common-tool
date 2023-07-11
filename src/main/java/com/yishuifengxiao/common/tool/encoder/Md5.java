package com.yishuifengxiao.common.tool.encoder;

import com.yishuifengxiao.common.tool.io.CloseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * <p>
 * Md5加密工具
 * </p>
 * 基于MD5算法实现的加密工具
 *
 * <p>
 * <strong>该工具是一个线程安全类的工具</strong>
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class Md5 {
    private static final String[] HEX_DIGITS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
            "e", "f"};

    /**
     * 对字符串md5加密(小写+字母)
     *
     * @param str 传入要加密的字符串
     * @return MD5加密后的字符串(32位)
     */
    public static String md5(String str) {
        str = null == str ? "" : str;
        try {
            // 创建MessageDigest对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] bytes = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(byteToHexString(bytes[i]));
            }
            // 将字节数组转换为16进制位,然后统一返回大写形式的字符串摘要
            return sb.toString().toLowerCase();
        } catch (Exception e) {
            log.warn("【易水工具】使用md5加密字符串{} 时出现问题，出现问题的原因为 {}", str, e.getMessage());
            return null;
        }
    }

    /**
     * 将1个字节（1 byte = 8 bit）转为 2个十六进制位
     * 1个16进制位 = 4个二进制位 （即4 bit）
     * 转换思路：最简单的办法就是先将byte转为10进制的int类型，然后将十进制数转十六进制
     */
    private static String byteToHexString(byte b) {
        // byte类型赋值给int变量时，java会自动将byte类型转int类型，从低位类型到高位类型自动转换
        int n = b;

        // 将十进制数转十六进制
        if (n < 0) {
            n += 256;
        }
        int d1 = n / 16;
        int d2 = n % 16;

        // d1和d2通过访问数组变量的方式转成16进制字符串；比如 d1 为12 ，那么就转为"c"; 因为int类型不会有a,b,c,d,e,f等表示16进制的字符
        return HEX_DIGITS[d1] + HEX_DIGITS[d2];
    }

    /**
     * 对字符串md5加密(小写+字母)
     *
     * @param str 传入要加密的字符串
     * @return MD5加密后的字符串(16位)
     */
    public static String md5Short(String str) {
        return StringUtils.substring(md5(str), 8, 24);
    }

    /**
     * <p>
     * 计算一个文件的MD5值
     * </p>
     * <strong>线程安全</strong>
     *
     * @param file 待计算的文件
     * @return 文件的MD5值(32位小写)
     */
    public synchronized static String md5(File file) {
        FileInputStream inputStream = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                md5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(md5.digest()));
        } catch (Exception e) {
            log.info("计算文件{}的md5值时出现问题{}", file, e.getMessage());
            return null;
        } finally {
            CloseUtil.close(inputStream);
        }
    }

    /**
     * <p>
     * 计算一个文件的MD5值(16位)
     * </p>
     * <strong>线程安全</strong>
     *
     * @param file 待计算的文件
     * @return MD5加密后的字符串(16位)
     */
    public static String md5Short(File file) {
        return StringUtils.substring(md5(file), 8, 24);
    }

}
