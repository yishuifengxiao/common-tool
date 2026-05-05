package com.yishuifengxiao.common.tool.jdbc;

import com.yishuifengxiao.common.tool.bean.ClassUtil;
import com.yishuifengxiao.common.tool.text.TextUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库结果集映射器，用于将ResultSet转换为Java对象
 * 支持基本类型、日期时间类型的自动转换和时区处理
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class SimpleRowMapper<T> implements RowMapper<T> {
    private static final String LOG_PREFIX = "【yishuifengxiao】";
    private static final Map<Class<?>, Map<String, FieldMapping>> FIELD_MAPPING_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = Map.of(
            int.class, 0, long.class, 0L, double.class, 0.0, float.class, 0.0f,
            boolean.class, false, byte.class, (byte) 0, short.class, (short) 0, char.class, '\0'
    );
    private static final Set<Class<?>> DATE_TIME_TYPES = Set.of(
            Date.class, LocalDateTime.class, LocalDate.class, LocalTime.class,
            Instant.class, ZonedDateTime.class, OffsetDateTime.class,
            java.sql.Date.class, Time.class, Timestamp.class
    );

    private final Class<T> targetClass;
    private final ZoneId databaseZoneId;

    /**
     * 构造函数（不指定时区）
     *
     * @param targetClass 目标映射类型
     */
    public SimpleRowMapper(Class<T> targetClass) {
        this(targetClass, null);
    }

    /**
     * 构造函数（指定数据库时区）
     *
     * @param targetClass   目标映射类型
     * @param databaseZoneId 数据库时区，用于日期时间类型转换
     */
    public SimpleRowMapper(Class<T> targetClass, ZoneId databaseZoneId) {
        this.targetClass = targetClass;
        this.databaseZoneId = databaseZoneId;
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        try {
            T instance = targetClass.getDeclaredConstructor().newInstance();
            Map<String, FieldMapping> fieldMappings = getFieldMappings(targetClass);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i).toLowerCase();
                FieldMapping mapping = fieldMappings.get(columnName);
                if (mapping == null) {
                    continue;
                }

                try {
                    Object value = getValue(rs, i, mapping);
                    if (value != null || !mapping.isPrimitive) {
                        mapping.field.set(instance, value);
                    }
                } catch (Exception e) {
                    log.warn("{}映射字段失败，字段: {}, 列: {}, 错误: {}", LOG_PREFIX, mapping.fieldName, columnName, e.getMessage());
                }
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | java.lang.reflect.InvocationTargetException e) {
            throw new SQLException("无法创建目标对象: " + targetClass.getName(), e);
        }
    }

    /**
     * 从ResultSet中获取并转换字段值
     *
     * @param rs           结果集
     * @param columnIndex  列索引
     * @param mapping      字段映射信息
     * @return 转换后的字段值
     * @throws SQLException SQL异常
     */
    private Object getValue(ResultSet rs, int columnIndex, FieldMapping mapping) throws SQLException {
        Object rawValue = rs.getObject(columnIndex);
        if (rawValue == null) {
            return handleNullValue(mapping.fieldType, mapping.isPrimitive);
        }

        if (DATE_TIME_TYPES.contains(mapping.fieldType)) {
            return convertDateTimeValue(rs, rs.getMetaData().getColumnName(columnIndex), mapping.fieldType);
        }

        if (mapping.isPrimitive || FieldExtractor.isBasicResult(mapping.fieldType)) {
            return convertBasicType(rawValue, mapping.fieldType, mapping.isPrimitive);
        }

        return rawValue;
    }

    /**
     * 转换日期时间类型值，支持时区转换
     *
     * @param rs         结果集
     * @param columnName 列名
     * @param targetType 目标类型
     * @return 转换后的日期时间对象
     * @throws SQLException SQL异常
     */
    private Object convertDateTimeValue(ResultSet rs, String columnName, Class<?> targetType) throws SQLException {
        try {
            if (targetType == Date.class) {
                return convertToUtilDate(rs, columnName);
            } else if (targetType == LocalDateTime.class) {
                return convertToLocalDateTime(rs, columnName);
            } else if (targetType == LocalDate.class) {
                return convertToLocalDate(rs, columnName);
            } else if (targetType == LocalTime.class) {
                return convertToLocalTime(rs, columnName);
            } else if (targetType == Instant.class) {
                return convertToInstant(rs, columnName);
            } else if (targetType == ZonedDateTime.class) {
                return convertToZonedDateTime(rs, columnName);
            } else if (targetType == OffsetDateTime.class) {
                return convertToOffsetDateTime(rs, columnName);
            } else if (targetType == java.sql.Date.class) {
                return rs.getDate(columnName);
            } else if (targetType == Time.class) {
                return rs.getTime(columnName);
            } else if (targetType == Timestamp.class) {
                return rs.getTimestamp(columnName);
            } else {
                return convertDateTimeFallback(rs, columnName, targetType);
            }
        } catch (SQLException e) {
            return convertDateTimeFallback(rs, columnName, targetType);
        }
    }

    private Date convertToUtilDate(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        }
        if (databaseZoneId != null) {
            LocalDateTime localDateTime = timestamp.toLocalDateTime();
            ZonedDateTime dbTime = localDateTime.atZone(databaseZoneId);
            ZonedDateTime appTime = dbTime.withZoneSameInstant(ZoneId.systemDefault());
            return Date.from(appTime.toInstant());
        }
        return new Date(timestamp.getTime());
    }

    private LocalDateTime convertToLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        }
        if (databaseZoneId != null) {
            LocalDateTime dbLocalDateTime = timestamp.toLocalDateTime();
            ZonedDateTime dbTime = dbLocalDateTime.atZone(databaseZoneId);
            ZonedDateTime appTime = dbTime.withZoneSameInstant(ZoneId.systemDefault());
            return appTime.toLocalDateTime();
        }
        return timestamp.toLocalDateTime();
    }

    private LocalDate convertToLocalDate(ResultSet rs, String columnName) throws SQLException {
        java.sql.Date sqlDate = rs.getDate(columnName);
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }

    private LocalTime convertToLocalTime(ResultSet rs, String columnName) throws SQLException {
        Time sqlTime = rs.getTime(columnName);
        if (sqlTime == null) {
            return null;
        }
        return sqlTime.toLocalTime();
    }

    private Instant convertToInstant(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        }
        if (databaseZoneId != null) {
            LocalDateTime localDateTime = timestamp.toLocalDateTime();
            ZonedDateTime dbTime = localDateTime.atZone(databaseZoneId);
            return dbTime.toInstant();
        }
        return timestamp.toInstant();
    }

    private ZonedDateTime convertToZonedDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        }
        if (databaseZoneId != null) {
            LocalDateTime localDateTime = timestamp.toLocalDateTime();
            ZonedDateTime dbTime = localDateTime.atZone(databaseZoneId);
            return dbTime.withZoneSameInstant(ZoneId.systemDefault());
        }
        return timestamp.toInstant().atZone(ZoneId.systemDefault());
    }

    private OffsetDateTime convertToOffsetDateTime(ResultSet rs, String columnName) throws SQLException {
        ZonedDateTime zonedDateTime = convertToZonedDateTime(rs, columnName);
        return zonedDateTime != null ? zonedDateTime.toOffsetDateTime() : null;
    }

    private Object convertDateTimeFallback(ResultSet rs, String columnName, Class<?> targetType) throws SQLException {
        Object rawValue = rs.getObject(columnName);
        if (rawValue == null) {
            return null;
        }
        if (rawValue instanceof String) {
            return parseDateTimeFromString((String) rawValue, targetType);
        }
        return rawValue;
    }

    private Object parseDateTimeFromString(String value, Class<?> targetType) {
        try {
            if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(value);
            } else if (targetType == LocalDate.class) {
                return LocalDate.parse(value);
            } else if (targetType == LocalTime.class) {
                return LocalTime.parse(value);
            } else if (targetType == Instant.class) {
                return Instant.parse(value);
            }
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 转换基本数据类型
     *
     * @param rawValue   原始值
     * @param targetType 目标类型
     * @param isPrimitive 是否为基本类型
     * @return 转换后的值
     */
    private Object convertBasicType(Object rawValue, Class<?> targetType, boolean isPrimitive) {
        if (targetType.isInstance(rawValue)) {
            return rawValue;
        }
        if (rawValue instanceof Number) {
            return convertNumber((Number) rawValue, targetType, isPrimitive);
        } else if (rawValue instanceof Boolean) {
            return convertBoolean((Boolean) rawValue, targetType, isPrimitive);
        } else if (rawValue instanceof String) {
            return convertString((String) rawValue, targetType, isPrimitive);
        }
        return rawValue;
    }

    private Object convertNumber(Number number, Class<?> targetType, boolean isPrimitive) {
        if (number == null || targetType == null) {
            return handleNullValue(targetType, isPrimitive);
        }
        if (targetType == Integer.class || targetType == int.class) {
            return number.intValue();
        } else if (targetType == Long.class || targetType == long.class) {
            return number.longValue();
        } else if (targetType == Double.class || targetType == double.class) {
            return number.doubleValue();
        } else if (targetType == Float.class || targetType == float.class) {
            return number.floatValue();
        } else if (targetType == Short.class || targetType == short.class) {
            return number.shortValue();
        } else if (targetType == Byte.class || targetType == byte.class) {
            return number.byteValue();
        } else if (targetType == BigDecimal.class) {
            if (number instanceof BigDecimal) {
                return number;
            }
            return BigDecimal.valueOf(number.doubleValue());
        }
        return number;
    }

    private Object convertBoolean(Boolean bool, Class<?> targetType, boolean isPrimitive) {
        if (targetType == Boolean.class || targetType == boolean.class) {
            return bool;
        } else if (targetType == Integer.class || targetType == int.class) {
            return bool ? 1 : 0;
        } else if (targetType == String.class) {
            return bool.toString();
        }
        return bool;
    }

    private Object convertString(String str, Class<?> targetType, boolean isPrimitive) {
        if (targetType == String.class) {
            return str;
        }
        try {
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(str);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(str);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(str);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(str);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return parseBoolean(str);
            }
        } catch (NumberFormatException e) {
            return handleNullValue(targetType, isPrimitive);
        }
        return str;
    }

    private boolean parseBoolean(String str) {
        if (str == null) {
            return false;
        }
        String lower = str.toLowerCase();
        return lower.equals("true") || lower.equals("1") || lower.equals("yes") || lower.equals("y");
    }

    private Object handleNullValue(Class<?> targetType, boolean isPrimitive) {
        if (isPrimitive) {
            return PRIMITIVE_DEFAULTS.getOrDefault(targetType, null);
        }
        return null;
    }

    /**
     * 获取字段映射缓存
     *
     * @param clazz 目标类
     * @return 字段映射Map，key为列名，value为字段映射信息
     */
    private Map<String, FieldMapping> getFieldMappings(Class<?> clazz) {
        return FIELD_MAPPING_CACHE.computeIfAbsent(clazz, key -> {
            Map<String, FieldMapping> mappings = new HashMap<>();
            try {
                List<Field> fields = ClassUtil.fields(clazz, true);
                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    if (field.getAnnotation(Transient.class) != null) {
                        continue;
                    }

                    FieldMapping mapping = new FieldMapping();
                    mapping.field = field;
                    mapping.fieldName = field.getName();
                    mapping.fieldType = field.getType();
                    mapping.isPrimitive = mapping.fieldType.isPrimitive();
                    mapping.isAccessible = field.isAccessible();
                    if (!mapping.isAccessible) {
                        field.setAccessible(true);
                    }

                    String columnName = null;
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    if (columnAnnotation != null && StringUtils.isNotBlank(columnAnnotation.name())) {
                        columnName = columnAnnotation.name().toLowerCase();
                    }
                    if (columnName == null) {
                        columnName = TextUtil.underscoreName(field.getName()).toLowerCase();
                    }
                    mapping.columnName = columnName;
                    mapping.originalColumnName = columnName;
                    mapping.isId = FieldExtractor.isPrimary(field);

                    mappings.put(columnName, mapping);
                }
            } catch (Exception e) {
                log.error("{}无法获取字段映射，类: {}", LOG_PREFIX, clazz.getName(), e);
            }
            return mappings;
        });
    }

    /**
     * 字段映射信息内部类
     */
    private static class FieldMapping {
        Field field;
        String fieldName;
        Class<?> fieldType;
        boolean isPrimitive;
        String columnName;
        String originalColumnName;
        boolean isId;
        boolean isAccessible;
    }
}
