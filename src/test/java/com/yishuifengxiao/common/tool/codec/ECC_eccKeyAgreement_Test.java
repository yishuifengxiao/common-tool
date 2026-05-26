package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;
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
        KeyPair aliceKeyPair = ECC.generateECCKeyPair("1.2.840.10045.3.1.7");
        KeyPair bobKeyPair = ECC.generateECCKeyPair("1.2.840.10045.3.1.7");
        String result1 = ECC.eccKeyAgreement("1.2.840.10045.3.1.7", KeyPairHelper.getPublicKeyHex(bobKeyPair),
                KeyPairHelper.getPrivateKeyDHex(aliceKeyPair), "012345", 48);
        System.out.println(result1);
        String result2 = ECC.eccKeyAgreement("1.2.840.10045.3.1.7", KeyPairHelper.getPublicKeyHex(aliceKeyPair),
                KeyPairHelper.getPrivateKeyDHex(bobKeyPair), "012345", 48);
        System.out.println(result2);
        assertEquals(result1, result2);
    }

    @Test
    public void test_parsePublicKeyFromHex1() throws Exception {
        String alicePrivateKeyDHex = "00c2a49764da324cf75134ac274a055a927ad214566583818aa81eb5ec4bfe6950";

        String bobPublicKeyHex =
                "0478619F549A3D5E94B8AB615AB451D8FCCFBBD2B9E705E1D66289BA8B45F896435C5F8FE53B49FFF0DF5443D394B5A1AFFE3B610841081A2C8FB165AAE26783C7";

        String share_info = "881005112233445589086029202200002122000046506478";
        int rspKeyLen = 48;
        String aliceKeyAgreement = ECC.eccKeyAgreement("1.2.840.10045.3.1.7", bobPublicKeyHex, alicePrivateKeyDHex,
                share_info, rspKeyLen);
        System.out.println(aliceKeyAgreement);
    }

    @Test
    public void test_parsePublicKeyFromHex2() throws Exception {
        String alicePrivateKeyDHex = "00c2a49764da324cf75134ac274a055a927ad214566583818aa81eb5ec4bfe6950";

        String bobPublicKeyHex =
                "0478619F549A3D5E94B8AB615AB451D8FCCFBBD2B9E705E1D66289BA8B45F896435C5F8FE53B49FFF0DF5443D394B5A1AFFE3B610841081A2C8FB165AAE26783C7";

        byte[] aliceKeyAgreement = ECC.performKeyAgreement("1.2.840.10045.3.1.7", alicePrivateKeyDHex, bobPublicKeyHex);
        System.out.println(Hex.bytesToHex(aliceKeyAgreement));
    }
}
