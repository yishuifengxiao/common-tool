/**
 * 
 */
package com.yishuifengxiao.common.tool.encoder.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.yishuifengxiao.common.tool.encoder.Encoder;
import com.yishuifengxiao.common.tool.encoder.Md5Util;

/**
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public class Md5Encoder implements Encoder {

	
	@Override
	public String encode(String rawPassword) {
		Assert.notNull(rawPassword, "待加密的内容不能为空");
		return Md5Util.getMd5(rawPassword);
	}

	
	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		if (StringUtils.isNoneBlank(rawPassword, encodedPassword)) {
			return StringUtils.equals(this.encode(rawPassword), encodedPassword);
		}
		return false;
	}

}
