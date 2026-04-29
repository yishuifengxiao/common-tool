package com.yishuifengxiao.common.tool.bean;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * <p>JSON转换与提取工具类</p>
 * <p>基于Jackson实现JSON数据的序列化与反序列化，并集成JSONPath支持复杂数据提取。</p>
 * <p>核心功能：</p>
 * <ul>
 * <li>JSON字符串与Java对象的相互转换</li>
 * <li>JSON字符串与List集合的相互转换</li>
 * <li>基于JSONPath表达式的数据提取</li>
 * <li>JSON格式验证（对象/数组判断）</li>
 * <li>对象深克隆（基于JSON序列化）</li>
 * </ul>
 *
 * <p><strong>注意</strong>：内部使用Jackson进行数据转换，需遵循Jackson的注解规范。</p>
 *
 * <p><strong>JSONPath语法说明：</strong></p>
 * <ul>
 * <li><code>$</code> - 根元素</li>
 * <li><code>@</code> - 当前元素</li>
 * <li><code>. or []</code> - 子元素</li>
 * <li><code>..</code> - 递归下降</li>
 * <li><code>*</code> - 通配符</li>
 * <li><code>[start:end:step]</code> - 数组切片</li>
 * <li><code>?()</code> - 过滤表达式</li>
 * </ul>
 *
 * <p><strong>索引说明</strong>：JSONPath数组索引从0开始，与Java数组保持一致。</p>
 *
 * @author qingteng
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public final class JsonUtil {

    /**
     * 默认ObjectMapper实例，配置了宽松的解析规则
     */
    static ObjectMapper default_mapper = new ObjectMapper();

    /**
     * 带类型信息的ObjectMapper，序列化时会添加@class属性
     */
    private static ObjectMapper with_class_mapper = null;

    /**
     * 忽略null值的ObjectMapper
     */
    private static ObjectMapper none_null_mapper = null;


    static {
        try {
            default_mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            default_mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            default_mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            default_mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
            default_mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
            default_mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            default_mapper.registerModule(new JavaTimeModule());
            default_mapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
            default_mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            default_mapper.configure(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, true);
            default_mapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
            default_mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            default_mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            default_mapper.configure(MapperFeature.USE_GETTERS_AS_SETTERS, false);
            default_mapper.configure(MapperFeature.INFER_PROPERTY_MUTATORS, false);

            with_class_mapper = default_mapper.copy();
            with_class_mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL,
                    JsonTypeInfo.As.PROPERTY);

            none_null_mapper = default_mapper.copy();
            none_null_mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            none_null_mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        } catch (Exception e) {
            log.warn("There was a problem initializing the Object Mapper, problem {}", e);
        }

    }

    /**
     * 获取项目中使用的ObjectMapper实例副本
     *
     * @return ObjectMapper实例副本，可安全地进行自定义配置
     */
    public static ObjectMapper mapper() {
        return default_mapper.copy();
    }

    /**
     * 将JSON字符串转换为指定类型的Java对象
     *
     * @param <T>   目标对象类型
     * @param json  JSON格式的字符串
     * @param clazz 目标对象的Class类型
     * @return 转换后的Java对象，若输入为空则返回null
     */
    public static <T> T strToBean(String json, Class<T> clazz) {
        return strToBean(json, clazz, true);
    }

    /**
     * 将JSON字符串转换为指定类型的Java对象
     *
     * @param <T>                     目标对象类型
     * @param json                    待转换的JSON字符串
     * @param clazz                   目标Java对象的Class类型
     * @param failOnUnknownProperties 是否在遇到未知属性时抛出异常
     * @return 转换后的Java对象，转换失败时返回null
     */
    public static <T> T strToBean(String json, Class<T> clazz, boolean failOnUnknownProperties) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            String trimmedJson = json.trim();
            ObjectMapper tempMapper = default_mapper.copy();
            tempMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);
            return tempMapper.readValue(trimmedJson, clazz);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert JSON string to Java object: clazz={}, error={}",
                    clazz.getSimpleName(), e.getMessage(), e);
        } catch (Exception e) {
            log.warn("Unexpected error when converting JSON string to Java object: clazz={}, error={}",
                    clazz.getSimpleName(), e.getMessage(), e);
        }
        return null;
    }


    /**
     * 将JSON字符串转换为指定类型的List集合
     *
     * @param <T>   集合元素类型
     * @param json  JSON格式的字符串
     * @param clazz 集合元素的Class类型
     * @return 转换后的List集合，转换失败时返回空列表
     */
    public static <T> List<T> strToList(String json, Class<T> clazz) {
        return strToList(json, clazz, true);
    }

    /**
     * 将JSON字符串转换为指定类型的List集合
     *
     * @param <T>                     集合元素类型
     * @param json                    待转换的JSON字符串
     * @param clazz                   List中元素的类型Class对象
     * @param failOnUnknownProperties 反序列化时遇到未知属性是否抛出异常
     * @return 转换后的List集合，转换失败时返回空列表
     */
    public static <T> List<T> strToList(String json, Class<T> clazz, boolean failOnUnknownProperties) {
        if (json == null || clazz == null) {
            return Collections.emptyList();
        }

        try {
            String trimmedJson = json.trim();
            ObjectMapper tempMapper = default_mapper.copy();
            tempMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownProperties);

            JavaType javaType = tempMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return tempMapper.readValue(trimmedJson, javaType);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("There was a problem converting the string {} to the {} List, the problem is {}", json, clazz, e.getMessage());
            } else {
                log.warn("There was a problem converting the string {} to the {} List", json, clazz);
            }
            return Collections.emptyList();
        }
    }


    /**
     * 根据JSONPath表达式从JSON字符串中提取数据
     *
     * @param <T>      返回值类型
     * @param json     JSON格式的字符串
     * @param jsonPath JSONPath提取表达式
     * @return 提取的数据，提取失败时返回null
     */
    public static <T> T extract(String json, String jsonPath) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return JsonPath.read(json.trim(), jsonPath);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("There was a problem extracting data from string {} based on expression {}. The problem is {}", jsonPath, json, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 根据JSONPath表达式提取数据并转换为指定类型的Java对象
     *
     * @param <T>      返回值类型
     * @param json     JSON格式的字符串
     * @param jsonPath JSONPath提取表达式
     * @param clazz    目标Java对象的Class类型
     * @return 转换后的Java对象，提取或转换失败时返回null
     * @throws IllegalArgumentException 当参数为空时抛出
     */
    public static <T> T extract(String json, String jsonPath, Class<T> clazz) {
        if (json == null || json.trim().isEmpty() || jsonPath == null || jsonPath.trim().isEmpty() || clazz == null) {
            throw new IllegalArgumentException("参数不能为空: json=" + json + ", jsonPath=" + jsonPath + ", clazz=" + clazz);
        }

        Object data = extract(json, jsonPath);
        if (null == data) {
            return null;
        }

        try {
            String dataStr = default_mapper.writeValueAsString(data);
            if ("{}".equals(dataStr)) {
                return null;
            }
            return strToBean(dataStr, clazz);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize extracted data to JSON: {}", e.getMessage());
            return null;
        }
    }


    /**
     * 根据JSONPath表达式提取数据并转换为指定类型的List集合
     *
     * @param <T>      集合元素类型
     * @param json     JSON格式的字符串
     * @param jsonPath JSONPath提取表达式
     * @param clazz    集合元素的Class类型
     * @return 转换后的List集合，提取或转换失败时返回null
     */
    public static <T> List<T> extractList(String json, String jsonPath, Class<T> clazz) {
        if (json == null || jsonPath == null || clazz == null) {
            return null;
        }

        Object data = extract(json, jsonPath);
        if (null == data) {
            return null;
        }

        try {
            String dataStr = default_mapper.writeValueAsString(data);
            return strToList(dataStr, clazz);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize extracted data to JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * 将JSON字符串转换为Map对象
     *
     * @param text 待转换的JSON字符串
     * @return 转换后的Map对象，转换失败时返回null
     */
    public static Map<String, Object> jsonToMap(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            return null;
        }
        try {
            return default_mapper.readValue(trimmedText, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.warn("There was a problem converting the string {} to a map, the problem is {} ", text, e.getMessage());
            if (log.isDebugEnabled()) {
                log.debug("Detailed exception: ", e);
            }
        } catch (Exception e) {
            log.error("Unexpected error when converting string {} to map", text, e);
        }
        return null;
    }


    /**
     * 判断字符串是否为JSON对象格式
     *
     * @param text 待判断的字符串
     * @return 是JSON对象返回true，否则返回false
     */
    public static boolean isJSONObject(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            JsonNode tree = with_class_mapper.readTree(text.trim());
            return tree != null && tree.isObject();
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 判断字符串是否为JSON数组格式
     *
     * @param text 待判断的字符串
     * @return 是JSON数组返回true，否则返回false
     */
    public static boolean isJSONArray(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }

        String trimmedText = text.trim();
        if (trimmedText.isEmpty()) {
            return false;
        }

        if (!trimmedText.startsWith("[") || !trimmedText.endsWith("]")) {
            return false;
        }

        try {
            JsonNode tree = with_class_mapper.readTree(trimmedText);
            return tree.isArray();
        } catch (JsonProcessingException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * 判断字符串是否为有效的JSON格式
     *
     * @param text 待判断的字符串
     * @return 是有效JSON返回true，否则返回false
     */
    public static boolean isJSON(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        try {
            with_class_mapper.readTree(text.trim());
            return true;
        } catch (JsonProcessingException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 将对象转换为JSON格式的字符串（包含null值）
     *
     * @param value 待转换的对象
     * @return JSON格式的字符串，转换失败时返回null
     */
    public static String toJSONString(Object value) {
        return toJSONString(true, value);
    }


    /**
     * 将对象转换为JSON格式的字符串
     *
     * @param includeNull 是否包含null值字段
     * @param value       待转换的对象
     * @return JSON格式的字符串，转换失败时返回null
     */
    public static String toJSONString(boolean includeNull, Object value) {
        try {
            ObjectMapper mapper = includeNull ? default_mapper : none_null_mapper;
            if (mapper == null) {
                throw new IllegalStateException("ObjectMapper is not properly initialized");
            }
            if (value == null) {
                return null;
            }
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("There was a problem converting data {} to a JSON format string", value, e);
            if (log.isDebugEnabled()) {
                log.debug("There was a problem converting data {} to a JSON format string, the problem is {} ", value, e);
            }
            return null;
        }
    }


    /**
     * 将对象转换为格式化的JSON字符串（包含null值）
     *
     * @param value 待转换的对象
     * @return 格式化的JSON字符串
     */
    public static String prettyPrinter(Object value) {
        return prettyPrinter(true, value);
    }

    /**
     * 将对象转换为格式化的JSON字符串
     *
     * @param includeNull 是否包含null值字段
     * @param value       待转换的对象
     * @return 格式化的JSON字符串
     * @throws UncheckedException 转换失败时抛出
     */
    public static String prettyPrinter(boolean includeNull, Object value) {
        if (null == value) {
            return null;
        }
        try {
            ObjectMapper mapper = includeNull ? default_mapper : none_null_mapper;
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            if (log.isDebugEnabled()) {
                log.debug("There was a problem converting data {} to a JSON format string, the problem is {}", value, e);
            } else {
                log.warn("There was a problem converting data to a JSON format string");
            }
            throw new UncheckedException("Failed to convert object to JSON string", e);
        } catch (Exception e) {
            log.error("Unexpected error occurred while converting data {} to JSON format string", value, e);
            throw new UncheckedException("Unexpected error during JSON conversion", e);
        }
    }


    /**
     * 使用JSON序列化方式实现对象深克隆
     *
     * @param val 待克隆的对象
     * @return 克隆后的对象
     * @throws UncheckedException 克隆失败时抛出
     */
    public static Object deepClone(Object val) {
        if (null == val) {
            return null;
        }
        try {
            String json = with_class_mapper.writeValueAsString(val);
            return with_class_mapper.readValue(json, val.getClass());
        } catch (JsonProcessingException e) {
            throw new UncheckedException("Failed to serialize object during deep clone", e);
        } catch (IOException e) {
            throw new UncheckedException("Failed to deserialize object during deep clone", e);
        }
    }


}