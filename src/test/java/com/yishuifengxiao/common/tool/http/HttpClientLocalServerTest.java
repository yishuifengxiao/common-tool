package com.yishuifengxiao.common.tool.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 基于本机回环 HTTP 服务的真实网络集成测试
 * <p>
 * 与其他仅校验配置项的 HttpClient 测试互补，验证真实 socket 收发、请求体编码与响应体解析，
 * 不依赖任何外部网站。
 * </p>
 */
class HttpClientLocalServerTest {

    private HttpServer server;
    private String baseUrl;

    /**
     * 启动本机回环 HTTP 服务，提供固定响应与请求体回显两个端点
     *
     * @throws IOException 端口绑定失败
     */
    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/hello", exchange -> writeResponse(exchange, "hello-local"));
        server.createContext("/echo", exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            writeResponse(exchange, requestBody);
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    /**
     * 停止本机回环 HTTP 服务
     */
    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * 验证 GET 请求能通过真实连接取回响应体
     */
    @Test
    void testGetWithRealLocalServer() {
        assertEquals("hello-local", HttpClient.get(baseUrl + "/hello"));
    }

    /**
     * 验证表单 POST 请求的请求体经过真实传输后能被服务端读取
     */
    @Test
    void testPostFormWithRealLocalServer() {
        Map<String, String> data = new HashMap<>();
        data.put("name", "张三");

        String response = HttpClient.postForm(baseUrl + "/echo", data);
        assertNotNull(response);
        assertTrue(response.contains("name="), "表单请求体应包含字段名, 实际响应: " + response);
    }

    /**
     * 验证 JSON POST 请求的请求体经过真实传输后保持原样
     */
    @Test
    void testPostJsonWithRealLocalServer() {
        String json = "{\"a\":1}";
        assertEquals(json, HttpClient.postJson(baseUrl + "/echo", json));
    }

    /**
     * 向 HTTP 交换写入固定响应并关闭连接
     *
     * @param exchange HTTP 交换对象
     * @param body     响应体文本
     * @throws IOException 写入失败
     */
    private void writeResponse(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }
}
