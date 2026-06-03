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
