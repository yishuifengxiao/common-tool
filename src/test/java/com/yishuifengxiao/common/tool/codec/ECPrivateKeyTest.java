package com.yishuifengxiao.common.tool.codec;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;

import static org.junit.Assert.assertTrue;

/**
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ECPrivateKeyTest {

    private ECPrivateKey privateKey;

    @Before
    public void test_before() throws Exception {
        KeyPair defaultKeyPair = ECC.generateECCKeyPair();
        privateKey = (ECPrivateKey) defaultKeyPair.getPrivate();
    }

    @Test
    public void test_ecPrivateKeyToPem() throws Exception {
        String pem = ECC.ecPrivateKeyToPem(privateKey);
        System.out.println(pem);
    }

    @Test
    public void test_ecPrivateKeyToHex() throws Exception {
        String hex = ECC.ecPrivateKeyToHex(privateKey);
        System.out.println(hex);
        String dValue = ECC.extractPrivateDValue(hex);
        System.out.println(dValue.length() + " : " + dValue);
    }

    @Test
    public void test_ecPrivateKeyToBase64() throws Exception {
        ECPrivateKey eCPrivateKey1 = ECC.parseECPrivateKey(ECC.ecPrivateKeyToPem(privateKey));
        ECPrivateKey eCPrivateKey2 = ECC.parseECPrivateKey(ECC.ecPrivateKeyToHex(privateKey));
        assertTrue(eCPrivateKey1.equals(eCPrivateKey2));
    }

}
