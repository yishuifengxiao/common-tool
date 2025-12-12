package com.yishuifengxiao.common.tool.asn1;

import com.yishuifengxiao.common.tool.lang.Hex;

import java.nio.charset.StandardCharsets;

/**
 * ASN.1工具类，提供ASN.1数据的编码和解码功能
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class Asn1Util {

    /**
     * 将十六进制字符串转换为BERReader对象
     *
     * @param hexVal 十六进制字符串
     * @return BERReader对象
     */
    public static BERReader hexToBERReader(String hexVal) {
        byte[] bytes = Hex.hexToBytes(hexVal);
        return new BERReader(new java.io.ByteArrayInputStream(bytes));
    }

    /**
     * 将UTF-8字符串转换为BERReader对象
     * 该方法将输入的UTF-8字符串转换为字节数组，然后创建BERReader对象用于ASN.1数据解析
     *
     * @param utf8Val UTF-8编码的字符串，将作为ASN.1数据源进行解析
     * @return BERReader对象，可用于读取和解析ASN.1 BER编码的数据
     * @example // 示例：将UTF-8字符串转换为BERReader进行解析
     * String utf8String = "示例UTF-8字符串";
     * BERReader reader = Asn1Util.utf8ToBERReader(utf8String);
     * // 使用reader读取ASN.1结构...
     * @see BERReader
     * @see #hexToBERReader(String)
     */
    public static BERReader utf8ToBERReader(String utf8Val) {
        byte[] bytes = utf8Val.getBytes(StandardCharsets.UTF_8);
        return new BERReader(new java.io.ByteArrayInputStream(bytes));
    }

}
