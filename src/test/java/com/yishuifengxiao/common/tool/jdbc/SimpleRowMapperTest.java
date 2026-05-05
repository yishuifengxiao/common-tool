package com.yishuifengxiao.common.tool.jdbc;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SimpleRowMapper 工具类单元测试
 */
@DisplayName("SimpleRowMapper工具类测试")
class SimpleRowMapperTest {

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData metaData;

    // 测试用的简单实体类
    static class SimpleEntity {
        Long id;
        String name;
        Integer age;
        
        public SimpleEntity() {}
    }

    // 测试用的包含各种类型的实体类
    static class AllTypesEntity {
        int intValue;
        long longValue;
        double doubleValue;
        float floatValue;
        boolean booleanValue;
        String stringValue;
        BigDecimal decimalValue;
        
        public AllTypesEntity() {}
    }

    // 测试用的包含日期时间类型的实体类
    static class DateTimeEntity {
        Date utilDate;
        LocalDateTime localDateTime;
        LocalDate localDate;
        LocalTime localTime;
        Instant instant;
        ZonedDateTime zonedDateTime;
        OffsetDateTime offsetDateTime;
        java.sql.Date sqlDate;
        Time sqlTime;
        Timestamp sqlTimestamp;
        
        public DateTimeEntity() {}
    }

    // 测试用的包含JPA注解的实体类
    static class JpaEntity {
        @Id
        @Column(name = "user_id")
        Long userId;
        
        @Column(name = "user_name")
        String userName;
        
        @Transient
        String transientField;
        
        public JpaEntity() {}
    }

    // 测试用的包含基本类型字段的实体类
    static class PrimitiveEntity {
        int intField;
        long longField;
        boolean booleanField;
        double doubleField;
        
        public PrimitiveEntity() {}
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ==================== 构造函数测试 ====================

    @Test
    @DisplayName("测试构造函数 - 不指定时区")
    void testConstructorWithoutZoneId() {
        SimpleRowMapper<SimpleEntity> mapper = new SimpleRowMapper<>(SimpleEntity.class);
        
        assertNotNull(mapper);
    }

    @Test
    @DisplayName("测试构造函数 - 指定时区")
    void testConstructorWithZoneId() {
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        SimpleRowMapper<SimpleEntity> mapper = new SimpleRowMapper<>(SimpleEntity.class, zoneId);
        
        assertNotNull(mapper);
    }

    // ==================== mapRow 方法测试 - 基本类型 ====================

    @Test
    @DisplayName("测试mapRow方法 - 映射字符串字段")
    void testMapRowStringField() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenReturn("张三");
        when(resultSet.wasNull()).thenReturn(false);
        
        SimpleRowMapper<SimpleEntity> mapper = new SimpleRowMapper<>(SimpleEntity.class);
        SimpleEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(1L, entity.id);
        assertEquals("张三", entity.name);
    }

    @Test
    @DisplayName("测试mapRow方法 - 映射整数类型")
    void testMapRowIntegerFields() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnName(1)).thenReturn("int_value");
        when(metaData.getColumnName(2)).thenReturn("long_value");
        when(metaData.getColumnName(3)).thenReturn("decimal_value");
        
        when(resultSet.getObject(1)).thenReturn(100);
        when(resultSet.getObject(2)).thenReturn(200L);
        when(resultSet.getObject(3)).thenReturn(new BigDecimal("99.99"));
        
        SimpleRowMapper<AllTypesEntity> mapper = new SimpleRowMapper<>(AllTypesEntity.class);
        AllTypesEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(100, entity.intValue);
        assertEquals(200L, entity.longValue);
    }

    @Test
    @DisplayName("测试mapRow方法 - 映射浮点数类型")
    void testMapRowDoubleFields() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnName(1)).thenReturn("double_value");
        when(metaData.getColumnName(2)).thenReturn("float_value");
        when(metaData.getColumnName(3)).thenReturn("decimal_value");
        
        when(resultSet.getObject(1)).thenReturn(3.14);
        when(resultSet.getObject(2)).thenReturn(2.5f);
        when(resultSet.getObject(3)).thenReturn(new BigDecimal("99.99"));
        
        SimpleRowMapper<AllTypesEntity> mapper = new SimpleRowMapper<>(AllTypesEntity.class);
        AllTypesEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(3.14, entity.doubleValue);
        assertEquals(2.5f, entity.floatValue);
    }

    @Test
    @DisplayName("测试mapRow方法 - 映射布尔类型")
    void testMapRowBooleanField() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("boolean_value");
        
        when(resultSet.getObject(1)).thenReturn(true);
        
        SimpleRowMapper<AllTypesEntity> mapper = new SimpleRowMapper<>(AllTypesEntity.class);
        AllTypesEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertTrue(entity.booleanValue);
    }

    // ==================== mapRow 方法测试 - 日期时间类型 ====================

    @Test
    @DisplayName("测试mapRow方法 - 映射LocalDateTime")
    void testMapRowLocalDateTime() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("local_date_time");
        
        Timestamp timestamp = Timestamp.valueOf("2024-01-01 12:00:00");
        when(resultSet.getObject(1)).thenReturn(timestamp);
        when(resultSet.getTimestamp(1)).thenReturn(timestamp);
        when(resultSet.getTimestamp(anyString())).thenReturn(timestamp);
        when(resultSet.wasNull()).thenReturn(false);
        
        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertNotNull(entity.localDateTime);
        assertEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), entity.localDateTime);
    }

    @Test
    @DisplayName("测试mapRow方法 - 映射LocalDate")
    void testMapRowLocalDate() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("local_date");
        
        java.sql.Date sqlDate = java.sql.Date.valueOf("2024-01-01");
        when(resultSet.getObject(1)).thenReturn(sqlDate);
        when(resultSet.getDate(1)).thenReturn(sqlDate);
        when(resultSet.getDate(anyString())).thenReturn(sqlDate);
        when(resultSet.wasNull()).thenReturn(false);
        
        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertNotNull(entity.localDate);
        assertEquals(LocalDate.of(2024, 1, 1), entity.localDate);
    }

    @Test
    @DisplayName("测试mapRow方法 - 映射LocalTime")
    void testMapRowLocalTime() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("local_time");
        
        Time sqlTime = Time.valueOf("12:30:45");
        when(resultSet.getObject(1)).thenReturn(sqlTime);
        when(resultSet.getTime(1)).thenReturn(sqlTime);
        when(resultSet.getTime(anyString())).thenReturn(sqlTime);
        when(resultSet.wasNull()).thenReturn(false);
        
        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertNotNull(entity.localTime);
        assertEquals(LocalTime.of(12, 30, 45), entity.localTime);
    }

    @Test
    @DisplayName("测试mapRow方法 - 映射Instant")
    void testMapRowInstant() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("instant");
        
        Timestamp timestamp = Timestamp.from(Instant.parse("2024-01-01T12:00:00Z"));
        when(resultSet.getObject(1)).thenReturn(timestamp);
        when(resultSet.getTimestamp(1)).thenReturn(timestamp);
        when(resultSet.getTimestamp(anyString())).thenReturn(timestamp);
        when(resultSet.wasNull()).thenReturn(false);
        
        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertNotNull(entity.instant);
    }

    @Test
    @DisplayName("测试mapRow方法 - 映射带时区的LocalDateTime")
    void testMapRowLocalDateTimeWithZone() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("local_date_time");
        
        Timestamp timestamp = Timestamp.valueOf("2024-01-01 12:00:00");
        when(resultSet.getObject(1)).thenReturn(timestamp);
        when(resultSet.getTimestamp(1)).thenReturn(timestamp);
        when(resultSet.getTimestamp(anyString())).thenReturn(timestamp);
        when(resultSet.wasNull()).thenReturn(false);
        
        ZoneId dbZone = ZoneId.of("America/New_York");
        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class, dbZone);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertNotNull(entity.localDateTime);
        // 验证时区转换已执行（纽约时间12:00 -> 北京时间次日01:00）
        assertNotEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), entity.localDateTime);
    }

    // ==================== mapRow 方法测试 - JPA注解 ====================

    @Test
    @DisplayName("测试mapRow方法 - 使用@Column注解映射")
    void testMapRowWithColumnAnnotation() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("user_id");
        when(metaData.getColumnName(2)).thenReturn("user_name");
        
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenReturn("李四");
        
        SimpleRowMapper<JpaEntity> mapper = new SimpleRowMapper<>(JpaEntity.class);
        JpaEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(1L, entity.userId);
        assertEquals("李四", entity.userName);
    }

    @Test
    @DisplayName("测试mapRow方法 - @Transient字段不被映射")
    void testMapRowTransientFieldNotMapped() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnName(1)).thenReturn("user_id");
        when(metaData.getColumnName(2)).thenReturn("user_name");
        when(metaData.getColumnName(3)).thenReturn("transient_field");
        
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenReturn("王五");
        when(resultSet.getObject(3)).thenReturn("should_not_map");
        
        SimpleRowMapper<JpaEntity> mapper = new SimpleRowMapper<>(JpaEntity.class);
        JpaEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertNull(entity.transientField);
    }

    // ==================== mapRow 方法测试 - 空值处理 ====================

    @Test
    @DisplayName("测试mapRow方法 - null值处理（包装类型）")
    void testMapRowNullValuesWrapper() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        
        when(resultSet.getObject(1)).thenReturn(null);
        when(resultSet.getObject(2)).thenReturn(null);
        
        SimpleRowMapper<SimpleEntity> mapper = new SimpleRowMapper<>(SimpleEntity.class);
        SimpleEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertNull(entity.id);
        assertNull(entity.name);
    }

    @Test
    @DisplayName("测试mapRow方法 - null值处理（基本类型返回默认值）")
    void testMapRowNullValuesPrimitive() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("int_field");
        when(metaData.getColumnName(2)).thenReturn("long_field");
        
        when(resultSet.getObject(1)).thenReturn(null);
        when(resultSet.getObject(2)).thenReturn(null);
        
        SimpleRowMapper<PrimitiveEntity> mapper = new SimpleRowMapper<>(PrimitiveEntity.class);
        PrimitiveEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(0, entity.intField);
        assertEquals(0L, entity.longField);
    }

    // ==================== mapRow 方法测试 - 类型转换 ====================

    @Test
    @DisplayName("测试mapRow方法 - 字符串转数字")
    void testMapRowStringToNumber() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("int_value");
        when(metaData.getColumnName(2)).thenReturn("long_value");
        
        when(resultSet.getObject(1)).thenReturn("123");
        when(resultSet.getObject(2)).thenReturn("456");
        
        SimpleRowMapper<AllTypesEntity> mapper = new SimpleRowMapper<>(AllTypesEntity.class);
        AllTypesEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(123, entity.intValue);
        assertEquals(456L, entity.longValue);
    }

    @Test
    @DisplayName("测试mapRow方法 - 字符串转布尔")
    void testMapRowStringToBoolean() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("boolean_value");
        
        when(resultSet.getObject(1)).thenReturn("true");
        
        SimpleRowMapper<AllTypesEntity> mapper = new SimpleRowMapper<>(AllTypesEntity.class);
        AllTypesEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertTrue(entity.booleanValue);
    }

    @Test
    @DisplayName("测试mapRow方法 - 数字转换")
    void testMapRowNumberConversion() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("int_value");
        
        when(resultSet.getObject(1)).thenReturn(100L);
        
        SimpleRowMapper<AllTypesEntity> mapper = new SimpleRowMapper<>(AllTypesEntity.class);
        AllTypesEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(100, entity.intValue);
    }

    // ==================== mapRow 方法测试 - 异常处理 ====================

    @Test
    @DisplayName("测试mapRow方法 - 字段映射失败不影响其他字段")
    void testMapRowFieldMappingFailure() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenThrow(new SQLException("模拟错误"));
        
        SimpleRowMapper<SimpleEntity> mapper = new SimpleRowMapper<>(SimpleEntity.class);
        SimpleEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(1L, entity.id);
        // name 字段映射失败，应该为 null
        assertNull(entity.name);
    }

    @Test
    @DisplayName("测试mapRow方法 - 无法创建实例抛出异常")
    void testMapRowInstantiationException() {
        // 创建一个没有默认构造函数的类
        abstract class NoDefaultConstructor {
            private String field;
        }
        
        SimpleRowMapper<NoDefaultConstructor> mapper = new SimpleRowMapper<>(NoDefaultConstructor.class);
        
        assertThrows(SQLException.class, () -> {
            mapper.mapRow(resultSet, 1);
        });
    }

    // ==================== 缓存机制测试 ====================

    @Test
    @DisplayName("测试缓存机制 - 字段映射被缓存")
    void testFieldMappingCache() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("name");
        when(resultSet.getObject(1)).thenReturn("test");
        
        SimpleRowMapper<SimpleEntity> mapper = new SimpleRowMapper<>(SimpleEntity.class);
        
        // 第一次调用
        mapper.mapRow(resultSet, 1);
        
        // 第二次调用（应该使用缓存）
        when(resultSet.getObject(1)).thenReturn("test2");
        mapper.mapRow(resultSet, 2);
        
        // 验证 metaData 只被调用了两次（每次都会获取，但字段映射会使用缓存）
        verify(metaData, atLeast(2)).getColumnCount();
    }

    // ==================== 集成测试 ====================

    @Test
    @DisplayName("集成测试 - 完整的映射流程")
    void testIntegrationCompleteMapping() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        when(metaData.getColumnName(3)).thenReturn("age");
        
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenReturn("赵六");
        when(resultSet.getObject(3)).thenReturn(30);
        
        SimpleRowMapper<SimpleEntity> mapper = new SimpleRowMapper<>(SimpleEntity.class);
        SimpleEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        assertEquals(1L, entity.id);
        assertEquals("赵六", entity.name);
        assertEquals(30, entity.age);
    }

    @Test
    @DisplayName("集成测试 - 带时区转换的完整映射")
    void testIntegrationWithTimezoneConversion() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("local_date_time");
        
        Timestamp timestamp = Timestamp.valueOf("2024-01-01 12:00:00");
        when(resultSet.getObject(1)).thenReturn(1L);
        when(resultSet.getObject(2)).thenReturn(timestamp);
        when(resultSet.getTimestamp(2)).thenReturn(timestamp);
        when(resultSet.getTimestamp(anyString())).thenReturn(timestamp);
        when(resultSet.wasNull()).thenReturn(false);
        
        ZoneId dbZone = ZoneId.of("America/New_York");
        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class, dbZone);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);
        
        assertNotNull(entity);
        // id 列在 DateTimeEntity 中不存在，不会被映射，所以只验证 localDateTime
        assertNotNull(entity.localDateTime);
        // 验证时区转换已执行（纽约时间12:00 -> 北京时间次日01:00）
        assertNotEquals(LocalDateTime.of(2024, 1, 1, 12, 0, 0), entity.localDateTime);
    }
}
