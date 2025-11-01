package com.yishuifengxiao.common.tool.codec;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Base64;
import java.util.regex.Pattern;

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

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9A-Fa-f]+$");
    private static final String PEM_HEADER_EC = "-----BEGIN EC PRIVATE KEY-----";
    private static final String PEM_FOOTER_EC = "-----END EC PRIVATE KEY-----";
    private static final String PEM_HEADER_PKCS8 = "-----BEGIN PRIVATE KEY-----";
    private static final String PEM_FOOTER_PKCS8 = "-----END PRIVATE KEY-----";
    private static final String PEM_HEADER_PKCS8_ENC = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
    private static final String PEM_FOOTER_PKCS8_ENC = "-----END ENCRYPTED PRIVATE KEY-----";

    /**
     * 从EC私钥数据中提取私钥的D值
     *
     * @param keyData 私钥数据字符串，可以是十六进制、base64编码或PEM格式
     * @return 提取的私钥D值（大写十六进制字符串，固定64个字符/32字节）
     * @throws Exception 提取过程中发生的错误
     */
    public static String extractPrivateDValue(String keyData) throws Exception {
        // 验证输入不为空
        if (keyData == null || keyData.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty private key data");
        }

        keyData = keyData.trim();
        // 移除 EC PARAMETERS 部分（包括标记和内容）

        // 查找并移除 EC PARAMETERS 部分
        int beginParamsIndex = keyData.indexOf("-----BEGIN EC PARAMETERS-----");
        int endParamsIndex = keyData.indexOf("-----END EC PARAMETERS-----");
        if (beginParamsIndex != -1 && endParamsIndex != -1) {
            // 移除整个 EC PARAMETERS 部分（包括换行符）
            int endIndex = keyData.indexOf("\n", endParamsIndex);
            if (endIndex == -1) {
                endIndex = endParamsIndex + "-----END EC PARAMETERS-----".length();
            } else {
                endIndex += 1; // 包含换行符
            }
            keyData = keyData.substring(0, beginParamsIndex) +
                    keyData.substring(endIndex);
        }

        // 然后处理 EC PRIVATE KEY 部分
        keyData = keyData
                .replace("-----BEGIN EC PRIVATE KEY-----", "")
                .replace("-----END EC PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // 使用优化的parseECPrivateKey函数解析私钥
        ECPrivateKey privateKey = parseECPrivateKey(keyData);
        if (privateKey == null) {
            throw new Exception("Failed to parse private key from provided data");
        }

        // 获取私钥的D值并转换为十六进制字符串
        BigInteger d = privateKey.getS();
        String dVal = d.toString(16).toUpperCase();

        // 验证D值长度，确保是标准32字节（64个十六进制字符）
        if (dVal.length() != 64) {
            // 对于长度不足的情况，在左侧补零
            if (dVal.length() < 64) {
                dVal = String.format("%64s", dVal).replace(' ', '0');
            } else {
                throw new Exception("Invalid private key D value length: expected 64 hex characters, got " + dVal.length());
            }
        }

        // 验证D值是否为有效的十六进制字符串（额外安全检查）
        if (!HEX_PATTERN.matcher(dVal).matches()) {
            throw new Exception("Invalid hex format in private key D value");
        }

        return dVal;
    }

    /**
     * 解析EC私钥数据，支持多种格式的输入
     *
     * @param keyData 私钥数据字符串，可以是十六进制、base64编码或PEM格式
     * @return 解析成功的ECDSA私钥对象
     * @throws Exception 解析过程中发生的错误
     */
    public static ECPrivateKey parseECPrivateKey(String keyData) throws Exception {
        // 清理输入数据
        keyData = keyData.trim();

        // 尝试作为十六进制字符串解析
        try {
            byte[] hexKey = parseHexString(keyData);
            if (hexKey != null) {
                ECPrivateKey privateKey = parseECPrivateKeyFromBytes(hexKey);
                if (privateKey != null) {
                    return privateKey;
                }
            }
        } catch (Exception e) {
            // 继续尝试其他格式
        }

        // 尝试作为base64字符串直接解析
        try {
            byte[] base64Key = parseBase64String(keyData);
            if (base64Key != null) {
                ECPrivateKey privateKey = parseECPrivateKeyFromBytes(base64Key);
                if (privateKey != null) {
                    return privateKey;
                }
            }
        } catch (Exception e) {
            // 继续尝试其他格式
        }

        // 尝试作为PEM格式解析
        try {
            ECPrivateKey privateKey = parsePEMPrivateKey(keyData);
            if (privateKey != null) {
                return privateKey;
            }
        } catch (Exception e) {
            throw new Exception("Failed to parse private key in any format: " + e.getMessage(), e);
        }

        throw new Exception("Unable to parse private key from provided data");
    }

    /**
     * 解析十六进制字符串
     */
    private static byte[] parseHexString(String hexString) {
        String cleanHex = hexString.replaceAll("\\s", "").replace("0x", "").replace("0X", "");

        if (!HEX_PATTERN.matcher(cleanHex).matches() || cleanHex.length() % 2 != 0) {
            return null;
        }

        try {
            byte[] result = new byte[cleanHex.length() / 2];
            for (int i = 0; i < cleanHex.length(); i += 2) {
                String byteStr = cleanHex.substring(i, i + 2);
                result[i / 2] = (byte) Integer.parseInt(byteStr, 16);
            }
            return result;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析Base64字符串
     */
    private static byte[] parseBase64String(String base64String) {
        // 移除可能的PEM头部和尾部（如果存在）
        String cleanBase64 = base64String
                .replace(PEM_HEADER_EC, "")
                .replace(PEM_FOOTER_EC, "")
                .replace(PEM_HEADER_PKCS8, "")
                .replace(PEM_FOOTER_PKCS8, "")
                .replace(PEM_HEADER_PKCS8_ENC, "")
                .replace(PEM_FOOTER_PKCS8_ENC, "")
                .replaceAll("\\s", "");

        try {
            return Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            // 尝试MIME类型的Base64解码（可能包含换行符）
            try {
                return Base64.getMimeDecoder().decode(cleanBase64);
            } catch (IllegalArgumentException e2) {
                return null;
            }
        }
    }

    /**
     * 从字节数组解析EC私钥
     */
    private static ECPrivateKey parseECPrivateKeyFromBytes(byte[] keyBytes) throws Exception {
        if (keyBytes == null || keyBytes.length == 0) {
            return null;
        }

        // 首先尝试解析为PKCS#8格式
        try {
            return parsePKCS8PrivateKey(keyBytes);
        } catch (Exception e) {
            // PKCS#8解析失败，继续尝试其他格式
        }

        // 尝试解析为SEC1格式（传统EC私钥格式）
        try {
            return parseSEC1PrivateKey(keyBytes);
        } catch (Exception e) {
            // SEC1解析失败
        }

        throw new Exception("Unable to parse private key from bytes - neither PKCS#8 nor SEC1 format recognized");
    }

    /**
     * 解析PKCS#8格式私钥
     */
    private static ECPrivateKey parsePKCS8PrivateKey(byte[] keyBytes) throws Exception {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            if (privateKey instanceof ECPrivateKey) {
                return (ECPrivateKey) privateKey;
            } else {
                throw new Exception("Private key is not an EC private key");
            }
        } catch (Exception e) {
            throw new Exception("Failed to parse PKCS8 private key: " + e.getMessage(), e);
        }
    }

    /**
     * 解析SEC1格式私钥（传统EC私钥格式）
     * 注意：Java原生不支持直接解析SEC1格式，这里需要手动解析ASN.1结构
     */
    private static ECPrivateKey parseSEC1PrivateKey(byte[] keyBytes) throws Exception {
        try {
            // SEC1格式的EC私钥ASN.1结构：
            // ECPrivateKey ::= SEQUENCE {
            //   version INTEGER { ecPrivkeyVer1(1) } (ecPrivkeyVer1),
            //   privateKey OCTET STRING,
            //   parameters [0] ECParameters OPTIONAL,
            //   publicKey [1] BIT STRING OPTIONAL
            // }

            // 简单的ASN.1解析来提取私钥D值
            BigInteger dValue = parseSEC1DValue(keyBytes);
            if (dValue != null) {
                // 使用标准的P-256曲线参数创建私钥
                // 注意：这里假设曲线是P-256，实际应用中可能需要根据证书确定曲线
                return createECPrivateKey(dValue);
            }

            throw new Exception("Failed to extract D value from SEC1 private key");
        } catch (Exception e) {
            throw new Exception("Failed to parse SEC1 private key: " + e.getMessage(), e);
        }
    }

    /**
     * 从SEC1格式字节数组中提取D值
     */
    private static BigInteger parseSEC1DValue(byte[] keyBytes) throws Exception {
        if (keyBytes == null || keyBytes.length < 10) {
            return null;
        }

        // 简单的ASN.1解析
        int pos = 0;

        // 检查SEQUENCE标签 (0x30)
        if (keyBytes[pos++] != 0x30) {
            throw new Exception("Invalid SEC1 format: expected SEQUENCE tag");
        }

        // 读取长度
        int length = keyBytes[pos++] & 0xFF;
        if (length > 0x80) {
            // 长格式长度
            int lengthBytes = length - 0x80;
            length = 0;
            for (int i = 0; i < lengthBytes; i++) {
                length = (length << 8) | (keyBytes[pos++] & 0xFF);
            }
        }

        // 检查版本 INTEGER (0x02)
        if (keyBytes[pos++] != 0x02) {
            throw new Exception("Invalid SEC1 format: expected INTEGER tag for version");
        }

        // 读取版本长度并跳过版本值（应该是1）
        int versionLength = keyBytes[pos++] & 0xFF;
        pos += versionLength;

        // 检查私钥 OCTET STRING (0x04)
        if (keyBytes[pos++] != 0x04) {
            throw new Exception("Invalid SEC1 format: expected OCTET STRING tag for private key");
        }

        // 读取私钥长度
        int privateKeyLength = keyBytes[pos++] & 0xFF;
        if (privateKeyLength > 0x80) {
            // 长格式长度
            int lengthBytes = privateKeyLength - 0x80;
            privateKeyLength = 0;
            for (int i = 0; i < lengthBytes; i++) {
                privateKeyLength = (privateKeyLength << 8) | (keyBytes[pos++] & 0xFF);
            }
        }

        // 提取私钥字节
        if (pos + privateKeyLength > keyBytes.length) {
            throw new Exception("Invalid SEC1 format: private key data truncated");
        }

        byte[] privateKeyBytes = new byte[privateKeyLength];
        System.arraycopy(keyBytes, pos, privateKeyBytes, 0, privateKeyLength);

        // 将私钥字节转换为BigInteger
        return new BigInteger(1, privateKeyBytes); // 使用1确保为正数
    }

    /**
     * 解析PEM格式私钥
     */
    private static ECPrivateKey parsePEMPrivateKey(String pemData) throws Exception {
        String processedPem = pemData;

        // 检测PEM类型并确保有正确的头部
        String pemUpper = pemData.toUpperCase();
        boolean hasPemHeader = pemUpper.contains("-----BEGIN");

        if (!hasPemHeader) {
            // 如果没有头部，假设是EC PRIVATE KEY格式
            processedPem = PEM_HEADER_EC + "\n" + pemData + "\n" + PEM_FOOTER_EC;
        }

        // 提取Base64部分
        String base64Part = extractBase64FromPEM(processedPem);
        if (base64Part.isEmpty()) {
            throw new Exception("No Base64 data found in PEM format");
        }

        byte[] keyBytes = Base64.getDecoder().decode(base64Part);
        return parseECPrivateKeyFromBytes(keyBytes);
    }

    /**
     * 从PEM字符串中提取Base64部分
     */
    private static String extractBase64FromPEM(String pemData) {
        // 移除所有PEM头部和尾部
        String cleanData = pemData
                .replace(PEM_HEADER_EC, "")
                .replace(PEM_FOOTER_EC, "")
                .replace(PEM_HEADER_PKCS8, "")
                .replace(PEM_FOOTER_PKCS8, "")
                .replace(PEM_HEADER_PKCS8_ENC, "")
                .replace(PEM_FOOTER_PKCS8_ENC, "")
                .replaceAll("\\s", "");

        return cleanData;
    }

    /**
     * 从BigInteger创建ECPrivateKey
     * 注意：这里使用标准的P-256曲线参数
     */
    private static ECPrivateKey createECPrivateKey(BigInteger s) throws Exception {
        try {
            // 使用P-256曲线参数
            java.security.spec.ECParameterSpec ecSpec = getP256ParameterSpec();
            ECPrivateKeySpec keySpec = new ECPrivateKeySpec(s, ecSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new Exception("Failed to create EC private key from D value: " + e.getMessage(), e);
        }
    }

    /**
     * 获取P-256曲线参数规范
     */
    private static java.security.spec.ECParameterSpec getP256ParameterSpec() {
        // P-256曲线参数（secp256r1, prime256v1）
        // 这些是标准参数，可以从Java安全提供者获取

        // 使用Java内置的曲线参数
        // 注意：在Java 7+中，可以使用标准的曲线名称
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            // 创建临时密钥对来获取曲线参数
            java.security.KeyPairGenerator keyGen = java.security.KeyPairGenerator.getInstance("EC");
            keyGen.initialize(256); // 256位曲线
            java.security.KeyPair keyPair = keyGen.generateKeyPair();
            ECPrivateKey tempKey = (ECPrivateKey) keyPair.getPrivate();
            return tempKey.getParams();
        } catch (Exception e) {
            // 如果无法动态获取，使用硬编码的参数（不推荐，但作为备选）
            return getHardcodedP256ParameterSpec();
        }
    }

    /**
     * 硬编码的P-256曲线参数（备选方案）
     */
    private static java.security.spec.ECParameterSpec getHardcodedP256ParameterSpec() {
        // P-256曲线参数（secp256r1）
        // 注意：这只是一个示例，实际应用中应该使用动态获取的方式
        BigInteger p = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16);
        BigInteger a = new BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFC", 16);
        BigInteger b = new BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16);
        BigInteger x = new BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16);
        BigInteger y = new BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16);
        BigInteger n = new BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16);
        BigInteger h = BigInteger.ONE;

        java.security.spec.EllipticCurve curve = new java.security.spec.EllipticCurve(
                new java.security.spec.ECFieldFp(p), a, b);
        java.security.spec.ECPoint g = new java.security.spec.ECPoint(x, y);

        return new java.security.spec.ECParameterSpec(curve, g, n, h.intValue());
    }

    /**
     * 将私钥转换为十六进制字符串
     * 对应openssl命令: openssl pkey -in euiccsk.pem -outform DER | xxd -p | tr -d '\n'
     *
     * @param pemData PEM格式的私钥字符串
     * @return 私钥的十六进制字符串表示
     * @throws Exception 转换过程中发生的错误
     */
    public static String convertPrivateKeyToHex(String pemData) throws Exception {
        ECPrivateKey privateKey = parseECPrivateKey(pemData);
        if (privateKey == null) {
            throw new Exception("EC private key not found in PEM data");
        }

        // 获取私钥的编码（DER格式）
        byte[] encodedKey = privateKey.getEncoded();
        if (encodedKey == null) {
            throw new Exception("Failed to encode private key");
        }

        // 转换为十六进制
        return bytesToHex(encodedKey).toUpperCase();
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }

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
     * 检测私钥格式
     */
    public static KeyFormat detectKeyFormat(String keyData) {
        if (keyData == null || keyData.trim().isEmpty()) {
            return KeyFormat.UNKNOWN;
        }

        String cleanData = keyData.trim();
        String upperData = cleanData.toUpperCase();

        // 检查PEM格式
        if (upperData.contains(PEM_HEADER_EC)) {
            return KeyFormat.PEM_EC;
        }
        if (upperData.contains(PEM_HEADER_PKCS8)) {
            return KeyFormat.PEM_PKCS8;
        }
        if (upperData.contains(PEM_HEADER_PKCS8_ENC)) {
            return KeyFormat.PEM_PKCS8_ENCRYPTED;
        }

        // 检查十六进制格式
        String testHex = cleanData.replaceAll("\\s", "").replace("0x", "").replace("0X", "");
        if (HEX_PATTERN.matcher(testHex).matches() && testHex.length() % 2 == 0) {
            return KeyFormat.HEX;
        }

        // 检查Base64格式
        try {
            String testBase64 = extractBase64FromPEM(cleanData);
            Base64.getDecoder().decode(testBase64);
            return KeyFormat.BASE64;
        } catch (Exception e) {
            // 不是有效的Base64
        }

        return KeyFormat.UNKNOWN;
    }

    /**
     * 私钥格式枚举
     */
    public enum KeyFormat {
        HEX,
        BASE64,
        PEM_EC,
        PEM_PKCS8,
        PEM_PKCS8_ENCRYPTED,
        UNKNOWN
    }
}
