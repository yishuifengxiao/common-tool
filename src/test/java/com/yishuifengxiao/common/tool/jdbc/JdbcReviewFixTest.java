package com.yishuifengxiao.common.tool.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * jdbc 工具类 P0/P1 问题修复的回归测试
 *
 * <p>覆盖以下历史缺陷，防止修复被回退：</p>
 * <ul>
 *   <li>useDefaultTimeZone 生成非法 serverTimezone 参数</li>
 *   <li>ZoneIdDetector 未对 URL 中编码的时区解码、时区缩写无法归一化</li>
 *   <li>SimpleRowMapper 列名与字段名不一致时映射静默失败</li>
 *   <li>PojoGenerator getter/setter 类型与字段类型不一致</li>
 *   <li>PojoGenerator 关闭 JPA 后仍生成 @Id/@GeneratedValue</li>
 * </ul>
 */
@DisplayName("jdbc工具类修复回归测试")
class JdbcReviewFixTest {

    private static final String POJO_GENERATOR = "com.yishuifengxiao.common.tool.jdbc.PojoGenerator";

    // ==================== JdbcUrlHelper ====================

    @Test
    @DisplayName("useDefaultTimeZone 不应产生非法的 serverTimezone 参数值")
    void testUseDefaultTimeZone() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("serverTimezone", "Asia/Shanghai");
        parameters.put("connectionTimeZone", "Asia/Shanghai");
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo("mysql", "localhost", 3306, "testdb", parameters);

        info.useDefaultTimeZone();

        assertNull(info.getParameters().get("serverTimezone"));
        assertNull(info.getParameters().get("connectionTimeZone"));
        assertEquals("true", info.getParameters().get("useTimezone"));
        String url = JdbcUrlHelper.buildJdbcUrl(info);
        assertFalse(url.contains("serverTimezone=useTimezone"));
    }

    // ==================== ZoneIdDetector ====================

    @Test
    @DisplayName("URL 中被编码的 serverTimezone 应能正确解码")
    void testTimezoneFromEncodedUrl() throws Exception {
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(metaData);
        when(metaData.getURL())
                .thenReturn("jdbc:mysql://localhost:3306/testdb?serverTimezone=Asia%2FShanghai&useSSL=false");

        ZoneId zoneId = ZoneIdDetector.detectDatabaseTimezone(connection);

        assertEquals(ZoneId.of("Asia/Shanghai"), zoneId);
    }

    @Test
    @DisplayName("时区缩写 CST 应归一化为 Asia/Shanghai")
    void testParseCstTimezone() throws Exception {
        Method parseTimezone = ZoneIdDetector.class.getDeclaredMethod("parseTimezone", String.class);
        parseTimezone.setAccessible(true);

        assertEquals(ZoneId.of("Asia/Shanghai"), parseTimezone.invoke(null, "CST"));
    }

    // ==================== SimpleRowMapper ====================

    @Test
    @DisplayName("驼峰列名无 @Column 注解时应按字段名兜底映射")
    void testColumnNameFallback() throws Exception {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn("createTime");
        when(metaData.getColumnName(1)).thenReturn("createTime");
        when(resultSet.getObject(1)).thenReturn("2024-01-01 10:00:00");

        CamelEntity entity = new SimpleRowMapper<>(CamelEntity.class).mapRow(resultSet, 1);

        assertEquals("2024-01-01 10:00:00", entity.getCreateTime());
    }

    // ==================== PojoGenerator ====================

    @Test
    @DisplayName("关闭 Java8 日期后 getter/setter 应与字段类型一致")
    void testGetterSetterTypeConsistency() throws Exception {
        PojoGenerator generator = new PojoGenerator();
        Object column = newInnerInstance("ColumnInfo");
        setField(column, "columnName", "create_time");
        setField(column, "dataType", "datetime");
        Object codeBuilder = newInnerInstance("CodeBuilder");

        PojoGenerator.GeneratorConfig config = new PojoGenerator.GeneratorConfig();
        config.setUseJava8Date(false);

        Method method = PojoGenerator.class.getDeclaredMethod("generateGetterSetter",
                innerClass("CodeBuilder"), innerClass("ColumnInfo"), PojoGenerator.GeneratorConfig.class);
        method.setAccessible(true);
        method.invoke(generator, codeBuilder, column, config);

        String code = codeBuilder.toString();
        assertTrue(code.contains("public Date getCreateTime()"));
        assertTrue(code.contains("public void setCreateTime(Date createTime)"));
        assertFalse(code.contains("LocalDateTime"));
    }

    @Test
    @DisplayName("关闭 JPA 后不应生成 @Id/@GeneratedValue")
    void testNoJpaAnnotationWhenJpaDisabled() throws Exception {
        PojoGenerator generator = new PojoGenerator();
        Object column = newInnerInstance("ColumnInfo");
        setField(column, "columnName", "id");
        setField(column, "dataType", "bigint");

        Object primaryKey = newInnerInstance("PrimaryKeyInfo");
        setField(primaryKey, "columns", new ArrayList<>(List.of("id")));
        setField(primaryKey, "autoIncrement", true);

        Object table = newInnerInstance("TableInfo");
        setField(table, "primaryKey", primaryKey);
        setField(table, "columns", new ArrayList<>(List.of(column)));

        Object codeBuilder = newInnerInstance("CodeBuilder");
        PojoGenerator.GeneratorConfig config = new PojoGenerator.GeneratorConfig();
        config.setUseJpa(false);

        Method method = PojoGenerator.class.getDeclaredMethod("generateField",
                innerClass("CodeBuilder"), innerClass("ColumnInfo"), innerClass("TableInfo"),
                PojoGenerator.GeneratorConfig.class);
        method.setAccessible(true);
        method.invoke(generator, codeBuilder, column, table, config);

        String code = codeBuilder.toString();
        assertFalse(code.contains("@Id"));
        assertFalse(code.contains("@GeneratedValue"));
        assertTrue(code.contains("private Long id;"));
    }

    /**
     * 创建 PojoGenerator 的私有静态内部类实例
     */
    private static Object newInnerInstance(String simpleName) throws Exception {
        Constructor<?> constructor = innerClass(simpleName).getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    /**
     * 获取 PojoGenerator 的私有静态内部类
     */
    private static Class<?> innerClass(String simpleName) throws ClassNotFoundException {
        return Class.forName(POJO_GENERATOR + "$" + simpleName);
    }

    /**
     * 通过反射设置私有内部类字段
     */
    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    /**
     * 测试用实体：字段为驼峰，数据库列名也是驼峰
     */
    public static class CamelEntity {
        private String createTime;

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }
    }
}
