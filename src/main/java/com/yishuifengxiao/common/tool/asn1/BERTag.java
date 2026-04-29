package com.yishuifengxiao.common.tool.asn1;

import java.util.ArrayList;

/**
 * <p>
 * BER标签工具类
 * </p>
 * <p>提供ASN.1 BER编码中标签的解析和构建功能，支持标签类、标签号和标签形式的处理。</p>
 *
 * @author yishuifengxiao
 * @version 1.0.0
 * @since 1.0.0
 */
public class BERTag {

    /**
     * 标签类枚举
     */
    public static final class Class {
        /**
         * 类名称
         */
        private final String name;

        /**
         * 通用类
         */
        public static final Class CONTEXT = new Class("CONTEXT");

        /**
         * 上下文特定类
         */
        public static final Class UNIVERSAL = new Class("UNIVERSAL");

        /**
         * 应用类
         */
        public static final Class APPLICATION = new Class("APPLICATION");

        /**
         * 私有类
         */
        public static final Class PRIVATE = new Class("PRIVATE");

        /**
         * 私有构造函数
         *
         * @param name 类名称
         */
        private Class(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * 标签形式枚举
     */
    public static final class Form {
        /**
         * 形式名称
         */
        private final String name;

        /**
         * 原始形式
         */
        public static final Form PRIMITIVE = new Form("PRIMITIVE");

        /**
         * 构造形式
         */
        public static final Form CONSTRUCTED = new Form("CONSTRUCTED");

        /**
         * 私有构造函数
         *
         * @param name 形式名称
         */
        private Form(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * 标签类
     */
    private Class tagClass;

    /**
     * 标签号
     */
    private int tagNumber = -1;

    /**
     * 标签形式
     */
    private Form tagForm;

    /**
     * 构造函数
     *
     * @param tagClass  标签类
     * @param tagNumber 标签号
     * @param tagForm   标签形式
     */
    public BERTag(Class tagClass, int tagNumber, Form tagForm) {
        this.tagClass = tagClass;
        this.tagNumber = tagNumber;
        this.tagForm = tagForm;
    }

    /**
     * 从编码字节数组构造标签
     *
     * @param tag 编码的标签字节数组
     */
    public BERTag(byte[] tag) {
        int mask = tag[0] & 0xC0;

        if (mask == 0x00) {
            tagClass = Class.UNIVERSAL;
        } else if (mask == 0x40) {
            tagClass = Class.APPLICATION;
        } else if (mask == 0x80) {
            tagClass = Class.CONTEXT;
        } else {
            tagClass = Class.PRIVATE;
        }

        mask = tag[0] & 0x20;

        if (mask == 0x20) {
            tagForm = Form.CONSTRUCTED;
        } else {
            tagForm = Form.PRIMITIVE;
        }

        mask = tag[0] & 0x1F;

        if (mask == 0x1F) {
            if (tag.length == 1) {
                throw new RuntimeException("Malformed tag: needs more bytes");
            }

            tagNumber = 0;

            for (int i = tag.length - 1, mult = 1; i > 0; i--, mult *= 128) {
                tagNumber += ((tag[i] & 0x7f) * mult);
            }
        } else {
            tagNumber = mask;
        }
    }

    /**
     * 获取标签的Byte列表表示
     *
     * @return Byte数组
     */
    public Byte[] getByteList() {
        if (tagNumber == -1) {
            throw new RuntimeException("Number not set");
        }

        ArrayList<Byte> result = new ArrayList<>();

        int int1 = (tagForm == Form.PRIMITIVE) ? 0x00 : 0x20;

        if (tagClass == Class.UNIVERSAL) {
            int1 |= 0;
        } else if (tagClass == Class.APPLICATION) {
            int1 |= 0x40;
        } else if (tagClass == Class.CONTEXT) {
            int1 |= 0x80;
        } else if (tagClass == Class.PRIVATE) {
            int1 |= 0xc0;
        }

        if (tagNumber < 31) {
            int1 += tagNumber;
            result.add(Byte.valueOf((byte) int1));
        } else {
            int1 += 31;
            result.add(Byte.valueOf((byte) int1));

            for (int highBits = tagNumber; highBits != 0; highBits = highBits >> 7) {
                int1 = (highBits & 0x7f) | 0x80;
                result.add(1, Byte.valueOf((byte) int1));
            }

            Byte last = result.get(result.size() - 1);
            result.remove(last);
            result.add(Byte.valueOf((byte) (last.byteValue() & (byte) 0x7f)));
        }

        return result.toArray(new Byte[0]);
    }

    /**
     * 获取标签的字节数组表示
     *
     * @return 字节数组
     */
    public byte[] getByteArray() {
        Byte[] tagBytes = getByteList();

        byte[] array = new byte[tagBytes.length];

        for (int i = 0; i < tagBytes.length; i++) {
            array[i] = tagBytes[i].byteValue();
        }

        return array;
    }

    @Override
    public String toString() {
        return tagForm.toString() + "_" + tagClass.toString() + "_" + tagNumber;
    }
}