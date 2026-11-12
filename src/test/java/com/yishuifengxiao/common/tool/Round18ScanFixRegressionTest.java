package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.asn1.BERReader;
import com.yishuifengxiao.common.tool.lang.Hex;
import com.yishuifengxiao.common.tool.lang.TLVFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Round18 扫描修复回归测试
 * <p>
 * 覆盖：Hex 十六进制转换在 byteNum &gt;= 8 时的掩码溢出、Hex.padHexLeft 的 0x 前缀处理、
 * TLVFormatter 长度字段整数溢出、BERReader.readRelativeOID 空输入与截断编码。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Round18 扫描修复回归测试")
class Round18ScanFixRegressionTest {

    @Test
    @DisplayName("numberToHexString 在 byteNum 为 8 时不再因位移溢出把值抹成 0")
    void numberToHexStringWithEightBytesKeepsValue() {
        // (1L << 64) 按 64 取模等于 1，旧实现掩码为 0，会把所有值转成 0
        assertEquals("0000000000000005", Hex.numberToHexString(5, 8));
        // int 只有 4 字节，负值按 4 字节无符号表示后再补齐到 8 字节
        assertEquals("00000000FFFFFFFF", Hex.numberToHexString(-1, 8));
        assertEquals("0000000000000005", Hex.numberToHexString(5L, 8));
        assertEquals("FFFFFFFFFFFFFFFF", Hex.numberToHexString(-1L, 8));
        assertEquals("0000000000000005", Hex.numberToHexString((short) 5, 8));
        assertEquals("0000000000000005", Hex.numberToHexString((byte) 5, 8));
        // byteNum 大于 8 同样不能触发按 64 取模的错误掩码
        assertEquals("00000000000000000000000000000005", Hex.numberToHexString(5L, 16));
    }

    @Test
    @DisplayName("numberToHexString 在 byteNum 小于 8 时保持原有符号扩展行为")
    void numberToHexStringWithSmallByteNumUnchanged() {
        // 文档约定输出长度允许超过 2 * byteNum，int 负值仍输出完整 4 字节
        assertEquals("FFFFFFFF", Hex.numberToHexString(-1, 1));
        assertEquals("FFFF", Hex.numberToHexString((short) -1, 2));
        assertEquals("0005", Hex.numberToHexString(5, 2));
        assertEquals("FF", Hex.numberToHexString((byte) -1, 1));
    }

    @Test
    @DisplayName("padHexLeft 支持 0x 前缀与内部空白，并对非法输入返回 null")
    void padHexLeftHandlesPrefixAndBlank() {
        assertEquals("0x0012", Hex.padHexLeft("0x12", 2));
        assertEquals("0X0012", Hex.padHexLeft("0X12", 2));
        assertEquals("0012", Hex.padHexLeft("12", 2));
        assertEquals("0012", Hex.padHexLeft(" 1 2 ", 2));
        // 已达目标长度时原样返回（仅大写十六进制部分）
        assertEquals("0x1234", Hex.padHexLeft("0x1234", 2));
        assertNull(Hex.padHexLeft(null, 2));
        assertNull(Hex.padHexLeft("0x1", 2));
        assertNull(Hex.padHexLeft("0xZZ", 2));
        assertNull(Hex.padHexLeft("", 2));
    }

    @Test
    @DisplayName("TLVFormatter 对超大或负数长度字段抛出受控异常而不是分配巨量内存")
    void tlvFormatterRejectsOverflowLength() {
        // 0x84 后跟 0x7FFFFFFF：非负但超出剩余字节，旧实现 position + length 溢出后绕过校验
        assertThrows(IllegalArgumentException.class, () -> TLVFormatter.formatTLV(false, "04847FFFFFFF"));
        // 0x84 后跟 0xFFFFFFFF：长格式最高位为 1，按有符号解析为 -1
        assertThrows(IllegalArgumentException.class, () -> TLVFormatter.formatTLV(false, "0484FFFFFFFF"));
    }

    @Test
    @DisplayName("readRelativeOID 对空输入与截断编码抛出异常")
    void readRelativeOIDRejectsEmptyAndTruncated() {
        BERReader emptyReader = new BERReader(new ByteArrayInputStream(new byte[0]));
        assertThrows(RuntimeException.class, () -> emptyReader.readRelativeOID(0));

        // 0x81 带延续位但没有后续字节，旧实现返回哨兵值 -1
        BERReader truncatedReader = new BERReader(new ByteArrayInputStream(new byte[]{(byte) 0x81}));
        assertThrows(RuntimeException.class, () -> truncatedReader.readRelativeOID(1));
    }

    @Test
    @DisplayName("readRelativeOID 对合法 Base-128 编码仍正确解析")
    void readRelativeOIDStillParsesValidInput() throws IOException {
        // 1.2.840 去掉首段后的相对 OID 示例：单字节 arc 与两字节 arc 混合
        BERReader reader = new BERReader(new ByteArrayInputStream(new byte[]{0x01, (byte) 0x81, 0x00}));
        long[] arcs = reader.readRelativeOID(3);
        assertEquals(2, arcs.length);
        assertEquals(1L, arcs[0]);
        assertEquals(128L, arcs[1]);
    }
}
