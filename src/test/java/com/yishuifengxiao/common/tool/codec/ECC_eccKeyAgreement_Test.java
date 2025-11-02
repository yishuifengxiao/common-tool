package com.yishuifengxiao.common.tool.codec;

import org.junit.Test;

import java.security.KeyPair;

import static org.junit.Assert.assertEquals;

/**
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
public class ECC_eccKeyAgreement_Test {

    @Test
    public void test_eccKeyAgreement() throws Exception {
        KeyPair aliceKeyPair = ECC.generateECCKeyPair();
        KeyPair bobKeyPair = ECC.generateECCKeyPair("1.2.840.10045.3.1.7");
        String result1 = ECC.eccKeyAgreement("1.2.840.10045.3.1.7", KeyPairHelper.getPublicKeyHex(aliceKeyPair), KeyPairHelper.getPrivateKeyDHex(bobKeyPair), "012345", 48);
        System.out.println(result1);
        String result2 = ECC.eccKeyAgreement("1.2.840.10045.3.1.7", KeyPairHelper.getPublicKeyHex(bobKeyPair), KeyPairHelper.getPrivateKeyDHex(aliceKeyPair), "012345", 48);
        System.out.println(result2);
        assertEquals(result1, result2);
    }
}
