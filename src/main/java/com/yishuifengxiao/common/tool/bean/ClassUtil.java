/**
 *
 */
package com.yishuifengxiao.common.tool.bean;

import com.yishuifengxiao.common.tool.collections.DataUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.beans.Introspector;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
 * <p>
 * <strong>当前工具是一个线程安全类工具</strong>
 * </p>
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
        return fields(null, clazz);
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
        List<Field> fields = fields(null, clazz);

        return noSpecialModifier ? fields.stream().filter(v -> !isSpecialModifier(v)).collect(Collectors.toList()) :
                fields;
    }

    /**
     * 提取出类的所有属性
     *
     * @param <T>   对象类型
     * @param list  提取后的属性字段
     * @param clazz 待处理的类
     * @return 所有提取的属性字段
     */
    private static <T> List<Field> fields(List<Field> list, Class<T> clazz) {
        list = null == list ? new ArrayList<>() : list;
        DataUtil.stream(clazz.getDeclaredFields()).filter(Objects::nonNull).forEach(list::add);
        Class<? super T> superclass = clazz.getSuperclass();
        if (null == superclass || superclass.isAssignableFrom(Object.class)) {
            return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        return fields(list, superclass);
    }

    /**
     * <p>
     * 该字段是否被特殊修饰
     * </p>
     *
     * <p>
     * 特殊修饰的关键字如：<code>@Transient</code>、<code>final</code>、<code>static</code>、<code>native</code>、<code>abstract
     * </code>
     * </p>
     *
     * @param field 字段
     * @return true表示被特殊修饰
     */
    public static boolean isSpecialModifier(Field field) {
        final Annotation[] annotations = field.getAnnotations();
        // 如果被@Transient修饰了就不处理
        if (Arrays.stream(annotations).anyMatch(v -> "javax.persistence.Transient".equals(v.getClass().getName()))) {
            return true;
        }
        // 被Transient 修饰的不处理
        if (Modifier.isTransient(field.getModifiers())) {
            return true;
        }

        // 被final修饰的不处理
        if (Modifier.isFinal(field.getModifiers())) {
            return true;
        }

        // 被static 修饰的不处理
        if (Modifier.isStatic(field.getModifiers())) {
            return true;
        }

        // 被native 修饰的不处理
        if (Modifier.isNative(field.getModifiers())) {
            return true;
        }

        // 被abstract 修饰的不处理
        if (Modifier.isAbstract(field.getModifiers())) {
            return true;
        }

        // 属性为接口
        return Modifier.isInterface(field.getModifiers());
    }

    /**
     * 根据属性名字获取对象里对应属性的值
     *
     * @param data      待处理的对象
     * @param fieldName 属性名字
     * @return 该属性对应的值
     */
    public static Object extractValue(Object data, String fieldName) {
        if (null == data || StringUtils.isBlank(fieldName)) {
            return null;
        }
        try {
            Field field =
                    fields(data.getClass()).stream().filter(v -> v.getName().equals(fieldName.trim())).findFirst().orElse(null);
            if (null == field) {
                return null;
            }
            field.setAccessible(true);
            return field.get(data);
        } catch (Exception e) {
            log.warn("根据属性名获取属性值时出现问题，出现问题的原因为 {}", e.getMessage());
        }
        return null;
    }

    /**
     * 遍历对象所有的属性和值
     *
     * @param data   待处理的对象
     * @param action 遍历操作
     */
    public static void forEach(Object data, BiConsumer<Field, Object> action) {
        fields(data.getClass()).forEach(t -> action.accept(t, extractValue(data, t.getName())));
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
            Method writeReplace = function.getClass().getDeclaredMethod("writeReplace");
            writeReplace.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) writeReplace.invoke(function);
            // 第2步 implMethodName 即为Field对应的Getter方法名
            String implMethodName = serializedLambda.getImplMethodName();
            // 判断是否为boolean属性字段对应的后的is开头的getter方法
            String instantiatedMethodType = serializedLambda.getInstantiatedMethodType();
            boolean bool = instantiatedMethodType.endsWith("Ljava/lang/Boolean;") && implMethodName.startsWith("is");
            String fieldName = bool ? Introspector.decapitalize(implMethodName.substring(2)) :
                    Introspector.decapitalize(implMethodName.substring(3));
            return fieldName;
        } catch (Exception e) {
            log.warn("根据pojo类的属性的Function函数获取原始属性的名字，出现问题的原因为 {}", e.getMessage());
        }

        return null;

    }


    /**
     * Legacy version of {@link java.util.function.Function java.util.function.Functiona}.
     *
     * <p>The {@link SerFunction} class provides common functions and related utilities.
     *
     * <p>As this interface extends {@code java.util.function.Functiona}, an instance of this type can be
     * used as a {@code java.util.function.Functiona} directly. To use a {@code
     * java.util.function.Functiona} in a context where a {@code com.google.common.base.Functiona} is
     * needed, use {@code function::apply}.
     *
     * <p>This interface is now a legacy type. Use {@code java.util.function.Functiona} (or the
     * appropriate primitive specialization such as {@code ToIntFunction}) instead whenever possible.
     * Otherwise, at least reduce <i>explicit</i> dependencies on this type by using lambda expressions
     * or method references instead of classes, leaving your code easier to migrate in the future.
     *
     * <p>See the Guava User Guide article on <a
     * href="https://github.com/google/guava/wiki/FunctionalExplained">the use of {@code Functiona}</a>.
     *
     * @author Kevin Bourrillion
     * @since 2.0
     */
    @FunctionalInterface
    public interface SerFunction<T extends Object, R extends Object> extends java.util.function.Function<T, R>,
            Serializable {

    }

}
