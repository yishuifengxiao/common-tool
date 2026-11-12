package com.yishuifengxiao.common.tool;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yishuifengxiao.common.tool.bean.JsonUtil;
import com.yishuifengxiao.common.tool.datetime.DateTimeUtil;
import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.http.UrlUtil;
import com.yishuifengxiao.common.tool.lang.HexBitset;
import com.yishuifengxiao.common.tool.lang.TLV;
import com.yishuifengxiao.common.tool.text.TextExtract;
import com.yishuifengxiao.common.tool.utils.ValidateUtils;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第八轮修复回归测试，覆盖 URL 相对路径解析、正文抽取尾部越界、TLV 长度字段、
 * 空 BIT STRING、校验工具、JSON 时区统一与时间格式化 Locale。
 */
class Round8FixRegressionTest {

    // ==================== UrlUtil.urlComplete ====================

    private static final String REFERRER = "http://a.com/b/c/d.html";

    @Test
    void urlCompleteResolvesParentDirectory() {
        assertEquals("http://a.com/b/x", UrlUtil.urlComplete(REFERRER, "../x"));
    }

    @Test
    void urlCompleteResolvesTwoParentDirectories() {
        assertEquals("http://a.com/x", UrlUtil.urlComplete(REFERRER, "../../x"));
    }

    @Test
    void urlCompleteResolvesRootRelative() {
        assertEquals("http://a.com/x", UrlUtil.urlComplete(REFERRER, "/x"));
    }

    @Test
    void urlCompleteResolvesSiblingRelative() {
        assertEquals("http://a.com/b/c/x/y", UrlUtil.urlComplete(REFERRER, "x/y"));
    }

    @Test
    void urlCompleteResolvesProtocolRelative() {
        assertEquals("http://cdn.com/a.js", UrlUtil.urlComplete(REFERRER, "//cdn.com/a.js"));
    }

    @Test
    void urlCompleteKeepsAbsoluteUrl() {
        assertEquals("https://x.com/y", UrlUtil.urlComplete(REFERRER, "https://x.com/y"));
    }

    @Test
    void urlCompleteRejectsInvalidReferrer() {
        assertNull(UrlUtil.urlComplete("not-a-url", "/x"));
    }

    @Test
    void urlCompleteRejectsBlank() {
        assertNull(UrlUtil.urlComplete(null, "/x"));
        assertNull(UrlUtil.urlComplete(REFERRER, " "));
    }

    // ==================== TextExtract 尾部越界 ====================

    @Test
    void textExtractShouldNotFailAtTail() {
        StringBuilder html = new StringBuilder("<html><body><div>标题</div>");
        for (int i = 0; i < 30; i++) {
            html.append("<p>第").append(i)
                    .append("段正文内容，包含足够多的文字用于触发正文块识别逻辑，")
                    .append("并验证抽取过程在列表尾部不会发生下标越界异常。</p>");
        }
        html.append("</body></html>");
        // 若发生下标越界会被 extract 捕获并返回 null，此处要求正常返回
        assertNotNull(new TextExtract().extract(html.toString()));
    }

    // ==================== TLV 长度字段 ====================

    @Test
    void toLvShortLength() {
        assertEquals("7F" + "AB".repeat(127), TLV.toLV("AB".repeat(127)));
    }

    @Test
    void toLvOneByteLongLength() {
        String lv = TLV.toLV("AB".repeat(128));
        assertTrue(lv.startsWith("8180"), lv.substring(0, 4));
    }

    @Test
    void toLvTwoByteLongLength() {
        String lv = TLV.toLV("AB".repeat(256));
        assertTrue(lv.startsWith("820100"), lv.substring(0, 6));
    }

    @Test
    void toLvEmptyValue() {
        assertEquals("00", TLV.toLV(""));
    }

    // ==================== HexBitset 空 BIT STRING ====================

    @Test
    void bitStringToHexEmptyReturnsUnusedBitsOnly() {
        assertEquals("00", HexBitset.bitStringToHex(""));
    }

    @Test
    void bitStringToHexKeepsNormalBehaviour() {
        assertEquals("0780", HexBitset.bitStringToHex("10000000"));
    }

    // ==================== ValidateUtils ====================

    @Test
    void isTrueOrNullThrowsWhenFalseWithMessage() {
        assertThrows(UncheckedException.class, () -> ValidateUtils.isTrueOrNull(false, "必须为true"));
    }

    @Test
    void isTrueOrNullAcceptsNull() {
        assertDoesNotThrow(() -> ValidateUtils.isTrueOrNull(null, "必须为true"));
    }

    // ==================== JSON 时区统一为 JVM 默认时区 ====================

    @Data
    public static class TimeBean {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private Date time;
    }

    @Test
    void jsonUtilUsesConfiguredTimeZone() {
        Date date = new Date(1_700_000_000_000L);
        TimeBean bean = new TimeBean();
        bean.setTime(date);

        // Date 序列化应使用 ObjectMapper 配置的时区（构造时快照的 JVM 默认时区）
        TimeZone mapperTimeZone = JsonUtil.mapper().serializationConfig().getTimeZone();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        formatter.setTimeZone(mapperTimeZone);
        String expected = formatter.format(date);

        String json = JsonUtil.toJSONString(bean);
        assertTrue(json.contains(expected), "json=" + json + ", tz=" + mapperTimeZone.getID());
    }

    @Test
    void jsonUtilMappersShareSameTimeZone() {
        // 默认 mapper 与忽略 null 的 mapper 必须输出一致的日期时间
        Date date = new Date(1_700_000_000_000L);
        TimeBean bean = new TimeBean();
        bean.setTime(date);

        String withNull = JsonUtil.toJSONString(bean);
        String noneNull = JsonUtil.toJSONString(false, bean);
        assertEquals(withNull, noneNull);
    }

    // ==================== DateTimeUtil Locale ====================

    @Test
    void dateTimeFormatUsesExplicitLocale() {
        Date date = new Date(1_700_000_000_000L);
        String expected = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date);
        assertEquals(expected, DateTimeUtil.format(date, "yyyy-MM-dd HH:mm:ss"));
    }
}
