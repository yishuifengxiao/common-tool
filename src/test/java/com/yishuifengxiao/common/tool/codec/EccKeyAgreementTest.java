package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;

/**
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class EccKeyAgreementTest {

    @Test
    public void test() throws Exception {
        String euiccOtpkHex = "04A96D0DEBC352387638D1A7ECBB0651234465BEA4A3F5195A9696FC53237E6298BEF31CD2D44C5B17FF2A1B1F4EC2553A1A0C8976337DAA4767B4A6D20F31D3DB";
        String privateKeyDHex = "43CE53DE35C6BAD13D839C362A0B566E23FDA64D3B0114152F1069477A7B459E";
        String shareInfo = "88100511223344551089086029202200000122101100001059";

        // 尝试两种方式：十六进制解析和普通字符串
        String sessionKeyHex = EccKeyAgreement.eccKeyAgreement("1.2.840.10045.3.1.7", euiccOtpkHex, privateKeyDHex, shareInfo, 48);
        System.out.println("Hex mode: " + sessionKeyHex);

        String expectedKey = "468F0EC0EDD9CDB75269379DCD5A340BD298270BA7920AAF9CB8C8BD440FFDC23B4600A3C2BC95C691484708375383AA";

        boolean flagHex = sessionKeyHex.equalsIgnoreCase(expectedKey);
        System.out.println("Hex mode: " + flagHex);
        if (flagHex) {
            System.out.println("Test passed!");
        } else {
            System.out.println("Test failed!");
            System.out.println("Expected: " + expectedKey);
        }
    }

    @Test
    public void test1() throws Exception {
        String euiccOtpkHex = "04A375247087727737688B2770A545364AE4D802F3E012D98D10BAF4E6EC8D8B90FC63E3E88429203B927041F087734903DD003B0BA31B91CD3C1169CBC615D19A";
        String privateKeyDHex = "57027BDDBB1F7F36362BB10DD1B7C1250636CA37CAFB32437AD14DA4C1B5609E";
        String shareInfo = "881005112233445589086029202200002122000046506478";

        // 尝试两种方式：十六进制解析和普通字符串
        String sessionKeyHex = EccKeyAgreement.eccKeyAgreement("1.2.840.10045.3.1.7", euiccOtpkHex, privateKeyDHex, shareInfo, 48);
        System.out.println("Hex mode: " + sessionKeyHex);

        String expectedKey = "0CA807EAC186E38F9712A6C27FBD7A1A55D7990FE88FEBCD81DE962311BE019D3DF25A6BB21DC34BB22EF5ED0B5E4D39";

        boolean flagHex = sessionKeyHex.equalsIgnoreCase(expectedKey);
        System.out.println("Hex mode: " + flagHex);
        if (flagHex) {
            System.out.println("Test passed!");
        } else {
            System.out.println("Test failed!");
            System.out.println("Expected: " + expectedKey);
        }
    }

    @Test
    public void test2() throws Exception {
        String euiccOtpkHex = "0456676C12819B80B0FB04B1D035B35BA52CB853F187BF3F2AEAE9A31D99F69DF6744E219477379F27A42CCF6354DE5B44FD5328F92B969C66C6201C46DF273837";
        String privateKeyDHex = "667C0D2BA9DC56CF40850088BD1873B53D66254330ED929D2166482BB4679AC9";
        String shareInfo = "881005112233445589086029202200002122000046506478";

        // 尝试两种方式：十六进制解析和普通字符串
        String sessionKeyHex = EccKeyAgreement.eccKeyAgreement("1.2.840.10045.3.1.7", euiccOtpkHex, privateKeyDHex, shareInfo, 48);
        System.out.println("Hex mode: " + sessionKeyHex);

        String expectedKey = "2517518C39EB1DA7CDFEE00CC6D54BDD5A9A3F5DA8FDA70C771C565676F7B8F1110FBE5F6447D3829B5C5B9FBEAABEA3";

        boolean flagHex = sessionKeyHex.equalsIgnoreCase(expectedKey);
        System.out.println("Hex mode: " + flagHex);
        if (flagHex) {
            System.out.println("Test passed!");
        } else {
            System.out.println("Test failed!");
            System.out.println("Expected: " + expectedKey);
        }
    }

    @Test
    public void test3() throws Exception {
        String euiccOtpkHex = "0478414C63C53046732D21A63020B6F64499C58BC15E089A399394EA4747A62C072D7BB890164361BE22797DA1A33BDDD621E79C2F33C380B61CBDFE82CC249C38";
        String privateKeyDHex = "E88E6F18F3F8544BC679498BC95A1196DF372C17AABDA4019E4150166A32B61F";
        String shareInfo = "88100511223344551089086029202200002122000046506478";

        // 尝试两种方式：十六进制解析和普通字符串
        String sessionKeyHex = EccKeyAgreement.eccKeyAgreement("1.2.840.10045.3.1.7", euiccOtpkHex, privateKeyDHex, shareInfo, 48);
        System.out.println("Hex mode: " + sessionKeyHex);

        String expectedKey = "D277377DBD72CB37C2809CA85A2404DD5B8C72B26A247E86149B57664D244E15FD8284645E47901D1DC491EA0382B8A6";

        boolean flagHex = sessionKeyHex.equalsIgnoreCase(expectedKey);
        System.out.println("Hex mode: " + flagHex);
        if (flagHex) {
            System.out.println("Test passed!");
        } else {
            System.out.println("Test failed!");
            System.out.println("Expected: " + expectedKey);
        }
    }
}
