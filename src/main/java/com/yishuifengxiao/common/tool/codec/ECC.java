package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.HexUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * ECC 签名算法工具
 *
 * @author shi
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class ECC {

    /**
     * secp256r1 (NIST P-256) - 256位素数域
     */
    public static final String defalut_curveOID = "1.2.840.10045.3.1.7";
    /**
     * secp256r1 (NIST P-256) - 256位素数域
     */
    public static final String defalut_curveName = "secp256r1";

    /**
     * 使用secp256r1曲线生成ECC密钥对
     *
     * @return ECC密钥对对象
     * @throws Exception 当密钥生成过程中发生错误时抛出异常
     */
    public static KeyPair generateECCKeyPair() throws Exception {

        return generateECCKeyPair(defalut_curveName);
    }


    /**
     * 生成ECC椭圆曲线加密算法的密钥对
     *
     * @param stdName 椭圆曲线的OID标识符，用于指定使用的标准曲线参数,例如1.2.840.10045.3.1.7或secp256r1
     * @return 生成的ECC密钥对，包含公钥和私钥
     * @throws Exception 当密钥生成过程中发生错误时抛出异常
     */
    public static KeyPair generateECCKeyPair(String stdName) throws Exception {
        // 使用KeyPairGenerator来获取标准曲线的参数
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");

        // 直接使用OID初始化
        ECGenParameterSpec ecGenSpec = new ECGenParameterSpec(stdName);
        keyPairGenerator.initialize(ecGenSpec);

        // 生成一个临时密钥对以获取曲线参数
        return keyPairGenerator.generateKeyPair();
    }


    /**
     * 常见ECC曲线信息
     *
     * @param oid 曲线的对象标识符(OID)
     * @return 返回对应OID的曲线信息描述，包括曲线名称和位数等信息
     */
    public static String getCurveInfo(String oid) {
        // 根据OID返回对应的ECC曲线信息
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
     *
     * @param curveOID       椭圆曲线的OID标识符
     * @param publicKeyHex   公钥的十六进制字符串表示
     * @param privateKeyDHex 私钥D值的十六进制字符串表示
     * @return 包含指定公钥和私钥的KeyPair对象
     * @throws Exception 当解析过程中发生错误时抛出异常
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
     *
     * @param oid 椭圆曲线的OID标识符
     * @return ECParameterSpec 椭圆曲线参数规范
     * @throws Exception 当无法根据OID获取椭圆曲线参数时抛出异常
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
     *
     * @param publicKeyHex    公钥的十六进制字符串表示
     * @param ecParameterSpec 椭圆曲线参数规范
     * @return ECPublicKey 解析后的公钥对象
     * @throws Exception 当解析过程中发生错误时抛出异常
     */
    public static ECPublicKey parsePublicKeyFromHex(String publicKeyHex, ECParameterSpec ecParameterSpec) throws Exception {
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
     * 从十六进制字符串解析椭圆曲线公钥
     *
     * @param curveOID     椭圆曲线对象标识符，用于获取对应的参数规范
     * @param publicKeyHex 公钥的十六进制字符串表示
     * @return 解析得到的ECPublicKey对象
     * @throws Exception 当解析过程中发生错误时抛出异常
     */
    public static ECPublicKey parsePublicKeyFromHex(String curveOID, String publicKeyHex) throws Exception {
        ECParameterSpec parameterSpec = getECParameterSpecFromOID(curveOID);
        return parsePublicKeyFromHex(publicKeyHex, parameterSpec);
    }


    /**
     * 从十六进制字符串解析私钥
     *
     * @param privateKeyDHex  私钥D值的十六进制字符串表示
     * @param ecParameterSpec 椭圆曲线参数规范
     * @return ECPrivateKey 解析后的私钥对象
     * @throws Exception 当解析过程中发生错误时抛出异常
     */
    public static ECPrivateKey parsePrivateKeyFromHex(String privateKeyDHex, ECParameterSpec ecParameterSpec) throws Exception {
        BigInteger privateKeyD = new BigInteger(privateKeyDHex, 16);

        // 构建私钥
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyD, ecParameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);
    }

    /**
     * 从十六进制字符串解析EC私钥
     *
     * @param curveOID       椭圆曲线OID标识符，用于获取对应的参数规格
     * @param privateKeyDHex 私钥D值的十六进制字符串表示
     * @return 解析后的EC私钥对象
     * @throws Exception 当解析过程中发生错误时抛出异常
     */
    public static ECPrivateKey parsePrivateKeyFromHex(String curveOID, String privateKeyDHex) throws Exception {

        ECParameterSpec parameterSpec = getECParameterSpecFromOID(curveOID);
        return parsePrivateKeyFromHex(privateKeyDHex, parameterSpec);
    }


    /**
     * 使用私钥对数据进行签名
     *
     * @param data       待签名的数据
     * @param privateKey 私钥对象，用于签名
     * @return byte[] 签名数据的字节数组
     * @throws Exception 当签名过程中发生错误时抛出异常
     */
    public static byte[] signData(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    /**
     * 使用公钥验证签名
     *
     * @param data      待验证的数据
     * @param signature 签名数据的字节数组
     * @param publicKey 公钥对象，用于验证签名
     * @return boolean 如果签名验证成功则返回true，否则返回false
     * @throws Exception 当验证过程中发生错误时抛出异常
     */
    public static boolean verifySignature(byte[] data, byte[] signature, PublicKey publicKey) throws Exception {
        Signature verifySignature = Signature.getInstance("SHA256withECDSA");
        verifySignature.initVerify(publicKey);
        verifySignature.update(data);
        return verifySignature.verify(signature);
    }

    /**
     * 验证证书与私钥是否匹配
     *
     * @param certVal       证书值
     * @param privateKeyVal 私钥值
     * @return 如果验证成功返回true，否则返回false
     */
    public static boolean verifyMatch(String certVal, String privateKeyVal) {
        try {
            // 准备测试数据
            String data = "0123456789ABCDEF";
            // 从证书中提取公钥
            PublicKey publicKey = X509Helper.extractPublicKey(certVal);
            // 解析EC私钥
            PrivateKey privateKey = parseECPrivateKey(privateKeyVal);
            // 使用私钥对数据进行签名
            byte[] signData = signData(data.getBytes(StandardCharsets.UTF_8), privateKey);
            // 使用公钥验证签名是否有效
            return verifySignature(data.getBytes(StandardCharsets.UTF_8), signData, publicKey);
        } catch (Exception e) {
            // 记录异常日志
            log.warn("verifyMatch error: ", e);
        }
        // 默认返回false
        return false;
    }

    /**
     * 验证密钥组件是否正确
     *
     * @param keyPair                包含公钥和私钥的密钥对对象
     * @param expectedPublicKeyHex   期望的公钥十六进制字符串表示
     * @param expectedPrivateKeyDHex 期望的私钥D值十六进制字符串表示
     * @throws Exception 当验证过程中发生错误时抛出异常
     */
    public static void verifyKeyComponents(KeyPair keyPair, String expectedPublicKeyHex,
                                           String expectedPrivateKeyDHex) throws Exception {
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
     *
     * @param value  要转换的BigInteger值
     * @param length 期望的十六进制字符串长度
     * @return 固定长度的十六进制字符串表示
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
     *
     * @param data           需要签名的数据字符串
     * @param curveOID       椭圆曲线OID标识符
     * @param publicKeyHex   公钥的十六进制字符串表示
     * @param privateKeyDHex 私钥D值的十六进制字符串表示
     * @return 签名结果的Base64编码字符串
     * @throws Exception 当密钥创建或签名过程中发生错误时抛出
     */
    public static String sign(String data, String curveOID, String publicKeyHex, String privateKeyDHex) throws Exception {
        // 根据提供的组件创建密钥对
        KeyPair keyPair = createKeyPairFromComponents(curveOID, publicKeyHex, privateKeyDHex);
        // 使用私钥对数据进行签名
        byte[] signature = signData(data.getBytes("UTF-8"), keyPair.getPrivate());
        // 将签名结果进行Base64编码并返回
        return Base64.getEncoder().encodeToString(signature);
    }


    /**
     * 验证签名
     *
     * @param data            待验证的数据字符串
     * @param signatureBase64 Base64编码的签名值
     * @param curveOID        椭圆曲线OID标识符
     * @param publicKeyHex    十六进制格式的公钥字符串
     * @return 验证成功返回true，验证失败返回false
     * @throws Exception 验证过程中可能抛出的异常
     */
    public static boolean verify(String data, String signatureBase64, String curveOID, String publicKeyHex) throws Exception {
        // 创建一个虚拟私钥来构建密钥对
        String dummyPrivateKey = "0000000000000000000000000000000000000000000000000000000000000001";
        KeyPair keyPair = createKeyPairFromComponents(curveOID, publicKeyHex, dummyPrivateKey);

        // 解码Base64签名并验证签名有效性
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
     *
     * @param hexString 十六进制字符串，可以包含空格和0x/0X前缀
     * @return 解析后的字节数组，如果解析失败则返回null
     */
    private static byte[] parseHexString(String hexString) {
        // 清理十六进制字符串，移除空格和0x/0X前缀
        String cleanHex = hexString.replaceAll("\\s", "").replace("0x", "").replace("0X", "");

        // 验证字符串格式是否正确且长度为偶数
        if (!HEX_PATTERN.matcher(cleanHex).matches() || cleanHex.length() % 2 != 0) {
            return null;
        }

        try {
            // 按每两个字符一组解析为字节
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
     *
     * @param base64String 需要解析的Base64字符串，可能包含PEM格式的头部和尾部
     * @return 解码后的字节数组，如果解码失败则返回null
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
            // 尝试标准Base64解码
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
     *
     * @param keyBytes 私钥字节数组
     * @return 解析成功的ECPrivateKey对象
     * @throws Exception 当无法识别私钥格式或解析失败时抛出异常
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
     *
     * @param keyBytes PKCS#8格式的私钥字节数组
     * @return ECPrivateKey EC私钥对象
     * @throws Exception 当解析失败或私钥不是EC类型时抛出异常
     */
    private static ECPrivateKey parsePKCS8PrivateKey(byte[] keyBytes) throws Exception {
        try {
            // 创建EC算法的密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            // 验证并转换私钥类型
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
     *
     * @param keyBytes SEC1格式的私钥字节数组
     * @return 解析后的ECPrivateKey对象
     * @throws Exception 当解析失败或无法提取私钥参数时抛出异常
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
     * 从SEC1格式的字节数组中解析并提取私钥的D值（即私钥对应的BigInteger值）。
     *
     * @param keyBytes 输入的SEC1格式的字节数据，应包含完整的ASN.1编码结构
     * @return 解析出的私钥D值，类型为BigInteger；如果输入无效或长度不足，则返回null
     * @throws Exception 当输入数据不符合SEC1格式规范时抛出异常
     */
    private static BigInteger parseSEC1DValue(byte[] keyBytes) throws Exception {
        if (keyBytes == null || keyBytes.length < 10) {
            return null;
        }

        // 简单的ASN.1解析
        int pos = 0;

        // 检查SEQUENCE标签 (0x30)，表示整个结构开始
        if (keyBytes[pos++] != 0x30) {
            throw new Exception("Invalid SEC1 format: expected SEQUENCE tag");
        }

        // 读取SEQUENCE的内容长度
        int length = keyBytes[pos++] & 0xFF;
        if (length > 0x80) {
            // 处理长格式长度字段：高位bit为1表示后续字节表示实际长度
            int lengthBytes = length - 0x80;
            length = 0;
            for (int i = 0; i < lengthBytes; i++) {
                length = (length << 8) | (keyBytes[pos++] & 0xFF);
            }
        }

        // 检查版本号字段是否为INTEGER类型 (0x02)
        if (keyBytes[pos++] != 0x02) {
            throw new Exception("Invalid SEC1 format: expected INTEGER tag for version");
        }

        // 跳过版本号内容（通常为1个字节）
        int versionLength = keyBytes[pos++] & 0xFF;
        pos += versionLength;

        // 检查私钥字段是否为OCTET STRING类型 (0x04)
        if (keyBytes[pos++] != 0x04) {
            throw new Exception("Invalid SEC1 format: expected OCTET STRING tag for private key");
        }

        // 读取OCTET STRING中私钥部分的长度
        int privateKeyLength = keyBytes[pos++] & 0xFF;
        if (privateKeyLength > 0x80) {
            // 同样处理长格式长度字段
            int lengthBytes = privateKeyLength - 0x80;
            privateKeyLength = 0;
            for (int i = 0; i < lengthBytes; i++) {
                privateKeyLength = (privateKeyLength << 8) | (keyBytes[pos++] & 0xFF);
            }
        }

        // 提取私钥的实际字节内容
        if (pos + privateKeyLength > keyBytes.length) {
            throw new Exception("Invalid SEC1 format: private key data truncated");
        }

        byte[] privateKeyBytes = new byte[privateKeyLength];
        System.arraycopy(keyBytes, pos, privateKeyBytes, 0, privateKeyLength);

        // 将私钥字节数组转换为正整数BigInteger返回
        return new BigInteger(1, privateKeyBytes); // 使用1确保结果为正数
    }


    /**
     * 解析PEM格式私钥
     *
     * @param pemData PEM格式的私钥字符串数据
     * @return 解析后的ECPrivateKey对象
     * @throws Exception 当解析失败或数据格式不正确时抛出异常
     */
    private static ECPrivateKey parsePEMPrivateKey(String pemData) throws Exception {
        String processedPem = pemData;
        // 先移除EC PARAMETERS部分（如果有）
        int beginParamsIndex = processedPem.indexOf("-----BEGIN EC PARAMETERS-----");
        int endParamsIndex = processedPem.indexOf("-----END EC PARAMETERS-----");

        if (beginParamsIndex != -1 && endParamsIndex != -1) {
            // 找到EC PARAMETERS结束标记后的换行符位置
            int endIndex = processedPem.indexOf("\n", endParamsIndex);
            if (endIndex == -1) {
                // 如果没找到换行符，则结束位置就是END PARAMETERS标记的末尾
                endIndex = endParamsIndex + "-----END EC PARAMETERS-----".length();
            } else {
                // 包含换行符
                endIndex += 1;
            }
            // 移除整个EC PARAMETERS部分
            processedPem = processedPem.substring(0, beginParamsIndex) +
                    processedPem.substring(endIndex);
        }
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
     *
     * @param pemData 包含PEM格式数据的字符串
     * @return 提取出的Base64编码数据，去除了PEM头部、尾部和空白字符
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
     *
     * @param s BigInteger类型的私钥值
     * @return ECPrivateKey对象
     * @throws Exception 当创建EC私钥失败时抛出异常
     */
    private static ECPrivateKey createECPrivateKey(BigInteger s) throws Exception {
        try {
            // 使用P-256曲线参数创建EC私钥
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
     *
     * @return ECParameterSpec对象，包含P-256椭圆曲线的参数规范
     */
    private static java.security.spec.ECParameterSpec getP256ParameterSpec() {
        // 尝试通过Java内置方式获取P-256曲线参数
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
     *
     * <p>该方法用于构造并返回一个硬编码的P-256椭圆曲线参数规范（ECParameterSpec），
     * 适用于secp256r1标准定义的椭圆曲线。这些参数包括有限域、椭圆曲线方程系数、
     * 基点坐标、阶数和辅因子。</p>
     *
     * <p><strong>注意：</strong>此实现仅为示例用途，在生产环境中应优先通过标准API
     * 动态获取参数以确保安全性和兼容性。</p>
     *
     * @return 返回包含P-256曲线参数的 {@link java.security.spec.ECParameterSpec} 实例
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

        // 构造椭圆曲线对象，基于素数域p，并设置曲线方程中的a和b系数
        java.security.spec.EllipticCurve curve = new java.security.spec.EllipticCurve(
                new java.security.spec.ECFieldFp(p), a, b);

        // 设置基点G的坐标(x, y)
        java.security.spec.ECPoint g = new java.security.spec.ECPoint(x, y);

        // 创建并返回完整的椭圆曲线参数规范对象
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
        return HexUtil.bytesToHex(encodedKey).toUpperCase();
    }


    /**
     * 检测私钥格式
     *
     * @param keyData 私钥数据字符串
     * @return 检测到的密钥格式类型，如果无法识别则返回UNKNOWN
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

    /**
     * 执行ECDH密钥协商
     *
     * @param privateKey 本地私钥，用于密钥协商
     * @param publicKey  对方公钥，用于密钥协商
     * @return 协商生成的共享密钥字节数组
     * @throws Exception 如果密钥协商过程中发生错误
     */
    public static byte[] performKeyAgreement(PrivateKey privateKey, PublicKey publicKey)
            throws Exception {
        // 创建ECDH密钥协商实例
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        // 初始化密钥协商对象，设置本地私钥
        keyAgreement.init(privateKey);
        // 执行密钥协商阶段，使用对方公钥
        keyAgreement.doPhase(publicKey, true);
        // 生成并返回共享密钥
        return keyAgreement.generateSecret();
    }

    /**
     * 执行密钥协商操作，根据提供的椭圆曲线参数和密钥信息生成共享密钥
     *
     * @param curveOID       椭圆曲线的OID标识符，用于指定使用的椭圆曲线类型
     * @param publicKeyHex   对方公钥的十六进制字符串表示
     * @param privateKeyDHex 本地私钥的十六进制字符串表示
     * @return 协商生成的共享密钥字节数组
     * @throws Exception 当密钥协商过程中发生错误时抛出异常
     */
    public static byte[] performKeyAgreement(String curveOID, String privateKeyDHex, String publicKeyHex) throws Exception {
        // 根据椭圆曲线参数和密钥组件创建密钥对
        KeyPair keyPair = createKeyPairFromComponents(curveOID, publicKeyHex, privateKeyDHex);
        // 执行密钥协商操作并返回结果
        return performKeyAgreement(keyPair.getPrivate(), keyPair.getPublic());
    }

    /**
     * 从字节数组重建公钥
     *
     * @param publicKeyBytes 公钥的字节数组表示
     * @return 重建后的PublicKey对象
     * @throws Exception 当密钥工厂创建失败或公钥生成失败时抛出异常
     */
    public static PublicKey rebuildPublicKey(byte[] publicKeyBytes) throws Exception {
        // 创建EC算法的密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        // 使用X509编码格式创建公钥规范
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        // 根据公钥规范生成公钥对象
        return keyFactory.generatePublic(keySpec);
    }


    /**
     * 从字节数组重建私钥
     *
     * @param privateKeyBytes 私钥的字节数组表示
     * @return 重建的PrivateKey对象
     * @throws Exception 当密钥工厂生成失败或密钥规范解析失败时抛出异常
     */
    public static PrivateKey rebuildPrivateKey(byte[] privateKeyBytes) throws Exception {
        // 创建EC算法的密钥工厂
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        // 使用PKCS8格式编码的密钥规范
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        // 根据密钥规范生成私钥对象
        return keyFactory.generatePrivate(keySpec);
    }


    /**
     * 获取公钥的字节数组表示
     *
     * @param publicKey 公钥对象，用于获取其字节编码表示
     * @return 返回公钥的字节数组编码，如果公钥为null则可能返回null或抛出异常
     */
    public static byte[] getPublicKeyBytes(PublicKey publicKey) {
        return publicKey.getEncoded();
    }


    /**
     * 获取私钥的字节数组表示
     *
     * @param privateKey 私钥对象，用于获取其字节编码表示
     * @return 返回私钥的字节数组编码，如果私钥为null则可能返回null
     */
    public static byte[] getPrivateKeyBytes(PrivateKey privateKey) {
        return privateKey.getEncoded();
    }


    /**
     * 创建共享密钥规范对象
     *
     * @param algorithm    算法名称，如"AES"等
     * @param sharedSecret 原始共享密钥字节数组
     * @return SecretKeySpec 对象，包含派生后的对称密钥
     * @throws Exception 当获取消息摘要实例或派生密钥过程中发生错误时抛出
     */
    public static SecretKeySpec createSharedSecret(String algorithm, byte[] sharedSecret) throws Exception {
        // 使用KDF（密钥派生函数）派生对称密钥
        // 这里使用简单的SHA-256哈希作为KDF示例
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] derivedKey = digest.digest(sharedSecret);

        // 根据需要的密钥长度截取
        int keyLength = getKeyLength(algorithm);
        byte[] finalKey = Arrays.copyOf(derivedKey, keyLength / 8);
        return new SecretKeySpec(finalKey, algorithm);
    }


    /**
     * 获取指定加密算法的密钥长度
     *
     * @param algorithm 加密算法名称，如AES、DES、DESEDE等
     * @return 返回对应算法的密钥长度（位数），默认返回128位
     */
    private static int getKeyLength(String algorithm) {
        // 根据算法名称返回对应的密钥长度
        switch (algorithm.toUpperCase()) {
            case "AES":
                return 128;
            case "DES":
                return 64;
            case "DESEDE":
                return 192;
            default:
                return 128;
        }
    }

    /**
     * 对hex字符串进行左侧填充到指定字节数
     *
     * @param hexString   原始hex字符串
     * @param targetBytes 目标字节数
     * @return 填充后的hex字符串（保持原始的前缀格式）
     */
    public static String padHexLeft(String hexString, int targetBytes) {
        if (hexString == null || targetBytes <= 0) {
            throw new IllegalArgumentException("参数不能为空且目标字节数必须大于0");
        }

        // 处理前缀（0x或0X）
        String prefix = "";
        String cleanHex = hexString;

        if (cleanHex.startsWith("0x") || cleanHex.startsWith("0X")) {
            prefix = cleanHex.substring(0, 2); // 保留原始的大小写格式
            cleanHex = cleanHex.substring(2);
        }

        // 移除可能存在的空格
        cleanHex = cleanHex.replaceAll("\\s+", "");

        // 验证是否为有效的hex字符串
        if (!cleanHex.matches("[0-9a-fA-F]+")) {
            throw new IllegalArgumentException("无效的hex字符串: " + hexString);
        }

        int targetLength = targetBytes * 2; // 每个字节对应2个hex字符
        int currentLength = cleanHex.length();

        // 如果已经达到或超过目标长度，直接返回
        if (currentLength >= targetLength) {
            return prefix + cleanHex;
        }

        // 左侧填充0
        int zerosToAdd = targetLength - currentLength;
        StringBuilder padded = new StringBuilder();
        for (int i = 0; i < zerosToAdd; i++) {
            padded.append('0');
        }
        padded.append(cleanHex);

        return prefix + padded.toString();
    }

    /**
     * 执行ECC密钥协商算法，生成共享密钥
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


        // 执行ECC密钥协商，获取原始共享密钥数据
        byte[] bytes = performKeyAgreement(curveOID, privateKeyDHex, publicKeyHex);
        String hex = HexUtil.bytesToHex(bytes);

        // 对原始密钥数据进行左填充，确保长度为32字节
        String result = HexUtil.padHexLeft(hex, 32);


        // 计算需要进行哈希运算的次数
        int klen_bit = iKeyLen * 8;
        int hlen = (klen_bit % 256 == 0) ? klen_bit / 256 : (klen_bit / 256 + 1);


        // 通过迭代哈希运算派生最终的密钥
        StringBuilder hashSb = new StringBuilder();
        for (int i = 1; i <= hlen; i++) {
            String counter = HexUtil.padHexLeft(Integer.toHexString(i), 4);
            hashSb.append(SHA256.calculateSHA256FromHex(result + counter + sShareInfo));
        }
        return hashSb.toString().substring(0, iKeyLen * 2).toUpperCase();
    }

}
