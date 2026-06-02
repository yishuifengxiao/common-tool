package com.yishuifengxiao.common.tool.codec;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>密钥对生成与操作工具类</p>
 * <p>提供多种非对称加密算法的密钥对生成、格式转换和信息提取功能。</p>
 * <p>支持的算法：</p>
 * <ul>
 * <li>ECC（椭圆曲线加密）- 支持多种曲线如secp256r1、secp384r1等</li>
 * <li>RSA（非对称加密）</li>
 * <li>DSA（数字签名算法）</li>
 * </ul>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class KeyPairHelper {

    private static final Map<String, String> ECC_CURVE_OID_MAP = new HashMap<>();

    static {
        ECC_CURVE_OID_MAP.put("1.2.840.10045.3.1.7", "secp256r1");
        ECC_CURVE_OID_MAP.put("1.3.132.0.34", "secp384r1");
        ECC_CURVE_OID_MAP.put("1.3.132.0.35", "secp521r1");
        ECC_CURVE_OID_MAP.put("1.3.132.0.10", "secp256k1");
        ECC_CURVE_OID_MAP.put("1.2.840.10045.3.1.1", "secp192r1");
        ECC_CURVE_OID_MAP.put("1.2.840.10045.3.1.6", "secp224r1");
    }

    /**
     * 根据算法类型和参数生成密钥对
     *
     * @param algorithm 算法类型，支持"EC"、"RSA"、"DSA"
     * @param parameter 算法参数，EC算法传入曲线名称或OID，RSA/DSA传入密钥大小（整数）
     * @return 生成的密钥对
     * @throws RuntimeException 密钥对生成失败时抛出
     */
    public static KeyPair generateKeyPair(String algorithm, Object parameter) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);

            if ("EC".equals(algorithm)) {
                if (parameter instanceof String) {
                    String param = (String) parameter;
                    if (param.contains(".")) {
                        String curveName = ECC_CURVE_OID_MAP.get(param);
                        if (curveName != null) {
                            keyPairGenerator.initialize(new ECGenParameterSpec(curveName));
                        } else {
                            keyPairGenerator.initialize(new ECGenParameterSpec(param));
                        }
                    } else {
                        keyPairGenerator.initialize(new ECGenParameterSpec(param));
                    }
                } else if (parameter instanceof Integer) {
                    throw new IllegalArgumentException("EC算法需要曲线名称或OID，而不是密钥大小");
                }
            } else if ("RSA".equals(algorithm) || "DSA".equals(algorithm)) {
                if (parameter instanceof Integer) {
                    keyPairGenerator.initialize((Integer) parameter);
                } else {
                    throw new IllegalArgumentException(algorithm + "算法需要整数密钥大小");
                }
            }

            return keyPairGenerator.generateKeyPair();

        } catch (Exception e) {
            throw new RuntimeException("密钥对生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ECC曲线OID生成密钥对
     *
     * @param curveOID 曲线OID，如"1.2.840.10045.3.1.7"（secp256r1）
     * @return 生成的ECC密钥对
     */
    public static KeyPair generateECCKeyPairByOID(String curveOID) {
        return generateKeyPair("EC", curveOID);
    }

    /**
     * 生成RSA密钥对
     *
     * @param keySize 密钥长度，通常为1024、2048或4096
     * @return 生成的RSA密钥对
     */
    public static KeyPair generateRSAKeyPair(int keySize) {
        return generateKeyPair("RSA", keySize);
    }

    /**
     * 生成DSA密钥对
     *
     * @param keySize 密钥长度
     * @return 生成的DSA密钥对
     */
    public static KeyPair generateDSAKeyPair(int keySize) {
        return generateKeyPair("DSA", keySize);
    }

    /**
     * 使用安全随机数生成ECC密钥对
     *
     * @param curveOID     曲线OID
     * @param secureRandom 安全随机数生成器
     * @return 生成的ECC密钥对
     * @throws RuntimeException 密钥对生成失败时抛出
     */
    public static KeyPair generateSecureECCKeyPairByOID(String curveOID, SecureRandom secureRandom) {
        try {
            String curveName = ECC_CURVE_OID_MAP.get(curveOID);
            if (curveName == null) {
                throw new IllegalArgumentException("不支持的ECC曲线OID: " + curveOID);
            }

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveName);
            keyPairGenerator.initialize(ecSpec, secureRandom);

            return keyPairGenerator.generateKeyPair();

        } catch (Exception e) {
            throw new RuntimeException("安全ECC密钥对生成失败", e);
        }
    }

    /**
     * 打印密钥基本信息
     *
     * @param keyPair 密钥对对象
     */
    public static void printKeyInfo(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        System.out.println("算法: " + publicKey.getAlgorithm());
        System.out.println("公钥格式: " + publicKey.getFormat());
        System.out.println("公钥长度: " + publicKey.getEncoded().length + " bytes");
        System.out.println("私钥格式: " + privateKey.getFormat());
        System.out.println("私钥长度: " + privateKey.getEncoded().length + " bytes");

        if (publicKey instanceof java.security.interfaces.ECPublicKey) {
            java.security.interfaces.ECPublicKey ecPublicKey = (java.security.interfaces.ECPublicKey) publicKey;
            System.out.println("曲线: " + ecPublicKey.getParams().toString());
        }
    }

    /**
     * 将密钥编码为Base64字符串
     *
     * @param key 密钥对象
     * @return Base64编码的密钥字符串
     */
    public static String encodeKeyToBase64(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 获取支持的ECC曲线OID列表
     *
     * @return OID字符串数组
     */
    public static String[] getSupportedECCurveOIDs() {
        return ECC_CURVE_OID_MAP.keySet().toArray(new String[0]);
    }

    /**
     * 添加自定义ECC曲线OID映射
     *
     * @param oid       曲线OID
     * @param curveName 曲线名称
     */
    public static void addECCurveMapping(String oid, String curveName) {
        ECC_CURVE_OID_MAP.put(oid, curveName);
    }

    /**
     * 获取支持的ECC曲线OID列表（别名方法）
     *
     * @return OID字符串数组
     */
    public static String[] getSupportedCurveOIDs() {
        return ECC_CURVE_OID_MAP.keySet().toArray(new String[0]);
    }

    /**
     * 根据OID获取曲线名称
     *
     * @param curveOID 曲线OID
     * @return 曲线名称，未知OID返回null
     */
    public static String getCurveNameByOID(String curveOID) {
        return ECC_CURVE_OID_MAP.get(curveOID);
    }

    /**
     * 打印密钥对详细信息
     *
     * @param keyPair 密钥对对象
     */
    public static void printKeyDetails(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        System.out.println("=== 密钥详细信息 ===");
        System.out.println("算法: " + publicKey.getAlgorithm());
        System.out.println("公钥格式: " + publicKey.getFormat());
        System.out.println("公钥Base64:");
        System.out.println(encodeKeyToBase64(publicKey));
        System.out.println("\n私钥格式: " + privateKey.getFormat());
        System.out.println("私钥Base64:");
        System.out.println(encodeKeyToBase64(privateKey));
    }

    /**
     * 根据曲线名称生成ECC密钥对
     *
     * @param curveName 曲线名称，如"secp256r1"
     * @return 生成的ECC密钥对
     * @throws RuntimeException 密钥对生成失败时抛出
     */
    public static KeyPair generateECCKeyPair(String curveName) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveName);
            keyPairGenerator.initialize(ecSpec);
            return keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("ECC算法不支持", e);
        } catch (Exception e) {
            throw new RuntimeException("ECC密钥对生成失败", e);
        }
    }

    /**
     * 从密钥对获取公钥的十六进制表示
     *
     * @param keyPair 密钥对对象
     * @return 公钥的十六进制字符串
     */
    public static String getPublicKeyHex(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();

        if (publicKey instanceof RSAPublicKey) {
            return getRSAPublicKeyHex((RSAPublicKey) publicKey).toUpperCase();
        } else if (publicKey instanceof DSAPublicKey) {
            return getDSAPublicKeyHex((DSAPublicKey) publicKey).toUpperCase();
        } else if (publicKey instanceof ECPublicKey) {
            return getECPublicKeyHex((ECPublicKey) publicKey).toUpperCase();
        } else {
            return bytesToHex(publicKey.getEncoded()).toUpperCase();
        }
    }

    /**
     * 从密钥对获取私钥D值的十六进制表示
     *
     * @param keyPair 密钥对对象
     * @return 私钥D值的十六进制字符串
     * @throws UnsupportedOperationException 不支持的私钥类型
     */
    public static String getPrivateKeyDHex(KeyPair keyPair) {
        PrivateKey privateKey = keyPair.getPrivate();

        if (privateKey instanceof RSAPrivateKey) {
            return getRSAPrivateKeyDHex((RSAPrivateKey) privateKey).toUpperCase();
        } else if (privateKey instanceof DSAPrivateKey) {
            return getDSAPrivateKeyDHex((DSAPrivateKey) privateKey).toUpperCase();
        } else if (privateKey instanceof ECPrivateKey) {
            return getECPrivateKeyDHex((ECPrivateKey) privateKey).toUpperCase();
        } else {
            throw new UnsupportedOperationException("不支持的私钥类型: " + privateKey.getAlgorithm());
        }
    }

    /**
     * 获取RSA公钥的十六进制表示（模数N）
     *
     * @param rsaPublicKey RSA公钥对象
     * @return 模数的十六进制字符串
     */
    private static String getRSAPublicKeyHex(RSAPublicKey rsaPublicKey) {
        BigInteger modulus = rsaPublicKey.getModulus();
        return bigIntegerToHex(modulus).toUpperCase();
    }

    /**
     * 获取RSA私钥D值的十六进制表示
     *
     * @param rsaPrivateKey RSA私钥对象
     * @return 私有指数的十六进制字符串
     */
    private static String getRSAPrivateKeyDHex(RSAPrivateKey rsaPrivateKey) {
        BigInteger privateExponent = rsaPrivateKey.getPrivateExponent();
        return bigIntegerToHex(privateExponent).toUpperCase();
    }

    /**
     * 获取DSA公钥的十六进制表示（公钥Y）
     *
     * @param dsaPublicKey DSA公钥对象
     * @return 公钥Y的十六进制字符串
     */
    private static String getDSAPublicKeyHex(DSAPublicKey dsaPublicKey) {
        BigInteger y = dsaPublicKey.getY();
        return bigIntegerToHex(y).toUpperCase();
    }

    /**
     * 获取DSA私钥D值的十六进制表示（私钥X）
     *
     * @param dsaPrivateKey DSA私钥对象
     * @return 私钥X的十六进制字符串
     */
    private static String getDSAPrivateKeyDHex(DSAPrivateKey dsaPrivateKey) {
        BigInteger x = dsaPrivateKey.getX();
        return bigIntegerToHex(x).toUpperCase();
    }

    /**
     * 获取EC公钥的十六进制表示（未压缩格式）
     *
     * @param ecPublicKey EC公钥对象
     * @return 公钥点的十六进制字符串（04 + X + Y）
     */
    private static String getECPublicKeyHex(ECPublicKey ecPublicKey) {
        java.security.spec.ECPoint point = ecPublicKey.getW();
        BigInteger x = point.getAffineX();
        BigInteger y = point.getAffineY();

        String xHex = bigIntegerToFixedLengthHex(x, 32);
        String yHex = bigIntegerToFixedLengthHex(y, 32);

        return ("04" + xHex + yHex).toUpperCase();
    }

    /**
     * 将BigInteger转换为固定长度的十六进制字符串
     *
     * @param bigInt      要转换的BigInteger
     * @param targetBytes 目标字节长度
     * @return 固定长度的十六进制字符串
     */
    private static String bigIntegerToFixedLengthHex(BigInteger bigInt, int targetBytes) {
        byte[] bytes = bigInt.toByteArray();

        if (bytes.length > targetBytes && bytes[0] == 0) {
            byte[] trimmedBytes = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmedBytes, 0, trimmedBytes.length);
            bytes = trimmedBytes;
        }

        if (bytes.length > targetBytes) {
            byte[] result = new byte[targetBytes];
            System.arraycopy(bytes, bytes.length - targetBytes, result, 0, targetBytes);
            bytes = result;
        } else if (bytes.length < targetBytes) {
            byte[] result = new byte[targetBytes];
            System.arraycopy(bytes, 0, result, targetBytes - bytes.length, bytes.length);
            bytes = result;
        }

        return bytesToHex(bytes).toUpperCase();
    }

    /**
     * 获取EC私钥D值的十六进制表示
     *
     * @param ecPrivateKey EC私钥对象
     * @return 私钥D的十六进制字符串（固定64字符，对应256位/32字节）
     */
    public static String getECPrivateKeyDHex(ECPrivateKey ecPrivateKey) {
        BigInteger s = ecPrivateKey.getS();
        return bigIntegerToFixedLengthHex(s, 32).toUpperCase();
    }

    /**
     * 将BigInteger转换为十六进制字符串
     *
     * @param bigInt 要转换的BigInteger
     * @return 十六进制字符串
     */
    public static String bigIntegerToHex(BigInteger bigInt) {
        byte[] bytes = bigInt.toByteArray();
        return bytesToHex(bytes).toUpperCase();
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
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
        return hexString.toString().toUpperCase();
    }

    /**
     * 打印密钥对详细信息（包含十六进制值）
     *
     * @param keyPair 密钥对对象
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