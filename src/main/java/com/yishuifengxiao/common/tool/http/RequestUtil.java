/**
 * 
 */
package com.yishuifengxiao.common.tool.http;

import javax.servlet.http.HttpServletRequest;

/**
 * HttpServletRequest 工具类
 * 
 * @author yishui
 * @Date 2019年3月4日
 * @version 1.0.0
 */
public class RequestUtil {

	private HttpServletRequest request;

	public static RequestUtil getInstance(HttpServletRequest request) {
		return new RequestUtil(request);
	}

	/**
	 * 获取HttpServletRequest的请求域里面的一个属性，并清空该属性
	 * 
	 * @param name
	 *            属性的名字
	 * @return 属性的值
	 */
	public Object getAttributeBeforeDelete(String name) {
		Object value = request.getAttribute(name);
		if (null != value) {
			request.removeAttribute(name);
		}
		return value;
	}

	/**
	 * 获取HttpServletRequest的session域里面的一个属性，并清空该属性
	 * 
	 * @param name
	 *            属性的名字
	 * @return 属性的值
	 */
	public Object getSessionBeforeDelete(String name) {
		Object value = request.getSession().getAttribute(name);
		if (null != value) {
			request.getSession().removeAttribute(name);
		}
		return value;
	}

	private RequestUtil(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
