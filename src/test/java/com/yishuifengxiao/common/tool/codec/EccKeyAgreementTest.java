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
    //20:16:19.331 [main] INFO com.yishuifengxiao.tool.personalkit.gsma.spg22.BppProducer -- [BppProducer]
    // ECC 密钥协商：OID=1.2.840.10045.3.1.7,
    // euiccOtpkHex=04697843B5C54E5B8DA68F5E8EA46CCA8D578FB8D23A02AAF6EAA6679C61D75A5B314B35254E463C15F30A2495AC2604338E5B2CF573A689FCE557A4584E4FFD32,
    // privateKeyDHex=00E8DA689492DF2968BF34C59C6D97E0DB6A1F82F82B2740D17EC4269AB5679C88,
    // shareInfo=881005112233445589086029202200002122000046506478, keyLen=48
}
