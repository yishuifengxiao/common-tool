/**
 * 
 */
package com.yishuifengxiao.common.tool.encoder.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.yishuifengxiao.common.tool.encoder.DES;
import com.yishuifengxiao.common.tool.encoder.Encoder;

/**
 * 基于DES加密的加密工具
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class DesEncoder implements Encoder {

	/**
	 * 加解密需要的密钥
	 */
	private String key;

	@Override
	public String encode(String rawPassword) {
		Assert.notNull(rawPassword, "待加密的内容不能为空");
		return DES.encrypt(key, rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		if (StringUtils.isNoneBlank(rawPassword, encodedPassword)) {
			return StringUtils.equals(this.encode(rawPassword), encodedPassword);
		}
		return false;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
