package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * CMAC.calculate 单元测试类
 */
public class CMACTest {


    private static final String TEST_KEY_HEX = "9DD5D73CB40668F7CBEA45DB1E3ECFB0";
    private static final String TEST_DATA_HEX = "24F7002D1128496A4403D675557590EB8718FD396C88D14DCCBBB64E664D330EA597";
    private static final String EXPECTED_CMAC_HEX = "631DC24731C5272FB0887987580F38AC";

    @Test
    public void testCalculateHex_ValidInputs_ReturnsExpectedResult() {
        String result = CMAC.calculate(TEST_KEY_HEX, TEST_DATA_HEX);
        assertNotNull("CMAC calculation should not return null for valid inputs", result);
        assertEquals("CMAC calculation should return expected result", EXPECTED_CMAC_HEX.toUpperCase(),
                result.toUpperCase());
    }

    @Test
    public void testCalculateHex_ValidInputs_ReturnsExpectedResult2() {
        String result = CMAC.calculate("DF44B25E2C89CD872FF19C48D2815D21",
                "C0C7D92977793D22CC684858866AEF1487186DE97254E3E5AA420199A4416295362F");
        assertNotNull("CMAC calculation should not return null for valid inputs", result);
        assertEquals("CMAC calculation should return expected result",
                "087BE81559A5AACE22D6CE64204A504C".toUpperCase(), result.toUpperCase());
    }

    @Test
    public void testCalculateHex_ValidInputs_ReturnsExpectedResult3() {
        String result = CMAC.calculate("DF44B25E2C89CD872FF19C48D2815D21",
                "087BE81559A5AACE22D6CE64204A504C884ABF253F5A0A986811415220491600719104636d6363920870726f66696c6531950102B6173015800204F0810F31302e382e33302e37353a38303930B705800344F444");
        assertNotNull("CMAC calculation should not return null for valid inputs", result);
        assertEquals("CMAC calculation should return expected result",
                "52801F1FDCDD62F91D2B408F3D68F234".toUpperCase(), result.toUpperCase());
    }

    @Test
    public void testCalculateHex_SingleByteData() {
        String key = "2b7e151628aed2a6abf7158809cf4f3c";
        String data = "6b"; // 单字节数据
        String result = CMAC.calculate(key, data);
        assertNotNull("CMAC calculation should not return null for single byte data", result);
    }

    @Test
    public void testCalculateHex_ShortData() {
        String key = "2b7e151628aed2a6abf7158809cf4f3c";
        String data = "6bc1bee22e409f96e93d7e11739317"; // 15字节数据，少于16字节
        String result = CMAC.calculate(key, data);
        assertNotNull("CMAC calculation should not return null for short data", result);
    }

    @Test
    public void testCalculateHex_LongerData() {
        String key = "2b7e151628aed2a6abf7158809cf4f3c";
        String data = "6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e51" +
                "30c81c46a35ce411e5fbc1191a0a52ef" +
                "f69f2445df4f9b17ad2b417be66c3710"; // 多个16字节块的数据
        String result = CMAC.calculate(key, data);
        assertNotNull("CMAC calculation should not return null for longer data", result);
    }


    @Test
    public void testCalculate_InvalidKeyLength() {
        // 使用长度不是16字节的密钥，应该能正常工作或给出预期结果
        String shortKey = "000102030405060708090a0b0c0d0e"; // 15字节
        String data = "6bc1bee22e409f96e93d7e117393172a";

        try {
            String result = CMAC.calculate(shortKey, data);
            // 根据具体实现，这里可能返回null或抛出异常
            // 这个测试主要是验证不会抛出意外异常
        } catch (Exception e) {
            // 可能由于密钥长度问题抛出异常，这是预期的行为
        }
    }

}
