package com.yishuifengxiao.common.tool.codec;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSATest {
    public static void main(String[] args) {
        // 生成密钥对
        KeyPair keyPair = RSA.generateKeyPair(2048);
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 原始数据
        String originalData = "Hello, RSA加密测试! 这是一段需要加密的敏感信息。";

        System.out.println("=== RSA加解密测试 ===");
        System.out.println("原始数据: " + originalData);

        // 加密
        String encryptedData = RSA.encrypt(originalData, publicKey);
        System.out.println("加密后: " + encryptedData);

        // 解密
        String decryptedData = RSA.decrypt(encryptedData, privateKey);
        System.out.println("解密后: " + decryptedData);

        // 验证加解密是否成功
        System.out.println("加解密成功: " + originalData.equals(decryptedData));

        // 测试密钥保存和恢复
        testKeyPersistence(originalData);


    }

    /**
     * 测试密钥持久化功能
     */
    private static void testKeyPersistence(String testData) {
        System.out.println("\n=== 密钥持久化测试 ===");

        // 生成新密钥对
        KeyPair keyPair = RSA.generateKeyPair(2048);

        // 将密钥转换为字符串（可以保存到文件或数据库）
        String publicKeyStr = RSA.publicKeyToString(keyPair.getPublic());
        String privateKeyStr = RSA.privateKeyToString(keyPair.getPrivate());

        System.out.println("公钥字符串长度: " + publicKeyStr.length());
        System.out.println("私钥字符串长度: " + privateKeyStr.length());

        // 从字符串恢复密钥
        PublicKey restoredPublicKey = RSA.stringToPublicKey(publicKeyStr);
        PrivateKey restoredPrivateKey = RSA.stringToPrivateKey(privateKeyStr);

        // 使用恢复的密钥进行加解密
        String encrypted = RSA.encrypt(testData, restoredPublicKey);
        String decrypted = RSA.decrypt(encrypted, restoredPrivateKey);

        System.out.println("密钥持久化测试成功: " + testData.equals(decrypted));
    }


}