package com.yishuifengxiao.common.tool;

import tools.jackson.databind.ObjectMapper;
import com.yishuifengxiao.common.tool.bean.JsonUtil;
import com.yishuifengxiao.common.tool.http.UrlUtil;
import com.yishuifengxiao.common.tool.jdbc.SimpleRowMapper;
import com.yishuifengxiao.common.tool.lang.HexBitset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

/**
 * Round12 全局扫描问题修复回归测试
 * <p>
 * 覆盖 UrlUtil 域名后缀精确匹配与协议大小写归一化、SimpleRowMapper 字符串日期回退与
 * BigDecimal 精度、HexBitset 既有行为兼容、JsonUtil 共享 ObjectMapper 实例。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Round12 扫描修复回归测试")
class Round12ScanFixRegressionTest {

    /**
     * 仅包含日期时间字段的测试实体
     */
    public static class DateTimeEntity {
        LocalDateTime eventTime;
        OffsetDateTime offsetTime;

        public DateTimeEntity() {
        }
    }

    /**
     * 仅包含金额字段的测试实体
     */
    public static class DecimalEntity {
        BigDecimal decimalValue;

        public DecimalEntity() {
        }
    }

    @Mock
    private ResultSet resultSet;

    @Mock
    private ResultSetMetaData metaData;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("UrlUtil.keyword 使用末尾两级标签精确匹配，不再被子串误判")
    void urlKeywordMatchesSuffixExactly() {
        assertEquals("acom", UrlUtil.keyword("http://www.acom.cn/a"));
        assertEquals("mygov", UrlUtil.keyword("https://www.mygov.cn"));
        assertEquals("myedu", UrlUtil.keyword("https://www.myedu.cn/index"));
        assertEquals("yishuifengxiao", UrlUtil.keyword("https://www.yishuifengxiao.com.cn/x"));
        assertEquals("yishuifengxiao", UrlUtil.keyword("www.yishuifengxiao.com"));
        assertNull(UrlUtil.keyword("com.cn"));
        assertNull(UrlUtil.keyword(""));
    }

    @Test
    @DisplayName("UrlUtil.extractProtocol 对入参协议大小写归一化为小写")
    void extractProtocolReturnsLowerCase() {
        assertEquals("http", UrlUtil.extractProtocol("HTTP://Example.com/a"));
        assertEquals("https", UrlUtil.extractProtocol("Https://Example.com/a"));
        assertNull(UrlUtil.extractProtocol("www.example.com"));
        assertNull(UrlUtil.extractProtocol(""));
    }

    @Test
    @DisplayName("SimpleRowMapper 字符串日期回退支持空格分隔格式")
    void mapRowParsesSpaceSeparatedDateTimeString() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn(null);
        when(metaData.getColumnName(1)).thenReturn("event_time");
        when(resultSet.getObject(1)).thenReturn("2025-01-01 12:00:00");
        when(resultSet.getTimestamp("event_time")).thenThrow(new SQLException("unsupported"));
        when(resultSet.getObject("event_time")).thenReturn("2025-01-01 12:00:00");

        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);

        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0, 0), entity.eventTime);
    }

    @Test
    @DisplayName("SimpleRowMapper 字符串日期回退支持 OffsetDateTime")
    void mapRowParsesOffsetDateTimeString() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn(null);
        when(metaData.getColumnName(1)).thenReturn("offset_time");
        when(resultSet.getObject(1)).thenReturn("2025-01-01T12:00:00+08:00");
        when(resultSet.getTimestamp("offset_time")).thenThrow(new SQLException("unsupported"));
        when(resultSet.getObject("offset_time")).thenReturn("2025-01-01T12:00:00+08:00");

        SimpleRowMapper<DateTimeEntity> mapper = new SimpleRowMapper<>(DateTimeEntity.class);
        DateTimeEntity entity = mapper.mapRow(resultSet, 1);

        assertEquals(OffsetDateTime.parse("2025-01-01T12:00:00+08:00"), entity.offsetTime);
    }

    @Test
    @DisplayName("SimpleRowMapper 数值转 BigDecimal 不丢失长整型精度")
    void mapRowKeepsBigDecimalPrecision() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnLabel(1)).thenReturn(null);
        when(metaData.getColumnName(1)).thenReturn("decimal_value");
        when(resultSet.getObject(1)).thenReturn(9007199254740993L);

        SimpleRowMapper<DecimalEntity> mapper = new SimpleRowMapper<>(DecimalEntity.class);
        DecimalEntity entity = mapper.mapRow(resultSet, 1);

        assertEquals(new BigDecimal("9007199254740993"), entity.decimalValue);
    }

    @Test
    @DisplayName("HexBitset.hexToBitString 返回值行为保持兼容")
    void hexToBitStringKeepsPaddedBits() {
        assertEquals("10000000", HexBitset.hexToBitString("0780"));
        assertEquals("", HexBitset.hexToBitString("00"));
    }

    @Test
    @DisplayName("JsonUtil.mapper 返回非空且为只读共享实例")
    void jsonUtilMapperIsShared() {
        ObjectMapper first = JsonUtil.mapper();
        ObjectMapper second = JsonUtil.mapper();

        assertNotNull(first);
        assertSame(first, second);
    }
}
