package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import java.util.BitSet;

/**
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class HexUtil_Test {

    @Test
    public void testHexStringToBitSet() {
        BitSet bitSet = new BitSet(4);
        bitSet.set(0);
        bitSet.set(2);
        String hexString1 = HexUtil.bitSetToHex(bitSet);
        System.out.println(hexString1);
        System.out.println(Integer.toBinaryString(Integer.parseInt(hexString1, 16)));
    }
}
