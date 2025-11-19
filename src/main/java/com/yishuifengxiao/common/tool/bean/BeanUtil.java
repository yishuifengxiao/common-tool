/**
 *
 */
package com.yishuifengxiao.common.tool.bean;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * <p>
 * 对象转换工具
 * </p>
 * 该工具的主要目标是对java对象进行操作，其具备以下的几项功能
 * <ol>
 * <li>将源对象的属性值根据属性的名字复制到目标对象</li>
 * <li>将java bean 对象转换成二进制数据</li>
 * <li>将二进制数据转换成 java bean</li>
 * </ol>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class BeanUtil {

    /**
     * 将源对象里属性值复制给目标对象
     *
     * @param <S>    源对象的类型
     * @param <T>    目标对象的类型
     * @param source 源对象
     * @param target 目标对象
     * @return 复制后的目标对象
     */
    public static <S, T> T copy(S source, T target) {
        // 工具类方法通常不需要同步，除非有明确的线程安全需求
        if (null == source || null == target) {
            return target;
        }

        // 直接使用ClassUtil提供的方法过滤特殊修饰符的字段
        List<Field> targetFields = ClassUtil.fields(target.getClass(), true);
        List<Field> sourceFields = ClassUtil.fields(source.getClass(), true);

        // 构建source字段映射表用于快速查找
        Map<String, Field> sourceFieldMap = sourceFields.stream().collect(Collectors.toMap(Field::getName, f -> f, (existing, replacement) -> existing));

        for (Field targetField : targetFields) {
            String fieldName = targetField.getName();
            Field sourceField = sourceFieldMap.get(fieldName);
            if (sourceField == null) {
                continue;
            }

            boolean isTargetAccessible = targetField.isAccessible();
            boolean isSourceAccessible = sourceField.isAccessible();
            try {
                targetField.setAccessible(true);
                sourceField.setAccessible(true);

                Object value = sourceField.get(source);
                if (value != null) {
                    // 改进的类型兼容性检查
                    if (isTypeCompatible(sourceField.getType(), targetField.getType())) {
                        // 如果是基本类型和包装类型之间的转换，需要特殊处理
                        Object convertedValue = convertValue(value, targetField.getType());
                        if (convertedValue != null) {
                            targetField.set(target, convertedValue);
                        } else {
                            if (log.isTraceEnabled()) {
                                log.trace("类型转换结果为null，跳过字段'{}'的复制", fieldName);
                            }
                        }
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("类型不匹配，复制字段'{}'时失败: 源类型={}, 目标类型={}", fieldName, sourceField.getType().getSimpleName(), targetField.getType().getSimpleName());
                        }
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException e) {
                if (log.isDebugEnabled()) {
                    log.debug("复制属性{}时出现问题，从{}到{}，问题原因：{}", fieldName, source.getClass().getSimpleName(), target.getClass().getSimpleName(), e.getMessage(), e);
                }
            } finally {
                // 还原字段可访问性
                targetField.setAccessible(isTargetAccessible);
                sourceField.setAccessible(isSourceAccessible);
            }
        }

        return target;
    }

    /**
     * 检查源类型和目标类型是否兼容
     * 包括基本类型和对应的包装类型、枚举类型、数组类型等
     */
    private static boolean isTypeCompatible(Class<?> sourceType, Class<?> targetType) {
        // 类型相同或源类型是目标类型的子类
        if (targetType.isAssignableFrom(sourceType)) {
            return true;
        }

        // 处理基本类型和包装类型之间的兼容
        Map<Class<?>, Class<?>> primitiveWrapperMap = new HashMap<>();
        primitiveWrapperMap.put(boolean.class, Boolean.class);
        primitiveWrapperMap.put(byte.class, Byte.class);
        primitiveWrapperMap.put(char.class, Character.class);
        primitiveWrapperMap.put(short.class, Short.class);
        primitiveWrapperMap.put(int.class, Integer.class);
        primitiveWrapperMap.put(long.class, Long.class);
        primitiveWrapperMap.put(float.class, Float.class);
        primitiveWrapperMap.put(double.class, Double.class);
        primitiveWrapperMap.put(void.class, Void.class);

        // 检查基本类型到包装类型和包装类型到基本类型的转换
        if (targetType.equals(primitiveWrapperMap.get(sourceType)) || sourceType.equals(primitiveWrapperMap.get(targetType))) {
            return true;
        }

        // 检查枚举类型兼容性：字符串可以转换为枚举
        if (targetType.isEnum() && sourceType == String.class) {
            return true;
        }

        // 检查数组类型兼容性
        if (targetType.isArray() && sourceType.isArray()) {
            Class<?> targetComponentType = targetType.getComponentType();
            Class<?> sourceComponentType = sourceType.getComponentType();
            // 递归检查数组元素类型是否兼容
            return isTypeCompatible(sourceComponentType, targetComponentType);
        }

        // 检查字符串与日期时间类型的兼容性
        if (isDateTimeType(targetType) && sourceType == String.class) {
            return true;
        }

        // 检查数字类型之间的兼容性
        if (Number.class.isAssignableFrom(sourceType) && Number.class.isAssignableFrom(targetType)) {
            return true;
        }

        return false;
    }

    /**
     * 在需要时转换值的类型
     * 支持基本类型和包装类型之间的转换、字符串与基本类型转换、日期时间类型转换、复杂对象的递归转换
     * 以及数组类型和枚举类型的转换处理
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertValue(Object value, Class<T> targetType) {
        // 如果类型相同，直接返回
        if (targetType.isInstance(value)) {
            return (T) value;
        }

        if (value == null) {
            return null;
        }

        // 处理字符串转换为其他类型
        if (value instanceof String && !targetType.equals(String.class)) {
            return convertFromString((String) value, targetType);
        }

        // 处理枚举类型的转换
        if (targetType.isEnum()) {
            return convertToEnum(value, targetType);
        }

        // 处理数组类型的转换
        if (targetType.isArray()) {
            if (value.getClass().isArray()) {
                return convertArray(value, targetType);
            } else if (value instanceof Iterable) {
                // 支持从集合转换为数组
                return convertIterableToArray((Iterable<?>) value, targetType);
            }
        }

        // 处理基本类型和包装类型之间的转换
        try {
            if (targetType.isPrimitive()) {
                // 包装类型转基本类型
                if (targetType == boolean.class && value instanceof Boolean) {
                    return (T) Boolean.valueOf(((Boolean) value).booleanValue());
                } else if (targetType == int.class && value instanceof Number) {
                    return (T) Integer.valueOf(((Number) value).intValue());
                } else if (targetType == long.class && value instanceof Number) {
                    return (T) Long.valueOf(((Number) value).longValue());
                } else if (targetType == double.class && value instanceof Number) {
                    return (T) Double.valueOf(((Number) value).doubleValue());
                } else if (targetType == float.class && value instanceof Number) {
                    return (T) Float.valueOf(((Number) value).floatValue());
                } else if (targetType == char.class && value instanceof Character) {
                    return (T) Character.valueOf(((Character) value).charValue());
                } else if (targetType == byte.class && value instanceof Number) {
                    return (T) Byte.valueOf(((Number) value).byteValue());
                } else if (targetType == short.class && value instanceof Number) {
                    return (T) Short.valueOf(((Number) value).shortValue());
                }
            } else {
                // 基本类型转包装类型或其他类型转换
                if (targetType == Boolean.class && value instanceof Boolean) {
                    return (T) value;
                } else if (Number.class.isAssignableFrom(targetType) && value instanceof Number) {
                    if (targetType == Integer.class) {
                        return (T) Integer.valueOf(((Number) value).intValue());
                    } else if (targetType == Long.class) {
                        return (T) Long.valueOf(((Number) value).longValue());
                    } else if (targetType == Double.class) {
                        return (T) Double.valueOf(((Number) value).doubleValue());
                    } else if (targetType == Float.class) {
                        return (T) Float.valueOf(((Number) value).floatValue());
                    } else if (targetType == Byte.class) {
                        return (T) Byte.valueOf(((Number) value).byteValue());
                    } else if (targetType == Short.class) {
                        return (T) Short.valueOf(((Number) value).shortValue());
                    }
                } else if (targetType == Character.class && value instanceof Character) {
                    return (T) value;
                }
                // 处理日期时间类型的转换
                else if (isDateTimeType(targetType)) {
                    return convertToDateTime(value, targetType);
                }
                // 处理复杂对象类型的递归转换
                else if (!targetType.isEnum() && !targetType.isArray() && !targetType.isPrimitive() && !targetType.getPackage().getName().startsWith("java.lang")) {
                    // 尝试创建目标对象并递归复制属性
                    try {
                        T targetInstance = targetType.getDeclaredConstructor().newInstance();
                        return copy(value, targetInstance);
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("无法创建目标对象实例: {}", targetType.getName(), e);
                        }
                        // 转换失败时返回null，而不是原始值
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            // 转换失败时记录日志
            if (log.isDebugEnabled()) {
                log.debug("类型转换失败: 值={}, 目标类型={}", value, targetType.getSimpleName(), e);
            }
            //转换失败时返回null，而不是原始值
            return null;
        }

        // 无法转换时返回null，而不是原始值，避免类型不兼容的问题
        return null;
    }

    /**
     * 从可迭代对象转换为数组
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertIterableToArray(Iterable<?> iterable, Class<T> targetArrayType) {
        try {
            Class<?> targetComponentType = targetArrayType.getComponentType();

            // 先将Iterable转换为List以便获取长度
            List<?> list = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());

            int length = list.size();
            // 创建目标数组
            Object targetArray = java.lang.reflect.Array.newInstance(targetComponentType, length);

            // 遍历源集合，转换每个元素
            int i = 0;
            for (Object sourceElement : list) {
                // 递归转换每个元素
                Object targetElement = convertValue(sourceElement, targetComponentType);
                if (targetElement != null) {
                    java.lang.reflect.Array.set(targetArray, i, targetElement);
                }
                i++;
            }

            return (T) targetArray;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("集合转换为数组失败: 目标数组类型={}", targetArrayType.getSimpleName(), e);
            }
        }
        return null;
    }

    /**
     * 判断是否为日期时间类型
     */
    private static boolean isDateTimeType(Class<?> type) {
        return java.util.Date.class.isAssignableFrom(type) || java.time.temporal.TemporalAccessor.class.isAssignableFrom(type) || java.time.Instant.class.isAssignableFrom(type);
    }

    /**
     * 将字符串转换为指定类型
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertFromString(String value, Class<T> targetType) {
        // 修复：空字符串处理
        if (value == null || value.trim().isEmpty()) {
            if (targetType.isPrimitive()) {
                // 基本类型不能为null，返回默认值
                return getDefaultPrimitiveValue(targetType);
            }
            return null;
        }

        try {
            // 处理布尔类型
            if (targetType == Boolean.class || targetType == boolean.class) {
                // 增强：支持更多的布尔值表示
                String lowerValue = value.trim().toLowerCase();
                return (T) Boolean.valueOf("true".equals(lowerValue) || "yes".equals(lowerValue) || "y".equals(lowerValue) || "1".equals(lowerValue));
            }

            // 处理数字类型
            if (Number.class.isAssignableFrom(targetType) || targetType.isPrimitive()) {
                // 处理数字字符串中的千位分隔符
                String numericValue = value.trim().replaceAll(",", "");

                if (targetType == Integer.class || targetType == int.class) {
                    return (T) Integer.valueOf(numericValue);
                } else if (targetType == Long.class || targetType == long.class) {
                    return (T) Long.valueOf(numericValue);
                } else if (targetType == Double.class || targetType == double.class) {
                    return (T) Double.valueOf(numericValue);
                } else if (targetType == Float.class || targetType == float.class) {
                    return (T) Float.valueOf(numericValue);
                } else if (targetType == Byte.class || targetType == byte.class) {
                    return (T) Byte.valueOf(numericValue);
                } else if (targetType == Short.class || targetType == short.class) {
                    return (T) Short.valueOf(numericValue);
                }
            }

            // 处理字符类型
            if (targetType == Character.class || targetType == char.class) {
                return (T) Character.valueOf(value.trim().length() > 0 ? value.trim().charAt(0) : '\0');
            }

            // 处理日期时间类型
            if (isDateTimeType(targetType)) {
                return convertToDateTime(value, targetType);
            }

            // 处理枚举类型
            if (targetType.isEnum()) {
                return convertToEnum(value, targetType);
            }

            // 处理数组类型
            if (targetType.isArray()) {
                // 解析逗号分隔的字符串为数组
                String[] elements = value.split(",");
                Class<?> componentType = targetType.getComponentType();

                if (componentType == String.class) {
                    // 修复：对每个元素进行trim处理
                    return (T) Arrays.stream(elements).map(String::trim).toArray(String[]::new);
                } else if (componentType.isPrimitive() || Number.class.isAssignableFrom(componentType)) {
                    // 根据组件类型创建对应数组
                    return (T) createPrimitiveArray(elements, componentType);
                }
            }

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("字符串转换失败: 字符串={}, 目标类型={}", value, targetType.getSimpleName(), e);
            }
        }
        return null;
    }

    /**
     * 获取基本类型的默认值
     */
    @SuppressWarnings("unchecked")
    private static <T> T getDefaultPrimitiveValue(Class<?> primitiveType) {
        if (primitiveType == boolean.class) {
            return (T) Boolean.FALSE;
        } else if (primitiveType == char.class) {
            return (T) Character.valueOf('\0');
        } else if (primitiveType.isPrimitive()) {
            // 其他基本类型默认值都是0
            return (T) Integer.valueOf(0);
        }
        return null;
    }

    /**
     * 转换为日期时间类型
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertToDateTime(Object value, Class<T> targetType) {
        try {
            // 简化处理，实际项目中可能需要更复杂的日期格式化和解析
            if (value instanceof String) {
                String strValue = (String) value;
                // 支持更多日期格式的解析
                try {
                    if (java.util.Date.class.isAssignableFrom(targetType)) {
                        return (T) new java.util.Date(java.sql.Timestamp.valueOf(strValue).getTime());
                    } else if (targetType == java.time.LocalDateTime.class) {
                        return (T) java.time.LocalDateTime.parse(strValue);
                    } else if (targetType == java.time.LocalDate.class) {
                        return (T) java.time.LocalDate.parse(strValue);
                    } else if (targetType == java.time.LocalTime.class) {
                        return (T) java.time.LocalTime.parse(strValue);
                    } else if (targetType == java.time.Instant.class) {
                        return (T) java.time.Instant.parse(strValue);
                    }
                } catch (DateTimeParseException e) {
                    // 如果标准格式解析失败，尝试使用毫秒时间戳解析
                    try {
                        long timestamp = Long.parseLong(strValue);
                        if (java.util.Date.class.isAssignableFrom(targetType)) {
                            return (T) new java.util.Date(timestamp);
                        } else if (targetType == java.time.LocalDateTime.class) {
return (T) java.time.LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
                        } else if (targetType == java.time.Instant.class) {
                            return (T) java.time.Instant.ofEpochMilli(timestamp);
                        }
                    } catch (NumberFormatException ex) {
                        // 两种解析方式都失败，记录日志
                        if (log.isDebugEnabled()) {
                            log.debug("日期格式不支持: {}", strValue);
                        }
                    }
                }
            } else if (value instanceof java.util.Date) {
                java.util.Date dateValue = (java.util.Date) value;
                if (targetType == java.time.LocalDateTime.class) {
                    return (T) java.time.LocalDateTime.ofInstant(dateValue.toInstant(), java.time.ZoneId.systemDefault());
                } else if (targetType == java.time.LocalDate.class) {
                    return (T) java.time.LocalDate.ofInstant(dateValue.toInstant(), java.time.ZoneId.systemDefault());
                } else if (targetType == java.time.Instant.class) {
                    return (T) dateValue.toInstant();
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("日期时间转换失败: 值={}, 目标类型={}", value, targetType.getSimpleName(), e);
            }
        }
        return null;
    }

    /**
     * 转换为枚举类型
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertToEnum(Object value, Class<T> enumType) {
        try {
            if (value instanceof String) {
                String enumName = ((String) value).trim();
                if (enumName.isEmpty()) {
                    return null;
                }

                // 首先尝试使用标准的 Enum.valueOf 方法（区分大小写）
                try {
                    return (T) Enum.valueOf((Class<Enum>) enumType, enumName);
                } catch (IllegalArgumentException e) {
                    // 如果严格匹配失败，尝试忽略大小写查找枚举值
                    for (Enum<?> enumConstant : ((Class<Enum>) enumType).getEnumConstants()) {
                        if (enumConstant.name().equalsIgnoreCase(enumName)) {
                            return (T) enumConstant;
                        }
                    }
                    // 找不到对应枚举值，记录日志
                    if (log.isDebugEnabled()) {
                        log.debug("未找到枚举值: {}", enumName);
                    }
                }
            } else if (value instanceof Number) {
                // 尝试通过序数查找枚举值
                Enum<?>[] enumConstants = ((Class<Enum>) enumType).getEnumConstants();
                int ordinal = ((Number) value).intValue();
                if (ordinal >= 0 && ordinal < enumConstants.length) {
                    return (T) enumConstants[ordinal];
                }
                if (log.isDebugEnabled()) {
                    log.debug("枚举序数超出范围: {}", ordinal);
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("枚举转换失败: 值={}, 目标枚举类型={}", value, enumType.getSimpleName(), e);
            }
        }
        return null;
    }

    /**
     * 转换数组类型
     */
    @SuppressWarnings("unchecked")
    private static <T> T convertArray(Object sourceArray, Class<T> targetArrayType) {
        try {
            Class<?> targetComponentType = targetArrayType.getComponentType();
            Class<?> sourceComponentType = sourceArray.getClass().getComponentType();

            // 获取源数组的长度
            int length = java.lang.reflect.Array.getLength(sourceArray);
// 创建目标数组
            Object targetArray = java.lang.reflect.Array.newInstance(targetComponentType, length);

            // 遍历源数组，转换每个元素
            for (int i = 0; i < length; i++) {
                Object sourceElement = java.lang.reflect.Array.get(sourceArray, i);
                // 递归转换每个元素
                Object targetElement = convertValue(sourceElement, targetComponentType);
                if (targetElement != null) {
                    java.lang.reflect.Array.set(targetArray, i, targetElement);
                } else {
                    // 如果元素转换失败，尝试设置默认值
                    if (targetComponentType.isPrimitive()) {
                        java.lang.reflect.Array.set(targetArray, i, getDefaultPrimitiveValue(targetComponentType));
                    }
                }
            }

            return (T) targetArray;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("数组转换失败: 源数组类型={}, 目标数组类型={}", sourceArray.getClass().getSimpleName(), targetArrayType.getSimpleName(), e);
            }
        }
        return null;
    }

    /**
     * 创建基本类型数组
     */
    private static Object createPrimitiveArray(String[] elements, Class<?> componentType) {
        try {
            Object array = java.lang.reflect.Array.newInstance(componentType, elements.length);

            for (int i = 0; i < elements.length; i++) {
                String element = elements[i].trim();
                // 处理空字符串元素
                if (element.isEmpty()) {
                    if (componentType.isPrimitive()) {
                        java.lang.reflect.Array.set(array, i, getDefaultPrimitiveValue(componentType));
                    }
                    continue;
                }

                Object convertedValue = convertFromString(element, componentType);
                if (convertedValue != null) {
                    java.lang.reflect.Array.set(array, i, convertedValue);
                } else if (componentType.isPrimitive()) {
                    // 转换失败时设置默认值
                    java.lang.reflect.Array.set(array, i, getDefaultPrimitiveValue(componentType));
                }
            }

            return array;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("创建基本类型数组失败: 组件类型={}", componentType.getSimpleName(), e);
            }
        }
        return null;
    }


    /**
     * 将Java对象序列化为二进制数据
     *
     * @param obj 需要序列化的的对象
     * @return 二进制数据
     */
    public static byte[] objectToByte(Object obj) {
        byte[] bytes = null;
        //@formatter:off
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
             ObjectOutputStream oo = new ObjectOutputStream(bo);) {
            // object to bytearray
            oo.writeObject(obj);
            bytes = bo.toByteArray();
        } catch (Exception e) {
            throw new UncheckedException(e.getMessage());
        }
        //@formatter:on
        return bytes;
    }

    /**
     * 将序列化化后的二进制数据反序列化为对象
     *
     * @param bytes 序列化化后的二进制数据
     * @return 对象
     */
    public static Object byteToObject(byte[] bytes) {
        Object obj = null;
        //@formatter:off
        try (ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
             ObjectInputStream oi = new ObjectInputStream(bi);) {
            // bytearray to object
            obj = oi.readObject();
        } catch (Exception e) {
            throw new UncheckedException(e.getMessage());
        }
        //@formatter:on
        return obj;
    }

    /**
     * 将序列化化后的二进制数据反序列化为对象
     *
     * @param <T>   目标对象的类型
     * @param bytes 原始的二进制数据
     * @param clazz 目标对象
     * @return 反序列化之后的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T byteToObject(byte[] bytes, Class<T> clazz) {
        T t = null;
        //@formatter:off
        try ( ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
              ObjectInputStream oi = new ObjectInputStream(bi);){
            // bytearray to object
            t = (T) oi.readObject();
        } catch (Exception e) {
            throw new UncheckedException(e.getMessage());
        }
        //@formatter:on
        return t;
    }

    /**
     * 将Map转成指定的JavaBean对象
     *
     * @param <T>   目标对象的类型
     * @param map   原始的map对象数据
     * @param clazz 目标对象的Class类型
     * @return 转换后的java对象，如果输入map为null则返回null
     */
    @SuppressWarnings({"rawtypes"})
    public static <T> T mapToBean(Map map, Class<T> clazz) {
        if (null == map) {
            return null;
        }
        ObjectMapper mapper = JsonUtil.mapper();
        try {
            // 使用Jackson的convertValue方法进行转换
            return mapper.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            // 如果转换失败，尝试使用备用方法
            return (T) mapToBeanWithFallback(mapper, map, clazz);
        }
    }


    /**
     * 备用转换方法（处理没有默认构造函数的情况）
     *
     * @param mapper ObjectMapper实例，用于JSON序列化和反序列化
     * @param map 包含属性键值对的Map对象
     * @param clazz 目标Bean类的Class对象
     * @return 转换后的Bean实例
     */
    private static <T> T mapToBeanWithFallback(ObjectMapper mapper, Map<String, Object> map, Class<T> clazz) {
        try {
            // 方法1: 通过JSON字符串转换
            String json = mapper.writeValueAsString(map);
            return mapper.readValue(json, clazz);
        } catch (Exception e1) {
            try {
                // 方法2: 尝试使用第一个可用的构造函数
                return tryConstructors(mapper, map, clazz);
            } catch (Exception e2) {
                throw new UncheckedException("无法创建 " + clazz.getName() + " 的实例: " + e1.getMessage() + ", " + e2.getMessage());
            }
        }
    }


    /**
     * 尝试使用构造函数创建实例
     *
     * @param mapper ObjectMapper对象，用于处理JSON映射
     * @param map    包含字段名和对应值的映射表
     * @param clazz  要创建实例的目标类
     * @return 创建的实例对象
     */
    private static <T> T tryConstructors(ObjectMapper mapper, Map<String, Object> map, Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // 按参数数量排序，优先尝试参数少的构造函数
        Arrays.sort(constructors, (c1, c2) -> Integer.compare(c1.getParameterCount(), c2.getParameterCount()));

        // 遍历所有声明的构造函数
        for (Constructor<?> constructor : constructors) {
            try {
                constructor.setAccessible(true);
                
                // 对于无参构造函数
                if (constructor.getParameterCount() == 0) {
                    T instance = (T) constructor.newInstance();
                    // 使用反射设置字段值
                    setFieldsFromMap(mapper, instance, map);
                    return instance;
                }
                // 对于带参数的构造函数
                else {
                    // 获取构造函数参数
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Parameter[] parameters = constructor.getParameters();
                    Object[] args = new Object[paramTypes.length];
                    
                    // 尝试从map中获取参数值
                    boolean allParamsFound = true;
                    for (int i = 0; i < parameters.length; i++) {
                        String paramName = parameters[i].getName();
                        Object value = map.get(paramName);
                        
                        if (value == null) {
                            // 如果参数名在map中不存在，尝试使用字段名（去掉set/get前缀）
                            String fieldName = paramName;
                            if (paramName.startsWith("arg")) {
                                // 对于编译生成的参数名（如arg0, arg1），尝试使用字段名
                                List<Field> fields = ClassUtil.fields(clazz);
                                if (i < fields.size()) {
                                    fieldName = fields.get(i).getName();
                                    value = map.get(fieldName);
                                }
                            } else {
                                value = map.get(fieldName);
                            }
                        }
                        
                        if (value != null) {
                            // 类型转换
                            if (!paramTypes[i].isAssignableFrom(value.getClass())) {
                                value = mapper.convertValue(value, paramTypes[i]);
                            }
                            args[i] = value;
                        } else {
                            allParamsFound = false;
                            break;
                        }
                    }
                    
                    if (allParamsFound) {
                        return (T) constructor.newInstance(args);
                    }
                }
            } catch (Exception e) {
                // 继续尝试下一个构造函数
                if (log.isDebugEnabled()) {
                    log.debug("尝试构造函数失败: {}", constructor, e);
                }
            }
        }
        throw new UncheckedException("没有找到可用的构造函数");
    }


    /**
     * 使用反射设置字段值
     * 该方法通过反射机制，将Map中的键值对映射到目标对象的对应字段上。
     * 支持自动类型转换，当Map中的值类型与目标字段类型不匹配时，
     * 会使用ObjectMapper进行类型转换。
     *
     * @param mapper   ObjectMapper实例，用于类型转换
     * @param instance 目标对象实例，需要被设置字段值的对象
     * @param map      包含字段名和对应值的映射表
     * @throws Exception 当反射操作或类型转换过程中发生错误时抛出异常
     */
    private static <T> void setFieldsFromMap(ObjectMapper mapper, T instance, Map<String, Object> map) throws Exception {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                // 获取目标字段并设置可访问性
                java.lang.reflect.Field field = instance.getClass().getDeclaredField(entry.getKey());
                field.setAccessible(true);

                Object value = entry.getValue();
                // 类型转换处理
                if (value != null && !field.getType().isAssignableFrom(value.getClass())) {
                    value = mapper.convertValue(value, field.getType());
                }

                field.set(instance, value);
            } catch (NoSuchFieldException e) {
                // 忽略不存在的字段
            }
        }
    }


    /**
     * 将java对象转换为Map
     *
     * @param data java对象
     * @return 转换后的map数据
     */
    @SuppressWarnings("rawtypes")
    public static Map beanToMap(Object data) {
        if (null == data) {
            return Collections.EMPTY_MAP;
        }
        try {
            return JsonUtil.mapper().convertValue(data, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            log.warn("Bean转Map失败: ", e);
        }
        return Collections.EMPTY_MAP;
    }

    /**
     * 对象深克隆
     *
     * @param val 待克隆的对象
     * @return 克隆后的对象
     */
    public static Object cloneVal(Object val) {
        if (null == val) {
            return null;
        }
        //@formatter:off
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(bos)) {
        // 将原始对象写入字节数组
        oos.writeObject(val);
        // 获取字节数组
        byte[] byteArr = bos.toByteArray();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArr);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            // 从字节数组中读取对象
            return ois.readObject();
        }
    } catch (Exception e) {
        if (log.isInfoEnabled()) {
            log.info("克隆对象时出现问题，对象类型：{}", val.getClass().getName(), e);
        }
        throw new UncheckedException(e.getMessage());
    }
    //@formatter:on
    }


    /**
     * 使用jackson的方式实现深克隆
     *
     * @param val 待克隆的的对象
     * @return 克隆后的对象
     */
    public static Object deepClone(Object val) {
        return JsonUtil.deepClone(val);
    }

}