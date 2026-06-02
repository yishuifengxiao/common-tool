package com.yishuifengxiao.common.tool.text;

import com.yishuifengxiao.common.tool.lang.TLVUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TLVUnit#parse(String tag) 的单元测试类
 */
public class TLVUnitParseTest {

    /**
     * TC01: 正常解析带tag的数据
     */
    @Test
    public void testParse_NormalCaseWithTag() {
        String hexData = "5F370101";
        TLVUnit tlvUnit = TLVUnit.of(hexData).parse("5F37");

        assertTrue(tlvUnit.isSuccess(), "应解析成功");
        assertEquals("", tlvUnit.getError(), "不应出现错误");
        assertEquals("5F37", tlvUnit.getTag(), "tag 应为 '5F37'");
        assertEquals("01", tlvUnit.getValue(), "value 应为 '01'");
        assertEquals(1, tlvUnit.getValueLength(), "valueLength 应为 1");
        assertEquals("", tlvUnit.getRemainingData(), "剩余数据应为 ''");
    }

    /**
     * TC02: tag为空时也能解析
     */
    @Test
    public void testParse_TagIsNull() {
        String hexData = "5F370101";
        TLVUnit tlvUnit = TLVUnit.of(hexData).parse(null);

        assertFalse(tlvUnit.isSuccess(), "数据长度不足");
        assertEquals("解析错误: 数据长度不足", tlvUnit.getError(), "数据长度不足");
        assertEquals("", tlvUnit.getTag(), "tag 应为空");
        assertEquals("", tlvUnit.getValue(), "value 应为 ''");
        assertEquals(0, tlvUnit.getValueLength(), "valueLength 应为 0");
        assertEquals("5F370101", tlvUnit.getRemainingData(), "剩余数据应为 5F370101");
    }

    /**
     * TC03: tag不匹配导致失败
     */
    @Test
    public void testParse_TagMismatch() {
        String hexData = "5F370101";
        TLVUnit tlvUnit = TLVUnit.of(hexData).parse("6F37");

        assertFalse(tlvUnit.isSuccess(), "应解析失败");
        assertEquals("数据不以指定标签开头", tlvUnit.getError(), "错误信息应为 '数据不以指定标签开头'");
    }

    /**
     * TC04: 输入非合法Hex数据
     */
    @Test
    public void testParse_InvalidHexData() {
        String invalidHex = "XYZZ";
        TLVUnit tlvUnit = TLVUnit.of(invalidHex).parse("");

        assertFalse(tlvUnit.isSuccess(), "应解析失败");
        assertEquals("无效的Hex数据", tlvUnit.getError(), "错误信息应为 '无效的Hex数据'");
    }

    /**
     * TC05: 输入空数据
     */
    @Test
    public void testParse_EmptyData() {
        String emptyData = "";
        TLVUnit tlvUnit = TLVUnit.of(emptyData).parse("5F37");

        assertFalse(tlvUnit.isSuccess(), "应解析失败");
        assertEquals("无效的Hex数据", tlvUnit.getError(), "错误信息应为 '数据为空'");
    }

    /**
     * TC06: 解析过程抛出异常（如长度字段不足）
     */
    @Test
    public void testParse_ParseException() {
        String shortData = "5F37"; // 缺少长度和值字段
        TLVUnit tlvUnit = TLVUnit.of(shortData).parse("5F37");

        assertFalse(tlvUnit.isSuccess(), "应解析失败");
        assertTrue(tlvUnit.getError().startsWith("解析错误:"), "应报告解析错误");
    }

    /**
     * TC07: 第二次解析剩余数据
     */
    @Test
    public void testParse_SecondParseWithRemainingData() {
        String multiTlvData = "5F3701016F380102";
        TLVUnit firstTlvUnit = TLVUnit.of(multiTlvData).parse("5F37");

        assertTrue(firstTlvUnit.isSuccess(), "第一次应解析成功");
        assertEquals("01", firstTlvUnit.getValue(), "第一次value应为'01'");
        assertEquals("6F380102", firstTlvUnit.getRemainingData(), "剩余数据应为第二段");

        // 再次解析剩余数据
        TLVUnit secondTlvUnit = firstTlvUnit.parseRemaining("6F38");
        assertTrue(secondTlvUnit.isSuccess(), "第二次也应解析成功");
        assertEquals("02", secondTlvUnit.getValue(), "第二次value应为'02'");
        assertEquals("", secondTlvUnit.getRemainingData(), "最后无剩余数据");
    }
}
