package com.yishuifengxiao.common.tool.lang;

import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Map;

/**
 * HexBitset 工具类的单元测试
 */
@DisplayName("HexBitset 工具类测试")
class HexBitsetTest {

    // ==================== hexToBitSet 方法测试 ====================

    @Test
    @DisplayName("hexToBitSet - null和空字符串输入")
    void testHexToBitSet_NullAndEmpty() {
        BitSet result1 = HexBitset.hexToBitSet(null);
        Assert.assertNotNull(result1);
        Assert.assertTrue(result1.isEmpty());

        BitSet result2 = HexBitset.hexToBitSet("");
        Assert.assertNotNull(result2);
        Assert.assertTrue(result2.isEmpty());
    }

    @Test
    @DisplayName("hexToBitSet - 带0x前缀的十六进制字符串")
    void testHexToBitSet_WithPrefix() {
        BitSet bs1 = HexBitset.hexToBitSet("0x80");
        Assert.assertTrue(bs1.get(7));
        Assert.assertFalse(bs1.get(6));

        BitSet bs2 = HexBitset.hexToBitSet("0X80");
        Assert.assertTrue(bs2.get(7));
    }

    @Test
    @DisplayName("hexToBitSet - 奇数长度十六进制字符串")
    void testHexToBitSet_OddLength() {
        BitSet bs = HexBitset.hexToBitSet("780");
        BitSet expected = HexBitset.hexToBitSet("0780");
        Assert.assertEquals(expected, bs);
    }

    @Test
    @DisplayName("hexToBitSet - 大小写不敏感")
    void testHexToBitSet_CaseInsensitive() {
        BitSet bs1 = HexBitset.hexToBitSet("0780");
        BitSet bs2 = HexBitset.hexToBitSet("078A");
        BitSet bs3 = HexBitset.hexToBitSet("078a");

        Assert.assertEquals(bs1, HexBitset.hexToBitSet("0780"));
        Assert.assertEquals(bs2, bs3);
    }

    @Test
    @DisplayName("hexToBitSet - 常规场景转换")
    void testHexToBitSet_Normal() {
        Map.of(
                "80", 7,
                "40", 6,
                "20", 5,
                "10", 4,
                "08", 3,
                "04", 2,
                "02", 1,
                "01", 0
        ).forEach((hex, expectedBit) -> {
            BitSet bs = HexBitset.hexToBitSet(hex);
            Assert.assertTrue("Expected bit " + expectedBit + " to be set for hex " + hex, bs.get(expectedBit));
        });
    }

    @Test
    @DisplayName("hexToBitSet - 多字节只处理最后一个字节")
    void testHexToBitSet_MultiByte() {
        BitSet bs1 = HexBitset.hexToBitSet("0780");
        BitSet bs2 = HexBitset.hexToBitSet("80");
        Assert.assertEquals(bs1, bs2);

        BitSet bs3 = HexBitset.hexToBitSet("FFFF80");
        Assert.assertEquals(bs1, bs3);
    }

    @Test
    @DisplayName("hexToBitSet - 全零输入")
    void testHexToBitSet_AllZeros() {
        BitSet bs = HexBitset.hexToBitSet("00");
        Assert.assertTrue(bs.isEmpty());
    }

    @Test
    @DisplayName("hexToBitSet - 全F输入")
    void testHexToBitSet_AllFs() {
        BitSet bs = HexBitset.hexToBitSet("FF");
        for (int i = 0; i < 8; i++) {
            Assert.assertTrue("Bit " + i + " should be set", bs.get(i));
        }
    }

    // ==================== bitSetToHex 方法测试 ====================

    @Test
    @DisplayName("bitSetToHex - null和空BitSet")
    void testBitSetToHex_NullAndEmpty() {
        String result1 = HexBitset.bitSetToHex(null);
        Assert.assertEquals("00", result1);

        String result2 = HexBitset.bitSetToHex(new BitSet());
        Assert.assertEquals("00", result2);
    }

    @Test
    @DisplayName("bitSetToHex - 单个位设置")
    void testBitSetToHex_SingleBit() {
        BitSet bs = new BitSet();
        bs.set(7);
        String hex = HexBitset.bitSetToHex(bs);
        Assert.assertEquals("0780", hex);
    }

    @Test
    @DisplayName("bitSetToHex - 多个位设置")
    void testBitSetToHex_MultipleBits() {
        BitSet bs = new BitSet();
        bs.set(7);
        bs.set(0);
        String hex = HexBitset.bitSetToHex(bs);
        Assert.assertEquals("0781", hex);
    }

    @Test
    @DisplayName("bitSetToHex - 高位字节场景")
    void testBitSetToHex_HighByte() {
        BitSet bs = new BitSet();
        bs.set(15);
        String hex = HexBitset.bitSetToHex(bs);
        Assert.assertTrue(hex.length() == 4);
    }

    // ==================== bitSetToHexFixed 方法测试 ====================

    @Test
    @DisplayName("bitSetToHexFixed - 固定长度补零")
    void testBitSetToHexFixed_Padding() {
        BitSet bs = HexBitset.hexToBitSet("80");
        String hex = HexBitset.bitSetToHexFixed(bs, 4);
        Assert.assertEquals(8, hex.length());
        Assert.assertTrue(hex.endsWith("80"));
    }

    @Test
    @DisplayName("bitSetToHexFixed - 超长截断")
    void testBitSetToHexFixed_Truncate() {
        BitSet bs = new BitSet();
        bs.set(15);
        bs.set(7);
        String hex = HexBitset.bitSetToHexFixed(bs, 1);
        Assert.assertEquals(2, hex.length());
    }

    @Test
    @DisplayName("bitSetToHexFixed - 空BitSet")
    void testBitSetToHexFixed_Empty() {
        String hex = HexBitset.bitSetToHexFixed(new BitSet(), 2);
        Assert.assertEquals("0000", hex);
    }

    // ==================== binaryToBitSet 方法测试 ====================

    @Test
    @DisplayName("binaryToBitSet - null和空字符串")
    void testBinaryToBitSet_NullAndEmpty() {
        BitSet result1 = HexBitset.binaryToBitSet(null);
        Assert.assertNotNull(result1);
        Assert.assertTrue(result1.isEmpty());

        BitSet result2 = HexBitset.binaryToBitSet("");
        Assert.assertNotNull(result2);
        Assert.assertTrue(result2.isEmpty());
    }

    @Test
    @DisplayName("binaryToBitSet - 常规二进制字符串")
    void testBinaryToBitSet_Normal() {
        BitSet bs = HexBitset.binaryToBitSet("10000000");
        Assert.assertTrue(bs.get(7));
        for (int i = 0; i < 7; i++) {
            Assert.assertFalse("Bit " + i + " should not be set", bs.get(i));
        }
    }

    @Test
    @DisplayName("binaryToBitSet - 多个位设置")
    void testBinaryToBitSet_MultipleBits() {
        BitSet bs = HexBitset.binaryToBitSet("10000001");
        Assert.assertTrue(bs.get(7));
        Assert.assertTrue(bs.get(0));
    }

    @Test
    @DisplayName("binaryToBitSet - 非法字符抛出异常")
    void testBinaryToBitSet_InvalidChar() {
        try {
            HexBitset.binaryToBitSet("10000002");
            Assert.fail("Should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(e.getMessage().contains("二进制字符串只能包含 '0' 和 '1'"));
        }
    }

    @Test
    @DisplayName("binaryToBitSet - 不同长度字符串")
    void testBinaryToBitSet_DifferentLengths() {
        BitSet bs4 = HexBitset.binaryToBitSet("1000");
        Assert.assertTrue(bs4.get(3));

        BitSet bs16 = HexBitset.binaryToBitSet("1000000000000000");
        Assert.assertTrue(bs16.get(15));
    }

    // ==================== bitSetToBinary 方法测试 ====================

    @Test
    @DisplayName("bitSetToBinary - null和空BitSet")
    void testBitSetToBinary_NullAndEmpty() {
        String result1 = HexBitset.bitSetToBinary(null);
        Assert.assertEquals("0", result1);

        String result2 = HexBitset.bitSetToBinary(new BitSet());
        Assert.assertEquals("0", result2);
    }

    @Test
    @DisplayName("bitSetToBinary - 至少输出8位")
    void testBitSetToBinary_Minimum8Bits() {
        BitSet bs = new BitSet();
        bs.set(0);
        String binary = HexBitset.bitSetToBinary(bs);
        Assert.assertEquals(8, binary.length());
        Assert.assertEquals("00000001", binary);
    }

    @Test
    @DisplayName("bitSetToBinary - 高位设置输出更多位")
    void testBitSetToBinary_HighBit() {
        BitSet bs = new BitSet();
        bs.set(15);
        String binary = HexBitset.bitSetToBinary(bs);
        Assert.assertEquals(16, binary.length());
        Assert.assertTrue(binary.startsWith("1"));
    }

    @Test
    @DisplayName("bitSetToBinary - 常规场景")
    void testBitSetToBinary_Normal() {
        BitSet bs = new BitSet();
        bs.set(7);
        String binary = HexBitset.bitSetToBinary(bs);
        Assert.assertEquals("10000000", binary);

        BitSet bs2 = new BitSet();
        bs2.set(4);
        String binary2 = HexBitset.bitSetToBinary(bs2);
        Assert.assertEquals("00010000", binary2);
    }

    // ==================== bitSetToBinaryFixed 方法测试 ====================

    @Test
    @DisplayName("bitSetToBinaryFixed - 固定长度补零")
    void testBitSetToBinaryFixed_Padding() {
        BitSet bs = new BitSet();
        bs.set(0);
        String binary = HexBitset.bitSetToBinaryFixed(bs, 16);
        Assert.assertEquals(16, binary.length());
        Assert.assertTrue(binary.endsWith("1"));
    }

    @Test
    @DisplayName("bitSetToBinaryFixed - 超长截断")
    void testBitSetToBinaryFixed_Truncate() {
        BitSet bs = new BitSet();
        bs.set(15);
        String binary = HexBitset.bitSetToBinaryFixed(bs, 8);
        Assert.assertEquals(8, binary.length());
    }

    @Test
    @DisplayName("bitSetToBinaryFixed - 空BitSet")
    void testBitSetToBinaryFixed_Empty() {
        String binary = HexBitset.bitSetToBinaryFixed(new BitSet(), 8);
        Assert.assertEquals("00000000", binary);
    }

    @Test
    @DisplayName("bitSetToBinaryFixed - 不同固定长度")
    void testBitSetToBinaryFixed_DifferentLengths() {
        BitSet bs = new BitSet();
        bs.set(3);

        String binary4 = HexBitset.bitSetToBinaryFixed(bs, 4);
        Assert.assertEquals("1000", binary4);

        String binary8 = HexBitset.bitSetToBinaryFixed(bs, 8);
        Assert.assertEquals("00001000", binary8);
    }

    // ==================== 往返转换测试 ====================

    @Test
    @DisplayName("往返转换 - hex到BitSet再回到hex")
    void testRoundTrip_HexToBitSetToHex() {
        String[] testCases = {"80", "40", "20", "10", "08", "04", "02", "01", "FF", "00"};
        for (String hex : testCases) {
            BitSet bs = HexBitset.hexToBitSet(hex);
            String resultHex = HexBitset.bitSetToHex(bs);
            Assert.assertEquals("Round trip failed for hex: " + hex,
                    hex.toUpperCase(), resultHex.substring(resultHex.length() - 2));
        }
    }

    @Test
    @DisplayName("往返转换 - binary到BitSet再回到binary")
    void testRoundTrip_BinaryToBitSetToBinary() {
        String[] testCases = {"10000000", "01000000", "00100000", "00010000", "00000001"};
        for (String binary : testCases) {
            BitSet bs = HexBitset.binaryToBitSet(binary);
            String resultBinary = HexBitset.bitSetToBinary(bs);
            Assert.assertEquals("Round trip failed for binary: " + binary, binary, resultBinary);
        }
    }

    @Test
    @DisplayName("往返转换 - hex到binary再回到hex")
    void testRoundTrip_HexToBinaryToHex() {
        Map.of("0780", "10000000", "0640", "01000000", "0520", "00100000", "0410", "00010000")
                .forEach((hex, expectedBinary) -> {
                    BitSet bsFromHex = HexBitset.hexToBitSet(hex);
                    String fullBinary = HexBitset.bitSetToBinary(bsFromHex);
                    Assert.assertEquals(expectedBinary, fullBinary);

                    BitSet bsFromBinary = HexBitset.binaryToBitSet(fullBinary);
                    String resultHex = HexBitset.bitSetToHex(bsFromBinary);
                    Assert.assertEquals(hex.toUpperCase(), resultHex.toUpperCase());
                });
    }
    
}
