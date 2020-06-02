/**
 * 
 */
package com.yishuifengxiao.common.tool.entity;

import java.io.Serializable;
import java.util.Date;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yishuifengxiao.common.tool.random.UID;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 公有返回信息类<br/>
 * <b>不会序列化null字段</b><br/>
 * 后期考虑增加返回参数校验字段
 * 
 * @author yishui
 * @date 2018年7月26日
 * @Version 0.0.1
 */
@ApiModel(value = "通用返回实体类", description = "用于所有接口的通用返回数据")
public class Response<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1306449295746670286L;
	/**
	 * 返回消息的序列号
	 */
	@ApiModelProperty("请求ID,用于请求追踪 .无论调用接口成功与否,都会返回请求 ID,该序列号全局唯一且随机")
	@JsonProperty("requtest-id")
	private String id;

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
	@ApiModelProperty("响应的简单基本信息,一般与响应码的状态对应,对响应结果进行简单地描述")
	private String msg;
	/**
	 * 返回的数据信息
	 */
	@ApiModelProperty("响应的数据信息，在基本基本信息无法满足时会出现此信息,一般情况下无此信息")
	@JsonProperty("data")
	private T data;
	/**
	 * 返回数据的时间
	 */
	@ApiModelProperty("响应的时间")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@JsonProperty("response-time")
	private Date date;

	/**
	 * 构建一个通用的响应对象
	 * 
	 * @param code
	 *            响应码
	 * @param msg
	 *            响应的基本信息
	 * @param data
	 *            响应的数据信息
	 * @return 响应对象
	 */
	public static <T> Response<T> of(int code, String msg, T data) {
		return new Response<>(code, msg, data);
	}

	/**
	 * 默认的请求成功时的返回信息(200响应码)
	 * 
	 * @param data
	 *            请求成功时返回的数据信息
	 * @return 请求成功的返回信息
	 */
	public static <T> Response<T> suc(T data) {
		return new Response<>(HttpStatus.OK.value(), Const.MSG_OK, data);
	}

	/**
	 * 默认的请求成功时的返回信息(200响应码)
	 * 
	 * @return 请求成功的返回信息
	 */
	public static Response<Object> suc() {
		return new Response<Object>(HttpStatus.OK.value(), Const.MSG_OK);
	}

	/**
	 * 默认的请求成功时的返回信息(200响应码)
	 * 
	 * @param msg
	 *            请求成功时返回的基本信息
	 * @return 请求成功的返回信息
	 */
	public static Response<Object> suc(String msg) {
		return new Response<Object>(HttpStatus.OK.value(), msg);
	}

	/**
	 * 默认的参数有误的返回信息(400响应码)
	 * 
	 * @return 参数有误时返回信息
	 */
	public static Response<Object> badParam() {
		return new Response<Object>(HttpStatus.BAD_REQUEST.value(), Const.MSG_BAD_REQUEST);
	}

	/**
	 * 默认的参数有误的返回信息(400响应码)
	 * 
	 * @param msg
	 *            参数有误时返回的基本信息
	 * @return 参数有误时返回信息
	 */
	public static Response<Object> badParam(String msg) {
		return new Response<Object>(HttpStatus.BAD_REQUEST.value(), msg);
	}

	/**
	 * 资源未授权的返回信息(401响应码)
	 * 
	 * @return 资源未授权的返回信息
	 */
	public static Response<Object> unAuth() {
		return new Response<Object>(HttpStatus.UNAUTHORIZED.value(), Const.MSG_UNAUTHORIZED);
	}

	/**
	 * 资源未授权的返回信息(401响应码)
	 * 
	 * @param msg
	 *            资源未授权的返回信息的基本信息
	 * @return 资源未授权的返回信息
	 */
	public static Response<Object> unAuth(String msg) {
		return new Response<Object>(HttpStatus.UNAUTHORIZED.value(), msg);
	}

	/**
	 * 资源不可用时的返回信息(403响应码)
	 * 
	 * @return 无权访问访问时的信息
	 */
	public static Response<Object> notAllow() {
		return new Response<Object>(HttpStatus.FORBIDDEN.value(), Const.MSG_FORBIDDEN);
	}

	/**
	 * 资源不可用时的返回信息(403响应码)
	 * 
	 * @param msg
	 *            参数有误时返回的基本信息
	 * @return 无权访问访问时的信息
	 */
	public static Response<Object> notAllow(String msg) {
		return new Response<Object>(HttpStatus.FORBIDDEN.value(), msg);
	}

	/**
	 * 默认的路径不存在时的返回信息(404响应码)
	 * 
	 * @return 默认的路径不存在时的返回信息
	 */
	public static Response<Object> notFoundt() {
		return new Response<Object>(HttpStatus.NOT_FOUND.value(), Const.MSG_NOT_FOUND);
	}

	/**
	 * 服务器内部异常时的返回信息(500响应码)
	 * 
	 * @return 服务器内部异常500时的返回信息
	 */
	public static Response<Object> error() {
		return new Response<Object>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Const.MSG_INTERNAL_SERVER_ERROR);
	}

	/**
	 * 服务器内部异常时的返回信息(500响应码)
	 * 
	 * @param msg
	 *            服务器内部异常时的返回信息的基本信息
	 * @return 服务器内部异常500时的返回信息
	 */

	public static Response<Object> error(String msg) {
		return new Response<Object>(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
	}

	/**
	 * 默认的路径不存在时的返回信息(404响应码)
	 * 
	 * @param msg
	 *            路径不存在时返回的基本信息
	 * @return 默认的路径不存在时的返回信息
	 */
	public static Response<Object> notFoundt(String msg) {
		return new Response<Object>(HttpStatus.NOT_FOUND.value(), msg);
	}

	/**
	 * 
	 */
	public Response() {

	}

	/**
	 * @param id
	 * @param code
	 * @param msg
	 * @param data
	 * @param date
	 */
	public Response(String id, int code, String msg, T data, Date date) {
		this.id = id;
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.date = date;
	}

	/**
	 * @param code
	 * @param msg
	 * @param data
	 */
	public Response(int code, String msg, T data) {
		this.id = UID.uuid();
		this.code = code;
		this.msg = msg;
		this.data = data;
		this.date = new Date();
	}

	/**
	 * @param code
	 * @param msg
	 */
	public Response(int code, String msg) {
		this.id = UID.uuid();
		this.code = code;
		this.msg = msg;
		this.date = new Date();
	}

	public String getId() {
		return id;
	}

	public Response<T> setId(String id) {
		this.id = id;
		return this;
	}

	public int getCode() {
		return code;
	}

	public Response<T> setCode(int code) {
		this.code = code;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public Response<T> setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public T getData() {
		return data;
	}

	public Response<T> setData(T data) {
		this.data = data;
		return this;
	}

	public Date getDate() {
		return date;
	}

	public Response<T> setDate(Date date) {
		this.date = date;
		return this;
	}

	/**
	 * 通用返回响应类的常用属性信息
	 * 
	 * @author yishui
	 * @date 2018年12月19日
	 * @Version 0.0.1
	 */
	public static class Const {
		/**
		 * 200响应码对应的默认信息
		 */
		public final static String MSG_OK = "请求成功";
		/**
		 * 200响应码
		 */
		public final static int CODE_OK = 200;
		/**
		 * 400响应码对应的默认信息
		 */
		public final static String MSG_BAD_REQUEST = "请求参数有误";
		/**
		 * 400响应码
		 */
		public final static int CODE_BAD_REQUEST = 400;
		/**
		 * 401响应码对应的默认信息
		 */
		public final static String MSG_UNAUTHORIZED = "请求要求身份验证";
		/**
		 * 401响应码
		 */
		public final static int CODE_UNAUTHORIZED = 401;
		/**
		 * 403响应码对应的默认信息
		 */
		public final static String MSG_FORBIDDEN = "无权访问此资源";
		/**
		 * 403响应码
		 */
		public final static int CODE_FORBIDDEN = 403;
		/**
		 * 404响应码对应的默认信息
		 */
		public final static String MSG_NOT_FOUND = "访问的资源路径不存在";
		/**
		 * 404响应码
		 */
		public final static int CODE_NOT_FOUND = 404;
		/**
		 * 500响应码对应的默认信息
		 */
		public final static String MSG_INTERNAL_SERVER_ERROR = "请求失败";
		/**
		 * 500响应码
		 */
		public final static int CODE_INTERNAL_SERVER_ERROR = 500;

	}

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

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Response other = (Response) obj;
		if (code != other.code) {
			return false;
		}
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else if (!data.equals(other.data)) {
			return false;
		}
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		if (msg == null) {
			if (other.msg != null) {
				return false;
			}
		} else if (!msg.equals(other.msg)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Response [id=" + id + ", code=" + code + ", msg=" + msg + ", data=" + data + ", date=" + date + "]";
	}

}
