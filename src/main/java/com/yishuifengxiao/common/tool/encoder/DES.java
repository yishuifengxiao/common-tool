package com.yishuifengxiao.common.tool.encoder;

import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 字符串加密解密工具，可逆加密，秘钥很重要，一定要自己改秘钥，打死也不要告诉其他人
 * 
 * @author 夏增明
 * @version 1.0 Date:2013年2月8日15:45:56
 */
public class DES {

	/**
	 * 密钥，是加密解密的凭据，长度为8的倍数
	 */
	private static final String PASSWORD_CRYPT_KEY = "yishui@#";
	private final static String DES = "DES";

	/**
	 * 对输入的数据进行加密
	 * 
	 * @param key
	 *            加密的密钥，如果为空则以默认密码进行加密，如果密钥长度不是8的倍数，系统会自动补0
	 * @param data
	 *            需要加密的数据
	 * @return 加密后的数据,null表示加密失败
	 */
	public final static String encrypt(String key, String data) {
		Assert.notNull(data, "需要加密的数据不能为空");
		try {
			return byte2hex(encrypt(data.getBytes("utf-8"), keyValidate(key).getBytes("utf-8")));
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 用默认的密码进行数据加密
	 * 
	 * @param data
	 *            需要加密的数据
	 * @return 加密后的数据,null表示加密失败
	 */
	public final static String encrypt(String data) {
		return encrypt(null, data);
	}

	/**
	 * 对输入的数据进行解密
	 * 
	 * @param key
	 *            解密的密钥，如果为空则以默认密码进行加密，如果密钥长度不是8的倍数，系统会自动补0
	 * @param data
	 *            需要解密的数据
	 * @return 解密后的数据,null表示解密失败
	 */
	public final static String decrypt(String key, String data) {
		Assert.notNull(data, "需要解密的数据不能为空");
		try {
			return new String(decrypt(hex2byte(data.getBytes("utf-8")), keyValidate(key).getBytes("utf-8")));
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * 用默认的密码进行数据解密
	 * 
	 * @param data
	 *            需要解密的数据
	 * @return 解密后的数据,null表示解密失败
	 */
	public final static String decrypt(String data) {
		return decrypt(null, data);
	}

	/**
	 * 加密
	 * 
	 * @param src
	 *            数据源
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * @return 返回加密后的数据
	 * @throws Exception
	 */
	private static byte[] encrypt(byte[] src, byte[] key) throws Exception {

		// DES算法要求有一个可信任的随机数源
		SecureRandom sr = new SecureRandom();

		// 从原始密匙数据创建DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);

		// 创建一个密匙工厂，然后用它把DESKeySpec转换成
		// 一个SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成加密操作
		Cipher cipher = Cipher.getInstance(DES);

		// 用密匙初始化Cipher对象
		cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

		// 现在，获取数据并加密
		// 正式执行加密操作
		return cipher.doFinal(src);

	}

	/**
	 * 解密
	 * 
	 * @param src
	 *            数据源
	 * @param key
	 *            密钥，长度必须是8的倍数
	 * @return 返回解密后的原始数据
	 * @throws Exception
	 */
	private static byte[] decrypt(byte[] src, byte[] key) throws Exception {

		// DES算法要求有一个可信任的随机数源
		SecureRandom sr = new SecureRandom();
		// 从原始密匙数据创建一个DESKeySpec对象
		DESKeySpec dks = new DESKeySpec(key);

		// 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
		// 一个SecretKey对象
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);

		SecretKey securekey = keyFactory.generateSecret(dks);

		// Cipher对象实际完成解密操作
		Cipher cipher = Cipher.getInstance(DES);

		// 用密匙初始化Cipher对象
		cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

		// 现在，获取数据并解密
		// 正式执行解密操作
		return cipher.doFinal(src);

	}

	/**
	 * 对key进行校验
	 * 
	 * @param key
	 * @return
	 */
	private static String keyValidate(String key) {
		if (StringUtils.isBlank(key)) {
			key = PASSWORD_CRYPT_KEY;
		} else {
			if (key.length() % 8 != 0) {
				for (int i = 0; i < key.length() % 8; i++) {
					key += "0";
				}
			}
		}
		return key;
	}

	/**
	 * 二行制转字符串
	 * 
	 * @param b
	 * @return
	 */
	private static String byte2hex(byte[] b) {

		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n++) {
			stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (stmp.length() == 1) {
				hs = hs + "0" + stmp;
			} else {
				hs = hs + stmp;
			}
		}
		return hs.toUpperCase();
	}

	private static byte[] hex2byte(byte[] b) {

		if ((b.length % 2) != 0) {
			throw new IllegalArgumentException("长度不是偶数");
		}
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	// 测试用例，不需要传递任何参数，直接执行即可。
	public static void main(String[] args) {
		String basestr = "zczc123456";
		String str1 = encrypt(basestr);

		System.out.println("原始值: " + basestr);
		System.out.println("加密后: " + str1);
		System.out.println("解密后: " + decrypt("sdd",str1.toLowerCase()));
		System.out.println("为空时 is : " + decrypt(encrypt("")));
	}

}
