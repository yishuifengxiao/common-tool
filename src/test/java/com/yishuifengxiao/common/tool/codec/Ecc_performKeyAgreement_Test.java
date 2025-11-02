package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
public class Ecc_performKeyAgreement_Test {

    @Test
    public void test_performKeyAgreement_01() throws Exception {
        KeyPair aliceKeyPair = ECC.generateECCKeyPair();
        KeyPair bobKeyPair = ECC.generateECCKeyPair("1.2.840.10045.3.1.7");
        byte[] aliceKeyAgreement = ECC.performKeyAgreement(aliceKeyPair.getPrivate(), bobKeyPair.getPublic());
        byte[] bobKeyAgreement = ECC.performKeyAgreement(bobKeyPair.getPrivate(), aliceKeyPair.getPublic());
        assertArrayEquals(aliceKeyAgreement, bobKeyAgreement);
    }

    @Test
    public void test_parsePublicKeyFromHex() throws Exception {
        KeyPair aliceKeyPair = ECC.generateECCKeyPair();
        KeyPair bobKeyPair = ECC.generateECCKeyPair("1.2.840.10045.3.1.7");
        String alicePublicKeyHex = KeyPairHelper.getPublicKeyHex(aliceKeyPair);
        String alicePrivateKeyDHex = KeyPairHelper.getPrivateKeyDHex(aliceKeyPair);

        String bobPublicKeyHex = KeyPairHelper.getPublicKeyHex(bobKeyPair);
        String bobPrivateKeyDHex = KeyPairHelper.getPrivateKeyDHex(bobKeyPair);

        System.out.println("alicePublicKeyHex len = " + alicePublicKeyHex.length());
        System.out.println("alicePrivateKeyDHex len = " + alicePrivateKeyDHex.length());
        System.out.println("bobPublicKeyHex len = " + bobPublicKeyHex.length());
        System.out.println("bobPrivateKeyDHex len = " + bobPrivateKeyDHex.length());


        byte[] aliceKeyAgreement = ECC.performKeyAgreement("1.2.840.10045.3.1.7", alicePrivateKeyDHex, bobPublicKeyHex);
        byte[] bobKeyAgreement = ECC.performKeyAgreement("1.2.840.10045.3.1.7", bobPrivateKeyDHex, alicePublicKeyHex);
        assertArrayEquals(aliceKeyAgreement, bobKeyAgreement);
    }
}
