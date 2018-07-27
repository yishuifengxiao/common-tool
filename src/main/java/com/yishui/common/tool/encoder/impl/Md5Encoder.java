/**
 * 
 */
package com.yishui.common.tool.encoder.impl;

import com.yishui.common.tool.encoder.Encoder;

/**
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public class Md5Encoder implements Encoder {

	/* (non-Javadoc)
	 * @see com.yishui.common.tool.encoder.Encoder#encode(java.lang.CharSequence)
	 */
	@Override
	public String encode(CharSequence rawPassword) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.yishui.common.tool.encoder.Encoder#matches(java.lang.CharSequence, java.lang.String)
	 */
	@Override
	public boolean matches(CharSequence rawPassword, String encodedPassword) {
		// TODO Auto-generated method stub
		return false;
	}

}
