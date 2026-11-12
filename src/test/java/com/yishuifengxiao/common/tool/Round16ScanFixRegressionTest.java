package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.asn1.BERWriter;
import com.yishuifengxiao.common.tool.lang.TLVFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round16 全局扫描问题修复回归测试
 * <p>
 * 覆盖 ASN.1 REAL 特殊值与二进制编码解析、相对 OID 写入的入参校验与 Base-128 编码。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Round16 扫描修复回归测试")
class Round16ScanFixRegressionTest {

    @Test
    @DisplayName("REAL 特殊值按 X.690 首字节解析为 +INF、-INF、NaN")
    void realSpecialValuesFollowX690() {
        assertTrue(TLVFormatter.formatTLV(true, "090140").contains("+INF"));
        assertTrue(TLVFormatter.formatTLV(true, "090141").contains("-INF"));
        assertTrue(TLVFormatter.formatTLV(true, "090142").contains("NaN"));
    }

    @Test
    @DisplayName("二进制 REAL 的 2 字节指数被正确识别")
    void binaryRealSupportsTwoByteExponent() {
        // 首字节 0x81：二进制、正数、底数 2、scale 0、2 字节指数；指数 1，尾数 1，结果为 2
        assertTrue(TLVFormatter.formatTLV(true, "090481000101").contains("(REAL, 2)"));
    }

    @Test
    @DisplayName("二进制 REAL 的 2 字节负指数按补码符号扩展")
    void binaryRealTwoByteNegativeExponent() {
        // 首字节 0x81：2 字节指数；指数 0xFFFF = -1，尾数 3，结果为 3 * 2^-1 = 1.5
        assertTrue(TLVFormatter.formatTLV(true, "090481FFFF03").contains("(REAL, 1.5)"));
    }

    @Test
    @DisplayName("二进制 REAL 的 scale factor 参与计算")
    void binaryRealAppliesScaleFactor() {
        // 首字节 0x84：scale=1；指数 0，尾数 3，结果为 3 * 10 = 30
        assertTrue(TLVFormatter.formatTLV(true, "0903840003").contains("(REAL, 30)"));
    }

    @Test
    @DisplayName("十进制编码 REAL 不再被误判为二进制")
    void decimalRealIsParsedAsDecimal() {
        // 首字节 0x08 表示 NR3，数值部分为 3.14E0
        assertTrue(TLVFormatter.formatTLV(true, "090708332E31344530").contains("(REAL, 3.14)"));
    }

    @Test
    @DisplayName("writeRelativeOID 拒绝 null、空数组与负数 arc")
    void writeRelativeOidRejectsInvalidInput() {
        BERWriter writer = new BERWriter();
        assertThrows(RuntimeException.class, () -> writer.writeRelativeOID(null));
        assertThrows(RuntimeException.class, () -> writer.writeRelativeOID(new long[0]));
        assertThrows(RuntimeException.class, () -> writer.writeRelativeOID(new long[]{1L, -1L}));
    }

    @Test
    @DisplayName("writeRelativeOID 按 Base-128 编码")
    void writeRelativeOidEncodesBase128() throws Exception {
        assertArrayEquals(new byte[]{0x01, 0x02}, writeRelativeOid(new long[]{1L, 2L}));
        assertArrayEquals(new byte[]{(byte) 0x81, 0x00}, writeRelativeOid(new long[]{128L}));
    }

    private byte[] writeRelativeOid(long[] arcs) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BERWriter writer = new BERWriter(out);
        writer.writeRelativeOID(arcs);
        writer.flush();
        return out.toByteArray();
    }
}
