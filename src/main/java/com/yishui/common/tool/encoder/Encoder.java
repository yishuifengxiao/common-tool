/**
 * 
 */
package com.yishui.common.tool.encoder;

/**
 * 自定义加密接口
 * 
 * @author yishui
 * @date 2018年7月27日
 * @Version 0.0.1
 */
public interface Encoder {

	/**
	 * Encode the raw password. Generally, a good encoding algorithm applies a
	 * SHA-1 or greater hash combined with an 8-byte or greater randomly
	 * generated salt.
	 */
	String encode(String rawPassword);

	/**
	 * Verify the encoded password obtained from storage matches the submitted
	 * raw password after it too is encoded. Returns true if the passwords
	 * match, false if they do not. The stored password itself is never decoded.
	 *
	 * @param rawPassword
	 *            the raw password to encode and match
	 * @param encodedPassword
	 *            the encoded password from storage to compare with
	 * @return true if the raw password, after encoding, matches the encoded
	 *         password from storage
	 */
	boolean matches(String rawPassword, String encodedPassword);

}
