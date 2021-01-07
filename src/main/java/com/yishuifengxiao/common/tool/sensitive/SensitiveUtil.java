package com.yishuifengxiao.common.tool.sensitive;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.utils.CertNoUtil;

/**
 * 脱敏工具
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class SensitiveUtil {

	/**
	 * 手机号的长度
	 */
	private static final int PHONE_LENGTH = 11;

	/**
	 * 姓名第一位脱敏(不考虑复姓，特殊姓氏)
	 * 
	 * @param name 姓名
	 * @return 脱敏后的数据
	 */
	public static String name(String name) {
		if (StringUtils.isEmpty(name) || name.length() < 1) {
			return name;
		}
		return name.replaceAll("(?<=[\\u4e00-\\u9fa5]{1})[\\u4e00-\\u9fa5]", "*");
	}

	/**
	 * 身份证前三后四脱敏
	 * 
	 * @param idCard 身份证号
	 * @return 脱敏后的数据
	 */
	public static String idCard(String idCard) {
		if (!CertNoUtil.isValid(idCard)) {
			return idCard;
		}
		return idCard.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
	}

	/**
	 * 手机号码前三后四脱敏
	 * 
	 * @param mobile 手机号
	 * @return 脱敏后的数据
	 */
	public static String phone(final String mobile) {
		if (StringUtils.isEmpty(mobile) || mobile.length() != PHONE_LENGTH) {
			return mobile;
		}
		return mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
	}

	/**
	 * 将密码替换成星号
	 * 
	 * @param password 密码
	 * @return 脱敏后的数据
	 */
	public static String password(final String password) {
		if (StringUtils.isBlank(password)) {
			return "";
		}
		return "*******";
	}

}
