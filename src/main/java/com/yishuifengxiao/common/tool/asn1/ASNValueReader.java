package com.yishuifengxiao.common.tool.asn1;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * <p>
 * ASN.1 值读取器
 * </p>
 * <p>提供从输入流中读取各种ASN.1数据类型的功能，支持整数、布尔值、字符串、位串、对象标识符等类型的解析。</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ASNValueReader {

    /**
     * 跳过的字符集合（空格、制表符、回车、换行）
     */
    private static final String SKIP_CHARS = " \t\r\n";

    /**
     * 分隔符标记集合（逗号、大括号、冒号）
     */
    private static final String TOKENS = ",{}:";

    /**
     * 输入流读取器
     */
    private Reader reader;

    /**
     * 暂存的标记（用于回退）
     */
    private String putAsideToken = "";

    /**
     * 当前行号
     */
    public int line = 1;

    /**
     * 构造函数，使用输入流创建读取器
     *
     * @param in 输入流
     */
    public ASNValueReader(InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
    }

    /**
     * 预读下一个标记（不消费）
     *
     * @return 下一个标记字符串，如果到达流末尾则返回空字符串
     * @throws Exception 读取异常
     */
    public String lookAheadToken() throws Exception {
        String ret = "";

        if (!putAsideToken.isEmpty()) {
            ret = putAsideToken;
        } else {
            reader.mark(1000);

            int c;
            do {
                c = reader.read();
                if (c == -1)
                    return "";
            } while (-1 != SKIP_CHARS.indexOf(c));

            ret = ret + (char) c;

            reader.reset();
        }

        return ret;
    }

    /**
     * 读取并消费下一个标记
     *
     * @return 标记字符串，如果到达流末尾则返回空字符串
     * @throws Exception 读取异常或遇到非法字符
     */
    public String readToken() throws Exception {
        String token = "";

        if (!putAsideToken.isEmpty()) {
            token = putAsideToken;
            putAsideToken = "";
        } else {
            int c;
            do {
                c = reader.read();
                if (c == '\n') {
                    line++;
                }
                if (c == -1)
                    return "";
            } while (-1 != SKIP_CHARS.indexOf(c));

            if (-1 == TOKENS.indexOf(c)) {
                throw new Exception("Expecting a token, read '" + (char) c + "'");
            }
            token = token + (char) c;
        }

        return token;
    }

    /**
     * 预读下一个标识符（不消费）
     *
     * @return 标识符字符串，如果到达流末尾则返回空字符串
     * @throws Exception 读取异常
     */
    public String lookAheadIdentifier() throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        int c = -1;

        reader.mark(1000);

        do {
            c = reader.read();
            if (c == -1)
                return "";
        } while (-1 != SKIP_CHARS.indexOf(c));

        while (-1 == SKIP_CHARS.indexOf(c) && -1 == TOKENS.indexOf(c) && c != -1) {
            stringBuffer.append((char) c);
            c = reader.read();
        }

        reader.reset();

        return stringBuffer.toString();
    }

    /**
     * 读取并消费下一个标识符
     *
     * @return 标识符字符串，如果到达流末尾则返回空字符串
     * @throws Exception 读取异常
     */
    public String readIdentifier() throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        int c = -1;

        do {
            c = reader.read();
            if (c == '\n') {
                line++;
            }
            if (c == -1)
                return "";
        } while (-1 != SKIP_CHARS.indexOf(c));

        while (-1 == SKIP_CHARS.indexOf(c) && -1 == TOKENS.indexOf(c) && c != -1) {
            stringBuffer.append((char) c);
            c = reader.read();
            if (c == '\n') {
                line++;
            }
        }

        if (-1 != TOKENS.indexOf(c)) {
            putAsideToken = "" + (char) c;
        }

        return stringBuffer.toString();
    }

    /**
     * 读取Byte类型值
     *
     * @return Byte对象
     * @throws Exception 读取异常或格式错误
     */
    public Byte readByte() throws Exception {
        String integerAsString = readIdentifier();
        return Byte.valueOf(integerAsString);
    }

    /**
     * 读取Short类型值
     *
     * @return Short对象
     * @throws Exception 读取异常或格式错误
     */
    public Short readShort() throws Exception {
        String integerAsString = readIdentifier();
        return Short.valueOf(integerAsString);
    }

    /**
     * 读取Integer类型值
     *
     * @return Integer对象
     * @throws Exception 读取异常或格式错误
     */
    public Integer readInteger() throws Exception {
        String integerAsString = readIdentifier();
        return Integer.valueOf(integerAsString);
    }

    /**
     * 读取Long类型值
     *
     * @return Long对象
     * @throws Exception 读取异常或格式错误
     */
    public Long readLong() throws Exception {
        String integerAsString = readIdentifier();
        return Long.valueOf(integerAsString);
    }

    /**
     * 读取BigInteger类型值
     *
     * @return BigInteger对象
     * @throws Exception 读取异常或格式错误
     */
    public java.math.BigInteger readBigInteger() throws Exception {
        String integerAsString = readIdentifier();
        return new java.math.BigInteger(integerAsString);
    }

    /**
     * 读取位串（支持'B'和'H'后缀格式）
     *
     * @return BitSet对象，如果读取失败返回null
     * @throws Exception 格式错误异常
     */
    public BitSet readBitString() throws Exception {
        BitSet ret = null;

        StringBuffer stringBuffer = new StringBuffer();
        int c = -1;

        do {
            c = reader.read();
            if (c == -1)
                return null;
        } while (-1 != SKIP_CHARS.indexOf(c));

        if ((char) c != '\'') {
            throw new Exception("BITSTRING value must start with \'");
        }

        c = reader.read();
        while (c != '\'' && c != -1) {
            stringBuffer.append((char) c);
            c = reader.read();
        }

        if ((char) c != '\'') {
            throw new Exception("BITSTRING value must end with \'H or \'B");
        }

        c = reader.read();
        switch ((char) c) {
            case 'B':
                ret = bitStringToBitSet(stringBuffer.toString());
                break;
            case 'H':
                ret = octetStringToBitSet(stringBuffer.toString());
                break;
            default:
                throw new Exception("BITSTRING value must end with \'H or \'B");
        }

        return ret;
    }

    /**
     * 读取布尔值
     *
     * @return Boolean对象
     * @throws Exception 格式错误异常（非TRUE/FALSE）
     */
    public Boolean readBoolean() throws Exception {
        Boolean ret = null;
        String booleanAsString = readIdentifier();
        switch (booleanAsString) {
            case "TRUE":
                ret = Boolean.TRUE;
                break;
            case "FALSE":
                ret = Boolean.FALSE;
                break;
            default:
                throw new Exception("Boolean must be either TRUE or FALSE");
        }
        return ret;
    }

    /**
     * 读取NULL值
     *
     * @return Object对象（表示NULL）
     * @throws Exception 格式错误异常（非NULL）
     */
    public Object readNull() throws Exception {
        Object ret = null;
        String NullAsTring = readIdentifier();
        switch (NullAsTring) {
            case "NULL":
                ret = new Object();
                break;
            default:
                throw new Exception("Expected NULL");
        }
        return ret;
    }

    /**
     * 读取受限字符字符串（双引号包裹）
     *
     * @return 字符串值，如果到达流末尾返回空字符串
     * @throws Exception 格式错误异常
     */
    public String readRestrictedCharacterString() throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        int c = -1;

        do {
            c = reader.read();
            if (c == -1)
                return "";
        } while (-1 != SKIP_CHARS.indexOf(c));

        if ((char) c != '"') {
            throw new Exception("String value must start with \"");
        }

        c = reader.read();
        while (c != '"' && c != -1) {
            stringBuffer.append((char) c);
            c = reader.read();
        }

        if ((char) c != '"') {
            throw new Exception("String value must end with \"");
        }

        return stringBuffer.toString();
    }

    /**
     * 读取八位字节串（十六进制格式，'...'H）
     *
     * @return 字节数组，如果读取失败返回null
     * @throws Exception 格式错误异常
     */
    public byte[] readOctetString() throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        int c = -1;

        do {
            c = reader.read();
            if (c == -1)
                return null;
        } while (-1 != SKIP_CHARS.indexOf(c));

        if ((char) c != '\'') {
            throw new Exception("OCTET STRING value must start with \'");
        }

        c = reader.read();
        while (c != '\'' && c != -1) {
            if (-1 != SKIP_CHARS.indexOf(c)) {
            } else {
                stringBuffer.append((char) c);
            }
            c = reader.read();
        }

        if ((char) c != '\'') {
            throw new Exception("OCTET STRING value must end with \'H");
        }

        c = reader.read();
        if ((char) c != 'H') {
            throw new Exception("OCTET STRING value must end with \'H");
        }

        return hexStringToByteArray(stringBuffer.toString());
    }

    /**
     * 读取对象标识符（OID）
     *
     * @return long数组，表示OID的各个弧
     * @throws Exception 格式错误异常
     */
    public long[] readObjectIdentifier() throws Exception {
        String start = lookAheadToken();
        if (start.equals("{")) {
            readToken();
        } else {
            throw new Exception("ObjectIdentifier must start with '{'. Found '" + start + "' instead.");
        }

        List<Long> list = new ArrayList<>();
        while (!"}".equals(lookAheadToken())) {
            list.add(readLong());
        }
        readToken();
        long[] ret = new long[list.size()];
        int i = 0;
        for (Long e : list)
            ret[i++] = e;
        return ret;
    }

    /**
     * 读取相对对象标识符
     *
     * @return long数组，表示相对OID的各个弧
     * @throws Exception 读取异常
     */
    public long[] readRelativeOID() throws Exception {
        return readObjectIdentifier();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param s 十六进制字符串
     * @return 字节数组
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 将二进制字符串转换为BitSet
     *
     * @param binary 二进制字符串
     * @return BitSet对象
     */
    private static BitSet bitStringToBitSet(String binary) {
        BitSet bitset = new BitSet(binary.length());
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                bitset.set(i);
            }
        }
        return bitset;
    }

    /**
     * 将十六进制字符串转换为BitSet
     *
     * @param hexString 十六进制字符串
     * @return BitSet对象
     */
    private static BitSet octetStringToBitSet(String hexString) {
        BitSet bitset = new BitSet(4 * hexString.length());
        for (int i = 0; i < hexString.length(); i++) {
            byte aByte = Byte.parseByte(hexString.substring(i, i + 1), 16);
            if ((aByte & 0x08) != 0x00)
                bitset.set(4 * i);
            if ((aByte & 0x04) != 0x00)
                bitset.set(4 * i + 1);
            if ((aByte & 0x02) != 0x00)
                bitset.set(4 * i + 2);
            if ((aByte & 0x01) != 0x00)
                bitset.set(4 * i + 3);
        }
        return bitset;
    }

}