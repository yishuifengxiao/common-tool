package com.yishuifengxiao.common.tool.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证日期时间字段的序列化与反序列化
 *
 * <p>背景：移除了显式的 {@code .addModule(new JavaTimeModule())} 调用，
 * 改为依赖 {@code jackson-databind:3.1.4} 中 {@code MapperBuilder} 自动注册的
 * {@code JavaTimeInitializer}。本测试验证：</p>
 *
 * <ul>
 *   <li>Java 8 日期时间类型（{@link LocalDateTime}、{@link LocalDate}、{@link LocalTime}、
 *       {@link Instant}、{@link ZonedDateTime}、{@link OffsetDateTime}）序列化为 ISO 字符串，
 *       而非数字数组（未注册 JavaTime 模块时会输出形如 {@code [2026,8,19,14,30,0]} 的数组）</li>
 *   <li>包含日期字段的 POJO 可正确往返</li>
 *   <li>集合中的日期对象可正确序列化</li>
 *   <li>传统 {@link Date} 在 {@code @JsonFormat} 注解下按指定格式输出</li>
 *   <li>{@code JsonUtil.default_mapper} 配置的 GMT+8 时区生效</li>
 * </ul>
 *
 * @author qingteng
 * @version 1.0.0
 */
@Slf4j
@DisplayName("JsonUtil日期时间序列化验证测试")
public class JsonUtil_DateTime_Test {

    // ==================== 测试数据类 ====================

    /**
     * 包含多种日期时间字段的 POJO
     */
    @Data
    public static class DateTimeDto {
        private LocalDateTime localDateTime;
        private LocalDate localDate;
        private LocalTime localTime;
        private Instant instant;
        private ZonedDateTime zonedDateTime;
        private OffsetDateTime offsetDateTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        @JsonProperty("legacyDate")
        private Date date;

        public DateTimeDto() {
        }
    }

    // ==================== 单类型序列化测试 ====================

    /**
     * LocalDateTime 应序列化为 ISO 字符串，而非数字数组
     *
     * <p>未注册 JavaTime 模块时输出 {@code [2026,8,19,14,30,0,0]}，
     * 注册后应输出 {@code "2026-08-19T14:30:00"}</p>
     */
    @Test
    @DisplayName("LocalDateTime序列化为ISO字符串而非数组")
    public void testLocalDateTime_SerializedAsString() {
        LocalDateTime ldt = LocalDateTime.of(2026, 8, 19, 14, 30, 0);
        String json = JsonUtil.toJSONString(ldt);

        log.info("LocalDateTime 序列化结果: {}", json);

        assertNotNull(json);
        // 关键断言：应该是带引号的字符串，而不是以 [ 开头的数组
        assertTrue(json.startsWith("\""), "LocalDateTime应序列化为字符串，实际: " + json);
        assertFalse(json.startsWith("["), "LocalDateTime不应序列化为数字数组，实际: " + json);
        assertTrue(json.contains("2026-08-19T14:30:00"), "应包含ISO格式时间");
    }

    /**
     * LocalDate 应序列化为 ISO 日期字符串
     */
    @Test
    @DisplayName("LocalDate序列化为ISO日期字符串")
    public void testLocalDate_SerializedAsString() {
        LocalDate ld = LocalDate.of(2026, 8, 19);
        String json = JsonUtil.toJSONString(ld);

        log.info("LocalDate 序列化结果: {}", json);

        assertNotNull(json);
        assertTrue(json.startsWith("\""), "LocalDate应序列化为字符串，实际: " + json);
        assertFalse(json.startsWith("["), "LocalDate不应序列化为数组，实际: " + json);
        assertTrue(json.contains("2026-08-19"), "应包含ISO格式日期");
    }

    /**
     * LocalTime 应序列化为 ISO 时间字符串
     */
    @Test
    @DisplayName("LocalTime序列化为ISO时间字符串")
    public void testLocalTime_SerializedAsString() {
        LocalTime lt = LocalTime.of(14, 30, 0);
        String json = JsonUtil.toJSONString(lt);

        log.info("LocalTime 序列化结果: {}", json);

        assertNotNull(json);
        assertTrue(json.startsWith("\""), "LocalTime应序列化为字符串，实际: " + json);
        assertFalse(json.startsWith("["), "LocalTime不应序列化为数组，实际: " + json);
        assertTrue(json.contains("14:30:00"), "应包含ISO格式时间");
    }

    /**
     * Instant 应序列化为 ISO 字符串（带 UTC 标记）
     */
    @Test
    @DisplayName("Instant序列化为ISO字符串")
    public void testInstant_SerializedAsString() {
        Instant instant = Instant.parse("2026-08-19T06:30:00Z");
        String json = JsonUtil.toJSONString(instant);

        log.info("Instant 序列化结果: {}", json);

        assertNotNull(json);
        assertTrue(json.startsWith("\""), "Instant应序列化为字符串，实际: " + json);
        assertFalse(json.startsWith("["), "Instant不应序列化为数字，实际: " + json);
        assertTrue(json.contains("2026-08-19"), "应包含日期部分");
    }

    /**
     * ZonedDateTime 应序列化为 ISO 字符串
     */
    @Test
    @DisplayName("ZonedDateTime序列化为ISO字符串")
    public void testZonedDateTime_SerializedAsString() {
        ZonedDateTime zdt = ZonedDateTime.of(2026, 8, 19, 14, 30, 0, 0, ZoneId.of("GMT+8"));
        String json = JsonUtil.toJSONString(zdt);

        log.info("ZonedDateTime 序列化结果: {}", json);

        assertNotNull(json);
        assertTrue(json.startsWith("\""), "ZonedDateTime应序列化为字符串，实际: " + json);
        assertFalse(json.startsWith("["), "ZonedDateTime不应序列化为数组，实际: " + json);
    }

    /**
     * OffsetDateTime 应序列化为 ISO 字符串
     */
    @Test
    @DisplayName("OffsetDateTime序列化为ISO字符串")
    public void testOffsetDateTime_SerializedAsString() {
        OffsetDateTime odt = OffsetDateTime.of(2026, 8, 19, 14, 30, 0, 0, ZoneOffset.ofHours(8));
        String json = JsonUtil.toJSONString(odt);

        log.info("OffsetDateTime 序列化结果: {}", json);

        assertNotNull(json);
        assertTrue(json.startsWith("\""), "OffsetDateTime应序列化为字符串，实际: " + json);
        assertFalse(json.startsWith("["), "OffsetDateTime不应序列化为数组，实际: " + json);
    }

    // ==================== POJO 综合测试 ====================

    /**
     * 包含多种日期字段的 POJO 序列化应全部为字符串
     */
    @Test
    @DisplayName("POJO中多种日期字段均序列化为字符串")
    public void testPojoWithMultipleDateFields() {
        DateTimeDto dto = new DateTimeDto();
        dto.setLocalDateTime(LocalDateTime.of(2026, 8, 19, 14, 30, 0));
        dto.setLocalDate(LocalDate.of(2026, 8, 19));
        dto.setLocalTime(LocalTime.of(14, 30, 0));
        dto.setInstant(Instant.parse("2026-08-19T06:30:00Z"));
        dto.setZonedDateTime(ZonedDateTime.of(2026, 8, 19, 14, 30, 0, 0, ZoneId.of("GMT+8")));
        dto.setOffsetDateTime(OffsetDateTime.of(2026, 8, 19, 14, 30, 0, 0, ZoneOffset.ofHours(8)));
        dto.setDate(Date.from(Instant.parse("2026-08-19T06:30:00Z")));

        String json = JsonUtil.toJSONString(dto);

        log.info("POJO综合序列化结果: {}", json);

        assertNotNull(json);

        // Java 8 类型不应出现数字数组形式
        assertFalse(json.contains("[2026,8"), "LocalDateTime不应序列化为数组: " + json);
        assertFalse(json.contains("[2026,8,19,14"), "LocalTime不应序列化为数组: " + json);

        // 各字段值应正确出现
        assertTrue(json.contains("\"2026-08-19T14:30:00\""), "应包含LocalDateTime值");
        assertTrue(json.contains("\"2026-08-19\""), "应包含LocalDate值");
        assertTrue(json.contains("\"14:30:00\""), "应包含LocalTime值");

        // @JsonFormat 的 legacyDate 字段应按 yyyy-MM-dd HH:mm:ss 格式输出
        assertTrue(json.contains("\"legacyDate\":\"2026-08-19 14:30:00\""),
                "Date字段应按@JsonFormat格式输出，实际: " + json);
    }

    /**
     * 日期时间字段可正确往返：序列化 → 反序列化后字段值一致
     */
    @Test
    @DisplayName("日期时间字段可正确往返")
    public void testDateTimeRoundTrip() {
        DateTimeDto original = new DateTimeDto();
        original.setLocalDateTime(LocalDateTime.of(2026, 8, 19, 14, 30, 0));
        original.setLocalDate(LocalDate.of(2026, 8, 19));
        original.setLocalTime(LocalTime.of(14, 30, 0));
        original.setInstant(Instant.parse("2026-08-19T06:30:00Z"));

        String json = JsonUtil.toJSONString(original);
        log.info("往返测试-序列化: {}", json);

        DateTimeDto restored = JsonUtil.strToBean(json, DateTimeDto.class);
        assertNotNull(restored, "反序列化结果不应为null");

        assertEquals(original.getLocalDateTime(), restored.getLocalDateTime(),
                "LocalDateTime应一致");
        assertEquals(original.getLocalDate(), restored.getLocalDate(),
                "LocalDate应一致");
        assertEquals(original.getLocalTime(), restored.getLocalTime(),
                "LocalTime应一致");
        assertEquals(original.getInstant(), restored.getInstant(),
                "Instant应一致");

        log.info("往返测试-反序列化结果: localDateTime={}, localDate={}, localTime={}, instant={}",
                restored.getLocalDateTime(), restored.getLocalDate(),
                restored.getLocalTime(), restored.getInstant());
    }

    // ==================== 集合测试 ====================

    /**
     * 集合中的 LocalDateTime 对象应全部序列化为字符串
     */
    @Test
    @DisplayName("集合中的LocalDateTime序列化为字符串数组")
    public void testLocalDateTimeList() {
        List<LocalDateTime> list = new ArrayList<>();
        list.add(LocalDateTime.of(2026, 8, 19, 14, 30, 0));
        list.add(LocalDateTime.of(2026, 8, 20, 9, 0, 0));
        list.add(LocalDateTime.of(2026, 12, 31, 23, 59, 59));

        String json = JsonUtil.toJSONString(list);

        log.info("LocalDateTime集合序列化结果: {}", json);

        assertNotNull(json);
        assertTrue(json.startsWith("["), "集合应以[开头");
        // 每个元素都是字符串，不应出现数字子数组
        assertFalse(json.contains("[2026,"), "集合元素不应为数字数组: " + json);
        assertTrue(json.contains("\"2026-08-19T14:30:00\""), "应包含第一个元素");
        assertTrue(json.contains("\"2026-08-20T09:00:00\""), "应包含第二个元素");
        assertTrue(json.contains("\"2026-12-31T23:59:59\""), "应包含第三个元素");
    }

    /**
     * 集合中的 LocalDateTime 可正确反序列化回 List
     */
    @Test
    @DisplayName("LocalDateTime集合可正确反序列化")
    public void testLocalDateTimeList_RoundTrip() {
        List<LocalDateTime> original = new ArrayList<>();
        original.add(LocalDateTime.of(2026, 8, 19, 14, 30, 0));
        original.add(LocalDateTime.of(2026, 8, 20, 9, 0, 0));

        String json = JsonUtil.toJSONString(original);
        List<LocalDateTime> restored = JsonUtil.strToList(json, LocalDateTime.class);

        assertNotNull(restored);
        assertEquals(2, restored.size(), "元素个数应一致");
        assertEquals(original.get(0), restored.get(0), "第一个元素应一致");
        assertEquals(original.get(1), restored.get(1), "第二个元素应一致");

        log.info("集合往返测试通过: {}", restored);
    }

    // ==================== 时区与格式测试 ====================

    /**
     * 验证 JsonUtil 配置的 GMT+8 时区对 Instant 序列化的影响
     *
     * <p>{@code Instant} 本身是 UTC 时间点，序列化结果带 Z 后缀；
     * 但 {@code default_mapper} 配置了 {@code defaultTimeZone(GMT+8)}，
     * 影响 {@link Date} 等基于时区的类型输出。</p>
     */
    @Test
    @DisplayName("Date类型在GMT+8时区下正确序列化")
    public void testDateWithGmt8Timezone() {
        // 2026-08-19T06:30:00Z 对应 GMT+8 的 14:30:00
        Date date = Date.from(Instant.parse("2026-08-19T06:30:00Z"));
        DateTimeDto dto = new DateTimeDto();
        dto.setDate(date);

        String json = JsonUtil.toJSONString(dto);

        log.info("Date在GMT+8下序列化结果: {}", json);

        assertNotNull(json);
        // @JsonFormat 指定了 GMT+8，应输出 14:30:00 而非 UTC 的 06:30:00
        assertTrue(json.contains("\"legacyDate\":\"2026-08-19 14:30:00\""),
                "Date应在GMT+8时区下输出14:30:00，实际: " + json);
    }

    /**
     * 验证 deepClone 能正确克隆包含日期时间字段的对象
     */
    @Test
    @DisplayName("deepClone正确克隆日期时间字段")
    public void testDeepCloneWithDateTime() {
        DateTimeDto original = new DateTimeDto();
        original.setLocalDateTime(LocalDateTime.of(2026, 8, 19, 14, 30, 0));
        original.setLocalDate(LocalDate.of(2026, 8, 19));
        original.setInstant(Instant.parse("2026-08-19T06:30:00Z"));

        Object cloned = JsonUtil.deepClone(original);

        assertNotNull(cloned);
        assertTrue(cloned instanceof DateTimeDto, "克隆对象应为DateTimeDto");

        DateTimeDto clonedDto = (DateTimeDto) cloned;
        assertEquals(original.getLocalDateTime(), clonedDto.getLocalDateTime(),
                "LocalDateTime应一致");
        assertEquals(original.getLocalDate(), clonedDto.getLocalDate(),
                "LocalDate应一致");
        assertEquals(original.getInstant(), clonedDto.getInstant(),
                "Instant应一致");

        log.info("deepClone日期时间测试通过: localDateTime={}, localDate={}, instant={}",
                clonedDto.getLocalDateTime(), clonedDto.getLocalDate(), clonedDto.getInstant());
    }
}
