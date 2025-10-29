/**
 *
 */
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
 * class 工具
 * </p>
 *
 * <p>
 * 主要功能为
 * </p>
 * <ul>
 * <li>获取类的所有属性字段</li>
 * <li>根据属性的名字获取对象的属性的值</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class ClassUtil {

    /**
     * 提取出一个类里所有的属性字段(包括父类里的属性字段)
     *
     * @param <T>   对象类型
     * @param clazz 待处理的类
     * @return 所有提取的属性字段
     */
    public static <T> List<Field> fields(Class<T> clazz) {
        return fields(clazz, false);
    }

    /**
     * 提取出一个类里所有的属性字段(包括父类里的属性字段)
     *
     * @param <T>               对象类型
     * @param clazz             待处理的类
     * @param noSpecialModifier 是否过滤掉特殊修饰的字段
     * @return 所有提取的属性字段
     */
    public static <T> List<Field> fields(Class<T> clazz, boolean noSpecialModifier) {
        // 参数校验
        if (clazz == null) {
            return Collections.emptyList();
        }

        // 使用缓存提高性能
        return getFieldsFromCache(clazz, noSpecialModifier);
    }

    /**
     * 字段缓存，提高重复调用性能
     */
    private static final Map<String, List<Field>> FIELDS_CACHE = new ConcurrentHashMap<>();

    /**
     * 从缓存获取字段列表，如果缓存未命中则构建并缓存
     */
    private static <T> List<Field> getFieldsFromCache(Class<T> clazz, boolean noSpecialModifier) {
        // 构建缓存键，包含类名和过滤标志
        String cacheKey = clazz.getName() + ":" + noSpecialModifier;

        return FIELDS_CACHE.computeIfAbsent(cacheKey, key -> {
            List<Field> result = new ArrayList<>();
            Class<?> current = clazz;

            // 遍历类及其所有父类（直到Object类）
            while (current != null && current != Object.class) {
                Field[] declaredFields = current.getDeclaredFields();

                // 对于每个字段，检查是否需要过滤
                for (Field field : declaredFields) {
                    // getDeclaredFields()不会返回null元素，无需额外null检查
                    if (!noSpecialModifier || !isSpecialModifier(field)) {
                        result.add(field);
                    }
                }

                current = current.getSuperclass();
            }

            // 返回不可修改的列表，防止外部修改
            return Collections.unmodifiableList(result);
        });
    }

    /**
     * <p>
     * 该字段是否被特殊修饰
     * </p>
     *
     * <p>
     * 特殊修饰的关键字如：<code>@Transient</code>、<code>final</code>、<code>static</code>、<code>native
     * </code>、<code>abstract
     * </code>
     * </p>
     *
     * @param field 字段
     * @return true表示被特殊修饰
     */
    public static boolean isSpecialModifier(Field field) {
        if (field == null) {
            return false;
        }

        // 缓存 javax.persistence.Transient 类，避免频繁反射查找
        Class<? extends Annotation> transientAnnotationClass = getTransientAnnotationClass();

        if (transientAnnotationClass != null && field.isAnnotationPresent(transientAnnotationClass)) {
            return true;
        }

        // 检查字段修饰符
        int modifiers = field.getModifiers();
        return Modifier.isTransient(modifiers) || Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers) || Modifier.isNative(modifiers) || Modifier.isAbstract(modifiers);
    }

    // 缓存 Transient 注解类，仅加载一次
    private static volatile Class<? extends Annotation> transientAnnotationClass;

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
                if (log != null && log.isTraceEnabled()) {
                    log.trace("未找到 javax.persistence.Transient 类，跳过注解检查");
                }
            }
            return transientAnnotationClass;
        }
    }


    /**
     * 字段查找缓存，提高 extractValue 方法性能
     */
    private static final Map<String, Field> FIELD_LOOKUP_CACHE = new ConcurrentHashMap<>();

    /**
     * 根据属性名字获取对象里对应属性的值
     *
     * @param data      待处理的对象
     * @param fieldName 属性名字，支持嵌套属性如 "user.address.street"
     * @return 该属性对应的值
     */
    public static Object extractValue(Object data, String fieldName) {
        if (null == data || StringUtils.isBlank(fieldName)) {
            return null;
        }

        String trimmedFieldName = fieldName.trim();

        // 检查是否为嵌套属性
        if (trimmedFieldName.contains(".")) {
            return extractNestedValue(data, trimmedFieldName);
        }

        return extractSimpleValue(data, trimmedFieldName);
    }

    /**
     * 提取嵌套属性的值
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
     * 提取简单属性的值
     */
    private static Object extractSimpleValue(Object data, String fieldName) {
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
     * 查找字段，使用缓存提高性能
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
     * 字段访问缓存，提高 forEach 方法性能
     */
    private static final Map<String, Boolean> FIELD_ACCESSIBLE_CACHE = new ConcurrentHashMap<>();

    /**
     * 遍历对象所有的属性和值
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
                // 检查并设置字段可访问性
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
            // 如果缓存值为false，重新设置可访问性
            field.setAccessible(true);
        }
    }


    /**
     * 根据pojo类的属性的Function函数获取原始属性的名字
     *
     * @param function pojo类的属性的Function函数
     * @param <T>      the type of the input to the function
     * @param <R>      the type of the result of the function
     * @return pojo类的属性的Function函数对应的原始属性的名字
     */
    public static <T, R> String pojoFieldName(SerFunction<T, R> function) {
        try {
            // 获取 writeReplace 方法用于反序列化 Lambda 表达式
            Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);

            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(function);
            String implMethodName = serializedLambda.getImplMethodName();

            // 判断是否为 Boolean 类型的 isXxx() 形式的 getter 方法
            boolean isBooleanGetter = isBooleanTypeGetter(serializedLambda.getInstantiatedMethodType(), implMethodName);

            // 截取字段名称
            String fieldName = isBooleanGetter ? Introspector.decapitalize(implMethodName.substring(2)) : Introspector.decapitalize(implMethodName.substring(3));

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
     * 判断该方法是否是 Boolean 类型对应的 isXxx() getter 方法
     *
     * @param instantiatedMethodType 实例化的方法签名
     * @param methodName             方法名
     * @return 是否为 Boolean 类型的 is 开头的 getter 方法
     */
    private static boolean isBooleanTypeGetter(String instantiatedMethodType, String methodName) {
        return instantiatedMethodType.endsWith("Ljava/lang/Boolean;") && methodName.startsWith("is");
    }


    /**
     * Legacy version of {@link java.util.function.Function java.util.function.Functiona}.
     *
     * <p>The {@link SerFunction} class provides common functions and related utilities.
     *
     * <p>As this interface extends {@code java.util.function.Functiona}, an instance of this
     * type can be
     * used as a {@code java.util.function.Functiona} directly. To use a {@code
     * java.util.function.Functiona} in a context where a {@code com.google.common.base
     * .Functiona} is
     * needed, use {@code function::apply}.
     *
     * <p>This interface is now a legacy type. Use {@code java.util.function.Functiona} (or the
     * appropriate primitive specialization such as {@code ToIntFunction}) instead whenever
     * possible.
     * Otherwise, at least reduce <i>explicit</i> dependencies on this type by using lambda
     * expressions
     * or method references instead of classes, leaving your code easier to migrate in the future.
     *
     * <p>See the Guava User Guide article on <a
     * href="https://github.com/google/guava/wiki/FunctionalExplained">the use of {@code
     * Functiona}</a>.
     *
     * @author Kevin Bourrillion
     * @since 2.0
     */
    @FunctionalInterface
    public interface SerFunction<T extends Object, R extends Object> extends java.util.function.Function<T, R>, Serializable {

    }

}
