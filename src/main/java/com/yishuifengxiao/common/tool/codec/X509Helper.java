package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.text.TLVUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.ByteArrayInputStream;
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
    public static class Cert {
        private String subject;
        private String issuer;
        private String serialNumber;
        private String notBefore;
        private String notAfter;
        private int version;
        private PublicKey publicKey;
        private String publicKeyAlgorithm;
        private String publicKeyValue; // 公钥的原始字节值（十六进制）
        private String skid; // Subject Key Identifier
        private String cipkid; // Certificate Issuer Public Key Identifier
        private String oid;  // Object Identifier from Subject Alternative Names
        private List<String> subjectAlternativeNames;
        private String akid; // Authority Key Identifier

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
                // ECDSA公钥处理
                publicKeyValue = extractECDSAPublicKeyHex((ECPublicKey) publicKey);
            } else if (publicKey instanceof RSAPublicKey) {
                // RSA公钥处理
                publicKeyValue = extractRSAPublicKeyHex((RSAPublicKey) publicKey);
            } else {
                // 其他类型的公钥，使用完整编码
                byte[] publicKeyBytes = publicKey.getEncoded();
                publicKeyValue = bytesToHex(publicKeyBytes);
            }

            info.setPublicKeyValue(publicKeyValue.toUpperCase());
        } catch (Exception e) {
            System.err.println("Failed to extract public key value: " + e.getMessage());
            // 如果提取失败，使用完整编码作为后备
            try {
                byte[] publicKeyBytes = publicKey.getEncoded();
                info.setPublicKeyValue(bytesToHex(publicKeyBytes).toUpperCase());
            } catch (Exception ex) {
                System.err.println("Failed to extract public key encoded value: " + ex.getMessage());
            }
        }
    }

    /**
     * 提取RSA公钥的十六进制表示
     */
    private static String extractRSAPublicKeyHex(RSAPublicKey publicKey) {
        try {
            // 将RSA公钥编码为DER格式
            byte[] publicKeyDER = publicKey.getEncoded();
            return bytesToHex(publicKeyDER);
        } catch (Exception e) {
            System.err.println("Failed to extract RSA public key: " + e.getMessage());
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

            return bytesToHex(buf);
        } catch (Exception e) {
            System.err.println("Failed to extract ECDSA public key: " + e.getMessage());
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
                    info.setSkid(bytesToHex(skidValue));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract Subject Key Identifier: " + e.getMessage());
        }
    }

    private static void extractCipkid(X509Certificate certificate, Cert info) {
        Cert cipkid = info.setCipkid(TLVUtil.fetchValueFromTlv("04", info.getSkid()));
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
            System.err.println("Failed to parse Authority Key Identifier: " + e.getMessage());
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
            System.err.println("Failed to parse OCTET STRING extension: " + e.getMessage());
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
                    info.setAkid(bytesToHex(keyIdentifier));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to extract Authority Key Identifier: " + e.getMessage());
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
            System.err.println("Failed to parse subject alternative names: " + e.getMessage());
        }

        info.setOid(oid);
        info.setSubjectAlternativeNames(alternativeNames);
    }

    /**
     * 仅提取OID
     */
    public static String extractOid(X509Certificate certificate) {
        String oid = "";
        try {
            Collection<List<?>> alternativeNames = certificate.getSubjectAlternativeNames();
            if (alternativeNames != null) {
                Iterator<List<?>> iterator = alternativeNames.iterator();
                while (iterator.hasNext()) {
                    List<?> alternativeName = iterator.next();
                    if (alternativeName != null && alternativeName.size() >= 2 &&
                            "8".equals(String.valueOf(alternativeName.get(0)))) {
                        oid = String.valueOf(alternativeName.get(1));
                        break;
                    }
                }
            }
        } catch (CertificateParsingException e) {
            System.err.println("Failed to extract OID: " + e.getMessage());
        }
        return oid;
    }

    /**
     * 仅提取公钥
     */
    public static PublicKey extractPublicKey(X509Certificate certificate) {
        return certificate != null ? certificate.getPublicKey() : null;
    }

    /**
     * 仅提取公钥值（十六进制字符串）
     */
    public static String extractPublicKeyValue(X509Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        try {
            byte[] publicKeyBytes = certificate.getPublicKey().getEncoded();
            return bytesToHex(publicKeyBytes);
        } catch (Exception e) {
            System.err.println("Failed to extract public key value: " + e.getMessage());
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
                return skidValue != null ? bytesToHex(skidValue) : null;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract SKID: " + e.getMessage());
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
                return keyIdentifier != null ? bytesToHex(keyIdentifier) : null;
            }
        } catch (Exception e) {
            System.err.println("Failed to extract CIPKID: " + e.getMessage());
        }
        return null;
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
        return hexString.toString().toUpperCase();
    }

    /**
     * 打印证书详细信息
     */
    public static void printCertificateDetails(X509Certificate certificate) {
        if (certificate == null) {
            System.out.println("证书为空");
            return;
        }

        Cert info = extractFullInfo(certificate);

        System.out.println("================= 证书详细信息 =================");
        System.out.println("主题 (Subject): " + info.getSubject());
        System.out.println("颁发者 (Issuer): " + info.getIssuer());
        System.out.println("序列号 (Serial Number): " + info.getSerialNumber());
        System.out.println("有效期从 (Not Before): " + info.getNotBefore());
        System.out.println("有效期至 (Not After): " + info.getNotAfter());
        System.out.println("版本 (Version): " + info.getVersion());
        System.out.println("公钥算法 (Public Key Algorithm): " + info.getPublicKeyAlgorithm());
        System.out.println("公钥值 (Public Key Value): " + info.getPublicKeyValue());
        System.out.println("主题密钥标识符 (SKID): " + info.getSkid());
        System.out.println("证书颁发者公钥标识符 (CIPKID): " + info.getCipkid());
        System.out.println("授权密钥标识符 (AKID): " + info.getAkid());
        System.out.println("OID: " + info.getOid());

        System.out.println("主题备用名称 (Subject Alternative Names):");
        if (info.getSubjectAlternativeNames() != null && !info.getSubjectAlternativeNames().isEmpty()) {
            for (String san : info.getSubjectAlternativeNames()) {
                System.out.println("  - " + san);
            }
        } else {
            System.out.println("  - 无");
        }
        System.out.println("==============================================");
    }

}