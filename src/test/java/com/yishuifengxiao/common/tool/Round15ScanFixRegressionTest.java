package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.asn1.BERReader;
import com.yishuifengxiao.common.tool.asn1.BERTag;
import com.yishuifengxiao.common.tool.io.ImageUtil;
import com.yishuifengxiao.common.tool.lang.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round15 全局扫描问题修复回归测试
 * <p>
 * 覆盖 BER 标签号溢出与负值防护、BER 标签/长度读取边界、ASCII 与十六进制往返、
 * 图片 Base64 空值传递。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Round15 扫描修复回归测试")
class Round15ScanFixRegressionTest {

    @Test
    @DisplayName("空标签数组应被拒绝，正常两字节标签可解析")
    void bertagRejectsEmptyAndParsesNormalTag() {
        assertThrows(IllegalArgumentException.class, () -> new BERTag(new byte[0]));

        BERTag tag = new BERTag(new byte[]{0x1F, 0x01});
        // 长格式编码的标签号 1 重新编码时规范化为单字节标签
        assertArrayEquals(new byte[]{0x01}, tag.getByteArray());
    }

    @Test
    @DisplayName("超长标签号应抛异常而不是溢出成负数")
    void bertagRejectsOverflowingTagNumber() {
        // 6 个续位字节后标签号约为 2^35，超出 int 表示范围
        byte[] tagBytes = new byte[]{0x1F, (byte) 0x81, (byte) 0x81, (byte) 0x81, (byte) 0x81,
                (byte) 0x81, (byte) 0x01};
        assertThrows(RuntimeException.class, () -> new BERTag(tagBytes));
    }

    @Test
    @Timeout(2)
    @DisplayName("负标签号调用 getByteList 应立即抛异常而不是死循环")
    void bertagRejectsNegativeNumberOnEncode() {
        BERTag tag = new BERTag(BERTag.Class.UNIVERSAL, -1, BERTag.Form.PRIMITIVE);
        assertThrows(RuntimeException.class, tag::getByteList);
    }

    @Test
    @DisplayName("超长 BER 标签应以 IOException 结束而不是数组越界")
    void berReaderRejectsTooLongTag() {
        byte[] malformed = new byte[12];
        malformed[0] = 0x1F;
        Arrays.fill(malformed, 1, malformed.length, (byte) 0xFF);
        BERReader reader = new BERReader(new ByteArrayInputStream(malformed));
        assertThrows(IOException.class, reader::readTag);
    }

    @Test
    @DisplayName("BER 读取负长度（不定长）应抛 IOException")
    void berReaderRejectsNegativeLength() {
        BERReader reader = new BERReader(new ByteArrayInputStream(new byte[0]));
        assertThrows(IOException.class, () -> reader.readOctetString(-1));
        assertThrows(IOException.class, () -> reader.readRestrictedCharacterString(-1));
    }

    @Test
    @DisplayName("BER 正常读取八位字节串与受限字符串")
    void berReaderReadsNormalPayload() throws IOException {
        byte[] payload = new byte[]{0x41, 0x42, 0x43};
        try (InputStream in = new ByteArrayInputStream(payload)) {
            BERReader reader = new BERReader(in);
            assertArrayEquals(payload, reader.readOctetString(payload.length));
        }
        try (InputStream in = new ByteArrayInputStream(payload)) {
            BERReader reader = new BERReader(in);
            assertEquals("ABC", reader.readRestrictedCharacterString(payload.length));
        }
    }

    @Test
    @DisplayName("ASCII 与十六进制互转应保留首尾空格")
    void asciiHexRoundTripKeepsSpaces() {
        assertEquals(" A ", Hex.hexToAscii(Hex.asciiToHex(" A ")));
        assertEquals("204120", Hex.asciiToHex(" A "));
    }

    @Test
    @DisplayName("图片 Base64 空值应返回 null，正常图片应带 PNG 头")
    void imageToBase64WithHeaderHandlesNull() {
        assertNull(ImageUtil.imageToBase64WithPngHeader(null));

        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(2, 2,
                java.awt.image.BufferedImage.TYPE_INT_RGB);
        String result = ImageUtil.imageToBase64WithPngHeader(image);
        assertTrue(result != null && result.startsWith("data:image/png;base64,"), "应带 PNG 数据头");
    }
}
