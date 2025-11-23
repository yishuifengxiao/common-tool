package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.HexUtil;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class ECCTest {

    @Test
    public void test_all() {
        try {
            String originalData = "这是要签名的测试数据";

            // 您提供的参数
            String curveOID = "1.2.840.10045.3.1.7"; // secp256r1 曲线的 OID
            String publicKeyHex = "042B44482A1263864699D706A5290E45075EACF8437C89D4FB2AB60F9D5524CF49ECF860609BA8E920011FD2A8DE2B23D7085A7832B7EE6FC0E73F0B66E0212227";
            String privateKeyDHex = "B116849E496A79DCB5F2A1B0D10582B347831908256D5B71454B2FA103D8F507";

            // 1. 从已知参数构建密钥对
            KeyPair keyPair = ECC.createKeyPairFromComponents(curveOID, publicKeyHex, privateKeyDHex);
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            System.out.println("成功从提供的组件构建密钥对");
            System.out.println("使用的曲线OID: " + curveOID);

            // 2. 使用私钥签名
            byte[] signature = ECC.signData(privateKey, originalData.getBytes(StandardCharsets.UTF_8));
            String signatureBase64 = Base64.getEncoder().encodeToString(signature);
            System.out.println("签名结果 (Base64): " + signatureBase64);
            System.out.println("签名结果 (Hex): " + HexUtil.bytesToHex(signature));

            // 3. 使用公钥验签
            boolean isValid = ECC.verifySignature(publicKey,originalData.getBytes(StandardCharsets.UTF_8), signature);
            System.out.println("签名验证结果: " + isValid);

            // 4. 测试篡改数据的情况
            String tamperedData = "这是被篡改的数据";
            boolean isTamperedValid = ECC.verifySignature(publicKey,tamperedData.getBytes(StandardCharsets.UTF_8), signature);
            System.out.println("篡改数据验证结果: " + isTamperedValid);

            // 5. 验证密钥组件
            ECC.verifyKeyComponents(keyPair, publicKeyHex, privateKeyDHex);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 测试不同的曲线OID
     */
    @Test
    public void testDifferentOIDs() {
        try {
            String publicKeyHex = "042B44482A1263864699D706A5290E45075EACF8437C89D4FB2AB60F9D5524CF49ECF860609BA8E920011FD2A8DE2B23D7085A7832B7EE6FC0E73F0B66E0212227";
            String privateKeyDHex = "B116849E496A79DCB5F2A1B0D10582B347831908256D5B71454B2FA103D8F507";
            String data = "测试数据";

            // 测试不同的曲线OID
            String[] curveOIDs = {
                    "1.2.840.10045.3.1.7",  // secp256r1 (P-256)
                    "1.3.132.0.34",         // secp384r1
                    "1.3.132.0.35"          // secp521r1
            };

            for (String oid : curveOIDs) {
                try {
                    System.out.println("\n测试曲线OID: " + oid);
                    KeyPair keyPair = ECC.createKeyPairFromComponents(oid, publicKeyHex, privateKeyDHex);

                    // 尝试签名和验证
                    byte[] signature = ECC.signData(keyPair.getPrivate(), data.getBytes(StandardCharsets.UTF_8));
                    boolean isValid = ECC.verifySignature(keyPair.getPublic(), data.getBytes(StandardCharsets.UTF_8), signature);

                    System.out.println("使用OID " + oid + " 的签名验证: " + isValid);
                } catch (Exception e) {
                    System.out.println("OID " + oid + " 失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}