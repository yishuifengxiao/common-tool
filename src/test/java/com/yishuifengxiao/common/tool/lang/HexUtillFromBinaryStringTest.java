package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.*;

/**
 * HexUtil.fromBinaryString 方法的单元测试类
 */
public class HexUtillFromBinaryStringTest {

    /**
     * TC01: 输入为 null，应返回空 BitSet
     */
    @Test
    public void testFromBinaryString_NullInput() {
        BitSet result = HexUtil.fromBinaryString(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * TC02: 输入为空字符串，应返回空 BitSet
     */
    @Test
    public void testFromBinaryString_EmptyInput() {
        BitSet result = HexUtil.fromBinaryString("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * TC03: 输入为 "1"，应返回第 0 位为 true 的 BitSet
     */
    @Test
    public void testFromBinaryString_SingleOne() {
        BitSet result = HexUtil.fromBinaryString("1");
        assertNotNull(result);
        assertEquals(1, result.length());
        assertTrue(result.get(0));
    }

    /**
     * TC04: 输入为 "0"，应返回空 BitSet
     */
    @Test
    public void testFromBinaryString_SingleZero() {
        BitSet result = HexUtil.fromBinaryString("0");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }



    /**
     * 验证较长的二进制字符串转换是否正确
     */
    @Test
    public void testFromBinaryString_LongerString() {
        BitSet result = HexUtil.fromBinaryString("100000001");
        assertNotNull(result);
        assertEquals(9, result.length());
        assertTrue(result.get(0));
        assertTrue(result.get(8));
        for (int i = 1; i < 8; i++) {
            assertFalse(result.get(i));
        }
    }

    /**
     * TC06: 输入为 "0000"，应返回空 BitSet
     */
    @Test
    public void testFromBinaryString_AllZeros() {
        BitSet result = HexUtil.fromBinaryString("0000");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * TC07: 输入为 "1111"，应返回第 0~3 位为 true 的 BitSet
     */
    @Test
    public void testFromBinaryString_AllOnes() {
        BitSet result = HexUtil.fromBinaryString("1111");
        assertNotNull(result);
        assertEquals(4, result.length());
        for (int i = 0; i < 4; i++) {
            assertTrue(result.get(i));
        }
    }


}
