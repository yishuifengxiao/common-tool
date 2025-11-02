package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.text.OIDConverter;
import com.yishuifengxiao.common.tool.text.TLVUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECPoint;
import java.util.*;
import java.util.regex.Pattern;

/**
 * X.509证书解析工具类
 * 支持十六进制、Base64和PEM格式的证书数据
 */
@Slf4j
public class X509Helper {

    private static final String PEM_HEADER = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_FOOTER = "-----END CERTIFICATE-----";
    private static final Pattern PEM_PATTERN = Pattern.compile("-----BEGIN CERTIFICATE-----.*-----END CERTIFICATE-----", Pattern.DOTALL);

    private static final Pattern HEX_PATTERN = Pattern.compile("^[0-9A-Fa-f]+$");

    /**
     * 解析证书数据，支持多种格式的输入
     *
     * @param certData 证书数据字符串，可以是十六进制、base64编码或PEM格式
     * @return 解析成功的X.509证书对象
     * @throws CertificateException 解析过程中发生的错误
     */
    public static X509Certificate parseCert(String certData) throws CertificateException {
        if (certData == null || certData.trim().isEmpty()) {
            throw new CertificateException("Certificate data cannot be null or empty");
        }

        certData = certData.trim();

        // 尝试作为十六进制字符串解析
        try {
            byte[] hexBytes = parseHexString(certData);
            if (hexBytes != null) {
                return parseCertificateBytes(hexBytes);
            }
        } catch (Exception e) {
            // 继续尝试其他格式
        }

        // 尝试作为base64字符串直接解析
        try {
            byte[] base64Bytes = parseBase64String(certData);
            if (base64Bytes != null) {
                return parseCertificateBytes(base64Bytes);
            }
        } catch (Exception e) {
            // 继续尝试其他格式
        }

        // 尝试作为PEM格式解析
        try {
            return parsePemCertificate(certData);
        } catch (Exception e) {
            throw new CertificateException("Failed to parse certificate data in any format: " + e.getMessage(), e);
        }
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
        String cleanBase64 = base64String.replace(PEM_HEADER, "").replace(PEM_FOOTER, "").replaceAll("\\s", "");

        try {
            // 尝试标准Base64解码
            return Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            // 尝试URL安全的Base64解码
            try {
                return Base64.getUrlDecoder().decode(cleanBase64);
            } catch (IllegalArgumentException e2) {
                // 尝试MIME类型的Base64解码（可能包含换行符）
                try {
                    return Base64.getMimeDecoder().decode(cleanBase64);
                } catch (IllegalArgumentException e3) {
                    return null;
                }
            }
        }
    }

    /**
     * 解析PEM格式证书
     */
    private static X509Certificate parsePemCertificate(String pemData) throws CertificateException {
        String processedPem = pemData;

        // 如果没有PEM头部，则添加
        if (!pemData.toUpperCase().contains(PEM_HEADER.toUpperCase())) {
            processedPem = PEM_HEADER + "\n" + pemData + "\n" + PEM_FOOTER;
        }

        // 提取Base64部分
        String base64Part = processedPem.replace(PEM_HEADER, "").replace(PEM_FOOTER, "").replaceAll("\\s", "");

        if (base64Part.isEmpty()) {
            throw new CertificateException("No Base64 data found in PEM format");
        }

        byte[] certBytes = Base64.getDecoder().decode(base64Part);
        return parseCertificateBytes(certBytes);
    }

    /**
     * 从字节数组解析证书
     */
    private static X509Certificate parseCertificateBytes(byte[] certBytes) throws CertificateException {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
        } catch (CertificateException e) {
            throw new CertificateException("Failed to parse certificate from bytes: " + e.getMessage(), e);
        }
    }

    /**
     * 证书信息完整类
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Cert implements Serializable {
        /**
         * 主题（Subject）
         */
        private String subject;
        /**
         * 颁发者（Issuer）
         */
        private String issuer;
        /**
         * 序列号（SerialNumber）
         */
        private String serialNumber;
        /**
         * Not Before（生效时间） 形式为 Mon Aug 18 17:26:25 CST 2025
         */
        private String notBefore;
        /**
         * Not After（失效时间） 形式为 Mon Aug 18 17:26:25 CST 2025
         */
        private String notAfter;
        /**
         * X.509版本号从v1开始计数，但ASN.1编码中版本号从0开始：
         * v1 → 0（通常省略）
         * v2 → 1
         * v3 → 2
         * 因此，0x02（十进制2）对应v3版本。
         */
        private int version;
        /**
         * 算法OID： 位置1：tbsCertificate.signature - 表示CA用来签署证书的算法，例如 ecdsa-with-SHA256。
         * 位置2：tbsCertificate.subjectPublicKeyInfo.algorithm - 这里的算法OID是 ecPublicKey (1.2.840.10045.2.1)。这告诉你这个公钥是基于ECC的。
         */
        private String sigAlgOID;
        /**
         * 算法名称
         */
        private String sigAlgName;
        private PublicKey publicKey;
        private String publicKeyAlgorithm;
        /**
         * 公钥的原始字节值（十六进制）
         */
        private String publicKeyValue;
        /**
         * Subject Key Identifier
         */
        private String skid;
        /**
         * Certificate Issuer Public Key Identifier
         */
        private String cipkid;
        /**
         * Object Identifier from Subject Alternative Names
         */
        private String oid;
        private List<String> subjectAlternativeNames;
        /**
         * Authority Key Identifier
         */
        private String akid;

        /**
         * 验证证书是否有效（当前时间是否在生效时间和失效时间之间）
         */
        private Boolean isValid;
        /**
         * 使用ECC算法时曲线的OID
         */
        String algid;

    }

    /**
     * 从X509Certificate提取完整信息
     *
     * @param certificate X.509证书对象
     * @return 证书完整信息
     */
    public static Cert extractFullInfo(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        Cert info = new Cert();

        // 基本证书信息
        info.setSubject(certificate.getSubjectX500Principal().getName());
        info.setIssuer(certificate.getIssuerX500Principal().getName());
        info.setSerialNumber(certificate.getSerialNumber().toString(16).toUpperCase());
        info.setNotBefore(certificate.getNotBefore().toString());
        info.setNotAfter(certificate.getNotAfter().toString());
        info.setVersion(certificate.getVersion());
        info.setIsValid(certificate.getNotBefore().before(new Date()) && certificate.getNotAfter().after(new Date()));


        info.setSigAlgOID(certificate.getSigAlgOID());
        info.setSigAlgName(certificate.getSigAlgName());


        // 提取公钥信息
        extractPublicKeyInfo(certificate, info);

        // 提取SKID（Subject Key Identifier）
        extractSubjectKeyIdentifier(certificate, info);

        // 提取CIPKID（Certificate Issuer Public Key Identifier）
        extractCipkid(certificate, info);

        // 提取OID和其他主题备用名称
        extractOidAndAlternativeNames(certificate, info);

        // 提取AKID（Authority Key Identifier）
        extractAuthorityKeyIdentifier(certificate, info);

        return info;
    }

    /**
     * 提取公钥信息
     */
    private static void extractPublicKeyInfo(X509Certificate certificate, Cert info) {
        PublicKey publicKey = certificate.getPublicKey();
        info.setPublicKey(publicKey);
        info.setPublicKeyAlgorithm(publicKey.getAlgorithm());
        // 根据公钥类型提取特定的公钥值
        try {
            String publicKeyValue = "";

            if (publicKey instanceof ECPublicKey) {
                ECPublicKey eCPublicKey = (ECPublicKey) publicKey;
                //曲线的algid
                String algid = getCurveOIDFromPublicKeyEncoding(eCPublicKey.getEncoded());
                info.setAlgid(algid);
                // ECDSA公钥处理
                publicKeyValue = extractECDSAPublicKeyHex(eCPublicKey);
            } else if (publicKey instanceof RSAPublicKey) {
                RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;

                // RSA公钥处理
                publicKeyValue = extractRSAPublicKeyHex(rsaPublicKey);
            } else {
                // 其他类型的公钥，使用完整编码
                byte[] publicKeyBytes = publicKey.getEncoded();
                publicKeyValue = TLVUtil.bytesToHex(publicKeyBytes);
            }

            info.setPublicKeyValue(publicKeyValue.toUpperCase());
        } catch (Exception e) {
            log.info("Failed to extract public key value: " + e.getMessage());
            // 如果提取失败，使用完整编码作为后备
            try {
                byte[] publicKeyBytes = publicKey.getEncoded();
                info.setPublicKeyValue(TLVUtil.bytesToHex(publicKeyBytes).toUpperCase());
            } catch (Exception ex) {
                log.warn("Failed to extract public key encoded value: " + ex.getMessage());
            }
        }
    }

    public static String getCurveOIDFromPublicKeyEncoding(byte[] encoded) {
        // 定位第二个OID的内容
        if (encoded.length < 23) {
            throw new IllegalArgumentException("公钥编码长度不足");
        }
        // 检查结构是否符合预期
        if (encoded[0] != 0x30 || encoded[2] != 0x30 || encoded[4] != 0x06 || encoded[13] != 0x06) {
            throw new IllegalArgumentException("非预期的公钥编码结构");
        }
        // 第二个OID的长度
        int oidLength = encoded[14];
        if (oidLength != 8) {
            throw new IllegalArgumentException("非预期的OID长度");
        }
        byte[] oidBytes = new byte[oidLength];
        System.arraycopy(encoded, 15, oidBytes, 0, oidLength);
        String hex = TLVUtil.bytesToHex(oidBytes);
        String notation = OIDConverter.hexToDotNotation(hex);
        return notation;
    }

    /**
     * 提取RSA公钥的十六进制表示
     */
    private static String extractRSAPublicKeyHex(RSAPublicKey publicKey) {
        try {
            // 将RSA公钥编码为DER格式
            byte[] publicKeyDER = publicKey.getEncoded();
            return TLVUtil.bytesToHex(publicKeyDER);
        } catch (Exception e) {
            log.info("Failed to extract RSA public key: " + e.getMessage());
            return "";
        }
    }

    /**
     * 提取ECDSA公钥的十六进制表示（未压缩格式）
     */
    private static String extractECDSAPublicKeyHex(ECPublicKey publicKey) {
        try {
            // 获取公钥点坐标
            ECPoint point = publicKey.getW();
            BigInteger x = point.getAffineX();
            BigInteger y = point.getAffineY();

            // 计算公钥点坐标的长度（字节）
            int keySize = (publicKey.getParams().getCurve().getField().getFieldSize() + 7) / 8;

            // 创建足够大的缓冲区（04 + X + Y）
            byte[] buf = new byte[1 + 2 * keySize];
            buf[0] = 0x04; // 未压缩格式标识

            // 将X坐标填充到缓冲区
            byte[] xBytes = x.toByteArray();
            int xStart = 1 + keySize - xBytes.length;
            if (xStart >= 1) {
                System.arraycopy(xBytes, 0, buf, xStart, xBytes.length);
            } else {
                // 如果X坐标太长，截断到合适长度
                System.arraycopy(xBytes, xBytes.length - keySize, buf, 1, keySize);
            }

            // 将Y坐标填充到缓冲区
            byte[] yBytes = y.toByteArray();
            int yStart = 1 + 2 * keySize - yBytes.length;
            if (yStart >= 1 + keySize) {
                System.arraycopy(yBytes, 0, buf, yStart, yBytes.length);
            } else {
                // 如果Y坐标太长，截断到合适长度
                System.arraycopy(yBytes, yBytes.length - keySize, buf, 1 + keySize, keySize);
            }

            return TLVUtil.bytesToHex(buf);
        } catch (Exception e) {
            log.info("Failed to extract ECDSA public key: " + e.getMessage());
            return "";
        }
    }

    /**
     * 提取主题密钥标识符（SKID）
     */
    private static void extractSubjectKeyIdentifier(X509Certificate certificate, Cert info) {
        try {
            byte[] skidExtension = certificate.getExtensionValue("2.5.29.14"); // Subject Key Identifier OID
            if (skidExtension != null) {
                // SKID扩展值是OCTET STRING包装的，需要解析
                byte[] skidValue = parseOctetStringExtension(skidExtension);
                if (skidValue != null) {
                    info.setSkid(TLVUtil.bytesToHex(skidValue));
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract Subject Key Identifier: " + e.getMessage());
        }
    }

    private static void extractCipkid(X509Certificate certificate, Cert info) {
        info.setCipkid(TLVUtil.fetchValueFromTlv("04", info.getSkid()));
    }

    /**
     * 从证书数据中提取CIPKID（Certificate Issuer Public Key Identifier）
     *
     * @param certData 证书数据字符串，用于解析和提取SKID信息
     * @return 返回提取到的CIPKID值，如果提取失败则返回null
     */
    public String extractCipkid(String certData) {
        try {
            // 解析证书数据获取X509证书对象
            X509Certificate certificate = parseCert(certData);
            // 获取Subject Key Identifier扩展值（OID: 2.5.29.14）
            byte[] skidExtension = certificate.getExtensionValue("2.5.29.14"); // Subject Key Identifier OID
            if (skidExtension != null) {
                // SKID扩展值是OCTET STRING包装的，需要解析
                byte[] skidValue = parseOctetStringExtension(skidExtension);
                if (skidValue != null) {
                    String skid = TLVUtil.bytesToHex(skidValue);
                    // 从TLV格式数据中提取值
                    return TLVUtil.fetchValueFromTlv("04", skid);
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract Certificate Issuer Public Key Identifier: " + e.getMessage());
        }
        return null;
    }


    /**
     * 从Authority Key Identifier扩展中提取Key Identifier
     */
    private static byte[] extractKeyIdentifierFromAKID(byte[] akidExtension) {
        try {
            // AKID扩展值是OCTET STRING包装的SEQUENCE
            byte[] akidSequence = parseOctetStringExtension(akidExtension);
            if (akidSequence == null || akidSequence.length < 4) {
                return null;
            }

            // AKID SEQUENCE的第一个元素通常是Key Identifier (tag 0x80)
            int pos = 0;
            if (akidSequence[pos] == 0x30) { // SEQUENCE tag
                pos++;
                int length = akidSequence[pos] & 0xFF;
                pos++;

                while (pos < akidSequence.length) {
                    byte tag = akidSequence[pos];
                    pos++;

                    if (pos >= akidSequence.length) break;

                    int elementLength = akidSequence[pos] & 0xFF;
                    pos++;

                    if (tag == (byte) 0x80) { // Context-specific tag 0 (Key Identifier)
                        if (pos + elementLength <= akidSequence.length) {
                            byte[] keyId = new byte[elementLength];
                            System.arraycopy(akidSequence, pos, keyId, 0, elementLength);
                            return keyId;
                        }
                    }

                    pos += elementLength;
                }
            }
        } catch (Exception e) {
            log.info("Failed to parse Authority Key Identifier: " + e.getMessage());
        }
        return null;
    }

    /**
     * 解析OCTET STRING类型的扩展值
     */
    private static byte[] parseOctetStringExtension(byte[] extensionValue) {
        if (extensionValue == null || extensionValue.length < 2) {
            return null;
        }

        try {
            // 扩展值通常是OCTET STRING (tag 0x04) 包装的实际值
            if (extensionValue[0] == 0x04) {
                int length = extensionValue[1] & 0xFF;
                // 处理不定长度的情况
                if (length == 0x81) {
                    length = extensionValue[2] & 0xFF;
                    if (extensionValue.length >= 3 + length) {
                        byte[] result = new byte[length];
                        System.arraycopy(extensionValue, 3, result, 0, length);
                        return result;
                    }
                } else if (length == 0x82) {
                    length = ((extensionValue[2] & 0xFF) << 8) | (extensionValue[3] & 0xFF);
                    if (extensionValue.length >= 4 + length) {
                        byte[] result = new byte[length];
                        System.arraycopy(extensionValue, 4, result, 0, length);
                        return result;
                    }
                } else {
                    // 简单长度
                    if (extensionValue.length >= 2 + length) {
                        byte[] result = new byte[length];
                        System.arraycopy(extensionValue, 2, result, 0, length);
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            log.info("Failed to parse OCTET STRING extension: " + e.getMessage());
        }
        return null;
    }

    /**
     * 提取授权密钥标识符（AKID）
     */
    private static void extractAuthorityKeyIdentifier(X509Certificate certificate, Cert info) {
        try {
            byte[] akidExtension = certificate.getExtensionValue("2.5.29.35"); // Authority Key Identifier OID
            if (akidExtension != null) {
                byte[] keyIdentifier = extractKeyIdentifierFromAKID(akidExtension);
                if (keyIdentifier != null) {
                    info.setAkid(TLVUtil.bytesToHex(keyIdentifier));
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract Authority Key Identifier: " + e.getMessage());
        }
    }

    /**
     * 提取OID和主题备用名称
     */
    private static void extractOidAndAlternativeNames(X509Certificate certificate, Cert info) {
        List<String> alternativeNames = new ArrayList<>();
        String oid = "";

        try {
            Collection<List<?>> subjectAlternativeNames = certificate.getSubjectAlternativeNames();
            if (subjectAlternativeNames != null) {
                Iterator<List<?>> iterator = subjectAlternativeNames.iterator();
                while (iterator.hasNext()) {
                    List<?> alternativeName = iterator.next();
                    if (alternativeName != null && alternativeName.size() >= 2) {
                        // 记录所有主题备用名称
                        String type = String.valueOf(alternativeName.get(0));
                        String value = String.valueOf(alternativeName.get(1));
                        alternativeNames.add("Type " + type + ": " + value);

                        // 特别提取类型8（OID）
                        if ("8".equals(type)) {
                            oid = value;
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
            log.info("Failed to parse subject alternative names: " + e.getMessage());
        }

        info.setOid(oid);
        info.setSubjectAlternativeNames(alternativeNames);
    }

    /**
     * 仅提取OID
     *
     * @param certificate X509证书对象
     * @return 返回从证书中提取的OID字符串，如果提取失败或未找到则返回空字符串
     */
    public static String extractOid(X509Certificate certificate) {
        String oid = "";
        try {
            // 获取证书的主题备用名称集合
            Collection<List<?>> alternativeNames = certificate.getSubjectAlternativeNames();
            if (alternativeNames != null) {
                // 遍历备用名称列表，查找OID类型的数据
                Iterator<List<?>> iterator = alternativeNames.iterator();
                while (iterator.hasNext()) {
                    List<?> alternativeName = iterator.next();
                    // 检查备用名称列表是否有效且包含足够的元素，同时判断是否为OID类型(类型标识为8)
                    if (alternativeName != null && alternativeName.size() >= 2 &&
                            "8".equals(String.valueOf(alternativeName.get(0)))) {
                        oid = String.valueOf(alternativeName.get(1));
                        break;
                    }
                }
            }
        } catch (CertificateParsingException e) {
            log.info("Failed to extract OID: " + e.getMessage());
        }
        return oid;
    }


    public static String extractOid(String certData) {
        try {
            // 解析证书数据并提取公钥值
            X509Certificate certificate = parseCert(certData);
            return extractOid(certificate);
        } catch (Exception e) {
            log.info("Failed to extract OID " + e.getMessage());
            return null;
        }
    }

    /**
     * 仅提取公钥
     */
    public static PublicKey extractPublicKey(X509Certificate certificate) {
        return certificate != null ? certificate.getPublicKey() : null;
    }


    /**
     * 从证书数据中提取公钥值
     *
     * @param certData 证书数据字符串，可以是PEM格式或DER格式的证书
     * @return 返回提取的公钥值字符串，如果提取失败则返回null
     */
    public static String extractPublicKeyValue(String certData) {
        try {
            // 解析证书数据并提取公钥值
            X509Certificate certificate = parseCert(certData);
            return extractPublicKeyValue(certificate);
        } catch (Exception e) {
            log.info("Failed to extract public key value: " + e.getMessage());
            return null;
        }
    }


    /**
     * 从X509证书中提取公钥值并转换为十六进制字符串
     *
     * @param certificate X509证书对象，用于提取公钥
     * @return 公钥的十六进制字符串表示，如果提取失败或证书为空则返回null
     */
    public static String extractPublicKeyValue(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        try {
            // 获取证书公钥的编码字节数组并转换为十六进制字符串
            byte[] publicKeyBytes = certificate.getPublicKey().getEncoded();
            return TLVUtil.bytesToHex(publicKeyBytes);
        } catch (Exception e) {
            log.info("Failed to extract public key value: " + e.getMessage());
            return null;
        }
    }


    /**
     * 仅提取SKID
     */
    public static String extractSkid(X509Certificate certificate) {
        try {
            byte[] skidExtension = certificate.getExtensionValue("2.5.29.14");
            if (skidExtension != null) {
                byte[] skidValue = parseOctetStringExtension(skidExtension);
                return skidValue != null ? TLVUtil.bytesToHex(skidValue) : null;
            }
        } catch (Exception e) {
            log.info("Failed to extract SKID: " + e.getMessage());
        }
        return null;
    }

    /**
     * 仅提取CIPKID
     */
    public static String extractCipkid(X509Certificate certificate) {
        try {
            // 首先尝试从SKID获取
            String skid = extractSkid(certificate);
            if (skid != null) {
                return skid;
            }

            // 如果SKID为空，尝试从AKID获取
            byte[] akidExtension = certificate.getExtensionValue("2.5.29.35");
            if (akidExtension != null) {
                byte[] keyIdentifier = extractKeyIdentifierFromAKID(akidExtension);
                return keyIdentifier != null ? TLVUtil.bytesToHex(keyIdentifier) : null;
            }
        } catch (Exception e) {
            log.info("Failed to extract CIPKID: " + e.getMessage());
        }
        return null;
    }


    /**
     * 打印证书详细信息
     */
    public static void printCertificateDetails(X509Certificate certificate) {
        if (certificate == null) {
            log.debug("证书为空");
            return;
        }

        Cert info = extractFullInfo(certificate);

        log.debug("================= 证书详细信息 =================");
        log.debug("主题 (Subject): " + info.getSubject());
        log.debug("颁发者 (Issuer): " + info.getIssuer());
        log.debug("序列号 (Serial Number): " + info.getSerialNumber());
        log.debug("有效期从 (Not Before): " + info.getNotBefore());
        log.debug("有效期至 (Not After): " + info.getNotAfter());
        log.debug("版本 (Version): " + info.getVersion());
        log.debug("公钥算法 (Public Key Algorithm): " + info.getPublicKeyAlgorithm());
        log.debug("公钥值 (Public Key Value): " + info.getPublicKeyValue());
        log.debug("主题密钥标识符 (SKID): " + info.getSkid());
        log.debug("证书颁发者公钥标识符 (CIPKID): " + info.getCipkid());
        log.debug("授权密钥标识符 (AKID): " + info.getAkid());
        log.debug("OID: " + info.getOid());

        log.debug("主题备用名称 (Subject Alternative Names):");
        if (info.getSubjectAlternativeNames() != null && !info.getSubjectAlternativeNames().isEmpty()) {
            for (String san : info.getSubjectAlternativeNames()) {
                log.debug("  - " + san);
            }
        } else {
            log.debug("  - 无");
        }
        log.debug("==============================================");
    }

}