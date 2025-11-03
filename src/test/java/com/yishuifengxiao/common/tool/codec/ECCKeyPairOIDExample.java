package com.yishuifengxiao.common.tool.codec;

import java.security.KeyPair;
import java.security.SecureRandom;

public class ECCKeyPairOIDExample {
    
    public static void main(String[] args) {
        System.out.println("=== 根据ECC曲线OID生成密钥对示例 ===");
        
        // 常见ECC曲线OID
        String[] commonOIDs = {
            "1.2.840.10045.3.1.7",  // P-256 (secp256r1)
            "1.3.132.0.34",         // P-384 (secp384r1) 
            "1.3.132.0.35",         // P-521 (secp521r1)
            "1.3.132.0.10"          // secp256k1 (比特币曲线)
        };
        
        // 使用普通方法生成
        System.out.println("\n1. 使用普通方法生成:");
        for (String oid : commonOIDs) {
            try {
                KeyPair keyPair = KeyPairHelper.generateECCKeyPairByOID(oid);
                String curveName = KeyPairHelper.getCurveNameByOID(oid);
                System.out.println("OID: " + oid + " -> " + curveName);
                KeyPairHelper.printKeyInfo(keyPair);
                System.out.println();
            } catch (Exception e) {
                System.out.println("生成 " + oid + " 失败: " + e.getMessage());
            }
        }
        
        // 使用安全随机数生成
        System.out.println("\n2. 使用安全随机数生成:");
        try {
            SecureRandom secureRandom = new SecureRandom();
            KeyPair keyPair = KeyPairHelper.generateSecureECCKeyPairByOID(
                "1.2.840.10045.3.1.7", secureRandom);
            System.out.println("使用安全随机数生成的P-256密钥对:");
            KeyPairHelper.printKeyInfo(keyPair);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 显示支持的曲线
        System.out.println("\n3. 支持的ECC曲线OID:");
        String[] supportedOIDs = KeyPairHelper.getSupportedECCurveOIDs();
        for (String oid : supportedOIDs) {
            String name = KeyPairHelper.getCurveNameByOID(oid);
            System.out.println("  " + oid + " -> " + name);
        }
        
        // 生成并显示Base64编码的密钥
        System.out.println("\n4. Base64编码的密钥示例:");
        KeyPair keyPair = KeyPairHelper.generateECCKeyPairByOID("1.2.840.10045.3.1.7");
        System.out.println("公钥Base64:");
        System.out.println(KeyPairHelper.encodeKeyToBase64(keyPair.getPublic()));
        System.out.println("私钥Base64:");
        System.out.println(KeyPairHelper.encodeKeyToBase64(keyPair.getPrivate()));

        System.out.println("生成RSA 2048位密钥对...");
        KeyPair rsaKeyPair = KeyPairHelper.generateRSAKeyPair(2048);
        KeyPairHelper.printKeyDetails(rsaKeyPair);
        System.out.println();

        System.out.println("生成ECC secp256r1密钥对...");
        KeyPair eccKeyPair = KeyPairHelper.generateECCKeyPair("secp256r1");
        KeyPairHelper. printKeyDetails(eccKeyPair);
        System.out.println();

        System.out.println("生成DSA 2048位密钥对...");
        KeyPair dsaKeyPair = KeyPairHelper.generateDSAKeyPair(2048);
        KeyPairHelper. printKeyDetails(dsaKeyPair);
    }
}