package com.yishuifengxiao.common.tool.jdbc;

import com.yishuifengxiao.common.tool.bean.ClassUtil;
import com.yishuifengxiao.common.tool.text.TextUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 字段提取器和工具类，用于从实体类中提取字段信息
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class FieldExtractor {
    /**
     * 字段定义缓存：使用ClassValue实现，随类卸载自动释放，避免静态Map无界增长与类加载器泄漏
     */
    private static final ClassValue<List<FieldValue>> FIELDS_CACHE = new ClassValue<>() {
        @Override
        protected List<FieldValue> computeValue(Class<?> type) {
            try {
                // 获取类的所有字段（包括继承的字段），并转换为主键标识的FieldValue
                return Collections.unmodifiableList(ClassUtil.fields(type, true).stream()
                        .map(field -> new FieldValue(field, isPrimary(field)))
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                throw new RuntimeException("Failed to extract fields from class: " + type.getName(), e);
            }
        }
    };

    /**
     * 表名缓存：使用ClassValue实现，随类卸载自动释放
     */
    private static final ClassValue<String> TABLE_CACHE = new ClassValue<>() {
        @Override
        protected String computeValue(Class<?> type) {
            String name = Optional.ofNullable(AnnotationUtils.findAnnotation(type, jakarta.persistence.Table.class))
                    .map(jakarta.persistence.Table::name)
                    .filter(StringUtils::isNotBlank)
                    .orElse(null);
            if (StringUtils.isBlank(name)) {
                name = Optional.ofNullable(AnnotationUtils.findAnnotation(type, Entity.class))
                        .map(Entity::name)
                        .filter(StringUtils::isNotBlank)
                        .orElse(null);
            }
            if (StringUtils.isBlank(name)) {
                name = TextUtil.underscoreName(type.getSimpleName());
            }
            return name;
        }
    };

    /**
     * 从实体对象中提取字段值
     * 提取流程：
     * 1. 检查实体对象是否为空，为空则返回空列表
     * 2. 获取实体对象的Class类型
     * 3. 调用extractField方法获取字段定义列表（包含字段元数据）
     * 4. 遍历字段定义，通过反射提取每个字段的实际值
     * 5. 创建包含字段信息和值的FieldValue对象列表
     *
     * @param t   实体对象，从中提取字段的实际值
     * @param <T> 实体类型泛型参数
     * @return 字段值列表，包含每个字段的定义、主键标识和实际值；如果对象为空则返回空列表
     */
    public static <T> List<FieldValue> extractFieldValue(T t) {
        if (null == t) {
            return Collections.emptyList();
        }
        // 获取实体对象的Class类型
        Class<?> clazz = t.getClass();
        // 获取字段定义列表（包含字段元数据，但不包含实际值）
        List<FieldValue> fieldDefinitions = extractField(clazz);
        // 遍历字段定义，提取每个字段的实际值并构建完整的FieldValue对象
        return fieldDefinitions.stream().map(fieldDef -> {
            String fieldName = fieldDef.getField().getName();
            try {
                // 通过反射从实体对象中提取字段的实际值
                Object value = ClassUtil.extractValue(t, fieldName);
                return new FieldValue(fieldDef.getField(), fieldDef.isPrimary(), value);
            } catch (Exception e) {
                // 如果提取失败，使用字段定义中的默认值，并保留排障线索
                log.debug("提取字段值失败，使用默认值，字段: {}", fieldName, e);
                return new FieldValue(fieldDef.getField(), fieldDef.isPrimary(), fieldDef.getValue());
            }
        }).collect(Collectors.toList());
    }

    /**
     * 从实体类中提取字段定义
     * 提取流程：
     * 1. 检查输入类是否为空，为空则返回空列表
     * 2. 使用缓存机制，如果已提取过则直接返回缓存结果
     * 3. 通过反射获取类的所有字段（包括父类字段）
     * 4. 过滤掉不符合要求的字段（静态、final、native、abstract、接口类型、transient等）
     * 5. 过滤掉标注了@Transient注解的字段
     * 6. 将符合条件的字段转换为FieldValue对象并缓存
     *
     * @param clazz 实体类类型，用于提取字段信息
     * @param <T>   实体类型泛型参数
     * @return 字段值列表，包含所有符合要求的字段及其元数据；如果类为空或无有效字段则返回空列表
     * @throws RuntimeException 当字段提取失败时抛出运行时异常
     */
    public static <T> List<FieldValue> extractField(Class<T> clazz) {
        if (null == clazz) {
            return Collections.emptyList();
        }
        // 使用缓存避免重复提取，提高性能
        return FIELDS_CACHE.get(clazz);
    }

    /**
     * 从实体类中提取表名
     *
     * @param clazz 实体类类型
     * @param <T>   实体类型
     * @return 表名
     */
    public static <T> String extractTableName(Class<T> clazz) {
        if (null == clazz) {
            return null;
        }
        return TABLE_CACHE.get(clazz);
    }

    /**
     * 从实体类中提取主键字段
     *
     * @param clazz 实体类类型
     * @param <T>   实体类型
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
     * @param <T>   类型
     * @return 是否为基本结果类型
     */
    public static <T> boolean isBasicResult(Class<T> clazz) {
        if (clazz == null) {
            return true;
        }
        String name = clazz.getName();
        return clazz.isPrimitive() || name.startsWith("java.lang") || name.startsWith("java.util") || name.startsWith("java.time") || name.startsWith("java.math");
    }
}