package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

/**
 * HexUtil.toBinaryString 方法的单元测试类
 */
public class HexUtilToBinaryStringTest {

    /**
     * TC01: BitSet 为 null，应返回空字符串 ""
     */
    @Test
    public void testToBinaryString_NullInput() {
        String result = HexUtil.toBinaryString(null);
        assertEquals("", result);
    }

    /**
     * TC02: BitSet 为空（未设置任何位），应返回空字符串 ""
     */
    @Test
    public void testToBinaryString_EmptyBitSet() {
        BitSet bitSet = new BitSet();
        String result = HexUtil.toBinaryString(bitSet);
        assertEquals("", result);
    }

    /**
     * TC03: BitSet 设置第 0 位为 true，应返回 "1"
     */
    @Test
    public void testToBinaryString_SingleBitSetAtZero() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        String result = HexUtil.toBinaryString(bitSet);
        assertEquals("1", result);
    }

    /**
     * TC04: BitSet 设置第 0、1、2 位为 true，其余为 false，应返回 "111"
     */
    @Test
    public void testToBinaryString_FirstThreeBitsSet() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        bitSet.set(2);
        String result = HexUtil.toBinaryString(bitSet);
        assertEquals("111", result);
    }

    /**
     * TC05: BitSet 设置第 0、2、4、6 位为 true，其余为 false，应返回 "1010101"
     */
    @Test
    public void testToBinaryString_AlternateBitsSet() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(2);
        bitSet.set(4);
        bitSet.set(6);
        String result = HexUtil.toBinaryString(bitSet);
        assertEquals("1010101", result);
    }

    /**
     * TC06: BitSet 设置较高位例如第 10 位为 true，前面全部为 false，
     * 应返回 "00000000001" （共11位）
     */
    @Test
    public void testToBinaryString_HigherBitSet() {
        BitSet bitSet = new BitSet();
        bitSet.set(10);
        String result = HexUtil.toBinaryString(bitSet);
        assertEquals("00000000001", result);
    }

    /**
     * TC07: BitSet 设置所有低八位为 true，即 11111111，应返回 "11111111"
     */
    @Test
    public void testToBinaryString_AllLowEightBitsSet() {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < 8; i++) {
            bitSet.set(i);
        }
        String result = HexUtil.toBinaryString(bitSet);
        assertEquals("11111111", result);
    }
}
