package com.yishuifengxiao.common.tool.http;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UrlUtilTest {

    /**
     * 测试正常场景：标准查询字符串转换
     * 验证包含多个键值对的查询字符串能正确转换为Map
     */
    @Test
    public void testQueryString2Map_NormalCase() {
        String queryString = "timeOption=0&page=1&pageSize=10&keyPlace=1&sort=dateDesc&qt=*";
        
        Map<String, String> result = UrlUtil.queryString2Map(queryString);
        
        assertEquals(6, result.size());
        assertEquals("0", result.get("timeOption"));
        assertEquals("1", result.get("page"));
        assertEquals("10", result.get("pageSize"));
        assertEquals("1", result.get("keyPlace"));
        assertEquals("dateDesc", result.get("sort"));
        assertEquals("*", result.get("qt"));
    }

    /**
     * 测试边界场景：空值处理
     * 验证当输入为null或空字符串时返回null
     */
    @Test
    public void testQueryString2Map_EmptyInput() {
        assertNull(UrlUtil.queryString2Map(null));
        assertNull(UrlUtil.queryString2Map(""));
        assertNull(UrlUtil.queryString2Map("   "));
    }

    /**
     * 测试边界场景：URL编码处理
     * 验证包含URL编码的键值对能正确解码
     */
    @Test
    public void testQueryString2Map_UrlEncoded() {
        String queryString = "name=%E6%B5%8B%E8%AF%95&value=test%20value";
        
        Map<String, String> result = UrlUtil.queryString2Map(queryString);
        
        assertEquals(2, result.size());
        assertEquals("测试", result.get("name"));
        assertEquals("test value", result.get("value"));
    }

    /**
     * 测试边界场景：空值键值对处理
     * 验证空值键值对和只有键没有值的键值对处理
     */
    @Test
    public void testQueryString2Map_EmptyKeyValue() {
        String queryString = "key1=&=value2&key3&";
        
        Map<String, String> result = UrlUtil.queryString2Map(queryString);
        
        assertEquals(2, result.size());
        assertEquals("", result.get("key1"));
        assertEquals("", result.get("key3"));
    }

    /**
     * 测试边界场景：单个键值对
     * 验证单个键值对能正确转换
     */
    @Test
    public void testQueryString2Map_SinglePair() {
        String queryString = "key=value";
        
        Map<String, String> result = UrlUtil.queryString2Map(queryString);
        
        assertEquals(1, result.size());
        assertEquals("value", result.get("key"));
    }

    /**
     * 测试边界场景：特殊字符处理
     * 验证包含特殊字符的键值对能正确处理
     */
    @Test
    public void testQueryString2Map_SpecialCharacters() {
        // 将特殊字符进行URL编码，避免&字符被误解析为分隔符
        String queryString = "key1=value!%40%23%24%25%5E%26*()&key2=value%2B-%3D_";

        Map<String, String> result = UrlUtil.queryString2Map(queryString);

        assertEquals(2, result.size());
        assertEquals("value!@#$%^&*()", result.get("key1"));
        assertEquals("value+-=_", result.get("key2"));
    }

}
