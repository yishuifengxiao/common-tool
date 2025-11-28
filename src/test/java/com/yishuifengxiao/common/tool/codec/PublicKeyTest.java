package com.yishuifengxiao.common.tool.codec;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;

/**
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class PublicKeyTest {

    private PublicKey publicKey;

    @Before
    public void before() throws NoSuchAlgorithmException {
        KeyPairGenerator ecKeyGen = KeyPairGenerator.getInstance("EC");
        ecKeyGen.initialize(256);
        KeyPair ecKeyPair = ecKeyGen.generateKeyPair();
        publicKey = ecKeyPair.getPublic();
    }

    @Test
    public void test_publicKeyToPem() throws CertificateException {
        String pem = X509Helper.publicKeyToPem(publicKey);
        System.out.println(pem);
    }

    @Test
    public void test_publicKeyToHex() throws CertificateException {
        String hex = X509Helper.publicKeyToHex(publicKey);
        System.out.println(hex);
    }

    @Test
    public void test_publicKeyToBase64() throws CertificateException {
        PublicKey publicKey1 = X509Helper.parsePublicKey(X509Helper.publicKeyToHex(publicKey));
        PublicKey publicKey2 = X509Helper.parsePublicKey(X509Helper.publicKeyToPem(publicKey));
        assert publicKey1.equals(publicKey2);
    }

}
