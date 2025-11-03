package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.*;

/**
 * HexUtil.hexStringToBitSet 方法的单元测试类
 */
public class HexUtilHexStringToBitSetTest {


    /**
     * TC02: 输入为空字符串，应返回空 BitSet
     */
    @Test
    public void testHexStringToBitSet_EmptyInput() {
        BitSet result = HexUtil.hexStringToBitSet("");
        assertNotNull(result);
        assertEquals(0, result.cardinality());
    }


    /**
     * TC06: 奇数长度字符串，会导致 StringIndexOutOfBoundsException
     */
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testHexStringToBitSet_OddLengthString() {
        HexUtil.hexStringToBitSet("ABC");
    }
}
