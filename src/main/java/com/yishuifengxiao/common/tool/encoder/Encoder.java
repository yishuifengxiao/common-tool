/**
 * 
 */
package com.yishuifengxiao.common.tool.encoder;

/**
 * 自定义加密接口
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Encoder {

	/**
	 * Encode the raw password. Generally, a good encoding algorithm applies a SHA-1
	 * or greater hash combined with an 8-byte or greater randomly generated salt.
	 * 
	 * @param rawPassword 需要加密的内容
	 * @return 加密后的内容
	 */
	String encode(String rawPassword);

	/**
	 * Verify the encoded password obtained from storage matches the submitted raw
	 * password after it too is encoded. Returns true if the passwords match, false
	 * if they do not. The stored password itself is never decoded.
	 *
	 * @param rawPassword     the raw password to encode and match
	 * @param encodedPassword the encoded password from storage to compare with
	 * @return true if the raw password, after encoding, matches the encoded
	 *         password from storage
	 */
	boolean matches(String rawPassword, String encodedPassword);

}
