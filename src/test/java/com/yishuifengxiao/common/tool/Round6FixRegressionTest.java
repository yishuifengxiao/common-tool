package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.asn1.BERReader;
import com.yishuifengxiao.common.tool.asn1.BERWriter;
import com.yishuifengxiao.common.tool.collections.ArrayUtil;
import com.yishuifengxiao.common.tool.collections.CollUtil;
import com.yishuifengxiao.common.tool.datetime.DateOffsetUtil;
import com.yishuifengxiao.common.tool.datetime.LocalDateTimeUtil;
import com.yishuifengxiao.common.tool.entity.Page;
import com.yishuifengxiao.common.tool.entity.Slice;
import com.yishuifengxiao.common.tool.io.ImageUtil;
import com.yishuifengxiao.common.tool.io.IoUtil;
import com.yishuifengxiao.common.tool.random.RandomUtil;
import com.yishuifengxiao.common.tool.text.RegexUtil;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第六轮修复回归测试，覆盖集合泛型、Base64 图片、日末毫秒精度、非法数字提取、ASN.1 编解码等边界场景。
 */
class Round6FixRegressionTest {

    @Test
    void asArrayShouldRejectNullAndKeepTypedArray() {
        assertThrows(IllegalArgumentException.class, () -> CollUtil.asArray((String[]) null));

        String[] values = CollUtil.asArray("a", "b");
        assertArrayEquals(new String[]{"a", "b"}, values);
    }

    @Test
    void base64ToBufferedImageShouldAcceptPngDataUri() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFF0000);

        BufferedImage decoded = ImageUtil.base64ToBufferedImage(ImageUtil.imageToBase64WithPngHeader(image));

        assertNotNull(decoded);
        assertEquals(1, decoded.getWidth());
        assertEquals(1, decoded.getHeight());
        assertEquals(0xFF0000, decoded.getRGB(0, 0) & 0xFFFFFF);
    }

    @Test
    void dayEndShouldKeepMillisecond999() {
        LocalDateTime localDayEnd = LocalDateTimeUtil.getDayEnd(LocalDateTime.of(2026, 11, 12, 8, 9, 10));
        assertEquals(23, localDayEnd.getHour());
        assertEquals(59, localDayEnd.getMinute());
        assertEquals(59, localDayEnd.getSecond());
        assertEquals(999_999_000, localDayEnd.getNano());

        Date dateDayEnd = DateOffsetUtil.getDayEnd(Date.from(LocalDateTime.of(2026, 11, 12, 8, 9, 10)
                .atZone(ZoneId.systemDefault()).toInstant()));
        LocalDateTime dateAsLocal = dateDayEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        assertEquals(23, dateAsLocal.getHour());
        assertEquals(59, dateAsLocal.getMinute());
        assertEquals(59, dateAsLocal.getSecond());
        assertEquals(999, dateAsLocal.getNano() / 1_000_000);
    }

    @Test
    void extractAllNumberShouldDropIllegalMultiDotToken() {
        assertTrue(RegexUtil.extractAllNumber("1.2.3").isEmpty());

        List<Number> numbers = RegexUtil.extractAllNumber("a1.2.3b 4.5 .5");
        assertEquals(2, numbers.size());
        assertEquals("4.5", numbers.get(0).toString());
        assertEquals("0.5", numbers.get(1).toString());
    }

    @Test
    void timestampShouldUse24HourClockAndRandomRange() {
        String timestamp = RandomUtil.generateTimestamp();
        LocalDateTime parsed = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        assertEquals(14, timestamp.length());
        assertEquals(LocalDateTime.now().getYear(), parsed.getYear());

        for (int i = 0; i < 50; i++) {
            int random = Integer.parseInt(RandomUtil.generateTimestampWithRandom().substring(14));
            assertTrue(random >= 100);
            assertTrue(random <= 999);
        }
    }

    @Test
    void berObjectIdentifierShouldSupportOneZeroAndRoundTrip() throws Exception {
        byte[] encoded = writeBer(writer -> writer.writeObjectIdentifier(new long[]{1L, 0L}));
        long[] decoded = new BERReader(new ByteArrayInputStream(encoded)).readObjectIdentifier(encoded.length);
        assertArrayEquals(new long[]{1L, 0L}, decoded);

        byte[] multiArc = writeBer(writer -> writer.writeObjectIdentifier(new long[]{2L, 100L, 3L}));
        long[] multiDecoded = new BERReader(new ByteArrayInputStream(multiArc)).readObjectIdentifier(multiArc.length);
        assertArrayEquals(new long[]{2L, 100L, 3L}, multiDecoded);
    }

    @Test
    void berBitStringShouldRoundTripAndKeepUnusedBits() throws Exception {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        byte[] encoded = writeBer(writer -> writer.writeBitString(bitSet));
        assertEquals(2, encoded.length);
        assertEquals(7, encoded[0]);
        assertEquals((byte) 0x80, encoded[1]);

        BitSet decoded = new BERReader(new ByteArrayInputStream(encoded)).readBitString(encoded.length);
        assertTrue(decoded.get(0));
    }

    @Test
    void pageNullSliceShouldUseIndependentDefault() {
        Page<Object> first = Page.of(Collections.emptyList(), 0L, (Slice) null);
        Page<Object> second = Page.of(Collections.emptyList(), 0L, (Slice) null);
        first.setCurrent(99);

        assertEquals(1, second.current().intValue());
    }

    @Test
    void ioCopyShouldReturnLongCountAndCreateTempFileOutsideCwd() throws Exception {
        byte[] data = "abc".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        long copied = IoUtil.copy(new ByteArrayInputStream(data), out);
        assertEquals(3L, copied);

        java.io.File tempFile = IoUtil.base64ToFile(Base64.getEncoder().encodeToString(data));
        assertEquals("temp", tempFile.getParentFile().getName().toLowerCase());
    }

    @Test
    void differenceShouldRejectNullPredicate() {
        BiPredicate<String, String> predicate = null;
        assertThrows(IllegalArgumentException.class,
                () -> ArrayUtil.difference(Arrays.asList("a"), Arrays.asList("b"), predicate));
    }

    private byte[] writeBer(Consumer<BERWriter> writerConsumer) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BERWriter writer = new BERWriter(out);
        writerConsumer.accept(writer);
        writer.flush();
        return out.toByteArray();
    }
}
