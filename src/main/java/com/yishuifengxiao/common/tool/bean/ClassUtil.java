package com.yishuifengxiao.common.tool.bean;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

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
     * 提取类中所有属性字段（包括父类属性）
     *
     * @param <T>   对象类型
     * @param clazz 待处理的类
     * @return 所有提取的属性字段列表
     */
    public static <T> List<Field> fields(Class<T> clazz) {
        return fields(clazz, false);
    }

    /**
     * 获取指定类的所有字段
     *
     * @param clazz              要获取字段的类，不能为null
     * @param noSpecialModifier 是否包含特殊修饰符字段（transient、@Transient注解等）
     * @return 类的字段列表
     * @throws NullPointerException 当clazz参数为null时抛出
     */
    public static <T> List<Field> fields(Class<T> clazz, boolean noSpecialModifier) {
        if (clazz == null) {
            throw new NullPointerException("Class cannot be null");
        }
        return getFieldsFromCache(clazz, noSpecialModifier);
    }

    /**
     * 判断字段是否为特殊修饰字段
     *
     * @param field 要检查的字段对象
     * @return 如果字段是特殊修饰字段（transient、@Transient注解、编译器生成字段）则返回true
     */
    public static boolean isSpecialModifier(Field field) {
        if (field == null) {
            return false;
        }

        String fieldName = field.getName();
        if (fieldName.startsWith("this$") || fieldName.startsWith("val$")) {
            return true;
        }

        Class<? extends Annotation> transientAnnotationClass = getTransientAnnotationClass();
        if (transientAnnotationClass != null && field.isAnnotationPresent(transientAnnotationClass)) {
            return true;
        }

        int modifiers = field.getModifiers();
        return Modifier.isTransient(modifiers);
    }

    /**
     * 从缓存中获取指定类的字段列表
     *
     * @param clazz              要获取字段的类
     * @param noSpecialModifier 是否包含特殊修饰符字段
     * @return 指定类及其父类的所有字段列表
     */
    private static <T> List<Field> getFieldsFromCache(Class<T> clazz, boolean noSpecialModifier) {
        String cacheKey = clazz.getName() + ":" + noSpecialModifier;

        return FIELDS_CACHE.computeIfAbsent(cacheKey, key -> {
            List<Field> result = new ArrayList<>();
            Class<?> current = clazz;

            while (current != null && current != Object.class) {
                try {
                    Field[] declaredFields = current.getDeclaredFields();
                    for (Field field : declaredFields) {
                        if (field != null) {
                            if (noSpecialModifier || !isSpecialModifier(field)) {
                                field.setAccessible(true);
                                result.add(field);
                            }
                        }
                    }
                } catch (SecurityException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("获取字段时发生安全异常，类名：{}", current.getName(), e);
                    }
                }
                current = current.getSuperclass();
            }

            return Collections.unmodifiableList(result);
        });
    }

    /**
     * javax.persistence.Transient注解类缓存
     */
    private static volatile Class<? extends Annotation> transientAnnotationClass;

    /**
     * 获取javax.persistence.Transient注解类并缓存
     *
     * @return javax.persistence.Transient注解类，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> getTransientAnnotationClass() {
        if (transientAnnotationClass != null) {
            return transientAnnotationClass;
        }

        synchronized (ClassUtil.class) {
            if (transientAnnotationClass != null) {
                return transientAnnotationClass;
            }

            try {
                Class<?> clazz = Class.forName("javax.persistence.Transient");
                if (Annotation.class.isAssignableFrom(clazz)) {
                    transientAnnotationClass = (Class<? extends Annotation>) clazz;
                }
            } catch (ClassNotFoundException e) {
                if (log.isTraceEnabled()) {
                    log.trace("未找到 javax.persistence.Transient 类，跳过注解检查");
                }
            }

            return transientAnnotationClass;
        }
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

        return extractSimpleValue(data, trimmedFieldName);
    }

    /**
     * 从嵌套对象中提取指定字段的值
     *
     * @param data            包含嵌套结构的数据对象
     * @param nestedFieldName 嵌套字段名称，使用点号分隔
     * @return 提取到的字段值，路径中任一对象为null则返回null
     */
    private static Object extractNestedValue(Object data, String nestedFieldName) {
        String[] fieldParts = nestedFieldName.split("\\.");
        Object currentObject = data;

        for (String fieldPart : fieldParts) {
            if (currentObject == null) {
                return null;
            }
            currentObject = extractSimpleValue(currentObject, fieldPart);
        }

        return currentObject;
    }

    /**
     * 从指定对象中提取简单属性值
     *
     * @param data      需要提取属性值的对象（支持普通对象或Map）
     * @param fieldName 要提取的属性名称
     * @return 提取到的属性值，未找到或发生异常则返回null
     */
    private static Object extractSimpleValue(Object data, String fieldName) {
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            return map.get(fieldName);
        }

        try {
            Field field = findField(data.getClass(), fieldName);
            if (field == null) {
                return null;
            }
            field.setAccessible(true);
            return field.get(data);
        } catch (IllegalAccessException e) {
            if (log.isWarnEnabled()) {
                log.warn("根据属性名获取属性值时发生访问异常，属性名：{}，异常原因：{}", fieldName, e.getMessage(), e);
            }
        } catch (Exception e) {
            if (log.isInfoEnabled()) {
                log.info("根据属性名获取属性值时发生异常，属性名：{}，异常原因：{}", fieldName, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 在指定类中查找指定名称的字段
     *
     * @param clazz      要查找字段的类
     * @param fieldName  要查找的字段名称
     * @return 找到的字段对象，未找到则返回null
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        String cacheKey = clazz.getName() + ":" + fieldName;

        return FIELD_LOOKUP_CACHE.computeIfAbsent(cacheKey, key -> {
            List<Field> allFields = fields(clazz);
            for (Field field : allFields) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            return null;
        });
    }

    /**
     * 字段访问缓存
     */
    private static final Map<String, Boolean> FIELD_ACCESSIBLE_CACHE = new ConcurrentHashMap<>();

    /**
     * 遍历对象所有属性和值
     *
     * @param data   待处理的对象
     * @param action 遍历操作
     */
    public static void forEach(Object data, BiConsumer<Field, Object> action) {
        if (data == null || action == null) {
            return;
        }

        List<Field> fields = fields(data.getClass());
        for (Field field : fields) {
            try {
                ensureFieldAccessible(field);
                Object value = field.get(data);
                action.accept(field, value);
            } catch (IllegalAccessException e) {
                if (log.isWarnEnabled()) {
                    log.warn("访问字段时发生异常，字段名：{}，异常原因：{}", field.getName(), e.getMessage(), e);
                }
            } catch (Exception e) {
                if (log.isInfoEnabled()) {
                    log.info("处理字段时发生异常，字段名：{}，异常原因：{}", field.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * 遍历对象所有属性，支持字段过滤
     *
     * @param data        待处理的对象
     * @param action      遍历操作
     * @param fieldFilter 字段过滤器，返回true表示处理该字段
     */
    public static void forEach(Object data, BiConsumer<Field, Object> action, java.util.function.Predicate<Field> fieldFilter) {
        if (data == null || action == null || fieldFilter == null) {
            return;
        }

        List<Field> fields = fields(data.getClass());
        for (Field field : fields) {
            if (!fieldFilter.test(field)) {
                continue;
            }

            try {
                ensureFieldAccessible(field);
                Object value = field.get(data);
                action.accept(field, value);
            } catch (IllegalAccessException e) {
                if (log.isWarnEnabled()) {
                    log.warn("访问字段时发生异常，字段名：{}，异常原因：{}", field.getName(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 批量获取对象所有字段的值
     *
     * @param data 待处理的对象
     * @return 字段名到字段值的映射
     */
    public static Map<String, Object> getAllFieldValues(Object data) {
        Map<String, Object> result = new java.util.HashMap<>();
        if (data == null) {
            return result;
        }

        forEach(data, (field, value) -> result.put(field.getName(), value));
        return result;
    }

    /**
     * 批量设置对象所有字段的值
     *
     * @param data   待处理的对象
     * @param values 字段名到字段值的映射
     */
    public static void setAllFieldValues(Object data, Map<String, Object> values) {
        if (data == null || values == null || values.isEmpty()) {
            return;
        }

        List<Field> fields = fields(data.getClass());
        for (Field field : fields) {
            String fieldName = field.getName();
            if (values.containsKey(fieldName)) {
                try {
                    ensureFieldAccessible(field);
                    Object value = values.get(fieldName);
                    field.set(data, value);
                } catch (IllegalAccessException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("设置字段值时发生异常，字段名：{}，异常原因：{}", fieldName, e.getMessage(), e);
                    }
                } catch (IllegalArgumentException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("设置字段值时类型不匹配，字段名：{}，期望类型：{}，实际值：{}",
                                fieldName, field.getType().getSimpleName(), values.get(fieldName), e);
                    }
                }
            }
        }
    }

    /**
     * 确保字段可访问，使用缓存提高性能
     *
     * @param field 字段对象
     */
    private static void ensureFieldAccessible(Field field) {
        String cacheKey = field.getDeclaringClass().getName() + ":" + field.getName();

        if (!FIELD_ACCESSIBLE_CACHE.computeIfAbsent(cacheKey, key -> {
            boolean accessible = field.isAccessible();
            if (!accessible) {
                field.setAccessible(true);
            }
            return true;
        })) {
            field.setAccessible(true);
        }
    }

    /**
     * 根据POJO属性的Function函数获取原始属性名称
     *
     * @param function POJO属性的Function函数
     * @param <T>      函数输入类型
     * @param <R>      函数返回类型
     * @return Function函数对应的原始属性名称
     */
    public static <T, R> String pojoFieldName(SerFunction<T, R> function) {
        if (function == null) {
            return null;
        }

        try {
            Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);

            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(function);
            String implMethodName = serializedLambda.getImplMethodName();

            if (!isValidGetterName(implMethodName)) {
                return null;
            }

            boolean isBooleanGetter = isBooleanTypeGetter(serializedLambda.getInstantiatedMethodType(), implMethodName);
            String fieldName = isBooleanGetter ? Introspector.decapitalize(implMethodName.substring(2))
                    : Introspector.decapitalize(implMethodName.substring(3));

            return fieldName;

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to obtain field name from POJO attribute Function due to reflection issues.", e);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Unexpected error occurred while extracting field name from lambda expression.", e);
            }
        }

        return null;
    }

    /**
     * 验证方法名是否符合getter命名规范
     *
     * @param methodName 方法名
     * @return 是否符合getter命名规范
     */
    private static boolean isValidGetterName(String methodName) {
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return true;
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return true;
        }
        return false;
    }

    /**
     * 判断方法是否是Boolean类型对应的isXxx() getter方法
     *
     * @param instantiatedMethodType 实例化的方法签名
     * @param methodName             方法名
     * @return 是否为Boolean类型的is开头getter方法
     */
    private static boolean isBooleanTypeGetter(String instantiatedMethodType, String methodName) {
        return instantiatedMethodType.endsWith("Ljava/lang/Boolean;") && methodName.startsWith("is");
    }

    /**
     * 可序列化函数式接口
     *
     * @param <T> 函数输入类型
     * @param <R> 函数返回类型
     */
    @FunctionalInterface
    public interface SerFunction<T extends Object, R extends Object> extends java.util.function.Function<T, R>, Serializable {
    }

}