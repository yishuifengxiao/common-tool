package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.HexUtil;
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
    private static final Pattern PEM_PATTERN = Pattern.compile("-----BEGIN CERTIFICATE-----.*-----END " + "CERTIFICATE-----", Pattern.DOTALL);

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
     * 解析十六进制字符串为字节数组
     *
     * @param hexString 十六进制编码的证书字符串，可能包含空格、0x或0X前缀
     * @return 解析得到的字节数组，如果字符串格式错误则返回null
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
     * 解析Base64字符串为字节数组
     *
     * @param base64String Base64编码的证书字符串，可能包含PEM头部和尾部
     * @return 解析得到的字节数组，如果解析失败则返回null
     */
    public static byte[] parseBase64String(String base64String) {
        // 移除可能的PEM头部和尾部（如果存在）
        String cleanBase64 = base64String.replace(PEM_HEADER, "").replace(PEM_FOOTER, "").replaceAll("\\s", "");

        return HexUtil.parseBase64String(cleanBase64);
    }

    /**
     * 解析PEM格式证书字符串
     *
     * @param pemData PEM格式的证书字符串，包含BEGIN和END标签
     * @return 解析得到的X509Certificate对象
     * @throws CertificateException 当PEM格式证书解析失败时抛出此异常
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
     * 从字节数组解析证书对象
     *
     * @param certBytes 证书的字节数组表示
     * @return 解析得到的X509Certificate对象
     * @throws CertificateException 当证书解析失败时抛出此异常
     */
    private static X509Certificate parseCertificateBytes(byte[] certBytes) throws CertificateException {
        try {
            // 创建X.509证书工厂并解析证书
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
        private Date notBefore;
        /**
         * Not After（失效时间） 形式为 Mon Aug 18 17:26:25 CST 2025
         */
        private Date notAfter;
        /**
         * X.509版本号从v1开始计数，但ASN.1编码中版本号从0开始：
         * v1 → 0（通常省略）
         * v2 → 1
         * v3 → 2
         * 因此，0x02（十进制2）对应v3版本。
         */
        private int version;
        /**
         * 定义如何签名和验证,签名算法字段。
         * 算法OID： 位置1：tbsCertificate.signature - 表示CA用来签署证书的算法，例如 ecdsa-with-SHA256。
         * 位置2：tbsCertificate.subjectPublicKeyInfo.algorithm - 这里的算法OID是 ecPublicKey (1.2.840.10045.2.1)
         * 。这告诉你这个公钥是基于ECC的。 在证书中通常出现在签名算法字段
         */
        private String sigAlgOID;
        /**
         * 算法名称
         */
        private String sigAlgName;
        /**
         * 公钥
         */
        private PublicKey publicKey;
        /**
         * 公钥算法
         */
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
        /**
         * Subject Alternative Names
         */
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
         * 使用ECC算法时曲线的OID.
         * 椭圆曲线参数标识符：指定使用NIST P-256椭圆曲线
         * <p>
         * 定义椭圆曲线的数学参数
         * <p>
         * 在证书中通常出现在公钥参数字段
         */
        String algid;

    }

    /**
     * 从证书字符串中提取完整证书信息
     *
     * @param certificate 证书字符串，通常为PEM或DER格式
     * @return 包含完整证书信息的Cert对象
     * @throws CertificateException 如果证书解析失败或格式不正确
     */
    public static Cert extractFullInfo(String certificate) throws CertificateException {
        // 将证书字符串解析为X509Certificate对象
        X509Certificate x509Certificate = parseCert(certificate);
        // 调用重载方法，使用X509Certificate对象提取证书信息
        return extractFullInfo(x509Certificate);
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
        info.setNotBefore(certificate.getNotBefore());
        info.setNotAfter(certificate.getNotAfter());
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
     * 提取证书中的公钥信息并设置到Cert对象中
     *
     * @param certificate X509证书对象，用于提取公钥信息
     * @param info        Cert信息对象，用于存储提取的公钥信息
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
                publicKeyValue = HexUtil.bytesToHex(publicKeyBytes);
            }

            info.setPublicKeyValue(publicKeyValue.toUpperCase());
        } catch (Exception e) {
            log.info("Failed to extract public key value: " + e.getMessage());
            // 如果提取失败，使用完整编码作为后备
            try {
                byte[] publicKeyBytes = publicKey.getEncoded();
                info.setPublicKeyValue(HexUtil.bytesToHex(publicKeyBytes).toUpperCase());
            } catch (Exception ex) {
                log.warn("Failed to extract public key encoded value: " + ex.getMessage());
            }
        }
    }


    /**
     * 从公钥编码中提取曲线OID
     *
     * @param encoded 公钥的字节编码数组
     * @return 曲线OID的点分十进制表示字符串
     * @throws IllegalArgumentException 当公钥编码格式不正确或长度不足时抛出
     */
    public static String getCurveOIDFromPublicKeyEncoding(byte[] encoded) {
        // 定位第二个OID的内容
        if (encoded.length < 23) {
            throw new IllegalArgumentException("公钥编码长度不足");
        }
        // 检查结构是否符合预期
        if (encoded[0] != 0x30 || encoded[2] != 0x30 || encoded[4] != 0x06 || encoded[13] != 0x06) {
            throw new IllegalArgumentException("非预期的公钥编码结构");
        }
        // 提取第二个OID的数据
        int oidLength = encoded[14];
        if (oidLength != 8) {
            throw new IllegalArgumentException("非预期的OID长度");
        }
        byte[] oidBytes = new byte[oidLength];
        System.arraycopy(encoded, 15, oidBytes, 0, oidLength);
        String hex = HexUtil.bytesToHex(oidBytes);
        String notation = OIDConverter.hexToDotNotation(hex);
        return notation;
    }


    /**
     * 提取RSA公钥的十六进制表示
     *
     * @param publicKey RSA公钥对象，用于提取编码
     * @return RSA公钥的十六进制表示字符串
     */
    public static String extractRSAPublicKeyHex(RSAPublicKey publicKey) {
        try {
            // 将RSA公钥编码为DER格式
            byte[] publicKeyDER = publicKey.getEncoded();
            return HexUtil.bytesToHex(publicKeyDER);
        } catch (Exception e) {
            log.info("Failed to extract RSA public key: " + e.getMessage());
            return "";
        }
    }

    /**
     * 提取ECDSA公钥的十六进制表示（未压缩格式）
     *
     * @param publicKey ECPublicKey对象，用于提取公钥点坐标
     * @return ECDSA公钥的十六进制表示字符串（未压缩格式）
     */
    public static String extractECDSAPublicKeyHex(ECPublicKey publicKey) {
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

            return HexUtil.bytesToHex(buf);
        } catch (Exception e) {
            log.info("Failed to extract ECDSA public key: " + e.getMessage());
            return "";
        }
    }

    /**
     * 提取主题密钥标识符（SKID）
     *
     * @param certificate X509证书对象，用于提取SKID扩展信息
     * @param info        证书信息对象，用于存储提取到的SKID值
     */
    private static void extractSubjectKeyIdentifier(X509Certificate certificate, Cert info) {
        try {
            // 获取Subject Key Identifier扩展值，OID为2.5.29.14
            byte[] skidExtension = certificate.getExtensionValue("2.5.29.14"); // Subject Key Identifier OID
            if (skidExtension != null) {
                // SKID扩展值是OCTET STRING包装的，需要解析
                byte[] skidValue = parseOctetStringExtension(skidExtension);
                if (skidValue != null) {
                    info.setSkid(HexUtil.bytesToHex(skidValue));
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract Subject Key Identifier: " + e.getMessage());
        }
    }


    /**
     * 从X509证书中提取CIPKID信息
     *
     * @param certificate X509证书对象，用于获取证书信息
     * @param info        证书信息对象，用于存储提取的CIPKID值
     */
    private static void extractCipkid(X509Certificate certificate, Cert info) {
        // 从TLV格式的SKID中提取标签为"04"的值作为CIPKID
        info.setCipkid(TLVUtil.fetchValueFromTlv("04", info.getSkid()));
    }


    /**
     * 从证书数据中提取CIPKID（Certificate Issuer Public Key Identifier）
     *
     * @param certData 证书数据字符串，用于解析和提取SKID信息
     * @return 返回提取到的CIPKID值，如果提取失败则返回null
     */
    public static String extractCipkid(String certData) {
        try {
            // 解析证书数据获取X509证书对象
            X509Certificate certificate = parseCert(certData);
            // 获取Subject Key Identifier扩展值（OID: 2.5.29.14）
            byte[] skidExtension = certificate.getExtensionValue("2.5.29.14"); // Subject Key Identifier OID
            if (skidExtension != null) {
                // SKID扩展值是OCTET STRING包装的，需要解析
                byte[] skidValue = parseOctetStringExtension(skidExtension);
                if (skidValue != null) {
                    String skid = HexUtil.bytesToHex(skidValue);
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
     * 从Authority Key Identifier扩展中提取Key Identifier（OID: 2.5.29.35）
     *
     * @param akidExtension Authority Key Identifier扩展值，用于提取Key Identifier
     * @return 返回提取到的Key Identifier值，如果提取失败则返回null
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
     * 解析OCTET STRING类型的扩展值，提取实际的字节数组
     *
     * @param extensionValue OCTET STRING类型的扩展值，包含包装的实际数据
     * @return 返回解析后的实际字节数组，如果解析失败则返回null
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
     * 提取授权密钥标识符（AKID），并将其转换为十六进制字符串存储在证书信息对象中
     *
     * @param certificate X509证书对象，用于获取扩展值
     * @param info        证书信息对象，用于存储提取的AKID值
     */
    private static void extractAuthorityKeyIdentifier(X509Certificate certificate, Cert info) {
        try {
            byte[] akidExtension = certificate.getExtensionValue("2.5.29.35"); // Authority Key Identifier OID
            if (akidExtension != null) {
                byte[] keyIdentifier = extractKeyIdentifierFromAKID(akidExtension);
                if (keyIdentifier != null) {
                    info.setAkid(HexUtil.bytesToHex(keyIdentifier));
                }
            }
        } catch (Exception e) {
            log.info("Failed to extract Authority Key Identifier: " + e.getMessage());
        }
    }

    /**
     * 提取OID和主题备用名称（SAN），并将其存储在证书信息对象中
     *
     * @param certificate X509证书对象，用于获取扩展值
     * @param info        证书信息对象，用于存储提取的OID和SAN值
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
                    if (alternativeName != null && alternativeName.size() >= 2 && "8".equals(String.valueOf(alternativeName.get(0)))) {
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


    /**
     * 从证书数据中提取OID标识符
     *
     * @param certData 证书数据字符串，用于解析和提取OID
     * @return 返回提取到的OID字符串，如果提取失败则返回null
     */
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
     * 从X509证书中提取公钥
     *
     * @param certificate X509证书对象，可以为null
     * @return 返回证书中的公钥，如果证书为null则返回null
     */
    public static PublicKey extractPublicKey(X509Certificate certificate) {
        // 使用三元运算符，如果证书不为null则返回其公钥，否则返回null
        return certificate != null ? certificate.getPublicKey() : null;
    }

    /**
     * 从证书字符串中提取公钥
     *
     * @param certificate 证书字符串，通常是PEM或DER格式的证书内容
     * @return 返回提取的公钥对象，如果证书解析失败则返回null
     * @throws CertificateException 当证书格式无效或解析过程中出现错误时抛出此异常
     */
    public static PublicKey extractPublicKey(String certificate) throws CertificateException {
        // 使用三元运算符判断证书是否解析成功，成功则返回公钥，失败则返回null
        X509Certificate x509Certificate = parseCert(certificate);
        return x509Certificate != null ? x509Certificate.getPublicKey() : null;
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
            return HexUtil.bytesToHex(publicKeyBytes);
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
                return skidValue != null ? HexUtil.bytesToHex(skidValue) : null;
            }
        } catch (Exception e) {
            log.info("Failed to extract SKID: " + e.getMessage());
        }
        return null;
    }

    /**
     * 仅提取CIPKID（证书颁发者公钥标识符），如果SKID为空则尝试从AKID获取
     *
     * @param certificate X509证书对象，用于提取CIPKID
     * @return 返回提取到的CIPKID字符串，如果提取失败则返回null
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
                return keyIdentifier != null ? HexUtil.bytesToHex(keyIdentifier) : null;
            }
        } catch (Exception e) {
            log.info("Failed to extract CIPKID: " + e.getMessage());
        }
        return null;
    }


    /**
     * 打印证书详细信息，包括主题、颁发者、序列号、有效期、版本、公钥算法、公钥值、SKID、CIPKID、AKID、OID和主题备用名称
     *
     * @param certificate X509证书对象，用于提取和打印详细信息
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

    /**
     * <p>
     * 判断证书B是否由证书A签发
     * </p>
     * <p>
     * 该方法通过验证证书B的签发者信息是否与证书A的主体信息匹配来判断签发关系。
     * 使用Java原生方法进行证书验证。
     * </p>
     *
     * @param issuerCert  签发者证书A
     * @param subjectCert 被签发证书B
     * @return true表示证书B由证书A签发，false表示不是
     */
    public static boolean isIssuedBy(X509Certificate issuerCert, X509Certificate subjectCert) {
        if (issuerCert == null || subjectCert == null) {
            throw new UncheckedException("证书对象不能为空");
        }

        try {
            // 验证签发者证书的主体名称是否与被签发证书的签发者名称匹配
            String issuerDN = issuerCert.getSubjectX500Principal().getName();
            String subjectIssuerDN = subjectCert.getIssuerX500Principal().getName();

            if (!issuerDN.equals(subjectIssuerDN)) {
                if (log.isDebugEnabled()) {
                    log.debug("签发者DN不匹配 - 签发者证书DN: {}, 被签发证书的签发者DN: {}", issuerDN, subjectIssuerDN);
                }
                return false;
            }

            // 使用Java原生方法验证签名
            // 如果证书B确实由证书A签发，那么使用证书A的公钥应该能验证证书B的签名
            subjectCert.verify(issuerCert.getPublicKey());
            return true;

        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("证书签发关系验证失败，签发者证书: {}, 被签发证书: {}, 错误信息: {}", issuerCert.getSubjectX500Principal(), subjectCert.getSubjectX500Principal(), e.getMessage());
            }
            return false;
        }
    }

    /**
     * <p>
     * 验证证书链的完整性
     * </p>
     * <p>
     * 验证证书链中每个证书是否由前一个证书签发，形成完整的信任链。
     * </p>
     *
     * @param certificateChain 证书链，按顺序排列（从终端证书到根证书）
     * @return true表示证书链完整有效，false表示不完整
     */
    public static boolean validateCertificateChain(X509Certificate[] certificateChain) {
        if (certificateChain == null || certificateChain.length < 2) {
            throw new UncheckedException("证书链至少需要包含两个证书");
        }

        for (int i = 0; i < certificateChain.length - 1; i++) {
            X509Certificate issuer = certificateChain[i + 1];
            X509Certificate subject = certificateChain[i];

            if (!isIssuedBy(issuer, subject)) {
                if (log.isWarnEnabled()) {
                    log.warn("证书链验证失败，第{}个证书不是由第{}个证书签发", i, i + 1);
                }
                return false;
            }
        }

        // 验证根证书是否自签名
        X509Certificate rootCert = certificateChain[certificateChain.length - 1];
        if (!isSelfSigned(rootCert)) {
            if (log.isWarnEnabled()) {
                log.warn("根证书不是自签名证书");
            }
            return false;
        }

        return true;
    }

    /**
     * <p>
     * 判断证书是否自签名
     * </p>
     *
     * @param cert 要检查的证书
     * @return true表示自签名，false表示不是
     */
    public static boolean isSelfSigned(X509Certificate cert) {
        if (cert == null) {
            return false;
        }

        try {
            // 自签名证书的签发者和主体名称相同
            String issuerDN = cert.getIssuerX500Principal().getName();
            String subjectDN = cert.getSubjectX500Principal().getName();

            if (!issuerDN.equals(subjectDN)) {
                return false;
            }

            // 验证签名（使用自己的公钥验证自己的签名）
            cert.verify(cert.getPublicKey());
            return true;
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("自签名验证失败，证书: {}, 错误信息: {}", cert.getSubjectX500Principal(), e.getMessage());
            }
            return false;
        }
    }

    /**
     * 将X509Certificate对象转换为PEM格式字符串
     *
     * @param certificate X.509证书对象
     * @return PEM格式的证书字符串
     * @throws CertificateException 当证书编码失败时抛出此异常
     */
    public static String toPemFormat(X509Certificate certificate) throws CertificateException {
        if (certificate == null) {
            throw new CertificateException("Certificate cannot be null");
        }

        try {
            byte[] certBytes = certificate.getEncoded();
            String base64Cert = Base64.getEncoder().encodeToString(certBytes);

            // 格式化PEM字符串，每64个字符换行
            StringBuilder pemBuilder = new StringBuilder();
            pemBuilder.append(PEM_HEADER).append("\n");

            for (int i = 0; i < base64Cert.length(); i += 64) {
                int endIndex = Math.min(i + 64, base64Cert.length());
                pemBuilder.append(base64Cert.substring(i, endIndex)).append("\n");
            }

            pemBuilder.append(PEM_FOOTER).append("\n");
            return pemBuilder.toString();
        } catch (Exception e) {
            throw new CertificateException("Failed to convert certificate to PEM format: " + e.getMessage(), e);
        }
    }

    /**
     * 将X509Certificate对象转换为十六进制格式字符串
     *
     * @param certificate X.509证书对象
     * @return 十六进制格式的证书字符串
     * @throws CertificateException 当证书编码失败时抛出此异常
     */
    public static String toHexFormat(X509Certificate certificate) throws CertificateException {
        if (certificate == null) {
            throw new CertificateException("Certificate cannot be null");
        }

        try {
            byte[] certBytes = certificate.getEncoded();
            return HexUtil.bytesToHex(certBytes).toUpperCase();
        } catch (Exception e) {
            throw new CertificateException("Failed to convert certificate to HEX format: " + e.getMessage(), e);
        }
    }

    /**
     * 将证书字符串转换为PEM格式
     *
     * @param certData 证书数据字符串，可以是十六进制、base64编码或PEM格式
     * @return PEM格式的证书字符串
     * @throws CertificateException 当证书解析或转换失败时抛出此异常
     */
    public static String convertToPem(String certData) throws CertificateException {
        X509Certificate certificate = parseCert(certData);
        return toPemFormat(certificate);
    }

    /**
     * 将证书字符串转换为十六进制格式
     *
     * @param certData 证书数据字符串，可以是十六进制、base64编码或PEM格式
     * @return 十六进制格式的证书字符串
     * @throws CertificateException 当证书解析或转换失败时抛出此异常
     */
    public static String convertToHex(String certData) throws CertificateException {
        X509Certificate certificate = parseCert(certData);
        return toHexFormat(certificate);
    }

    /**
     * 将PublicKey对象转换为PEM格式字符串
     *
     * @param publicKey 公钥对象
     * @return PEM格式的公钥字符串
     * @throws CertificateException 当公钥编码失败时抛出此异常
     */
    public static String publicKeyToPem(PublicKey publicKey) throws CertificateException {
        if (publicKey == null) {
            throw new CertificateException("PublicKey cannot be null");
        }

        try {
            byte[] keyBytes = publicKey.getEncoded();
            String base64Key = Base64.getEncoder().encodeToString(keyBytes);

            // 根据公钥类型确定PEM头部和尾部
            String pemHeader = getPublicKeyPemHeader(publicKey);
            String pemFooter = getPublicKeyPemFooter(publicKey);

            // 格式化PEM字符串，每64个字符换行
            StringBuilder pemBuilder = new StringBuilder();
            pemBuilder.append(pemHeader).append("\n");

            for (int i = 0; i < base64Key.length(); i += 64) {
                int endIndex = Math.min(i + 64, base64Key.length());
                pemBuilder.append(base64Key.substring(i, endIndex)).append("\n");
            }

            pemBuilder.append(pemFooter).append("\n");
            return pemBuilder.toString();
        } catch (Exception e) {
            throw new CertificateException("Failed to convert public key to PEM format: " + e.getMessage(), e);
        }
    }

    /**
     * 将PublicKey对象转换为十六进制格式字符串
     *
     * @param publicKey 公钥对象
     * @return 十六进制格式的公钥字符串
     * @throws CertificateException 当公钥编码失败时抛出此异常
     */
    public static String publicKeyToHex(PublicKey publicKey) throws CertificateException {
        if (publicKey == null) {
            throw new CertificateException("PublicKey cannot be null");
        }

        try {
            byte[] keyBytes = publicKey.getEncoded();
            return HexUtil.bytesToHex(keyBytes).toUpperCase();
        } catch (Exception e) {
            throw new CertificateException("Failed to convert public key to HEX format: " + e.getMessage(), e);
        }
    }

    /**
     * 根据公钥类型获取对应的PEM头部
     *
     * @param publicKey 公钥对象
     * @return 对应的PEM头部字符串
     */
    private static String getPublicKeyPemHeader(PublicKey publicKey) {
        String algorithm = publicKey.getAlgorithm().toUpperCase();
        switch (algorithm) {
            case "RSA":
                return "-----BEGIN RSA PUBLIC KEY-----";
            case "EC":
            case "ECDSA":
                return "-----BEGIN EC PUBLIC KEY-----";
            case "DSA":
                return "-----BEGIN DSA PUBLIC KEY-----";
            default:
                return "-----BEGIN PUBLIC KEY-----";
        }
    }

    /**
     * 根据公钥类型获取对应的PEM尾部
     *
     * @param publicKey 公钥对象
     * @return 对应的PEM尾部字符串
     */
    private static String getPublicKeyPemFooter(PublicKey publicKey) {
        String algorithm = publicKey.getAlgorithm().toUpperCase();
        switch (algorithm) {
            case "RSA":
                return "-----END RSA PUBLIC KEY-----";
            case "EC":
            case "ECDSA":
                return "-----END EC PUBLIC KEY-----";
            case "DSA":
                return "-----END DSA PUBLIC KEY-----";
            default:
                return "-----END PUBLIC KEY-----";
        }
    }

    /**
     * 解析公钥字符串，支持PEM格式和HEX格式
     *
     * @param publicKey 公钥字符串，可以是PEM格式或HEX格式
     * @return 解析成功的PublicKey对象
     * @throws CertificateException 当公钥解析失败时抛出此异常
     */
    public static PublicKey parsePublicKey(String publicKey) throws CertificateException {
        try {
            return parsePublicKeyFromPem(publicKey);
        } catch (CertificateException e) {
            // 记录PEM格式解析失败的日志，便于调试
            return parsePublicKeyFromHex(publicKey);
        } catch (Exception e) {
            // 捕获其他可能的解析异常，转换为统一的CertificateException
            throw new CertificateException("Failed to parse public key", e);
        }
    }


    /**
     * 从PEM格式的公钥字符串生成PublicKey对象
     *
     * @param pemPublicKey PEM格式的公钥字符串
     * @return 解析得到的PublicKey对象
     * @throws CertificateException 当公钥解析失败时抛出此异常
     */
    public static PublicKey parsePublicKeyFromPem(String pemPublicKey) throws CertificateException {
        if (pemPublicKey == null || pemPublicKey.trim().isEmpty()) {
            throw new CertificateException("PEM public key cannot be null or empty");
        }

        try {
            // 移除PEM头部和尾部，提取Base64部分
            String cleanedPem = pemPublicKey.trim();

            // 定义可能的PEM头部模式
            String[] pemHeaders = {"-----BEGIN RSA PUBLIC KEY-----", "-----BEGIN EC PUBLIC KEY-----", "-----BEGIN DSA PUBLIC KEY-----", "-----BEGIN PUBLIC KEY-----"};

            String[] pemFooters = {"-----END RSA PUBLIC KEY-----", "-----END EC PUBLIC KEY-----", "-----END DSA PUBLIC KEY-----", "-----END PUBLIC KEY-----"};

            // 移除所有可能的PEM头部和尾部
            for (String header : pemHeaders) {
                cleanedPem = cleanedPem.replace(header, "");
            }
            for (String footer : pemFooters) {
                cleanedPem = cleanedPem.replace(footer, "");
            }

            // 移除空白字符
            cleanedPem = cleanedPem.replaceAll("\\s", "");

            if (cleanedPem.isEmpty()) {
                throw new CertificateException("No Base64 data found in PEM public key");
            }

            // 解码Base64数据
            byte[] keyBytes = Base64.getDecoder().decode(cleanedPem);

            // 使用KeyFactory解析公钥
            return parsePublicKeyFromBytes(keyBytes);

        } catch (Exception e) {
            throw new CertificateException("Failed to parse public key from PEM format: " + e.getMessage(), e);
        }
    }

    /**
     * 从十六进制格式的公钥字符串生成PublicKey对象
     *
     * @param hexPublicKey 十六进制格式的公钥字符串
     * @return 解析得到的PublicKey对象
     * @throws CertificateException 当公钥解析失败时抛出此异常
     */
    public static PublicKey parsePublicKeyFromHex(String hexPublicKey) throws CertificateException {
        if (hexPublicKey == null || hexPublicKey.trim().isEmpty()) {
            throw new CertificateException("HEX public key cannot be null or empty");
        }

        try {
            // 清理十六进制字符串（移除空格、0x前缀等）
            String cleanHex = hexPublicKey.replaceAll("\\s", "").replace("0x", "").replace("0X", "");

            if (!HEX_PATTERN.matcher(cleanHex).matches() || cleanHex.length() % 2 != 0) {
                throw new CertificateException("Invalid HEX format for public key");
            }

            // 将十六进制字符串转换为字节数组
            byte[] keyBytes = new byte[cleanHex.length() / 2];
            for (int i = 0; i < cleanHex.length(); i += 2) {
                String byteStr = cleanHex.substring(i, i + 2);
                keyBytes[i / 2] = (byte) Integer.parseInt(byteStr, 16);
            }

            // 使用KeyFactory解析公钥
            return parsePublicKeyFromBytes(keyBytes);

        } catch (Exception e) {
            throw new CertificateException("Failed to parse public key from HEX format: " + e.getMessage(), e);
        }
    }

    /**
     * 从字节数组解析PublicKey对象
     *
     * @param keyBytes 公钥的字节数组表示
     * @return 解析得到的PublicKey对象
     * @throws CertificateException 当公钥解析失败时抛出此异常
     */
    private static PublicKey parsePublicKeyFromBytes(byte[] keyBytes) throws CertificateException {
        if (keyBytes == null || keyBytes.length == 0) {
            throw new CertificateException("Key bytes cannot be null or empty");
        }

        try {
            // 尝试使用X.509编码格式解析（标准公钥格式）
            try {
                java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);

                // 尝试RSA算法
                try {
                    java.security.KeyFactory rsaFactory = java.security.KeyFactory.getInstance("RSA");
                    return rsaFactory.generatePublic(keySpec);
                } catch (Exception e) {
                    // 继续尝试其他算法
                }

                // 尝试EC算法
                try {
                    java.security.KeyFactory ecFactory = java.security.KeyFactory.getInstance("EC");
                    return ecFactory.generatePublic(keySpec);
                } catch (Exception e) {
                    // 继续尝试其他算法
                }

                // 尝试DSA算法
                try {
                    java.security.KeyFactory dsaFactory = java.security.KeyFactory.getInstance("DSA");
                    return dsaFactory.generatePublic(keySpec);
                } catch (Exception e) {
                    // 继续尝试其他算法
                }
            } catch (Exception e) {
                // 如果X.509格式失败，尝试PKCS#1格式（RSA专用）
                try {
                    // 对于RSA公钥，可能需要使用PKCS#1格式
                    if (keyBytes.length > 0 && (keyBytes[0] & 0xFF) == 0x30) {
                        // 可能是DER编码的RSA公钥
                        java.security.KeyFactory rsaFactory = java.security.KeyFactory.getInstance("RSA");
                        java.security.spec.RSAPublicKeySpec rsaSpec = parseRSAPublicKeyFromDER(keyBytes);
                        if (rsaSpec != null) {
                            return rsaFactory.generatePublic(rsaSpec);
                        }
                    }
                } catch (Exception ex) {
                    // 继续尝试其他方法
                }
            }

            throw new CertificateException("Unable to determine public key algorithm from bytes");

        } catch (Exception e) {
            throw new CertificateException("Failed to parse public key from bytes: " + e.getMessage(), e);
        }
    }

    /**
     * 从DER编码的字节数组解析RSA公钥规范
     *
     * @param derBytes DER编码的RSA公钥字节数组
     * @return RSAPublicKeySpec对象，如果解析失败则返回null
     */
    private static java.security.spec.RSAPublicKeySpec parseRSAPublicKeyFromDER(byte[] derBytes) {
        try {
            // 简单的DER解析：SEQUENCE { INTEGER (modulus), INTEGER (exponent) }
            if (derBytes.length < 10 || derBytes[0] != 0x30) {
                return null;
            }

            int pos = 1;
            int sequenceLength = derBytes[pos] & 0xFF;
            pos++;

            // 处理不定长度
            if (sequenceLength == 0x81) {
                sequenceLength = derBytes[pos] & 0xFF;
                pos++;
            } else if (sequenceLength == 0x82) {
                sequenceLength = ((derBytes[pos] & 0xFF) << 8) | (derBytes[pos + 1] & 0xFF);
                pos += 2;
            }

            // 读取模数（第一个INTEGER）
            if (derBytes[pos] != 0x02) {
                return null;
            }
            pos++;

            int modulusLength = derBytes[pos] & 0xFF;
            pos++;

            // 处理不定长度
            if (modulusLength == 0x81) {
                modulusLength = derBytes[pos] & 0xFF;
                pos++;
            } else if (modulusLength == 0x82) {
                modulusLength = ((derBytes[pos] & 0xFF) << 8) | (derBytes[pos + 1] & 0xFF);
                pos += 2;
            }

            byte[] modulusBytes = new byte[modulusLength];
            System.arraycopy(derBytes, pos, modulusBytes, 0, modulusLength);
            pos += modulusLength;

            // 读取指数（第二个INTEGER）
            if (derBytes[pos] != 0x02) {
                return null;
            }
            pos++;

            int exponentLength = derBytes[pos] & 0xFF;
            pos++;

            byte[] exponentBytes = new byte[exponentLength];
            System.arraycopy(derBytes, pos, exponentBytes, 0, exponentLength);

            BigInteger modulus = new BigInteger(1, modulusBytes);
            BigInteger exponent = new BigInteger(1, exponentBytes);

            return new java.security.spec.RSAPublicKeySpec(modulus, exponent);

        } catch (Exception e) {
            return null;
        }
    }

}