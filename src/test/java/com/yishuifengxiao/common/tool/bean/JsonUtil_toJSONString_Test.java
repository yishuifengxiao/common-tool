package com.yishuifengxiao.common.tool.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@Slf4j
public class JsonUtil_toJSONString_Test {

    private static ObjectMapper default_mapper;
    private static ObjectMapper none_null_mapper;

    @BeforeClass
    public static void setUpClass() {
        default_mapper = JsonMapper.builder()
                .changeDefaultVisibility(vc -> vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .defaultTimeZone(TimeZone.getDefault())
                .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES, true)
                .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.ALWAYS))
                .build();

        none_null_mapper = JsonMapper.builder()
                .changeDefaultVisibility(vc -> vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .defaultTimeZone(TimeZone.getDefault())
                .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES, true)
                .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }

    /**
     * 测试正常场景：包含空值的对象转换为JSON字符串
     * 预期结果：返回包含空值的JSON字符串
     */
    @Test
    public void testToJSONString_IncludeNull() {
        TestObject testObj = new TestObject("test", null);
        String result = JsonUtil.toJSONString(true, testObj);
        assertEquals("{\"field1\":\"test\",\"field2\":null}", result);
    }

    /**
     * 测试正常场景：不包含空值的对象转换为JSON字符串
     * 预期结果：返回不包含空值的JSON字符串
     */
    @Test
    public void testToJSONString_ExcludeNull() {
        TestObject testObj = new TestObject("test", null);
        String result = JsonUtil.toJSONString(false, testObj);
        assertEquals("{\"field1\":\"test\"}", result);
    }

    /**
     * 测试边界场景：输入为null值
     * 预期结果：返回null
     */
    @Test
    public void testToJSONString_NullInput() {
        String result = JsonUtil.toJSONString(true, null);
        assertNull(result);
    }

    /**
     * 测试异常场景：JSON序列化失败
     * 预期结果：返回null并记录错误日志
     * <p>
     * default_mapper 已改为静态初始化且为 final，无法再通过置空或替换实例来模拟失败，
     * 改为用自引用对象触发 Jackson 的无限递归异常。
     * </p>
     */
    @Test
    public void testToJSONString_JsonProcessingException() {
        assertNull(JsonUtil.toJSONString(true, new SelfReferenceObject()));
    }

    /**
     * 自引用对象，序列化时会触发无限递归异常
     */
    private static class SelfReferenceObject {
        private final SelfReferenceObject self;

        SelfReferenceObject() {
            this.self = this;
        }

        public SelfReferenceObject getSelf() {
            return self;
        }
    }

    private static class TestObject {
        private String field1;
        private String field2;

        public TestObject(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        public String getField1() {
            return field1;
        }

        public String getField2() {
            return field2;
        }
    }
}
