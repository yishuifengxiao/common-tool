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
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JDBC辅助工具类，提供基于命名参数的数据库操作方法
 * <p>
 * 主要功能包括：
 * 1. 基于主键的增删改查操作
 * 2. 动态条件查询和统计
 * 3. 分页查询支持
 * 4. 批量操作支持
 * 5. 函数式SQL构建接口
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class JdbcHelper {
    private static final String LOG_PREFIX = "【yishuifengxiao】";
    private static final int DEFAULT_BATCH_SIZE = 500;

    private JdbcTemplate jdbcTemplate;
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private ZoneId databaseZoneId = ZoneId.systemDefault();

    public JdbcHelper() {
    }

    public JdbcHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initialize();
    }

    /**
     * 初始化NamedParameterJdbcTemplate和数据库时区信息
     * <p>
     * 执行流程：
     * 1. 验证JdbcTemplate是否已设置
     * 2. 创建NamedParameterJdbcTemplate实例
     * 3. 检测数据库时区信息
     * 4. 记录初始化状态
     * </p>
     *
     * @throws UncheckedException 当初始化失败时抛出异常
     */
    private void initialize() {
        if (this.jdbcTemplate == null) {
            log.warn("{}JdbcTemplate为空，跳过初始化", LOG_PREFIX);
            return;
        }
        try {
            this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this.jdbcTemplate);
            this.databaseZoneId = ZoneIdDetector.detectDatabaseTimezone(jdbcTemplate.getDataSource().getConnection());
            log.debug("{}SQL执行器初始化成功，时区: {}", LOG_PREFIX, this.databaseZoneId);
        } catch (Exception e) {
            log.error("{}SQL执行器初始化失败", LOG_PREFIX, e);
            throw new UncheckedException(JdbcError.SQL_HELPER_INIT_ERROR, "SQL执行器初始化失败");
        }
    }

    /**
     * 过滤出有效的查询字段值列表
     * <p>
     * 过滤规则：
     * 1. 排除值为null的字段
     * 2. 对于String类型字段，额外排除空白字符串
     * 3. 其他类型字段只要非null即为有效
     * </p>
     *
     * @param fieldValueList 原始字段值列表
     * @return 过滤后的有效字段值列表
     */
    private List<FieldValue> filterValidFieldValues(List<FieldValue> fieldValueList) {
        return fieldValueList.stream().filter(field -> {
            if (null == field.getValue()) {
                return false;
            }
            if (field.getValue() instanceof String) {
                return !StringUtils.isBlank((String) field.getValue());
            }
            return true;
        }).collect(Collectors.toList());
    }

    /**
     * 构建WHERE条件的SQL片段
     * <p>
     * 根据字段值和模糊查询模式生成对应的WHERE条件：
     * - 模糊模式：对String类型字段使用LIKE CONCAT('%', :param, '%')
     * - 精确模式：对所有字段使用 = :param
     * </p>
     *
     * @param fieldValues 字段值列表
     * @param likeMode    是否启用模糊查询模式
     * @param params      SQL参数源，用于添加参数值
     * @return WHERE条件的SQL片段（不包含WHERE关键字）
     */
    private String buildWhereClause(List<FieldValue> fieldValues, boolean likeMode, MapSqlParameterSource params) {
        StringBuilder whereClause = new StringBuilder();
        for (FieldValue fieldValue : fieldValues) {
            if (likeMode && fieldValue.getValue() instanceof String) {
                whereClause.append(" AND `").append(fieldValue.getColumnName()).append("` LIKE CONCAT('%', :").append(fieldValue.getColumnName()).append(", '%')");
            } else {
                whereClause.append(" AND `").append(fieldValue.getColumnName()).append("`= :").append(fieldValue.getColumnName());
            }
            params.addValue(fieldValue.getColumnName(), fieldValue.getValue(), fieldValue.sqlType());
        }
        return whereClause.toString();
    }

    /**
     * 构建ORDER BY子句的SQL片段
     *
     * @param orders 排序条件列表
     * @return ORDER BY子句的SQL片段（包含ORDER BY关键字），如果无排序条件则返回空字符串
     */
    private String buildOrderByClause(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return "";
        }
        String orderByClause = orders.stream().map(order -> order.getOrderName() + " " + order.getDirection()).collect(Collectors.joining(", "));
        return " ORDER BY " + orderByClause;
    }

    /**
     * 将Order数组转换为过滤后的Order列表
     * <p>
     * 过滤规则：排除null对象和orderName为空的Order对象
     * </p>
     *
     * @param orders Order数组
     * @return 过滤后的Order列表
     */
    private List<Order> convertToOrderList(Order... orders) {
        if (null == orders) {
            return Collections.emptyList();
        }
        return Arrays.asList(orders).stream().filter(order -> null != order && StringUtils.isNotBlank(order.getOrderName())).collect(Collectors.toList());
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
        if (null == primaryKeyField) {
            return null;
        }
        return this.findOne(clazz, params -> {
            StringBuilder sql = new StringBuilder("SELECT * FROM `");
            sql.append(tableName).append("` WHERE `").append(primaryKeyField.getColumnName()).append("`= :").append(primaryKeyField.getColumnName());
            params.addValue(primaryKeyField.getColumnName(), primaryKey, primaryKeyField.sqlType());
            return sql.toString();
        });
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
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t).stream().filter(s -> {
            if (null == s.getValue()) {
                return false;
            }
            if (s.getValue() instanceof String) {
                return !StringUtils.isBlank((String) s.getValue());
            }
            return true;
        }).collect(Collectors.toList());
        return this.findOne(Long.class, params -> {
            StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM `");
            sql.append(tableName).append("` WHERE 1=1 ");

            for (FieldValue fieldValue : fieldValues) {

                if (likeMode && fieldValue.getValue() instanceof String) {
                    sql.append(" AND `").append(fieldValue.getColumnName()).append("`like CONCAT('%', :").append(fieldValue.getColumnName()).append(", '%')");
                } else {
                    sql.append(" AND `").append(fieldValue.getColumnName()).append("`= :").append(fieldValue.getColumnName());
                }
                params.addValue(fieldValue.getColumnName(), fieldValue.getValue(), fieldValue.sqlType());
            }
            return sql.toString();
        });
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
        List<Order> orderBys = null == orders ? Collections.emptyList() : Arrays.asList(orders).stream().filter(s -> null != s && StringUtils.isNotBlank(s.orderName)).collect(Collectors.toList());
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t).stream().filter(s -> {
            if (null == s.getValue()) {
                return false;
            }
            if (s.getValue() instanceof String) {
                return !StringUtils.isBlank((String) s.getValue());
            }
            return true;
        }).collect(Collectors.toList());
        return (T) this.findOne(t.getClass(), params -> {
            StringBuilder sql = new StringBuilder("SELECT * FROM `");
            sql.append(tableName).append("` WHERE 1=1 ");

            for (FieldValue fieldValue : fieldValues) {

                if (likeMode && fieldValue.getValue() instanceof String) {
                    sql.append(" AND `").append(fieldValue.getColumnName()).append("`like CONCAT('%', :").append(fieldValue.getColumnName()).append(", '%')");
                } else {
                    sql.append(" AND `").append(fieldValue.getColumnName()).append("`= :").append(fieldValue.getColumnName());
                }
                params.addValue(fieldValue.getColumnName(), fieldValue.getValue(), fieldValue.sqlType());
            }
            if (!orderBys.isEmpty()) {
                sql.append(" ORDER BY ").append(orderBys.stream().map(s -> s.orderName + " " + s.direction).collect(Collectors.joining(", ")));
            }

            return sql.toString();
        });
    }


    /**
     * 查询所有符合条件的记录列表
     * <p>
     * 执行流程：
     * 1. 验证查询对象是否为空，为空则返回空列表
     * 2. 从查询对象中提取表名和所有字段值
     * 3. 过滤出非空字段值作为WHERE条件
     * 4. 处理排序参数，将字段名转换为数据库列名
     * 5. 构建不带分页限制的查询SQL以获取所有匹配记录
     * 6. 支持模糊查询模式（likeMode）和自定义排序
     * </p>
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
        List<Order> orderBys = convertToOrderList(orders);
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = filterValidFieldValues(FieldExtractor.extractFieldValue(t));

        return (List<T>) this.find(t.getClass(), params -> {
            StringBuilder sql = new StringBuilder("SELECT * FROM `");
            sql.append(tableName).append("` WHERE 1=1");
            sql.append(buildWhereClause(fieldValues, likeMode, params));
            sql.append(buildOrderByClause(orderBys));
            return sql.toString();
        });
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
     * <p>
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
     * </p>
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
        List<Order> orderBys = convertToOrderList(orders);
        String tableName = FieldExtractor.extractTableName(t.getClass());
        List<FieldValue> fieldValues = filterValidFieldValues(FieldExtractor.extractFieldValue(t));

        return (Page<T>) this.findPage(t.getClass(), slice, params -> {
            StringBuilder sql = new StringBuilder("SELECT * FROM `");
            sql.append(tableName).append("` WHERE 1=1");
            sql.append(buildWhereClause(fieldValues, likeMode, params));
            sql.append(buildOrderByClause(orderBys));
            return sql.toString();
        });
    }

    /**
     * 根据主键更新记录（全量更新）
     * <p>
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则返回0表示未更新任何记录
     * 2. 从实体对象中提取表名和所有字段值
     * 3. 查找主键字段，如果不存在或为null则返回
     * 4. 构建UPDATE SQL语句，更新所有非主键字段
     * 5. 执行SQL并返回受影响的行数
     * </p>
     *
     * @param t   实体对象，包含要更新的字段值和主键值；如果为null则返回0
     * @param <T> 实体类型泛型参数
     * @return 受影响的行数，如果实体对象为空则返回0
     */
    public <T> Result updateByPrimaryKey(T t) {
        if (t == null) {
            return new Result(null, 0, null);
        }
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        FieldValue primaryKeyValue = fieldValues.stream().filter(Objects::nonNull).filter(field -> field.isPrimary() && field.isNotNullVal()).findFirst().orElse(null);
        if (null == primaryKeyValue || primaryKeyValue.isNullVal()) {
            return new Result(null, 0, null);
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());

        return this.update(params -> {
            StringBuilder sql = new StringBuilder("UPDATE `");
            sql.append(tableName).append("` SET ");

            String setClause = fieldValues.stream().map(field -> {
                params.addValue(field.getColumnName(), field.getValue(), field.sqlType());
                return "`" + field.getColumnName() + "` = :" + field.getColumnName();
            }).collect(Collectors.joining(","));

            sql.append(setClause).append(" WHERE `").append(primaryKeyValue.getColumnName()).append("` = :").append(primaryKeyValue.getColumnName());

            return sql.toString();
        });
    }


    /**
     * 根据主键选择性更新记录（只更新非空字段）
     * <p>
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则返回0表示未更新任何记录
     * 2. 从实体对象中提取表名和所有字段值
     * 3. 查找主键字段，如果不存在或为null则返回
     * 4. 过滤出非空字段，仅更新这些字段
     * 5. 构建UPDATE SQL语句，只更新非空字段
     * 6. 执行SQL并返回受影响的行数
     * </p>
     *
     * @param t   实体对象，包含要更新的字段值和主键值；只有非null的字段会被更新；如果为null则返回0
     * @param <T> 实体类型泛型参数
     * @return 受影响的行数，如果实体对象为空则返回0
     */
    public <T> Result updateByPrimaryKeySelective(T t) {
        if (t == null) {
            return new Result(null, 0, null);
        }
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        FieldValue primaryKeyValue = fieldValues.stream().filter(Objects::nonNull).filter(field -> field.isPrimary() && field.isNotNullVal()).findFirst().orElse(null);
        if (null == primaryKeyValue || primaryKeyValue.isNullVal()) {
            return new Result(null, 0, null);
        }
        String tableName = FieldExtractor.extractTableName(t.getClass());

        return this.update(params -> {
            StringBuilder sql = new StringBuilder("UPDATE `");
            sql.append(tableName).append("` SET ");

            String setClause = fieldValues.stream().filter(field -> !field.isNullVal()).map(field -> {
                params.addValue(field.getColumnName(), field.getValue(), field.sqlType());
                return "`" + field.getColumnName() + "` = :" + field.getColumnName();
            }).collect(Collectors.joining(","));

            sql.append(setClause).append(" WHERE `").append(primaryKeyValue.getColumnName()).append("` = :").append(primaryKeyValue.getColumnName());

            return sql.toString();
        });
    }


    /**
     * 根据主键删除记录（支持单个或多个主键）
     * <p>
     * 执行流程：
     * 1. 验证主键参数数组是否为空，为空则返回0表示未删除任何记录
     * 2. 从实体类中提取表名和主键字段信息
     * 3. 为每个主键值生成独立的WHERE条件（使用OR连接）
     * 4. 生成DELETE SQL语句
     * 5. 执行SQL并返回受影响的行数
     * </p>
     *
     * @param clazz       实体类型，用于提取表名和主键字段信息
     * @param primaryKeys 主键值数组，支持单个或多个主键；null值和空字符串会被过滤；如果为空或无有效值则返回0
     * @param <T>         实体类型泛型参数
     * @return 受影响的行数，如果主键参数为空或无有效值则返回0
     */
    public <T> Result deleteByPrimaryKey(Class<T> clazz, Object... primaryKeys) {
        if (primaryKeys == null || primaryKeys.length == 0) {
            return new Result(null, 0, null);
        }
        FieldValue primaryKeyValue = FieldExtractor.extractPrimaryField(clazz);
        if (null == primaryKeyValue) {
            return new Result(null, 0, null);
        }
        String tableName = FieldExtractor.extractTableName(clazz);

        return this.update(params -> {
            StringBuilder sql = new StringBuilder("DELETE FROM `");
            sql.append(tableName).append("` WHERE `");

            for (int i = 0; i < primaryKeys.length; i++) {
                String variableName = primaryKeyValue.getColumnName() + i;
                params.addValue(variableName, primaryKeys[i], primaryKeyValue.sqlType());
                sql.append("`").append(primaryKeyValue.getColumnName()).append("` = :").append(variableName);
                if (i < primaryKeys.length - 1) {
                    sql.append(" OR ");
                }
            }
            return sql.toString();
        });
    }


    /**
     * 插入新记录并返回生成的主键
     * <p>
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则记录警告日志并返回null
     * 2. 从实体对象中提取表名和所有字段值
     * 3. 检查主键字段：如果主键存在但值为null，则从插入字段列表中移除主键（通常用于自增主键）
     * 4. 生成INSERT SQL语句
     * 5. 执行插入操作并返回包含生成主键的KeyHolder对象
     * </p>
     *
     * @param t   实体对象，包含要插入的字段值；如果为null则返回null
     * @param <T> 实体类型泛型参数
     * @return 包含生成主键的KeyHolder对象，如果实体对象为空则返回null
     */
    public <T> Result insert(T t) {
        if (t == null) {
            return new Result(null, 0, null);
        }
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(t);
        String tableName = FieldExtractor.extractTableName(t.getClass());

        return this.update(params -> {
            StringBuilder sql = new StringBuilder("INSERT INTO `");
            sql.append(tableName).append("` (");
            StringBuilder placeholder = new StringBuilder();
            StringBuilder valueClause = new StringBuilder();

            for (FieldValue field : fieldValues) {
                placeholder.append("`").append(field.getColumnName()).append("`").append(",");
                valueClause.append(":").append(field.getColumnName()).append(",");
                params.addValue(field.getColumnName(), field.getValue(), field.sqlType());
            }

            // 移除最后一个逗号
            placeholder = placeholder.deleteCharAt(placeholder.length() - 1);
            valueClause = valueClause.deleteCharAt(valueClause.length() - 1);
            sql.append(placeholder).append(" ) VALUES ( ").append(valueClause).append(" )");

            return sql.toString();
        });
    }


    /**
     * 保存或更新记录（根据主键是否存在决定执行插入还是更新）
     * 执行流程：
     * 1. 验证实体对象是否为空，为空则记录警告日志并返回null
     * 2. 从实体对象中提取所有字段值
     * 3. 查找具有非空值的主键字段
     * 4. 如果主键不存在或主键值为null，则执行插入操作
     * 5. 如果主键存在且有值，则检查数据库中是否已存在该主键对应的记录：
     * - 如果记录不存在，执行插入操作
     * - 如果记录存在，执行全量更新操作
     * 6. 构造并返回包含主键信息的KeyHolder对象
     *
     * @param t   实体对象，包含要保存或更新的字段值；如果为null则返回null
     * @param <T> 实体类型泛型参数
     * @return 包含主键信息的KeyHolder对象，如果实体对象为空则返回null
     */
    public <T> Result saveOrUpdate(T t) {
        if (t == null) {
            return new Result(null, 0, null);
        }
        Result result = this.updateByPrimaryKey(t);
        if (result.affectedRows > 0) {
            return result;
        }
        result = this.insert(t);
        return result;
    }

    /**
     * 批量插入记录
     * <p>
     * 执行流程：
     * 1. 验证集合是否为空或null，为空则直接返回
     * 2. 初始化SQL构建器和参数列表
     * 3. 遍历所有非空的实体对象：
     * - 提取每个对象的字段值并转换为MapSqlParameterSource
     * - 使用第一个有效对象提取表名和字段信息构建INSERT SQL语句模板
     * - 将所有参数添加到参数列表中
     * 4. 如果参数列表为空，返回空结果
     * 5. 执行批量更新操作并返回Result对象
     * </p>
     *
     * @param list 实体对象集合，包含要批量插入的数据；如果为null或空集合则直接返回
     * @param <T>  实体类型泛型参数
     * @return Result对象，包含KeyHolder、受影响的行数和批量更新的行数数组；如果集合为空则返回空结果
     */
    public <T> Result saveAll(List<T> list) {
        if (null == list || list.isEmpty()) {
            return new Result(null, 0, null);
        }
        StringBuilder sql = new StringBuilder("INSERT INTO `");
        List<SqlParameterSource> paramsList = new ArrayList<>();
        AtomicReference<Class> reference = new AtomicReference<>();

        // 遍历实体列表，构建SQL模板和参数列表
        list.stream().filter(Objects::nonNull).forEach(s -> {
            MapSqlParameterSource params = new MapSqlParameterSource();
            List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(s);
            if (reference.getAndSet(s.getClass()) != null) {
                String tableName = FieldExtractor.extractTableName(s.getClass());
                sql.append(tableName).append("` (");
                StringBuilder placeholder = new StringBuilder();
                StringBuilder valueClause = new StringBuilder();

                for (FieldValue field : fieldValues) {
                    placeholder.append("`").append(field.getColumnName()).append("`").append(",");
                    valueClause.append(":").append(field.getColumnName()).append(",");
                    params.addValue(field.getColumnName(), field.getValue(), field.sqlType());
                }

                // 移除最后一个逗号
                placeholder = placeholder.deleteCharAt(placeholder.length() - 1);
                valueClause = valueClause.deleteCharAt(valueClause.length() - 1);
                sql.append(placeholder).append(" ) VALUES ( ").append(valueClause).append(" )");
            }
            paramsList.add(params);

        });
        if (paramsList.isEmpty()) {
            return new Result(null, 0, null);
        }
        return this.batchUpdate(reference.get(), sql.toString(), paramsList.toArray(new SqlParameterSource[paramsList.size()]));
    }


    /**
     * 查询单条记录（使用命名参数SQL）
     * <p>
     * 执行流程：
     * 1. 清理SQL语句（去除换行符、分号等）
     * 2. 如果参数为空，使用EmptySqlParameterSource
     * 3. 根据目标类型选择查询方式：
     * - 基本类型：直接使用queryForObject
     * - 复杂对象：使用SimpleRowMapper进行行映射，支持时区转换
     * </p>
     *
     * @param clazz  目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql    要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param params SQL参数源对象，提供命名参数的值；可以为null表示无参数查询
     * @param <T>    结果类型泛型参数
     * @return 查询到的单个实体对象，如果没有匹配记录则返回null
     */
    public <T> T findOne(Class<T> clazz, String sql, SqlParameterSource params) {
        sql = this.selectOneSql(sql);
        if (null == params) {
            params = EmptySqlParameterSource.INSTANCE;
        }
        List<T> list = this.find(clazz, sql, params);
        return null == list || list.isEmpty() ? null : list.get(0);
    }

    /**
     * 查询单条记录（使用函数式接口构建SQL和参数）
     * <p>
     * 执行流程：
     * 1. 创建空的MapSqlParameterSource对象用于收集SQL参数
     * 2. 调用传入的函数，该函数接收MapSqlParameterSource并返回SQL语句
     * 3. 函数可以在MapSqlParameterSource中添加命名参数，同时构建对应的SQL语句
     * 4. 委托给重载的findOne方法执行具体的查询逻辑
     * </p>
     *
     * @param clazz    目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param function 函数式接口，接收MapSqlParameterSource参数用于添加SQL命名参数，返回要执行的SQL语句
     * @param <T>      结果类型泛型参数
     * @return 查询到的单个实体对象，如果没有匹配记录则返回null
     */
    public <T> T findOne(Class<T> clazz, Function<MapSqlParameterSource, String> function) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = function.apply(params);
        return this.findOne(clazz, sql, params);
    }


    /**
     * 查询所有符合条件的记录列表（使用命名参数SQL）
     * <p>
     * 执行流程：
     * 1. 清理SQL语句（去除换行符、分号等）
     * 2. 如果参数为空，使用EmptySqlParameterSource
     * 3. 根据目标类型选择查询方式：
     * - 基本类型：使用queryForList直接获取结果
     * - 复杂对象：使用SimpleRowMapper进行行映射，支持时区转换
     * </p>
     *
     * @param clazz  目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql    要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param params SQL参数源对象，提供命名参数的值；可以为null表示无参数查询
     * @param <T>    结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录则返回空列表
     */
    public <T> List<T> find(Class<T> clazz, String sql, SqlParameterSource params) {
        sql = this.sql(sql);
        if (null == params) {
            params = EmptySqlParameterSource.INSTANCE;
        }
        List<T> list = FieldExtractor.isBasicResult(clazz) ? this.namedParameterJdbcTemplate.queryForList(sql, params, clazz) : this.namedParameterJdbcTemplate.query(sql, params, new SimpleRowMapper<>(clazz, this.databaseZoneId));
        return null == list ? Collections.emptyList() : list;
    }

    /**
     * 执行命名参数SQL查询并返回结果列表（使用Map作为参数）
     * <p>
     * 执行流程：
     * 1. 清理和标准化SQL语句（去除换行符、分号等）
     * 2. 初始化参数源，默认为EmptySqlParameterSource
     * 3. 如果传入的Map不为空，则将其转换为MapSqlParameterSource
     * 4. 根据目标类型选择查询方式：
     * - 基本类型：使用queryForList直接获取结果
     * - 复杂对象：使用SimpleRowMapper进行行映射，支持时区转换
     * </p>
     *
     * @param clazz 目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param sql   要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param map   参数Map，key对应SQL中的命名参数名称，value为参数值；如果为null或空则执行无参查询
     * @param <T>   结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录则返回空列表
     */
    public <T> List<T> find(Class<T> clazz, String sql, Map<String, Object> map) {
        SqlParameterSource params = EmptySqlParameterSource.INSTANCE;
        if (null != map && !map.isEmpty()) {
            params = new MapSqlParameterSource(map);
        }
        return this.find(clazz, sql, params);
    }

    /**
     * 查询所有符合条件的记录列表（使用函数式接口构建SQL和参数）
     * <p>
     * 执行流程：
     * 1. 创建空的MapSqlParameterSource对象用于收集SQL参数
     * 2. 调用传入的函数，该函数接收MapSqlParameterSource并返回SQL语句
     * 3. 函数可以在MapSqlParameterSource中添加命名参数，同时构建对应的SQL语句
     * 4. 委托给重载的find方法执行具体的查询逻辑
     * </p>
     *
     * @param clazz    目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param function 函数式接口，接收MapSqlParameterSource参数用于添加SQL命名参数，返回要执行的SQL语句
     * @param <T>      结果类型泛型参数
     * @return 查询结果列表，如果没有匹配记录则返回空列表
     */
    public <T> List<T> find(Class<T> clazz, Function<MapSqlParameterSource, String> function) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = function.apply(params);
        return this.find(clazz, sql, params);
    }

    /**
     * 执行分页查询并返回分页结果（使用命名参数SQL）
     * <p>
     * 执行流程：
     * 1. 初始化分页参数，如果slice为空则使用默认值
     * 2. 构建带分页限制的查询SQL（使用LIMIT/OFFSET）
     * 3. 构建COUNT查询SQL以获取总记录数
     * 4. 分别执行数据查询和总数查询
     * 5. 组装分页结果对象并返回
     * </p>
     *
     * @param clazz  目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param slice  分页参数对象，包含页码和每页大小；如果为null则使用默认值
     * @param sql    要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param params SQL参数源对象，提供命名参数的值；可以为null表示无参数查询
     * @param <T>    结果类型泛型参数
     * @return 分页结果对象，包含数据列表、总记录数和分页参数
     */
    public <T> Page<T> findPage(Class<T> clazz, Slice slice, String sql, SqlParameterSource params) {
        slice = null == slice ? Slice.DEFAULT_SLICE : slice;
        String pageSql = this.pageSql(sql, slice);
        String countSql = this.countSql(sql);
        if (null == params) {
            params = EmptySqlParameterSource.INSTANCE;
        }
        List<T> data = this.find(clazz, pageSql, params);
        Long total = this.findOne(Long.class, countSql, params);

        return Page.of(slice, total, data);
    }

    /**
     * 执行分页查询并返回分页结果（使用Map作为参数）
     * <p>
     * 执行流程：
     * 1. 初始化参数源，默认为EmptySqlParameterSource
     * 2. 如果传入的Map不为空，则将其转换为MapSqlParameterSource
     * 3. 委托给重载的findPage方法执行具体的分页查询逻辑
     * </p>
     *
     * @param clazz 目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param slice 分页参数对象，包含页码和每页大小；如果为null则使用默认值
     * @param sql   要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param map   参数Map，key对应SQL中的命名参数名称，value为参数值；如果为null或空则执行无参查询
     * @param <T>   结果类型泛型参数
     * @return 分页结果对象，包含数据列表、总记录数和分页参数
     */
    /**
     * 执行分页查询并返回分页结果（使用Map作为参数）
     * <p>
     * 执行流程：
     * 1. 初始化参数源，默认为EmptySqlParameterSource
     * 2. 如果传入的Map不为空，则将其转换为MapSqlParameterSource
     * 3. 委托给重载的findPage方法执行具体的分页查询逻辑
     * </p>
     *
     * @param clazz 目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param slice 分页参数对象，包含页码和每页大小信息
     * @param sql   要执行的SQL查询语句，支持命名参数（如:name, :age等）
     * @param map   参数Map，key对应SQL中的命名参数名称，value为参数值；如果为null或空则执行无参查询
     * @param <T>   结果类型泛型参数
     * @return 分页结果对象，包含数据列表、总记录数和分页参数
     */
    public <T> Page<T> findPage(Class<T> clazz, Slice slice, String sql, Map<String, Object> map) {
        SqlParameterSource params = EmptySqlParameterSource.INSTANCE;
        if (null != map && !map.isEmpty()) {
            params = new MapSqlParameterSource(map);
        }
        return this.findPage(clazz, slice, sql, params);
    }

    /**
     * 执行分页查询并返回分页结果（使用函数式接口构建SQL和参数）
     * <p>
     * 执行流程：
     * 1. 创建空的MapSqlParameterSource对象用于收集SQL参数
     * 2. 调用传入的函数，该函数接收MapSqlParameterSource并返回SQL语句
     * 3. 委托给重载的findPage方法执行具体的分页查询逻辑
     * </p>
     *
     * @param clazz    目标结果类型，用于确定使用哪种查询方式；可以是基本类型或实体类
     * @param slice    分页参数对象，包含页码和每页大小
     * @param function 函数式接口，接收MapSqlParameterSource参数用于添加SQL命名参数，返回要执行的SQL语句
     * @param <T>      结果类型泛型参数
     * @return 分页结果对象，包含数据列表、总记录数和分页参数
     */
    public <T> Page<T> findPage(Class<T> clazz, Slice slice, Function<MapSqlParameterSource, String> function) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = function.apply(params);
        return this.findPage(clazz, slice, sql, params);
    }


    /**
     * 执行更新操作（使用命名参数SQL）
     * <p>
     * 执行流程：
     * 1. 清理SQL语句（去除换行符、分号等）
     * 2. 创建GeneratedKeyHolder用于接收生成的主键
     * 3. 如果参数为空，使用EmptySqlParameterSource
     * 4. 执行更新操作并返回包含主键和受影响行数的Result对象
     * </p>
     *
     * @param sql    要执行的SQL语句，支持命名参数（如:name, :age等）
     * @param params SQL参数源对象，提供命名参数的值；可以为null表示无参数查询
     * @return Result对象，包含KeyHolder、受影响的行数和批量更新的行数数组
     */
    public <T> Result update(String sql, SqlParameterSource params) {
        sql = this.sql(sql);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (null == params) {
            params = EmptySqlParameterSource.INSTANCE;
        }
        int update = this.namedParameterJdbcTemplate.update(sql, params, keyHolder);
        return new Result(keyHolder, update, new int[]{update});
    }

    /**
     * 执行更新操作（使用函数式接口构建SQL和参数）
     * <p>
     * 执行流程：
     * 1. 创建空的MapSqlParameterSource对象用于收集SQL参数
     * 2. 调用传入的函数，该函数接收MapSqlParameterSource并返回SQL语句
     * 3. 委托给重载的update方法执行具体的更新逻辑
     * </p>
     *
     * @param function 函数式接口，接收MapSqlParameterSource参数用于添加SQL命名参数，返回要执行的SQL语句
     * @return Result对象，包含KeyHolder、受影响的行数和批量更新的行数数组
     */
    public <T> Result update(Function<MapSqlParameterSource, String> function) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        String sql = function.apply(params);
        return this.update(sql, params);
    }

    /**
     * 执行批量更新操作
     * <p>
     * 执行流程：
     * 1. 清理SQL语句（去除换行符、分号等）
     * 2. 创建GeneratedKeyHolder用于接收生成的主键
     * 3. 如果参数数组为空，使用空数组
     * 4. 执行批量更新操作并返回包含主键和受影响行数的Result对象
     * </p>
     *
     * @param clazz  目标结果类型（此参数在当前实现中未使用）
     * @param sql    要执行的SQL语句，支持命名参数（如:name, :age等）
     * @param params SQL参数源对象数组，每个元素对应一次批量操作的参数
     * @return Result对象，包含KeyHolder、受影响的行数和批量更新的行数数组
     */
    public <T> Result batchUpdate(Class<T> clazz, String sql, SqlParameterSource[] params) {
        sql = this.sql(sql);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (null == params) {
            params = new SqlParameterSource[0];
        }
        int[] update = this.namedParameterJdbcTemplate.batchUpdate(sql, params, keyHolder);
        return new Result(keyHolder, update.length, update);
    }


    /**
     * 清理和标准化SQL语句
     * <p>
     * 执行流程：
     * 1. 验证SQL语句是否为空，为空则抛出异常
     * 2. 去除换行符（\r、\n）并替换为空格
     * 3. 去除末尾的分号
     * 4. 去除首尾空格
     * </p>
     *
     * @param sql 原始SQL语句
     * @return 清理后的SQL语句
     * @throws UncheckedException 当SQL语句为空时抛出异常
     */
    private String sql(String sql) {
        if (StringUtils.isBlank(sql)) {
            throw new UncheckedException(JdbcError.SQL_IS_NULL, "SQL语句不能为空");
        }
        sql = sql.replaceAll("\r", "  ").replaceAll("\n", "  ").replaceAll(";", " ").trim();
        return sql;
    }

    /**
     * 构建查询单条记录的SQL语句
     * <p>
     * 将原始SQL包装为子查询，并添加LIMIT 1限制，确保只返回一条记录
     * </p>
     *
     * @param sql 原始SQL语句
     * @return 添加了LIMIT 1限制的SQL语句
     */
    private String selectOneSql(String sql) {
        sql = this.sql(sql);
        sql = "SELECT  * from (" + sql + ") tmp LIMIT 1 ";
        return sql;
    }

    /**
     * 构建COUNT查询的SQL语句
     * <p>
     * 将原始SQL包装为子查询，并使用COUNT(*)统计总记录数
     * </p>
     *
     * @param sql 原始SQL语句
     * @return COUNT查询的SQL语句
     */
    private String countSql(String sql) {
        sql = this.sql(sql);
        sql = "SELECT COUNT(*) as count from (" + sql + ") tmp ";
        return sql;
    }

    /**
     * 构建分页查询的SQL语句
     * <p>
     * 将原始SQL包装为子查询，并添加LIMIT offset, size实现分页
     * </p>
     *
     * @param sql   原始SQL语句
     * @param slice 分页参数对象，包含起始偏移量和每页大小
     * @return 添加了分页限制的SQL语句
     */
    private String pageSql(String sql, Slice slice) {
        sql = this.sql(sql);
        sql = "SELECT  * from (" + sql + ") tmp LIMIT  " + slice.startOffset() + ", " + slice.size();
        return sql;
    }


    /**
     * 数据库操作结果封装类
     * <p>
     * 包含执行更新、插入或删除操作后的结果信息：
     * - KeyHolder：用于接收生成的主键值（适用于自增主键）
     * - affectedRows：受影响的行数
     * - batchAffectedRows：批量操作中每次操作受影响的行数数组
     * </p>
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    @Data
    @AllArgsConstructor
    public static class Result {
        private final static GeneratedKeyHolder defaultKeyHolder = new GeneratedKeyHolder();
        /**
         * KeyHolder对象，用于存储生成的主键值
         * <p>
         * 当执行INSERT操作且数据库使用自增主键时，可以通过此对象获取生成的主键值
         * </p>
         */
        private KeyHolder keyHolder;

        /**
         * 受影响的行数
         * <p>
         * 对于UPDATE、DELETE操作，表示实际修改或删除的记录数
         * 对于INSERT操作，通常为1（成功插入一条记录）
         * </p>
         */
        private int affectedRows;

        /**
         * 批量操作中每次操作受影响的行数数组
         * <p>
         * 仅在执行batchUpdate操作时有值，数组长度等于批量操作的次数
         * 可能包含JDBC定义的特殊值：
         * - Statement.SUCCESS_NO_INFO：执行成功但未知影响行数
         * - Statement.EXECUTE_FAILED：执行失败
         * </p>
         */
        private int[] batchAffectedRows;

        /**
         * KeyHolder对象，用于存储生成的主键值
         *
         * @return 当执行INSERT操作且数据库使用自增主键时，可以通过此对象获取生成的主键值
         */
        public KeyHolder keyHolder() {
            if (null != keyHolder) {
                return this.keyHolder;
            }
            return defaultKeyHolder;
        }
    }

    /**
     * 获取JdbcTemplate实例
     *
     * @return JdbcTemplate对象
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * 设置JdbcTemplate实例并重新初始化
     * <p>
     * 调用此方法会触发initialize()方法，重新创建NamedParameterJdbcTemplate并检测数据库时区
     * </p>
     *
     * @param jdbcTemplate JdbcTemplate对象
     */
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.initialize();
    }

    /**
     * 获取NamedParameterJdbcTemplate实例
     *
     * @return NamedParameterJdbcTemplate对象
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
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
        private Direction direction;

        /**
         * 设置为降序
         *
         * @return 当前 Order 对象
         */
        public Order desc() {
            this.direction = Direction.DESC;
            return this;
        }

        /**
         * 获取排序方向字符串
         *
         * @return 排序方向
         */
        public String direction() {
            return (null == this.direction || this.direction == Direction.ASC) ? "ASC" : "DESC";
        }

        /**
         * 设置为升序
         *
         * @return 当前 Order 对象
         */
        public Order asc() {
            this.direction = Direction.ASC;
            return this;
        }

        /**
         * 创建升序 Order 对象
         *
         * @param name 排序字段名
         * @return Order 对象
         */
        public static Order asc(String name) {
            return Order.of(name, Direction.ASC);
        }

        /**
         * 创建降序 Order 对象
         *
         * @param name 排序字段名
         * @return Order 对象
         */
        public static Order desc(String name) {
            return Order.of(name, Direction.DESC);
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
        public static Order of(String orderName, Direction direction) {
            return new Order(orderName, direction);
        }

        /**
         * 创建 Order 对象
         *
         * @param direction 排序方向
         * @return Order 对象
         */
        public static Order of(Direction direction) {
            return new Order(null, direction);
        }

        /**
         * 创建默认升序的 Order 对象
         *
         * @param orderName 排序字段名
         * @return Order 对象
         */
        public static Order of(String orderName) {
            return new Order(orderName, Direction.ASC);
        }


    }

    /**
     * 排序方向枚举
     */
    public enum Direction {
        ASC, DESC
    }

}
