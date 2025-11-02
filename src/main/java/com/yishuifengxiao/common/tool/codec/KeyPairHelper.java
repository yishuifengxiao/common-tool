package com.yishuifengxiao.common.tool.codec;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.*;

public class KeyPairHelper {

    /**
     * 从 KeyPair 获取公钥的 Hex 值
     */
    public static String getPublicKeyHex(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();

        if (publicKey instanceof RSAPublicKey) {
            return getRSAPublicKeyHex((RSAPublicKey) publicKey);
        } else if (publicKey instanceof DSAPublicKey) {
            return getDSAPublicKeyHex((DSAPublicKey) publicKey);
        } else if (publicKey instanceof ECPublicKey) {
            return getECPublicKeyHex((ECPublicKey) publicKey);
        } else {
            // 通用方法：获取编码后的字节数组
            return bytesToHex(publicKey.getEncoded());
        }
    }

    /**
     * 从 KeyPair 获取私钥 D 值的 Hex 值
     */
    public static String getPrivateKeyDHex(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();

        if (privateKey instanceof RSAPrivateKey) {
            return getRSAPrivateKeyDHex((RSAPrivateKey) privateKey);
        } else if (privateKey instanceof DSAPrivateKey) {
            return getDSAPrivateKeyDHex((DSAPrivateKey) privateKey);
        } else if (privateKey instanceof ECPrivateKey) {
            return getECPrivateKeyDHex((ECPrivateKey) privateKey);
        } else {
            throw new UnsupportedOperationException("不支持的私钥类型: " + privateKey.getAlgorithm());
        }
    }

    /**
     * 获取 RSA 公钥的 Hex 值（模数 N）
     */
    private static String getRSAPublicKeyHex(RSAPublicKey rsaPublicKey) {
        BigInteger modulus = rsaPublicKey.getModulus();
        return bigIntegerToHex(modulus);
    }

    /**
     * 获取 RSA 私钥 D 值的 Hex 值
     */
    private static String getRSAPrivateKeyDHex(RSAPrivateKey rsaPrivateKey) {
        // 对于 RSA 私钥，D 值是私有指数
        BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();
        return bigIntegerToHex(privateExponent);
    }

    /**
     * 获取 DSA 公钥的 Hex 值（公钥 Y）
     */
    private static String getDSAPublicKeyHex(DSAPublicKey dsaPublicKey) {
        BigInteger y = dsaPublicKey.getY();
        return bigIntegerToHex(y);
    }

    /**
     * 获取 DSA 私钥 D 值的 Hex 值（私钥 X）
     */
    private static String getDSAPrivateKeyDHex(DSAPrivateKey dsaPrivateKey) {
        BigInteger x = dsaPrivateKey.getX();
        return bigIntegerToHex(x);
    }

    /**
     * 获取 EC 公钥的 Hex 值（编码后的点）
     */
    private static String getECPublicKeyHex(ECPublicKey ecPublicKey) {
        // 获取椭圆曲线上的点
        java.security.spec.ECPoint point = ecPublicKey.getW();
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();

        // 转换为未压缩格式: 04 + X + Y
        // 使用固定长度的十六进制表示，每个坐标32字节（64字符）
        String xHex = bigIntegerToFixedLengthHex(x, 32);
        String yHex = bigIntegerToFixedLengthHex(y, 32);

        return "04" + xHex + yHex;
    }

    /**
     * 将 BigInteger 转换为固定长度的十六进制字符串
     */
    private static String bigIntegerToFixedLengthHex(BigInteger bigInt, int targetBytes) {
        // 获取BigInteger的字节数组表示
        byte[] bytes = bigInt.toByteArray();

        // 处理符号位：如果第一个字节是0（表示正数且最高位为0），则移除它
        if (bytes.length > targetBytes && bytes[0] == 0) {
            byte[] trimmedBytes = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmedBytes, 0, trimmedBytes.length);
            bytes = trimmedBytes;
        }

        // 如果字节数仍然大于目标长度，截取后面的部分
        if (bytes.length > targetBytes) {
            byte[] result = new byte[targetBytes];
            System.arraycopy(bytes, bytes.length - targetBytes, result, 0, targetBytes);
            bytes = result;
        }
        // 如果字节数小于目标长度，前面补零
        else if (bytes.length < targetBytes) {
            byte[] result = new byte[targetBytes];
            System.arraycopy(bytes, 0, result, targetBytes - bytes.length, bytes.length);
            // 前面的字节保持为0（默认值）
            bytes = result;
        }

        return bytesToHex(bytes);
    }


    /**
     * 获取 EC 私钥 D 值的 Hex 值（私钥 S）
     */
    private static String getECPrivateKeyDHex(ECPrivateKey ecPrivateKey) {
        BigInteger s = ecPrivateKey.getS();
        return bigIntegerToHex(s);
    }

    /**
     * 将 BigInteger 转换为 Hex 字符串
     */
    private static String bigIntegerToHex(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();
        return bytesToHex(bytes);
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 获取密钥对详细信息
     */
    public static void printKeyPairDetails(KeyPair keyPair) {
        System.out.println("=== 密钥对详细信息 ===");
        System.out.println("算法: " + keyPair.getPublic().getAlgorithm());
        System.out.println("格式: " + keyPair.getPublic().getFormat());

        System.out.println("\n--- 公钥 ---");
        String publicKeyHex = getPublicKeyHex(keyPair);
        System.out.println("Hex 值: " + publicKeyHex);
        System.out.println("长度: " + publicKeyHex.length() + " 字符 (" + (publicKeyHex.length() / 2) + " 字节)");

        System.out.println("\n--- 私钥 ---");
        String privateKeyDHex = getPrivateKeyDHex(keyPair);
        System.out.println("D 值 Hex: " + privateKeyDHex);
        System.out.println("长度: " + privateKeyDHex.length() + " 字符 (" + (privateKeyDHex.length() / 2) + " 字节)");

        System.out.println("========================\n");
    }
}