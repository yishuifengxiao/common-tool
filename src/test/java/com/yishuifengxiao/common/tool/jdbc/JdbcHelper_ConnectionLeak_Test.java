package com.yishuifengxiao.common.tool.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * JdbcHelper 连接泄露防护测试
 *
 * <p>验证 initialize() 中获取的数据库连接被正确关闭（try-with-resources），
 * 确保修复后的连接泄露 Bug 不会再次出现。</p>
 */
@ExtendWith(MockitoExtension.class)
class JdbcHelper_ConnectionLeak_Test {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    /**
     * 验证正常初始化后连接被关闭
     *
     * <p>修复前：Connection 获取后从未关闭，每次初始化泄露一个连接</p>
     * <p>修复后：使用 try-with-resources 自动关闭</p>
     */
    @Test
    void testConnectionClosedAfterInit() throws Exception {
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        try (MockedStatic<ZoneIdDetector> mocked = Mockito.mockStatic(ZoneIdDetector.class)) {
            mocked.when(() -> ZoneIdDetector.detectDatabaseTimezone(any(Connection.class)))
                    .thenReturn(ZoneId.systemDefault());

            // 构造 JdbcHelper，触发 initialize()
            new JdbcHelper(jdbcTemplate);
        }

        // 核心断言：连接必须被关闭
        verify(connection, times(1)).close();
    }

    /**
     * 验证即使 detectDatabaseTimezone 抛异常，连接仍然被关闭
     *
     * <p>try-with-resources 保证即使内部代码抛异常，连接也会被关闭</p>
     */
    @Test
    void testConnectionClosedEvenOnError() throws Exception {
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        try (MockedStatic<ZoneIdDetector> mocked = Mockito.mockStatic(ZoneIdDetector.class)) {
            mocked.when(() -> ZoneIdDetector.detectDatabaseTimezone(any(Connection.class)))
                    .thenThrow(new SQLException("simulated DB error"));

            // initialize() 内部 catch 了 Exception，不会向外抛出
            // 但会抛出 UncheckedException
            assertThrows(Exception.class, () -> new JdbcHelper(jdbcTemplate));
        }

        // 核心断言：即使发生异常，连接也必须被关闭
        verify(connection, times(1)).close();
    }

    /**
     * 验证 JdbcTemplate 为 null 时不获取连接
     */
    @Test
    void testNoConnectionWhenJdbcTemplateNull() {
        // 使用默认构造函数，jdbcTemplate 为 null
        JdbcHelper helper = new JdbcHelper();

        // 实例创建成功，但不应获取连接
        assertNotNull(helper);

        // dataSource 和 connection 从未被 mock，不会调用 getConnection()
    }

    /**
     * 验证多次初始化不会泄露多个连接
     *
     * <p>模拟连续创建3个 JdbcHelper，每个都应正确关闭各自的连接</p>
     */
    @Test
    void testNoLeakAcrossMultipleInit() throws Exception {
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        try (MockedStatic<ZoneIdDetector> mocked = Mockito.mockStatic(ZoneIdDetector.class)) {
            mocked.when(() -> ZoneIdDetector.detectDatabaseTimezone(any(Connection.class)))
                    .thenReturn(ZoneId.systemDefault());

            // 连续创建3个实例
            new JdbcHelper(jdbcTemplate);
            new JdbcHelper(jdbcTemplate);
            new JdbcHelper(jdbcTemplate);
        }

        // 核心断言：每个连接都被关闭了3次（每次初始化1次）
        verify(connection, times(3)).close();
    }

    /**
     * 验证 getConnection 只被调用一次
     *
     * <p>确保没有重复获取连接的冗余调用</p>
     */
    @Test
    void testGetConnectionCalledExactlyOnce() throws Exception {
        when(jdbcTemplate.getDataSource()).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);

        try (MockedStatic<ZoneIdDetector> mocked = Mockito.mockStatic(ZoneIdDetector.class)) {
            mocked.when(() -> ZoneIdDetector.detectDatabaseTimezone(any(Connection.class)))
                    .thenReturn(ZoneId.systemDefault());

            new JdbcHelper(jdbcTemplate);
        }

        // 核心断言：getConnection 只被调用1次
        verify(dataSource, times(1)).getConnection();
    }
}
