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


    /**
     * 根据主键值查询单条记录
     * 执行流程：
     * 1. 验证主键值是否为空，为空则直接返回null
     * 2. 从实体类中提取表名和主键字段信息
     * 3. 设置主键字段的值并构建基于主键的查询SQL
     * 4. 执行查询并返回单个结果对象
     *
     * @param clazz      目标实体类型，用于提取表名和主键字段信息
     * @param primaryKey 主键值，用于构建WHERE条件；如果为null则跳过查询
     * @param <T>        实体类型泛型参数
     * @return 查询到的实体对象，如果主键为空或无匹配记录则返回null
     */
    public <T> T findByPrimaryKey(Class<T> clazz, Object primaryKey) {
        if (primaryKey == null) {
            log.debug("{}主键值为空，跳过查询", LOG_PREFIX);
            return null;
        }
        String tableName = FieldExtractor.extractTableName(clazz);
        FieldValue primaryKeyField = FieldExtractor.extractPrimaryField(clazz);
        primaryKeyField.setValue(primaryKey);
        String sql = sqlTranslator.findAll(tableName, Collections.singletonList(primaryKeyField), false, null,
                new Slice(1, 1));
        return executeSingleQuery(clazz, sql, primaryKeyField);
    }


    /**
     * 统计符合条件的记录总数
     * 执行流程：
     * 1. 验证查询对象是否为空，为空则返回0
     * 2. 从查询对象中提取表名和所有字段值
     * 3. 过滤出非空字段值作为查询条件
     * 4. 如果无非空字段，则统计表的所有记录数
     * 5. 否则根据非空字段构建WHERE条件并统计匹配的记录数
     * 6. 支持模糊查询模式（likeMode）
     *
     * @param t        查询条件对象，其非空字段将作为WHERE条件；如果为null则返回0
     * @param likeMode 是否启用模糊查询模式，true表示对字符串字段使用LIKE匹配，false表示使用精确匹配
     * @param <T>      实体类型泛型参数
     * @return 符合条件的记录总数，如果查询对象为空或发生异常则返回0
     */
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


    

    /**
     * 查询单条符合条件的记录
     * 执行流程：
     * 1. 验证查询对象是否为空，为空则返回null
     * 2. 从查询对象中提取表名和所有字段值
     * 3. 过滤出非空字段值作为WHERE条件
     * 4. 处理排序参数，将字段名转换为数据库列名
     * 5. 构建带LIMIT 1的查询SQL以获取单条记录
     * 6. 支持模糊查询模式（likeMode）和自定义排序
     *
     * @param t        查询条件对象，其非空字段将作为WHERE条件；如果为null则返回null
     * @param likeMode 是否启用模糊查询模式，true表示对字符串字段使用LIKE匹配，false表示使用精确匹配
     * @param orders   排序参数数组，指定查询结果的排序规则；可以为空
     * @param <T>      实体类型泛型参数
     * @return 查询到的单个实体对象，如果没有匹配记录或查询对象为空则返回null
     */
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




    

    /**
     * 查询所有符合条件的记录列表
     * 执行流程：
     * 1. 验证查询对象是否为空，为空则返回空列表
     * 2. 从查询对象中提取表名和所有字段值
     * 3. 过滤出非空字段值作为WHERE条件
     * 4. 处理排序参数，将字段名转换为数据库列名
     * 5. 构建不带分页限制的查询SQL以获取所有匹配记录
     * 6. 支持模糊查询模式（likeMode）和自定义排序
     *
     * @param t        查询条件对象，其非空字段将作为WHERE条件；如果为null则返回空列表
     * @param likeMode 是否启用模糊查询模式，true表示对字符串字段使用LIKE匹配，false表示使用精确匹配
     * @param orders   排序参数数组，指定查询结果的排序规则；可以为空
     * @param <T>      实体类型泛型参数
     * @return 符合条件的实体对象列表，如果没有匹配记录或查询对象为空则返回空列表
     */
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




    

    /**
     * 根据分页查询对象执行分页查询
     * 执行流程：
     * 1. 验证分页查询对象是否为空，为空则返回空分页结果
     * 2. 从PageQuery中提取实际的查询条件对象
     * 3. 委托给重载的findPage方法执行具体的分页查询逻辑
     *
     * @param pageQuery 分页查询对象，包含查询条件、分页参数等信息；如果为null则返回空分页结果
     * @param likeMode  是否启用模糊查询模式，true表示对字符串字段使用LIKE匹配，false表示使用精确匹配
     * @param orders    排序参数数组，指定查询结果的排序规则；可以为空
     * @param <T>       实体类型泛型参数
     * @return 分页结果对象，包含数据列表和总记录数；如果pageQuery为空则返回空分页结果
     */
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




    

    /**
     * 执行分页查询并返回分页结果
     * 执行流程：
     * 1. 初始化分页参数，如果slice为空则使用默认值（每页10条，第1页）
     * 2. 验证查询对象是否为空，为空则返回空分页结果
     * 3. 从查询对象中提取表名和所有字段值
     * 4. 过滤出非空字段值作为WHERE条件
     * 5. 处理排序参数，将字段名转换为数据库列名
     * 6. 构建带分页限制的查询SQL（使用LIMIT/OFFSET）
     * 7. 构建不带排序的COUNT查询SQL以获取总记录数
     * 8. 分别执行数据查询和总数查询
     * 9. 组装分页结果对象并返回
     *
     * @param t        查询条件对象，其非空字段将作为WHERE条件；如果为null则返回空分页结果
     * @param likeMode 是否启用模糊查询模式，true表示对字符串字段使用LIKE匹配，false表示使用精确匹配
     * @param slice    分页参数对象，包含页码和每页大小；如果为null则使用默认值（每页10条，第1页）
     * @param orders   排序参数数组，指定查询结果的排序规则；可以为空
     * @param <T>      实体类型泛型参数
     * @return 分页结果对象，包含数据列表、总记录数和分页参数；如果查询对象为空则返回空分页结果
     */
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




    

    /**
     * 执行原生SQL查询并返回结果列表
     * 执行流程：
     * 1. 判断目标类型是否为基本结果类型（如基本类型、String、Date等）
     * 2. 如果是基本类型，直接使用JdbcTemplate的queryForList方法执行查询
     * 3. 如果是复杂对象类型，使用SimpleRowMapper进行行映射，支持时区转换
     * 4. 如果查询结果为null，则返回空列表
     *
     * @param clazz  目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql    要执行的SQL查询语句
     * @param params SQL参数数组，按顺序对应SQL中的占位符；可以为空
     * @param <T>    结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录或结果为null则返回空列表
     */
    public <T> List<T> findAll(Class<T> clazz, String sql, Object... params) {
        List<T> results;
        if (FieldExtractor.isBasicResult(clazz)) {
            results = this.jdbcTemplate.queryForList(sql, clazz, params);
        } else {
            results = jdbcTemplate.query(sql, new SimpleRowMapper<>(clazz, this.databaseZoneId), params);
        }
        return results == null ? Collections.emptyList() : results;
    }




    

    /**
     * 执行命名参数SQL查询并返回结果列表（使用对象属性作为参数）
     * 执行流程：
     * 1. 验证参数对象是否为空，为空则委托给无参版本的find方法
     * 2. 将参数对象转换为BeanPropertySqlParameterSource，自动映射对象属性到SQL命名参数
     * 3. 委托给重载的find方法执行具体的查询逻辑
     *
     * @param clazz 目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql   要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param param 参数对象，其属性值将自动映射到SQL中的命名参数；如果为null则执行无参查询
     * @param <T>   结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录则返回空列表
     */
    public <T> List<T> find(Class<T> clazz, String sql, Object param) {
        if (null == param) {
            return this.find(clazz, sql, (SqlParameterSource) null);
        }
        SqlParameterSource params = new BeanPropertySqlParameterSource(param);
        return this.find(clazz, sql, params);
    }




    

    /**
     * 执行命名参数SQL查询并返回结果列表（使用Map作为参数）
     * 执行流程：
     * 1. 验证参数Map是否为空或null，为空则委托给无参版本的find方法
     * 2. 如果配置了数据库时区，对日期时间类型参数进行时区转换处理
     * 3. 将参数Map转换为MapSqlParameterSource，支持SQL命名参数绑定
     * 4. 委托给重载的find方法执行具体的查询逻辑
     *
     * @param clazz  目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql    要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param params 参数Map，key对应SQL中的命名参数名称，value为参数值；如果为null或空则执行无参查询
     * @param <T>    结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录则返回空列表
     */
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




    

    /**
     * 执行命名参数SQL查询并返回结果列表（核心查询方法）
     * 执行流程：
     * 1. 验证SQL语句是否为空，为空则抛出异常
     * 2. 清理SQL语句中的换行符和回车符，便于日志记录和调试
     * 3. 如果启用TRACE日志，记录执行的SQL和参数信息
     * 4. 根据是否有参数选择不同的执行路径：
     *    - 无参数：直接使用JdbcTemplate执行查询
     *    - 有参数：使用NamedParameterJdbcTemplate执行查询
     * 5. 根据目标类型选择查询方式：
     *    - 基本类型：使用queryForList直接获取结果
     *    - 复杂对象：使用SimpleRowMapper进行行映射，支持时区转换
     *
     * @param clazz  目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql    要执行的SQL查询语句，支持命名参数（如:name, :age等）；不能为空
     * @param params SQL参数源对象，提供命名参数的值；可以为null表示无参数查询
     * @param <T>    结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录则返回空列表
     * @throws UncheckedException 当SQL语句为空时抛出异常
     */
    public <T> List<T> find(Class<T> clazz, String sql, SqlParameterSource params) {
        if (StringUtils.isBlank(sql)) {
            throw new UncheckedException(JdbcError.SQL_IS_NULL, "SQL语句不能为空");
        }
        sql = sql.replaceAll("\r", "  ").replaceAll("\n", "  ").trim();
        if (log.isTraceEnabled()) {
            log.trace("{}执行查询：{}，参数：{}", LOG_PREFIX, sql, params);
        }
        if (null == params) {
            return FieldExtractor.isBasicResult(clazz) ? this.jdbcTemplate.queryForList(sql, clazz) :
                    this.jdbcTemplate.query(sql, new SimpleRowMapper<>(clazz, this.databaseZoneId));
        }
        return FieldExtractor.isBasicResult(clazz) ?
                this.namedParameterJdbcTemplate.queryForList(sql, params, clazz) :
                this.namedParameterJdbcTemplate.query(sql, params, new SimpleRowMapper<>(clazz, this.databaseZoneId));
    }




    

    /**
     * 执行原生SQL分页查询并返回分页结果
     * 执行流程：
     * 1. 验证必要参数（clazz和sql）是否完整，不完整则返回空分页结果
     * 2. 初始化分页参数，如果slice为空则使用默认值（每页10条，第1页）
     * 3. 构建带分页限制的查询SQL（使用LIMIT/OFFSET）
     * 4. 执行数据查询获取当前页的记录列表
     * 5. 构建COUNT查询SQL以获取总记录数
     * 6. 执行总数查询并提取总记录数
     * 7. 组装分页结果对象并返回
     * 8. 捕获异常时记录错误日志并返回空分页结果
     *
     * @param clazz  目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param slice  分页参数对象，包含页码和每页大小；如果为null则使用默认值（每页10条，第1页）
     * @param sql    要执行的SQL查询语句，支持命名参数（如:name, :age等）；不能为空
     * @param params 参数Map，key对应SQL中的命名参数名称，value为参数值；可以为null或空
     * @param <T>    结果类型泛型参数
     * @return 分页结果对象，包含数据列表、总记录数和分页参数；如果参数不完整或发生异常则返回空分页结果
     */
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




    

    /**
     * 根据主键更新记录（全量更新）
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则返回0表示未更新任何记录
     * 2. 从实体对象中提取表名和所有字段值
     * 3. 分离主键字段和非主键字段
     * 4. 构建参数列表：先添加非主键字段（用于SET子句），最后添加主键字段（用于WHERE条件）
     * 5. 生成UPDATE SQL语句，更新所有非主键字段
     * 6. 执行SQL并返回受影响的行数
     *
     * @param t 实体对象，包含要更新的字段值和主键值；如果为null则返回0
     * @param <T> 实体类型泛型参数
     * @return 受影响的行数，如果实体对象为空则返回0
     */
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




    

    /**
     * 根据主键选择性更新记录（只更新非空字段）
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则返回0表示未更新任何记录
     * 2. 从实体对象中提取表名和所有字段值
     * 3. 分离主键字段和非主键字段
     * 4. 过滤出非空且非主键的字段，仅更新这些字段
     * 5. 构建参数列表：先添加非空非主键字段（用于SET子句），最后添加主键字段（用于WHERE条件）
     * 6. 生成UPDATE SQL语句，只更新非空字段
     * 7. 执行SQL并返回受影响的行数
     *
     * @param t 实体对象，包含要更新的字段值和主键值；只有非null的字段会被更新；如果为null则返回0
     * @param <T> 实体类型泛型参数
     * @return 受影响的行数，如果实体对象为空则返回0
     */
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




    

    /**
     * 根据主键删除记录（支持单个或多个主键）
     * 执行流程：
     * 1. 验证主键参数数组是否为空，为空则返回0表示未删除任何记录
     * 2. 过滤出有效的主键值：排除null值和空字符串
     * 3. 如果无有效主键值，则返回0
     * 4. 从实体类中提取表名和主键字段信息
     * 5. 根据有效主键数量设置主键字段的值：
     *    - 单个主键：直接设置该主键值
     *    - 多个主键：设置为主键值列表，生成IN条件
     * 6. 生成DELETE SQL语句
     * 7. 执行SQL并返回受影响的行数
     *
     * @param clazz     实体类型，用于提取表名和主键字段信息
     * @param primaryKeys 主键值数组，支持单个或多个主键；null值和空字符串会被过滤；如果为空或无有效值则返回0
     * @param <T>       实体类型泛型参数
     * @return 受影响的行数，如果主键参数为空或无有效值则返回0
     */
    public <T> int deleteByPrimaryKey(Class<T> clazz, Object... primaryKeys) {
        if (primaryKeys == null || primaryKeys.length == 0) {
            log.debug("{}主键参数为空，跳过删除", LOG_PREFIX);
            return 0;
        }
        List<Object> validPrimaryKeys =
                Arrays.stream(primaryKeys).filter(Objects::nonNull).filter(key -> !(key instanceof String) || StringUtils.isNotBlank(((String) key).trim())).collect(Collectors.toList());
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




    

    /**
     * 插入新记录并返回生成的主键
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则记录警告日志并返回null
     * 2. 从实体对象中提取表名和所有字段值
     * 3. 检查主键字段：如果主键存在但值为null，则从插入字段列表中移除主键（通常用于自增主键）
     * 4. 生成INSERT SQL语句
     * 5. 执行插入操作并返回包含生成主键的KeyHolder对象
     *
     * @param t 实体对象，包含要插入的字段值；如果为null则返回null
     * @param <T> 实体类型泛型参数
     * @return 包含生成主键的KeyHolder对象，如果实体对象为空则返回null
     */
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




    

    /**
     * 保存或更新记录（根据主键是否存在决定执行插入还是更新）
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则记录警告日志并返回null
     * 2. 从实体对象中提取所有字段值
     * 3. 查找具有非空值的主键字段
     * 4. 如果主键不存在或主键值为null，则执行插入操作
     * 5. 如果主键存在且有值，则检查数据库中是否已存在该主键对应的记录：
     *    - 如果记录不存在，执行插入操作
     *    - 如果记录存在，执行全量更新操作
     * 6. 构造并返回包含主键信息的KeyHolder对象
     *
     * @param t 实体对象，包含要保存或更新的字段值；如果为null则返回null
     * @param <T> 实体类型泛型参数
     * @return 包含主键信息的KeyHolder对象，如果实体对象为空则返回null
     */
    public <T> KeyHolder saveOrUpdate(T t) {
        if (t == null) {
            log.warn("{}保存或更新数据为空", LOG_PREFIX);
            return null;
        }
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        FieldValue primaryKeyValue =
                fieldValues.stream().filter(Objects::nonNull).filter(field -> field.isPrimary() && field.isNotNullVal()).findFirst().orElse(null);
        if (null == primaryKeyValue || primaryKeyValue.isNullVal()) {
            return this.insert(t);
        } else {
            String tableName = FieldExtractor.extractTableName(t.getClass());
            String sql = "SELECT COUNT(1) FROM `" + tableName + "` WHERE `" + primaryKeyValue.getColumnName() + "` = " +
                    ":id";
            List<Long> result = this.find(Long.class, sql, Map.of("id", primaryKeyValue.getValue()));
            if (result == null || result.isEmpty() || result.get(0) <= 0) {
                return this.insert(t);
            }
            this.updateByPrimaryKey(t);
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            Map<String, Object> keys = Collections.singletonMap(primaryKeyValue.getField().getName(),
                    primaryKeyValue.getValue());
            keyHolder.getKeyList().add(keys);
            return keyHolder;
        }
    }




    

    /**
     * 批量插入记录
     * 执行流程：
     * 1. 验证集合是否为空或null，为空则直接返回
     * 2. 过滤出非空的实体对象，避免处理null元素
     * 3. 使用第一个有效实体对象提取表名、字段信息和主键字段
     * 4. 生成INSERT SQL语句模板
     * 5. 提取所有字段的SQL类型数组，用于PreparedStatement参数设置
     * 6. 遍历所有有效实体对象，提取每个对象的字段值作为批量参数：
     *    - 如果主键不存在或主键值为null，则从参数字段列表中移除主键（通常用于自增主键）
     * 7. 执行批量更新操作，内部采用分批处理以提高性能
     *
     * @param list 实体对象集合，包含要批量插入的数据；如果为null或空集合则直接返回
     * @param <T>  实体类型泛型参数
     */
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




    

    /**
     * 将FieldValue列表转换为数组
     * 用于将集合类型的字段值列表转换为数组格式，以便传递给SQL执行器进行参数绑定
     *
     * @param fieldValues FieldValue对象列表，包含字段信息和值；如果为null则返回空数组
     * @return FieldValue数组，如果输入列表为null则返回长度为0的空数组
     */
    private FieldValue[] toArray(List<FieldValue> fieldValues) {
        if (null == fieldValues) {
            return new FieldValue[0];
        }
        return fieldValues.toArray(new FieldValue[fieldValues.size()]);
    }




    

    /**
     * 处理排序参数，将Java字段名转换为数据库列名
     * 执行流程：
     * 1. 验证排序参数数组是否为空，为空则返回null
     * 2. 过滤掉null的Order对象和排序字段名为空的Order对象
     * 3. 遍历有效的Order对象，尝试在字段值列表中查找匹配的字段：
     *    - 通过忽略大小写比较Java字段名来匹配
     *    - 如果找到匹配字段，则获取其对应的数据库列名并更新Order对象
     *    - 如果未找到匹配字段，则保持原排序字段名不变
     * 4. 收集处理后的Order对象列表并返回
     *
     * @param fieldValues 字段值列表，用于查找Java字段名与数据库列名的映射关系
     * @param orders      排序参数数组，包含排序字段名和排序方向；可以为空或null
     * @return 处理后的Order对象列表，其中排序字段名已转换为数据库列名；如果输入为空则返回null
     */
    private List<Order> createOrder(List<FieldValue> fieldValues, Order... orders) {
        if (orders == null || orders.length == 0) {
            return null;
        }
        return Arrays.stream(orders).filter(Objects::nonNull).filter(order -> StringUtils.isNotBlank(order.getOrderName())).map(order -> {
            String columnName =
                    fieldValues.stream().filter(field -> field != null && field.getField() != null).filter(field -> field.getField().getName().equalsIgnoreCase(order.getOrderName())).map(FieldValue::getColumnName).findFirst().orElse(null);
            if (StringUtils.isNotBlank(columnName)) {
                order.setOrderName(columnName);
            }
            return order;
        }).collect(Collectors.toList());
    }




    

    /**
     * 执行单条记录查询
     * 委托给SqlExecutor执行SQL查询，并从结果列表中提取第一条记录返回
     *
     * @param clazz 目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql   要执行的SQL查询语句
     * @param args  SQL参数数组，按顺序对应SQL中的占位符；可以为空
     * @param <T>   结果类型泛型参数
     * @return 查询到的单个实体对象，如果没有匹配记录或结果为null则返回null
     */
    private <T> T executeSingleQuery(Class<T> clazz, String sql, FieldValue... args) {
        List<T> list = SqlExecutor.findAll(jdbcTemplate, clazz, sql, args, this.databaseZoneId);
        return list == null || list.isEmpty() ? null : list.get(0);
    }




    

    /**
     * 执行列表查询并返回结果集合
     * 委托给SqlExecutor执行SQL查询，如果结果为null则返回空列表，避免返回null导致调用方出现空指针异常
     *
     * @param clazz 目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql   要执行的SQL查询语句
     * @param args  SQL参数数组，按顺序对应SQL中的占位符；可以为空
     * @param <T>   结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录或结果为null则返回空列表
     */
    private <T> List<T> executeListQuery(Class<T> clazz, String sql, FieldValue... args) {
        List<T> list = SqlExecutor.findAll(jdbcTemplate, clazz, sql, args, this.databaseZoneId);
        return list == null ? Collections.emptyList() : list;
    }




    

    /**
     * 执行计数查询并返回总记录数
     * 委托给SqlExecutor执行COUNT SQL查询，从结果列表中提取第一个值作为总数；如果结果为空或null则返回0
     *
     * @param sql 要执行的COUNT SQL查询语句
     * @param args SQL参数数组，按顺序对应SQL中的占位符；可以为空
     * @return 查询到的总记录数，如果没有匹配记录或结果为null/空则返回0
     */
    private Long executeCountQuery(String sql, FieldValue... args) {
        List<Long> numbers = SqlExecutor.findAll(jdbcTemplate, Long.class, sql, args, this.databaseZoneId);
        return numbers == null || numbers.isEmpty() ? 0L : numbers.get(0);
    }




    

    /**
     * 过滤出非空字段值列表
     * 从给定的FieldValue列表中筛选出值不为null的字段，通常用于构建动态查询条件或选择性更新
     *
     * @param fieldValues FieldValue对象列表，包含字段信息和值
     * @return 仅包含非空值的FieldValue列表
     */
    private List<FieldValue> extractNonNullFieldValues(List<FieldValue> fieldValues) {
        return fieldValues.stream().filter(FieldValue::isNotNullVal).collect(Collectors.toList());
    }




    

    /**
     * 构建COUNT查询SQL语句
     * 执行流程：
     * 1. 验证输入SQL是否为空，为空则返回默认的COUNT语句
     * 2. 清理SQL语句：去除首尾空格和末尾的分号
     * 3. 移除ORDER BY子句，因为计数查询不需要排序
     * 4. 检测是否为复杂查询（包含UNION、DISTINCT、GROUP BY、HAVING等）：
     *    - 如果是复杂查询，将整个SQL作为子查询包裹在COUNT中
     * 5. 验证是否为SELECT语句，非SELECT语句也作为子查询处理
     * 6. 查找主查询的FROM关键字位置（排除子查询中的FROM）：
     *    - 通过检查FROM之前是否包含未闭合的左括号来判断是否为主查询
     * 7. 如果找到FROM关键字，将SELECT部分替换为SELECT COUNT(1)
     * 8. 如果无法找到FROM关键字，则将整个SQL作为子查询处理
     *
     * @param sql 原始查询SQL语句
     * @return 优化后的COUNT查询SQL语句
     */
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




    

    /**
     * 检测SQL查询是否为复杂查询，无法直接优化为简单COUNT查询
     * 复杂查询特征包括：UNION联合查询、DISTINCT去重、GROUP BY分组、HAVING过滤等
     * 对于复杂查询，需要将其作为子查询包裹在COUNT中以确保计数准确性
     *
     * @param sql 待检测的SQL查询语句
     * @return 如果是复杂查询返回true，否则返回false
     */
    private boolean isComplexQueryForCount(String sql) {
        if (StringUtils.isBlank(sql)) {
            return false;
        }
        String upperSql = sql.toUpperCase();
        return upperSql.contains(" UNION ") || upperSql.contains(" UNION ALL ") || upperSql.contains(" DISTINCT ") || upperSql.contains(" GROUP BY ") || upperSql.contains(" HAVING ");
    }




    

    /**
     * 构建带分页限制的SQL语句
     * 执行流程：
     * 1. 验证输入参数，如果SQL为空或分页对象为null则直接返回原SQL
     * 2. 清理SQL语句：去除首尾空格和末尾的分号
     * 3. 提取页码和每页大小，处理可能的null值
     * 4. 根据分页参数生成LIMIT子句：
     *    - 如果只有pageSize没有pageNum，仅限制返回记录数（LIMIT pageSize）
     *    - 如果同时有pageNum和pageSize，计算偏移量并生成分页查询（LIMIT offset,pageSize）
     *    - 偏移量计算公式：(pageNum - 1) * pageSize
     * 5. 如果分页参数无效，返回原始SQL
     *
     * @param sql   原始查询SQL语句
     * @param slice 分页参数对象，包含页码和每页大小信息
     * @return 添加了LIMIT子句的分页SQL语句，如果参数无效则返回原SQL
     */
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




    

    /**
     * 处理SQL参数中的日期时间类型，进行时区转换
     * 遍历参数Map中的所有值，递归调用processDateTimeValueRecursive方法处理每个参数：
     * - 对于日期时间类型参数，根据数据库时区和应用时区的差异进行转换
     * - 对于非日期时间类型参数，保持原值不变
     * - 支持嵌套结构（Map、Collection、Array）的递归处理
     *
     * @param params 原始参数Map，key为参数名，value为参数值
     * @return 处理后的参数Map，其中日期时间类型已根据时区进行转换
     */
    private Map<String, Object> processDateTimeParameters(Map<String, Object> params) {
        Map<String, Object> processedParams = new HashMap<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            processedParams.put(entry.getKey(), processDateTimeValueRecursive(entry.getValue()));
        }
        return processedParams;
    }




    

    /**
     * 递归处理参数值中的日期时间类型，支持复杂嵌套结构
     * 执行流程：
     * 1. 验证参数值是否为null，为null则直接返回
     * 2. 根据参数值的类型进行不同的处理：
     *    - Collection类型（List/Set）：遍历集合元素，递归处理每个元素，并保持原有集合类型
     *    - 数组类型：遍历数组元素，递归处理每个元素，返回处理后的新数组
     *    - Map类型：遍历Map条目，递归处理每个value值，key保持不变
     *    - 其他类型：调用processSingleDateTimeValue处理单个日期时间值
     * 3. 通过递归处理，确保嵌套结构中的所有日期时间对象都能被正确转换
     *
     * @param value 待处理的参数值，可以是单个对象或包含嵌套结构的集合/数组/Map
     * @return 处理后的参数值，日期时间类型已根据时区进行转换，结构类型保持不变
     */
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




    

    /**
     * 处理单个日期时间值，根据数据库时区进行转换
     * 执行流程：
     * 1. 验证输入值是否为null，为null则直接返回
     * 2. 如果未配置数据库时区或应用时区与数据库时区相同，则仅转换为java.sql类型而不进行时区转换
     * 3. 如果时区不同，则根据具体的日期时间类型进行转换：
     *    - Date/LocalDateTime/ZonedDateTime/OffsetDateTime/Instant/Timestamp：
     *      将应用时区的时间转换为数据库时区的时间，并返回Timestamp对象
     *    - LocalDate：转换为java.sql.Date，不涉及时区转换（只包含日期）
     *    - LocalTime：转换为java.sql.Time，不涉及时区转换（只包含时间）
     *    - java.sql.Date/Time：已经是SQL类型，直接返回
     * 4. 如果转换过程中发生异常，记录警告日志并返回原始值
     *
     * @param value 待处理的单个日期时间对象，支持多种日期时间类型
     * @return 转换后的日期时间对象，通常为java.sql.Timestamp/Date/Time类型；如果转换失败则返回原始值
     */
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




    

    /**
     * 将Java日期时间类型转换为对应的java.sql类型，不进行任何时区转换
     * 适用于以下场景：
     * - 未配置数据库时区
     * - 应用时区与数据库时区相同
     * 
     * 支持的转换类型：
     * - LocalDateTime -> Timestamp
     * - LocalDate -> java.sql.Date
     * - LocalTime -> Time
     * - ZonedDateTime -> Timestamp（提取本地日期时间部分）
     * - OffsetDateTime -> Timestamp（提取本地日期时间部分）
     * - Instant -> Timestamp
     * - Date -> Timestamp
     * - 其他类型保持不变
     *
     * @param value 待转换的日期时间对象，支持多种Java日期时间类型
     * @return 转换后的java.sql类型对象，如果输入为null或非日期时间类型则返回原值
     */
    private static Object convertToJavaSqlTypeNoTimezone(Object value) {
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




    

    /**
     * 判断两个时区在当前时刻是否具有相同的偏移量
     * 执行流程：
     * 1. 验证时区对象是否为null，任一为null则返回false
     * 2. 如果两个时区对象相等（引用相同或ID相同），直接返回true
     * 3. 获取当前时刻的Instant对象
     * 4. 比较两个时区在当前时刻的UTC偏移量是否相同
     *    注意：某些时区在不同季节可能有不同的偏移量（如夏令时），此方法仅比较当前时刻的偏移量
     *
     * @param zone1 第一个时区对象
     * @param zone2 第二个时区对象
     * @return 如果两个时区在当前时刻具有相同的UTC偏移量则返回true，否则返回false
     */
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






    public static class SqlExecutor {
        

        /**
         * 执行SQL更新操作（INSERT、UPDATE、DELETE）
         * 执行流程：
         * 1. 记录SQL执行开始日志，包含SQL语句和参数信息
         * 2. 根据是否有参数选择不同的执行方式：
         *    - 无参数：直接调用jdbcTemplate.update执行SQL
         *    - 有参数：使用PreparedStatement回调方式，先预编译SQL，再设置参数，最后执行
         * 3. 记录SQL执行结束日志，包含受影响的行数
         * 4. 返回受影响的行数
         *
         * @param jdbcTemplate JdbcTemplate实例，用于执行SQL操作
         * @param sql          要执行的SQL语句，支持占位符（?）
         * @param args         SQL参数数组，按顺序对应SQL中的占位符；可以为空或null表示无参数
         * @return 受影响的行数
         */
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




        public static <T> List<T> findAll(JdbcTemplate jdbcTemplate, Class<T> clazz, String sql, FieldValue[] args,
                                          ZoneId databaseZoneId) {
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

        public static KeyHolder update(JdbcTemplate jdbcTemplate, String sql, List<FieldValue> fieldValues,
                                       ZoneId databaseZoneId) {
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

        private static KeyHolder updateWithLargeFields(JdbcTemplate jdbcTemplate, String sql,
                                                       List<FieldValue> fieldValues, ZoneId databaseZoneId) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                setPreparedStatementParameters(ps, fieldValues, databaseZoneId);
                return ps;
            }, keyHolder);
            return keyHolder;
        }

        public static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, int[] types,
                                       List<List<FieldValue>> batchParams, ZoneId databaseZoneId) {
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

        private static void executeBatch(JdbcTemplate jdbcTemplate, String sql, int[] types,
                                         List<List<FieldValue>> batchParams, ZoneId databaseZoneId) {
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

        private static void setPreparedStatementParameters(PreparedStatement ps, List<FieldValue> fieldValues,
                                                           ZoneId databaseZoneId) throws SQLException {
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
            if (value == null) {
                return null;
            }
            if (databaseZoneId == null) {
                return convertToJavaSqlTypeNoTimezone(value);
            }
            ZoneId appZone = ZoneId.systemDefault();
            if (isSameTimezone(appZone, databaseZoneId)) {
                return convertToJavaSqlTypeNoTimezone(value);
            }
            try {
                if (value instanceof Date) {
                    Date date = (Date) value;
                    Instant instant = date.toInstant();
                    ZonedDateTime appTime = instant.atZone(appZone);
                    ZonedDateTime dbTime = appTime.withZoneSameInstant(databaseZoneId);
                    return Timestamp.valueOf(dbTime.toLocalDateTime());
                } else if (value instanceof LocalDateTime) {
                    LocalDateTime localDateTime = (LocalDateTime) value;
                    ZonedDateTime appTime = localDateTime.atZone(appZone);
                    ZonedDateTime dbTime = appTime.withZoneSameInstant(databaseZoneId);
                    return Timestamp.valueOf(dbTime.toLocalDateTime());
                } else if (value instanceof ZonedDateTime) {
                    ZonedDateTime zonedDateTime = (ZonedDateTime) value;
                    ZonedDateTime dbTime = zonedDateTime.withZoneSameInstant(databaseZoneId);
                    return Timestamp.valueOf(dbTime.toLocalDateTime());
                } else if (value instanceof OffsetDateTime) {
                    OffsetDateTime offsetDateTime = (OffsetDateTime) value;
                    ZonedDateTime dbTime = offsetDateTime.atZoneSameInstant(databaseZoneId);
                    return Timestamp.valueOf(dbTime.toLocalDateTime());
                } else if (value instanceof Instant) {
                    Instant instant = (Instant) value;
                    ZonedDateTime dbTime = instant.atZone(databaseZoneId);
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
                    ZonedDateTime dbTime = appTime.withZoneSameInstant(databaseZoneId);
                    return Timestamp.valueOf(dbTime.toLocalDateTime());
                }
            } catch (Exception e) {
                log.warn("{}日期时间转换失败，使用原始值: {}", LOG_PREFIX, e.getMessage());
            }
            return value;
        }

        private static Object convertToJavaSqlTypeNoTimezone(Object value) {
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
            List<FieldValue> insertValues =
                    fieldValues.stream().filter(fieldValue -> fieldValue != null).filter(fieldValue -> {
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
            String fieldNames =
                    insertValues.stream().map(FieldValue::getColumnName).map(this::escapeColumnName).collect(Collectors.joining("`,`"));
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
            List<FieldValue> nonNullValues =
                    fieldValues.stream().filter(fieldValue -> fieldValue != null).filter(fieldValue -> !fieldValue.isPrimary()).collect(Collectors.toList());
            if (nonNullValues.isEmpty()) {
                throw new IllegalArgumentException("至少需要一个非主键字段用于更新");
            }
            StringBuilder sql = new StringBuilder("UPDATE `");
            sql.append(escapeTableName(table)).append("` SET ");
            String setClause =
                    nonNullValues.stream().map(fieldValue -> "`" + escapeColumnName(fieldValue.getColumnName()) + "` " +
                            "= ?").collect(Collectors.joining(", "));
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
        public String findAll(String table, List<FieldValue> fieldValues, boolean like, List<Order> orders,
                              com.yishuifengxiao.common.tool.entity.Slice slice) {
            StringBuilder sql = new StringBuilder("SELECT * FROM `");
            sql.append(table).append("`");
            if (fieldValues != null && !fieldValues.isEmpty()) {
                List<FieldValue> nonNullValues =
                        fieldValues.stream().filter(fieldValue -> fieldValue != null && fieldValue.isNotNullVal()).collect(Collectors.toList());
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
                List<Order> validOrders =
                        orders.stream().filter(order -> order != null && StringUtils.isNotBlank(order.getOrderName())).collect(Collectors.toList());
                if (!validOrders.isEmpty()) {
                    sql.append(" ORDER BY ");
                    String orderClause =
                            validOrders.stream().map(order -> "`" + order.getOrderName() + "` " + (order.getDirection() == Order.Direction.DESC ? "DESC" : "ASC")).collect(Collectors.joining(", "));
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