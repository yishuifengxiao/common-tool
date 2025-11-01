package com.yishuifengxiao.common.tool.text;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.*;

/**
 * TLVUtil.hexStringToBitSet 方法的单元测试类
 */
public class TLVUtilHexStringToBitSetTest {


    /**
     * TC02: 输入为空字符串，应返回空 BitSet
     */
    @Test
    public void testHexStringToBitSet_EmptyInput() {
        BitSet result = TLVUtil.hexStringToBitSet("");
        assertNotNull(result);
        assertEquals(0, result.cardinality());
    }


    /**
     * TC06: 奇数长度字符串，会导致 StringIndexOutOfBoundsException
     */
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testHexStringToBitSet_OddLengthString() {
        TLVUtil.hexStringToBitSet("ABC");
    }
}
