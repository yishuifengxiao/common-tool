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
import tools.jackson.datatype.jsr310.JavaTimeModule;

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
                .addModule(new JavaTimeModule())
                .configure(JsonReadFeature.ALLOW_SINGLE_QUOTES, true)
                .configure(JsonReadFeature.ALLOW_UNQUOTED_PROPERTY_NAMES, true)
                .changeDefaultPropertyInclusion(v -> v.withValueInclusion(JsonInclude.Include.ALWAYS))
                .build();

        none_null_mapper = JsonMapper.builder()
                .changeDefaultVisibility(vc -> vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY))
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .defaultTimeZone(TimeZone.getDefault())
                .addModule(new JavaTimeModule())
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
     * 测试异常场景：ObjectMapper未初始化
     * 预期结果：抛出IllegalStateException
     */
    @Test(expected = IllegalStateException.class)
    public void testToJSONString_MapperNotInitialized() {
        ObjectMapper originalMapper = JsonUtil.default_mapper;
        JsonUtil.default_mapper = null;
        try {
            JsonUtil.toJSONString(true, "test");
        } finally {
            JsonUtil.default_mapper = originalMapper;
        }
    }

    /**
     * 测试异常场景：JSON处理异常
     * 预期结果：返回null并记录错误日志
     */
    @Test
    public void testToJSONString_JsonProcessingException() {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        ObjectMapper originalMapper = JsonUtil.default_mapper;
        JsonUtil.default_mapper = mockMapper;
        try {
            when(mockMapper.writeValueAsString(any())).thenThrow(new JacksonException("test") {
            });
            String result = JsonUtil.toJSONString(true, "test");
            assertNull(result);
        } finally {
            JsonUtil.default_mapper = originalMapper;
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
