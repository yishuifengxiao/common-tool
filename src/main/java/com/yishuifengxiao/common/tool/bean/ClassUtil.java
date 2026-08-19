package com.yishuifengxiao.common.tool.bean;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * Class工具类
 * </p>
 * <p>提供类反射操作的工具方法，包括：</p>
 * <ul>
 * <li>获取类的所有属性字段（支持继承链）</li>
 * <li>根据属性名称获取对象的属性值（支持嵌套属性）</li>
 * <li>遍历对象属性并执行自定义操作</li>
 * <li>从Lambda表达式中提取POJO字段名</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class ClassUtil {

    /**
     * 字段缓存，提高重复调用性能
     */
    private static final Map<String, List<Field>> FIELDS_CACHE = new ConcurrentHashMap<>();


    /**
     * 获取指定类及其父类的所有字段列表（默认排除特殊修饰符字段）
     * 功能说明：
     * 1. 返回当前类及继承链上所有父类的字段（不包括Object类）
     * 2. 默认排除特殊修饰符的字段（如transient、static、final等）
     * 3. 使用缓存机制避免重复反射操作，提高性能
     *
     * @param clazz 要获取字段的类对象，不能为null
     * @return 字段列表，返回不可修改的List；如果类没有字段则返回空列表；结果会被缓存以提高后续查询性能
     */
    public static <T> List<Field> fields(Class<T> clazz) {
        // 调用完整版本的方法，默认排除特殊修饰符字段（noSpecialModifier=true）
        return fields(clazz, true);
    }


    /**
     * 获取指定类及其父类的所有字段列表（支持缓存）
     * 功能说明：
     * 1. 返回当前类及继承链上所有父类的字段（不包括Object类）
     * 2. 可根据参数选择是否排除特殊修饰符的字段（如transient、static、final等）
     * 3. 使用缓存机制避免重复反射操作，提高性能
     *
     * @param clazz             要获取字段的类对象，不能为null
     * @param noSpecialModifier 是否排除特殊修饰符字段；true表示排除（只返回普通字段），false表示包含所有字段
     * @return 字段列表，返回不可修改的List；如果类没有字段则返回空列表；结果会被缓存以提高后续查询性能
     */
    public static <T> List<Field> fields(Class<T> clazz, boolean noSpecialModifier) {
        // 从缓存中获取字段列表，避免重复反射操作提高性能
        return getFieldsFromCache(clazz, noSpecialModifier);
    }


    /**
     * 判断字段是否为特殊修饰字段
     * 检查规则（满足任一条件即为特殊修饰字段）：
     * 1. null字段
     * 2. 编译器生成的内部类字段（this$、val$前缀）
     * 3. 标注了@Transient注解的字段（不参与持久化）
     * 4. 使用特殊修饰符的字段：transient、static、final、native、abstract
     * 5. 接口类型的字段
     *
     * @param field 要检查的字段对象，如果为null则返回false
     * @return 如果字段是特殊修饰字段则返回true，否则返回false
     */
    public static boolean isSpecialModifier(Field field) {
        if (field == null) {
            return false;
        }

        String fieldName = field.getName();
        if (fieldName.startsWith("this$") || fieldName.startsWith("val$")) {
            return true;
        }

        Annotation[] annotations = field.getDeclaredAnnotations();
        for (Annotation annotation : annotations) {
            if ("Transient".equals(annotation.annotationType().getSimpleName())) {
                return true;
            }
        }

        int modifiers = field.getModifiers();
        if (hasSpecialModifier(modifiers) || field.getType().isInterface()) {
            return true;
        }

        return false;
    }

    /**
     * 检查修饰符是否包含特殊修饰符
     *
     * @param modifiers 字段的修饰符整数表示
     * @return 如果包含特殊修饰符则返回true，否则返回false
     */
    public static boolean hasSpecialModifier(int modifiers) {
        return Modifier.isTransient(modifiers)
                || Modifier.isStatic(modifiers)
                || Modifier.isFinal(modifiers)
                || Modifier.isNative(modifiers)
                || Modifier.isAbstract(modifiers);
    }

    /**
     * 从缓存中获取指定类及其父类的所有字段列表
     * 处理流程：
     * 1. 验证输入参数clazz是否为null，为null则抛出IllegalArgumentException
     * 2. 构建缓存键（类名:是否排除特殊修饰符）
     * 3. 使用ConcurrentHashMap的computeIfAbsent方法实现线程安全的缓存读取和写入
     * 4. 如果缓存未命中，通过反射遍历类继承链获取所有字段
     * 5. 从当前类开始，逐级向上遍历父类，直到Object类为止
     * 6. 对每个类的字段进行过滤：根据noSpecialModifier参数决定是否排除特殊修饰符字段
     * 7. 捕获SecurityException异常并记录警告日志，发生异常时继续遍历父类
     * 8. 将结果包装为不可修改的List并存入缓存
     *
     * @param clazz             要获取字段的类对象，不能为null，否则抛出IllegalArgumentException
     * @param noSpecialModifier 是否排除特殊修饰符字段；true表示排除（只返回普通字段），false表示包含所有字段
     * @return 不可修改的字段列表；如果类没有字段则返回空列表；结果会被缓存以提高后续查询性能
     * @throws IllegalArgumentException 当clazz参数为null时抛出
     */
    private static <T> List<Field> getFieldsFromCache(Class<T> clazz, boolean noSpecialModifier) {
        if (clazz == null) {
            throw new IllegalArgumentException("类对象不能为null");
        }

        String cacheKey = clazz.getName() + ":" + noSpecialModifier;

        return FIELDS_CACHE.computeIfAbsent(cacheKey, key -> {
            List<Field> result = new ArrayList<>();
            Class<?> current = clazz;

            while (current != null && current != Object.class) {
                try {
                    Field[] declaredFields = current.getDeclaredFields();
                    for (Field field : declaredFields) {
                        if (!noSpecialModifier || !isSpecialModifier(field)) {
                            result.add(field);
                        }
                    }
                } catch (SecurityException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("获取字段时发生安全异常，类名：{}，将继续遍历父类", current.getName(), e);
                    }
                }
                current = current.getSuperclass();
            }

            return Collections.unmodifiableList(result);
        });
    }


    /**
     * 字段查找缓存
     */
    private static final Map<String, Field> FIELD_LOOKUP_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据属性名获取对象中对应属性的值
     *
     * @param data      待处理的对象
     * @param fieldName 属性名称，支持嵌套属性如 "user.address.street"
     * @return 该属性对应的值，若参数无效则返回null
     */
    public static Object extractValue(Object data, String fieldName) {
        if (null == data || StringUtils.isBlank(fieldName)) {
            return null;
        }

        String trimmedFieldName = fieldName.trim();
        if (trimmedFieldName.contains(".")) {
            return extractNestedValue(data, trimmedFieldName);
        }

        return getValue(data, trimmedFieldName);
    }

    /**
     * 从嵌套对象中提取指定字段的值
     *
     * @param data            包含嵌套结构的数据对象
     * @param nestedFieldName 嵌套字段名称，使用点号分隔
     * @return 提取到的字段值，路径中任一对象为null则返回null
     */
    public static Object extractNestedValue(Object data, String nestedFieldName) {
        String[] fieldParts = nestedFieldName.split("\\.");
        Object currentObject = data;

        for (String fieldPart : fieldParts) {
            if (currentObject == null) {
                return null;
            }
            currentObject = getValue(currentObject, fieldPart);
        }

        return currentObject;
    }

    /**
     * 从指定对象中提取简单属性值
     * 提取流程：
     * 1. 如果data是Map类型，直接从Map中获取指定key的值
     * 2. 如果data是普通对象，通过反射机制获取字段值
     * 3. 查找字段并临时设置accessible为true以访问私有字段
     * 4. 获取字段值后恢复原始的accessible状态
     * 5. 发生异常时记录DEBUG日志并返回null
     *
     * @param data      需要提取属性值的对象，支持普通Java对象或Map类型；如果为null则返回null
     * @param fieldName 要提取的属性名称，区分大小写；如果为null或空字符串则返回null
     * @return 提取到的属性值，如果字段不存在、data为null或发生异常则返回null
     */
    public static Object getValue(Object data, String fieldName) {
        // 处理Map类型对象，直接通过key获取value
        if (null == data || StringUtils.isBlank(fieldName)) {
            return null;
        } else if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            return map.get(fieldName);
        } else if (data instanceof Iterable) {
            return null;
        }
        Field field = null;
        boolean originalAccessible = false;
        try {
            // 通过缓存机制查找指定名称的字段
            field = findField(data.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            // 保存字段的原始访问权限，并设置为可访问以读取私有字段
            originalAccessible = field.canAccess(data);
            if (!originalAccessible) {
                field.setAccessible(true);
            }
            // 通过反射获取字段的值
            return field.get(data);
        } catch (IllegalAccessException e) {
            // 记录访问权限异常的DEBUG日志
            if (log.isDebugEnabled()) {
                log.debug("根据属性名获取属性值时发生访问异常，属性名：{}，异常原因：{}", fieldName, e);
            }
            return null;
        } catch (Exception e) {
            // 记录其他异常的DEBUG日志
            if (log.isDebugEnabled()) {
                log.debug("根据属性名获取属性值时发生异常，属性名：{}，异常原因：{}", fieldName, e.getMessage());
            }
            return null;
        } finally {
            // 无论是否发生异常，都恢复字段的原始访问状态
            if (field != null && !originalAccessible) {
                field.setAccessible(originalAccessible);
            }
        }
    }


    /**
     * 在指定类中查找指定名称的字段
     * 查找流程：
     * 1. 构建缓存键（类名:字段名）
     * 2. 从FIELD_LOOKUP_CACHE中查找，如果已缓存则直接返回
     * 3. 如果未缓存，调用fields(clazz)获取所有字段列表（包括父类字段）
     * 4. 遍历字段列表，通过名称匹配找到目标字段
     * 5. 将结果存入缓存并返回
     *
     * @param clazz     要查找字段的类，不能为null
     * @param fieldName 要查找的字段名称，区分大小写
     * @return 找到的字段对象，如果未找到则返回null；结果会被缓存以提高后续查询性能
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        // 构建缓存键，使用类名和字段名的组合作为唯一标识
        String cacheKey = clazz.getName() + ":" + fieldName;

        // 使用缓存机制避免重复查找，提高性能
        return FIELD_LOOKUP_CACHE.computeIfAbsent(cacheKey, key -> {
            // 获取类的所有字段（包括继承的字段）
            List<Field> allFields = fields(clazz);
            // 遍历字段列表，通过名称精确匹配查找目标字段
            for (Field field : allFields) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            // 未找到匹配的字段，返回null
            return null;
        });
    }


}