package com.yishuifengxiao.common.tool.asn1;

import com.yishuifengxiao.common.tool.lang.Hex;

import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;

/**
 * <p>
 * BER写入器
 * </p>
 * <p>提供将ASN.1数据编码为BER格式并写入输出流的功能，支持整数、布尔值、字符串、位串、对象标识符等类型。</p>
 *
 * @author yishuifengxiao
 * @version 1.0.0
 * @since 1.0.0
 */
public class BERWriter {

    /**
     * 位掩码数组，用于位操作
     */
    private static final byte[] BIT_MASK = new byte[]{(byte) 0x80, (byte) 0x40, (byte) 0x20, (byte) 0x10,
            (byte) 0x08, (byte) 0x04, (byte) 0x02, (byte) 0x01};

    /**
     * 十六进制字符数组
     */
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * 输出流
     */
    private final OutputStream out;

    /**
     * 缓冲区
     */
    private byte[] buffer = null;

    /**
     * 数据大小
     */
    private int dataSize = 0;

    /**
     * 刷新标志
     */
    private boolean flushFlag = false;

    /**
     * 缓冲区增量
     */
    private final int increment;

    /**
     * 构造函数
     *
     * @param out       输出流
     * @param initialSize 初始大小
     * @param increment   增量大小
     */
    public BERWriter(OutputStream out, int initialSize, int increment) {
        this.out = out;
        this.increment = increment;
        buffer = new byte[initialSize];
    }

    /**
     * 构造函数（使用默认初始大小和增量）
     *
     * @param out 输出流
     */
    public BERWriter(OutputStream out) {
        this(out, 100, 100);
    }

    /**
     * 构造函数（无输出流）
     */
    public BERWriter() {
        this(null);
    }

    /**
     * 刷新输出
     *
     * @throws IOException IO异常
     */
    public void flush() throws IOException {
        if (out != null) {
            out.write(buffer, buffer.length - dataSize, dataSize);
        }

        flushFlag = true;
    }

    /**
     * 增加数据大小
     *
     * @param nBytes 字节数
     */
    private void increaseDataSize(int nBytes) {
        if (flushFlag) {
            flushFlag = false;
            dataSize = 0;
        }

        if ((dataSize + nBytes) > buffer.length) {
            byte[] tempBuffer = new byte[buffer.length + increment + nBytes];
            System.arraycopy(buffer, buffer.length - dataSize, tempBuffer, tempBuffer.length - dataSize, dataSize);
            buffer = tempBuffer;
        }

        dataSize += nBytes;
    }

    /**
     * 写入Byte类型整数
     *
     * @param value Byte值
     * @return 写入的字节数
     */
    public int writeInteger(Byte value) {
        return writeInteger(value.longValue());
    }

    /**
     * 写入Short类型整数
     *
     * @param value Short值
     * @return 写入的字节数
     */
    public int writeInteger(Short value) {
        return writeInteger(value.longValue());
    }

    /**
     * 写入Integer类型整数
     *
     * @param value Integer值
     * @return 写入的字节数
     */
    public int writeInteger(Integer value) {
        return writeInteger(value.longValue());
    }

    /**
     * 写入Long类型整数
     *
     * @param value Long值
     * @return 写入的字节数
     */
    public int writeInteger(Long value) {
        return writeInteger(java.math.BigInteger.valueOf(value));
    }

    /**
     * 写入BigInteger类型整数
     *
     * @param value BigInteger值
     * @return 写入的字节数
     */
    public int writeInteger(java.math.BigInteger value) {
        return writeOctetString(value.toByteArray());
    }

    /**
     * 写入位串（版本2）
     *
     * @param value BitSet值
     * @return 写入的字节数
     */
    public int writeBitString2(BitSet value) {
        int significantBitNumber = value.length();

        int nBytes = 0;
        int nPadding = 0;

        if (significantBitNumber == 0) {
            nBytes = 0;
        } else {
            nBytes = significantBitNumber / 8;
            nPadding = significantBitNumber % 8;

            if (nPadding != 0) {
                nBytes += 1;
            }
        }

        increaseDataSize(nBytes);

        int currentIndex = buffer.length - dataSize;
        int maskId = 0;

        for (int i = 0; i < significantBitNumber; i++) {
            if (value.get(i)) {
                buffer[currentIndex] |= BIT_MASK[maskId];
            }

            if (maskId == 7) {
                currentIndex++;
                maskId = 0;
            } else {
                maskId++;
            }
        }

        increaseDataSize(1);

        buffer[buffer.length - dataSize] = 0;

        return nBytes + 1;
    }

    /**
     * 写入位串
     *
     * @param value BitSet值
     * @return 写入的字节数
     */
    public int writeBitString(BitSet value) {
        int significantBitNumber = value.length();

        int nBytes = 0;
        int nPadding = 0;

        if (significantBitNumber == 0) {
            nBytes = 0;
        } else {
            nBytes = significantBitNumber / 8;
            nPadding = significantBitNumber % 8;

            if (nPadding != 0) {
                nBytes += 1;
            }
        }

        increaseDataSize(nBytes);

        int currentIndex = buffer.length - dataSize;
        int maskId = 0;

        for (int i = 0; i < significantBitNumber; i++) {
            if (value.get(i)) {
                buffer[currentIndex] |= BIT_MASK[maskId];
            }

            if (maskId == 7) {
                currentIndex++;
                maskId = 0;
            } else {
                maskId++;
            }
        }

        increaseDataSize(1);

        if (nPadding == 0) {
            buffer[buffer.length - dataSize] = 0;
        } else {
            buffer[buffer.length - dataSize] = (byte) (8 - nPadding);
        }

        return nBytes + 1;
    }

    /**
     * 写入布尔值
     *
     * @param value Boolean值
     * @return 写入的字节数
     */
    public int writeBoolean(Boolean value) {
        boolean boolValue = value.booleanValue();
        increaseDataSize(1);

        if (boolValue) {
            buffer[buffer.length - dataSize] = (byte) 0xFF;
        } else {
            buffer[buffer.length - dataSize] = 0x00;
        }

        return 1;
    }

    /**
     * 写入受限字符字符串
     *
     * @param value 字符串值
     * @return 写入的字节数
     */
    public int writeRestrictedCharacterString(String value) {
        byte[] bytes = value.getBytes();
        return writeOctetString(bytes);
    }

    /**
     * 写入十六进制字符串
     *
     * @param value 十六进制字符串
     * @return 写入的字节数
     */
    public int writeHexString(String value) {
        byte[] bytes = Hex.hexToBytes(value);
        return writeOctetString(bytes);
    }

    /**
     * 写入长度字段
     *
     * @param value 长度值
     * @return 写入的字节数
     */
    public int writeLength(int value) {
        if (value < 0) {
            throw new RuntimeException("negative length");
        }

        int nBytes = 0;

        if (value > 0xFFFFFF) {
            nBytes = 5;
        } else if (value > 0xFFFF) {
            nBytes = 4;
        } else if (value > 0xFF) {
            nBytes = 3;
        } else if (value > 0x7F) {
            nBytes = 2;
        } else {
            nBytes = 1;
        }

        int nShift = 0;

        for (int i = nBytes; i > 1; i--, nShift += 8) {
            increaseDataSize(1);
            buffer[buffer.length - dataSize] = (byte) (value >> nShift);
        }

        increaseDataSize(1);

        if (nBytes > 1) {
            buffer[buffer.length - dataSize] = (byte) ((nBytes - 1) | 0x80);
        } else {
            buffer[buffer.length - dataSize] = (byte) value;
        }

        return nBytes;
    }

    /**
     * 写入八位字节串
     *
     * @param value 字节数组
     * @return 写入的字节数
     */
    public int writeOctetString(byte[] value) {
        increaseDataSize(value.length);
        System.arraycopy(value, 0, buffer, buffer.length - dataSize, value.length);
        return value.length;
    }

    /**
     * 写入单个字节
     *
     * @param value 字节值
     * @return 写入的字节数
     */
    public int writeByte(byte value) {
        increaseDataSize(1);
        buffer[buffer.length - dataSize] = value;
        return 1;
    }

    /**
     * 写入对象标识符
     *
     * @param value OID数组
     * @return 写入的字节数
     */
    public int writeObjectIdentifier(long[] value) {
        if (value == null) {
            throw new RuntimeException("Object Identifier cannot be null");
        }
        if (value.length < 2) {
            throw new RuntimeException("Object Identifier must have at least 2 arcs");
        }
        if (value[0] > 2) {
            throw new RuntimeException("Object Identifier first arc must be 0, 1 or 2");
        }
        if (value[0] == 0 && value[1] > 39) {
            throw new RuntimeException("Object Identifier second arc must be < 40 when first arc is 0");
        }
        if (value[0] == 1 && (value[1] == 0 || value[1] > 39)) {
            throw new RuntimeException("Object Identifier second arc must be > 0 and < 40 when first arc is 1");
        }

        int size = 0;
        for (int i = value.length - 1; i > 1; i--) {
            long arc = value[i];
            boolean isLast = true;
            do {
                long aByte = arc % 128;
                arc = arc / 128;
                if (isLast) {
                    isLast = false;
                } else {
                    aByte |= 0x80;
                }
                writeByte((byte) aByte);
                size++;
            } while (arc > 0);
        }

        long arc = 40 * value[0] + value[1];
        boolean isLast = true;
        do {
            long aByte = arc % 128;
            arc = arc / 128;
            if (isLast) {
                isLast = false;
            } else {
                aByte |= 0x80;
            }
            writeByte((byte) aByte);
            size++;
        } while (arc > 0);

        return size;
    }

    /**
     * 写入相对对象标识符
     *
     * @param value 相对OID数组
     * @return 写入的字节数
     */
    public int writeRelativeOID(long[] value) {
        int size = 0;
        for (int i = value.length - 1; i >= 0; i--) {
            long arc = value[i];
            boolean isLast = true;
            do {
                long aByte = arc % 128;
                arc = arc / 128;
                if (isLast) {
                    isLast = false;
                } else {
                    aByte |= 0x80;
                }
                writeByte((byte) aByte);
                size++;
            } while (arc > 0);
        }

        return size;
    }

    /**
     * 获取追踪缓冲区（编码后的数据）
     *
     * @return 编码后的字节数组
     */
    public byte[] getTraceBuffer() {
        byte[] copy = null;

        if (dataSize > 0) {
            copy = new byte[dataSize];
            System.arraycopy(buffer, buffer.length - dataSize, copy, 0, dataSize);
        }

        return copy;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String fromBytes(byte[] bytes) {
        return fromBytes(bytes, 0, bytes.length);
    }

    /**
     * 将字节数组的指定部分转换为十六进制字符串
     *
     * @param bytes  字节数组
     * @param offset 偏移量
     * @param length 长度
     * @return 十六进制字符串
     */
    public static String fromBytes(byte[] bytes, int offset, int length) {
        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int v = bytes[j + offset] & 0xff;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0f];
        }
        return new String(hexChars);
    }

    /**
     * 将字节数组转换为八位字节串格式
     *
     * @param bytes 字节数组
     * @return 八位字节串格式字符串
     */
    public String bytesToOctetString(byte[] bytes) {
        return "'" + fromBytes(bytes) + "'H";
    }
}