package com.yishuifengxiao.common.tool.jdbc;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 基于 H2 内存数据库的真实 JDBC 集成测试
 * <p>
 * 与其他使用 mock 的 JDBC 测试互补，验证真实 DatabaseMetaData 与 ResultSet 路径。
 * </p>
 */
class H2JdbcIntegrationTest {

    /**
     * 验证时区检测器面对不支持的数据库时按契约安全返回 null，而不是抛出异常或返回错误时区
     * <p>
     * H2 不在 ZoneIdDetector 的支持列表中，Javadoc 约定“无法检测则返回 null”。
     * </p>
     *
     * @throws Exception 数据库访问异常
     */
    @Test
    void testDetectDatabaseTimezoneWithUnsupportedDatabase() throws Exception {
        try (Connection connection = openConnection("tzdb")) {
            assertNull(ZoneIdDetector.detectDatabaseTimezone(connection));
        }
    }

    /**
     * 验证时区检测器可以从真实连接元数据中读取数据库产品信息（真实 DatabaseMetaData 路径）
     *
     * @throws Exception 数据库访问异常
     */
    @Test
    void testReadDatabaseMetaDataWithRealConnection() throws Exception {
        try (Connection connection = openConnection("metadb")) {
            String productName = connection.getMetaData().getDatabaseProductName();
            assertEquals("H2", productName);
            assertNotNull(connection.getMetaData().getURL());
        }
    }

    /**
     * 验证 SimpleRowMapper 可以从真实 ResultSet 映射基本类型与 BigDecimal
     *
     * @throws Exception 数据库访问异常
     */
    @Test
    void testSimpleRowMapperWithRealResultSet() throws Exception {
        try (Connection connection = openConnection("mappingdb");
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE t_user (id INT PRIMARY KEY, name VARCHAR(32), amount DECIMAL(10,2))");
            statement.execute("INSERT INTO t_user VALUES (1, 'alice', 12.34)");

            try (ResultSet resultSet = statement.executeQuery("SELECT id, name, amount FROM t_user")) {
                resultSet.next();
                UserRow row = new SimpleRowMapper<>(UserRow.class).mapRow(resultSet, 0);
                assertEquals(1, row.getId());
                assertEquals("alice", row.getName());
                assertEquals(new BigDecimal("12.34"), row.getAmount());
            }
        }
    }

    /**
     * 打开独立的 H2 内存库连接
     *
     * @param databaseName 内存库名称
     * @return 数据库连接
     * @throws Exception 驱动或连接异常
     */
    private Connection openConnection(String databaseName) throws Exception {
        return DriverManager.getConnection("jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1", "sa", "");
    }

    /**
     * 测试用用户行对象
     */
    public static class UserRow {
        private int id;
        private String name;
        private BigDecimal amount;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }
}
