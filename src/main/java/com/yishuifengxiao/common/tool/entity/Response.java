/**
 *
 */
package com.yishuifengxiao.common.tool.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yishuifengxiao.common.tool.random.IdWorker;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 通用响应
 * </p>
 * <p>
 * 该工具主要是用于对系统的的响应进行一个形式上的统一， 以便各个接口返回的响应的形式保持一致。在初步设计时主要是借助HttpStatus作为状态标识
 * （HttpStatus具体的响应值的信息可以参见
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Status</a>） 。
 * </p>
 * <p>
 * 简单地说，当code的值为200时表示系统成功处理了用户的请求，当code的值为401时表示用户无权访问请求的资源，
 * 当code的值为500时表示系统成功接收到了用户的请求，但是未能按照用户的意图进行业务处理。。。。。
 * </p>
 *
 * <p>
 * 在某些情况下，如果系统内置的响应码不符合已进行的业务的需求但是又需要统一响应格式时可以自定义响应码等信息
 * </p>
 *
 * @param <T> 响应数据的数据类型
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class Response<T> implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1306449295746670286L;
    /**
     * 请求ID,用于请求追踪 .无论调用接口成功与否,都会返回请求 ID,该序列号全局唯一且随机
     */
    @JsonProperty("request-id")
    protected String id;

    /**
     * 请求的响应吗,这里借用HttpStatus作为状态标识
     * <p>
     * 具体的响应值的信息可以参见 <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">https://developer.mozilla
     * .org/en-US/docs/Web/HTTP/Status</a>
     */
    protected int code;

    /**
     * 响应提示信息,一般与响应码的状态对应,对响应结果进行简单地描述
     */
    protected String msg;

    /**
     * 响应数据，在基本基本信息无法满足时会出现此信息,一般情况下无此信息
     */
    @JsonProperty("data")
    protected T data;

    /**
     * 响应时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty("response-time")
    protected Date date;

    /**
     * 构建一个通用的响应对象
     *
     * @param <T>  响应的数据信息的数据类型
     * @param code 响应码
     * @param msg  响应提示信息
     * @param data 响应数据
     * @return 响应对象
     */
    public static <T> Response<T> of(int code, String msg, T data) {
        return new Response<>(code, msg, data);
    }

    /**
     * 根据响应数据生成一个表示成功的响应对象
     *
     * @param <T>  响应的数据信息的数据类型
     * @param data 请求成功时返回的响应的数据信息
     * @return 表示请求成功的响应对象
     */
    public static <T> Response<T> suc(T data) {
        return new Response<>(HttpStatus.OK.value(), Const.MSG_OK, data);
    }

    /**
     * 生成一个默认的一个表示成功的响应对象
     *
     * @return 表示成功的响应对象(响应码200)
     */
    public static Response<Object> suc() {
        return new Response<Object>(HttpStatus.OK.value(), Const.MSG_OK);
    }

    /**
     * 根据响应提示信息生成一个表示成功的响应对象
     *
     * @param msg 响应提示信息
     * @return 表示成功的响应对象(响应码200)
     */
    public static Response<Object> suc(String msg) {
        return new Response<Object>(HttpStatus.OK.value(), msg);
    }

    /**
     * 根据响应提示信息生成一个表示成功的响应对象
     *
     * @param <T>  响应数据的类型
     * @param data 响应数据
     * @return 表示成功的响应对象(响应码200)
     */
    public static <T> Response<T> sucData(T data) {
        return new Response<T>(HttpStatus.OK.value(), Const.MSG_OK, data);
    }

    /**
     * 根据响应提示信息和响应数据生成一个表示成功的响应对象
     *
     * @param <T>  响应数据的数据类型
     * @param msg  响应提示信息
     * @param data 响应数据
     * @return 表示成功的响应对象(响应码200)
     */
    public static <T> Response<T> suc(String msg, T data) {
        return new Response<>(HttpStatus.OK.value(), msg, data);
    }

    /**
     * 生成一个默认的表示参数有误的响应对象(响应码400)
     *
     * @return 表示参数有误的响应对象(响应码400)
     */
    public static Response<Object> badParam() {
        return new Response<Object>(HttpStatus.BAD_REQUEST.value(), Const.MSG_BAD_REQUEST);
    }

    /**
     * 根据响应提示信息生成一个表示参数有误的响应对象(响应码400)
     *
     * @param msg 响应提示信息
     * @return 表示参数有误的响应对象(响应码400)
     */
    public static Response<Object> badParam(String msg) {
        return new Response<Object>(HttpStatus.BAD_REQUEST.value(), msg);
    }

    /**
     * 根据响应提示信息和响应数据生成一个表示参数有误的响应对象(响应码400)
     *
     * @param <T>  响应数据的数据类型
     * @param msg  响应提示信息
     * @param data 响应数据
     * @return 表示参数有误的响应对象(响应码400)
     */
    public static <T> Response<T> badParam(String msg, T data) {
        return new Response<>(HttpStatus.BAD_REQUEST.value(), msg, data);
    }

    /**
     * 生成一个默认的表示资源未授权的响应对象(401响应码)
     *
     * @return 表示资源未授权的响应对象(401响应码)
     */
    public static Response<Object> unAuth() {
        return new Response<Object>(HttpStatus.UNAUTHORIZED.value(), Const.MSG_UNAUTHORIZED);
    }

    /**
     * 根据响应提示信息生成一个表示资源未授权的响应对象(401响应码)
     *
     * @param msg 响应提示信息
     * @return 表示资源未授权的响应对象(401响应码)
     */
    public static Response<Object> unAuth(String msg) {
        return new Response<Object>(HttpStatus.UNAUTHORIZED.value(), msg);
    }

    /**
     * 根据响应提示信息和响应数据生成一个表示资源未授权的响应对象(401响应码)
     *
     * @param <T>  响应数据的类型
     * @param msg  响应提示信息
     * @param data 响应数据
     * @return 表示资源未授权的响应对象(401响应码)
     */
    public static <T> Response<T> unAuth(String msg, T data) {
        return new Response<>(HttpStatus.UNAUTHORIZED.value(), msg, data);
    }

    /**
     * 生成一个默认的表示资源不可用的响应对象(403响应码)
     *
     * @return 表示资源不可用的响应对象(403响应码)
     */
    public static Response<Object> notAllow() {
        return new Response<Object>(HttpStatus.FORBIDDEN.value(), Const.MSG_FORBIDDEN);
    }

    /**
     * 根据响应提示信息生成表示资源不可用的响应对象(403响应码)
     *
     * @param msg 响应提示信息
     * @return 表示资源不可用的响应对象(403响应码)
     */
    public static Response<Object> notAllow(String msg) {
        return new Response<Object>(HttpStatus.FORBIDDEN.value(), msg);
    }

    /**
     * 生成一个默认的表示资源不存在的响应对象(404响应码)
     *
     * @return 表示资源不存在的响应对象(404响应码)
     */
    public static Response<Object> notFoundt() {
        return new Response<Object>(HttpStatus.NOT_FOUND.value(), Const.MSG_NOT_FOUND);
    }

    /**
     * 生成一个默认表示请求业务未完成的响应对象(500响应码)
     *
     * @return 表示请求业务未完成的响应对象(500响应码)
     */
    public static Response<Object> error() {
        return new Response<Object>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Const.MSG_INTERNAL_SERVER_ERROR);
    }

    /**
     * 生成一个默认表示请求业务未完成的响应对象(500响应码)
     *
     * @param <T>  响应数据的类型
     * @param data 响应数据
     * @return 表示请求业务未完成的响应对象(500响应码)
     */
    public static <T> Response<T> errorData(T data) {
        return new Response<T>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Const.MSG_INTERNAL_SERVER_ERROR, data);
    }

    /**
     * 根据响应提示信息生成一个表示服务器内部异常500时的返回信息
     *
     * @param msg 响应提示信息
     * @return 表示服务器内部异常500时的返回信息
     */
    public static Response<Object> error(String msg) {
        return new Response<Object>(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg);
    }

    /**
     * 根据响应数据生成表示服务器内部异常500时的返回信息
     *
     * @param <T>  响应数据的数据类型
     * @param data 响应数据
     * @return 表示服务器内部异常500时的返回信息
     */
    public static <T> Response<T> error(T data) {
        return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), Const.MSG_INTERNAL_SERVER_ERROR, data);
    }

    /**
     * 根据响应提示信息和响应数据生成表示服务器内部异常500时的返回信息
     *
     * @param <T>  响应数据的数据类型
     * @param msg  响应提示信息
     * @param data 响应数据
     * @return 表示服务器内部异常500时的返回信息
     */
    public static <T> Response<T> error(String msg, T data) {
        return new Response<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, data);
    }

    /**
     * 默认的构造函数
     */
    public Response() {

    }

    /**
     * 全参构造函数
     *
     * @param id   请求ID,用于请求追踪 .无论调用接口成功与否,都会返回请求 ID,该序列号全局唯一且随机
     * @param code 响应码
     * @param msg  响应提示信息
     * @param data 响应数据
     * @param date 响应时间
     */
    public Response(String id, int code, String msg, T data, Date date) {
        this.id = id;
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.date = date;
    }

    /**
     * 构造函数
     *
     * @param code 响应码
     * @param msg  响应提示信息
     * @param data 响应数据
     */
    public Response(int code, String msg, T data) {
        this(IdWorker.uuid(), code, msg, data, new Date());
    }

    /**
     * 构造函数
     *
     * @param code 响应码
     * @param msg  响应提示信息
     */
    public Response(int code, String msg) {
        this(IdWorker.uuid(), code, msg, null, new Date());
    }

    /**
     * 获取请求ID
     *
     * @return 请求ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置请求ID
     *
     * @param id 请求ID
     * @return 当前通用响应对象
     */
    public Response<T> setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * 获取响应码
     *
     * @return 当前响应的响应码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置响应码
     *
     * @param code 响应码
     * @return 当前通用响应对象
     */
    public Response<T> setCode(int code) {
        this.code = code;
        return this;
    }

    /**
     * 获取响应提示信息
     *
     * @return 响应提示信息
     */
    public String getMsg() {
        return msg;
    }

    /**
     * 设置响应提示信息
     *
     * @param msg 响应提示信息
     * @return 当前通用响应对象
     */
    public Response<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    /**
     * 设置响应提示信息
     *
     * @param msg 响应提示信息
     * @return 当前通用响应对象
     */
    public Response<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    /**
     * 获取响应数据
     *
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     * @return 当前通用响应对象
     */
    public Response<T> setData(T data) {
        this.data = data;
        return this;
    }

    /**
     * 设置响应数据
     *
     * @param data 响应数据
     * @return 当前通用响应对象
     */
    public Response<T> data(T data) {
        this.data = data;
        return this;
    }

    /**
     * 获取响应的时间
     *
     * @return 响应的时间
     */
    public Date getDate() {
        return date;
    }

    /**
     * 设置响应的时间
     *
     * @param date 响应的时间
     * @return 当前通用响应对象
     */
    public Response<T> setDate(Date date) {
        this.date = date;
        return this;
    }

    /**
     * 通用返回响应类的常用属性信息
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
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
