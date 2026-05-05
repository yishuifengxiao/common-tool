package com.yishuifengxiao.common.tool.jdbc;

import com.yishuifengxiao.common.tool.bean.ClassUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 字段提取器和工具类，用于从实体类中提取字段信息
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class FieldExtractor {
    private static final Map<String, List<FieldValue>> FIELDS_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> TABLE_MAP = new ConcurrentHashMap<>();

    /**
     * 从实体对象中提取字段值
     *
     * @param t  实体对象
     * @param <T> 实体类型
     * @return 字段值列表
     */
    public static <T> List<FieldValue> extractFieldValue(T t) {
        if (null == t) {
            return java.util.Collections.emptyList();
        }
        Class<?> clazz = t.getClass();
        List<FieldValue> fieldDefinitions = extractField(clazz);
        return fieldDefinitions.stream().map(fieldDef -> {
            String fieldName = fieldDef.getField().getName();
            try {
                Object value = ClassUtil.extractValue(t, fieldName);
                return new FieldValue(fieldDef.getField(), fieldDef.isPrimary(), value);
            } catch (Exception e) {
                return new FieldValue(fieldDef.getField(), fieldDef.isPrimary(), fieldDef.getValue());
            }
        }).collect(Collectors.toList());
    }

    /**
     * 从实体类中提取字段定义
     *
     * @param clazz 实体类类型
     * @param <T>  实体类型
     * @return 字段值列表
     */
    public static <T> List<FieldValue> extractField(Class<T> clazz) {
        if (null == clazz) {
            return java.util.Collections.emptyList();
        }
        return FIELDS_MAP.computeIfAbsent(clazz.getName(), key -> {
            try {
                List<Field> fields = com.yishuifengxiao.common.tool.bean.ClassUtil.fields(clazz, true);
                return fields.stream()
                        .filter(field -> !Modifier.isStatic(field.getModifiers()))
                        .filter(field -> !Modifier.isFinal(field.getModifiers()))
                        .filter(field -> !Modifier.isNative(field.getModifiers()))
                        .filter(field -> !Modifier.isAbstract(field.getModifiers()))
                        .filter(field -> !field.getType().isInterface())
                        .filter(field -> !Modifier.isTransient(field.getModifiers()))
                        .filter(field -> field.getAnnotation(Transient.class) == null)
                        .map(field -> new FieldValue(field, isPrimary(field)))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract fields from class: " + clazz.getName(), e);
            }
        });
    }

    /**
     * 从实体类中提取表名
     *
     * @param clazz 实体类类型
     * @param <T>  实体类型
     * @return 表名
     */
    public static <T> String extractTableName(Class<T> clazz) {
        return TABLE_MAP.computeIfAbsent(clazz.getName(), key -> {
            String name = Optional.ofNullable(AnnotationUtils.findAnnotation(clazz, jakarta.persistence.Table.class))
                    .map(jakarta.persistence.Table::name)
                    .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                    .orElse(null);
            if (org.apache.commons.lang3.StringUtils.isBlank(name)) {
                name = Optional.ofNullable(AnnotationUtils.findAnnotation(clazz, Entity.class))
                        .map(Entity::name)
                        .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                        .orElse(null);
            }
            if (org.apache.commons.lang3.StringUtils.isBlank(name)) {
                name = com.yishuifengxiao.common.tool.text.TextUtil.underscoreName(clazz.getSimpleName());
            }
            return name;
        });
    }

    /**
     * 从实体类中提取主键字段
     *
     * @param clazz 实体类类型
     * @param <T>  实体类型
     * @return 主键字段
     */
    public static <T> FieldValue extractPrimaryField(Class<T> clazz) {
        if (null == clazz) {
            return null;
        }
        List<FieldValue> fieldValues = extractField(clazz);
        return fieldValues.stream().filter(FieldValue::isPrimary).findFirst().orElse(null);
    }

    /**
     * 判断字段是否为主键
     *
     * @param field 字段对象
     * @return 是否为主键
     */
    public static boolean isPrimary(Field field) {
        if (null == field) {
            return false;
        }
        Id id = AnnotationUtils.findAnnotation(field, Id.class);
        if (null != id) {
            return true;
        }
        Column column = AnnotationUtils.findAnnotation(field, Column.class);
        if (null != column && "id".equalsIgnoreCase(column.name())) {
            return true;
        }
        return "id".equalsIgnoreCase(field.getName());
    }

    /**
     * 判断是否为基本结果类型
     *
     * @param clazz 类型
     * @param <T> 类型
     * @return 是否为基本结果类型
     */
    public static <T> boolean isBasicResult(Class<T> clazz) {
        if (clazz == null) {
            return false;
        }
        String name = clazz.getName();
        return clazz.isPrimitive() || name.startsWith("java.lang") || name.startsWith("java.util") || name.startsWith("java.time") || name.startsWith("java.math");
    }
}