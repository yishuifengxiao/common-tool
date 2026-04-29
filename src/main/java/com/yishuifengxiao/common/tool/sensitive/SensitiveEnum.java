package com.yishuifengxiao.common.tool.sensitive;

/**
 * <p>敏感信息枚举类型</p>
 * <p>定义支持脱敏的敏感数据类型。</p>
 * <p>支持类型：</p>
 * <ul>
 * <li>ID_CARD - 身份证号</li>
 * <li>PASSWORD - 密码</li>
 * <li>MOBILE_PHONE - 手机号</li>
 * <li>NAME - 真实姓名</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public enum SensitiveEnum {

	/**
	 * 身份证号
	 */
	ID_CARD,
	/**
	 * 密码
	 */
	PASSWORD,
	/**
	 * 手机号
	 */
	MOBILE_PHONE,
	/**
	 * 真实姓名
	 */
	NAME

}
