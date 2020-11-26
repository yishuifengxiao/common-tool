/**
 * 
 */
package com.yishuifengxiao.common.tool.random;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

import org.apache.commons.lang3.RandomUtils;

import com.yishuifengxiao.common.tool.datetime.DateTimeUtil;

/**
 * 随机工具类
 * 
 * @author yishui
 * @date 2018年8月6日
 * @Version 0.0.1
 */
public class RandomUtil {

	/**
	 * 时间格式化器
	 */
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");

	/**
	 * 随机生成一个常见的汉字
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String chinese() throws UnsupportedEncodingException {
		Random random = new Random(System.currentTimeMillis());
		// B0 + 0~39(16~55)
		// 一级汉字所占区
		int highCode = (176 + Math.abs(random.nextInt(39)));
		// A1 + 0~93 每区有94个汉字
		int lowCode = (161 + Math.abs(random.nextInt(93)));
		byte[] b = new byte[2];
		b[0] = (Integer.valueOf(highCode)).byteValue();
		b[1] = (Integer.valueOf(lowCode)).byteValue();
		return new String(b, "GBK");
	}

	/**
	 * 生成指定长度的汉字
	 * 
	 * @param len 汉字长度
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static final String chinese(int len) throws UnsupportedEncodingException {
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
		return LocalDateTime.now(DateTimeUtil.zoneIdOfChina()).format(FORMAT);

	}

	/**
	 * 根据当前时间生成形如 前缀+yyyyMMddhhmmss的字符串
	 * 
	 * @param prefix 增加的前缀
	 * @return 形如 前缀+yyyyMMddhhmmss的字符串
	 */
	public static final synchronized String fromNow(String prefix) {
		return new StringBuffer(prefix).append(LocalDateTime.now(DateTimeUtil.zoneIdOfChina()).format(FORMAT))
				.toString();

	}

	/**
	 * 根据当前时间生成形如yyyyMMddhhmmss100的字符串<br/>
	 * 其中yyyyMMddhhmmss部分为根据当前时间格式化生成,数字部分时100-999之间的随机数
	 * 
	 * @return 形如yyyyMMddhhmmss100的字符串
	 */
	public static final synchronized String fromNowWithNumber() {
		return new StringBuffer(LocalDateTime.now(DateTimeUtil.zoneIdOfChina()).format(FORMAT))
				.append(RandomUtils.nextInt(100, 999)).toString();
	}

}
