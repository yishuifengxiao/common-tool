package com.yishuifengxiao.common.tool.codec;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class KeyPairExample {
    public static void main(String[] args) {
        try {
            // 生成 RSA 密钥对
            System.out.println("生成 RSA 密钥对...");
            KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance("RSA");
            rsaKeyGen.initialize(2048);
            KeyPair rsaKeyPair = rsaKeyGen.generateKeyPair();
            KeyPairHelper.printKeyPairDetails(rsaKeyPair);

            // 生成 DSA 密钥对
            System.out.println("生成 DSA 密钥对...");
            KeyPairGenerator dsaKeyGen = KeyPairGenerator.getInstance("DSA");
            dsaKeyGen.initialize(1024);
            KeyPair dsaKeyPair = dsaKeyGen.generateKeyPair();
            KeyPairHelper.printKeyPairDetails(dsaKeyPair);

            // 生成 EC 密钥对
            System.out.println("生成 EC 密钥对...");
            KeyPairGenerator ecKeyGen = KeyPairGenerator.getInstance("EC");
            ecKeyGen.initialize(256);
            KeyPair ecKeyPair = ecKeyGen.generateKeyPair();
            KeyPairHelper.printKeyPairDetails(ecKeyPair);

            // 单独获取 Hex 值的示例
            System.out.println("单独获取 Hex 值示例:");
            String rsaPublicHex = KeyPairHelper.getPublicKeyHex(rsaKeyPair);
            String rsaPrivateDHex = KeyPairHelper.getPrivateKeyDHex(rsaKeyPair);
            
            System.out.println("RSA 公钥 Hex (前64字符): " + 
                (rsaPublicHex.length() > 64 ? rsaPublicHex.substring(0, 64) + "..." : rsaPublicHex));
            System.out.println("RSA 私钥 D 值 Hex: " + 
                (rsaPrivateDHex.length() > 64 ? rsaPrivateDHex.substring(0, 64) + "..." : rsaPrivateDHex));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}