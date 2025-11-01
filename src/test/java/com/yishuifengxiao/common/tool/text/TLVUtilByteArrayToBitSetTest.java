package com.yishuifengxiao.common.tool.text;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.*;

/**
 * TLVUtil.byteArrayToBitSet 方法的单元测试类
 */
public class TLVUtilByteArrayToBitSetTest {

    /**
     * TC01: 输入为 null，应返回空 BitSet
     */
    @Test
    public void testByteArrayToBitSet_NullInput() {
        BitSet result = TLVUtil.byteArrayToBitSet(null);
        assertNotNull(result);
        assertEquals(0, result.length());
    }

    /**
     * TC02: 输入为空数组，应返回空 BitSet
     */
    @Test
    public void testByteArrayToBitSet_EmptyArray() {
        BitSet result = TLVUtil.byteArrayToBitSet(new byte[0]);
        assertNotNull(result);
        assertEquals(0, result.length());
    }

    /**
     * TC03: 单个字节，最低位为 1（0x01），应设置 BitSet 第 0 位为 true
     */
    @Test
    public void testByteArrayToBitSet_SingleByteLowestBit() {
        byte[] input = new byte[]{0x01}; // 0000 0001
        BitSet result = TLVUtil.byteArrayToBitSet(input);

        assertTrue(result.get(0));
        for (int i = 1; i < 8; i++) {
            assertFalse(result.get(i));
        }
    }

    /**
     * TC04: 单个字节，最高位为 1（0x80），应设置 BitSet 第 7 位为 true
     */
    @Test
    public void testByteArrayToBitSet_SingleByteHighestBit() {
        byte[] input = new byte[]{(byte) 0x80}; // 1000 0000
        BitSet result = TLVUtil.byteArrayToBitSet(input);

        assertTrue(result.get(7));
        for (int i = 0; i < 7; i++) {
            assertFalse(result.get(i));
        }
    }

    /**
     * TC05: 多个字节，跨字节设置（0x01, 0x02），应设置 BitSet 第 0 和第 9 位为 true
     */
    @Test
    public void testByteArrayToBitSet_MultipleBytes() {
        byte[] input = new byte[]{0x01, 0x02}; // 0000 0001, 0000 0010
        BitSet result = TLVUtil.byteArrayToBitSet(input);

        assertTrue(result.get(0));   // 第一个字节第 0 位
        assertTrue(result.get(9));   // 第二个字节第 1 位（8 + 1）
        assertFalse(result.get(1));  // 第一个字节第 1 位未设置
        assertFalse(result.get(8));  // 第二个字节第 0 位未设置
    }

    /**
     * TC06: 全 1 字节（0xFF），应设置 BitSet 前 8 位全为 true
     */
    @Test
    public void testByteArrayToBitSet_AllBitsSet() {
        byte[] input = new byte[]{(byte) 0xFF}; // 1111 1111
        BitSet result = TLVUtil.byteArrayToBitSet(input);

        for (int i = 0; i < 8; i++) {
            assertTrue(result.get(i));
        }
    }
}
