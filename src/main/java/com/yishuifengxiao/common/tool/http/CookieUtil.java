package com.yishuifengxiao.common.tool.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.collections.EmptyUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * cookie 操作工具类
 * 
 * @author yishui
 * @date 2019年1月26日
 * @version v1.0.0
 */
@Slf4j
public class CookieUtil {

	public static final int COOKIE_MAX_AGE = 7 * 24 * 3600;
	public static final int COOKIE_HALF_HOUR = 30 * 60;

	private HttpServletRequest request;

	private HttpServletResponse response;

	/**
	 * 获得一个cookie操作类的示例
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static CookieUtil getInstance(HttpServletRequest request, HttpServletResponse response) {
		return new CookieUtil(request, response);
	}

	/**
	 * 根据Cookie名称得到Cookie对象，不存在该对象则返回Null
	 *
	 * @param request
	 * @param name
	 * @return
	 */
	public Cookie getCookie(String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null || cookies.length < 1) {
			return null;
		}
		Cookie cookie = null;
		for (Cookie c : cookies) {
			if (name.equals(c.getName())) {
				cookie = c;
				break;
			}
		}
		return cookie;
	}

	/**
	 * 根据Cookie名称直接得到Cookie值
	 *
	 * @param request
	 * @param name
	 * @return
	 */
	public String getCookieValue(String name) {
		Cookie cookie = getCookie(name);
		if (cookie != null) {
			return cookie.getValue();
		}
		return null;
	}

	/**
	 * 移除cookie
	 * 
	 * @param request
	 * @param response
	 * @param name     这个是名称，不是值
	 */
	public void removeCookie(String name) {
		if (null == name) {
			return;
		}
		Cookie cookie = getCookie(name);
		if (null != cookie) {
			cookie.setPath("/");
			cookie.setValue("");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}

	/**
	 * 添加一条新的Cookie，可以指定过期时间(单位：秒)
	 *
	 * @param response
	 * @param name
	 * @param value
	 * @param maxValue
	 */
	public void setCookie(String name, String value, int maxValue) {
		if (StringUtils.isBlank(name)) {
			return;
		}
		if (null == value) {
			value = "";
		}
		Cookie cookie = new Cookie(name, value);
		cookie.setPath("/");
		if (maxValue != 0) {
			cookie.setMaxAge(maxValue);
		} else {
			cookie.setMaxAge(COOKIE_HALF_HOUR);
		}
		response.addCookie(cookie);
		try {
			response.flushBuffer();
		} catch (IOException e) {
			log.debug("设置cookie时出现问题，出现问题的原因为 {} ", e.getMessage());
		}
	}

	/**
	 * 添加一条新的Cookie，默认30分钟过期时间
	 *
	 * @param response
	 * @param name
	 * @param value
	 */
	public void setCookie(String name, String value) {
		setCookie(name, value, COOKIE_HALF_HOUR);
	}

	/**
	 * 将cookie封装到Map里面
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, Cookie> getCookieMap() {

		Cookie[] cookies = request.getCookies();
		if (EmptyUtil.notEmpty(cookies)) {
			Map<String, Cookie> cookieMap = new HashMap<>(cookies.length);
			for (Cookie cookie : cookies) {
				cookieMap.put(cookie.getName(), cookie);
			}
		}
		return null;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	public CookieUtil(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	public CookieUtil() {

	}

}