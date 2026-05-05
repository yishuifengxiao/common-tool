package com.yishuifengxiao.common.tool.jdbc;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.HashMap;
import java.util.Map;

/**
 * 字段值对象，封装字段信息、值和相关属性
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldValue implements Serializable {
    private static final long serialVersionUID = 338863052159133444L;
    
    /** Java类型到SQL类型的映射表，用于根据Java字段类型确定对应的JDBC类型 */
    private static final Map<Class<?>, SQLType> JAVA_TO_SQL_TYPE_MAP = new HashMap<>();
    
    /** SQL数据类型名称到JDBC类型的映射表，用于从columnDefinition解析SQL类型 */
    private static final Map<String, SQLType> TYPE_MAP = new HashMap<>();

    static {
        // 初始化Java基本类型及其包装类到SQL类型的映射
        JAVA_TO_SQL_TYPE_MAP.put(int.class, JDBCType.INTEGER);
        JAVA_TO_SQL_TYPE_MAP.put(Integer.class, JDBCType.INTEGER);
        JAVA_TO_SQL_TYPE_MAP.put(long.class, JDBCType.BIGINT);
        JAVA_TO_SQL_TYPE_MAP.put(Long.class, JDBCType.BIGINT);
        JAVA_TO_SQL_TYPE_MAP.put(short.class, JDBCType.SMALLINT);
        JAVA_TO_SQL_TYPE_MAP.put(Short.class, JDBCType.SMALLINT);
        JAVA_TO_SQL_TYPE_MAP.put(byte.class, JDBCType.TINYINT);
        JAVA_TO_SQL_TYPE_MAP.put(Byte.class, JDBCType.TINYINT);
        JAVA_TO_SQL_TYPE_MAP.put(float.class, JDBCType.REAL);
        JAVA_TO_SQL_TYPE_MAP.put(Float.class, JDBCType.REAL);
        JAVA_TO_SQL_TYPE_MAP.put(double.class, JDBCType.DOUBLE);
        JAVA_TO_SQL_TYPE_MAP.put(Double.class, JDBCType.DOUBLE);
        JAVA_TO_SQL_TYPE_MAP.put(boolean.class, JDBCType.BOOLEAN);
        JAVA_TO_SQL_TYPE_MAP.put(Boolean.class, JDBCType.BOOLEAN);
        JAVA_TO_SQL_TYPE_MAP.put(char.class, JDBCType.CHAR);
        JAVA_TO_SQL_TYPE_MAP.put(Character.class, JDBCType.CHAR);
        JAVA_TO_SQL_TYPE_MAP.put(String.class, JDBCType.VARCHAR);
        JAVA_TO_SQL_TYPE_MAP.put(java.util.Date.class, JDBCType.TIMESTAMP);
        JAVA_TO_SQL_TYPE_MAP.put(java.sql.Date.class, JDBCType.DATE);
        JAVA_TO_SQL_TYPE_MAP.put(java.sql.Time.class, JDBCType.TIME);
        JAVA_TO_SQL_TYPE_MAP.put(java.sql.Timestamp.class, JDBCType.TIMESTAMP);
        JAVA_TO_SQL_TYPE_MAP.put(java.time.LocalDate.class, JDBCType.DATE);
        JAVA_TO_SQL_TYPE_MAP.put(java.time.LocalTime.class, JDBCType.TIME);
        JAVA_TO_SQL_TYPE_MAP.put(java.time.LocalDateTime.class, JDBCType.TIMESTAMP);
        JAVA_TO_SQL_TYPE_MAP.put(BigDecimal.class, JDBCType.DECIMAL);
        JAVA_TO_SQL_TYPE_MAP.put(BigInteger.class, JDBCType.BIGINT);
        JAVA_TO_SQL_TYPE_MAP.put(byte[].class, JDBCType.VARBINARY);
        JAVA_TO_SQL_TYPE_MAP.put(Byte[].class, JDBCType.VARBINARY);

        // 初始化SQL数据类型名称到JDBC类型的映射，支持常见的数据库列类型定义

        TYPE_MAP.put("INT", JDBCType.INTEGER);
        TYPE_MAP.put("INTEGER", JDBCType.INTEGER);
        TYPE_MAP.put("BIGINT", JDBCType.BIGINT);
        TYPE_MAP.put("SMALLINT", JDBCType.SMALLINT);
        TYPE_MAP.put("TINYINT", JDBCType.TINYINT);
        TYPE_MAP.put("DECIMAL", JDBCType.DECIMAL);
        TYPE_MAP.put("NUMERIC", JDBCType.DECIMAL);
        TYPE_MAP.put("FLOAT", JDBCType.FLOAT);
        TYPE_MAP.put("DOUBLE", JDBCType.DOUBLE);
        TYPE_MAP.put("REAL", JDBCType.REAL);
        TYPE_MAP.put("BOOLEAN", JDBCType.BOOLEAN);
        TYPE_MAP.put("BOOL", JDBCType.BOOLEAN);
        TYPE_MAP.put("CHAR", JDBCType.CHAR);
        TYPE_MAP.put("VAR", JDBCType.VARCHAR);
        TYPE_MAP.put("VARCHAR", JDBCType.VARCHAR);
        TYPE_MAP.put("TEXT", JDBCType.CLOB);
        TYPE_MAP.put("CLOB", JDBCType.CLOB);
        TYPE_MAP.put("BLOB", JDBCType.BLOB);
        TYPE_MAP.put("BINARY", JDBCType.BINARY);
        TYPE_MAP.put("VARBINARY", JDBCType.VARBINARY);
        TYPE_MAP.put("DATE", JDBCType.DATE);
        TYPE_MAP.put("TIME", JDBCType.TIME);
        TYPE_MAP.put("TIMESTAMP", JDBCType.TIMESTAMP);
        TYPE_MAP.put("DATETIME", JDBCType.TIMESTAMP);
    }

    private SQLType sqlType;
    
    /** Java反射字段对象，用于获取字段的类型和注解信息 */
    private Field field;
    
    /** JPA Column注解，包含列名、列定义等数据库映射信息 */
    private Column column;
    
    /** 标识该字段是否为主键 */
    private boolean primary;
    
    /** 字段的实际值 */
    private Object value;
    
    /** 数据库列名，优先使用Column注解的name属性，否则使用字段名的下划线命名 */
    private String columnName;

    /**
     * 构造函数（不包含字段值）
     *
     * @param field   字段对象，用于提取字段类型、注解等信息
     * @param primary 是否为主键，标识该字段在数据库表中的主键状态
     */
    public FieldValue(Field field, boolean primary) {
        this(field, primary, null);
    }

    /**
     * 构造函数（包含字段值）
     * 执行流程：
     * 1. 初始化字段、主键标识和字段值
     * 2. 提取字段上的JPA Column注解
     * 3. 确定数据库列名：优先使用Column注解的name属性，未配置则转换字段名为下划线命名
     * 4. 确定SQL类型：基于columnDefinition或Java字段类型推断
     *
     * @param field   字段对象，用于提取字段类型、注解等元数据
     * @param primary 是否为主键，标识该字段在数据库表中的主键状态
     * @param value   字段值，可以为null
     */
    public FieldValue(Field field, boolean primary, Object value) {
        this.field = field;
        this.primary = primary;
        this.value = value;
        // 提取字段上的JPA Column注解，用于获取列名和列定义等信息
        this.column = org.springframework.core.annotation.AnnotationUtils.findAnnotation(this.field, Column.class);
        if (null != this.column && org.apache.commons.lang3.StringUtils.isNotBlank(this.column.name())) {
            // 优先使用Column注解中显式配置的列名
            this.columnName = this.column.name();
        } else if (null != this.field) {
            // 未配置列名时，将字段名转换为下划线命名风格作为列名
            this.columnName = com.yishuifengxiao.common.tool.text.TextUtil.underscoreName(this.field.getName());
        }
        this.sqlType = determineSqlType();
    }

    /**
     * 获取 SQL 类型
     *
     * @return SQL 类型
     */
    public SQLType sqlType() {
        return this.sqlType;
    }

    /**
     * 根据字段信息推断SQL数据类型
     * 推断优先级：
     * 1. Column注解的columnDefinition属性中显式指定的SQL类型
     * 2. 数组类型特殊处理：byte[]/Byte[]映射为VARBINARY，其他数组映射为VARCHAR
     * 3. 基于JAVA_TO_SQL_TYPE_MAP映射表查找Java类型对应的SQL类型
     * 4. 枚举类型统一映射为VARCHAR
     * 5. 默认返回VARCHAR
     *
     * @return 推断出的SQL类型，默认为JDBCType.VARCHAR
     */
    private SQLType determineSqlType() {
        if (this.column != null && org.apache.commons.lang3.StringUtils.isNotBlank(this.column.columnDefinition())) {
            // 优先从Column注解的columnDefinition中提取SQL类型定义
            SQLType typeFromDefinition = extractSqlTypeFromDefinition(this.column.columnDefinition());
            if (typeFromDefinition != null) {
                return typeFromDefinition;
            }
        }
        if (this.field != null) {
            // 基于Java字段类型进行SQL类型映射
            Class<?> fieldType = this.field.getType();
            if (fieldType.isArray()) {
                // 数组类型需要特殊处理：字节数组映射为二进制类型，其他数组映射为字符串类型
                Class<?> componentType = fieldType.getComponentType();
                if (componentType == byte.class || componentType == Byte.class) {
                    return JDBCType.VARBINARY;
                }
                return JDBCType.VARCHAR;
            }
            SQLType mappedType = JAVA_TO_SQL_TYPE_MAP.get(fieldType);
            if (mappedType != null) {
                return mappedType;
            }
            // 枚举类型统一映射为VARCHAR，存储枚举名称
            if (fieldType.isEnum()) {
                return JDBCType.VARCHAR;
            }
        }
        return JDBCType.VARCHAR;
    }

    /**
     * 从Column注解的columnDefinition属性中提取SQL类型
     * 处理流程：
     * 1. 验证columnDefinition是否为空
     * 2. 提取数据类型部分（去除括号内的长度精度等信息）
     * 3. 从TYPE_MAP映射表中查找对应的JDBC类型
     *
     * @param columnDefinition 列定义字符串，如"VARCHAR(255)"、"DECIMAL(10,2)"等
     * @return 提取的SQL类型，无法提取时返回null
     */
    private SQLType extractSqlTypeFromDefinition(String columnDefinition) {
        if (org.apache.commons.lang3.StringUtils.isBlank(columnDefinition)) {
            return null;
        }
        // 转换为大写并提取数据类型部分，忽略长度、精度等附加信息
        String definition = columnDefinition.trim().toUpperCase();
        String dataType = extractDataType(definition);
        if (org.apache.commons.lang3.StringUtils.isBlank(dataType)) {
            return null;
        }
        return TYPE_MAP.getOrDefault(dataType, null);
    }

    /**
     * 从SQL定义字符串中提取纯数据类型名称
     * 处理场景：
     * - "VARCHAR(255)" -> "VARCHAR"
     * - "DECIMAL(10,2)" -> "DECIMAL"
     * - "INT NOT NULL" -> "INT"
     * - "TIMESTAMP" -> "TIMESTAMP"
     *
     * @param sqlDefinition SQL定义字符串，可能包含长度、约束等附加信息
     * @return 纯数据类型名称，输入为空时返回空字符串
     */
    public static String extractDataType(String sqlDefinition) {
        if (sqlDefinition == null || sqlDefinition.trim().isEmpty()) {
            return "";
        }
        String trimmed = sqlDefinition.trim();
        // 优先查找左括号，处理带长度精度的类型定义（如VARCHAR(255)、DECIMAL(10,2)）
        int parenthesisIndex = trimmed.indexOf('(');
        if (parenthesisIndex > 0) {
            return trimmed.substring(0, parenthesisIndex);
        }
        // 其次查找空格，处理带约束条件的类型定义（如INT NOT NULL、VARCHAR UNIQUE）
        int spaceIndex = trimmed.indexOf(' ');
        if (spaceIndex > 0) {
            return trimmed.substring(0, spaceIndex);
        }
        return trimmed;
    }

    /**
     * 设置字段值（支持链式调用）
     *
     * @param value 要设置的字段值，可以是任意类型的对象
     * @return 当前FieldValue对象本身，支持方法链式调用
     */
    public FieldValue setValue(Object value) {
        this.value = value;
        return this;
    }

    /**
     * 判断字段值是否不为空（非null）
     *
     * @return 字段值不为null时返回true，否则返回false
     */
    public boolean isNotNullVal() {
        return null != this.value;
    }

    /**
     * 判断字段值是否为空（null）
     *
     * @return 字段值为null时返回true，否则返回false
     */
    public boolean isNullVal() {
        return null == this.value;
    }

    @Override
    public String toString() {
        return "FieldValue{" + "sqlType=" + sqlType + ", value=" + value + ", primary=" + primary + ", columnName='" + columnName + '\'' + '}';
    }
}
