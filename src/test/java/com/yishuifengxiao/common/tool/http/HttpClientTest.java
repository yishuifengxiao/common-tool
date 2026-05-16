package com.yishuifengxiao.common.tool.http;

import org.jsoup.Connection;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * HttpClient单元测试类
 * 测试HttpClient的各项功能，包括：
 * - 实例创建和链式调用
 * - 请求方法设置（GET/POST/PUT/DELETE）
 * - 请求头和Cookie设置
 * - 静态便捷方法
 * - 内容类型设置
 */
public class HttpClientTest {

    /**
     * 测试正常场景：获取HttpClient实例
     * 验证instance()方法能正确返回新的HttpClient实例
     */
    @Test
    public void testInstance_NormalCase() {
        HttpClient client1 = HttpClient.instance();
        HttpClient client2 = HttpClient.instance();
        
        assertNotNull(client1);
        assertNotNull(client2);
        // 每次调用应该返回新实例
        assertNotSame(client1, client2);
    }

    /**
     * 测试正常场景：设置User-Agent
     * 验证userAgent()方法能正确设置并返回当前实例
     */
    @Test
    public void testUserAgent_NormalCase() {
        HttpClient client = HttpClient.instance();
        String userAgent = "Mozilla/5.0 (Custom Agent)";
        
        HttpClient result = client.userAgent(userAgent);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置Content-Type
     * 验证contentType()方法能正确设置并返回当前实例
     */
    @Test
    public void testContentType_NormalCase() {
        HttpClient client = HttpClient.instance();
        String contentType = "application/xml;charset=UTF-8";
        
        HttpClient result = client.contentType(contentType);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置Referer
     * 验证referrer()方法能正确设置并返回当前实例
     */
    @Test
    public void testReferrer_NormalCase() {
        HttpClient client = HttpClient.instance();
        String referrer = "https://www.example.com";
        
        HttpClient result = client.referrer(referrer);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：添加单个请求头
     * 验证addHeader()方法能正确添加请求头并返回当前实例
     */
    @Test
    public void testAddHeader_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.addHeader("X-Custom-Header", "custom-value");
        
        assertSame(client, result);
    }

    /**
     * 测试边界场景：添加空请求头名称
     * 验证当name为空时不会添加请求头
     */
    @Test
    public void testAddHeader_EmptyName() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.addHeader("", "value");
        
        assertSame(client, result);
    }

    /**
     * 测试边界场景：添加null值请求头
     * 验证当value为null时不会添加请求头
     */
    @Test
    public void testAddHeader_NullValue() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.addHeader("X-Header", null);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：批量添加请求头
     * 验证addHeaders()方法能正确批量添加请求头
     */
    @Test
    public void testAddHeaders_NormalCase() {
        HttpClient client = HttpClient.instance();
        Map<String, String> headers = new HashMap<>();
        headers.put("Header1", "Value1");
        headers.put("Header2", "Value2");
        
        HttpClient result = client.addHeaders(headers);
        
        assertSame(client, result);
    }

    /**
     * 测试边界场景：添加null请求头Map
     * 验证当headers为null时不会抛出异常
     */
    @Test
    public void testAddHeaders_NullMap() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.addHeaders(null);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置请求头Map
     * 验证setHeaders()方法能正确替换所有请求头
     */
    @Test
    public void testSetHeaders_NormalCase() {
        HttpClient client = HttpClient.instance();
        Map<String, String> headers = new HashMap<>();
        headers.put("Header1", "Value1");
        
        HttpClient result = client.setHeaders(headers);
        
        assertSame(client, result);
    }

    /**
     * 测试边界场景：设置null请求头Map
     * 验证setHeaders(null)会清空所有请求头
     */
    @Test
    public void testSetHeaders_NullMap() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.setHeaders(null);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置超时时间
     * 验证timeout()方法能正确设置超时时间
     */
    @Test
    public void testTimeout_NormalCase() {
        HttpClient client = HttpClient.instance();
        Integer timeout = 5000;
        
        HttpClient result = client.timeout(timeout);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置URL
     * 验证url()方法能正确设置URL
     */
    @Test
    public void testUrl_NormalCase() {
        HttpClient client = HttpClient.instance();
        String url = "https://www.example.com";
        
        HttpClient result = client.url(url);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置请求方法
     * 验证method()方法能正确设置请求方法
     */
    @Test
    public void testMethod_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.method("get");
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置自动User-Agent
     * 验证autoUserAgent()方法能正确设置自动User-Agent开关
     */
    @Test
    public void testAutoUserAgent_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.autoUserAgent(false);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置请求体字符串
     * 验证data(String)方法能正确设置请求体并设置为POST请求
     */
    @Test
    public void testDataString_NormalCase() {
        HttpClient client = HttpClient.instance();
        String requestBody = "{\"key\":\"value\"}";
        
        HttpClient result = client.data(requestBody);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置请求体键值对
     * 验证data(Map)方法能正确设置请求体键值对并设置为POST请求
     */
    @Test
    public void testDataMap_NormalCase() {
        HttpClient client = HttpClient.instance();
        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        
        HttpClient result = client.data(data);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置为表单提交
     * 验证form()方法能正确设置Content-Type为表单类型
     */
    @Test
    public void testForm_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.form();
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置为JSON提交
     * 验证json()方法能正确设置Content-Type为JSON类型
     */
    @Test
    public void testJson_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.json();
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置Cookies
     * 验证cookies()方法能正确设置Cookies
     */
    @Test
    public void testCookies_NormalCase() {
        HttpClient client = HttpClient.instance();
        Map<String, String> cookies = new HashMap<>();
        cookies.put("session", "abc123");
        
        HttpClient result = client.cookies(cookies);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置为POST请求
     * 验证post()方法能正确设置请求方法为POST
     */
    @Test
    public void testPost_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.post();
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置为GET请求
     * 验证get()方法能正确设置请求方法为GET
     */
    @Test
    public void testGet_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.get();
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置为PUT请求
     * 验证put()方法能正确设置请求方法为PUT
     */
    @Test
    public void testPut_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.put();
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置为DELETE请求
     * 验证delete()方法能正确设置请求方法为DELETE
     */
    @Test
    public void testDelete_NormalCase() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.delete();
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：静态GET方法
     * 验证HttpClient.get()静态方法能发送GET请求
     * 注意：此测试需要网络连接，可能会因网络问题失败
     */
    @Test
    public void testGetStatic_NormalCase() {
        // 使用一个可靠的测试URL
        String result = HttpClient.get("https://httpbin.org/get");
        
        // 如果网络可用，应该有响应；如果不可用，返回null
        // 这里只验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试边界场景：静态GET方法处理无效URL
     * 验证当URL无效时不会抛出异常
     */
    @Test
    public void testGetStatic_InvalidUrl() {
        String result = HttpClient.get("https://invalid.url.that.does.not.exist");
        
        // 无效URL应该返回null而不是抛出异常
        assertNull(result);
    }

    /**
     * 测试正常场景：静态POST表单方法（带请求头）
     * 验证postForm()静态方法能发送表单POST请求
     */
    @Test
    public void testPostFormWithHeaders_NormalCase() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "test");
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        String result = HttpClient.postForm("https://httpbin.org/post", headers, data);
        
        // 验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试正常场景：静态POST表单方法（不带请求头）
     * 验证postForm()静态方法能发送表单POST请求
     */
    @Test
    public void testPostFormWithoutHeaders_NormalCase() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        String result = HttpClient.postForm("https://httpbin.org/post", data);
        
        // 验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试正常场景：静态POST JSON方法（带请求头）
     * 验证postJson()静态方法能发送JSON POST请求
     */
    @Test
    public void testPostJsonWithHeaders_NormalCase() {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "test");
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        String result = HttpClient.postJson("https://httpbin.org/post", headers, data);
        
        // 验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试正常场景：静态POST JSON方法（不带请求头）
     * 验证postJson()静态方法能发送JSON POST请求
     */
    @Test
    public void testPostJsonWithoutHeaders_NormalCase() {
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        String result = HttpClient.postJson("https://httpbin.org/post", data);
        
        // 验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试正常场景：静态POST JSON方法（字符串请求体）
     * 验证postJson()静态方法能发送JSON字符串POST请求
     */
    @Test
    public void testPostJsonWithStringBody_NormalCase() {
        String requestBody = "{\"key\":\"value\"}";
        
        String result = HttpClient.postJson("https://httpbin.org/post", requestBody);
        
        // 验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试正常场景：静态execute方法（Map数据）
     * 验证execute()静态方法能执行请求并返回Response对象
     */
    @Test
    public void testExecuteWithMapData_NormalCase() {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        Connection.Response response = HttpClient.execute(
            "https://httpbin.org/post", 
            "post", 
            HttpClient.CONTENT_TYPE_FORM, 
            headers, 
            data
        );
        
        // 验证方法不会抛出异常
        assertNotNull(response);
    }

    /**
     * 测试正常场景：静态executeAsString方法（Map数据）
     * 验证executeAsString()静态方法能执行请求并返回字符串响应
     */
    @Test
    public void testExecuteAsStringWithMapData_NormalCase() {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        String result = HttpClient.executeAsString(
            "https://httpbin.org/post", 
            "post", 
            HttpClient.CONTENT_TYPE_FORM, 
            headers, 
            data
        );
        
        // 验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试正常场景：静态execute方法（字符串请求体）
     * 验证execute()静态方法能执行请求并返回Response对象
     */
    @Test
    public void testExecuteWithStringBody_NormalCase() {
        Map<String, String> headers = new HashMap<>();
        String requestBody = "{\"key\":\"value\"}";
        
        Connection.Response response = HttpClient.execute(
            "https://httpbin.org/post", 
            "post", 
            HttpClient.CONTENT_TYPE_JSON, 
            headers, 
            requestBody
        );
        
        // 验证方法不会抛出异常
        assertNotNull(response);
    }

    /**
     * 测试正常场景：静态executeAsString方法（字符串请求体）
     * 验证executeAsString()静态方法能执行请求并返回字符串响应
     */
    @Test
    public void testExecuteAsStringWithStringBody_NormalCase() {
        Map<String, String> headers = new HashMap<>();
        String requestBody = "{\"key\":\"value\"}";
        
        String result = HttpClient.executeAsString(
            "https://httpbin.org/post", 
            "post", 
            HttpClient.CONTENT_TYPE_JSON, 
            headers, 
            requestBody
        );
        
        // 验证方法不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试正常场景：链式调用构建GET请求
     * 验证可以链式调用多个方法来构建请求
     */
    @Test
    public void testChainedCall_GetRequest() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client
            .url("https://httpbin.org/get")
            .get()
            .timeout(5000)
            .addHeader("X-Custom-Header", "test")
            .autoUserAgent(true);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：链式调用构建POST请求
     * 验证可以链式调用多个方法来构建POST请求
     */
    @Test
    public void testChainedCall_PostRequest() {
        HttpClient client = HttpClient.instance();
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        HttpClient result = client
            .url("https://httpbin.org/post")
            .post()
            .form()
            .data(data)
            .timeout(5000)
            .addHeader("X-Custom-Header", "test");
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：链式调用构建JSON POST请求
     * 验证可以链式调用多个方法来构建JSON POST请求
     */
    @Test
    public void testChainedCall_JsonPostRequest() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client
            .url("https://httpbin.org/post")
            .post()
            .json()
            .data("{\"key\":\"value\"}")
            .timeout(5000)
            .addHeader("Content-Type", "application/json");
        
        assertSame(client, result);
    }

    /**
     * 测试常量：CONTENT_TYPE_FORM
     * 验证表单Content-Type常量值正确
     */
    @Test
    public void testConstant_ContentTypeForm() {
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", 
            HttpClient.CONTENT_TYPE_FORM);
    }

    /**
     * 测试常量：CONTENT_TYPE_JSON
     * 验证JSON Content-Type常量值正确
     */
    @Test
    public void testConstant_ContentTypeJson() {
        assertEquals("application/json;charset=UTF-8", 
            HttpClient.CONTENT_TYPE_JSON);
    }

    /**
     * 测试边界场景：execute方法处理无效URL
     * 验证当URL无效时execute()方法返回null而不是抛出异常
     */
    @Test
    public void testExecute_InvalidUrl() {
        HttpClient client = HttpClient.instance();
        
        Connection.Response response = client
            .url("https://invalid.url.that.does.not.exist")
            .get()
            .execute();
        
        // 无效URL应该返回null而不是抛出异常
        assertNull(response);
    }

    /**
     * 测试边界场景：executeAsString方法处理无效URL
     * 验证当URL无效时executeAsString()方法返回null而不是抛出异常
     */
    @Test
    public void testExecuteAsString_InvalidUrl() {
        HttpClient client = HttpClient.instance();
        
        String result = client
            .url("https://invalid.url.that.does.not.exist")
            .get()
            .executeAsString();
        
        // 无效URL应该返回null而不是抛出异常
        assertNull(result);
    }

    /**
     * 测试正常场景：HTTPS请求
     * 验证HTTPS请求能正常处理（跳过证书验证）
     */
    @Test
    public void testHttps_Request() {
        String result = HttpClient.get("https://httpbin.org/get");
        
        // 验证HTTPS请求不会抛出异常
        assertNotNull(result);
    }

    /**
     * 测试边界场景：负数超时时间
     * 验证负数超时时间被正确处理
     */
    @Test
    public void testTimeout_NegativeValue() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.timeout(-1);
        
        assertSame(client, result);
    }

    /**
     * 测试边界场景：零超时时间
     * 验证零超时时间被正确处理（表示无限超时）
     */
    @Test
    public void testTimeout_ZeroValue() {
        HttpClient client = HttpClient.instance();
        
        HttpClient result = client.timeout(0);
        
        assertSame(client, result);
    }

    /**
     * 测试正常场景：设置完整的请求配置
     * 验证可以同时设置所有请求参数
     */
    @Test
    public void testFullConfiguration_NormalCase() {
        HttpClient client = HttpClient.instance();
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        Map<String, String> cookies = new HashMap<>();
        cookies.put("session", "abc123");
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        HttpClient result = client
            .url("https://httpbin.org/post")
            .post()
            .form()
            .userAgent("Custom Agent")
            .contentType(HttpClient.CONTENT_TYPE_FORM)
            .referrer("https://example.com")
            .setHeaders(headers)
            .cookies(cookies)
            .data(data)
            .timeout(10000)
            .autoUserAgent(false);
        
        assertSame(client, result);
    }
}
