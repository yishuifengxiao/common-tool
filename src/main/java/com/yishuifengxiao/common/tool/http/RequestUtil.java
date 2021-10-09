/**
 * 
 */
package com.yishuifengxiao.common.tool.http;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * HttpServletRequest处理工具
 * </p>
 * 该工具的主要作用是针对HttpServletRequest各种作用域里的数据进行操作,其主要功能如下：
 * <ol>
 * <li>根据指定的名字从HttpServletRequest的Attribute里获取指定的值，然后移除该属性</li>
 * <li>根据指定的名字从HttpServletRequest的session里获取指定的值，然后移除该属性</li>
 * <li>将session域里面的属性放置到请求域中，并返回对应的值</li>
 * </ol>
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class RequestUtil {

	private HttpServletRequest request;

	/**
	 * 获取一个请求处理工具实例
	 * 
	 * @param request HttpServletRequest
	 * @return 请求处理工具实例
	 */
	public static RequestUtil getInstance(HttpServletRequest request) {
		return new RequestUtil(request);
	}

	/**
	 * 获取HttpServletRequest的请求域里面的一个属性，并清空该属性
	 * 
	 * @param name 属性的名字
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
	 * @param name 属性的名字
	 * @return 属性的值
	 */
	public Object getSessionBeforeDelete(String name) {
		Object value = request.getSession().getAttribute(name);
		if (null != value) {
			request.getSession().removeAttribute(name);
		}
		return value;
	}

	/**
	 * 将session域里面的属性放置到请求域中，并返回对应的值
	 * 
	 * @param name 属性的名字
	 * @return 属性的值
	 */
	public Object session2Attribute(String name) {
		Object value = request.getSession().getAttribute(name);
		if (null != value) {
			request.setAttribute(name, value);
			request.getSession().removeAttribute(name);
		}
		return value;
	}

	/**
	 * 构造函数
	 * 
	 * @param request HttpServletRequest
	 */
	private RequestUtil(HttpServletRequest request) {
		this.request = request;
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
	 * 设置HttpServletRequest
	 * 
	 * @param request HttpServletRequest
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

}
