package com.yishuifengxiao.common.tool.codec;

import javax.crypto.KeyAgreement;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

/**
 * ECC密钥协商工具类
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class EccKeyAgreement {
    /**
     * 执行ECC密钥协商算法，生成共享密钥
     *
     * @param curveOID       椭圆曲线OID标识符
     * @param publicKeyHex   对方公钥的十六进制字符串表示
     * @param privateKeyDHex 己方私钥的十六进制字符串表示
     * @param sShareInfo     共享信息（十六进制字符串或普通字符串）
     * @param iKeyLen        期望生成的密钥长度（字节数）
     * @param isHexShareInfo 共享信息是否为十六进制格式
     * @return 派生出的共享密钥的十六进制字符串表示
     * @throws Exception 当密钥协商或哈希计算过程中发生错误时抛出
     */
    public static String eccKeyAgreement(String curveOID, String publicKeyHex, String privateKeyDHex,
                                         String sShareInfo, int iKeyLen, boolean isHexShareInfo) throws Exception {
        // 1. 根据曲线OID获取EC参数
        ECParameterSpec ecParams = getECParameterSpec(curveOID);

        // 2. 解析对方公钥（未压缩格式，以04开头）
        byte[] publicKeyBytes = hexStringToByteArray(publicKeyHex);
        if (publicKeyBytes[0] != 0x04) {
            throw new IllegalArgumentException("Unsupported public key format, expected uncompressed (04)");
        }
        int keyLen = (ecParams.getCurve().getField().getFieldSize() + 7) / 8; // 曲线字节长度，P-256为32
        byte[] xBytes = Arrays.copyOfRange(publicKeyBytes, 1, 1 + keyLen);
        byte[] yBytes = Arrays.copyOfRange(publicKeyBytes, 1 + keyLen, 1 + 2 * keyLen);
        BigInteger x = new BigInteger(1, xBytes);
        BigInteger y = new BigInteger(1, yBytes);
        ECPoint publicPoint = new ECPoint(x, y);
        PublicKey publicKey = KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(publicPoint, ecParams));

        // 3. 解析己方私钥
        BigInteger d = new BigInteger(1, hexStringToByteArray(privateKeyDHex));
        PrivateKey privateKey = KeyFactory.getInstance("EC").generatePrivate(new ECPrivateKeySpec(d, ecParams));

        // 4. ECDH密钥协商，得到共享秘密（原始X坐标，大端表示）
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret(); // 长度为keyLen的字节数组（P-256为32）

        // 5. 使用ANSI X9.63 KDF（基于SHA-256）派生指定长度的密钥
        byte[] sharedInfo;
        if (isHexShareInfo) {
            // 如果共享信息是十六进制字符串，转换为字节数组
            sharedInfo = hexStringToByteArray(sShareInfo);
        } else {
            // 否则作为UTF-8字符串处理
            sharedInfo = sShareInfo.getBytes(StandardCharsets.UTF_8);
        }
        byte[] derivedKey = kdf(sharedSecret, sharedInfo, iKeyLen);

        // 6. 返回十六进制字符串
        return bytesToHex(derivedKey);
    }

    /**
     * 执行ECC密钥协商算法，生成共享密钥（共享信息作为HEX字符串）
     *
     * @param curveOID       椭圆曲线OID标识符
     * @param publicKeyHex   对方公钥的十六进制字符串表示
     * @param privateKeyDHex 己方私钥的十六进制字符串表示
     * @param sShareInfo     共享信息字符串，用于密钥派生
     * @param iKeyLen        期望生成的密钥长度（字节数）
     * @return 派生出的共享密钥的十六进制字符串表示
     * @throws Exception 当密钥协商或哈希计算过程中发生错误时抛出
     */
    public static String eccKeyAgreement(String curveOID, String publicKeyHex, String privateKeyDHex,
                                         String sShareInfo, int iKeyLen) throws Exception {
        return eccKeyAgreement(curveOID, publicKeyHex, privateKeyDHex, sShareInfo, iKeyLen, true);
    }

    /**
     * 根据OID获取椭圆曲线参数（支持常见曲线，原生Java无OID直接映射，需手动转换）
     */
    private static ECParameterSpec getECParameterSpec(String curveOID) throws Exception {
        String curveName;
        switch (curveOID) {
            case "1.2.840.10045.3.1.7": // NIST P-256 / secp256r1
                curveName = "secp256r1";
                break;
            case "1.3.132.0.34": // NIST P-384 / secp384r1
                curveName = "secp384r1";
                break;
            case "1.3.132.0.35": // NIST P-521 / secp521r1
                curveName = "secp521r1";
                break;
            default:
                throw new UnsupportedOperationException("Unsupported curve OID: " + curveOID);
        }
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec(curveName));
        return parameters.getParameterSpec(ECParameterSpec.class);
    }

    /**
     * ANSI X9.63 KDF with SHA-256
     *
     * @param Z          共享秘密（字节数组）
     * @param SharedInfo 共享信息（字节数组）
     * @param keyLen     派生密钥长度（字节）
     * @return 派生密钥字节数组
     * @throws NoSuchAlgorithmException
     */
    private static byte[] kdf(byte[] Z, byte[] SharedInfo, int keyLen) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        int hashLen = sha256.getDigestLength(); // 32
        int iterations = (keyLen + hashLen - 1) / hashLen;
        byte[] result = new byte[keyLen];
        byte[] counter = new byte[4]; // 大端计数器
        int offset = 0;

        for (int i = 1; i <= iterations; i++) {
            // 设置计数器（大端）
            counter[0] = (byte) ((i >> 24) & 0xFF);
            counter[1] = (byte) ((i >> 16) & 0xFF);
            counter[2] = (byte) ((i >> 8) & 0xFF);
            counter[3] = (byte) (i & 0xFF);

            sha256.reset();
            sha256.update(Z);
            sha256.update(counter);
            sha256.update(SharedInfo);
            byte[] hash = sha256.digest();

            int copyLen = Math.min(hashLen, keyLen - offset);
            System.arraycopy(hash, 0, result, offset, copyLen);
            offset += copyLen;
        }
        return result;
    }

    // 辅助方法：十六进制字符串转字节数组
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    // 辅助方法：字节数组转十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }


}
