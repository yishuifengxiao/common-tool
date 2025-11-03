package com.yishuifengxiao.common.tool.text;

import com.yishuifengxiao.common.tool.lang.HexUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * TLV格式字符串解析工具类
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class TLVFormatter {

    /**
     * OID信息存储类
     */
    private static class OIDInfo {
        private final String description;
        private final String shortDesc;

        public OIDInfo(String description, String shortDesc) {
            this.description = description;
            this.shortDesc = shortDesc;
        }

        @Override
        public String toString() {
            return description + " - " + shortDesc;
        }
    }

    /**
     * OID映射表
     */
    private static final Map<String, OIDInfo> OID_MAPPINGS = new HashMap<>();

    static {
        // 初始化OID映射表
        OID_MAPPINGS.put("2.5.4.3", new OIDInfo("Common Name (CN)", "通用名称,通常代表域名或主体名"));
        OID_MAPPINGS.put("2.5.4.6", new OIDInfo("Country (C)", "国家/地区,使用双字符代码"));
        OID_MAPPINGS.put("2.5.4.7", new OIDInfo("Locality (L)", "地理位置,如城市或地理区域"));
        OID_MAPPINGS.put("2.5.4.8", new OIDInfo("State or Province Name (ST)", "州或省名称"));
        OID_MAPPINGS.put("2.5.4.10", new OIDInfo("Organization (O)", "组织或机构名称"));
        OID_MAPPINGS.put("2.5.4.11", new OIDInfo("Organizational Unit (OU)", "组织单元名称"));
        OID_MAPPINGS.put("2.5.29.17", new OIDInfo("subjectAltName", "主题备用名称,包含非X.500名称(如邮箱、DNS)"));
        OID_MAPPINGS.put("2.5.29.19", new OIDInfo("basicConstraints", "基本约束,标识证书是否为CA证书及路径长度"));
        OID_MAPPINGS.put("2.5.29.15", new OIDInfo("keyUsage", "密钥用法,规定证书公钥的用途"));
        OID_MAPPINGS.put("2.5.29.37", new OIDInfo("extendedKeyUsage", "扩展密钥用法,规定证书更具体的用途"));
        OID_MAPPINGS.put("2.5.29.32", new OIDInfo("certificatePolicies", "证书策略,包含证书颁发和使用的策略信息"));
        OID_MAPPINGS.put("2.5.29.35", new OIDInfo("authorityKeyIdentifier", "授权密钥标识符,标识签发证书的CA密钥"));
        OID_MAPPINGS.put("2.5.4.5", new OIDInfo("Serial Number", "序列号,设备或证书的序列标识"));
        OID_MAPPINGS.put("2.5.4.9", new OIDInfo("Street Address", "街道地址"));
        OID_MAPPINGS.put("2.5.4.12", new OIDInfo("Title", "职位或头衔"));
        OID_MAPPINGS.put("2.5.4.13", new OIDInfo("Description", "描述信息"));
        OID_MAPPINGS.put("2.5.4.17", new OIDInfo("Postal Code", "邮政编码"));
        OID_MAPPINGS.put("2.5.4.20", new OIDInfo("Telephone Number", "电话号码"));
        OID_MAPPINGS.put("2.5.4.42", new OIDInfo("Given Name", "名字"));
        OID_MAPPINGS.put("2.5.4.43", new OIDInfo("Initials", "姓名首字母"));
        OID_MAPPINGS.put("2.5.4.44", new OIDInfo("Generation Qualifier", "代次限定符(如Jr., Sr.)"));
        OID_MAPPINGS.put("2.5.29.14", new OIDInfo("subjectKeyIdentifier", "主体密钥标识符"));
        OID_MAPPINGS.put("2.5.29.16", new OIDInfo("privateKeyUsagePeriod", "私钥使用期限"));
        OID_MAPPINGS.put("2.5.29.31", new OIDInfo("CRL Distribution Points", "CRL分发点"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.3.1", new OIDInfo("serverAuth", "TLS Web服务器认证"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.3.2", new OIDInfo("clientAuth", "TLS Web客户端认证"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.3.3", new OIDInfo("codeSigning", "代码签名"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.3.4", new OIDInfo("emailProtection", "电子邮件保护"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.3.5", new OIDInfo("ipsecEndSystem", "IPsec终端系统"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.3.8", new OIDInfo("timeStamping", "时间戳"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.3.9", new OIDInfo("OCSPSigning", "OCSP签名"));
        OID_MAPPINGS.put("2.23.140.1.2.1", new OIDInfo("CA/Browser Forum - DV", "域名验证证书"));
        OID_MAPPINGS.put("2.23.140.1.2.2", new OIDInfo("CA/Browser Forum - OV", "组织验证证书"));
        OID_MAPPINGS.put("2.23.140.1.2.3", new OIDInfo("CA/Browser Forum - IV", "个人验证证书"));
        OID_MAPPINGS.put("2.23.140.1.2.4", new OIDInfo("CA/Browser Forum - EV", "扩展验证证书"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.48.1", new OIDInfo("OCSP - OCSP", "OCSP服务端点"));
        OID_MAPPINGS.put("1.3.6.1.5.5.7.48.2", new OIDInfo("CA Issuers", "CA证书颁发端点"));
    }

    /**
     * 格式化TLV字符串
     *
     * @param beautiful 是否以美化格式输出(true为美化格式,false为紧凑格式)
     * @param val       待格式化的字符串,可以是十六进制或Base64编码
     * @return 格式化后的TLV字符串
     * @throws IllegalArgumentException 如果输入格式无效
     */
    public static String formatTLVStr(boolean beautiful, String val) {
        String tlvRaw;

        // 验证输入字符串是否为有效的十六进制表示
        if (!HexUtil.isHex(val)) {
            // 如果不是有效的十六进制,则尝试作为Base64编码的字符串进行解码
            try {
                // 先检查是否为PEM格式
                if (isPEMFormat(val)) {
                    // 如果是PEM格式，先提取Base64内容
                    String base64Content = extractBase64FromPEM(val);
                    tlvRaw = base64StringToHex(base64Content);
                } else {
                    // 如果不是PEM格式，先移除空白字符再尝试Base64解码
                    String cleanedVal = removeInvisibleChars(val);
                    tlvRaw = base64StringToHex(cleanedVal);
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("输入既不是有效的十六进制也不是有效的Base64编码或PEM格式", e);
            }
        } else {
            // 如果是有效的十六进制,则直接使用输入字符串进行处理
            tlvRaw = val;
        }

        // 对处理后的字符串进行TLV格式化
        try {
            return formatTLV(beautiful, tlvRaw);
        } catch (Exception e) {
            log.info("Error: " + e.getMessage());
            throw new IllegalArgumentException("TLV格式化失败", e);
        }
    }

    /**
     * 检查字符串是否为PEM格式
     *
     * @param str 待检查的字符串
     * @return 如果是PEM格式返回true，否则返回false
     */
    private static boolean isPEMFormat(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }

        String trimmed = str.trim();
        // PEM格式通常以"-----BEGIN"开头，以"-----END"结尾
        return trimmed.startsWith("-----BEGIN") && trimmed.contains("-----END");
    }

    /**
     * 从PEM格式字符串中提取Base64编码的内容
     *
     * @param pemStr PEM格式的字符串
     * @return 提取出的Base64内容
     * @throws IllegalArgumentException 如果PEM格式无效
     */
    private static String extractBase64FromPEM(String pemStr) {
        if (!isPEMFormat(pemStr)) {
            throw new IllegalArgumentException("输入不是有效的PEM格式");
        }

        // 按行分割
        String[] lines = pemStr.split("\\r?\\n");
        StringBuilder base64Content = new StringBuilder();

        // 跳过PEM头标记，提取Base64内容
        boolean inBase64Section = false;
        for (String line : lines) {
            String trimmedLine = line.trim();

            if (trimmedLine.startsWith("-----BEGIN")) {
                inBase64Section = true;
                continue;
            }

            if (trimmedLine.startsWith("-----END")) {
                break;
            }

            if (inBase64Section && !trimmedLine.isEmpty()) {
                base64Content.append(trimmedLine);
            }
        }

        if (base64Content.length() == 0) {
            throw new IllegalArgumentException("PEM格式中未找到有效的Base64内容");
        }

        return base64Content.toString();
    }

    /**
     * 将TLV格式的十六进制字符串解码为可读的字符串表示形式
     *
     * @param beautiful 是否优雅格式化,即对于非构造类型标签的标签值进行格式化
     * @param hexStr    包含TLV数据的十六进制字符串
     * @return 解析后的TLV数据的字符串表示
     * @throws IllegalArgumentException 如果解析过程中发生错误
     */
    public static String formatTLV(boolean beautiful, String hexStr) {
        byte[] data;
        try {
            data = hexStringToByteArray(hexStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的十六进制字符串", e);
        }

        try {
            ParseResult result = parseTLV(beautiful, data, 0);
            return result.output;
        } catch (Exception e) {
            throw new IllegalArgumentException("TLV解析失败", e);
        }
    }

    // 解析结果类
    private static class ParseResult {
        final String output;
        final int bytesParsed;

        ParseResult(String output, int bytesParsed) {
            this.output = output;
            this.bytesParsed = bytesParsed;
        }
    }

    /**
     * 值转换结果类
     */
    private static class ValueConversionResult {
        final String valueStr;
        final boolean converted;
        final String asnType;

        ValueConversionResult(String valueStr, boolean converted, String asnType) {
            this.valueStr = valueStr;
            this.converted = converted;
            this.asnType = asnType;
        }
    }

    /**
     * 解析TLV结构的核心方法
     *
     * @param beautiful   是否以美化格式输出(true为美化格式,false为紧凑格式)
     * @param data        包含TLV数据的字节数组
     * @param indentLevel 当前缩进级别,用于美化输出
     * @return 解析结果, 包含格式化后的字符串和解析的字节数
     */
    private static ParseResult parseTLV(boolean beautiful, byte[] data, int indentLevel) {
        StringBuilder output = new StringBuilder();
        int position = 0;
        int maxPosition = data.length;

        while (position < maxPosition) {
            int tagStart = position;

            // 解析TAG
            if (position >= maxPosition) {
                throw new IllegalArgumentException("invalid tag");
            }

            byte firstByte = data[position];
            position++;

            // 判断TAG是否需要更多字节
            int tagBytes = 1;
            if ((firstByte & 0x1F) == 0x1F) {
                // TAG需要多字节表示
                while (position < maxPosition) {
                    byte b = data[position];
                    position++;
                    tagBytes++;
                    // 检查是否还有后续字节 (最高位为0表示结束)
                    if ((b & 0x80) == 0) {
                        break;
                    }
                }
            }

            int tagEnd = tagStart + tagBytes;

            // 解析LEN
            if (position >= maxPosition) {
                throw new IllegalArgumentException("missing length");
            }

            byte lenByte = data[position];
            position++;

            int length = 0;
            int lenBytesConsumed = 1;

            if (lenByte == (byte) 0x81 && position < maxPosition) {
                length = data[position] & 0xFF;
                position++;
                lenBytesConsumed = 2;
            } else if (lenByte == (byte) 0x82 && position + 1 < maxPosition) {
                length = ((data[position] & 0xFF) << 8) | (data[position + 1] & 0xFF);
                position += 2;
                lenBytesConsumed = 3;
            } else if (lenByte == (byte) 0x83 && position + 2 < maxPosition) {
                length = ((data[position] & 0xFF) << 16) | ((data[position + 1] & 0xFF) << 8) | (data[position + 2] & 0xFF);
                position += 3;
                lenBytesConsumed = 4;
            } else if (lenByte == (byte) 0x84 && position + 3 < maxPosition) {
                length = ((data[position] & 0xFF) << 24) | ((data[position + 1] & 0xFF) << 16) |
                        ((data[position + 2] & 0xFF) << 8) | (data[position + 3] & 0xFF);
                position += 4;
                lenBytesConsumed = 5;
            } else {
                length = lenByte & 0xFF;
            }

            // 有效性校验
            if (position + length > maxPosition) {
                throw new IllegalArgumentException("value overflow at " + position);
            }

            // 生成当前TLV行
            String indent = "    ".repeat(indentLevel);
            String tagHex = bytesToHex(data, tagStart, tagEnd);
            String lenHex = bytesToHex(data, tagEnd, tagEnd + lenBytesConsumed);

            // 处理VALUE
            byte[] valueData = new byte[length];
            System.arraycopy(data, position, valueData, 0, length);

            // 检查是否可能包含嵌套TLV结构
            boolean hasNestedTLV = false;

            if ((firstByte & 0x20) != 0 || mightContainTLV(valueData)) {
                try {
                    ParseResult subResult = parseTLV(beautiful, valueData, indentLevel + 1);
                    if (subResult.bytesParsed > 0) {
                        hasNestedTLV = true;
                        output.append(String.format("%s%s %s\n%s", indent, tagHex, lenHex, subResult.output));
                    }
                } catch (Exception e) {
                    // 递归解析失败，继续按常规方式处理
                }
            }

            // 如果不是嵌套TLV或者递归解析失败，按常规方式处理
            if (!hasNestedTLV) {
                if (beautiful) {
                    ValueConversionResult conversion = tryConvertValue(tagHex, valueData);
                    String valueHex = bytesToHex(valueData);

                    if (conversion.converted) {
                        if (!conversion.valueStr.isEmpty()) {
                            output.append(String.format("%s%s %s %s (%s, %s)\n",
                                    indent, tagHex, lenHex, valueHex, conversion.asnType, conversion.valueStr));
                        } else {
                            output.append(String.format("%s%s %s %s (%s)\n",
                                    indent, tagHex, lenHex, valueHex, conversion.asnType));
                        }
                    } else {
                        output.append(String.format("%s%s %s %s\n", indent, tagHex, lenHex, valueHex));
                    }
                } else {
                    String valueHex = bytesToHex(valueData);
                    output.append(String.format("%s%s %s %s\n", indent, tagHex, lenHex, valueHex));
                }
            }

            position += length;
        }

        if (position != maxPosition) {
            throw new IllegalArgumentException("invalid TLV data: " + (maxPosition - position) + " bytes remaining after parsing");
        }

        return new ParseResult(output.toString(), position);
    }

    // 尝试转换值的辅助方法 - 完整实现Go中的所有类型
    private static ValueConversionResult tryConvertValue(String tagHex, byte[] valueData) {
        switch (tagHex) {
            case "0C": // UTF8String
                if (isValidUTF8(valueData)) {
                    String str = new String(valueData, StandardCharsets.UTF_8);
                    return new ValueConversionResult("'" + str + "'", true, "UTF8String");
                }
                break;
            case "1C": // GraphicString
                String graphicStr = new String(valueData, StandardCharsets.ISO_8859_1);
                return new ValueConversionResult("'" + graphicStr + "'", true, "GraphicString");
            case "16": // IA5String (ASCII)
                if (isASCII(valueData)) {
                    String ia5Str = new String(valueData, StandardCharsets.US_ASCII);
                    return new ValueConversionResult("'" + ia5Str + "'", true, "IA5String");
                }
                break;
            case "13": // PrintableString
                if (isPrintableString(valueData)) {
                    String printableStr = new String(valueData, StandardCharsets.US_ASCII);
                    return new ValueConversionResult("'" + printableStr + "'", true, "PrintableString");
                }
                break;
            case "14": // TeletexString
                String teletexStr = latin1ToString(valueData);
                return new ValueConversionResult("'" + teletexStr + "'", true, "TeletexString");
            case "1E": // BMPString (UCS-2)
                String bmpStr = bmpToString(valueData);
                if (!bmpStr.isEmpty()) {
                    return new ValueConversionResult("'" + bmpStr + "'", true, "BMPString");
                }
                break;
            case "17": // UTCTime
                String utcTime = parseUTCTime(valueData);
                if (utcTime != null) {
                    return new ValueConversionResult(utcTime, true, "UTCTime");
                }
                break;
            case "18": // GeneralizedTime
                String generalizedTime = parseGeneralizedTime(valueData);
                if (generalizedTime != null) {
                    return new ValueConversionResult(generalizedTime, true, "GeneralizedTime");
                }
                break;
            case "06": // OBJECT IDENTIFIER
                String oidStr = parseObjectIdentifier(valueData);
                if (oidStr != null) {
                    OIDInfo oidInfo = getOIDInfo(oidStr);
                    if (oidInfo != null) {
                        return new ValueConversionResult(oidStr + " [" + oidInfo + "]", true, "OBJECT IDENTIFIER");
                    }
                    return new ValueConversionResult(oidStr, true, "OBJECT IDENTIFIER");
                }
                break;
            case "01": // BOOLEAN
                String boolStr = parseBoolean(valueData);
                if (boolStr != null) {
                    return new ValueConversionResult(boolStr, true, "BOOLEAN");
                }
                break;
            case "5A": // 解析TAG 5A标签
                String tag5AStr = parseTag5A(valueData);
                if (tag5AStr != null) {
                    return new ValueConversionResult(tag5AStr, true, "Tag 5A");
                }
                break;
            case "12": // NumericString
                String numStr = parseNumericString(valueData);
                if (numStr != null) {
                    return new ValueConversionResult(numStr, true, "NumericString");
                }
                break;
            case "02": // INTEGER
                String intStr = parseInteger(valueData);
                if (intStr != null) {
                    return new ValueConversionResult(intStr, true, "INTEGER");
                }
                break;
            case "09": // REAL
                String realStr = parseReal(valueData);
                if (realStr != null) {
                    return new ValueConversionResult(realStr, true, "REAL");
                }
                break;
            case "03": // BIT STRING
                if (valueData.length > 0) {
                    int unusedBits = valueData[0] & 0xFF;
                    return new ValueConversionResult("unused bits: " + unusedBits, true, "BIT STRING");
                }
                break;
            case "04": // OCTET STRING
                return new ValueConversionResult("", true, "OCTET STRING");
            case "05": // NULL
                return new ValueConversionResult("NULL", true, "NULL");
            case "30": // SEQUENCE
                return new ValueConversionResult("[SEQUENCE]", true, "SEQUENCE");
            case "31": // SET
                return new ValueConversionResult("[SET]", true, "SET");
        }

        return new ValueConversionResult("", false, "");
    }

    // 检查是否可能包含TLV结构
    private static boolean mightContainTLV(byte[] data) {
        if (data.length < 2) {
            return false;
        }

        byte firstByte = data[0];

        if (data.length >= 3) {
            if ((firstByte & 0x1F) != 0x1F) {
                byte lenByte = data[1];
                if (lenByte <= 0x7F || (lenByte >= (byte) 0x81 && lenByte <= (byte) 0x84)) {
                    return true;
                }
            } else {
                if (data.length >= 4 && (data[1] & 0x80) != 0) {
                    return true;
                }
            }
        }

        return false;
    }

    // ========== 类型解析方法 ==========

    // 解析NumericString
    private static String parseNumericString(byte[] data) {
        for (byte b : data) {
            if (!((b >= '0' && b <= '9') || b == ' ')) {
                return null;
            }
        }
        return "'" + new String(data, StandardCharsets.US_ASCII) + "'";
    }

    // 解析INTEGER
    private static String parseInteger(byte[] data) {
        if (data.length == 0) {
            return "0";
        }

        // 处理不同长度的整数
        long result;
        switch (data.length) {
            case 1:
                result = data[0];
                break;
            case 2:
                result = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                // 检查是否为负数
                if ((data[0] & 0x80) != 0) {
                    result = result - 0x10000;
                }
                break;
            case 4:
                result = ((data[0] & 0xFFL) << 24) | ((data[1] & 0xFFL) << 16) |
                        ((data[2] & 0xFFL) << 8) | (data[3] & 0xFFL);
                // 检查是否为负数
                if ((data[0] & 0x80) != 0) {
                    result = result - 0x100000000L;
                }
                break;
            case 8:
                result = ((data[0] & 0xFFL) << 56) | ((data[1] & 0xFFL) << 48) |
                        ((data[2] & 0xFFL) << 40) | ((data[3] & 0xFFL) << 32) |
                        ((data[4] & 0xFFL) << 24) | ((data[5] & 0xFFL) << 16) |
                        ((data[6] & 0xFFL) << 8) | (data[7] & 0xFFL);
                break;
            default:
                // 对于非常长的整数，使用大数表示
                if (data.length <= 16) {
                    BigInteger bigInt = new BigInteger(data);
                    return bigInt.toString();
                } else {
                    // 对于非常大的整数，返回十六进制表示
                    return "0x" + bytesToHex(data);
                }
        }

        return String.valueOf(result);
    }

    // 解析REAL类型
    private static String parseReal(byte[] data) {
        if (data.length == 0) {
            return "0.0";
        }

        byte firstByte = data[0];

        // 检查是否为特殊值(无穷大或NaN)
        if ((firstByte & 0x40) != 0) {
            if ((firstByte & 0x01) != 0) {
                return "+INF";
            } else if ((firstByte & 0x02) != 0) {
                return "-INF";
            } else if ((firstByte & 0x03) != 0) {
                return "NaN";
            }
        }

        // 检查编码格式
        if ((firstByte & 0x80) == 0) {
            // 二进制编码
            return parseBinaryReal(data);
        } else {
            // 十进制编码
            return parseDecimalReal(data);
        }
    }

    // 解析二进制编码的REAL
    private static String parseBinaryReal(byte[] data) {
        if (data.length < 2) {
            return null;
        }

        byte firstByte = data[0];

        // 提取符号位
        double sign = 1.0;
        if ((firstByte & 0x40) != 0) {
            sign = -1.0;
        }

        // 提取底数和指数
        int base = (firstByte >> 4) & 0x03;
        int exponentFormat = (firstByte >> 2) & 0x03;

        int exponent = 0;
        long mantissa = 0;

        // 解析指数
        int expStart = 1;
        switch (exponentFormat) {
            case 0: // 1字节指数
                if (data.length < 2) return null;
                exponent = data[1];
                expStart = 2;
                break;
            case 1: // 2字节指数
                if (data.length < 3) return null;
                exponent = ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
                expStart = 3;
                break;
            case 2: // 3字节指数
                if (data.length < 4) return null;
                // 3字节有符号整数
                int expValue = ((data[1] & 0xFF) << 16) | ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
                if ((expValue & 0x800000) != 0) {
                    exponent = expValue - 0x1000000;
                } else {
                    exponent = expValue;
                }
                expStart = 4;
                break;
            default: // 保留
                return null;
        }

        // 解析尾数
        byte[] mantissaBytes = Arrays.copyOfRange(data, expStart, data.length);

        switch (base) {
            case 0: // 底数为2
                if (mantissaBytes.length == 0) {
                    return "0.0";
                }

                // 将尾数字节转换为整数
                mantissa = 0;
                for (byte b : mantissaBytes) {
                    mantissa = (mantissa << 8) | (b & 0xFF);
                }

                // 计算浮点值
                double value = mantissa * Math.pow(2, exponent);
                return formatDouble(value * sign);

            case 1: // 底数为8
                if (mantissaBytes.length == 0) {
                    return "0.0";
                }

                mantissa = 0;
                for (byte b : mantissaBytes) {
                    mantissa = (mantissa << 8) | (b & 0xFF);
                }

                double value8 = mantissa * Math.pow(8, exponent);
                return formatDouble(value8 * sign);

            case 2: // 底数为16
                if (mantissaBytes.length == 0) {
                    return "0.0";
                }

                mantissa = 0;
                for (byte b : mantissaBytes) {
                    mantissa = (mantissa << 8) | (b & 0xFF);
                }

                double value16 = mantissa * Math.pow(16, exponent);
                return formatDouble(value16 * sign);

            default: // 保留
                return null;
        }
    }

    // 解析十进制编码的REAL
    private static String parseDecimalReal(byte[] data) {
        if (data.length < 2) {
            return null;
        }

        byte firstByte = data[0];

        // 提取符号位
        String sign = "";
        if ((firstByte & 0x40) != 0) {
            sign = "-";
        }

        // 提取NR格式
        int nrFormat = (firstByte >> 2) & 0x03;

        // 解析数值部分
        String numberStr;
        int exponent = 0;

        String dataStr = new String(data, 1, data.length - 1, StandardCharsets.US_ASCII);

        switch (nrFormat) {
            case 0: // NR1格式 - 整数形式
                numberStr = dataStr;
                exponent = 0;
                break;
            case 1: // NR2格式 - 固定小数位
                int decimalPoint = dataStr.indexOf('.');
                if (decimalPoint == -1) {
                    return null;
                }

                numberStr = dataStr;
                exponent = dataStr.length() - decimalPoint - 1;
                break;
            case 2: // NR3格式 - 科学计数法
                int ePos = -1;
                for (int i = 0; i < dataStr.length(); i++) {
                    char c = dataStr.charAt(i);
                    if (c == 'E' || c == 'e') {
                        ePos = i;
                        break;
                    }
                }

                if (ePos == -1) {
                    return null;
                }

                // 解析尾数部分
                numberStr = dataStr.substring(0, ePos);

                // 解析指数部分
                String expStr = dataStr.substring(ePos + 1);
                try {
                    exponent = Integer.parseInt(expStr);
                } catch (NumberFormatException e) {
                    return null;
                }
                break;
            default: // 保留
                return null;
        }

        // 转换为浮点数
        try {
            double value = Double.parseDouble(numberStr);
            // 应用指数
            value *= Math.pow(10, exponent);
            return sign + formatDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 格式化双精度浮点数
    private static String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("0.####################");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
        df.setDecimalFormatSymbols(dfs);
        return df.format(value);
    }

    // 检查字节切片是否只包含ASCII字符
    private static boolean isASCII(byte[] data) {
        for (byte b : data) {
            if ((b & 0xFF) > 127) {
                return false;
            }
        }
        return true;
    }

    // 检查字节切片是否只包含可打印ASCII字符
    private static boolean isPrintableString(byte[] data) {
        for (byte b : data) {
            int unsigned = b & 0xFF;
            if (unsigned < 32 || unsigned > 126) {
                return false;
            }
        }
        return true;
    }

    // 将ISO-8859-1字节转换为字符串
    private static String latin1ToString(byte[] data) {
        return new String(data, StandardCharsets.ISO_8859_1);
    }

    // 将BMPString (UCS-2) 字节转换为字符串
    private static String bmpToString(byte[] data) {
        if (data.length % 2 != 0) {
            return "";
        }

        // 转换为UTF-16序列
        char[] chars = new char[data.length / 2];
        for (int i = 0; i < data.length; i += 2) {
            chars[i / 2] = (char) (((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF));
        }

        return new String(chars);
    }

    // 解析UTCTime格式
    private static String parseUTCTime(byte[] data) {
        if (data.length < 10) {
            return null;
        }

        String timeStr = new String(data, StandardCharsets.US_ASCII);

        // UTCTime格式: YYMMDDhhmm[ss]Z 或 YYMMDDhhmm[ss]+hhmm 或 YYMMDDhhmm[ss]-hhmm
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyMMddHHmm'Z'"),
                DateTimeFormatter.ofPattern("yyMMddHHmmss'Z'"),
                DateTimeFormatter.ofPattern("yyMMddHHmmxx"),
                DateTimeFormatter.ofPattern("yyMMddHHmmssxx")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                // 对于带时区的格式
                if (formatter.toString().contains("xx")) {
                    LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
                    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX"));
                } else {
                    // 对于UTC时间
                    LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
                    return dateTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'"));
                }
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }

        return null;
    }

    // 解析GeneralizedTime格式
    private static String parseGeneralizedTime(byte[] data) {
        if (data.length < 12) {
            return null;
        }

        String timeStr = new String(data, StandardCharsets.US_ASCII);

        // GeneralizedTime格式: YYYYMMDDhhmm[ss]Z 或 YYYYMMDDhhmm[ss].fffZ 或 带时区偏移
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyyMMddHHmm'Z'"),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss'Z'"),
                DateTimeFormatter.ofPattern("yyyyMMddHHmm.SSS'Z'"),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS'Z'"),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmxx"),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmssxx")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                // 对于带时区的格式
                if (formatter.toString().contains("xx")) {
                    LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
                    return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS XXX"));
                } else {
                    // 对于UTC时间
                    LocalDateTime dateTime = LocalDateTime.parse(timeStr, formatter);
                    return dateTime.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS 'UTC'"));
                }
            } catch (Exception e) {
                // 继续尝试下一个格式
            }
        }

        return null;
    }

    // 解析OBJECT IDENTIFIER
    private static String parseObjectIdentifier(byte[] data) {
        if (data.length == 0) {
            return null;
        }

        // 解码OID字节
        List<Integer> oidList = new ArrayList<>();
        for (int i = 0; i < data.length; ) {
            int value = 0;
            while (true) {
                if (i >= data.length) {
                    return null; // 防止越界访问
                }
                byte b = data[i];
                value = (value << 7) | (b & 0x7F);
                i++;
                if ((b & 0x80) == 0) {
                    break;
                }
            }
            oidList.add(value);
        }

        // 处理第一个字节的特殊编码 (40 * X + Y)
        if (!oidList.isEmpty()) {
            int first = oidList.get(0);
            int x = first / 40;
            int y = first % 40;

            // 替换第一个节点为 x 和 y
            oidList.set(0, y);
            oidList.add(0, x);
        }

        // 转换为点分十进制字符串
        StringBuilder oidBuilder = new StringBuilder();
        for (int i = 0; i < oidList.size(); i++) {
            if (i > 0) {
                oidBuilder.append(".");
            }
            oidBuilder.append(oidList.get(i));
        }

        return oidBuilder.toString();
    }

    // 根据OID字符串获取对应的含义和说明
    private static OIDInfo getOIDInfo(String oidStr) {
        return OID_MAPPINGS.get(oidStr);
    }

    // 解析BOOLEAN值
    private static String parseBoolean(byte[] data) {
        if (data.length != 1) {
            return null;
        }

        if (data[0] == 0x00) {
            return "FALSE";
        } else {
            return "TRUE";
        }
    }

    // 解析TAG 5A值
    private static String parseTag5A(byte[] data) {
        if (data.length % 2 != 0) {
            return null;
        }
        // 将字节切片转换为字符串
        String result = new String(data, StandardCharsets.US_ASCII);
        return result.toUpperCase();
    }

    // ========== 工具方法 ==========

    // 移除不可见字符
    private static String removeInvisibleChars(String str) {
        return str.replaceAll("\\s+", "");
    }


    // Base64字符串转十六进制
    private static String base64StringToHex(String base64Str) {
        byte[] decoded = Base64.getDecoder().decode(base64Str);
        return bytesToHex(decoded);
    }

    // 字节数组转十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }

    // 字节数组部分转十六进制字符串
    private static String bytesToHex(byte[] bytes, int start, int end) {
        StringBuilder hex = new StringBuilder();
        for (int i = start; i < end; i++) {
            hex.append(String.format("%02X", bytes[i]));
        }
        return hex.toString();
    }

    // 十六进制字符串转字节数组
    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("十六进制字符串长度必须为偶数");
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    // 检查是否为有效的UTF-8
    private static boolean isValidUTF8(byte[] data) {
        try {
            String str = new String(data, StandardCharsets.UTF_8);
            // 简单验证：重新编码后应该得到相同字节
            byte[] reencoded = str.getBytes(StandardCharsets.UTF_8);
            return Arrays.equals(data, reencoded);
        } catch (Exception e) {
            return false;
        }
    }
}