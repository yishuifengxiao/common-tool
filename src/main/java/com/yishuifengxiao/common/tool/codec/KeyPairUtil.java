package com.yishuifengxiao.common.tool.codec;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class KeyPairUtil {

    private static final Map<String, String> ECC_CURVE_OID_MAP = new HashMap<>();

    static {
        // 初始化常见ECC曲线OID映射
        ECC_CURVE_OID_MAP.put("1.2.840.10045.3.1.7", "secp256r1");
        ECC_CURVE_OID_MAP.put("1.3.132.0.34", "secp384r1");
        ECC_CURVE_OID_MAP.put("1.3.132.0.35", "secp521r1");
        ECC_CURVE_OID_MAP.put("1.3.132.0.10", "secp256k1");
        ECC_CURVE_OID_MAP.put("1.2.840.10045.3.1.1", "secp192r1");
        ECC_CURVE_OID_MAP.put("1.2.840.10045.3.1.6", "secp224r1");
    }

    /**
     * 根据算法类型和参数生成密钥对
     */
    public static KeyPair generateKeyPair(String algorithm, Object parameter) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);

            if ("EC".equals(algorithm)) {
                if (parameter instanceof String) {
                    String param = (String) parameter;
                    // 判断是OID还是曲线名称
                    if (param.contains(".")) {
                        // 可能是OID
                        String curveName = ECC_CURVE_OID_MAP.get(param);
                        if (curveName != null) {
                            keyPairGenerator.initialize(new ECGenParameterSpec(curveName));
                        } else {
                            // 如果不是已知OID，尝试直接作为曲线名称使用
                            keyPairGenerator.initialize(new ECGenParameterSpec(param));
                        }
                    } else {
                        // 直接作为曲线名称
                        keyPairGenerator.initialize(new ECGenParameterSpec(param));
                    }
                } else if (parameter instanceof Integer) {
                    // 对于EC，参数应该是曲线名称或OID，不是整数大小
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
     */
    public static KeyPair generateECCKeyPairByOID(String curveOID) {
        return generateKeyPair("EC", curveOID);
    }

    /**
     * 生成RSA密钥对
     */
    public static KeyPair generateRSAKeyPair(int keySize) {
        return generateKeyPair("RSA", keySize);
    }

    /**
     * 生成DSA密钥对
     */
    public static KeyPair generateDSAKeyPair(int keySize) {
        return generateKeyPair("DSA", keySize);
    }

    /**
     * 使用安全随机数生成ECC密钥对
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
     * 获取密钥信息
     */
    public static void printKeyInfo(KeyPair keyPair) {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        System.out.println("算法: " + publicKey.getAlgorithm());
        System.out.println("公钥格式: " + publicKey.getFormat());
        System.out.println("公钥长度: " + publicKey.getEncoded().length + " bytes");
        System.out.println("私钥格式: " + privateKey.getFormat());
        System.out.println("私钥长度: " + privateKey.getEncoded().length + " bytes");

        // 对于EC密钥，可以获取更多信息
        if (publicKey instanceof java.security.interfaces.ECPublicKey) {
            java.security.interfaces.ECPublicKey ecPublicKey = (java.security.interfaces.ECPublicKey) publicKey;
            System.out.println("曲线: " + ecPublicKey.getParams().toString());
        }
    }

    /**
     * 编码密钥为Base64
     */
    public static String encodeKeyToBase64(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 获取支持的ECC曲线OID列表
     */
    public static String[] getSupportedECCurveOIDs() {
        return ECC_CURVE_OID_MAP.keySet().toArray(new String[0]);
    }

    /**
     * 添加自定义ECC曲线映射
     */
    public static void addECCurveMapping(String oid, String curveName) {
        ECC_CURVE_OID_MAP.put(oid, curveName);
    }

    /**
     * 获取支持的ECC曲线OID列表
     */
    public static String[] getSupportedCurveOIDs() {
        return ECC_CURVE_OID_MAP.keySet().toArray(new String[0]);
    }

    /**
     * 根据OID获取曲线名称
     */
    public static String getCurveNameByOID(String curveOID) {
        return ECC_CURVE_OID_MAP.get(curveOID);
    }

    /**
     * 打印密钥对详细信息
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

    public static KeyPair generateECCKeyPair(String curveName) {
        try {
            // 获取ECC密钥对生成器实例
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");

            // 初始化椭圆曲线参数
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(curveName);
            keyPairGenerator.initialize(ecSpec);

            // 生成密钥对
            return keyPairGenerator.generateKeyPair();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("ECC算法不支持", e);
        } catch (Exception e) {
            throw new RuntimeException("ECC密钥对生成失败", e);
        }
    }

}