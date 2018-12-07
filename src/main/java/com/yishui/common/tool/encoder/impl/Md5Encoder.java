/**
 * 
 */
package com.yishui.common.tool.encoder.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.yishui.common.tool.encoder.Encoder;
import com.yishui.common.tool.encoder.Md5Util;

/**
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public class Md5Encoder implements Encoder {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yishui.common.tool.encoder.Encoder#encode(java.lang.CharSequence)
	 */
	@Override
	public String encode(String rawPassword) {
		Assert.notNull(rawPassword, "待加密的内容不能为空");
		return Md5Util.getMd5(rawPassword);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.yishui.common.tool.encoder.Encoder#matches(java.lang.CharSequence,
	 * java.lang.String)
	 */
	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		if (StringUtils.isNoneBlank(rawPassword, encodedPassword)) {
			return StringUtils.equals(this.encode(rawPassword), encodedPassword);
		}
		return false;
	}

}
