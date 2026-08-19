package com.yishuifengxiao.common.tool.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpClient SSL 全局污染防护测试
 *
 * <p>验证修复后的 SSL 配置不会污染全局 HttpsURLConnection 默认设置，
 * 且 SSLContext 仅按需创建一次（懒加载 + 双重检查锁）。</p>
 *
 * <p>修复前问题：</p>
 * <ul>
 *   <li>每次 HTTPS 请求都调用 HttpsURLConnection.setDefaultSSLSocketFactory()，全局禁用证书验证</li>
 *   <li>每次 HTTPS 请求都调用 HttpsURLConnection.setDefaultHostnameVerifier()，全局跳过主机名验证</li>
 *   <li>每次请求都新建 SSLContext（资源浪费）</li>
 * </ul>
 *
 * <p>修复后行为：</p>
 * <ul>
 *   <li>SSLContext 仅创建一次（懒加载 + 双重检查锁）</li>
 *   <li>仅通过 connection.sslSocketFactory() 设置到当前连接，不修改全局默认值</li>
 * </ul>
 */
class HttpClient_SslNoGlobalPollution_Test {

    /** 修复前的全局默认 SSLSocketFactory 快照 */
    private SSLSocketFactory originalDefaultFactory;

    /** 修复前的全局默认 HostnameVerifier 快照 */
    private HostnameVerifier originalHostnameVerifier;

    @BeforeEach
    void saveGlobalDefaults() {
        // 保存全局默认值，用于测试后验证未被修改
        originalDefaultFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
        originalHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
    }

    @AfterEach
    void resetTrustAllContext() throws Exception {
        // 重置 TRUST_ALL_CONTEXT 为 null，避免跨测试污染
        Field field = HttpClient.class.getDeclaredField("TRUST_ALL_CONTEXT");
        field.setAccessible(true);
        field.set(null, null);
    }

    // ==================== SSLContext 懒加载测试 ====================

    /**
     * 验证 TRUST_ALL_CONTEXT 初始为 null（懒加载，类加载时不创建）
     */
    @Test
    void testTrustAllContext_InitiallyNull() throws Exception {
        Field field = HttpClient.class.getDeclaredField("TRUST_ALL_CONTEXT");
        field.setAccessible(true);
        assertNull(field.get(null), "TRUST_ALL_CONTEXT 应在首次调用前为 null");
    }

    /**
     * 验证调用 getTrustAllContext() 后 TRUST_ALL_CONTEXT 被初始化
     */
    @Test
    void testTrustAllContext_LazyInit() throws Exception {
        SSLContext ctx = invokeGetTrustAllContext();

        Field field = HttpClient.class.getDeclaredField("TRUST_ALL_CONTEXT");
        field.setAccessible(true);
        assertNotNull(field.get(null), "调用后 TRUST_ALL_CONTEXT 应为非 null");
        assertSame(ctx, field.get(null), "返回值与字段值应为同一实例");
    }

    /**
     * 验证多次调用返回同一个 SSLContext 实例（不重复创建）
     */
    @Test
    void testTrustAllContext_SameInstance() throws Exception {
        SSLContext ctx1 = invokeGetTrustAllContext();
        SSLContext ctx2 = invokeGetTrustAllContext();
        SSLContext ctx3 = invokeGetTrustAllContext();

        assertSame(ctx1, ctx2, "第1次和第2次调用应返回同一实例");
        assertSame(ctx2, ctx3, "第2次和第3次调用应返回同一实例");
    }

    /**
     * 验证 TRUST_ALL_CONTEXT 的协议为 TLS
     */
    @Test
    void testTrustAllContext_ProtocolIsTls() throws Exception {
        SSLContext ctx = invokeGetTrustAllContext();
        assertEquals("TLS", ctx.getProtocol(), "SSLContext 协议应为 TLS");
    }

    /**
     * 验证 getTrustAllContext 返回的 SSLContext 的 SSLSocketFactory 不为 null
     */
    @Test
    void testTrustAllContext_SocketFactoryNotNull() throws Exception {
        SSLContext ctx = invokeGetTrustAllContext();
        assertNotNull(ctx.getSocketFactory(), "SSLSocketFactory 不应为 null");
    }

    // ==================== 全局污染防护测试 ====================

    /**
     * 核心测试：调用 getTrustAllContext() 不修改全局默认 SSLSocketFactory
     *
     * <p>修复前：getTrust() 调用 HttpsURLConnection.setDefaultSSLSocketFactory()，
     * 全局替换为 trust-all 工厂</p>
     * <p>修复后：getTrustAllContext() 仅返回 SSLContext，不修改全局默认值</p>
     */
    @Test
    void testNoGlobalSslSocketFactoryPollution() throws Exception {
        // 调用 getTrustAllContext()
        invokeGetTrustAllContext();

        // 核心断言：全局默认值未被修改
        assertSame(originalDefaultFactory,
                HttpsURLConnection.getDefaultSSLSocketFactory(),
                "全局默认 SSLSocketFactory 不应被修改");
    }

    /**
     * 核心测试：调用 getTrustAllContext() 不修改全局默认 HostnameVerifier
     *
     * <p>修复前：getTrust() 调用 HttpsURLConnection.setDefaultHostnameVerifier()，
     * 全局跳过主机名验证</p>
     * <p>修复后：不再调用 setDefaultHostnameVerifier</p>
     */
    @Test
    void testNoGlobalHostnameVerifierPollution() throws Exception {
        // 调用 getTrustAllContext()
        invokeGetTrustAllContext();

        // 核心断言：全局默认 HostnameVerifier 未被修改
        assertSame(originalHostnameVerifier,
                HttpsURLConnection.getDefaultHostnameVerifier(),
                "全局默认 HostnameVerifier 不应被修改");
    }

    /**
     * 核心测试：创建 HttpClient 实例不修改全局 SSL 默认值
     *
     * <p>即使创建多个 HttpClient 实例，全局 SSL 设置也不应被修改</p>
     */
    @Test
    void testHttpClientCreation_NoGlobalPollution() {
        // 创建多个 HttpClient 实例
        HttpClient client1 = HttpClient.instance().url("https://example.com");
        HttpClient client2 = HttpClient.instance().url("https://example.com");
        HttpClient client3 = HttpClient.instance().url("http://example.com");

        // 核心断言：全局默认值未被修改
        assertSame(originalDefaultFactory,
                HttpsURLConnection.getDefaultSSLSocketFactory(),
                "创建 HttpClient 实例不应修改全局 SSLSocketFactory");
        assertSame(originalHostnameVerifier,
                HttpsURLConnection.getDefaultHostnameVerifier(),
                "创建 HttpClient 实例不应修改全局 HostnameVerifier");
    }

    /**
     * 核心测试：重复调用 getTrustAllContext 多次，全局默认值仍不被修改
     */
    @Test
    void testMultipleCalls_NoGlobalPollution() throws Exception {
        for (int i = 0; i < 10; i++) {
            invokeGetTrustAllContext();
        }

        assertSame(originalDefaultFactory,
                HttpsURLConnection.getDefaultSSLSocketFactory(),
                "10次调用后全局默认值仍不应被修改");
        assertSame(originalHostnameVerifier,
                HttpsURLConnection.getDefaultHostnameVerifier(),
                "10次调用后全局 HostnameVerifier 仍不应被修改");
    }

    // ==================== 辅助方法 ====================

    /**
     * 通过反射调用私有静态方法 getTrustAllContext()
     */
    private SSLContext invokeGetTrustAllContext() throws Exception {
        Method method = HttpClient.class.getDeclaredMethod("getTrustAllContext");
        method.setAccessible(true);
        return (SSLContext) method.invoke(null);
    }
}
