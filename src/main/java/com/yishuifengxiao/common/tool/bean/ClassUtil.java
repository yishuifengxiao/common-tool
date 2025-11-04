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
     * 字段缓存，提高重复调用性能
     */
    private static final Map<String, List<Field>> FIELDS_CACHE = new ConcurrentHashMap<>();

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
     * 获取指定类的所有字段
     * @param clazz 要获取字段的类，不能为null
     * @param noSpecialModifier 是否排除特殊修饰符的字段
     * @return 类的字段列表
     * @throws NullPointerException 当clazz参数为null时抛出
     */
    public static <T> List<Field> fields(Class<T> clazz, boolean noSpecialModifier) {
        // 参数校验
        if (clazz == null) {
            throw new NullPointerException("Class cannot be null");
        }

        // 使用缓存提高性能
        return getFieldsFromCache(clazz, noSpecialModifier);
    }


    /**
     * 判断给定的字段是否为特殊修饰字段
     *
     * @param field 要检查的字段对象，可以为null
     * @return 如果字段是特殊修饰字段则返回true，否则返回false
     */
    public static boolean isSpecialModifier(Field field) {
        if (field == null) {
            return false;
        }

        // 过滤编译器生成的字段（如内部类的this$0字段）
        String fieldName = field.getName();
        if (fieldName.startsWith("this$") || fieldName.startsWith("val$")) {
            return true;
        }

        // 缓存 javax.persistence.Transient 类，避免频繁反射查找
        Class<? extends Annotation> transientAnnotationClass = getTransientAnnotationClass();

        if (transientAnnotationClass != null && field.isAnnotationPresent(transientAnnotationClass)) {
            return true;
        }

        // 检查字段修饰符 - 只过滤transient字段，其他修饰符都是正常的
        int modifiers = field.getModifiers();
        return Modifier.isTransient(modifiers);
    }



    /**
     * 从缓存中获取指定类的字段列表，支持缓存和字段过滤功能
     *
     * @param <T> 类的泛型类型
     * @param clazz 要获取字段的类
     * @param noSpecialModifier 是否不过滤特殊修饰符字段的标志，
     *                          true表示不过滤任何字段，false表示只返回非特殊修饰符字段
     * @return 指定类及其父类的所有字段列表，如果启用了过滤则只包含非特殊修饰符字段
     */
    private static <T> List<Field> getFieldsFromCache(Class<T> clazz, boolean noSpecialModifier) {
        // 构建缓存键，包含类名和过滤标志
        String cacheKey = clazz.getName() + ":" + noSpecialModifier;

        return FIELDS_CACHE.computeIfAbsent(cacheKey, key -> {
            List<Field> result = new ArrayList<>();
            Class<?> current = clazz;

            // 遍历类及其所有父类（直到Object类）
            while (current != null && current != Object.class) {
                try {
                    Field[] declaredFields = current.getDeclaredFields();

                    // 对于每个字段，检查是否需要过滤
                    for (Field field : declaredFields) {
                        // 确保字段不为null
                        if (field != null) {
                            // 修复逻辑：只有当字段不是特殊修饰时才添加到结果中
                            // 当noSpecialModifier为true时，不过滤任何字段；为false时，只返回非特殊修饰符字段
                            if (noSpecialModifier || !isSpecialModifier(field)) {
                                // 设置字段为可访问，确保能获取私有字段
                                field.setAccessible(true);
                                result.add(field);
                            }
                        }
                    }
                } catch (SecurityException e) {
                    if (log != null && log.isWarnEnabled()) {
                        log.warn("获取字段时发生安全异常，类名：{}", current.getName(), e);
                    }
                }

                current = current.getSuperclass();
            }

            // 返回不可修改的列表，防止外部修改
            return Collections.unmodifiableList(result);
        });
    }



    private static volatile Class<? extends Annotation> transientAnnotationClass;

    /**
     * 获取 javax.persistence.Transient 注解类并进行缓存
     * <p>
     * 使用双重检查锁模式确保线程安全，并缓存结果提高性能
     * </p>
     *
     * @return javax.persistence.Transient 注解类，如果不存在则返回null
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Annotation> getTransientAnnotationClass() {
        // 第一次检查，避免不必要的同步
        if (transientAnnotationClass != null) {
            return transientAnnotationClass;
        }

        // 同步块确保线程安全
        synchronized (ClassUtil.class) {
            // 第二次检查，防止其他线程已经初始化
            if (transientAnnotationClass != null) {
                return transientAnnotationClass;
            }

            try {
                // 动态加载 javax.persistence.Transient 类
                Class<?> clazz = Class.forName("javax.persistence.Transient");

                // 确保加载的类确实是注解类型
                if (Annotation.class.isAssignableFrom(clazz)) {
                    transientAnnotationClass = (Class<? extends Annotation>) clazz;
                }
            } catch (ClassNotFoundException e) {
                // 类不存在时记录跟踪日志（仅在跟踪级别启用时）
                if (log != null && log.isTraceEnabled()) {
                    log.trace("未找到 javax.persistence.Transient 类，跳过注解检查");
                }
            }

            // 返回结果（可能为null）
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
     * 从嵌套对象中提取指定字段的值
     *
     * @param data 包含嵌套结构的数据对象
     * @param nestedFieldName 嵌套字段名称，使用点号分隔，如 "user.address.street"
     * @return 返回提取到的字段值，如果路径中的任何对象为null则返回null
     */
    private static Object extractNestedValue(Object data, String nestedFieldName) {
        // 将嵌套字段名按点号分割成多个层级
        String[] fieldParts = nestedFieldName.split("\\.");
        Object currentObject = data;

        // 逐级深入访问嵌套对象，直到获取最终字段值
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
     * @param data 需要提取属性值的对象，可以是普通对象或Map类型
     * @param fieldName 要提取的属性名称
     * @return 返回提取到的属性值，如果未找到或发生异常则返回null
     */
    private static Object extractSimpleValue(Object data, String fieldName) {
        // 首先检查是否为Map对象
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            return map.get(fieldName);
        }

        try {
            // 查找对象中的指定字段
            Field field = findField(data.getClass(), fieldName);
            if (field == null) {
                return null;
            }

            // 设置字段可访问并获取字段值
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
     * @param clazz 要查找字段的类
     * @param fieldName 要查找的字段名称
     * @return 找到的字段对象，如果未找到则返回null
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        // 构造缓存键值
        String cacheKey = clazz.getName() + ":" + fieldName;

        // 从缓存中获取字段，如果不存在则进行查找并缓存结果
        return FIELD_LOOKUP_CACHE.computeIfAbsent(cacheKey, key -> {
            // 获取类的所有字段
            List<Field> allFields = fields(clazz);
            // 遍历所有字段查找匹配的字段名
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
        if (function == null) {
            return null;
        }

        try {
            // 获取 writeReplace 方法用于反序列化 Lambda 表达式
            Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);

            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(function);
            String implMethodName = serializedLambda.getImplMethodName();

            // 验证方法名是否符合getter命名规范
            if (!isValidGetterName(implMethodName)) {
                return null;
            }

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
     * 验证方法名是否符合getter命名规范
     *
     * @param methodName 方法名
     * @return 是否符合getter命名规范
     */
    private static boolean isValidGetterName(String methodName) {
        // 检查是否为标准的getter方法名（以"get"开头且长度大于3）
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return true;
        }

        // 检查是否为boolean类型的getter方法名（以"is"开头且长度大于2）
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return true;
        }

        return false;
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
     * 函数式接口
     *
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     */
    @FunctionalInterface
    public interface SerFunction<T extends Object, R extends Object> extends java.util.function.Function<T, R>, Serializable {

    }

}
