package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;

/**
 * Hex工具类的单元测试
 */
public class HexTest {

    @Test
    public void test_001() {
        String hex = "0A";
        BitSet bitSet = Hex.hexToBitSet(hex);
        String toHex = Hex.bitSetToHex(bitSet);
        System.out.println(toHex);
        assertEquals(hex, toHex);
    }

    @Test
    public void test_002() {
        String hex = "0A";
        String binaryString = Hex.hexStringToBinaryString(hex);
        System.out.println(binaryString);
        String hexString = Hex.binaryStringToHexString(binaryString);
        assertEquals(hex, hexString);
    }

    @Test
    public void test_003() {
        String val = "0101";
        String hexString = Hex.binaryStringToHexString(val);
        System.out.println(hexString);
        String binaryString = Hex.hexStringToBinaryString(hexString);
        assertEquals(val, binaryString);
    }
}
