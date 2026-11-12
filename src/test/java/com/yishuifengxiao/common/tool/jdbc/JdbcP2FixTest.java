package com.yishuifengxiao.common.tool.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * jdbc 工具类 P2 问题修复的回归测试
 *
 * <p>覆盖以下历史缺陷，防止修复被回退：</p>
 * <ul>
 *   <li>PojoGenerator 下划线转驼峰时压平已有驼峰命名</li>
 *   <li>PojoGenerator BLOB/POINT/INTERVAL/tinyint(1) 类型映射错误</li>
 *   <li>JdbcUrlHelper 丢弃空字符串参数、多主机/负载均衡URL提示不明确</li>
 *   <li>JdbcHelper INSERT 写入null自增主键、UPDATE将主键写入SET、DELETE保留null条件</li>
 *   <li>JdbcHelper.Result 返回共享的静态KeyHolder（线程安全问题）</li>
 * </ul>
 */
@DisplayName("jdbc工具类P2修复回归测试")
class JdbcP2FixTest {

    private static final String POJO_GENERATOR = "com.yishuifengxiao.common.tool.jdbc.PojoGenerator";

    // ==================== PojoGenerator ====================

    @Test
    @DisplayName("已有驼峰命名转驼峰时不应被压平")
    void testToCamelCaseKeepsCamelCase() throws Exception {
        PojoGenerator generator = new PojoGenerator();
        Method toCamelCase = PojoGenerator.class.getDeclaredMethod("toCamelCase", String.class, boolean.class);
        toCamelCase.setAccessible(true);

        assertEquals("createTime", toCamelCase.invoke(generator, "createTime", false));
        assertEquals("createTime", toCamelCase.invoke(generator, "CREATE_TIME", false));
        assertEquals("userName", toCamelCase.invoke(generator, "USER_NAME", false));
        assertEquals("UserInfo", toCamelCase.invoke(generator, "user_info", true));
    }

    @Test
    @DisplayName("BLOB/POINT/INTERVAL/tinyint(1) 应映射为正确的Java类型")
    void testJavaTypeMapping() throws Exception {
        PojoGenerator generator = new PojoGenerator();
        PojoGenerator.GeneratorConfig config = new PojoGenerator.GeneratorConfig();
        Method getJavaType = PojoGenerator.class.getDeclaredMethod("getJavaType",
                innerClass("ColumnInfo"), PojoGenerator.GeneratorConfig.class);
        getJavaType.setAccessible(true);

        assertEquals("byte[]", getJavaType.invoke(generator, column("blob", 0), config));
        assertEquals("String", getJavaType.invoke(generator, column("point", 0), config));
        assertEquals("String", getJavaType.invoke(generator, column("interval", 0), config));
        assertEquals("Boolean", getJavaType.invoke(generator, column("tinyint", 1), config));
        assertEquals("Integer", getJavaType.invoke(generator, column("tinyint", 3), config));
        assertEquals("Integer", getJavaType.invoke(generator, column("INT UNSIGNED", 0), config));
    }

    // ==================== JdbcUrlHelper ====================

    @Test
    @DisplayName("空字符串参数值应保留在JDBC URL中")
    void testBuildUrlKeepsEmptyValue() {
        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("useSSL", "");
        JdbcUrlHelper.ConnInfo info = new JdbcUrlHelper.ConnInfo("mysql", "localhost", 3306, "testdb", parameters);

        String url = JdbcUrlHelper.buildJdbcUrl(info);

        assertTrue(url.contains("useSSL="));
    }

    @Test
    @DisplayName("负载均衡与多主机格式的JDBC URL应给出明确提示")
    void testParseUnsupportedUrl() {
        IllegalArgumentException loadBalance = assertThrows(IllegalArgumentException.class,
                () -> JdbcUrlHelper.parseJdbcUrl("jdbc:mysql:loadbalance://127.0.0.1:3306,127.0.0.2:3306/db"));
        assertTrue(loadBalance.getMessage().contains("负载均衡"));

        IllegalArgumentException multiHost = assertThrows(IllegalArgumentException.class,
                () -> JdbcUrlHelper.parseJdbcUrl("jdbc:mysql://127.0.0.1:3306,127.0.0.2:3306/db"));
        assertTrue(multiHost.getMessage().contains("多主机"));
    }

    // ==================== JdbcHelper ====================

    @Test
    @DisplayName("INSERT 不应写入值为null的自增主键")
    void testInsertSkipsNullPrimaryKey() throws Exception {
        NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        when(template.update(anyString(), any(), any(KeyHolder.class))).thenReturn(1);
        JdbcHelper helper = helperWith(template);

        IdEntity entity = new IdEntity();
        entity.setName("abc");
        helper.insert(entity);

        String sql = captureUpdateSql(template);
        assertTrue(sql.contains("(`name`"));
        assertFalse(sql.contains("`id`"));
    }

    @Test
    @DisplayName("UPDATE 不应把主键写入SET子句")
    void testUpdateExcludesPrimaryKeyFromSet() throws Exception {
        NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        when(template.update(anyString(), any(), any(KeyHolder.class))).thenReturn(1);
        JdbcHelper helper = helperWith(template);

        IdEntity entity = new IdEntity();
        entity.setId(1L);
        entity.setName("abc");
        JdbcHelper.Result result = helper.updateByPrimaryKey(entity);

        assertTrue(captureUpdateSql(template).contains("SET `name` = :name WHERE `id` = :id"));
        assertNotNull(result.getKeyHolder().getKeyList().get(0));
    }

    @Test
    @DisplayName("选择性更新无有效字段时不应执行SQL")
    void testSelectiveUpdateWithOnlyPrimaryKey() throws Exception {
        NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        JdbcHelper helper = helperWith(template);

        IdEntity entity = new IdEntity();
        entity.setId(1L);
        JdbcHelper.Result result = helper.updateByPrimaryKeySelective(entity);

        assertEquals(0, result.getAffectedRows());
        verify(template, never()).update(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class),
                any(KeyHolder.class));
    }

    @Test
    @DisplayName("DELETE 应过滤null与空白主键，避免生成 col = NULL")
    void testDeleteFiltersInvalidKeys() throws Exception {
        NamedParameterJdbcTemplate template = mock(NamedParameterJdbcTemplate.class);
        when(template.update(anyString(), any(), any(KeyHolder.class))).thenReturn(1);
        JdbcHelper helper = helperWith(template);

        helper.deleteByPrimaryKey(IdEntity.class, null, "2", "   ");

        String sql = captureUpdateSql(template);
        assertEquals(1, sql.chars().filter(ch -> ch == '=').count());
        assertFalse(sql.toUpperCase().contains("NULL"));
        assertFalse(sql.contains(" OR "));
    }

    @Test
    @DisplayName("Result 未持有时应返回非null且互不共享的KeyHolder")
    void testResultKeyHolderNullSafe() {
        JdbcHelper.Result first = new JdbcHelper.Result(null, 0, null);
        JdbcHelper.Result second = new JdbcHelper.Result(null, 0, null);

        assertNotNull(first.getKeyHolder());
        assertNotNull(first.keyHolder().getKeyList());
        assertTrue(first.getKeyHolder() != second.getKeyHolder());
    }

    // ==================== 测试辅助 ====================

    /**
     * 构建注入了mock模板的JdbcHelper
     */
    private static JdbcHelper helperWith(NamedParameterJdbcTemplate template) throws Exception {
        JdbcHelper helper = new JdbcHelper();
        Field field = JdbcHelper.class.getDeclaredField("namedParameterJdbcTemplate");
        field.setAccessible(true);
        field.set(helper, template);
        return helper;
    }

    /**
     * 捕获JdbcHelper实际执行的更新SQL
     */
    private static String captureUpdateSql(NamedParameterJdbcTemplate template) {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(template).update(captor.capture(), any(), any(KeyHolder.class));
        return captor.getValue();
    }

    /**
     * 构建指定类型名与长度的列信息
     */
    private static Object column(String dataType, int columnSize) throws Exception {
        Object column = newInnerInstance("ColumnInfo");
        setField(column, "columnName", "test_column");
        setField(column, "dataType", dataType);
        setField(column, "columnSize", columnSize);
        return column;
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
     * 测试用实体
     */
    public static class IdEntity {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
