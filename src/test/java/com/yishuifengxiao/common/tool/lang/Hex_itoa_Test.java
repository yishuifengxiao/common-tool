package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Hex工具类的单元测试
 */
public class Hex_itoa_Test {

    @Test
    public void test_itoa() {
        // 测试正常情况
        assertEquals("000A", Hex.itoa(10, 2));
        assertEquals("000000FF", Hex.itoa(255, 4));
        assertEquals("000000000000000A", Hex.itoa(10, 8));
        assertEquals("7FFFFFFF", Hex.itoa(Integer.MAX_VALUE, 4));
    }

    @Test
    public void test_itoaWithInvalidParams() {
        // 测试负数参数
        try {
            Hex.itoa(-1, 2);
            fail("Expected UncheckedException to be thrown");
        } catch (UncheckedException e) {
            assertEquals("参数非法", e.getMessage());
        }

        // 测试超出长度限制的参数
        try {
            Hex.itoa(256, 1); // 256需要至少2个字节，但只分配了1个字节
            fail("Expected UncheckedException to be thrown");
        } catch (UncheckedException e) {
            assertEquals("参数非法", e.getMessage());
        }
    }

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
