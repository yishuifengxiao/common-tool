/**
 *
 */
package com.yishuifengxiao.common.tool.random;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.utils.OsUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * <p>
 * 随机工具
 * </p>
 * 该工具主要生成各种类型的随机字符串以便适应于各种场景。该工具的主要功能如下:
 * <ol>
 * <li>生成指定长度的汉字字符串</li>
 * <li>根据当前时间生成yyyyMMddhhmmss的字符串</li>
 * <li>根据当前时间生成yyyyMMddhhmmss+随机数 形式的字符串</li>
 * </ol>
 * <p>
 * <strong>该工具是一个线程安全类的工具。</strong>
 * </p>
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
    public static final synchronized String chinese() {
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
            throw UncheckedException.of(e.getMessage());
        }

    }

    /**
     * 生成指定长度的汉字
     *
     * @param len 汉字长度
     * @return 指定长度的汉字
     */
    public static final synchronized String chineseText(int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            sb.append(chinese());
        }
        return sb.toString();
    }

    /**
     * 根据当前时间生成形如yyyyMMddhhmmss的字符串
     *
     * @return 形如yyyyMMddhhmmss的字符串
     */
    public static final synchronized String fromNow() {
        return LocalDateTime.now(OsUtils.ZONEID_OF_CHINA).format(FORMAT);

    }

    /**
     * 根据当前时间生成形如 前缀+yyyyMMddhhmmss的字符串
     *
     * @param prefix 增加的前缀
     * @return 形如 前缀+yyyyMMddhhmmss的字符串
     */
    public static final synchronized String fromNow(String prefix) {
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
    public static final synchronized String fromNowWithNumber() {
        return new StringBuffer(LocalDateTime.now(OsUtils.ZONEID_OF_CHINA).format(FORMAT)).append(RandomUtils.nextInt(100, 999)).toString();
    }

}
