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

/**
 * 公有返回信息类<br/>
 * <b>不会序列化null字段</b>
 * 
 * @author yishui
 * @date 2018年7月26日
 * @Version 0.0.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value="通用返回实体类",description="用于所有接口的通用返回数据")
public class Response implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1306449295746670286L;
	/**
	 * 返回消息的序列号
	 */
	@ApiModelProperty("返回消息的序列号")
	private String id;
	/**
	 * 返回消息中包含的token信息
	 */
	@ApiModelProperty("返回消息的token")
	private String token;

	/**
	 * 请求的状态
	 */
	@ApiModelProperty("返回消息的请求的状态")
	private HttpStatus code;

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
	 * 默认的请求成功的返回信息
	 * 
	 * @param data
	 *            请求成功时返回的数据
	 * @return 请求成功的返回信息
	 */
	public static Response suc(Object data) {
		return new Response(HttpStatus.OK, "请求成功", data);
	}

	/**
	 * 参数有误
	 * 
	 * @param msg
	 *            参数有误的原因
	 * @return 参数有误时返回信息
	 */
	public static Response badParam(String msg) {
		return new Response(HttpStatus.BAD_REQUEST, msg);
	}
	
	/**
	 * 无权访问
	 * @param msg 无权访问的原因
	 * @return  无权访问访问时的信息
	 */
	public static Response forbidden(String msg){
		return new Response(HttpStatus.FORBIDDEN, msg);
	}
	

	/**
	 * 服务器内部异常时返回的数据
	 * 
	 * @param msg
	 *            异常的原因
	 * @return 服务器内部异常
	 */
	public static Response error(String msg) {
		return new Response(HttpStatus.INTERNAL_SERVER_ERROR, msg);
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
	public void setId(String id) {
		this.id = id;
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
	public void setToken(String token) {
		this.token = token;
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
	public void setMsg(String msg) {
		this.msg = msg;
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
	public void setData(Object data) {
		this.data = data;
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
	public HttpStatus getCode() {
		return code;
	}

	/**
	 * @param code
	 *            the code to set
	 */
	public void setCode(HttpStatus code) {
		this.code = code;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @param code
	 * @param msg
	 */
	public Response(HttpStatus code, String msg) {
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
	public Response(HttpStatus code, String msg, Object data) {
		this.id = System.currentTimeMillis() + "";
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.date = new Date();
	}

	public Response(String id, String token, HttpStatus code, String msg, Object data, Date date) {
		this.id = id;
		this.token = token;
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.date = date;
	}

	public Response() {

	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Response [id=" + id + ", token=" + token + ", code=" + code + ", msg=" + msg + ", data=" + data
				+ ", date=" + date + "]";
	}

}
