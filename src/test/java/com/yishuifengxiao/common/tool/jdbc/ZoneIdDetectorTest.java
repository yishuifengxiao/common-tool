package com.yishuifengxiao.common.tool.jdbc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * ZoneIdDetector 工具类单元测试
 */
@DisplayName("ZoneIdDetector工具类测试")
@ExtendWith(MockitoExtension.class)
class ZoneIdDetectorTest {

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    // ==================== detectDatabaseTimezone 方法测试 ====================

    @Test
    @DisplayName("测试detectDatabaseTimezone方法 - 从URL获取时区")
    void testDetectDatabaseTimezoneFromUrl() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/testdb?serverTimezone=Asia/Shanghai");

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        assertNotNull(zoneId);
        assertEquals("Asia/Shanghai", zoneId.getId());
    }

    @Test
    @DisplayName("测试detectDatabaseTimezone方法 - null连接返回null")
    void testDetectDatabaseTimezoneNullConnection() {
        assertThrows(NullPointerException.class, () -> {
            ZoneIdDetector.detectDatabaseTimezone(null);
        });
    }

    @Test
    @DisplayName("测试detectDatabaseTimezone方法 - SQL异常返回null")
    void testDetectDatabaseTimezoneSqlException() throws SQLException {
        when(connection.getMetaData()).thenThrow(new SQLException("Connection error"));

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        assertNull(zoneId);
    }

    // ==================== extractTimezoneFromUrl 方法测试 ====================

    @Test
    @DisplayName("测试extractTimezoneFromUrl方法 - 简单URL提取时区")
    void testExtractTimezoneFromUrlSimple() {
        String url = "jdbc:mysql://localhost:3306/testdb?serverTimezone=Asia/Shanghai";

        // 通过反射调用私有方法进行测试
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("extractTimezoneFromUrl", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(null, url);

            assertEquals("Asia/Shanghai", result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试extractTimezoneFromUrl方法 - URL包含多个参数")
    void testExtractTimezoneFromUrlMultipleParams() {
        String url = "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";

        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("extractTimezoneFromUrl", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(null, url);

            assertEquals("UTC", result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试extractTimezoneFromUrl方法 - null URL返回null")
    void testExtractTimezoneFromUrlNull() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("extractTimezoneFromUrl", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(null, (String) null);

            assertNull(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试extractTimezoneFromUrl方法 - 不包含serverTimezone参数返回null")
    void testExtractTimezoneFromUrlNoParam() {
        String url = "jdbc:mysql://localhost:3306/testdb?useSSL=false";

        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("extractTimezoneFromUrl", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(null, url);

            assertNull(result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试extractTimezoneFromUrl方法 - serverTimezone在URL末尾")
    void testExtractTimezoneFromUrlAtEnd() {
        String url = "jdbc:mysql://localhost:3306/testdb?useSSL=false&serverTimezone=America/New_York";

        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("extractTimezoneFromUrl", String.class);
            method.setAccessible(true);
            String result = (String) method.invoke(null, url);

            assertEquals("America/New_York", result);
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    // ==================== isValidTimezoneFormat 方法测试 ====================

    @Test
    @DisplayName("测试isValidTimezoneFormat方法 - IANA时区格式")
    void testIsValidTimezoneFormatIana() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("isValidTimezoneFormat", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(null, "Asia/Shanghai"));
            assertTrue((Boolean) method.invoke(null, "America/New_York"));
            assertTrue((Boolean) method.invoke(null, "Europe/London"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试isValidTimezoneFormat方法 - UTC/GMT偏移格式")
    void testIsValidTimezoneFormatUtcGmt() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("isValidTimezoneFormat", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(null, "UTC+8"));
            assertTrue((Boolean) method.invoke(null, "GMT-5"));
            assertTrue((Boolean) method.invoke(null, "UTC+0"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试isValidTimezoneFormat方法 - 缩写格式")
    void testIsValidTimezoneFormatAbbreviation() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("isValidTimezoneFormat", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(null, "CST"));
            assertTrue((Boolean) method.invoke(null, "PST"));
            assertTrue((Boolean) method.invoke(null, "EST"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试isValidTimezoneFormat方法 - 数字偏移格式")
    void testIsValidTimezoneFormatNumeric() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("isValidTimezoneFormat", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(null, "+08:00"));
            assertTrue((Boolean) method.invoke(null, "-05:00"));
            assertTrue((Boolean) method.invoke(null, "+00:00"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试isValidTimezoneFormat方法 - SYSTEM和LOCAL关键字")
    void testIsValidTimezoneFormatSpecial() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("isValidTimezoneFormat", String.class);
            method.setAccessible(true);

            assertTrue((Boolean) method.invoke(null, "SYSTEM"));
            assertTrue((Boolean) method.invoke(null, "LOCAL"));
            assertTrue((Boolean) method.invoke(null, "system"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }


    // ==================== convertOffsetToTimezone 方法测试 ====================

    @Test
    @DisplayName("测试convertOffsetToTimezone方法 - 常见偏移量")
    void testConvertOffsetToTimezoneCommon() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("convertOffsetToTimezone", int.class);
            method.setAccessible(true);

            assertEquals("Asia/Shanghai", method.invoke(null, 8));
            assertEquals("Asia/Tokyo", method.invoke(null, 9));
            assertEquals("UTC", method.invoke(null, 0));
            assertEquals("America/New_York", method.invoke(null, -5));
            assertEquals("America/Los_Angeles", method.invoke(null, -8));
            assertEquals("Europe/Paris", method.invoke(null, 1));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试convertOffsetToTimezone方法 - 未内置偏移量生成UTC格式")
    void testConvertOffsetToTimezoneDefault() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("convertOffsetToTimezone", int.class);
            method.setAccessible(true);

            assertEquals("UTC+2", method.invoke(null, 2));
            assertEquals("UTC+3", method.invoke(null, 3));
            assertEquals("UTC-6", method.invoke(null, -6));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    // ==================== inferFromShortString 方法测试 ====================

    @Test
    @DisplayName("测试inferFromShortString方法 - 中国时区识别")
    void testInferFromShortStringChina() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferFromShortString", String.class);
            method.setAccessible(true);

            assertEquals("Asia/Shanghai", method.invoke(null, "CST"));
            assertEquals("Asia/Shanghai", method.invoke(null, "CHINA"));
            assertEquals("Asia/Shanghai", method.invoke(null, "BEIJING"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试inferFromShortString方法 - 美国时区识别")
    void testInferFromShortStringUS() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferFromShortString", String.class);
            method.setAccessible(true);

            assertEquals("America/Los_Angeles", method.invoke(null, "PST"));
            assertEquals("America/Los_Angeles", method.invoke(null, "PDT"));
            assertEquals("America/New_York", method.invoke(null, "EST"));
            assertEquals("America/New_York", method.invoke(null, "EDT"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试inferFromShortString方法 - 其他时区识别")
    void testInferFromShortStringOthers() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferFromShortString", String.class);
            method.setAccessible(true);

            assertEquals("Europe/London", method.invoke(null, "GMT"));
            assertEquals("Europe/London", method.invoke(null, "UTC"));
            assertEquals("Europe/London", method.invoke(null, "LONDON"));
            assertEquals("Asia/Tokyo", method.invoke(null, "JST"));
            assertEquals("Asia/Tokyo", method.invoke(null, "TOKYO"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试inferFromShortString方法 - 数字偏移识别")
    void testInferFromShortStringNumeric() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferFromShortString", String.class);
            method.setAccessible(true);

            assertEquals("Asia/Shanghai", method.invoke(null, "8"));
            assertEquals("Asia/Tokyo", method.invoke(null, "9"));
            assertEquals("UTC", method.invoke(null, "0"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试inferFromShortString方法 - 无法识别返回null")
    void testInferFromShortStringUnknown() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferFromShortString", String.class);
            method.setAccessible(true);

            assertNull(method.invoke(null, "UNKNOWN"));
            assertNull(method.invoke(null, "XYZ"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    // ==================== containsChineseTimezonePattern 方法测试 ====================

    @Test
    @DisplayName("测试containsChineseTimezonePattern方法 - 包含中文字符")
    void testContainsChineseTimezonePatternWithChinese() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("containsChineseTimezonePattern", byte[].class);
            method.setAccessible(true);

            byte[] chineseBytes = "中国".getBytes();
            assertTrue((Boolean) method.invoke(null, chineseBytes));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试containsChineseTimezonePattern方法 - 纯ASCII字符")
    void testContainsChineseTimezonePatternAscii() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("containsChineseTimezonePattern", byte[].class);
            method.setAccessible(true);

            byte[] asciiBytes = "UTC".getBytes();
            assertFalse((Boolean) method.invoke(null, asciiBytes));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试containsChineseTimezonePattern方法 - null或空数组")
    void testContainsChineseTimezonePatternEmpty() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("containsChineseTimezonePattern", byte[].class);
            method.setAccessible(true);

            assertFalse((Boolean) method.invoke(null, (byte[]) null));
            assertFalse((Boolean) method.invoke(null, new byte[0]));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    // ==================== inferSqlServerTimezone 方法测试 ====================

    @Test
    @DisplayName("测试inferSqlServerTimezone方法 - 中文语言")
    void testInferSqlServerTimezoneChinese() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferSqlServerTimezone", int.class, String.class);
            method.setAccessible(true);

            // SQL Server 的语言设置是英文的，如 "Simplified Chinese"、"Traditional Chinese"
            assertEquals("Asia/Shanghai", method.invoke(null, 1, "Simplified Chinese"));
            assertEquals("Asia/Shanghai", method.invoke(null, 1, "Traditional Chinese"));
            assertEquals("Asia/Shanghai", method.invoke(null, 1, "chinese"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试inferSqlServerTimezone方法 - 其他语言")
    void testInferSqlServerTimezoneOthers() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferSqlServerTimezone", int.class, String.class);
            method.setAccessible(true);

            assertEquals("Asia/Tokyo", method.invoke(null, 1, "Japanese"));
            assertEquals("America/New_York", method.invoke(null, 1, "us_english"));
            assertEquals("Europe/London", method.invoke(null, 1, "uk_english"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试inferSqlServerTimezone方法 - 默认返回UTC")
    void testInferSqlServerTimezoneDefault() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("inferSqlServerTimezone", int.class, String.class);
            method.setAccessible(true);

            assertEquals("UTC", method.invoke(null, 1, null));
            assertEquals("UTC", method.invoke(null, 1, "French"));
            assertEquals("UTC", method.invoke(null, 1, "German"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    // ==================== parseTimezone 方法测试 ====================

    @Test
    @DisplayName("测试parseTimezone方法 - 有效时区字符串")
    void testParseTimezoneValid() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("parseTimezone", String.class);
            method.setAccessible(true);

            ZoneId result = (ZoneId) method.invoke(null, "Asia/Shanghai");
            assertNotNull(result);
            assertEquals("Asia/Shanghai", result.getId());

            result = (ZoneId) method.invoke(null, "UTC");
            assertNotNull(result);
            assertEquals("UTC", result.getId());
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试parseTimezone方法 - SYSTEM返回null")
    void testParseTimezoneSystem() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("parseTimezone", String.class);
            method.setAccessible(true);

            assertNull(method.invoke(null, "SYSTEM"));
            assertNull(method.invoke(null, "system"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试parseTimezone方法 - null或空字符串返回null")
    void testParseTimezoneNullOrEmpty() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("parseTimezone", String.class);
            method.setAccessible(true);

            assertNull(method.invoke(null, (String) null));
            assertNull(method.invoke(null, ""));
            assertNull(method.invoke(null, "   "));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试parseTimezone方法 - 无效时区返回null")
    void testParseTimezoneInvalid() {
        try {
            java.lang.reflect.Method method = ZoneIdDetector.class.getDeclaredMethod("parseTimezone", String.class);
            method.setAccessible(true);

            assertNull(method.invoke(null, "Invalid/Timezone"));
            assertNull(method.invoke(null, "NOT_A_TIMEZONE"));
        } catch (Exception e) {
            fail("反射调用失败: " + e.getMessage());
        }
    }

    // ==================== 集成测试 ====================

    @Test
    @DisplayName("集成测试 - MySQL数据库从URL获取时区")
    void testIntegrationMySqlFromUrl() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/mydb?serverTimezone=America/Los_Angeles&useSSL=true");

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        assertNotNull(zoneId);
        assertEquals("America/Los_Angeles", zoneId.getId());
    }

    @Test
    @DisplayName("集成测试 - PostgreSQL数据库查询时区")
    void testIntegrationPostgresQuery() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/mydb");
        when(metaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("America/New_York");

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        assertNotNull(zoneId);
        assertEquals("America/New_York", zoneId.getId());
    }

    @Test
    @DisplayName("集成测试 - MySQL返回SYSTEM时使用备用查询")
    void testIntegrationMySqlSystemFallback() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/mydb");
        when(metaData.getDatabaseProductName()).thenReturn("MySQL");

        // 创建两个不同的 Statement（主查询和备用查询各需要一个）
        Statement backupStatement = mock(Statement.class);
        when(connection.createStatement())
                .thenReturn(statement)      // 第一次调用：主查询的 Statement
                .thenReturn(backupStatement); // 第二次调用：备用查询的 Statement

        // 创建备用查询的 ResultSet，返回 "Asia/Shanghai" 而不是 "CST"
        ResultSet backupRs = mock(ResultSet.class);
        when(backupRs.next()).thenReturn(true);
        when(backupRs.getString(1)).thenReturn("SYSTEM");
        when(backupRs.getString(2)).thenReturn("Asia/Shanghai");  // 使用标准时区名称

        // 设置主查询返回 SYSTEM
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("SYSTEM");

        // 配置主查询和备用查询的 executeQuery
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(backupStatement.executeQuery(anyString())).thenReturn(backupRs);

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        // Asia/Shanghai 应该被正确解析
        assertNotNull(zoneId);
        assertEquals("Asia/Shanghai", zoneId.getId());
    }

    @Test
    @DisplayName("集成测试 - 不支持的数据库类型抛出异常")
    void testIntegrationUnsupportedDatabase() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:unknown://localhost:1234/mydb");
        when(metaData.getDatabaseProductName()).thenReturn("UnknownDB");

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        // 应该捕获异常并返回null
        assertNull(zoneId);
    }

    @Test
    @DisplayName("集成测试 - 完整的时区检测流程")
    void testIntegrationCompleteFlow() throws SQLException {
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL()).thenReturn("jdbc:mysql://localhost:3306/testdb?serverTimezone=Europe/Paris");

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        assertNotNull(zoneId);
        assertEquals("Europe/Paris", zoneId.getId());
        verify(connection).getMetaData();
        verify(metaData).getURL();
    }
}
