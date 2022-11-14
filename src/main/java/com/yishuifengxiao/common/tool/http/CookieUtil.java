package com.yishuifengxiao.common.tool.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.yishuifengxiao.common.tool.collections.SizeUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * cookie 操作工具
 * </p>
 * 该工具的主要作用是对cookie进行操作，其主要的功能如下：
 * <ol>
 * <li>根据cookie里键的名字获取对应的值</li>
 * <li>根据cookie里键的名字移除对应的值</li>
 * <li>在cookie里增加一个键值对数据</li>
 * </ol>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class CookieUtil {

    /**
     * cookie的默认有效时间
     */
    public static final int COOKIE_HALF_HOUR = 30 * 60;

    /**
     * HttpServletRequest
     */
    private HttpServletRequest request;

    /**
     * HttpServletResponse
     */
    private HttpServletResponse response;

    /**
     * 获得一个cookie操作实例
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @return cookie操作实例
     */
    public static CookieUtil getInstance(HttpServletRequest request, HttpServletResponse response) {
        return new CookieUtil(request, response);
    }

    /**
     * 根据Cookie名称得到Cookie对象，不存在该对象则返回Null
     *
     * @param name cookie的名字
     * @return Cookie对象
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
     * @param name cookie的名字
     * @return Cookie值
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
     * @param name 这个是名称，不是值
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
     * @param name     cookie的名字
     * @param value    cookie的值
     * @param maxValue 过期时间(单位：秒)
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
        cookie.setSecure(true);
        if (maxValue != 0) {
            cookie.setMaxAge(maxValue);
        } else {
            cookie.setMaxAge(COOKIE_HALF_HOUR);
        }
        response.addCookie(cookie);
        try {
            response.flushBuffer();
        } catch (IOException e) {
            log.debug("【易水工具】设置cookie时出现问题，出现问题的原因为 {} ", e.getMessage());
        }
    }

    /**
     * 添加一条新的Cookie，默认30分钟过期时间
     *
     * @param name  cookie的名字
     * @param value cookie的值
     */
    public void setCookie(String name, String value) {
        setCookie(name, value, COOKIE_HALF_HOUR);
    }

    /**
     * 将cookie封装到Map里面
     *
     * @return cookie数据
     */
    public Map<String, Cookie> getCookieMap() {

        Cookie[] cookies = request.getCookies();
        if (SizeUtil.notEmpty(cookies)) {
            Map<String, Cookie> cookieMap = new HashMap<>(cookies.length);
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
        return null;
    }

    /**
     * 获取HttpServletRequest
     *
     * @return HttpServletRequest
     */
    public HttpServletRequest getRequest() {
        return request;
    }

    /**
     * 设置 HttpServletRequest
     *
     * @param request HttpServletRequest
     */
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * 获取HttpServletResponse
     *
     * @return HttpServletResponse
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * 设置HttpServletResponse
     *
     * @param response HttpServletResponse
     */
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    /**
     * 全参构造函数
     *
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     */
    public CookieUtil(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 无参构造函数
     */
    public CookieUtil() {

    }

}