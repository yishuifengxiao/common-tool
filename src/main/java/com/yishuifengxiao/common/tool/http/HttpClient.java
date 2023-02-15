package com.yishuifengxiao.common.tool.http;

import com.yishuifengxiao.common.tool.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>
 * 基于jsoup实现的简单的http请求客户端
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class HttpClient {

    /**
     * Content-Type:application/x-www-form-urlencoded; charset=UTF-8
     */
    public final static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; charset=UTF-8";

    /**
     * Content-Type:application/json;charset=UTF-8
     */
    public final static String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";


    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 是否使用自动User-Agent
     */
    private boolean autoUserAgent = true;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * Referer
     */
    private String referrer;

    /**
     * 请求头
     */
    private Map<String, Object> headers = new HashMap<>();

    /**
     * 超时连接或读取之前的毫秒数（千分之一秒）
     */
    private Integer timeout;

    /**
     * 请求的url
     */
    private String url;

    /**
     * 请求的类型
     */
    private String method;
    /**
     * cookies
     */
    private Map<String, Object> cookies;
    /**
     * 请求体键值对与 requestBody 互斥
     */
    private Map<String, Object> data;

    /**
     * 请求体，与data  互斥
     */
    private String requestBody;


    /**
     * 获取http客户端（单例模式）
     *
     * @return http客户端
     */
    public final static HttpClient instance() {
        return new HttpClient();
    }


    /**
     * 设置 userAgent
     *
     * @param userAgent 设置 userAgent
     * @return
     */
    public HttpClient userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * 设置 contentType
     *
     * @param contentType contentType
     * @return
     */
    public HttpClient contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置 referrer
     *
     * @param referrer referrer
     * @return
     */
    public HttpClient referrer(String referrer) {
        this.referrer = referrer;
        return this;
    }

    /**
     * 增加一个请求头
     *
     * @param name  请求头名字
     * @param value 请求头的值
     * @return
     */
    public HttpClient addHeader(String name, Object value) {
        if (StringUtils.isNoneBlank(name) && null != value) {
            this.headers.put(name, value.toString());
        }
        return this;
    }

    /**
     * 批量增加请求头
     *
     * @param headers 请求头
     * @return
     */
    public HttpClient addHeaders(Map<String, Object> headers) {
        if (null == headers) {
            return this;
        }
        headers.forEach((name, value) -> this.addHeader(name, value));
        return this;
    }

    /**
     * 设置请求头
     *
     * @param headers 请求头
     * @return
     */
    public HttpClient setHeaders(Map<String, Object> headers) {
        if (null == headers) {
            this.headers = new HashMap<>(0);
            return this;
        }
        this.headers.clear();
        this.addHeaders(headers);
        return this;
    }

    /**
     * <p>设置超时时间</p>
     * <p>设置总请求超时持续时间。如果发生超时，将抛出java.net.SocketTimeoutException。
     * 默认超时为30秒（30000毫秒）。超时为零被视为无限超时。
     * 请注意，此超时指定了连接时间和读取完整响应时间的组合最大持续时间。</p>
     *
     * @param timeout 连接或读取超时前的毫秒数（千分之一秒）。
     * @return
     */
    public HttpClient timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置请求的url
     *
     * @param url
     * @return
     */
    public HttpClient url(String url) {
        this.url = url;
        return this;
    }

    /**
     * <p>设置请求的类型</p>
     * <p>默认为get请求</p>
     *
     * @param method 请求的类型
     * @return
     */
    public HttpClient method(String method) {
        this.method = method;
        return this;
    }

    /**
     * 设置是否使用是否使用自动User-Agent
     *
     * @param autoUserAgent 是否使用是否使用自动User-Agent,默认值为true
     * @return
     */
    public HttpClient autoUserAgent(boolean autoUserAgent) {
        this.autoUserAgent = autoUserAgent;
        return this;
    }

    /**
     * 设置请求体
     *
     * @param requestBody 请求体(与data  互斥)
     * @return
     */
    public HttpClient requestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * 设置请求体键值对
     *
     * @param data 请求体键值对与 requestBody 互斥
     * @return
     */
    public HttpClient data(Map<String, Object> data) {
        this.data = data;
        return this;
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送请求
     *
     * @return
     */
    public HttpClient form() {
        this.contentType = CONTENT_TYPE_FORM;
        return this;
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送请求
     *
     * @return
     */
    public HttpClient json() {
        this.contentType = CONTENT_TYPE_JSON;
        return this;
    }

    /**
     * 设置 cookies
     *
     * @param cookies
     * @return
     */
    public HttpClient cookies(Map<String, Object> cookies) {
        this.cookies = cookies;
        return this;
    }


    /**
     * 根据url发送get请求，并把响应体转换成文本
     *
     * @param url url
     * @return 请求的响应体文本
     */
    public String get(String url) {
        this.url = url;
        this.method = "get";
        return this.executeAsString();
    }


    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url     url
     * @param headers 请求头
     * @param data    请求数据
     * @return 请求的响应体的响应文本
     */
    public String postForm(String url, Map<String, Object> headers, Map<String, Object> data) {
        return executeAsString(url, "post", CONTENT_TYPE_FORM, headers, data);
    }


    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应体的响应文本
     */
    public String postForm(String url, Map<String, Object> data) {

        return executeAsString(url, "post", CONTENT_TYPE_FORM, null, data);
    }


    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url     url
     * @param headers 请求头
     * @param data    请求数据
     * @return 请求的响应体的响应文本
     */
    public String postJson(String url, Map<String, Object> headers, Map<String, Object> data) {
        return executeAsString(url, "post", CONTENT_TYPE_JSON, headers, data);
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应体的响应文本
     */
    public String postJson(String url, Map<String, Object> data) {
        return executeAsString(url, "post", CONTENT_TYPE_JSON, null, data);
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url         url
     * @param requestBody 请求体
     * @return 请求的响应体的响应文本
     */
    public String postJson(String url, String requestBody) {
        return executeAsString(url, "post", CONTENT_TYPE_JSON, null, requestBody);
    }

    /**
     * 发送POST请求并将响应体转换为文本
     *
     * @param url         url
     * @param method      请求方法
     * @param contentType Content-Type
     * @param headers     请求头
     * @param data        请求体
     * @return 响应
     */
    public Connection.Response execute(String url, String method, String contentType, Map<String, Object> headers, Map<String, Object> data) {
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.data = data;
        this.requestBody = null;
        return this.execute();
    }

    /**
     * 发送POST请求并将响应体转换为文本
     *
     * @param url         url
     * @param method      请求方法
     * @param contentType Content-Type
     * @param headers     请求头
     * @param data        请求体
     * @return 响应
     */
    public String executeAsString(String url, String method, String contentType, Map<String, Object> headers, Map<String, Object> data) {
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.data = data;
        this.requestBody = null;
        Connection.Response response = this.execute();
        return null == response ? null : response.body();
    }

    /**
     * 发送POST请求并将响应体转换为文本
     *
     * @param url         url
     * @param method      请求方法
     * @param contentType Content-Type
     * @param headers     请求头
     * @param requestBody 请求体
     * @return 响应
     */
    public Connection.Response execute(String url, String method, String contentType, Map<String, Object> headers, String requestBody) {
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.data = null;
        this.requestBody = requestBody;
        return this.execute();
    }

    /**
     * 发送POST请求并将响应体转换为文本
     *
     * @param url         url
     * @param method      请求方法
     * @param contentType Content-Type
     * @param headers     请求头
     * @param requestBody 请求体
     * @return 响应
     */
    public String executeAsString(String url, String method, String contentType, Map<String, Object> headers, String requestBody) {
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.data = null;
        this.requestBody = requestBody;
        Connection.Response response = this.execute();
        return null == response ? null : response.body();
    }

    /**
     * 执行请求
     *
     * @return 响应
     */
    public Connection.Response execute() {
        Connection.Response response = null;
        try {
            response = this.connection().execute();
        } catch (Throwable e) {
            log.warn("======> 请求 {} 时出现问题 {}", this.url, e);
        }
        return response;
    }

    /**
     * 执行请求,并将响应体转换为文本
     *
     * @return 文本响应体
     */
    public String executeAsString() {
        Connection.Response response = this.execute();
        return null == response ? null : response.body();
    }

    /**
     * 建立一个连接
     *
     * @return 连接
     */
    private Connection connection() {
        String userAgent = this.userAgent;
        if (this.autoUserAgent && StringUtils.isBlank(this.userAgent)) {
            userAgent = UserAgent.USER_AGENT_EDGE_VERSION_110_0;
        }
        Assert.notNull(this.url, "请求的url不能为空");
        String url = this.url.trim();
        // 如果是Https请求
        if (url.startsWith("https")) {
            getTrust();
        }
        Connection connection = Jsoup.connect(url);
        connection.method(getMethod(this.method));
        if (null != this.timeout) {
            connection.timeout(this.timeout);
        }
        connection.ignoreHttpErrors(true);
        connection.ignoreContentType(true);
        connection.followRedirects(true);
        connection.maxBodySize(0);
        // 浏览器标识

        if (StringUtils.isNotBlank(userAgent)) {
            connection.userAgent(userAgent);
        }
        // 用于指明当前流量的来源参考页面
        if (StringUtils.isNotBlank(this.referrer)) {
            connection.referrer(this.referrer);
        }
        Map<String, Object> headers = null == this.headers ? new HashMap<>(0) : this.headers;
        if (StringUtils.isNotBlank(this.contentType)) {
            headers.put("Content-Type", this.contentType);
        }
        headers.forEach((k, v) -> connection.header(k, null == v ? null : v.toString()));
        if (null != this.cookies && !this.cookies.isEmpty()) {
            this.cookies.forEach((k, v) -> {
                connection.cookie(k, null == v ? null : v.toString());
            });

        }
        if (StringUtils.isNotBlank(this.requestBody)) {
            connection.requestBody(this.requestBody.trim());
        } else {
            if (null != this.data) {
                this.data.forEach((k, v) -> {
                    connection.data(k, null == v ? null : v.toString());
                });
            }
        }

        return connection;
    }


    /**
     * 获取请求类型
     *
     * @param method 请求方式
     * @return 请求方式
     */
    private static Connection.Method getMethod(String method) {
        if (StringUtils.isBlank(method)) {
            return Connection.Method.GET;
        }
        Connection.Method connMethod = Connection.Method.GET;
        switch (method.trim().toLowerCase()) {
            case "post":
                connMethod = Connection.Method.POST;
                break;
            case "delete":
                connMethod = Connection.Method.DELETE;
                break;
            case "put":
                connMethod = Connection.Method.PUT;
                break;
            default:
                break;
        }
        return connMethod;
    }

    /**
     * 获取服务器信任
     */
    private static void getTrust() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
