/**
 * 
 */
package com.yishui.common.tool.response;

import java.io.Serializable;
import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;

/**
 * 公有返回信息类<br/>
 * <b>不会序列化null字段</b><br/>
 * 后期考虑增加返回参数校验字段
 * 
 * @author yishui
 * @date 2018年7月26日
 * @Version 0.0.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value = "通用返回实体类", description = "用于所有接口的通用返回数据")
public class Response implements Serializable {
	/**
	 * 200响应码对应的默认信息
	 */
	private final static String OK = "请求成功";
	/**
	 * 400响应码对应的默认信息
	 */
	private final static String BAD_REQUEST = "请求参数有误";
	/**
	 * 401响应码对应的默认信息
	 */
	private final static String UNAUTHORIZED = "请求要求身份验证";
	/**
	 * 403响应码对应的默认信息
	 */
	private final static String FORBIDDEN = "无权访问此资源";
	/**
	 * 404响应码对应的默认信息
	 */
	private final static String NOT_FOUND = "访问的资源路径不存在";
	/**
	 * 500响应码对应的默认信息
	 */
	private final static String INTERNAL_SERVER_ERROR = "请求失败";
	/**
	 * 
	 */
	private static final long serialVersionUID = -1306449295746670286L;
	/**
	 * 返回消息的序列号
	 */
	@ApiModelProperty("请求ID .无论调用接口成功与否,都会返回请求 ID,该序列号全局唯一且随机,用于请求追踪")
	private String id;
	/**
	 * 返回消息中包含的token信息<br/>
	 * 在以后的版本中将不在携带此信息，将只在获取token的接口里返回
	 */
	@ApiModelProperty(value = "返回消息的token", hidden = true)
	@ApiParam(hidden = true)
	@Deprecated
	private String token;

	/**
	 * 请求的响应吗,这里借用HttpStatus作为状态标识
	 * 
	 * @see 具体的响应值的信息可以参见
	 *      https://developer.mozilla.org/en-US/docs/Web/HTTP/Status
	 */
	@ApiModelProperty("请求的响应码,这里借用HttpStatus作为状态标识,具体代码的含义请参见 HttpStatus( https://developer.mozilla.org/en-US/docs/Web/HTTP/Status)")
	private int code;

	/**
	 * 返回的基本信息
	 */
	@ApiModelProperty("返回的基本信息")
	private String msg;
	/**
	 * 返回的数据信息
	 */
	@ApiModelProperty("返回的数据信息")
	private Object data;
	/**
	 * 返回数据的时间
	 */
	@ApiModelProperty("返回数据的时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date date;

	/**
	 * 默认的请求成功时的返回信息(200响应码)
	 * 
	 * @return 请求成功的返回信息
	 */
	public static Response suc() {
		return new Response(HttpStatus.OK.value(), OK);
	}

	/**
	 * 默认的请求成功时的返回信息(200响应码)
	 * 
	 * @param msg
	 *            请求成功时返回的基本信息
	 * @return 请求成功的返回信息
	 */
	public static Response suc(String msg) {
		return new Response(HttpStatus.OK.value(), msg);
	}

	/**
	 * 默认的请求成功时的返回信息(200响应码)
	 * 
	 * @param msg
	 *            请求成功时返回的基本信息
	 * @param data
	 *            请求成功时返回的数据信息
	 * @return 请求成功的返回信息
	 */
	public static Response suc(String msg, Object data) {
		return new Response(HttpStatus.OK.value(), msg, data);
	}

	/**
	 * 默认的请求成功的返回信息(200响应码)
	 * 
	 * @param data
	 *            请求成功时返回的数据信息
	 * @return 请求成功的返回信息
	 */
	public static Response sucResult(Object data) {
		return new Response(HttpStatus.OK.value(), OK, data);
	}

	/**
	 * 默认的参数有误的返回信息(400响应码)
	 * 
	 * @return 参数有误时返回信息
	 */
	public static Response badParam() {
		return new Response(HttpStatus.BAD_REQUEST.value(), BAD_REQUEST);
	}

	/**
	 * 默认的参数有误的返回信息(400响应码)
	 * 
	 * @param msg
	 *            参数有误时返回的基本信息
	 * @return 参数有误时返回信息
	 */
	public static Response badParam(String msg) {
		return new Response(HttpStatus.BAD_REQUEST.value(), msg);
	}

	/**
	 * 默认的参数有误的返回信息(400响应码)
	 * 
	 * @param msg
	 *            参数有误时返回的基本信息
	 * @param data
	 *            BAD_REQUEST
	 * @return 默认的参数有误的返回信息
	 */
	public static Response badParam(String msg, Object data) {
		return new Response(HttpStatus.BAD_REQUEST.value(), msg, data);
	}

	/**
	 * 默认的参数有误的返回信息(400响应码)
	 * 
	 * @param data
	 *            参数有误时返回的数据信息
	 * @return 参数有误时返回信息
	 */
	public static Response badParamResult(Object data) {
		return new Response(HttpStatus.BAD_REQUEST.value(), BAD_REQUEST, data);
	}

	/**
	 * 资源未授权的返回信息(401响应码)
	 * 
	 * @return 资源未授权的返回信息
	 */
	public static Response unAuth() {
		return new Response(HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED);
	}

	/**
	 * 资源未授权的返回信息(401响应码)
	 * 
	 * @param msg
	 *            资源未授权的返回信息的基本信息
	 * @return 资源未授权的返回信息
	 */
	public static Response unAuth(String msg) {
		return new Response(HttpStatus.UNAUTHORIZED.value(), msg);
	}

	/**
	 * 资源未授权的返回信息(401响应码)
	 * 
	 * @param msg
	 *            资源未授权的返回信息的基本信息
	 * @param data
	 *            资源未授权的返回信息的数据信息
	 * @return 资源未授权的返回信息
	 */
	public static Response unAuth(String msg, Object data) {
		return new Response(HttpStatus.UNAUTHORIZED.value(), msg, data);
	}

	/**
	 * 资源未授权的返回信息(401响应码)
	 * 
	 * @param data
	 *            资源未授权的返回信息的数据信息
	 * @return 资源未授权的返回信息
	 */
	public static Response unAuthResult(Object data) {
		return new Response(HttpStatus.UNAUTHORIZED.value(), UNAUTHORIZED, data);
	}

	/**
	 * 资源不可用时的返回信息(403响应码)
	 * 
	 * @return 无权访问访问时的信息
	 */
	public static Response notAllow() {
		return new Response(HttpStatus.FORBIDDEN.value(), FORBIDDEN);
	}

	/**
	 * 资源不可用时的返回信息(403响应码)
	 * 
	 * @param msg
	 *            参数有误时返回的基本信息
	 * @return 无权访问访问时的信息
	 */
	public static Response notAllow(String msg) {
		return new Response(HttpStatus.FORBIDDEN.value(), msg);
	}

	/**
	 * 资源不可用时的返回信息(403响应码)
	 * 
	 * @param msg
	 *            参数有误时返回的基本信息
	 * @param data
	 *            参数有误时返回的数据信息
	 * @return 无权访问访问时的信息
	 */
	public static Response notAllow(String msg, Object data) {
		return new Response(HttpStatus.FORBIDDEN.value(), msg, data);
	}

	/**
	 * 资源不可用时的返回信息(403响应码)
	 * 
	 * @param data
	 *            参数有误时返回的数据信息
	 * @return 无权访问访问时的信息
	 */
	public static Response notAllowResult(Object data) {
		return new Response(HttpStatus.FORBIDDEN.value(), FORBIDDEN, data);
	}

	/**
	 * 默认的路径不存在时的返回信息(404响应码)
	 * 
	 * @return 默认的路径不存在时的返回信息
	 */
	public static Response notFoundt() {
		return new Response(HttpStatus.NOT_FOUND.value(), NOT_FOUND);
	}

	/**
	 * 默认的路径不存在时的返回信息(404响应码)
	 * 
	 * @param msg
	 *            路径不存在时返回的基本信息
	 * @return 默认的路径不存在时的返回信息
	 */
	public static Response notFoundt(String msg) {
		return new Response(HttpStatus.NOT_FOUND.value(), msg);
	}

	/**
	 * 默认的路径不存在时的返回信息(404响应码)
	 * 
	 * @param msg
	 *            路径不存在时返回的基本信息
	 * @param data
	 *            路径不存在时返回的数据信息
	 * @return 默认的路径不存在时的返回信息
	 */
	public static Response notFoundt(String msg, Object data) {
		return new Response(HttpStatus.NOT_FOUND.value(), msg, data);
	}

	/**
	 * 默认的路径不存在时的返回信息(404响应码)
	 * 
	 * @param msg
	 *            路径不存在时返回的数据信息
	 * @return 默认的路径不存在时的返回信息
	 */
	public static Response notFoundt(Object data) {
		return new Response(HttpStatus.NOT_FOUND.value(), NOT_FOUND, data);
	}

	/**
	 * 服务器内部异常时的返回信息(500响应码)
	 * 
	 * @return 服务器内部异常500时的返回信息
	 */
	public static Response error() {
		return new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR);
	}

	/**
	 * 服务器内部异常时的返回信息(500响应码)
	 * 
	 * @param msg
	 *            服务器内部异常时的返回信息的基本信息
	 * @return 服务器内部异常500时的返回信息
	 */
	public static Response error(String msg) {
		return new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
	}

	/**
	 * 服务器内部异常时的返回信息(500响应码)
	 * 
	 * @param msg
	 *            服务器内部异常时的返回信息的基本信息
	 * @param data
	 *            服务器内部异常时的返回信息的数据信息
	 * @return 服务器内部异常500时的返回信息
	 */
	public static Response error(String msg, Object data) {
		return new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, data);
	}

	/**
	 * 服务器内部异常时的返回信息(500响应码)
	 * 
	 * @param data
	 *            服务器内部异常时的返回信息的数据信息
	 * @return 服务器内部异常500时的返回信息
	 */
	public static Response errorResult(Object data) {
		return new Response(HttpStatus.INTERNAL_SERVER_ERROR.value(), INTERNAL_SERVER_ERROR, data);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public Response setId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token
	 *            the token to set
	 */
	public Response setToken(String token) {
		this.token = token;
		return this;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg
	 *            the msg to set
	 */
	public Response setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	/**
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public Response setData(Object data) {
		this.data = data;
		return this;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public Response setCode(int code) {
		this.code = code;
		return this;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public Response setDate(Date date) {
		this.date = date;
		return this;
	}

	/**
	 * @param code
	 * @param msg
	 */
	public Response(int code, String msg) {
		this.id = System.currentTimeMillis() + "";
		this.msg = msg;
		this.code = code;
		this.date = new Date();
	}

	/**
	 * @param code
	 * @param msg
	 * @param data
	 */
	public Response(int code, String msg, Object data) {
		this.id = System.currentTimeMillis() + "";
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.date = new Date();
	}

	/**
	 * @param id
	 * @param code
	 * @param msg
	 * @param data
	 * @param date
	 */
	public Response(String id, int code, String msg, Object data, Date date) {
		this.id = id;
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.date = date;
	}

	public Response(String id, String token, int code, String msg, Object data, Date date) {
		this.id = id;
		this.token = token;
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.date = date;
	}

	public Response() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Response [id=" + id + ", token=" + token + ", code=" + code + ", msg=" + msg + ", data=" + data
				+ ", date=" + date + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + code;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Response other = (Response) obj;
		if (code != other.code)
			return false;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (date == null) {
			if (other.date != null)
				return false;
		} else if (!date.equals(other.date))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (msg == null) {
			if (other.msg != null)
				return false;
		} else if (!msg.equals(other.msg))
			return false;
		return true;
	}

}
