package com.yishuifengxiao.common.tool.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JdbcUrlHelper 工具类单元测试
 */
@DisplayName("JdbcUrlHelper工具类测试")
class JdbcUrlHelperTest {

    // ==================== parseJdbcUrl 方法测试 ====================

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 完整URL解析")
    void testParseJdbcUrlFull() {
        String url = "jdbc:mysql://localhost:3306/personkit?useUnicode=true&characterEncoding=utf8&useSSL=false";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        assertNotNull(info);
        assertEquals("mysql", info.getProtocol());
        assertEquals("localhost", info.getHost());
        assertEquals(3306, info.getPort());
        assertEquals("personkit", info.getDatabase());

        Map<String, String> params = info.getParameters();
        assertNotNull(params);
        assertEquals(3, params.size());
        assertEquals("true", params.get("useUnicode"));
        assertEquals("utf8", params.get("characterEncoding"));
        assertEquals("false", params.get("useSSL"));
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 默认端口")
    void testParseJdbcUrlDefaultPort() {
        String url = "jdbc:mysql://localhost/testdb";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        assertEquals("localhost", info.getHost());
        assertEquals(3306, info.getPort()); // 默认端口
        assertEquals("testdb", info.getDatabase());
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 非默认端口")
    void testParseJdbcUrlCustomPort() {
        String url = "jdbc:mysql://192.168.1.1:3307/mydb";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        assertEquals("192.168.1.1", info.getHost());
        assertEquals(3307, info.getPort());
        assertEquals("mydb", info.getDatabase());
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 不含数据库名")
    void testParseJdbcUrlNoDatabase() {
        String url = "jdbc:mysql://localhost:3306/";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        assertNull(info.getDatabase());
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 不含参数")
    void testParseJdbcUrlNoParams() {
        String url = "jdbc:mysql://localhost:3306/testdb";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        assertTrue(info.getParameters().isEmpty());
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 特殊字符参数解码")
    void testParseJdbcUrlSpecialCharacters() {
        // 姓名=%E5%BC%A0%E4%B8%89 解码后是 "张三"
        // msg=hello%20world 解码后是 "hello world"
        String url = "jdbc:mysql://localhost:3306/test?name=%E5%BC%A0%E4%B8%89&msg=hello%20world&flag=true";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        Map<String, String> params = info.getParameters();
        assertEquals("张三", params.get("name"));
        assertEquals("hello world", params.get("msg"));
        assertEquals("true", params.get("flag"));
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 编码的数据库名")
    void testParseJdbcUrlEncodedDatabase() {
        // 数据库名 test%20db 解码后是 "test db"
        String url = "jdbc:mysql://localhost:3306/test%20db";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        assertEquals("test db", info.getDatabase());
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 参数值为空")
    void testParseJdbcUrlEmptyParamValue() {
        String url = "jdbc:mysql://localhost:3306/testdb?empty=&nullish=&key=value";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        Map<String, String> params = info.getParameters();
        assertEquals("", params.get("empty"));
        assertEquals("", params.get("nullish"));
        assertEquals("value", params.get("key"));
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - null URL抛出异常")
    void testParseJdbcUrlNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            JdbcUrlHelper.parseJdbcUrl(null);
        });
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 无效前缀抛出异常")
    void testParseJdbcUrlInvalidPrefix() {
        String url = "jdbc:postgresql://localhost:5432/testdb";

        assertThrows(IllegalArgumentException.class, () -> {
            JdbcUrlHelper.parseJdbcUrl(url);
        });
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 无主机名抛出异常")
    void testParseJdbcUrlNoHost() {
        String url = "jdbc:mysql://:3306/testdb";

        assertThrows(IllegalArgumentException.class, () -> {
            JdbcUrlHelper.parseJdbcUrl(url);
        });
    }

    @Test
    @DisplayName("测试parseJdbcUrl方法 - 格式错误URL抛出异常")
    void testParseJdbcUrlInvalidFormat() {
        String url = "jdbc:mysql://invalid:url/testdb";

        assertThrows(IllegalArgumentException.class, () -> {
            JdbcUrlHelper.parseJdbcUrl(url);
        });
    }

    // ==================== buildJdbcUrl 方法测试 ====================

    @Test
    @DisplayName("测试buildJdbcUrl方法 - 完整信息构建")
    void testBuildJdbcUrlFull() {
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo(
                "mysql", "localhost", 3306, "testdb",
                Map.of("useSSL", "false", "characterEncoding", "utf8")
        );

        String url = JdbcUrlHelper.buildJdbcUrl(info);

        assertNotNull(url);
        assertTrue(url.startsWith("jdbc:mysql://"));
        assertTrue(url.contains("localhost"));
        assertTrue(url.contains("testdb"));
        assertTrue(url.contains("useSSL=false"));
        assertTrue(url.contains("characterEncoding=utf8"));
    }


    @Test
    @DisplayName("测试buildJdbcUrl方法 - 非默认端口显示")
    void testBuildJdbcUrlCustomPort() {
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo(
                "mysql", "localhost", 3307, "testdb", Map.of()
        );

        String url = JdbcUrlHelper.buildJdbcUrl(info);

        assertTrue(url.contains(":3307"));
    }

    @Test
    @DisplayName("测试buildJdbcUrl方法 - 特殊字符参数编码")
    void testBuildJdbcUrlSpecialCharacters() {
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo(
                "mysql", "localhost", 3306, "testdb",
                Map.of("name", "张三", "msg", "hello world")
        );

        String url = JdbcUrlHelper.buildJdbcUrl(info);

        assertTrue(url.contains("name="));
        assertTrue(url.contains("msg="));
        // 空格编码为 '+'
        assertTrue(url.contains("msg=hello+world"));
    }

    @Test
    @DisplayName("测试buildJdbcUrl方法 - 特殊字符数据库名编码")
    void testBuildJdbcUrlEncodedDatabase() {
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo(
                "mysql", "localhost", 3306, "test db", Map.of()
        );

        String url = JdbcUrlHelper.buildJdbcUrl(info);

        assertTrue(url.contains("/test+db")); // 空格编码为 '+'
    }

    @Test
    @DisplayName("测试buildJdbcUrl方法 - 不含数据库名")
    void testBuildJdbcUrlNoDatabase() {
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo(
                "mysql", "localhost", 3306, null, Map.of()
        );

        String url = JdbcUrlHelper.buildJdbcUrl(info);

        assertTrue(url.endsWith("/"));
        assertFalse(url.contains("?"));
    }

    @Test
    @DisplayName("测试buildJdbcUrl方法 - 不含参数")
    void testBuildJdbcUrlNoParams() {
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo(
                "mysql", "localhost", 3306, "testdb", Map.of()
        );

        String url = JdbcUrlHelper.buildJdbcUrl(info);

        assertFalse(url.contains("?"));
    }

    // ==================== 综合测试 ====================

    @Test
    @DisplayName("综合测试 - 解析与构建往返")
    void testParseBuildRoundTrip() {
        String originalUrl = "jdbc:mysql://localhost:3306/personkit?useUnicode=true&characterEncoding=utf8&useSSL=false";

        // 解析
        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(originalUrl);

        // 构建
        String rebuiltUrl = JdbcUrlHelper.buildJdbcUrl(info);

        // 验证语义等价（参数顺序相同）
        assertEquals(originalUrl, rebuiltUrl);
    }

    @Test
    @DisplayName("综合测试 - 特殊字符编解码往返")
    void testSpecialCharactersRoundTrip() {
        String originalUrl = "jdbc:mysql://localhost:3306/test?name=%E5%BC%A0%E4%B8%89&msg=hello%20world";

        // 解析
        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(originalUrl);

        // 验证解析结果
        assertEquals("张三", info.getParameters().get("name"));
        assertEquals("hello world", info.getParameters().get("msg"));

        // 构建（注意：空格会被编码为 '+' 而非 '%20'）
        String rebuiltUrl = JdbcUrlHelper.buildJdbcUrl(info);

        // 验证构建结果包含正确编码
        assertTrue(rebuiltUrl.contains("name="));
        assertTrue(rebuiltUrl.contains("msg=hello+world"));

        // 再次解析验证往返一致性
        JdbcUrlHelper.ConnInfo info2 = JdbcUrlHelper.parseJdbcUrl(rebuiltUrl);
        assertEquals("张三", info2.getParameters().get("name"));
        assertEquals("hello world", info2.getParameters().get("msg"));
    }

    @Test
    @DisplayName("综合测试 - 复杂URL解析与重建")
    void testComplexUrlRoundTrip() {
        String originalUrl = "jdbc:mysql://192.168.1.100:3307/my%20database?param1=value%20one&param3=中文&serverTimezone=Asia/Shanghai";

        // 解析
        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(originalUrl);

        assertEquals("192.168.1.100", info.getHost());
        assertEquals(3307, info.getPort());
        assertEquals("my database", info.getDatabase());
        assertEquals("value one", info.getParameters().get("param1"));
//        assertEquals("", info.getParameters().get("param2"));
        assertEquals("中文", info.getParameters().get("param3"));
        assertEquals("Asia/Shanghai", info.getParameters().get("serverTimezone"));

        // 构建
        String rebuiltUrl = JdbcUrlHelper.buildJdbcUrl(info);

        // 再次解析验证
        JdbcUrlHelper.ConnInfo info2 = JdbcUrlHelper.parseJdbcUrl(rebuiltUrl);

        assertEquals(info.getHost(), info2.getHost());
        assertEquals(info.getPort(), info2.getPort());
        assertEquals(info.getDatabase(), info2.getDatabase());
        assertEquals(info.getParameters(), info2.getParameters());
    }

    // ==================== ConnInfo 实体测试 ====================


    @Test
    @DisplayName("测试JdbcConnectionInfo - 参数顺序保持")
    void testJdbcConnectionInfoParameterOrder() {
        String url = "jdbc:mysql://localhost:3306/testdb?a=1&b=2&c=3";

        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(url);

        // LinkedHashMap 保持插入顺序，遍历验证顺序
        String[] expectedKeys = {"a", "b", "c"};
        int idx = 0;
        for (String key : info.getParameters().keySet()) {
            assertEquals(expectedKeys[idx++], key);
        }
    }


    // ---------- 示例用法 ----------
    @Test
    @DisplayName("用法测试 - 参数顺序保持")
    void testAll() {
        String originalUrl = "jdbc:mysql://localhost:3306/personkit?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC&zeroDateTimeBehavior=CONVERT_TO_NULL";

        System.out.println("原始URL: " + originalUrl);
        System.out.println();

        // 解析
        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(originalUrl);
        System.out.println("解析结果: " + info);
        System.out.println();

        // 构建
        String rebuiltUrl = JdbcUrlHelper.buildJdbcUrl(info);
        System.out.println("重建URL: " + rebuiltUrl);
        System.out.println();

        // 构建
        rebuiltUrl = JdbcUrlHelper.buildJdbcUrl(info.enableBatch());
        System.out.println("enableBatch重建URL: " + rebuiltUrl);
        System.out.println();

        rebuiltUrl = JdbcUrlHelper.buildJdbcUrl(info.useBeijingTimeZone());
        System.out.println("useBeijingTimeZone重建URL: " + rebuiltUrl);
        System.out.println();

        // 验证是否等价（参数顺序可能相同，编码形式可能略有差异但语义一致）
        System.out.println("重建URL与原始URL" + (originalUrl.equals(rebuiltUrl) ? "完全相同" : "语义等价（参数值重新编码）"));
        System.out.println();

        // 测试含特殊字符的参数
        testSpecialCharacters();
    }

    private static void testSpecialCharacters() {
        String specialUrl = "jdbc:mysql://localhost:3306/test?name=%E5%BC%A0%E4%B8%89&msg=hello%20world&flag=true";
        System.out.println("含特殊字符的URL: " + specialUrl);
        JdbcUrlHelper.ConnInfo info = JdbcUrlHelper.parseJdbcUrl(specialUrl);
        System.out.println("解析后参数: " + info.getParameters());
        String rebuilt = JdbcUrlHelper.buildJdbcUrl(info);
        System.out.println("重建后URL: " + rebuilt);
        // 由于空格编码为'+'而原URL是'%20'，字符串不相同但语义一致
        System.out.println("重建URL中的空格被编码为 '+'，与原始 '%20' 语义相同，可被JDBC驱动正确解析。");
    }
}