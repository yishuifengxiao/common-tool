/**
 * 
 */
package com.yishui.common.tool.random;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * 随机生成汉字
 * 
 * @author yishui
 * @date 2018年8月6日
 * @Version 0.0.1
 */
public class RandomChinese {

	/**
	 * 随机生成一个常见的汉字
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getRandomChar() throws UnsupportedEncodingException {
		Random random = new Random();
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
	 * @param len
	 *            汉字长度
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getRandomChar(int len) throws UnsupportedEncodingException {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < len; i++) {
			sb.append(getRandomChar());
		}
		return sb.toString();
	}

}
