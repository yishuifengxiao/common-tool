package com.yishuifengxiao.common.tool.http;

import com.yishuifengxiao.common.tool.utils.Assert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
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
    public final static String Content_Type_FORM = "application/x-www-form-urlencoded; charset=UTF-8";

    /**
     * Content-Type:application/json;charset=UTF-8
     */
    public final static String Content_Type_JSON = "application/json;charset=UTF-8";

    /**
     * 请求客户端实例
     */
    private static volatile HttpClient httpClient = null;

    /**
     * 获取http客户端（单例模式）
     *
     * @return http客户端
     */
    public final static HttpClient instance() {
        if (null == httpClient) {
            synchronized (HttpClient.class) {
                if (null == httpClient) {
                    httpClient = new HttpClient();
                }
            }
        }
        return httpClient;
    }

    /**
     * 根据url发送get请求
     *
     * @param url url
     * @return 请求的响应
     */
    public Connection.Response get(String url) {
        Connection connection = connection(url, null, 0, UserAgent.autoUserAgent(), null, null, null, null);
        return execute(connection);
    }

    /**
     * 根据url发送get请求
     *
     * @param url      url
     * @param referrer 流量来源页
     * @return 请求的响应
     */
    public Connection.Response get(String url, String referrer) {
        Connection connection = connection(url, null, 0, UserAgent.autoUserAgent(), referrer, null, null, null);
        return execute(connection);
    }

    /**
     * 根据url发送get请求，并把响应体转换成文本
     *
     * @param url url
     * @return 请求的响应体文本
     */
    public String getAsString(String url) {
        Connection.Response response = get(url);
        return null == response ? null : response.body();
    }


    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求
     *
     * @param url     url
     * @param headers 请求头
     * @param data    请求数据
     * @return 请求的响应
     */
    public Connection.Response postForm(String url, Map<String, String> headers, Map<String, String> data) {

        return post(url, Content_Type_FORM, headers, data);
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url     url
     * @param headers 请求头
     * @param data    请求数据
     * @return 请求的响应体的响应文本
     */
    public String postFormAsString(String url, Map<String, String> headers, Map<String, String> data) {
        Connection.Response response = post(url, Content_Type_FORM, headers, data);
        return null == response ? null : response.body();
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应
     */
    public Connection.Response postForm(String url, Map<String, String> data) {

        return post(url, Content_Type_FORM, null, data);
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应体的响应文本
     */
    public String postFormAsString(String url, Map<String, String> data) {

        Connection.Response response = post(url, Content_Type_FORM, null, data);
        return null == response ? null : response.body();
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求
     *
     * @param url     url
     * @param headers 请求头
     * @param data    请求数据
     * @return 请求的响应
     */
    public Connection.Response postJson(String url, Map<String, String> headers, Map<String, String> data) {
        return post(url, Content_Type_JSON, headers, data);
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url     url
     * @param headers 请求头
     * @param data    请求数据
     * @return 请求的响应体的响应文本
     */
    public String postJsonAsString(String url, Map<String, String> headers, Map<String, String> data) {
        Connection.Response response = post(url, Content_Type_JSON, headers, data);
        return null == response ? null : response.body();
    }

    /**
     * 使用 application/x-www-form-urlencoded; charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应体的响应文本
     */
    public Connection.Response postJson(String url, Map<String, String> data) {
        return post(url, Content_Type_JSON, null, data);
    }

    /**
     * 使用 application/json;charset=UTF-8 方式发送POST请求，并把响应体转换成文本
     *
     * @param url  url
     * @param data 请求数据
     * @return 请求的响应体的响应文本
     */
    public String postJsonAsString(String url, Map<String, String> data) {
        Connection.Response response = post(url, Content_Type_JSON, null, data);
        return null == response ? null : response.body();
    }

    /**
     * 根据url发送POST请求
     *
     * @param url         url
     * @param contentType Content-Type
     * @param headers     请求头
     * @param data        请求体
     * @return 响应
     */
    public Connection.Response post(String url, String contentType, Map<String, String> headers, Map<String, String> data) {
        Connection connection = connection(url, "post", 0, UserAgent.autoUserAgent(), null, contentType, headers, null);
        if (null != data) {
            connection = connection.data(data);
        }
        return execute(connection);
    }

    /**
     * 执行请求
     *
     * @param connection 请求连接
     * @return 响应
     */
    private Connection.Response execute(Connection connection) {
        Connection.Response response = null;
        try {
            response = connection.execute();
        } catch (Throwable e) {
            log.warn("======> 请求 {} 时出现问题 {}", connection, e);
        }
        return response;
    }

    /**
     * 根据参数构建出请求连接
     *
     * @param url         请求的目标URL
     * @param method      请求类型
     * @param timeOut     连接超时时间
     * @param userAgent   浏览器标识
     * @param referrer    流量来源页
     * @param contentType 内容类型
     * @param headers     请求头
     * @param cookies     cookies
     * @return 请求连接
     */
    public Connection connection(String url, String method, int timeOut, String userAgent, String referrer, String contentType, Map<String, String> headers, Map<String, String> cookies) {

        Assert.notNull(url, "请求的url不能为空");
        url = url.trim();
        // 如果是Https请求
        if (url.startsWith("https")) {
            getTrust();
        }

        Connection connection = Jsoup.connect(url);
        connection.method(getMethod(method));
        connection.timeout(timeOut);
        connection.ignoreHttpErrors(true);
        connection.ignoreContentType(true);
        connection.maxBodySize(0);

        // 浏览器标识

        if (StringUtils.isNotBlank(userAgent)) {
            connection.userAgent(userAgent);
        }
        // 用于指明当前流量的来源参考页面
        if (StringUtils.isNotBlank(referrer)) {
            connection.referrer(referrer);
        }
        if (StringUtils.isNotBlank(contentType)) {
            if (null == headers) {
                headers = new HashMap<>(1);
            }
            headers.put("Content-Type", contentType);
        }

        if (null != headers) {
            headers.forEach((k, v) -> connection.header(k, v));
        }
        if (null != cookies && !cookies.isEmpty()) {
            connection.cookies(cookies);
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
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
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
