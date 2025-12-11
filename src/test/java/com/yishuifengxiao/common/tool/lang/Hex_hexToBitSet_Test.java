package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.BitSet;

/**
 * Hex工具类中hexToBitSet和bytesToBitSet方法的单元测试
 */
public class Hex_hexToBitSet_Test {

    /**
     * 测试hexToBitSet方法 - 空或null输入
     */
    @Test
    public void testHexToBitSetWithNullAndEmptyInput() {
        // 测试null输入
        BitSet nullResult = Hex.hexToBitSet(null);
        assertNotNull(nullResult);
        assertEquals(0, nullResult.length());

        // 测试空字符串输入
        BitSet emptyResult = Hex.hexToBitSet("");
        assertNotNull(emptyResult);
        assertEquals(0, emptyResult.length());
    }

    /**
     * 测试hexToBitSet方法 - 正常的十六进制字符串
     */
    @Test
    public void testHexToBitSetWithValidHex() {
        // 测试简单的十六进制字符串 0001
        BitSet result1 = Hex.hexToBitSet("0001");
        assertNotNull(result1);
        assertFalse(result1.get(0)); // 第一个字节的第一个位应该是0
        assertFalse(result1.get(7)); // 第一个字节的最后一个位应该是0

        // 测试十六进制字符串 FF
        BitSet result2 = Hex.hexToBitSet("FF");
        assertNotNull(result2);
        for (int i = 0; i < 8; i++) {
            assertTrue("Bit at position " + i + " should be set", result2.get(i));
        }

        // 测试十六进制字符串 00
        BitSet result3 = Hex.hexToBitSet("00");
        assertNotNull(result3);
        for (int i = 0; i < 8; i++) {
            assertFalse("Bit at position " + i + " should not be set", result3.get(i));
        }
    }

    /**
     * 测试hexToBitSet方法 - 与bytesToBitSet方法的互操作性
     */
    @Test
    public void testHexToBitSetAndBytesToBitSetInteroperability() {
        // 创建一个字节数组
        byte[] originalBytes = new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x0F};

        // 将字节数组转换为十六进制字符串
        String hexString = Hex.bytesToHex(originalBytes);

        // 使用hexToBitSet将十六进制字符串转换为BitSet
        BitSet bitSetFromHex = Hex.hexToBitSet(hexString);

        // 使用bytesToBitSet将字节数组直接转换为BitSet
        BitSet bitSetFromBytes = Hex.bytesToBitSet(originalBytes);

        // 验证两个BitSet是否相等
        assertTrue(Hex.contentEquals(bitSetFromHex, bitSetFromBytes));
    }

    /**
     * 测试bytesToBitSet方法 - 空或null输入
     */
    @Test
    public void testBytesToBitSetWithNullAndEmptyInput() {
        // 测试null输入
        BitSet nullResult = Hex.bytesToBitSet(null);
        assertNotNull(nullResult);
        assertEquals(0, nullResult.length());

        // 测试空字节数组输入
        BitSet emptyResult = Hex.bytesToBitSet(new byte[0]);
        assertNotNull(emptyResult);
        assertEquals(0, emptyResult.length());
    }

    /**
     * 测试bytesToBitSet方法 - 正常的字节数组
     */
    @Test
    public void testBytesToBitSetWithValidBytes() {
        // 测试全1字节
        byte[] allOnes = new byte[]{(byte) 0xFF};
        BitSet allOnesResult = Hex.bytesToBitSet(allOnes);
        for (int i = 0; i < 8; i++) {
            assertTrue("Bit at position " + i + " should be set", allOnesResult.get(i));
        }

        // 测试全0字节
        byte[] allZeros = new byte[]{(byte) 0x00};
        BitSet allZerosResult = Hex.bytesToBitSet(allZeros);
        for (int i = 0; i < 8; i++) {
            assertFalse("Bit at position " + i + " should not be set", allZerosResult.get(i));
        }

        // 测试混合字节
        byte[] mixed = new byte[]{(byte) 0xA5}; // 10100101
        BitSet mixedResult = Hex.bytesToBitSet(mixed);
        assertTrue(mixedResult.get(0));  // 1
        assertFalse(mixedResult.get(1)); // 0
        assertTrue(mixedResult.get(2));  // 1
        assertFalse(mixedResult.get(3)); // 0
        assertFalse(mixedResult.get(4)); // 0
        assertTrue(mixedResult.get(5));  // 1
        assertFalse(mixedResult.get(6)); // 0
        assertTrue(mixedResult.get(7));  // 1
    }

    /**
     * 测试bytesToBitSet方法 - 多字节
     */
    @Test
    public void testBytesToBitSetWithMultipleBytes() {
        // 测试多字节数组
        byte[] multiBytes = new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0xF0};
        BitSet multiBytesResult = Hex.bytesToBitSet(multiBytes);

        // 验证第一个字节 (0xFF)
        for (int i = 0; i < 8; i++) {
            assertTrue("Bit at position " + i + " should be set", multiBytesResult.get(i));
        }

        // 验证第二个字节 (0x00)
        for (int i = 8; i < 16; i++) {
            assertFalse("Bit at position " + i + " should not be set", multiBytesResult.get(i));
        }

    }

    /**
     * 测试hexToBitSet方法 - 大小写十六进制字符
     */
    @Test
    public void testHexToBitSetWithMixedCase() {
        // 测试小写十六进制字符
        BitSet lowerCaseResult = Hex.hexToBitSet("ff");

        // 测试大写十六进制字符
        BitSet upperCaseResult = Hex.hexToBitSet("FF");

        // 测试大小写混合的十六进制字符
        BitSet mixedCaseResult = Hex.hexToBitSet("Ff");

        // 验证三个结果应该相同
        assertTrue(Hex.contentEquals(lowerCaseResult, upperCaseResult));
        assertTrue(Hex.contentEquals(upperCaseResult, mixedCaseResult));
    }

    /**
     * 测试BitSet相关方法的完整转换链
     */
    @Test
    public void testBitSetCompleteConversionChain() {
        // 原始数据
        String originalString = "Hello BitSet";
        byte[] originalBytes = originalString.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // 转换链: bytes -> hex -> BitSet -> bytes
        String hexString = Hex.bytesToHex(originalBytes);
        BitSet bitSet = Hex.hexToBitSet(hexString);
        byte[] restoredBytes = Hex.bitSetToBytes(bitSet);

        // 验证转换后的内容与原始内容一致
        assertArrayEquals(originalBytes, restoredBytes);
    }

    /**
     * 测试复杂的BitSet操作
     */
    @Test
    public void testComplexBitSetOperations() {
        // 测试较长的十六进制字符串
        String longHexString = "DEADBEEFCAFEBABE";
        BitSet longBitSet = Hex.hexToBitSet(longHexString);
        assertNotNull(longBitSet);
        assertTrue(longBitSet.length() > 0);

        // 验证转换回的十六进制字符串与原始字符串一致（除了大小写）
        String restoredHex = Hex.bitSetToHex(longBitSet);
        assertEquals(longHexString, restoredHex);
    }
}
