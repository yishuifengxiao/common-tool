package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;
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
            java.security.PrivateKey privateKey = keyPair.getPrivate();
            java.security.PublicKey publicKey = keyPair.getPublic();

            System.out.println("成功从提供的组件构建密钥对");
            System.out.println("使用的曲线OID: " + curveOID);

            // 2. 使用私钥签名（返回固定64字节的签名）
            byte[] signature = ECC.sign(privateKey, originalData.getBytes(StandardCharsets.UTF_8));
            String signatureHex = Hex.bytesToHex(signature).toUpperCase();
            System.out.println("签名结果 (Hex): " + signatureHex);
            System.out.println("签名长度: " + signatureHex.length() + " 字符（应为128字符）");

            // 3. 使用公钥验签（传入固定长度签名）
            boolean isValid = ECC.verifySignature(publicKey, originalData.getBytes(StandardCharsets.UTF_8), signature);
            System.out.println("签名验证结果: " + isValid);

            // 4. 测试篡改数据的情况
            String tamperedData = "这是被篡改的数据";
            boolean isTamperedValid = ECC.verifySignature(publicKey, tamperedData.getBytes(StandardCharsets.UTF_8), signature);
            System.out.println("篡改数据验证结果: " + isTamperedValid);

            // 5. 验证密钥组件
            ECC.verifyKeyComponents(keyPair, publicKeyHex, privateKeyDHex);

            // 6. 测试signHex方法（十六进制数据签名）
            String hexData = Hex.bytesToHex(originalData.getBytes(StandardCharsets.UTF_8)).toUpperCase();
            String signatureHex2 = ECC.signHex(curveOID, privateKeyDHex, hexData);
            System.out.println("\nsignHex方法签名结果: " + signatureHex2);
            System.out.println("signHex签名长度: " + signatureHex2.length() + " 字符（应为128字符）");

            // 7. 测试verify方法（证书验签）
            boolean isValid2 = ECC.verify(curveOID, publicKeyHex, hexData, signatureHex2);
            System.out.println("verify方法验证结果: " + isValid2);

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

                    // 尝试签名和验证（使用固定长度签名）
                    byte[] signature = ECC.sign(keyPair.getPrivate(), data.getBytes(StandardCharsets.UTF_8));
                    String signatureHex = Hex.bytesToHex(signature).toUpperCase();
                    System.out.println("签名长度: " + signatureHex.length() + " 字符");
                    
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

    /**
     * 测试签名和验签的完整流程
     */
    @Test
    public void testSignAndVerifyFlow() {
        try {
            // 生成密钥对
            KeyPair keyPair = ECC.generateECCKeyPair();
            java.security.PrivateKey privateKey = keyPair.getPrivate();
            java.security.PublicKey publicKey = keyPair.getPublic();

            String testData = "Hello, ECC Signature!";
            byte[] dataBytes = testData.getBytes(StandardCharsets.UTF_8);

            // 签名
            byte[] signature = ECC.sign(privateKey, dataBytes);
            String signatureHex = Hex.bytesToHex(signature).toUpperCase();
            
            System.out.println("原始数据: " + testData);
            System.out.println("签名 (Hex): " + signatureHex);
            System.out.println("签名长度: " + signatureHex.length() + " 字符");

            // 验证正确签名
            boolean validResult = ECC.verifySignature(publicKey, dataBytes, signature);
            System.out.println("正确签名验证: " + validResult);

            // 验证错误数据
            String wrongData = "Wrong data";
            boolean invalidResult = ECC.verifySignature(publicKey, wrongData.getBytes(StandardCharsets.UTF_8), signature);
            System.out.println("错误数据验证: " + invalidResult);

            // 验证错误签名
            byte[] wrongSignature = new byte[64];
            boolean wrongSigResult = ECC.verifySignature(publicKey, dataBytes, wrongSignature);
            System.out.println("错误签名验证: " + wrongSigResult);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试固定长度签名格式转换
     */
    @Test
    public void testFixedLengthSignatureFormat() {
        try {
            // 生成密钥对
            KeyPair keyPair = ECC.generateECCKeyPair();
            java.security.PrivateKey privateKey = keyPair.getPrivate();
            java.security.PublicKey publicKey = keyPair.getPublic();

            String testData = "Test fixed length signature format";
            byte[] dataBytes = testData.getBytes(StandardCharsets.UTF_8);

            // 签名
            byte[] signature = ECC.sign(privateKey, dataBytes);
            String signatureHex = Hex.bytesToHex(signature).toUpperCase();

            // 验证签名长度为128字符
            if (signatureHex.length() != 128) {
                throw new AssertionError("签名长度应为128字符，实际为: " + signatureHex.length());
            }
            System.out.println("签名长度验证通过: " + signatureHex.length() + " 字符");

            // 验证R和S各为64字符
            String rPart = signatureHex.substring(0, 64);
            String sPart = signatureHex.substring(64);
            System.out.println("R部分: " + rPart);
            System.out.println("S部分: " + sPart);

            // 验签
            boolean isValid = ECC.verifySignature(publicKey, dataBytes, signature);
            System.out.println("签名验证结果: " + isValid);

            if (!isValid) {
                throw new AssertionError("签名验证失败");
            }

            System.out.println("固定长度签名格式测试通过");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}