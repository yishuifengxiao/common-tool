package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * CMAC工具类的单元测试
 */
public class CMACTest {

    // RFC 4493标准测试用例
    private static final String TEST_KEY = "2b7e151628aed2a6abf7158809cf4f3c"; // AES-128密钥
    private static final String EMPTY_DATA = "";
    private static final String EMPTY_DATA_CMAC = "bb1d6929e95937287fa37d129b756746"; // 空数据的CMAC

    private static final String ONE_BLOCK_DATA = "6bc1bee22e409f96e93d7e117393172a";
    private static final String ONE_BLOCK_DATA_CMAC = "070a16b46b4d4144f79bdd9dd04a287c";

    private static final String TWO_BLOCK_DATA = "6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e51";
    private static final String TWO_BLOCK_DATA_CMAC = "CE0CBF1738F4DF6428B1D93BF12081C9";

    private static final String THREE_BLOCK_DATA = "6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e5130c81c46a35ce411";
    private static final String THREE_BLOCK_DATA_CMAC = "51f0bebf7e3b9d92fc49741779363cfe";


    /**
     * 测试calculateCMAC方法 - 一个数据块
     */
    @Test
    public void testCalculateCMAC_OneBlock() throws Exception {
        String result = CMAC.calculateCMAC(ONE_BLOCK_DATA, TEST_KEY);
        assertEquals("一个数据块CMAC计算错误", ONE_BLOCK_DATA_CMAC.toLowerCase(), result.toLowerCase());
    }

    /**
     * 测试calculateCMAC方法 - 两个数据块
     */
    @Test
    public void testCalculateCMAC_TwoBlocks() throws Exception {
        String result = CMAC.calculateCMAC(TWO_BLOCK_DATA, TEST_KEY);
        assertEquals("两个数据块CMAC计算错误", TWO_BLOCK_DATA_CMAC.toLowerCase(), result.toLowerCase());
    }



    /**
     * 测试leftShiftOneBit方法 - 正常情况
     */
    @Test
    public void testLeftShiftOneBit_Normal() throws Exception {
        // 构造一个测试数组
        byte[] input = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        
        // 手动计算期望结果 (0x80左移一位变成0x00，最高位的1丢失，没有进位)
        byte[] expected = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        
        // 通过反射访问私有方法
        java.lang.reflect.Method method = CMAC.class.getDeclaredMethod("leftShiftOneBit", byte[].class);
        method.setAccessible(true);
        byte[] result = (byte[]) method.invoke(null, (Object) input);
        
        assertArrayEquals("左移一位计算错误", expected, result);
    }

    /**
     * 测试leftShiftOneBit方法 - 带进位情况
     */
    @Test
    public void testLeftShiftOneBit_WithCarry() throws Exception {
        // 构造一个测试数组
        byte[] input = new byte[]{(byte) 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        
        // 手动计算期望结果 (0xC0左移一位变成0x80，最高位的1丢失，有进位)
        byte[] expected = new byte[]{(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                                     0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        
        // 通过反射访问私有方法
        java.lang.reflect.Method method = CMAC.class.getDeclaredMethod("leftShiftOneBit", byte[].class);
        method.setAccessible(true);
        byte[] result = (byte[]) method.invoke(null, (Object) input);
        
        assertArrayEquals("左移一位计算错误", expected, result);
    }

    /**
     * 测试xor方法 - 正常情况
     */
    @Test
    public void testXor_Normal() throws Exception {
        byte[] a = new byte[]{(byte) 0xFF, 0x00, (byte) 0xAA, 0x55};
        byte[] b = new byte[]{(byte) 0x00, (byte) 0xFF, (byte) 0x55, (byte) 0xAA};
        byte[] expected = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        
        // 通过反射访问私有方法
        java.lang.reflect.Method method = CMAC.class.getDeclaredMethod("xor", byte[].class, byte[].class);
        method.setAccessible(true);
        method.invoke(null, (Object) a, (Object) b);
        
        assertArrayEquals("异或操作错误", expected, a);
    }

    /**
     * 测试xor方法 - 相同数组异或
     */
    @Test
    public void testXor_SameArray() throws Exception {
        byte[] a = new byte[]{(byte) 0xFF, 0x00, (byte) 0xAA, 0x55};
        byte[] b = new byte[]{(byte) 0xFF, 0x00, (byte) 0xAA, 0x55};
        byte[] expected = new byte[]{0x00, 0x00, 0x00, 0x00};
        
        // 通过反射访问私有方法
        java.lang.reflect.Method method = CMAC.class.getDeclaredMethod("xor", byte[].class, byte[].class);
        method.setAccessible(true);
        method.invoke(null, (Object) a, (Object) b);
        
        assertArrayEquals("相同数组异或操作错误", expected, a);
    }
}