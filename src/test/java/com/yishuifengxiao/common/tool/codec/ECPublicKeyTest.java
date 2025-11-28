package com.yishuifengxiao.common.tool.codec;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;

import static org.junit.Assert.assertTrue;

/**
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ECPublicKeyTest {

    private ECPublicKey eCPublicKey;

    @Before
    public void test_before() throws Exception {
        KeyPair defaultKeyPair = ECC.generateECCKeyPair();
        eCPublicKey = (ECPublicKey) defaultKeyPair.getPublic();
    }

    @Test
    public void test_ecPublicKeyToPem() throws Exception {
        String pem = ECC.ecPublicKeyToPem(eCPublicKey);
        System.out.println(pem);
    }

    @Test
    public void test_ecPublicKeyToHex() throws Exception {
        String hex = ECC.ecPublicKeyToHex(eCPublicKey);
        System.out.println(hex);
    }

    @Test
    public void test_pemToEcPublicKey() throws Exception {
        ECPublicKey ecPublicKey1 = ECC.parseECPublicKey(ECC.ecPublicKeyToPem(eCPublicKey));
        ECPublicKey ecPublicKey2 = ECC.parseECPublicKey(ECC.ecPublicKeyToHex(eCPublicKey));
        assertTrue(ecPublicKey1.equals(ecPublicKey2));
    }
}
