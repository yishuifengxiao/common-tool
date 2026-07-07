/**
 * <p>通用响应对象工具类</p>
 * <p>提供统一的接口响应格式封装，支持多种HTTP状态码的快捷创建方法</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
package com.yishuifengxiao.common.tool.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yishuifengxiao.common.tool.random.IdWorker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


/**
 * <p>通用响应对象</p>
 * <p>统一系统接口的响应格式，便于各个接口返回的响应形式保持一致。</p>
 * <p>特性：</p>
 * <ul>
 * <li>包含请求ID用于请求追踪</li>
 * <li>使用HttpStatus作为状态码标识</li>
 * <li>包含响应提示信息和响应数据</li>
 * <li>包含响应时间戳</li>
 * <li>提供多种便捷创建方法（成功、失败、参数错误等）</li>
 * </ul>
 * <p>状态码说明：</p>
 * <ul>
 * <li>200 - 请求成功</li>
 * <li>400 - 请求参数有误</li>
 * <li>401 - 请求要求身份验证</li>
 * <li>403 - 无权访问此资源</li>
 * <li>404 - 访问的资源路径不存在</li>
 * <li>500 - 请求处理失败</li>
 * </ul>
 *
 * @param <T> 响应数据的数据类型
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Response<T> implements Serializable {


    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = -1306449295746670286L;
    /**
     * 请求ID,用于请求追踪 .无论调用接口成功与否,都会返回请求 ID,该序列号全局唯一且随机
     */
    @JsonProperty("requestId")
    protected String requestId;

    /**
     * 请求的响应码,这里借用HttpStatus作为状态标识
     * <p>
     * 具体的响应值的信息可以参见 <a href=
     * "https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">https://developer.mozilla
     * .org/en-US/docs/Web/HTTP/Status</a>
     */

    protected Object code;

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
    @JsonProperty("responseTime")
    protected Date responseTime;

    /**
     * 构建一个通用的响应对象
     *
     * @param <T>  响应的数据信息的数据类型
     * @param code 响应码
     * @param msg  响应提示信息
     * @param data 响应数据
     * @return 响应对象
     */
    public static <T> Response<T> of(Object code, String msg, T data) {
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
        return new Response<>(Const.CODE_OK, Const.MSG_OK, data);
    }

    /**
     * 生成一个默认的一个表示成功的响应对象
     *
     * @return 表示成功的响应对象(响应码200)
     */
    public static Response<Object> suc() {
        return new Response<>(Const.CODE_OK, Const.MSG_OK);
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
        return new Response<>(Const.CODE_OK, msg, data);
    }

    /**
     * 生成一个默认的表示参数有误的响应对象(响应码400)
     *
     * @return 表示参数有误的响应对象(响应码400)
     */
    public static Response<Object> badParam() {
        return new Response<>(Const.CODE_BAD_REQUEST, Const.MSG_BAD_REQUEST);
    }

    /**
     * 根据响应提示信息生成一个表示参数有误的响应对象(响应码400)
     *
     * @param msg 响应提示信息
     * @return 表示参数有误的响应对象(响应码400)
     */
    public static Response<Object> badParam(String msg) {
        return new Response<>(Const.CODE_BAD_REQUEST, msg);
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
        return new Response<>(Const.CODE_BAD_REQUEST, msg, data);
    }

    /**
     * 生成一个默认的表示资源未授权的响应对象(401响应码)
     *
     * @return 表示资源未授权的响应对象(401响应码)
     */
    public static Response<Object> unAuth() {
        return new Response<>(Const.CODE_UNAUTHORIZED, Const.MSG_UNAUTHORIZED);
    }

    /**
     * 根据响应提示信息生成一个表示资源未授权的响应对象(401响应码)
     *
     * @param msg 响应提示信息
     * @return 表示资源未授权的响应对象(401响应码)
     */
    public static Response<Object> unAuth(String msg) {
        return new Response<>(Const.CODE_UNAUTHORIZED, msg);
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
        return new Response<>(Const.CODE_UNAUTHORIZED, msg, data);
    }

    /**
     * 生成一个默认的表示资源不可用的响应对象(403响应码)
     *
     * @return 表示资源不可用的响应对象(403响应码)
     */
    public static Response<Object> notAllow() {
        return new Response<>(Const.CODE_FORBIDDEN, Const.MSG_FORBIDDEN);
    }

    /**
     * 根据响应提示信息生成表示资源不可用的响应对象(403响应码)
     *
     * @param msg 响应提示信息
     * @return 表示资源不可用的响应对象(403响应码)
     */
    public static Response<Object> notAllow(String msg) {
        return new Response<>(Const.CODE_FORBIDDEN, msg);
    }

    /**
     * 生成一个默认的表示资源不存在的响应对象(404响应码)
     *
     * @return 表示资源不存在的响应对象(404响应码)
     */
    public static Response<Object> notFound() {
        return new Response<>(Const.CODE_NOT_FOUND, Const.MSG_NOT_FOUND);
    }

    /**
     * 生成一个默认表示请求业务未完成的响应对象(500响应码)
     *
     * @return 表示请求业务未完成的响应对象(500响应码)
     */
    public static Response<Object> error() {
        return new Response<>(Const.CODE_INTERNAL_SERVER_ERROR,
                Const.MSG_INTERNAL_SERVER_ERROR);
    }

    /**
     * 根据响应提示信息生成一个表示服务器内部异常500时的返回信息
     *
     * @param msg 响应提示信息
     * @return 表示服务器内部异常500时的返回信息
     */
    public static Response<Object> error(String msg) {
        return new Response<>(Const.CODE_INTERNAL_SERVER_ERROR, msg);
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
        return new Response<>(Const.CODE_INTERNAL_SERVER_ERROR, msg, data);
    }


    /**
     * 构造函数
     *
     * @param code 响应码
     * @param msg  响应提示信息
     * @param data 响应数据
     */
    public Response(Object code, String msg, T data) {
        this(IdWorker.uuid(), code, msg, data, new Date());
    }

    /**
     * 构造函数
     *
     * @param code 响应码
     * @param msg  响应提示信息
     */
    public Response(Object code, String msg) {
        this(IdWorker.uuid(), code, msg, null, new Date());
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
        public final static String MSG_INTERNAL_SERVER_ERROR = "请求处理失败";
        /**
         * 500响应码
         */
        public final static int CODE_INTERNAL_SERVER_ERROR = 500;

    }


}