/**
 * 
 */
package com.yishuifengxiao.common.tool.encoder.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.yishuifengxiao.common.tool.encoder.Encoder;
import com.yishuifengxiao.common.tool.encoder.Md5;


/**
 * 基于MD5的加密工具
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class Md5Encoder implements Encoder {

	
	@Override
	public String encode(String rawPassword) {
		Assert.notNull(rawPassword, "待加密的内容不能为空");
		return Md5.md5Short(rawPassword);
	}

	
	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		if (StringUtils.isNoneBlank(rawPassword, encodedPassword)) {
			return StringUtils.equals(this.encode(rawPassword), encodedPassword);
		}
		return false;
	}

}
