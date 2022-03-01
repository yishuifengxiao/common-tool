package com.yishuifengxiao.common.tool.text;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

/**
 * 链接处理工具类
 * 
 * @author yishui
 * @version 1.0.0
 */
public final class LinkUtils {

	/**
	 * com.cn域名表达式
	 */
	public final static String COM_CN_DOMAIN = "com.cn";
	/**
	 * gov.cn域名表达式
	 */
	public final static String GOV_CN_DOMAIN = "gov.cn";

	/**
	 * edu.cn域名表达式
	 */
	public final static String EDU_CN_DOMAIN = "edu.cn";

	/**
	 * 从url中提取出来协议和域名
	 * 
	 * @param url
	 * @return 返回协议和域名，形如 http://www.yishuifengxiao.com
	 */
	public static String extractProtocolAndHost(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		Matcher matcher = RegexUtil.PATTERN_PROTOCOL_AND_HOST.matcher(url);
		return matcher.find() ? matcher.group() : null;

	}

	/**
	 * 判断是否符合网络请求的地址形式
	 * 
	 * @param url 需要判断的url
	 * @return 符合要求为true,否则为false
	 */
	public static boolean matchHttpRequest(String url) {
		if (StringUtils.isBlank(url)) {
			return false;
		}
		Matcher matcher = RegexUtil.PATTERN_PROTOCOL_AND_HOST.matcher(url);
		return matcher.find();
	}

	/**
	 * 从url中提取出来完整的域名
	 * 
	 * @param url
	 * @return 返回完整的域名，形如 www.yishuifengxiao.com
	 */
	public static String extractDomain(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		}
		Matcher matcher = RegexUtil.PATTERN_DOMAIN.matcher(url);
		return matcher.find() ? matcher.group() : null;
	}

	/**
	 * 从url里提取出简短域名信息<br/>
	 * 例如www.yishuifengxiao.com 的提取值为 yishuifengxiao
	 * 
	 * @param url url
	 * @return url里提取出简短域名信息
	 */
	public static String keyword(String url) {

		String domain = extractDomain(url);

		if (StringUtils.isBlank(domain)) {
			return null;
		}
		domain = domain.toLowerCase();
		String[] tokens = StringUtils.splitByWholeSeparatorPreserveAllTokens(domain, ".");
		int position = tokens.length - 2;
		if (StringUtils.containsAny(domain, COM_CN_DOMAIN, GOV_CN_DOMAIN, EDU_CN_DOMAIN)) {
			position -= 1;
		}

		return tokens[position];
	}

	/**
	 * 从url中提取出协议
	 * 
	 * @param url 待提取的url
	 * @return url中提取出协议
	 */
	public static String extractProtocol(String url) {
		Matcher matcher = RegexUtil.PATTERN_PROTOCOL_AND_HOST.matcher(url);
		if (matcher.find()) {
			String protocolAndDomian = matcher.group();
			return StringUtils.substringBefore(protocolAndDomian, ":");
		}
		return null;
	}

}
