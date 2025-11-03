package com.yishuifengxiao.common.tool.bean;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;

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
        default_mapper = new ObjectMapper();
        default_mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        default_mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        default_mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        default_mapper.setTimeZone(TimeZone.getDefault());
        default_mapper.registerModule(new JavaTimeModule());
        default_mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        default_mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        default_mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        default_mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);

        none_null_mapper = new ObjectMapper();
        none_null_mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        none_null_mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        none_null_mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        none_null_mapper.setTimeZone(TimeZone.getDefault());
        none_null_mapper.registerModule(new JavaTimeModule());
        none_null_mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        none_null_mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        none_null_mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        none_null_mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
            when(mockMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("test") {});
            String result = JsonUtil.toJSONString(true, "test");
            assertNull(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
