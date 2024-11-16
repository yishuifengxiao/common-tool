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
    public final static String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded; "
            + "charset=UTF-8";

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
    private Map<String, String> headers = new HashMap<>();

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
    private Map<String, String> cookies;
    /**
     * 请求体键值对与 requestBody 互斥
     */
    private Map<String, String> data;

    /**
     * 请求体，与data 互斥
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
     * @return 当前对象实例
     */
    public HttpClient userAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * 设置 contentType
     *
     * @param contentType contentType
     * @return 当前对象实例
     */
    public HttpClient contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 设置 referrer
     *
     * @param referrer referrer
     * @return 当前对象实例
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
     * @return 当前对象实例
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
     * @return 当前对象实例
     */
    public HttpClient addHeaders(Map<String, String> headers) {
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
     * @return 当前对象实例
     */
    public HttpClient setHeaders(Map<String, String> headers) {
        if (null == headers) {
            this.headers = new HashMap<>(0);
            return this;
        }
        this.headers.clear();
        this.addHeaders(headers);
        return this;
    }

    /**
     * <p>
     * 设置超时时间
     * </p>
     * <p>
     * 设置总请求超时持续时间。如果发生超时，将抛出java.net.SocketTimeoutException。
     * 默认超时为30秒（30000毫秒）。超时为零被视为无限超时。 请注意，此超时指定了连接时间和读取完整响应时间的组合最大持续时间。
     * </p>
     *
     * @param timeout 连接或读取超时前的毫秒数（千分之一秒）。
     * @return 当前对象实例
     */
    public HttpClient timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * 设置请求的url
     *
     * @param url 请求的url
     * @return 当前对象实例
     */
    public HttpClient url(String url) {
        this.url = url;
        return this;
    }

    /**
     * <p>
     * 设置请求的类型
     * </p>
     * <p>
     * 默认为get请求
     * </p>
     *
     * @param method 请求的类型
     * @return 当前对象实例
     */
    public HttpClient method(String method) {
        this.method = method;
        return this;
    }

    /**
     * 设置是否使用是否使用自动User-Agent
     *
     * @param autoUserAgent 是否使用是否使用自动User-Agent,默认值为true
     * @return 当前对象实例
     */
    public HttpClient autoUserAgent(boolean autoUserAgent) {
        this.autoUserAgent = autoUserAgent;
        return this;
    }

    /**
     * 设置请求体
     *
     * @param requestBody 请求体(与data 互斥)
     * @return 当前对象实例
     */
    public HttpClient data(String requestBody) {
        this.requestBody = requestBody;
        return this.post();
    }

    /**
     * 设置请求体键值对
     *
     * @param data 请求体键值对与 requestBody 互斥
     * @return 当前对象实例
     */
    public HttpClient data(Map<String, String> data) {
        this.data = data;
        return this.post();
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送请求
     *
     * @return 当前对象实例
     */
    public HttpClient form() {
        this.contentType = CONTENT_TYPE_FORM;
        return this;
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送请求
     *
     * @return 当前对象实例
     */
    public HttpClient json() {
        this.contentType = CONTENT_TYPE_JSON;
        return this;
    }

    /**
     * 设置 cookies
     *
     * @param cookies cookies
     * @return 当前对象实例
     */
    public HttpClient cookies(Map<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }

    /**
     * 设置为post请求
     *
     * @return 当前对象实例
     */
    public HttpClient post() {
        this.method = "post";
        return this;
    }

    /**
     * 设置为get请求
     *
     * @return 当前对象实例
     */
    public HttpClient get() {
        this.method = "get";
        return this;
    }

    /**
     * 设置为put请求
     *
     * @return 当前对象实例
     */
    public HttpClient put() {
        this.method = "put";
        return this;
    }

    /**
     * 设置为delete请求
     *
     * @return 当前对象实例
     */
    public HttpClient delete() {
        this.method = "delete";
        return this;
    }

    /**
     * 根据url发送get请求，并把响应体转换成文本
     *
     * @param url url
     * @return 请求的响应体文本
     */
    public static String get(String url) {
        HttpClient client = new HttpClient();
        client.url = url;
        client.method = "get";
        return client.executeAsString();
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url     url
     * @param headers 请求头
     * @param data    请求数据
     * @return 请求的响应体的响应文本
     */
    public static String postForm(String url, Map<String, String> headers,
                                  Map<String, String> data) {
        return executeAsString(url, "post", CONTENT_TYPE_FORM, headers, data);
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应体的响应文本
     */
    public static String postForm(String url, Map<String, String> data) {

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
    public static String postJson(String url, Map<String, String> headers,
                                  Map<String, String> data) {
        return executeAsString(url, "post", CONTENT_TYPE_JSON, headers, data);
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应体的响应文本
     */
    public static String postJson(String url, Map<String, String> data) {
        return executeAsString(url, "post", CONTENT_TYPE_JSON, null, data);
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url         url
     * @param requestBody 请求体
     * @return 请求的响应体的响应文本
     */
    public static String postJson(String url, String requestBody) {
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
    public static Connection.Response execute(String url, String method, String contentType,
                                              Map<String, String> headers,
                                              Map<String, String> data) {
        HttpClient client = new HttpClient();
        client.url(url);
        client.method(method);
        client.contentType(contentType);
        client.setHeaders(headers);
        client.data(data);
        return client.execute();
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
    public static String executeAsString(String url, String method, String contentType,
                                         Map<String, String> headers,
                                         Map<String, String> data) {
        Connection.Response response = execute(url, method, contentType, headers, data);
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
    public static Connection.Response execute(String url, String method, String contentType,
                                              Map<String, String> headers, String requestBody) {
        HttpClient client = new HttpClient();
        client.url(url);
        client.method(method);
        client.contentType(contentType);
        client.setHeaders(headers);
        client.data(requestBody);
        return client.execute();
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
    public static String executeAsString(String url, String method, String contentType,
                                         Map<String, String> headers,
                                         String requestBody) {
        Connection.Response response = execute(url, method, contentType, headers, requestBody);
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
            if (log.isInfoEnabled()) {
                log.info("There was a problem requesting {}, the problem is {}", this.url, e);
            }

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
        Assert.isNotBlank(this.url, "The requested URL cannot be empty");
        String userAgent = this.userAgent;
        if (this.autoUserAgent && StringUtils.isBlank(this.userAgent)) {
            userAgent = UserAgent.USER_AGENT_EDGE_VERSION_110_0.description();
        }
        String url = this.url.trim();
        // 如果是Https请求
        if (url.startsWith("https")) {
            getTrust();
        }
        Connection connection = Jsoup.connect(url);
        connection.method(getMethod(this.method));
        if (null != this.timeout) {
            if (this.timeout < 0) {
                connection.timeout(0);
            } else {
                connection.timeout(this.timeout);
            }

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
        Map<String, String> headers = null == this.headers ? new HashMap<>(0) : this.headers;
        if (StringUtils.isNotBlank(this.contentType)) {
            headers.put("Content-Type", this.contentType);
        }
        headers.forEach((k, v) -> connection.header(k, null == v ? null : v));
        if (null != this.cookies && !this.cookies.isEmpty()) {
            this.cookies.forEach((k, v) -> {
                if (null != k && null != v) {
                    connection.cookie(k, v);
                }
            });

        }
        if (StringUtils.isNotBlank(this.requestBody)) {
            connection.requestBody(this.requestBody.trim());
        } else {
            if (null != this.data) {
                this.data.forEach((k, v) -> {
                    if (null != k && null != v) {
                        connection.data(k, v);
                    }
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
