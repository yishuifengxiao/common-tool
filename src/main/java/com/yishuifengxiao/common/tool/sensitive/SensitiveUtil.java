package com.yishuifengxiao.common.tool.sensitive;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.utils.CertNoUtil;

/**
 * <p>数据脱敏工具类</p>
 * <p>提供对敏感数据的脱敏处理功能，保护用户隐私信息。</p>
 * <p>支持的脱敏类型：</p>
 * <ul>
 * <li>姓名脱敏：张三四 -> 张**</li>
 * <li>身份证号脱敏：保留前3位和后4位</li>
 * <li>手机号脱敏：保留前3位和后4位，中间用****代替</li>
 * <li>密码脱敏：直接替换为*******</li>
 * </ul>
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
		if (StringUtils.isBlank(idCard)) {
			return idCard;
		}
		if (CertNoUtil.isValid(idCard)) {
			return idCard.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
		}
		// 非严格合法的身份证号仍可能是历史数据，长度足够时按"前三后四"兜底脱敏，避免原样返回
		if (idCard.length() >= 8) {
			return idCard.replaceAll("(?<=\\w{3})\\w(?=\\w{4})", "*");
		}
		return idCard;
	}

	/**
	 * 手机号码前三后四脱敏
	 * 
	 * @param mobile 手机号
	 * @return 脱敏后的数据
	 */
	public static String phone(final String mobile) {
		if (StringUtils.isEmpty(mobile)) {
			return mobile;
		}
		// 兼容 +86、空格、连字符等历史格式：先剥离非数字，再按末11位脱敏
		String digits = mobile.replaceAll("\\D", "");
		if (digits.length() > PHONE_LENGTH) {
			digits = digits.substring(digits.length() - PHONE_LENGTH);
		}
		if (digits.length() != PHONE_LENGTH) {
			return mobile;
		}
		return digits.substring(0, 3) + "****" + digits.substring(PHONE_LENGTH - 4);
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
