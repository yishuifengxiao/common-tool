/**
 *
 */
package com.yishuifengxiao.common.tool.random;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.utils.OsUtils;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>随机工具类</p>
 * <p>提供各种类型的随机字符串生成功能，适应不同业务场景需求。</p>
 * <p>特性：</p>
 * <ul>
 * <li>生成指定长度的汉字字符串</li>
 * <li>根据当前时间生成yyyyMMddhhmmss格式字符串</li>
 * <li>生成带前缀的时间戳字符串</li>
 * <li>生成带随机数的时间戳字符串</li>
 * <li>生成指定字节数的随机十六进制字符串</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class RandomUtil {

    /**
     * 时间格式化器
     */
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");

    /**
     * 随机生成一个常见的汉字
     *
     * @return 一个常见的汉字
     */
    public static final synchronized String generateChineseChar() {
        try {
            Random random = new Random(IdWorker.snowflakeId() + System.currentTimeMillis());
            // B0 + 0~39(16~55)
            // 一级汉字所占区
            int highCode = (176 + Math.abs(random.nextInt(39)));
            // A1 + 0~93 每区有94个汉字
            int lowCode = (161 + Math.abs(random.nextInt(93)));
            byte[] b = new byte[2];
            b[0] = (Integer.valueOf(highCode)).byteValue();
            b[1] = (Integer.valueOf(lowCode)).byteValue();
            return new String(b, "GBK");
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedException(e);
        }

    }

    /**
     * 生成指定长度的汉字
     *
     * @param len 汉字长度
     * @return 指定长度的汉字
     */
    public static final synchronized String generateChineseText(int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            sb.append(generateChineseChar());
        }
        return sb.toString();
    }

    /**
     * 根据当前时间生成形如yyyyMMddhhmmss的字符串
     *
     * @return 形如yyyyMMddhhmmss的字符串
     */
    public static final synchronized String generateTimestamp() {
        return LocalDateTime.now(OsUtils.ZONEID_OF_CHINA).format(FORMAT);

    }

    /**
     * 根据当前时间生成形如 前缀+yyyyMMddhhmmss的字符串
     *
     * @param prefix 增加的前缀
     * @return 形如 前缀+yyyyMMddhhmmss的字符串
     */
    public static final synchronized String generateTimestampWithPrefix(String prefix) {
        return new StringBuffer(prefix).append(LocalDateTime.now(OsUtils.ZONEID_OF_CHINA).format(FORMAT)).toString();

    }

    /**
     * <p>
     * 根据当前时间生成形如yyyyMMddhhmmss100的字符串
     * </p>
     * 其中yyyyMMddhhmmss部分为根据当前时间格式化生成,数字部分时100-999之间的随机数
     *
     * @return 形如yyyyMMddhhmmss100的字符串
     */
    public static final synchronized String generateTimestampWithRandom() {
        return new StringBuffer(LocalDateTime.now(OsUtils.ZONEID_OF_CHINA).format(FORMAT)).append(ThreadLocalRandom.current().nextInt(100, 999)).toString();
    }

    /**
     * 生成指定字节数的随机 hex 字符串
     *
     * @param numBytes 需要的随机字节数（结果字符串长度为 numBytes * 2）
     * @return 十六进制随机字符串（小写字母）
     * @throws IllegalArgumentException 如果 numBytes &lt;= 0
     */
    public static String generateRandomHexString(int numBytes) {
        if (numBytes <= 0) {
            throw new IllegalArgumentException("numBytes must be positive");
        }
        byte[] randomBytes = new byte[numBytes];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);

        StringBuilder hexBuilder = new StringBuilder(numBytes * 2);
        for (byte b : randomBytes) {
            // 将每个字节转换为两位十六进制数
            hexBuilder.append(String.format("%02x", b));
        }
        return hexBuilder.toString();
    }
}
