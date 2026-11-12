package com.yishuifengxiao.common.tool.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import com.yishuifengxiao.common.tool.bean.ClassUtil;
import com.yishuifengxiao.common.tool.text.TextUtil;

import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import lombok.extern.slf4j.Slf4j;
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
    /**
     * 字段映射缓存：使用ClassValue实现，随类卸载自动释放，避免静态Map无界增长与类加载器泄漏
     */
    private static final ClassValue<Map<String, FieldMapping>> FIELD_MAPPING_CACHE = new ClassValue<>() {
        @Override
        protected Map<String, FieldMapping> computeValue(Class<?> type) {
            return buildFieldMappings(type);
        }
    };
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
                // 优先使用列标签（兼容 SELECT xx AS alias），标签缺失时回退到列名
                String columnLabel = metaData.getColumnLabel(i);
                if (StringUtils.isBlank(columnLabel)) {
                    columnLabel = metaData.getColumnName(i);
                }
                String mappingKey = null == columnLabel ? "" : columnLabel.toLowerCase(Locale.ROOT);
                FieldMapping mapping = fieldMappings.get(mappingKey);
                if (mapping == null) {
                    String columnName = metaData.getColumnName(i);
                    mapping = null == columnName ? null : fieldMappings.get(columnName.toLowerCase(Locale.ROOT));
                    if (mapping == null) {
                        log.trace("{}未找到列对应的实体字段，列: {}", LOG_PREFIX, columnLabel);
                        continue;
                    }
                }

                try {
                    Object value = getValue(rs, i, mapping, columnLabel);
                    if (value != null || !mapping.isPrimitive) {
                        mapping.field.set(instance, value);
                    }
                } catch (Exception e) {
                    log.warn("{}映射字段失败，字段: {}, 列: {}, 错误: {}", LOG_PREFIX, mapping.fieldName, columnLabel, e.getMessage());
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
     * @param columnName   列标签（标签缺失时为列名），用于按列名读取日期时间值
     * @return 转换后的字段值
     * @throws SQLException SQL异常
     */
    private Object getValue(ResultSet rs, int columnIndex, FieldMapping mapping, String columnName) throws SQLException {
        Object rawValue = rs.getObject(columnIndex);
        if (rawValue == null) {
            return handleNullValue(mapping.fieldType, mapping.isPrimitive);
        }

        if (DATE_TIME_TYPES.contains(mapping.fieldType)) {
            return convertDateTimeValue(rs, columnName, mapping.fieldType);
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
        // 直接使用timestamp转换为Date,不做额外的时区转换
        // JDBC驱动已经根据serverTimezone正确处理了时区
        return new Date(timestamp.getTime());
    }

    private LocalDateTime convertToLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        }
        if (databaseZoneId != null) {
            // 基于同一时间点按时区转换一次，避免对JVM本地时间再次套用时区造成二次偏移
            return timestamp.toInstant().atZone(databaseZoneId).toLocalDateTime();
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
        // 直接使用timestamp的Instant,不做时区转换
        // 因为JDBC驱动已经根据serverTimezone正确处理了时区
        return timestamp.toInstant();
    }

    private ZonedDateTime convertToZonedDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        if (timestamp == null) {
            return null;
        }
        // 将timestamp转换为系统默认时区的ZonedDateTime
        // JDBC驱动已经根据serverTimezone正确处理了时区
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
        if (targetType.isInstance(rawValue)) {
            return rawValue;
        }
        // 部分驱动直接返回 java.time / java.sql 日期对象，需按目标类型做等价转换，否则反射赋值会因类型不匹配被丢弃
        ZoneId zone = null != databaseZoneId ? databaseZoneId : ZoneId.systemDefault();
        Instant instant = extractInstant(rawValue);
        LocalDateTime localDateTime = null != instant
                ? LocalDateTime.ofInstant(instant, zone)
                : extractLocalDateTime(rawValue);
        if (localDateTime == null) {
            return rawValue;
        }
        if (targetType == LocalDateTime.class) {
            return localDateTime;
        }
        if (targetType == LocalDate.class) {
            return localDateTime.toLocalDate();
        }
        if (targetType == LocalTime.class) {
            return localDateTime.toLocalTime();
        }
        if (targetType == java.sql.Date.class) {
            return java.sql.Date.valueOf(localDateTime.toLocalDate());
        }
        if (targetType == Time.class) {
            return Time.valueOf(localDateTime.toLocalTime());
        }
        // 无时间点信息的原始值按数据库时区（缺省为系统时区）补全时区后再转换
        Instant targetInstant = null != instant ? instant : localDateTime.atZone(zone).toInstant();
        if (targetType == Instant.class) {
            return targetInstant;
        }
        if (targetType == ZonedDateTime.class) {
            return targetInstant.atZone(zone);
        }
        if (targetType == OffsetDateTime.class) {
            return targetInstant.atZone(zone).toOffsetDateTime();
        }
        if (targetType == Timestamp.class) {
            return Timestamp.from(targetInstant);
        }
        if (targetType == Date.class) {
            return Date.from(targetInstant);
        }
        return rawValue;
    }

    /**
     * 提取原始值中蕴含的时间点
     *
     * @param rawValue 原始值
     * @return 时间点，局部日期时间类型不携带时区信息时返回null
     */
    private Instant extractInstant(Object rawValue) {
        if (rawValue instanceof Instant) {
            return (Instant) rawValue;
        }
        if (rawValue instanceof Timestamp) {
            return ((Timestamp) rawValue).toInstant();
        }
        if (rawValue instanceof ZonedDateTime) {
            return ((ZonedDateTime) rawValue).toInstant();
        }
        if (rawValue instanceof OffsetDateTime) {
            return ((OffsetDateTime) rawValue).toInstant();
        }
        // java.sql.Date/Time 的 toInstant() 会抛 UnsupportedOperationException，因此必须排除后最后处理普通 Date
        if (rawValue instanceof Date && !(rawValue instanceof java.sql.Date) && !(rawValue instanceof Time)) {
            return ((Date) rawValue).toInstant();
        }
        return null;
    }

    /**
     * 提取原始值中的本地日期时间
     *
     * @param rawValue 原始值
     * @return 本地日期时间，无法识别时返回null
     */
    private LocalDateTime extractLocalDateTime(Object rawValue) {
        if (rawValue instanceof LocalDateTime) {
            return (LocalDateTime) rawValue;
        }
        if (rawValue instanceof LocalDate) {
            return ((LocalDate) rawValue).atStartOfDay();
        }
        if (rawValue instanceof LocalTime) {
            return LocalDate.EPOCH.atTime((LocalTime) rawValue);
        }
        if (rawValue instanceof java.sql.Date) {
            return ((java.sql.Date) rawValue).toLocalDate().atStartOfDay();
        }
        if (rawValue instanceof Time) {
            return LocalDate.EPOCH.atTime(((Time) rawValue).toLocalTime());
        }
        return null;
    }

    /**
     * 解析字符串形式的日期时间值
     * <p>
     * 支持 ISO 格式以及数据库/文本中常见的空格分隔格式（如 {@code 2025-01-01 12:00:00}），
     * 并覆盖 OffsetDateTime、ZonedDateTime 目标类型。解析失败时记录告警日志并返回 null。
     * </p>
     *
     * @param value      原始字符串值
     * @param targetType 目标类型
     * @return 解析后的日期时间对象，无法解析时返回 null
     */
    private Object parseDateTimeFromString(String value, Class<?> targetType) {
        String text = value.trim();
        // LocalDateTime 常见格式：ISO、空格分隔带秒、空格分隔到分钟
        DateTimeFormatter[] localDateTimeFormats = {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        };
        try {
            if (targetType == LocalDateTime.class) {
                for (DateTimeFormatter formatter : localDateTimeFormats) {
                    try {
                        return LocalDateTime.parse(text, formatter);
                    } catch (DateTimeParseException ignored) {
                        // 尝试下一个格式
                    }
                }
                // 仅日期时按当天零点处理
                return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
            } else if (targetType == LocalDate.class) {
                // 兼容 "yyyy-MM-dd HH:mm:ss" 这类带时间部分的字符串
                String datePart = text.length() >= 10 ? text.substring(0, 10) : text;
                return LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE);
            } else if (targetType == LocalTime.class) {
                return LocalTime.parse(text, DateTimeFormatter.ISO_LOCAL_TIME);
            } else if (targetType == Instant.class) {
                try {
                    return Instant.parse(text);
                } catch (DateTimeParseException ignored) {
                    // 非 ISO Instant 时回退为本地日期时间，再按数据库时区（缺省为系统时区）转换
                    ZoneId zone = null != databaseZoneId ? databaseZoneId : ZoneId.systemDefault();
                    for (DateTimeFormatter formatter : localDateTimeFormats) {
                        try {
                            return LocalDateTime.parse(text, formatter).atZone(zone).toInstant();
                        } catch (DateTimeParseException e) {
                            // 尝试下一个格式
                        }
                    }
                    return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(zone).toInstant();
                }
            } else if (targetType == OffsetDateTime.class) {
                return OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } else if (targetType == ZonedDateTime.class) {
                return ZonedDateTime.parse(text, DateTimeFormatter.ISO_ZONED_DATE_TIME);
            }
        } catch (DateTimeParseException e) {
            // 不再静默丢值，至少保留可检索的告警日志
            log.warn("{}日期字符串解析失败，值: {}, 目标类型: {}", LOG_PREFIX, value, targetType.getSimpleName());
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
        if (rawValue instanceof byte[]) {
            return convertByteArray((byte[]) rawValue, targetType, isPrimitive);
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

    private Object convertByteArray(byte[] bytes, Class<?> targetType, boolean isPrimitive) {
        if (bytes == null) {
            return handleNullValue(targetType, isPrimitive);
        }
        if (targetType == String.class) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        if (targetType == Byte[].class) {
            // 包装类型数组需要逐元素装箱，直接返回byte[]会因类型不匹配导致赋值失败
            Byte[] boxed = new Byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                boxed[i] = bytes[i];
            }
            return boxed;
        }
        return bytes;
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
            // 通过字符串构造，避免 long 超过 2^53 时经 double 中转丢失精度
            return new BigDecimal(number.toString());
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
        String lower = str.toLowerCase(Locale.ROOT);
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
        return FIELD_MAPPING_CACHE.get(clazz);
    }

    /**
     * 构建指定类的列名到字段的映射
     *
     * @param clazz 目标类
     * @return 字段映射Map，key为列名，value为字段映射信息
     */
    private static Map<String, FieldMapping> buildFieldMappings(Class<?> clazz) {
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
                field.setAccessible(true);

                String columnName = null;
                Column columnAnnotation = field.getAnnotation(Column.class);
                if (columnAnnotation != null && StringUtils.isNotBlank(columnAnnotation.name())) {
                    columnName = columnAnnotation.name().toLowerCase(Locale.ROOT);
                }
                if (columnName == null) {
                    columnName = TextUtil.underscoreName(field.getName()).toLowerCase(Locale.ROOT);
                }
                mapping.columnName = columnName;
                mapping.originalColumnName = columnName;
                mapping.isId = FieldExtractor.isPrimary(field);

                mappings.put(columnName, mapping);
                // 兜底：允许列名与字段名完全一致（如驼峰列名）时也能匹配
                mappings.putIfAbsent(field.getName().toLowerCase(Locale.ROOT), mapping);
            }
        } catch (Exception e) {
            log.error("{}无法获取字段映射，类: {}", LOG_PREFIX, clazz.getName(), e);
        }
        return mappings;
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
    }
}
