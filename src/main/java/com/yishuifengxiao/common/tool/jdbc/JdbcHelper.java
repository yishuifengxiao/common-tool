package com.yishuifengxiao.common.tool.jdbc;

import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.entity.PageQuery;
import com.yishuifengxiao.common.tool.entity.Slice;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class JdbcHelper {
    private static final String LOG_PREFIX = "【yishuifengxiao】";
    private static final int DEFAULT_BATCH_SIZE = 500;
    private static final int CHUNK_SIZE = 1024 * 1024;

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ZoneId databaseZoneId;
    private SqlTranslator sqlTranslator = new SqlTranslator();

    public JdbcHelper() {
    }

    public JdbcHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initialize();
    }

    private void initialize() {
        if (this.jdbcTemplate == null) {
            log.warn("{}JdbcTemplate为空，跳过初始化", LOG_PREFIX);
            return;
        }
        try {
            this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
            this.databaseZoneId = ZoneIdDetector.detectDatabaseTimezone(jdbcTemplate.getDataSource().getConnection());
            log.debug("{}SQL执行器初始化成功，时区: {}", LOG_PREFIX, this.databaseZoneId);
        } catch (SQLException e) {
            log.warn("{}无法获取数据库时区信息，使用默认时区", LOG_PREFIX);
        } catch (Exception e) {
            log.error("{}SQL执行器初始化失败", LOG_PREFIX, e);
            throw new UncheckedException(JdbcError.SQL_HELPER_INIT_ERROR, "SQL执行器初始化失败");
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initialize();
    }

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return this.namedParameterJdbcTemplate;
    }

    public <T> T findByPrimaryKey(Class<T> clazz, Object primaryKey) {
        if (primaryKey == null) {
            log.debug("{}主键值为空，跳过查询", LOG_PREFIX);
            return null;
        }
        String tableName = FieldExtractor.extractTableName(clazz);
        FieldValue primaryKeyField = FieldExtractor.extractPrimaryField(clazz);
        primaryKeyField.setValue(primaryKey);
        String sql = sqlTranslator.findAll(tableName, Collections.singletonList(primaryKeyField), false, null, new Slice(1, 1));
        return executeSingleQuery(clazz, sql, primaryKeyField);
    }

    public <T> Long countAll(T t, boolean likeMode) {
        if (t == null) {
            return 0L;
        }
        try {
            String tableName = FieldExtractor.extractTableName(t.getClass());
            List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
            List<FieldValue> nonNullValues = extractNonNullFieldValues(fieldValues);
            if (nonNullValues.isEmpty()) {
                String countSql = "SELECT COUNT(1) FROM `" + tableName + "`";
                return executeCountQuery(countSql);
            }
            String sql = sqlTranslator.findAll(tableName, nonNullValues, likeMode, null, null);
            String countSql = buildCountSql(sql);
            return executeCountQuery(countSql, toArray(nonNullValues));
        } catch (Exception e) {
            log.error("{}执行countAll时发生异常", LOG_PREFIX, e);
            return 0L;
        }
    }

    public <T> T findOne(T t, boolean likeMode, Order... orders) {
        if (t == null) {
            return null;
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        List<FieldValue> nonNullValues = extractNonNullFieldValues(fieldValues);
        List<Order> processedOrders = createOrder(fieldValues, orders);
        String sql = sqlTranslator.findAll(tableName, nonNullValues, likeMode, processedOrders, new Slice(1, 1));
        return executeSingleQuery((Class<T>) t.getClass(), sql, toArray(nonNullValues));
    }

    public <T> List<T> findAll(T t, boolean likeMode, Order... orders) {
        if (t == null) {
            return Collections.emptyList();
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        List<FieldValue> nonNullValues = extractNonNullFieldValues(fieldValues);
        List<Order> processedOrders = createOrder(fieldValues, orders);
        String querySql = sqlTranslator.findAll(tableName, nonNullValues, likeMode, processedOrders, null);
        return executeListQuery((Class<T>) t.getClass(), querySql, toArray(nonNullValues));
    }

    public <T> Page<T> findPage(PageQuery<T> pageQuery, boolean likeMode, Order... orders) {
        if (pageQuery == null) {
            return Page.ofEmpty();
        }
        T query = null;
        if (pageQuery.query() != null) {
            query = pageQuery.query().orElse(null);
        }
        return this.findPage(query, likeMode, pageQuery, orders);
    }

    public <T> Page<T> findPage(T t, boolean likeMode, Slice slice, Order... orders) {
        slice = slice == null ? new Slice(10, 1) : slice;
        if (t == null) {
            return Page.ofEmpty(slice.getSize());
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        List<FieldValue> nonNullValues = extractNonNullFieldValues(fieldValues);
        List<Order> processedOrders = createOrder(fieldValues, orders);
        FieldValue[] params = toArray(nonNullValues);
        String querySql = sqlTranslator.findAll(tableName, nonNullValues, likeMode, processedOrders, slice);
        String countSql = sqlTranslator.findAll(tableName, nonNullValues, likeMode, null, null);
        String wrappedCountSql = buildCountSql(countSql);
        List<T> list = executeListQuery((Class<T>) t.getClass(), querySql, params);
        Long count = executeCountQuery(wrappedCountSql, params);
        return Page.of(list, count, slice);
    }

    public <T> List<T> findAll(Class<T> clazz, String sql, Object... params) {
        List<T> results;
        if (FieldExtractor.isBasicResult(clazz)) {
            results = this.jdbcTemplate.queryForList(sql, clazz, params);
        } else {
            results = jdbcTemplate.query(sql, new SimpleRowMapper<>(clazz, this.databaseZoneId), params);
        }
        return results == null ? Collections.emptyList() : results;
    }

    public <T> List<T> find(Class<T> clazz, String sql, Object param) {
        if (null == param) {
            return this.find(clazz, sql, (SqlParameterSource) null);
        }
        SqlParameterSource params = new BeanPropertySqlParameterSource(param);
        return this.find(clazz, sql, params);
    }

    public <T> List<T> find(Class<T> clazz, String sql, Map<String, Object> params) {
        if (null == params || params.isEmpty()) {
            return this.find(clazz, sql, (SqlParameterSource) null);
        }
        SqlParameterSource paramSource;
        if (this.databaseZoneId != null) {
            Map<String, Object> processedParams = processDateTimeParameters(params);
            paramSource = new MapSqlParameterSource(processedParams);
        } else {
            paramSource = new MapSqlParameterSource(params);
        }
        return this.find(clazz, sql, paramSource);
    }

    public <T> List<T> find(Class<T> clazz, String sql, SqlParameterSource params) {
        if (StringUtils.isBlank(sql)) {
            throw new UncheckedException(JdbcError.SQL_IS_NULL, "SQL语句不能为空");
        }
        sql = sql.replaceAll("\r", "  ").replaceAll("\n", "  ").trim();
        if (log.isTraceEnabled()) {
            log.trace("{}执行查询：{}，参数：{}", LOG_PREFIX, sql, params);
        }
        if (null == params) {
            return FieldExtractor.isBasicResult(clazz) ? this.jdbcTemplate.queryForList(sql, clazz) : this.jdbcTemplate.query(sql, new SimpleRowMapper<>(clazz, this.databaseZoneId));
        }
        return FieldExtractor.isBasicResult(clazz) ? this.namedParameterJdbcTemplate.queryForList(sql, params, clazz) : this.namedParameterJdbcTemplate.query(sql, params, new SimpleRowMapper<>(clazz, this.databaseZoneId));
    }

    public <T> Page<T> find(Class<T> clazz, Slice slice, String sql, Map<String, Object> params) {
        if (clazz == null || StringUtils.isBlank(sql)) {
            log.info("{}参数不完整，clazz: {}, sql: {}", LOG_PREFIX, clazz, sql);
            return Page.ofEmpty();
        }
        log.debug("{}执行findPage查询，clazz: {}, slice: {}, sql: {}, params: {}", LOG_PREFIX, clazz, slice, sql, params);
        slice = slice == null ? new Slice(10, 1) : slice;
        try {
            String paginatedSql = buildPaginatedSql(sql, slice);
            List<T> dataList = this.find(clazz, paginatedSql, params);
            String countSql = buildCountSql(sql);
            List<Long> countResult = this.find(Long.class, countSql, params);
            Long totalCount = countResult == null || countResult.isEmpty() ? 0L : countResult.get(0);
            return Page.of(dataList, totalCount, slice);
        } catch (Exception e) {
            log.error("{}执行findPage查询时发生异常，sql: {}, params: {}", LOG_PREFIX, sql, params, e);
            return Page.ofEmpty(slice.getSize());
        }
    }

    public <T> int updateByPrimaryKey(T t) {
        if (t == null) {
            return 0;
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        FieldValue primaryKey = fieldValues.stream().filter(FieldValue::isPrimary).findFirst().orElse(null);
        List<FieldValue> nonPrimaryKeys = fieldValues.stream().filter(v -> !v.isPrimary()).collect(Collectors.toList());
        List<FieldValue> params = new ArrayList<>(fieldValues.size());
        params.addAll(nonPrimaryKeys);
        params.add(primaryKey);
        String sql = sqlTranslator.updateByPrimaryKey(tableName, primaryKey, params);
        return SqlExecutor.execute(jdbcTemplate, sql, toArray(params));
    }

    public <T> int updateByPrimaryKeySelective(T t) {
        if (t == null) {
            return 0;
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        FieldValue primaryKey = fieldValues.stream().filter(FieldValue::isPrimary).findFirst().orElse(null);
        List<FieldValue> nonPrimaryKeys = fieldValues.stream().filter(v -> !v.isPrimary()).collect(Collectors.toList());
        List<FieldValue> nonNullNonPrimaryKeys = extractNonNullFieldValues(nonPrimaryKeys);
        List<FieldValue> params = new ArrayList<>(fieldValues.size());
        params.addAll(nonNullNonPrimaryKeys);
        params.add(primaryKey);
        String sql = sqlTranslator.updateByPrimaryKey(tableName, primaryKey, params);
        return SqlExecutor.execute(jdbcTemplate, sql, toArray(params));
    }

    public <T> int deleteByPrimaryKey(Class<T> clazz, Object... primaryKeys) {
        if (primaryKeys == null || primaryKeys.length == 0) {
            log.debug("{}主键参数为空，跳过删除", LOG_PREFIX);
            return 0;
        }
        List<Object> validPrimaryKeys = Arrays.stream(primaryKeys).filter(Objects::nonNull).filter(key -> !(key instanceof String) || StringUtils.isNotBlank(((String) key).trim())).collect(Collectors.toList());
        if (validPrimaryKeys.isEmpty()) {
            log.debug("{}无有效主键值，跳过删除", LOG_PREFIX);
            return 0;
        }
        String tableName = FieldExtractor.extractTableName(clazz);
        FieldValue primaryKey = FieldExtractor.extractPrimaryField(clazz);
        if (validPrimaryKeys.size() == 1) {
            primaryKey.setValue(validPrimaryKeys.get(0));
        } else {
            primaryKey.setValue(validPrimaryKeys);
        }
        String sql = sqlTranslator.deleteByPrimaryKeys(tableName, primaryKey.getColumnName(), validPrimaryKeys);
        return SqlExecutor.execute(jdbcTemplate, sql, primaryKey);
    }

    public <T> KeyHolder insert(T t) {
        if (t == null) {
            log.warn("{}新增数据为空", LOG_PREFIX);
            return null;
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        FieldValue primaryKey = fieldValues.stream().filter(FieldValue::isPrimary).findFirst().orElse(null);
        if (null != primaryKey && primaryKey.isNullVal()) {
            fieldValues = fieldValues.stream().filter(v -> !v.isPrimary()).collect(Collectors.toList());
        }
        String sql = sqlTranslator.insert(tableName, fieldValues);
        return SqlExecutor.update(this.jdbcTemplate, sql, fieldValues, this.databaseZoneId);
    }

    public <T> KeyHolder saveOrUpdate(T t) {
        if (t == null) {
            log.warn("{}保存或更新数据为空", LOG_PREFIX);
            return null;
        }
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        FieldValue primaryKeyValue = fieldValues.stream().filter(Objects::nonNull).filter(field -> field.isPrimary() && field.isNotNullVal()).findFirst().orElse(null);
        if (null == primaryKeyValue || primaryKeyValue.isNullVal()) {
            return this.insert(t);
        } else {
            String tableName = FieldExtractor.extractTableName(t.getClass());
            String sql = "SELECT COUNT(1) FROM `" + tableName + "` WHERE `" + primaryKeyValue.getColumnName() + "` = :id";
            List<Long> result = this.find(Long.class, sql, Map.of("id", primaryKeyValue.getValue()));
            if (result == null || result.isEmpty() || result.get(0) <= 0) {
                return this.insert(t);
            }
            this.updateByPrimaryKey(t);
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            Map<String, Object> keys = Collections.singletonMap(primaryKeyValue.getField().getName(), primaryKeyValue.getValue());
            keyHolder.getKeyList().add(keys);
            return keyHolder;
        }
    }

    public <T> void saveAll(Collection<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<T> validItems = list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (validItems.isEmpty()) {
            return;
        }
        T firstValidItem = validItems.get(0);
        String tableName = FieldExtractor.extractTableName(firstValidItem.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(firstValidItem);
        FieldValue primaryKey = fieldValues.stream().filter(FieldValue::isPrimary).findFirst().orElse(null);
        String sql = sqlTranslator.insert(tableName, fieldValues);
        int[] types = fieldValues.stream().mapToInt(field -> field.sqlType().getVendorTypeNumber()).toArray();
        List<List<FieldValue>> batchParams = list.stream().filter(Objects::nonNull).map(s -> {
            List<FieldValue> values = FieldExtractor.extractFieldValue(s);
            if (null == primaryKey || primaryKey.isNullVal()) {
                values = values.stream().filter(v -> !v.isPrimary()).collect(Collectors.toList());
            }
            return values;
        }).collect(Collectors.toList());
        SqlExecutor.batchUpdate(jdbcTemplate, sql, types, batchParams, this.databaseZoneId);
    }

    private FieldValue[] toArray(List<FieldValue> fieldValues) {
        if (null == fieldValues) {
            return new FieldValue[0];
        }
        return fieldValues.toArray(new FieldValue[fieldValues.size()]);
    }

    private List<Order> createOrder(List<FieldValue> fieldValues, Order... orders) {
        if (orders == null || orders.length == 0) {
            return null;
        }
        return Arrays.stream(orders).filter(Objects::nonNull).filter(order -> StringUtils.isNotBlank(order.getOrderName())).map(order -> {
            String columnName = fieldValues.stream().filter(field -> field != null && field.getField() != null).filter(field -> field.getField().getName().equalsIgnoreCase(order.getOrderName())).map(FieldValue::getColumnName).findFirst().orElse(null);
            if (StringUtils.isNotBlank(columnName)) {
                order.setOrderName(columnName);
            }
            return order;
        }).collect(Collectors.toList());
    }

    private <T> T executeSingleQuery(Class<T> clazz, String sql, FieldValue... args) {
        List<T> list = SqlExecutor.findAll(jdbcTemplate, clazz, sql, args, this.databaseZoneId);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    private <T> List<T> executeListQuery(Class<T> clazz, String sql, FieldValue... args) {
        List<T> list = SqlExecutor.findAll(jdbcTemplate, clazz, sql, args, this.databaseZoneId);
        return list == null ? Collections.emptyList() : list;
    }

    private Long executeCountQuery(String sql, FieldValue... args) {
        List<Long> numbers = SqlExecutor.findAll(jdbcTemplate, Long.class, sql, args, this.databaseZoneId);
        return numbers == null || numbers.isEmpty() ? 0L : numbers.get(0);
    }

    private List<FieldValue> extractNonNullFieldValues(List<FieldValue> fieldValues) {
        return fieldValues.stream().filter(FieldValue::isNotNullVal).collect(Collectors.toList());
    }

    private String buildCountSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "SELECT COUNT(1)";
        }
        String cleanSql = sql.trim();
        if (cleanSql.endsWith(";")) {
            cleanSql = cleanSql.substring(0, cleanSql.length() - 1).trim();
        }
        Pattern orderByPattern = Pattern.compile("\\bORDER\\s+BY\\b", Pattern.CASE_INSENSITIVE);
        Matcher orderByMatcher = orderByPattern.matcher(cleanSql);
        if (orderByMatcher.find()) {
            cleanSql = cleanSql.substring(0, orderByMatcher.start()).trim();
        }
        if (isComplexQueryForCount(cleanSql)) {
            log.debug("{}检测到复杂查询，使用默认COUNT查询: {}", LOG_PREFIX, cleanSql);
            return "SELECT COUNT(1) FROM (" + cleanSql + ") AS temp_count_table";
        }
        Pattern selectPattern = Pattern.compile("^\\s*SELECT\\b", Pattern.CASE_INSENSITIVE);
        if (!selectPattern.matcher(cleanSql).find()) {
            log.warn("{}非SELECT查询，使用默认COUNT查询: {}", LOG_PREFIX, cleanSql);
            return "SELECT COUNT(1) FROM (" + cleanSql + ") AS temp_count_table";
        }
        Pattern fromPattern = Pattern.compile("\\bFROM\\b", Pattern.CASE_INSENSITIVE);
        Matcher fromMatcher = fromPattern.matcher(cleanSql);
        int fromIndex = -1;
        while (fromMatcher.find()) {
            int currentFrom = fromMatcher.start();
            String beforeFrom = cleanSql.substring(0, currentFrom);
            if (!beforeFrom.contains("(")) {
                fromIndex = currentFrom;
                break;
            }
        }
        if (fromIndex == -1) {
            log.debug("{}无法找到FROM关键字，使用默认COUNT查询: {}", LOG_PREFIX, cleanSql);
            return "SELECT COUNT(1) FROM (" + cleanSql + ") AS temp_count_table";
        }
        return "SELECT COUNT(1)" + cleanSql.substring(fromIndex);
    }

    private boolean isComplexQueryForCount(String sql) {
        if (StringUtils.isBlank(sql)) {
            return false;
        }
        String upperSql = sql.toUpperCase();
        return upperSql.contains(" UNION ") || upperSql.contains(" UNION ALL ") || upperSql.contains(" DISTINCT ") || upperSql.contains(" GROUP BY ") || upperSql.contains(" HAVING ");
    }

    private String buildPaginatedSql(String sql, Slice slice) {
        if (sql == null || sql.trim().isEmpty() || slice == null) {
            return sql;
        }
        String cleanSql = sql.trim();
        if (cleanSql.endsWith(";")) {
            cleanSql = cleanSql.substring(0, cleanSql.length() - 1).trim();
        }
        Integer pageNum = slice.getNum() == null ? null : slice.getNum().intValue();
        Integer pageSize = slice.getSize() == null ? null : slice.getSize().intValue();
        if (pageSize != null) {
            if (pageNum == null) {
                return cleanSql + " LIMIT " + pageSize;
            } else {
                if (pageNum > 0 && pageSize > 0) {
                    int offset = (pageNum - 1) * pageSize;
                    return cleanSql + " LIMIT " + offset + "," + pageSize;
                }
            }
        }
        return cleanSql;
    }

    private Map<String, Object> processDateTimeParameters(Map<String, Object> params) {
        Map<String, Object> processedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            processedParams.put(entry.getKey(), processDateTimeValueRecursive(entry.getValue()));
        }
        return processedParams;
    }

    private Object processDateTimeValueRecursive(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            List<Object> processedList = new ArrayList<>();
            for (Object item : collection) {
                processedList.add(processDateTimeValueRecursive(item));
            }
            if (value instanceof Set) {
                return new HashSet<>(processedList);
            } else {
                return processedList;
            }
        } else if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            Object[] processedArray = new Object[array.length];
            for (int i = 0; i < array.length; i++) {
                processedArray[i] = processDateTimeValueRecursive(array[i]);
            }
            return processedArray;
        } else if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            Map<Object, Object> processedMap = new HashMap<>();
            for (Map.Entry<?, ?> mapEntry : map.entrySet()) {
                processedMap.put(mapEntry.getKey(), processDateTimeValueRecursive(mapEntry.getValue()));
            }
            return processedMap;
        }
        return processSingleDateTimeValue(value);
    }

    private Object processSingleDateTimeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (this.databaseZoneId == null) {
            return convertToJavaSqlTypeNoTimezone(value);
        }
        ZoneId appZone = ZoneId.systemDefault();
        if (isSameTimezone(appZone, this.databaseZoneId)) {
            return convertToJavaSqlTypeNoTimezone(value);
        }
        try {
            if (value instanceof Date) {
                Date date = (Date) value;
                Instant instant = date.toInstant();
                ZonedDateTime appTime = instant.atZone(appZone);
                ZonedDateTime dbTime = appTime.withZoneSameInstant(this.databaseZoneId);
                return Timestamp.valueOf(dbTime.toLocalDateTime());
            } else if (value instanceof LocalDateTime) {
                LocalDateTime localDateTime = (LocalDateTime) value;
                ZonedDateTime appTime = localDateTime.atZone(appZone);
                ZonedDateTime dbTime = appTime.withZoneSameInstant(this.databaseZoneId);
                return Timestamp.valueOf(dbTime.toLocalDateTime());
            } else if (value instanceof ZonedDateTime) {
                ZonedDateTime zonedDateTime = (ZonedDateTime) value;
                ZonedDateTime dbTime = zonedDateTime.withZoneSameInstant(this.databaseZoneId);
                return Timestamp.valueOf(dbTime.toLocalDateTime());
            } else if (value instanceof OffsetDateTime) {
                OffsetDateTime offsetDateTime = (OffsetDateTime) value;
                ZonedDateTime dbTime = offsetDateTime.atZoneSameInstant(this.databaseZoneId);
                return Timestamp.valueOf(dbTime.toLocalDateTime());
            } else if (value instanceof Instant) {
                Instant instant = (Instant) value;
                ZonedDateTime dbTime = instant.atZone(this.databaseZoneId);
                return Timestamp.valueOf(dbTime.toLocalDateTime());
            } else if (value instanceof LocalDate) {
                LocalDate localDate = (LocalDate) value;
                return java.sql.Date.valueOf(localDate);
            } else if (value instanceof LocalTime) {
                LocalTime localTime = (LocalTime) value;
                return Time.valueOf(localTime);
            } else if (value instanceof java.sql.Date) {
                return value;
            } else if (value instanceof Time) {
                return value;
            } else if (value instanceof Timestamp) {
                Timestamp timestamp = (Timestamp) value;
                LocalDateTime localDateTime = timestamp.toLocalDateTime();
                ZonedDateTime appTime = localDateTime.atZone(appZone);
                ZonedDateTime dbTime = appTime.withZoneSameInstant(this.databaseZoneId);
                return Timestamp.valueOf(dbTime.toLocalDateTime());
            }
        } catch (Exception e) {
            log.warn("{}日期时间转换失败，使用原始值: {}", LOG_PREFIX, e.getMessage());
        }
        return value;
    }

    private static boolean isSameTimezone(ZoneId zone1, ZoneId zone2) {
        if (zone1 == null || zone2 == null) {
            return false;
        }
        if (zone1.equals(zone2)) {
            return true;
        }
        Instant now = Instant.now();
        return zone1.getRules().getOffset(now).equals(zone2.getRules().getOffset(now));
    }

    private Object convertToJavaSqlTypeNoTimezone(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
            return java.sql.Date.valueOf((LocalDate) value);
        } else if (value instanceof LocalTime) {
            return Time.valueOf((LocalTime) value);
        } else if (value instanceof ZonedDateTime) {
            ZonedDateTime zonedDateTime = (ZonedDateTime) value;
            return Timestamp.valueOf(zonedDateTime.toLocalDateTime());
        } else if (value instanceof OffsetDateTime) {
            OffsetDateTime offsetDateTime = (OffsetDateTime) value;
            return Timestamp.valueOf(offsetDateTime.toLocalDateTime());
        } else if (value instanceof Instant) {
            return Timestamp.from((Instant) value);
        } else if (value instanceof Date) {
            return new Timestamp(((Date) value).getTime());
        }
        return value;
    }

    public static class SqlExecutor {
        public static int execute(JdbcTemplate jdbcTemplate, String sql, FieldValue... args) {
            logSqlExecutionStart("执行sql", sql, args);
            int count;
            if (args == null || args.length == 0) {
                count = jdbcTemplate.update(sql);
            } else {
                count = jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql);
                    setPreparedStatementParameters(ps, Arrays.asList(args), null);
                    return ps;
                });
            }
            logSqlExecutionEnd("执行sql", count);
            return count;
        }

        public static <T> List<T> findAll(JdbcTemplate jdbcTemplate, Class<T> clazz, String sql, FieldValue[] args, ZoneId databaseZoneId) {
            logSqlExecutionStart("查询记录", sql, args);
            List<T> results;
            if (null == args || args.length == 0) {
                if (FieldExtractor.isBasicResult(clazz)) {
                    results = jdbcTemplate.queryForList(sql, clazz);
                } else {
                    results = jdbcTemplate.query(sql, new SimpleRowMapper<>(clazz, databaseZoneId));
                }
            } else {
                if (FieldExtractor.isBasicResult(clazz)) {
                    Object[] values = new Object[args.length];
                    int[] argTypes = new int[args.length];
                    for (int i = 0; i < args.length; i++) {
                        Object value = processDateTimeValue(args[i].getValue(), databaseZoneId);
                        values[i] = value;
                        SQLType sqlType = args[i].sqlType();
                        argTypes[i] = sqlType != null ? sqlType.getVendorTypeNumber() : Types.OTHER;
                    }
                    results = jdbcTemplate.queryForList(sql, values, argTypes, clazz);
                } else {
                    results = jdbcTemplate.query(connection -> {
                        PreparedStatement ps = connection.prepareStatement(sql);
                        setPreparedStatementParameters(ps, Arrays.asList(args), databaseZoneId);
                        return ps;
                    }, new SimpleRowMapper<>(clazz, databaseZoneId));
                }
            }
            logSqlExecutionEnd("查询记录", results != null ? results.size() : 0);
            return results;
        }

        public static KeyHolder update(JdbcTemplate jdbcTemplate, String sql, List<FieldValue> fieldValues, ZoneId databaseZoneId) {
            logSqlExecutionStart("查询记录", sql, fieldValues);
            if (containsLargeField(fieldValues)) {
                return updateWithLargeFields(jdbcTemplate, sql, fieldValues, databaseZoneId);
            }
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            try {
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    setPreparedStatementParameters(ps, fieldValues, databaseZoneId);
                    return ps;
                }, keyHolder);
            } catch (Exception e) {
                log.error("更新失败: ", e);
                throw e;
            }
            logSqlExecutionEnd("update", keyHolder);
            return keyHolder;
        }

        private static boolean containsLargeField(List<FieldValue> fieldValues) {
            if (fieldValues == null || fieldValues.isEmpty()) {
                return false;
            }
            for (FieldValue fieldValue : fieldValues) {
                if (fieldValue == null || fieldValue.getValue() == null) {
                    continue;
                }
                Object value = fieldValue.getValue();
                if (value instanceof byte[]) {
                    if (((byte[]) value).length > CHUNK_SIZE) {
                        return true;
                    }
                } else if (value instanceof String) {
                    byte[] bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
                    if (bytes.length > CHUNK_SIZE) {
                        return true;
                    }
                } else if (value instanceof InputStream) {
                    return true;
                }
            }
            return false;
        }

        private static KeyHolder updateWithLargeFields(JdbcTemplate jdbcTemplate, String sql, List<FieldValue> fieldValues, ZoneId databaseZoneId) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                setPreparedStatementParameters(ps, fieldValues, databaseZoneId);
                return ps;
            }, keyHolder);
            return keyHolder;
        }

        public static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, int[] types, List<List<FieldValue>> batchParams, ZoneId databaseZoneId) {
            logSqlExecutionStart("批量更新", sql, batchParams);
            int batchSize = DEFAULT_BATCH_SIZE;
            int totalSize = batchParams.size();
            for (int i = 0; i < totalSize; i += batchSize) {
                int end = Math.min(i + batchSize, totalSize);
                List<List<FieldValue>> chunk = batchParams.subList(i, end);
                executeBatch(jdbcTemplate, sql, types, chunk, databaseZoneId);
            }
            logSqlExecutionEnd("批量更新", totalSize);
        }

        private static void executeBatch(JdbcTemplate jdbcTemplate, String sql, int[] types, List<List<FieldValue>> batchParams, ZoneId databaseZoneId) {
            if (batchParams.isEmpty()) {
                return;
            }
            jdbcTemplate.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    List<FieldValue> fieldValues = batchParams.get(i);
                    setPreparedStatementParameters(ps, fieldValues, databaseZoneId);
                }

                @Override
                public int getBatchSize() {
                    return batchParams.size();
                }
            });
        }

        private static void setPreparedStatementParameters(PreparedStatement ps, List<FieldValue> fieldValues, ZoneId databaseZoneId) throws SQLException {
            if (fieldValues == null || fieldValues.isEmpty()) {
                return;
            }
            int index = 1;
            for (FieldValue fieldValue : fieldValues) {
                if (fieldValue == null) {
                    ps.setNull(index++, Types.NULL);
                    continue;
                }
                Object value = processDateTimeValue(fieldValue.getValue(), databaseZoneId);
                SQLType sqlType = fieldValue.sqlType();
                if (value == null) {
                    ps.setNull(index++, sqlType != null ? sqlType.getVendorTypeNumber() : Types.NULL);
                } else if (value instanceof InputStream || value instanceof ByteArrayInputStream) {
                    ps.setBinaryStream(index++, (InputStream) value);
                } else if (value instanceof byte[]) {
                    ps.setBytes(index++, (byte[]) value);
                } else if (sqlType != null) {
                    ps.setObject(index++, value, sqlType);
                } else {
                    ps.setObject(index++, value);
                }
            }
        }

        private static Object processDateTimeValue(Object value, ZoneId databaseZoneId) {
            return SqlExecutor.processDateTimeValue(value, databaseZoneId);
        }

        private static void logSqlExecutionStart(String operation, String sql, Object params) {
            if (log.isTraceEnabled()) {
                log.trace("{}开始{}，SQL: {}，参数: {}", LOG_PREFIX, operation, sql, params);
            }
        }

        private static void logSqlExecutionEnd(String operation, Object result) {
            if (log.isTraceEnabled()) {
                log.trace("{}完成{}，结果: {}", LOG_PREFIX, operation, result);
            }
        }
    }

    /**
     * SQL翻译器内部类，用于生成各种SQL语句
     */
    private static class SqlTranslator {

        /**
         * 生成 INSERT 语句
         *
         * @param table       表名
         * @param fieldValues 字段值列表
         * @return INSERT 语句
         */
        public String insert(String table, List<FieldValue> fieldValues) {
            if (table == null || table.trim().isEmpty()) {
                throw new IllegalArgumentException("表名不能为空");
            }
            if (fieldValues == null || fieldValues.isEmpty()) {
                throw new IllegalArgumentException("字段值列表不能为空");
            }
            List<FieldValue> insertValues = fieldValues.stream().filter(fieldValue -> fieldValue != null).filter(fieldValue -> {
                if (fieldValue.isPrimary()) {
                    return fieldValue.isNotNullVal();
                }
                return true;
            }).collect(Collectors.toList());
            if (insertValues.isEmpty()) {
                throw new IllegalArgumentException("至少需要一个非空的字段值");
            }
            StringBuilder sql = new StringBuilder("INSERT INTO `");
            sql.append(escapeTableName(table)).append("` (`");
            String fieldNames = insertValues.stream().map(FieldValue::getColumnName).map(this::escapeColumnName).collect(Collectors.joining("`,`"));
            sql.append(fieldNames).append("`) VALUES (");
            String placeholders = insertValues.stream().map(fieldValue -> "?").collect(Collectors.joining(", "));
            sql.append(placeholders).append(")");
            return sql.toString();
        }

        /**
         * 生成 UPDATE 语句（根据主键）
         *
         * @param table       表名
         * @param primaryKey  主键字段
         * @param fieldValues 字段值列表
         * @return UPDATE 语句
         */
        public String updateByPrimaryKey(String table, FieldValue primaryKey, List<FieldValue> fieldValues) {
            if (table == null || table.trim().isEmpty()) {
                throw new IllegalArgumentException("表名不能为空");
            }
            if (primaryKey == null) {
                throw new IllegalArgumentException("主键不能为空");
            }
            List<FieldValue> nonNullValues = fieldValues.stream().filter(fieldValue -> fieldValue != null).filter(fieldValue -> !fieldValue.isPrimary()).collect(Collectors.toList());
            if (nonNullValues.isEmpty()) {
                throw new IllegalArgumentException("至少需要一个非主键字段用于更新");
            }
            StringBuilder sql = new StringBuilder("UPDATE `");
            sql.append(escapeTableName(table)).append("` SET ");
            String setClause = nonNullValues.stream().map(fieldValue -> "`" + escapeColumnName(fieldValue.getColumnName()) + "` = ?").collect(Collectors.joining(", "));
            sql.append(setClause);
            sql.append(" WHERE `").append(escapeColumnName(primaryKey.getColumnName())).append("` = ?");
            return sql.toString();
        }

        /**
         * 生成 DELETE 语句（根据主键）
         *
         * @param table      表名
         * @param primaryKey 主键名
         * @param values     主键值列表
         * @return DELETE 语句
         */
        public String deleteByPrimaryKeys(String table, String primaryKey, List<Object> values) {
            if (StringUtils.isBlank(table) || StringUtils.isBlank(primaryKey) || values == null) {
                throw new IllegalArgumentException("表名、主键名和值列表不能为空");
            }
            if (values.isEmpty()) {
                throw new IllegalArgumentException("主键值列表不能为空");
            }
            StringBuilder sql = new StringBuilder("DELETE FROM `");
            sql.append(escapeTableName(table)).append("` WHERE `").append(escapeColumnName(primaryKey)).append("`");
            if (values.size() == 1) {
                sql.append(" = ?");
            } else {
                String placeholders = values.stream().map(value -> "?").collect(Collectors.joining(", "));
                sql.append(" IN (").append(placeholders).append(")");
            }
            return sql.toString();
        }

        private String escapeTableName(String tableName) {
            if (tableName == null) {
                return "";
            }
            return tableName.replaceAll("[^a-zA-Z0-9_\\-\\.]", "");
        }

        private String escapeColumnName(String columnName) {
            if (columnName == null) {
                return "";
            }
            return columnName.replaceAll("[^a-zA-Z0-9_\\-]", "");
        }

        /**
         * 生成 SELECT 查询语句
         *
         * @param table       表名
         * @param fieldValues 字段值列表（用于 WHERE 条件）
         * @param like        是否使用模糊查询
         * @param orders      排序条件
         * @param slice       分页参数
         * @return SELECT 语句
         */
        public String findAll(String table, List<FieldValue> fieldValues, boolean like, List<Order> orders, com.yishuifengxiao.common.tool.entity.Slice slice) {
            StringBuilder sql = new StringBuilder("SELECT * FROM `");
            sql.append(table).append("`");
            if (fieldValues != null && !fieldValues.isEmpty()) {
                List<FieldValue> nonNullValues = fieldValues.stream().filter(fieldValue -> fieldValue != null && fieldValue.isNotNullVal()).collect(Collectors.toList());
                if (!nonNullValues.isEmpty()) {
                    sql.append(" WHERE ");
                    String whereClause = nonNullValues.stream().map(fieldValue -> {
                        if (like && fieldValue.isNotNullVal() && fieldValue.getValue() instanceof String && !fieldValue.isPrimary()) {
                            return "`" + fieldValue.getColumnName() + "` LIKE CONCAT('%', ?, '%')";
                        } else {
                            return "`" + fieldValue.getColumnName() + "` = ?";
                        }
                    }).collect(Collectors.joining(" AND "));
                    sql.append(whereClause);
                }
            }
            if (orders != null && !orders.isEmpty()) {
                List<Order> validOrders = orders.stream().filter(order -> order != null && StringUtils.isNotBlank(order.getOrderName())).collect(Collectors.toList());
                if (!validOrders.isEmpty()) {
                    sql.append(" ORDER BY ");
                    String orderClause = validOrders.stream().map(order -> "`" + order.getOrderName() + "` " + (order.getDirection() == Order.Direction.DESC ? "DESC" : "ASC")).collect(Collectors.joining(", "));
                    sql.append(orderClause);
                }
            }
            if (slice != null) {
                Integer pageNum = slice.getNum() == null ? null : slice.getNum().intValue();
                Integer pageSize = slice.getSize() == null ? null : slice.getSize().intValue();
                if (pageSize != null) {
                    if (pageNum == null) {
                        sql.append(" LIMIT ").append(pageSize);
                    } else {
                        if (pageNum > 0 && pageSize > 0) {
                            int offset = (pageNum - 1) * pageSize;
                            sql.append(" LIMIT ").append(offset).append(",").append(pageSize);
                        }
                    }
                }
            }
            return sql.toString();
        }
    }


    /**
     * 排序对象，用于描述 SQL 查询的排序条件
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Order implements Serializable {
        private static final long serialVersionUID = -3667336189389139540L;
        private String orderName;
        private Order.Direction direction;

        /**
         * 设置为降序
         *
         * @return 当前 Order 对象
         */
        public Order desc() {
            this.direction = Order.Direction.DESC;
            return this;
        }

        /**
         * 获取排序方向字符串
         *
         * @return 排序方向
         */
        public String direction() {
            return (null == this.direction || this.direction == Order.Direction.ASC) ? "ASC" : "DESC";
        }

        /**
         * 设置为升序
         *
         * @return 当前 Order 对象
         */
        public Order asc() {
            this.direction = Order.Direction.ASC;
            return this;
        }

        /**
         * 创建升序 Order 对象
         *
         * @param name 排序字段名
         * @return Order 对象
         */
        public static Order asc(String name) {
            return Order.of(name, Order.Direction.ASC);
        }

        /**
         * 创建降序 Order 对象
         *
         * @param name 排序字段名
         * @return Order 对象
         */
        public static Order desc(String name) {
            return Order.of(name, Order.Direction.DESC);
        }

        /**
         * 设置排序字段名
         *
         * @param name 排序字段名
         * @return 当前 Order 对象
         */
        public Order name(String name) {
            this.orderName = name;
            return this;
        }

        /**
         * 创建 Order 对象
         *
         * @param orderName 排序字段名
         * @param direction 排序方向
         * @return Order 对象
         */
        public static Order of(String orderName, Order.Direction direction) {
            return new Order(orderName, direction);
        }

        /**
         * 创建 Order 对象
         *
         * @param direction 排序方向
         * @return Order 对象
         */
        public static Order of(Order.Direction direction) {
            return new Order(null, direction);
        }

        /**
         * 创建默认升序的 Order 对象
         *
         * @param orderName 排序字段名
         * @return Order 对象
         */
        public static Order of(String orderName) {
            return new Order(orderName, Order.Direction.ASC);
        }

        /**
         * 排序方向枚举
         */
        public enum Direction {
            ASC, DESC
        }
    }
}