package com.yishuifengxiao.common.tool.asn1;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * <p>
 * ASN.1 值写入器
 * </p>
 * <p>提供将各种ASN.1数据类型写入输出流的功能，支持整数、布尔值、字符串、位串、对象标识符等类型的序列化。</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ASNValueWriter {

    /**
     * 位掩码数组，用于位操作
     */
    private static final byte[] BIT_MASK = new byte[]{(byte) 0x80, (byte) 0x40, (byte) 0x20, (byte) 0x10,
            (byte) 0x08, (byte) 0x04, (byte) 0x02, (byte) 0x01};

    /**
     * 输出写入器
     */
    private final PrintWriter writer;

    /**
     * 序列栈，用于管理嵌套结构
     */
    private final ArrayList<Sequence> sequences = new ArrayList<>();

    /**
     * 是否等待选择值
     */
    private boolean isWaitingForChoiceValue = false;

    /**
     * 构造函数，使用PrintWriter创建写入器
     *
     * @param writer PrintWriter实例
     */
    public ASNValueWriter(PrintWriter writer) {
        this.writer = writer;
    }

    /**
     * 开始数组写入
     *
     * @param elementName 元素名称
     */
    public void beginArray(String elementName) {
        indentValue();
        sequences.add(new Sequence(sequences.size() + 1, true, elementName));
        writer.println("{");
    }

    /**
     * 结束数组写入
     */
    public void endArray() {
        sequences.remove(sequences.size() - 1);
        indent();
        writer.println("}");
    }

    /**
     * 开始序列写入
     */
    public void beginSequence() {
        indentValue();
        sequences.add(new Sequence(sequences.size() + 1, false));
        writer.println("{");
    }

    /**
     * 结束序列写入
     */
    public void endSequence() {
        sequences.remove(sequences.size() - 1);
        indent();
        writer.println("}");
    }

    /**
     * 写入组件名称
     *
     * @param componentName 组件名称
     */
    public void writeComponent(String componentName) {
        indentComponent();
        writer.print(componentName + " ");
    }

    /**
     * 写入选择项
     *
     * @param selectionName 选择项名称
     */
    public void writeSelection(String selectionName) {
        if ((sequences.size() != 0) && sequences.get(sequences.size() - 1).isArray
                && !isWaitingForChoiceValue) {
            indentComponent();
        }

        if (sequences.size() != 0) {
            Sequence sequence = sequences.get(sequences.size() - 1);

            if (sequence.isArray() && !sequence.getElementName().isEmpty()) {
                writer.print(sequence.getElementName() + " ");
            }
        }

        writer.print(selectionName + " : ");
        isWaitingForChoiceValue = true;
    }

    /**
     * 写入Byte类型整数
     *
     * @param value Byte值
     */
    public void writeInteger(Byte value) {
        indentValue();
        writer.println(value);
    }

    /**
     * 写入Short类型整数
     *
     * @param value Short值
     */
    public void writeInteger(Short value) {
        indentValue();
        writer.println(value);
    }

    /**
     * 写入Integer类型整数
     *
     * @param value Integer值
     */
    public void writeInteger(Integer value) {
        indentValue();
        writer.println(value);
    }

    /**
     * 写入Long类型整数
     *
     * @param value Long值
     */
    public void writeInteger(Long value) {
        indentValue();
        writer.println(value);
    }

    /**
     * 写入BigInteger类型整数
     *
     * @param value BigInteger值
     */
    public void writeInteger(java.math.BigInteger value) {
        indentValue();
        writer.println(value);
    }

    /**
     * 写入布尔值
     *
     * @param value Boolean值
     */
    public void writeBoolean(Boolean value) {
        indentValue();

        boolean boolValue = value.booleanValue();

        if (boolValue) {
            writer.println("TRUE");
        } else {
            writer.println("FALSE");
        }
    }

    /**
     * 写入NULL值
     */
    public void writeNull() {
        indentValue();
        writer.println("NULL");
    }

    /**
     * 写入枚举值
     *
     * @param enumerated 枚举字符串
     */
    public void writeEnumerated(String enumerated) {
        indentValue();
        writer.println(enumerated);
    }

    /**
     * 写入受限字符字符串
     *
     * @param value 字符串值
     */
    public void writeRestrictedCharacterString(String value) {
        indentValue();
        writer.println("\"" + value + "\"");
    }

    /**
     * 写入标识符
     *
     * @param value 标识符值
     */
    public void writeIdentifier(String value) {
        indentValue();
        writer.println(value);
    }

    /**
     * 写入八位字节串
     *
     * @param value 字节数组
     */
    public void writeOctetString(byte[] value) {
        indentValue();
        writer.print("'" + bytesToString(value) + "'H");
        writer.println();
    }

    /**
     * 写入位串
     *
     * @param value BitSet值
     */
    public void writeBitString(BitSet value) {
        indentValue();

        int size = value.length();
        StringBuffer buffer = new StringBuffer();
        buffer.append("'");

        if (size < 16) {
            for (int i = 0; i < size; i++) {
                buffer.append(value.get(i) ? "1" : "0");
            }
            buffer.append("'B");
        } else {
            buffer.append(bytesToString(bitSetToBytes(value)));
            buffer.append("'H");
        }
        writer.println(buffer.toString());
    }

    /**
     * 写入位串列表
     *
     * @param bitList 位名称列表
     */
    public void writeBitString(List<String> bitList) {
        indentValue();

        boolean isFirst = true;
        writer.print("{ ");
        for (String bit : bitList) {
            if (isFirst) {
                isFirst = false;
            } else {
                writer.print(", ");
            }
            writer.print(bit);
        }
        writer.println(" }");
    }

    /**
     * 写入对象标识符
     *
     * @param value OID数组
     */
    public void writeObjectIdentifier(long[] value) {
        indentValue();
        writer.print("{ ");
        for (long arc : value) {
            writer.print(arc + " ");
        }
        writer.println("}");
    }

    /**
     * 写入相对对象标识符
     *
     * @param value 相对OID数组
     */
    public void writeRelativeOID(long[] value) {
        writeObjectIdentifier(value);
    }

    /**
     * 刷新输出缓冲区
     */
    public void flush() {
        writer.flush();
    }

    /**
     * 输出缩进
     */
    private void indent() {
        if (sequences.size() != 0) {
            writer.print(sequences.get(sequences.size() - 1).getIndent());
        }
    }

    /**
     * 输出组件缩进
     */
    private void indentComponent() {
        if (sequences.size() != 0) {
            Sequence sequence = sequences.get(sequences.size() - 1);

            if (sequence.isEmpty) {
                writer.print(sequence.getIndent());
            } else {
                writer.print(",");
                writer.print(sequence.getIndent().substring(1));
            }

            sequence.setEmpty(false);
        }
    }

    /**
     * 输出值缩进
     */
    private void indentValue() {
        if (sequences.size() != 0) {
            Sequence sequence = sequences.get(sequences.size() - 1);

            if (sequence.isArray() && !isWaitingForChoiceValue) {
                if (sequence.isEmpty) {
                    writer.print(sequence.getIndent());
                } else {
                    writer.print(",");
                    writer.print(sequence.getIndent().substring(1));
                }
                if (!sequence.getElementName().isEmpty()) {
                    writer.print(sequence.getElementName() + " ");
                }

                sequence.setEmpty(false);
            }
        }

        isWaitingForChoiceValue = false;
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param buffer 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToString(byte[] buffer) {
        String text = "";

        for (int i = 0; i < buffer.length; i++) {
            String byteText = Integer.toHexString(buffer[i]);

            switch (byteText.length()) {
                case 1:
                    byteText = "0" + byteText;
                    break;
                case 2:
                    break;
                default:
                    byteText = byteText.substring(byteText.length() - 2);
                    break;
            }

            if (i == 0) {
                text = byteText;
            } else {
                text += byteText;
            }
        }

        return text.toUpperCase();
    }

    /**
     * 将BitSet转换为字节数组
     *
     * @param value BitSet值
     * @return 字节数组
     */
    private static byte[] bitSetToBytes(BitSet value) {
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

        byte[] buffer = new byte[nBytes];

        int currentIndex = 0;
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

        return buffer;
    }

    /**
     * 内部类：表示序列状态
     */
    private class Sequence {
        /**
         * 是否为数组
         */
        private boolean isArray = false;

        /**
         * 是否为空
         */
        private boolean isEmpty = true;

        /**
         * 元素名称
         */
        private String elementName = "";

        /**
         * 缩进字符串
         */
        private String indent = "";

        /**
         * 构造函数
         *
         * @param rank        嵌套层级
         * @param isArray     是否为数组
         * @param elementName 元素名称
         */
        public Sequence(int rank, boolean isArray, String elementName) {
            for (int i = 0; i < rank; i++) {
                indent += "  ";
            }
            this.isArray = isArray;
            this.elementName = elementName;
        }

        /**
         * 构造函数
         *
         * @param rank    嵌套层级
         * @param isArray 是否为数组
         */
        public Sequence(int rank, boolean isArray) {
            for (int i = 0; i < rank; i++) {
                indent += "  ";
            }
            this.isArray = isArray;
        }

        /**
         * 获取缩进字符串
         *
         * @return 缩进字符串
         */
        public String getIndent() {
            return indent;
        }

        /**
         * 设置是否为空
         *
         * @param isEmpty 是否为空
         */
        public void setEmpty(boolean isEmpty) {
            this.isEmpty = isEmpty;
        }

        /**
         * 判断是否为数组
         *
         * @return 是否为数组
         */
        public boolean isArray() {
            return isArray;
        }

        /**
         * 获取元素名称
         *
         * @return 元素名称
         */
        public String getElementName() {
            return elementName;
        }
    }
}