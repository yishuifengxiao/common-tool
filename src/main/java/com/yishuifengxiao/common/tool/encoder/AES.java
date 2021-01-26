package com.yishuifengxiao.common.tool.encoder;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * AES加密工具
 * </p>
 * 基于DES加解密实现的加密工具，该工具可以进行可逆加密，加密时的秘钥很重要，一定要自己改秘钥，打死也不要告诉其他人。
 * <strong>该工具是一个线程安全类的工具。</strong>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public  class AES {

	/**
	 * 密钥，是加密解密的凭据，长度为8的倍数
	 */
	private static final String PASSWORD_CRYPT_KEY = "yishui@#";

	/**
	 * 用默认的密钥进行数据加密
	 * 
	 * @param data 需要加密的数据
	 * @return 加密后的数据。若待加密的数据为空或加密出现问题时返回为null
	 */
	public static final String encrypt(String data) {
		return encrypt(null, data);
	}

	/**
	 * 使用指定的密钥对数据进行加载
	 * 
	 * @param key  加密密钥
	 * @param data 需要加密的数据
	 * @return 加密后的数据。若待加密的数据为空或加密出现问题时返回为null
	 */
	public static synchronized final String encrypt(String key, String data) {
		if (StringUtils.isBlank(key)) {
			key = PASSWORD_CRYPT_KEY;
		}
		if (StringUtils.isBlank(data)) {
			return null;
		}
		try {
			// 1.构造密钥生成器，指定为AES算法,不区分大小写
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			// 2.根据ecnodeRules规则初始化密钥生成器,然后生成一个128位的随机源,根据传入的字节数组
			keygen.init(128, new SecureRandom(key.getBytes("utf-8")));
			// 3.产生原始对称密钥，获得原始对称密钥的字节数组，然后根据字节数组生成AES密钥
			SecretKey aesKey = new SecretKeySpec(keygen.generateKey().getEncoded(), "AES");
			// 4.根据指定算法AES自成密码器
			Cipher cipher = Cipher.getInstance("AES");
			// 5.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.ENCRYPT_MODE, aesKey);
			// 8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码,然后根据密码器的初始化方式--加密：将数据加密，最后将加密后的数据转换为字符串
			return new String(Base64.getEncoder().encode(cipher.doFinal(data.getBytes("utf-8"))), "utf-8");
		} catch (Exception e) {
			log.info("【易水工具】使用密钥加密数据 {} 时出现问题，出现问题的原因为 {}", data, e.getMessage());
		}
		// 如果有错就返加nulll
		return null;
	}

	/**
	 * 使用默认的密钥对数据进行解密
	 * 
	 * @param data 待解密的数据
	 * @return 解密后的数据。若待解密的数据为空或解密出现问题时返回为null
	 */
	public static final String decrypt(String data) {
		return decrypt(null, data);
	}

	/**
	 * 使用指定的密钥对数据进行解密
	 * 
	 * @param key  解密密钥
	 * @param data 待解密的数据
	 * @return 解密后的数据。若待解密的数据为空或解密出现问题时返回为null
	 */
	public static synchronized final String decrypt(String key, String data) {
		if (StringUtils.isBlank(key)) {
			key = PASSWORD_CRYPT_KEY;
		}
		if (StringUtils.isBlank(data)) {
			return null;
		}
		try {
			// 1.构造密钥生成器，指定为AES算法,不区分大小写
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
			// 2.根据ecnodeRules规则初始化密钥生成器，然后生成一个128位的随机源,根据传入的字节数组
			keygen.init(128, new SecureRandom(key.getBytes("utf-8")));
			// 3.产生原始对称密钥，然后获得原始对称密钥的字节数组，接下来根据字节数组生成AES密钥
			SecretKey aesKey = new SecretKeySpec(keygen.generateKey().getEncoded(), "AES");
			// 4.根据指定算法AES自成密码器
			Cipher cipher = Cipher.getInstance("AES");
			// 5.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.DECRYPT_MODE, aesKey);
			// 6.将加密并编码后的内容解码成字节数组
			return new String(cipher.doFinal(Base64.getDecoder().decode(data.getBytes("utf-8"))), "utf-8");
		} catch (Exception e) {
			log.info("【易水工具】使用密钥解密数据 {} 时出现问题，出现问题的原因为 {}", data, e.getMessage());
		}

		// 如果有错就返加nulll
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(encrypt("sdfs","sdfs"));
	}
}
