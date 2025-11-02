package com.yishuifengxiao.common.tool.text;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertArrayEquals;

/**
 * HexUtil.bitSetToByteArray 方法的单元测试类
 */
public class HexUtilBitSetToByteArrayTest {

    /**
     * TC01: BitSet 为 null，应返回空数组
     */
    @Test
    public void testBitSetToByteArray_NullInput() {
        byte[] result = HexUtil.bitSetToByteArray(null);
        assertArrayEquals(new byte[0], result);
    }

    /**
     * TC02: BitSet 为空（未设置任何位），应返回空数组
     */
    @Test
    public void testBitSetToByteArray_EmptyBitSet() {
        BitSet bitSet = new BitSet();
        byte[] result = HexUtil.bitSetToByteArray(bitSet);
        assertArrayEquals(new byte[0], result);
    }

    /**
     * TC03: BitSet 设置第 0 位为 true，应返回 [0x01]
     */
    @Test
    public void testBitSetToByteArray_SingleBitSetAtZero() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        byte[] result = HexUtil.bitSetToByteArray(bitSet);
        assertArrayEquals(new byte[]{0x01}, result);
    }

    /**
     * TC04: BitSet 设置第 0、1、7 位为 true，应返回 [0x83]
     */
    @Test
    public void testBitSetToByteArray_MultipleBitsInOneByte() {
        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);
        bitSet.set(7);
        byte[] result = HexUtil.bitSetToByteArray(bitSet);
        assertArrayEquals(new byte[]{(byte) 0x83}, result); // 1000 0011
    }

    /**
     * TC05: BitSet 设置第 8 位为 true，应返回 [0x00, 0x01]
     */
    @Test
    public void testBitSetToByteArray_BitBeyondFirstByte() {
        BitSet bitSet = new BitSet();
        bitSet.set(8);
        byte[] result = HexUtil.bitSetToByteArray(bitSet);
        assertArrayEquals(new byte[]{0x00, 0x01}, result);
    }


    /**
     * 验证 bit 位顺序是否正确：设置第 0-7 位全为 true
     */
    @Test
    public void testBitSetToByteArray_AllBitsInFirstByte() {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < 8; i++) {
            bitSet.set(i);
        }
        byte[] result = HexUtil.bitSetToByteArray(bitSet);
        assertArrayEquals(new byte[]{(byte) 0xFF}, result);
    }

}
