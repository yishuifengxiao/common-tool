package com.yishuifengxiao.common.tool.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * HexUtil.base64ToHex 方法的单元测试类
 */
public class HexUtilBase64ToHexTest {

    /**
     * TC01: 输入为 null，应返回空字符串
     */
    @Test
    public void testBase64ToHex_NullInput() {
        String result = HexUtil.base64ToHex(null);
        assertEquals("", result);
    }

    /**
     * TC02: 输入为空字符串，应返回空字符串
     */
    @Test
    public void testBase64ToHex_EmptyString() {
        String result = HexUtil.base64ToHex("");
        assertEquals("", result);
    }

    /**
     * TC03: 正常 Base64 字符串 "AQID" -> 应返回 "010203"
     */
    @Test
    public void testBase64ToHex_NormalCase() {
        String base64Str = "AQID"; // [0x01, 0x02, 0x03]
        String expectedHex = "010203";
        String result = HexUtil.base64ToHex(base64Str);
        assertEquals(expectedHex, result);
    }

    /**
     * TC04: 包含填充的 Base64 字符串 "AA==" -> 应返回 "00"
     */
    @Test
    public void testBase64ToHex_WithPadding() {
        String base64Str = "AA=="; // [0x00]
        String expectedHex = "00";
        String result = HexUtil.base64ToHex(base64Str);
        assertEquals(expectedHex, result);
    }

    /**
     * TC05: 非法 Base64 字符串 "!!!" -> 应抛出 IllegalArgumentException
     */
    @Test
    public void testBase64ToHex_IllegalBase64() {
        String illegalBase64 = "!!!";
        assertThrows(IllegalArgumentException.class, () -> {
            HexUtil.base64ToHex(illegalBase64);
        });
    }

    @Test
    public void testBase64ToHex_SpecialCharacters() {
        String base64Str = """
                MIICTDCCAfGgAwIBAgIBATAKBggqhkjOPQQDAjBJMRUwEwYDVQQDDAxHU01BIFRl
                c3QgQ0kxETAPBgNVBAsMCFRFU1RDRVJUMRAwDgYDVQQKDAdSU1BURVNUMQswCQYD
                VQQGEwJJVDAeFw0xODAzMjAxMDQwMjJaFw0xOTAzMjAxMDQwMjJaMEkxFTATBgNV
                BAMMDEdTTUEgVGVzdCBDSTERMA8GA1UECwwIVEVTVENFUlQxEDAOBgNVBAoMB1JT
                UFRFU1QxCzAJBgNVBAYTAklUMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEA/PB
                6uvmm6S81GX7+3TAgku+4mjU+YqaeONdVFFoc4PnCvtGXstiXwq3qXT/yy3JeDo9
                dp58nVQdQz1AfEVUsaOByTCBxjAdBgNVHQ4EFgQUAMOLTWNfZq1jcHrDNOWGqckM
                IgAwDwYDVR0TAQH/BAUwAwEB/zAXBgNVHSABAf8EDTALMAkGB2eBEgECAQAwDgYD
                VR0PAQH/BAQDAgEGMA4GA1UdEQQHMAWIA4g3ATBbBgNVHR8EVDBSMCegJaAjhiFo
                dHRwOi8vY2kudGVzdC5nc21hLmNvbS9DUkwtQS5jcmwwJ6AloCOGIWh0dHA6Ly9j
                aS50ZXN0LmdzbWEuY29tL0NSTC1CLmNybDAKBggqhkjOPQQDAgNJADBGAiEA49aD
                cyVvfVGH+1ezy0gT10M/oMgYStBXZjs62HG2dD0CIQCprPpRh8qwNl4toPVr159G
                GT+il8g1UqapmvFJb/jOsg==
                """.trim();
        String hex = HexUtil.base64ToHex(base64Str);
        System.out.println(hex);
    }
}
