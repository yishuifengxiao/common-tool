package com.yishuifengxiao.common.tool.text;

import com.yishuifengxiao.common.tool.lang.TLVUtil;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TLVUtil.toTLV 方法的单元测试类
 */
public class TLVUtilTest {

    /**
     * TC01: 正常路径，长度 ≤ 127
     */
    @Test
    public void testToTLV_LengthLessThanOrEqualTo127() {
        String tag = "9F01";
        String input = "AABBCC"; // 长度 = 3
        String expected = "9F0103AABBCC";
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }

    /**
     * TC02: 空标签
     */
    @Test
    public void testToTLV_EmptyTag() {
        String tag = "";
        String input = "AABBCCDD"; // 长度 = 4
        String expected = "04AABBCCDD";
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }

    /**
     * TC03: 正常路径，长度 ≤ 127
     */
    @Test
    public void testToTLV_NormalCase() {
        String tag = "9F02";
        String input = "A0B0C0D0E0F0"; // 长度 = 6
        String expected = "9F0206A0B0C0D0E0F0";
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }

    /**
     * TC04: 长度 = 128，使用 81 标识
     */
    @Test
    public void testToTLV_Length128() {
        String tag = "9F03";
        String input = "AA".repeat(128); // 长度 = 128
        String expected = "9F038180" + input;
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }

    /**
     * TC05: 长度 = 256，使用 82 标识
     */
    @Test
    public void testToTLV_Length256() {
        String tag = "9F04";
        String input = "AA".repeat(256); // 长度 = 256
        String expected = "9F04820100" + input;
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }

    /**
     * TC06: 长度 = 65536，使用 83 标识
     */
    @Test
    public void testToTLV_Length65536() {
        String tag = "9F05";
        String input = "AA".repeat(65536); // 长度 = 65536
        String expected = "9F0583010000" + input;
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }

    /**
     * TC07: 标签为 null
     */
    @Test
    public void testToTLV_NullTag() {
        String tag = null;
        String input = "AABB"; // 长度 = 2
        String expected = "02AABB";
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }

    /**
     * TC08: 输入为空字符串
     */
    @Test
    public void testToTLV_EmptyInput() {
        String tag = "9F06";
        String input = "";
        String expected = "9F0600";
        assertEquals(expected, TLVUtil.toTLV(tag, input));
    }
}
