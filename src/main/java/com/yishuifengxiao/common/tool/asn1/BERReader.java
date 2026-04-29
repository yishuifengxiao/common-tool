package com.yishuifengxiao.common.tool.asn1;

import com.yishuifengxiao.common.tool.lang.Hex;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;

/**
 * <p>
 * BER读取器
 * </p>
 * <p>提供从输入流中读取ASN.1 BER编码数据的功能，支持标签读取、长度解析、以及各种数据类型的解码。</p>
 *
 * @author yishuifengxiao
 * @version 1.0.0
 * @since 1.0.0
 */
public class BERReader {

    /**
     * 位掩码数组，用于位操作
     */
    private static final byte[] BIT_MASK = new byte[]{(byte) 0x80, (byte) 0x40, (byte) 0x20, (byte) 0x10,
            (byte) 0x08, (byte) 0x04, (byte) 0x02, (byte) 0x01};

    /**
     * 输入流
     */
    private final java.io.InputStream in;

    /**
     * 长度字段的字节长度
     */
    private int lengthLength;

    /**
     * 长度值
     */
    private int lengthValue;

    /**
     * 长度缓冲区
     */
    private final byte[] lengthBuffer = new byte[10];

    /**
     * 标签的字节数
     */
    private int tagNumBytes;

    /**
     * 标签缓冲区
     */
    private final byte[] tagBuffer = new byte[10];

    /**
     * 标签是否匹配
     */
    private boolean tagMatched = true;

    /**
     * 追踪缓冲区
     */
    private byte[] traceBuffer = null;

    /**
     * 追踪索引
     */
    private int traceIndex = 0;

    /**
     * 是否启用追踪缓冲区
     */
    private boolean isTraceBufferEnabled = false;

    /**
     * 追踪缓冲区增量
     */
    private final int traceBufferIncrement = 500;

    /**
     * 构造函数
     *
     * @param in 输入流
     */
    public BERReader(java.io.InputStream in) {
        this.in = in;
    }

    /**
     * 获取自上次reset()调用以来接收的字节数
     *
     * @return 字节数
     */
    public int getTraceLength() {
        return traceIndex;
    }

    /**
     * 读取一个字符
     *
     * @return 字符值
     * @throws IOException 读取异常
     */
    private int readChar() throws IOException {
        int value = in.read();

        if (value == -1) {
            throw new EOFException();
        }

        if (isTraceBufferEnabled) {
            checkTraceBufferSize();
            traceBuffer[traceIndex] = (byte) value;
        }

        traceIndex++;

        return value;
    }

    /**
     * 必须匹配指定的标签
     *
     * @param tag 标签字节数组
     * @throws Exception 标签不匹配异常
     */
    public void mustMatchTag(byte[] tag) throws Exception {
        if (tagNumBytes != tag.length) {
            throw new Exception("Expected size: " + tag.length + ", actual: " + tagNumBytes);
        }
        for (int i = 0; i < tag.length; i++) {
            if (tag[i] != tagBuffer[i]) {
                throw new Exception("Unexpected byte (expected: " + tag[i] + ", actual: " + tagBuffer[i]);
            }
        }
        this.tagMatched = true;
    }

    /**
     * 预查标签是否匹配多个候选标签之一
     *
     * @param tags 标签数组数组
     * @return 是否匹配
     */
    public boolean lookAheadTag(byte[][] tags) {
        boolean foundMatch = false;

        for (int k = 0; k < tags.length && !foundMatch; k++) {
            byte[] tag = tags[k];
            foundMatch = false;
            if (tagNumBytes == tag.length) {
                foundMatch = true;
                for (int i = 0; i < tag.length; i++) {
                    if (tag[i] != tagBuffer[i]) {
                        foundMatch = false;
                        break;
                    }
                }
            }
        }

        return foundMatch;
    }

    /**
     * 匹配标签
     *
     * @param tag 标签字节数组
     * @return 是否匹配
     */
    public boolean matchTag(byte[] tag) {
        tagMatched = false;
        if (tagNumBytes == tag.length) {
            tagMatched = true;
            for (int i = 0; i < tag.length; i++) {
                if (tag[i] != tagBuffer[i]) {
                    tagMatched = false;
                    break;
                }
            }
        }
        return tagMatched;
    }

    /**
     * 读取标签
     *
     * @throws IOException 读取异常
     */
    public void readTag() throws IOException {
        boolean isLastByte = false;
        tagNumBytes = 1;

        tagBuffer[0] = (byte) readChar();

        if ((tagBuffer[0] & 0x1F) != 0x1F) {
            isLastByte = true;
        }

        for (int i = 1; !isLastByte; i++) {
            tagBuffer[i] = (byte) readChar();
            tagNumBytes++;

            if ((tagBuffer[i] & 0x80) == 0) {
                isLastByte = true;
            }
        }

        tagMatched = false;
    }

    /**
     * 获取标签长度
     *
     * @return 标签字节数
     */
    public int getTagLength() {
        return tagNumBytes;
    }

    /**
     * 获取标签字节数组
     *
     * @return 标签字节数组
     */
    public byte[] getTag() {
        byte[] ret = new byte[tagNumBytes];
        System.arraycopy(tagBuffer, 0, ret, 0, tagNumBytes);
        return ret;
    }

    /**
     * 必须读取零长度
     *
     * @throws Exception 长度不为零异常
     */
    public void mustReadZeroLength() throws Exception {
        readLength();
        if (getLengthLength() != 1 || getLengthValue() != 0) {
            throw new Exception("Expecting 0 length here");
        }
    }

    /**
     * 读取长度字段
     *
     * @throws IOException 读取异常
     */
    public void readLength() throws IOException {
        tagMatched = true;

        lengthLength = 0;
        lengthValue = 0;

        int lengthBufferIndex = 0;
        int aByte = readChar();
        lengthBuffer[lengthBufferIndex++] = (byte) aByte;

        if (aByte == 0x80) {
            lengthLength = 1;
            lengthValue = -1;
        } else {
            if (aByte > 0x7f) {
                int nBytes = (aByte & 0x7f);

                if (nBytes > 4) {
                    throw new RuntimeException("Length over 4 bytes not supported");
                }

                lengthLength = nBytes + 1;
                lengthValue = 0;

                for (int i = nBytes; i > 0; i--) {
                    aByte = readChar();
                    lengthBuffer[lengthBufferIndex++] = (byte) aByte;
                    lengthValue += (aByte << ((i - 1) * 8));
                }
            } else {
                lengthLength = 1;
                lengthValue = aByte;
            }
        }
    }

    /**
     * 读取Byte类型值
     *
     * @param nBytes 字节数
     * @return Byte值
     * @throws IOException 读取异常
     */
    public Byte readByte(int nBytes) throws IOException {
        if (nBytes > 1) {
            throw new RuntimeException("Size of Byte cannot be " + nBytes);
        }
        return readBigInteger(nBytes).byteValue();
    }

    /**
     * 读取Short类型值
     *
     * @param nBytes 字节数
     * @return Short值
     * @throws IOException 读取异常
     */
    public Short readShort(int nBytes) throws IOException {
        if (nBytes > 2) {
            throw new RuntimeException("Size of Short cannot be " + nBytes);
        }
        return readBigInteger(nBytes).shortValue();
    }

    /**
     * 读取Integer类型值
     *
     * @param nBytes 字节数
     * @return Integer值
     * @throws IOException 读取异常
     */
    public Integer readInteger(int nBytes) throws IOException {
        if (nBytes > 5) {
            throw new RuntimeException("Size of Integer cannot be " + nBytes);
        }
        return readBigInteger(nBytes).intValue();
    }

    /**
     * 读取Long类型值
     *
     * @param nBytes 字节数
     * @return Long值
     * @throws IOException 读取异常
     */
    public Long readLong(int nBytes) throws IOException {
        if (nBytes > 8) {
            throw new RuntimeException("Size of Long cannot be " + nBytes);
        }
        return readBigInteger(nBytes).longValue();
    }

    /**
     * 读取BigInteger类型值
     *
     * @param nBytes 字节数
     * @return BigInteger值
     * @throws IOException 读取异常
     */
    public java.math.BigInteger readBigInteger(int nBytes) throws IOException {
        return new java.math.BigInteger(readOctetString(nBytes));
    }

    /**
     * 读取受限字符字符串
     *
     * @param nBytes 字节数
     * @return 字符串
     * @throws IOException 读取异常
     */
    public String readRestrictedCharacterString(int nBytes) throws IOException {
        byte[] buffer = new byte[nBytes];

        for (int i = 0; i < nBytes; i++) {
            buffer[i] = (byte) readChar();
        }

        return new String(buffer);
    }

    /**
     * 读取十六进制字符串
     *
     * @param nBytes 字节数
     * @return 十六进制字符串
     * @throws IOException 读取异常
     */
    public String readHexString(int nBytes) throws IOException {
        byte[] buffer = new byte[nBytes];
        return Hex.bytesToHex(buffer);
    }

    /**
     * 读取位串
     *
     * @param nBytes 字节数
     * @return BitSet对象
     * @throws IOException 读取异常
     */
    public BitSet readBitString(int nBytes) throws IOException {
        if (nBytes < 1) {
            throw new RuntimeException("Length of a bitstring cannot be less than one");
        }

        if (nBytes == 1) {
            byte b = (byte) readChar();
            byte[] byteArray = {b};
            return BitSet.valueOf(byteArray);
        }

        byte[] copy = new byte[nBytes];

        for (int i = 0; i < nBytes; i++) {
            copy[i] = (byte) readChar();
        }

        int numSignificantBitInLastByte = 8 - copy[0];

        BitSet result = new BitSet();

        int bitIndex = 0;
        int byteIndex = 0;

        for (byteIndex = 1; byteIndex < (nBytes - 1); byteIndex++) {
            for (int k = 0; k < 8; k++, bitIndex++) {
                if ((copy[byteIndex] & BIT_MASK[k]) != 0x00) {
                    result.set(bitIndex);
                }
            }
        }

        for (int k = 0; k < numSignificantBitInLastByte; k++, bitIndex++) {
            if ((copy[byteIndex] & BIT_MASK[k]) != 0x00) {
                result.set(bitIndex);
            }
        }

        return result;
    }

    /**
     * 读取对象标识符
     *
     * @param nBytes 字节数
     * @return OID数组
     * @throws IOException 读取异常
     */
    public long[] readObjectIdentifier(int nBytes) throws IOException {
        ArrayList<Long> objectIdentifier = new ArrayList<>();
        byte[] buffer = readOctetString(nBytes);
        long arc = -1;
        long mult = 1;
        for (int i = nBytes - 1; i >= 0; i--) {
            if ((buffer[i] & 0x80) == 0x00) {
                if (arc != -1) {
                    objectIdentifier.add(0, arc);
                }
                arc = buffer[i];
                mult = 1;
            } else {
                mult = Math.multiplyExact(mult, 128);
                arc = Math.addExact(arc, Math.multiplyExact(mult, (buffer[i] & 0x7F)));
            }
        }
        if (arc < 40) {
            objectIdentifier.add(0, arc);
            objectIdentifier.add(0, 0L);
        } else if (arc < 80) {
            objectIdentifier.add(0, arc - 40);
            objectIdentifier.add(0, 1L);
        } else {
            objectIdentifier.add(0, arc - 80);
            objectIdentifier.add(0, 2L);
        }

        long[] ret = new long[objectIdentifier.size()];
        int i = 0;
        for (Long arcAsLong : objectIdentifier) {
            ret[i++] = arcAsLong;
        }
        return ret;
    }

    /**
     * 读取相对对象标识符
     *
     * @param nBytes 字节数
     * @return 相对OID数组
     * @throws IOException 读取异常
     */
    public long[] readRelativeOID(int nBytes) throws IOException {
        ArrayList<Long> objectIdentifier = new ArrayList<>();
        byte[] buffer = readOctetString(nBytes);
        long arc = -1;
        long mult = 1;
        for (int i = nBytes - 1; i >= 0; i--) {
            if ((buffer[i] & 0x80) == 0x00) {
                if (arc != -1) {
                    objectIdentifier.add(0, arc);
                }
                arc = buffer[i];
                mult = 1;
            } else {
                mult = Math.multiplyExact(mult, 128);
                arc = Math.addExact(arc, Math.multiplyExact(mult, (buffer[i] & 0x7F)));
            }
        }
        objectIdentifier.add(0, arc);

        long[] ret = new long[objectIdentifier.size()];
        int i = 0;
        for (Long arcAsLong : objectIdentifier) {
            ret[i++] = arcAsLong;
        }
        return ret;
    }

    /**
     * 读取任意数据（包含标签和长度）
     *
     * @param nBytes 数据字节数
     * @return 字节数组（标签+长度+数据）
     * @throws IOException 读取异常
     */
    public byte[] readAny(int nBytes) throws IOException {
        byte[] ret = new byte[tagNumBytes + lengthLength + nBytes];

        System.arraycopy(tagBuffer, 0, ret, 0, tagNumBytes);
        System.arraycopy(lengthBuffer, 0, ret, tagNumBytes, lengthLength);
        System.arraycopy(readOctetString(nBytes), 0, ret, tagNumBytes + lengthLength, nBytes);
        return ret;
    }

    /**
     * 读取八位字节串
     *
     * @param nBytes 字节数
     * @return 字节数组
     * @throws IOException 读取异常
     */
    public byte[] readOctetString(int nBytes) throws IOException {
        byte[] result = new byte[nBytes];

        for (int i = 0; i < nBytes; i++) {
            result[i] = (byte) readChar();
        }

        return result;
    }

    /**
     * 读取布尔值
     *
     * @param bytes 字节数
     * @return Boolean值
     * @throws IOException 读取异常
     */
    public Boolean readBoolean(int bytes) throws IOException {
        int value = readChar();
        return value != 0;
    }

    /**
     * 启用/禁用追踪缓冲区
     *
     * @param state 是否启用
     */
    public void setTraceBufferEnabled(boolean state) {
        this.isTraceBufferEnabled = state;
    }

    /**
     * 获取追踪缓冲区
     *
     * @return 追踪缓冲区字节数组
     */
    public byte[] getTraceBuffer() {
        byte[] copy = null;

        if (traceIndex > 0) {
            copy = new byte[traceIndex];
            System.arraycopy(traceBuffer, 0, copy, 0, traceIndex);
        }

        return copy;
    }

    /**
     * 重置追踪索引
     */
    public void reset() {
        traceIndex = 0;
    }

    /**
     * 检查追踪缓冲区大小
     */
    private void checkTraceBufferSize() {
        if (traceBuffer == null) {
            traceBuffer = new byte[traceBufferIncrement];
        } else {
            if (traceIndex >= traceBuffer.length) {
                byte[] old = traceBuffer;
                traceBuffer = new byte[old.length + traceBufferIncrement];
                System.arraycopy(old, 0, traceBuffer, 0, old.length);
            }
        }
    }

    /**
     * 获取长度字段的字节长度
     *
     * @return 长度字段字节数
     */
    public int getLengthLength() {
        return lengthLength;
    }

    /**
     * 获取长度值
     *
     * @return 长度值
     */
    public int getLengthValue() {
        return lengthValue;
    }

    /**
     * 获取长度字节数组
     *
     * @return 长度字节数组
     */
    public byte[] getLength() {
        byte[] ret = new byte[lengthLength];
        System.arraycopy(lengthBuffer, 0, ret, 0, lengthLength);
        return ret;
    }

    /**
     * 标签是否已匹配
     *
     * @return 是否匹配
     */
    public boolean isTagMatched() {
        return tagMatched;
    }

    /**
     * 设置标签匹配状态
     *
     * @param tagMatched 是否匹配
     */
    public void setTagMatched(boolean tagMatched) {
        this.tagMatched = tagMatched;
    }
}