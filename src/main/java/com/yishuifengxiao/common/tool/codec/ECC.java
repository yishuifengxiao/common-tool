package com.yishuifengxiao.common.tool.codec;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Base64;

/**
 * ECC 签名算法工具
 *
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
public class ECC {
    /**
     * 常见ECC曲线信息
     */
    public static String getCurveInfo(String oid) {
        switch (oid) {
            case "1.2.840.10045.3.1.7":
                return "secp256r1 (NIST P-256) - 256位素数域";
            case "1.3.132.0.34":
                return "secp384r1 (NIST P-384) - 384位素数域";
            case "1.3.132.0.35":
                return "secp521r1 (NIST P-521) - 521位素数域";
            case "1.3.132.0.10":
                return "secp256k1 - 比特币使用";
            case "1.3.132.0.33":
                return "secp224r1 (NIST P-224) - 224位素数域";
            case "1.3.132.0.32":
                return "secp192r1 (NIST P-192) - 192位素数域";
            default:
                return "unknown curve oid: " + oid;
        }
    }

    /**
     * 从已知的曲线OID、公钥十六进制字符串和私钥D值创建ECC密钥对
     */
    public static KeyPair createKeyPairFromComponents(String curveOID, String publicKeyHex, String privateKeyDHex) throws Exception {
        // 根据OID获取椭圆曲线参数
        ECParameterSpec ecParameterSpec = getECParameterSpecFromOID(curveOID);

        // 解析公钥
        ECPublicKey publicKey = parsePublicKeyFromHex(publicKeyHex, ecParameterSpec);

        // 解析私钥
        ECPrivateKey privateKey = parsePrivateKeyFromHex(privateKeyDHex, ecParameterSpec);

        return new KeyPair(publicKey, privateKey);
    }

    /**
     * 根据OID获取椭圆曲线参数
     */
    private static ECParameterSpec getECParameterSpecFromOID(String oid) throws Exception {
        // 使用KeyPairGenerator来获取标准曲线的参数
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");

        // 直接使用OID初始化
        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(oid);
        keyPairGenerator.initialize(ecGenSpec);

        // 生成一个临时密钥对以获取曲线参数
        KeyPair tempKeyPair = keyPairGenerator.generateKeyPair();
        ECPublicKey tempPublicKey = (ECPublicKey) tempKeyPair.getPublic();

        return tempPublicKey.getParams();
    }

    /**
     * 从十六进制字符串解析公钥
     */
    private static ECPublicKey parsePublicKeyFromHex(String publicKeyHex, ECParameterSpec ecParameterSpec) throws Exception {
        // 移除可能的前缀
        if (publicKeyHex.startsWith("04")) {
            publicKeyHex = publicKeyHex.substring(2);
        }

        // 公钥格式应该是未压缩的: 04 + X + Y
        int keyLength = publicKeyHex.length();
        // 64字节 = 128个十六进制字符 (X和Y各32字节)
        if (keyLength != 128) {
            throw new IllegalArgumentException("无效的公钥长度: " + keyLength);
        }

        // 提取X和Y坐标
        String xHex = publicKeyHex.substring(0, 64);
        String yHex = publicKeyHex.substring(64);

        BigInteger x = new BigInteger(xHex, 16);
        BigInteger y = new BigInteger(yHex, 16);
        ECPoint publicKeyPoint = new ECPoint(x, y);

        // 构建公钥
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(publicKeyPoint, ecParameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
    }

    /**
     * 从十六进制字符串解析私钥
     */
    private static ECPrivateKey parsePrivateKeyFromHex(String privateKeyDHex, ECParameterSpec ecParameterSpec) throws Exception {
        BigInteger privateKeyD = new BigInteger(privateKeyDHex, 16);

        // 构建私钥
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyD, ecParameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }

    /**
     * 使用私钥对数据进行签名
     */
    public static byte[] signData(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * 使用公钥验证签名
     */
    public static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature verifySignature = Signature.getInstance("SHA256withECDSA");
        verifySignature.initVerify(publicKey);
        verifySignature.update(data);
        return verifySignature.verify(signature);
    }

    /**
     * 验证密钥组件是否正确
     */
    public static void verifyKeyComponents(KeyPair keyPair, String expectedPublicKeyHex, String expectedPrivateKeyDHex) throws Exception {
        ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();
        ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();

        ECPoint publicKeyPoint = ecPublicKey.getW();
        BigInteger publicKeyX = publicKeyPoint.getAffineX();
        BigInteger publicKeyY = publicKeyPoint.getAffineY();
        BigInteger privateKeyD = ecPrivateKey.getS();

        // 构建实际的公钥十六进制字符串
        String actualPublicKeyHex = "04" +
                toPaddedHex(publicKeyX, 64) +
                toPaddedHex(publicKeyY, 64);

        // 构建实际的私钥十六进制字符串
        String actualPrivateKeyHex = toPaddedHex(privateKeyD, 64);

        System.out.println("\n密钥组件验证:");
        System.out.println("期望公钥: " + expectedPublicKeyHex);
        System.out.println("实际公钥: " + actualPublicKeyHex);
        System.out.println("公钥匹配: " + expectedPublicKeyHex.equalsIgnoreCase(actualPublicKeyHex));

        System.out.println("期望私钥: " + expectedPrivateKeyDHex);
        System.out.println("实际私钥: " + actualPrivateKeyHex);
        System.out.println("私钥匹配: " + expectedPrivateKeyDHex.equalsIgnoreCase(actualPrivateKeyHex));

        // 获取曲线信息
        System.out.println("曲线参数: " + ecPublicKey.getParams().toString());
    }

    /**
     * 将BigInteger转换为固定长度的十六进制字符串
     */
    private static String toPaddedHex(BigInteger value, int length) {
        String hex = value.toString(16);
        while (hex.length() < length) {
            hex = "0" + hex;
        }
        return hex;
    }


    /**
     * 使用提供的密钥对数据进行签名
     */
    public static String sign(String data, String curveOID, String publicKeyHex, String privateKeyDHex) throws Exception {
        KeyPair keyPair = createKeyPairFromComponents(curveOID, publicKeyHex, privateKeyDHex);
        byte[] signature = signData(data.getBytes("UTF-8"), keyPair.getPrivate());
        return Base64.getEncoder().encodeToString(signature);
    }

    /**
     * 验证签名
     */
    public static boolean verify(String data, String signatureBase64, String curveOID, String publicKeyHex) throws Exception {
        // 创建一个虚拟私钥来构建密钥对
        String dummyPrivateKey = "0000000000000000000000000000000000000000000000000000000000000001";
        KeyPair keyPair = createKeyPairFromComponents(curveOID, publicKeyHex, dummyPrivateKey);
        byte[] signature = Base64.getDecoder().decode(signatureBase64);
        return verifySignature(data.getBytes("UTF-8"), signature, keyPair.getPublic());
    }
}
