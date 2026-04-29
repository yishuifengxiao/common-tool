package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TLVUtil 单元测试类
 * 测试TLV数据提取功能，包括短格式和长格式长度字段
 */
public class TLVUtilTest {

    // ==================== extract 方法测试 ====================

    /**
     * 测试正常场景：提取短格式长度的TLV数据（长度<=127）
     * 预期结果：成功提取值并返回剩余数据
     */
    @Test
    public void testExtract_ShortFormat_Success() {
        String tlv = "9F02060000000005009F0306000000000000";
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", tlv);

        assertTrue(result.isSuccess());
        assertEquals("000000000500", result.getVal("9F02"));
        assertEquals("9F0306000000000000", result.getRemain());
    }

    @Test
    public void testExtract_ShortFormat_Success1() {
        String tlv = "A000";
        TLVUtil.TlvResult result = TLVUtil.extract("A0", tlv);

        assertTrue(result.isSuccess());
        assertEquals("", result.getVal("A0"));
        assertEquals("", result.getRemain());
    }

    /**
     * 测试正常场景：提取长度为0的TLV数据（空值）
     */
    @Test
    public void testExtract_ZeroLengthValue_Success() {
        String tlv = "9F02009F0306000000000000";
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", tlv);

        assertTrue(result.isSuccess());
        assertEquals("", result.getVal("9F02"));
        assertEquals("9F0306000000000000", result.getRemain());
    }

    /**
     * 测试正常场景：提取长格式长度的TLV数据（长度>127）
     */
    @Test
    public void testExtract_LongFormat_Success() {
        String value = "000102030405060708090A0B0C0D0E0F";
        String tlv = "9F028110" + value + "9F0306000000000000";
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", tlv);

        assertTrue(result.isSuccess());
        assertEquals(value, result.getVal("9F02"));
        assertEquals("9F0306000000000000", result.getRemain());
    }

    /**
     * 测试边界场景：tag参数为null
     */
    @Test
    public void testExtract_NullTag_Failure() {
        String tlv = "9F0206000000000500";
        TLVUtil.TlvResult result = TLVUtil.extract(null, tlv);

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("illegal data", result.getException().getMessage());
    }

    /**
     * 测试边界场景：tlv参数为null
     */
    @Test
    public void testExtract_NullTLV_Failure() {
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", null);

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("illegal data", result.getException().getMessage());
    }

    /**
     * 测试边界场景：TLV不以指定tag开头
     */
    @Test
    public void testExtract_TagNotAtStart_Failure() {
        String tlv = "9F03060000000000009F0206000000000500";
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", tlv);

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
        assertTrue(result.getException().getMessage().contains("The data does not start with TAG 9F02"));
    }

    /**
     * 测试边界场景：value数据不足
     */
    @Test
    public void testExtract_InsufficientValueData_Failure() {
        String tlv = "9F020600000000";
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", tlv);

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("Not valid TLV data", result.getException().getMessage());
    }

    /**
     * 测试边界场景：不定长不支持
     */
    @Test
    public void testExtract_IndefiniteLength_Failure() {
        String tlv = "9F0280";
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", tlv);

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("Indefinite length not supported", result.getException().getMessage());
    }

    /**
     * 测试边界场景：长度字节超过最大值
     */
    @Test
    public void testExtract_LengthBytesExceedMaximum_Failure() {
        String tlv = "9F028400000010";
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", tlv);

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("Length bytes exceed maximum", result.getException().getMessage());
    }

    // ==================== extractVal 方法测试 ====================

    /**
     * 测试正常场景：extractVal成功提取值
     */
    @Test
    public void testExtractVal_Success() {
        String tlv = "9F0206000000000500";
        String value = TLVUtil.extractVal("9F02", tlv);

        assertEquals("000000000500", value);
    }

    /**
     * 测试边界场景：extractVal提取失败返回空字符串
     */
    @Test
    public void testExtractVal_Failure_ReturnsEmptyString() {
        String tlv = "9F0306000000000000";
        String value = TLVUtil.extractVal("9F02", tlv);

        assertEquals("", value);
    }

    // ==================== extractValsOnSameLevel 方法测试 ====================

    /**
     * 测试正常场景：在同一层级提取多个标签
     */
    @Test
    public void testExtractValsOnSameLevel_Success() {
        String tlv = "9F02060000000005009F03060000000000009F1A020156";
        TLVUtil.TlvResult result = TLVUtil.extractValsOnSameLevel(tlv, "9F02", "9F03", "9F1A");

        assertTrue(result.isSuccess());
        assertEquals("000000000500", result.getVal("9F02"));
        assertEquals("000000000000", result.getVal("9F03"));
        assertEquals("0156", result.getVal("9F1A"));
    }

    /**
     * 测试边界场景：tags参数为空
     */
    @Test
    public void testExtractValsOnSameLevel_NoTags_Failure() {
        String tlv = "9F0206000000000500";
        TLVUtil.TlvResult result = TLVUtil.extractValsOnSameLevel(tlv);

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
        assertEquals("no tags provided", result.getException().getMessage());
    }

    // ==================== TlvResult 类测试 ====================

    /**
     * 测试正常场景：getResults返回Map副本
     */
    @Test
    public void testTlvResult_GetResultsReturnsCopy() {
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", "9F0206000000000500");

        java.util.Map<String, String> results1 = result.getResults();
        java.util.Map<String, String> results2 = result.getResults();

        assertNotSame(results1, results2);

        results1.put("TEST", "VALUE");
        assertNull(results2.get("TEST"));
    }

    /**
     * 测试边界场景：getVal传入null或空标签
     */
    @Test
    public void testTlvResult_GetValWithNullOrEmptyTag() {
        TLVUtil.TlvResult result = TLVUtil.extract("9F02", "9F0206000000000500");

        assertEquals("", result.getVal(null));
        assertEquals("", result.getVal(""));
        assertEquals("", result.getVal("   "));
    }

    /**
     * 测试正常场景：isSuccess判断成功状态
     */
    @Test
    public void testTlvResult_IsSuccess() {
        TLVUtil.TlvResult successResult = TLVUtil.extract("9F02", "9F0206000000000500");
        assertTrue(successResult.isSuccess());

        TLVUtil.TlvResult failureResult = TLVUtil.extract(null, "9F0206000000000500");
        assertFalse(failureResult.isSuccess());
    }

    // ==================== 综合场景测试 ====================

    /**
     * 测试综合场景：EMV标准TLV数据解析
     */
    @Test
    public void testExtract_EMVData_Success() {
        String emvTLV =
                "9F0206000000000500" +
                        "9F0306000000000000" +
                        "9F1A020156" +
                        "5F2A020156";

        TLVUtil.TlvResult result1 = TLVUtil.extract("9F02", emvTLV);
        assertTrue(result1.isSuccess());
        assertEquals("000000000500", result1.getVal("9F02"));

        TLVUtil.TlvResult result2 = TLVUtil.extract("9F03", result1.getRemain());
        assertTrue(result2.isSuccess());
        assertEquals("000000000000", result2.getVal("9F03"));
    }

    /**
     * 测试综合场景：连续提取多个TLV元素
     */
    @Test
    public void testExtract_ConsecutiveTLVs_Success() {
        String tlv = "9F02060000000005009F03060000000000009F1A020156";

        TLVUtil.TlvResult result1 = TLVUtil.extract("9F02", tlv);
        assertTrue(result1.isSuccess());

        TLVUtil.TlvResult result2 = TLVUtil.extract("9F03", result1.getRemain());
        assertTrue(result2.isSuccess());

        TLVUtil.TlvResult result3 = TLVUtil.extract("9F1A", result2.getRemain());
        assertTrue(result3.isSuccess());

        assertEquals("0156", result3.getVal("9F1A"));
        assertEquals("", result3.getRemain());
    }

    // ==================== extractValsRecursive 方法测试 ====================

    /**
     * 测试正常场景：递归提取嵌套TLV数据
     */
    @Test
    public void testExtractValsRecursive_Nested_Success() {
        // 构造嵌套TLV：外层9F02包含内层9F03
        String innerTLV = "9F0306000000000000";
        String outerValue = String.format("%02X", innerTLV.length() / 2) + innerTLV;
        String tlv = "9F02" + outerValue;

        TLVUtil.TlvResult result = TLVUtil.extractValsRecursive(tlv, "9F02", "9F03");

        assertTrue(result.isSuccess());
        assertNotNull(result.getVal("9F02"));
        assertEquals("000000000000", result.getVal("9F03"));
    }

    /**
     * 测试边界场景：递归提取时标签不存在
     */
    @Test
    public void testExtractValsRecursive_MissingTag_Failure() {
        String tlv = "9F0206000000000500";
        TLVUtil.TlvResult result = TLVUtil.extractValsRecursive(tlv, "9F02", "9F99");

        assertFalse(result.isSuccess());
        assertNotNull(result.getException());
    }

    /**
     * 测试边界场景：递归提取tags为null
     */
    @Test
    public void testExtractValsRecursive_NullTags_Failure() {
        TLVUtil.TlvResult result = TLVUtil.extractValsRecursive("9F0206000000000500", (String[]) null);

        assertFalse(result.isSuccess());
        assertEquals("no tags provided", result.getException().getMessage());
    }

    /**
     * 测试边界场景：递归提取无效TLV
     */
    @Test
    public void testExtractValsRecursive_InvalidTLV_Failure() {
        TLVUtil.TlvResult result = TLVUtil.extractValsRecursive("XYZ", "9F02");

        assertFalse(result.isSuccess());
        assertEquals("illegal data", result.getException().getMessage());
    }

    // ==================== extractValsLoop 方法测试 ====================

    /**
     * 测试正常场景：循环提取多个相同标签
     */
    @Test
    public void testExtractValsLoop_MultipleSameTags_Success() {
        String tlv = "9F02060000000005009F02060000000006009F0206000000000700";
        java.util.List<String> values = TLVUtil.extractValsLoop("9F02", tlv);

        assertEquals(3, values.size());
        assertEquals("000000000500", values.get(0));
        assertEquals("000000000600", values.get(1));
        assertEquals("000000000700", values.get(2));
    }

    /**
     * 测试正常场景：循环提取单个标签
     */
    @Test
    public void testExtractValsLoop_SingleTag_Success() {
        String tlv = "9F0206000000000500";
        java.util.List<String> values = TLVUtil.extractValsLoop("9F02", tlv);

        assertEquals(1, values.size());
        assertEquals("000000000500", values.get(0));
    }

    /**
     * 测试边界场景：循环提取无匹配标签
     */
    @Test
    public void testExtractValsLoop_NoMatch_ReturnsEmptyList() {
        String tlv = "9F0306000000000000";
        java.util.List<String> values = TLVUtil.extractValsLoop("9F02", tlv);

        assertEquals(0, values.size());
    }

    /**
     * 测试边界场景：循环提取tag为null
     */
    @Test
    public void testExtractValsLoop_NullTag_ReturnsEmptyList() {
        java.util.List<String> values = TLVUtil.extractValsLoop(null, "9F0206000000000500");
        assertEquals(0, values.size());
    }

    /**
     * 测试边界场景：循环提取tlv为null
     */
    @Test
    public void testExtractValsLoop_NullTLV_ReturnsEmptyList() {
        java.util.List<String> values = TLVUtil.extractValsLoop("9F02", null);
        assertEquals(0, values.size());
    }

    // ==================== toTLV 方法测试 ====================

    /**
     * 测试正常场景：短格式长度（<=127）
     */
    @Test
    public void testToTLV_ShortFormat_Success() {
        String result = TLVUtil.toTLV("9F02", "000000000500");
        assertEquals("9F0206000000000500", result);
    }

    /**
     * 测试正常场景：1字节长格式长度（128-255）
     */
    @Test
    public void testToTLV_OneByteLongFormat_Success() {
        // 构造128字节的值
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            value.append("00");
        }
        String result = TLVUtil.toTLV("9F02", value.toString());
        assertTrue(result.startsWith("9F028180"));
    }

    /**
     * 测试正常场景：2字节长格式长度（256-65535）
     */
    @Test
    public void testToTLV_TwoByteLongFormat_Success() {
        // 构造256字节的值
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            value.append("00");
        }
        String result = TLVUtil.toTLV("9F02", value.toString());
        assertTrue(result.startsWith("9F02820100"));
    }

    /**
     * 测试边界场景：value为null
     */
    @Test
    public void testToTLV_NullValue_ReturnsTagOnly() {
        String result = TLVUtil.toTLV("9F02", null);
        assertEquals("9F02", result);
    }

    /**
     * 测试边界场景：value为空字符串
     */
    @Test
    public void testToTLV_EmptyValue_ReturnsTagOnly() {
        String result = TLVUtil.toTLV("9F02", "");
        assertEquals("9F02", result);
    }

    /**
     * 测试边界场景：tag为null或空
     */
    @Test
    public void testToTLV_NullOrBlankTag_Success() {
        String result1 = TLVUtil.toTLV(null, "000000000500");
        assertEquals("06000000000500", result1);

        String result2 = TLVUtil.toTLV("", "000000000500");
        assertEquals("06000000000500", result2);

        String result3 = TLVUtil.toTLV("   ", "000000000500");
        assertEquals("06000000000500", result3);
    }

    /**
     * 测试边界场景：value为非十六进制字符串
     */
    @Test
    public void testToTLV_InvalidHexValue_ReturnsTagOnly() {
        String result = TLVUtil.toTLV("9F02", "XYZ");
        assertEquals("9F02", result);
    }

    // ==================== calculateLengthHex 方法测试 ====================

    /**
     * 测试正常场景：短格式长度（<128）
     */
    @Test
    public void testCalculateLengthHex_ShortFormat_Success() {
        String result = TLVUtil.calculateLengthHex("000000000500");
        assertEquals("06", result);
    }

    /**
     * 测试正常场景：1字节长格式（128-255）
     */
    @Test
    public void testCalculateLengthHex_OneByteLongFormat_Success() {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 128; i++) {
            hex.append("00");
        }
        String result = TLVUtil.calculateLengthHex(hex.toString());
        assertEquals("8180", result);
    }

    /**
     * 测试正常场景：2字节长格式（256-65535）
     */
    @Test
    public void testCalculateLengthHex_TwoByteLongFormat_Success() {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 256; i++) {
            hex.append("00");
        }
        String result = TLVUtil.calculateLengthHex(hex.toString());
        assertEquals("820100", result);
    }

    /**
     * 测试正常场景：3字节长格式（65536-16777215）
     */
    @Test
    public void testCalculateLengthHex_ThreeByteLongFormat_Success() {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 65536; i++) {
            hex.append("00");
        }
        String result = TLVUtil.calculateLengthHex(hex.toString());
        assertEquals("83010000", result);
    }

    /**
     * 测试边界场景：空输入
     */
    @Test
    public void testCalculateLengthHex_EmptyInput_ReturnsZero() {
        assertEquals("00", TLVUtil.calculateLengthHex(""));
        assertEquals("00", TLVUtil.calculateLengthHex(null));
    }

    /**
     * 测试边界场景：带空格的十六进制字符串
     */
    @Test
    public void testCalculateLengthHex_WithSpaces_Success() {
        String result = TLVUtil.calculateLengthHex("00 00 00 00 05 00");
        assertEquals("06", result);
    }

    /**
     * 测试异常场景：长度超过最大范围
     */
    @Test(expected = com.yishuifengxiao.common.tool.exception.UncheckedException.class)
    public void testCalculateLengthHex_ExceedsMaximum_ThrowsException() {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < 16777216; i++) {
            hex.append("00");
        }
        TLVUtil.calculateLengthHex(hex.toString());
    }
}
