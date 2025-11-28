package com.yishuifengxiao.common.tool.text;

import java.io.Serializable;

/**
 * TLV解析工具类
 *
 * @author yishui
 * @version 1.0.0
 */
public class TLV implements Serializable {
    private String data;
    private String tag;
    private String value;
    private String remainingData;
    private int valueLength;
    private String error;
    private boolean success;

    /**
     * 构造方法
     *
     * @param data 待处理的原始数据字符串，可能包含空白字符和换行符
     */
    private TLV(String data) {
        this.data = cleanData(data);
    }

    /**
     * 静态工厂方法，用于创建TLV对象实例
     *
     * @param data 用于初始化TLV对象的字符串数据
     * @return 返回新创建的TLV对象实例
     */
    // 静态工厂方法
    public static TLV of(String data) {
        return new TLV(data);
    }


    /**
     * 清理数据函数
     * <p>
     * 该函数用于清理输入的字符串数据，主要功能包括：
     * 1. 将null值转换为空字符串
     * 2. 移除所有空白字符和换行符
     * 3. 将字符串转换为大写格式
     *
     * @param data 待清理的原始数据字符串，可能为null
     * @return 清理后的字符串，如果输入为null则返回空字符串
     */
    // 清理数据
    private String cleanData(String data) {
        if (data == null) return "";
        return data.replaceAll("\\s+|\\n", "").toUpperCase();
    }


    /**
     * 验证hex数据
     *
     * @param data 待验证的字符串数据
     * @return 如果数据是有效的十六进制格式则返回true，否则返回false
     */
    private boolean isValidHex(String data) {
        return data.matches("^[0-9A-F]*$");
    }


    /**
     * 统一的解析方法 - 支持链式调用
     *
     * @param tag 标签字符串，用于匹配数据开头的标签
     * @return 返回TLV对象本身，支持链式调用
     */
    // 统一的解析方法 - 支持链式调用
    public TLV parse(String tag) {
        // 如果已经解析过且有剩余数据，则使用剩余数据继续解析
        if (success && !remainingData.isEmpty() && !this.data.equals(remainingData)) {
            this.data = remainingData;
        }

        // 重置状态（保留data）
        resetState();
        this.tag = tag == null ? "" : cleanData(tag);

        // 验证数据有效性
        if (!isValidHex(data)) {
            setError("无效的Hex数据");
            return this;
        }

        if (data.isEmpty()) {
            setError("数据为空");
            return this;
        }

        // 检查是否以tag开头（tag不为空时）
        if (!this.tag.isEmpty() && !data.startsWith(this.tag)) {
            setError("数据不以指定标签开头");
            return this;
        }

        // 执行实际解析操作
        try {
            int startIndex = this.tag.isEmpty() ? 0 : this.tag.length();
            parseFromIndex(startIndex);
            this.success = true;
            return this;
        } catch (Exception e) {
            setError("解析错误: " + e.getMessage());
            return this;
        }
    }


    /**
     * 从指定位置开始解析数据
     *
     * @param startIndex 起始解析位置索引
     */
    // 从指定位置解析
    private void parseFromIndex(int startIndex) {
        String workingData = data.substring(startIndex);

        // 解析长度字段，获取长度信息
        LengthInfo lengthInfo = parseLength(workingData);
        if (lengthInfo == null) {
            throw new IllegalArgumentException("长度字段解析失败");
        }

        // 计算值字段的起始和结束位置
        int valueStart = lengthInfo.lengthFieldSize;
        int valueEnd = valueStart + lengthInfo.valueLength * 2;

        // 验证数据是否足够长以包含值字段
        if (valueEnd > workingData.length()) {
            throw new IllegalArgumentException("数据长度不足");
        }

        // 提取值字段和剩余数据
        this.value = workingData.substring(valueStart, valueEnd);
        this.remainingData = workingData.substring(valueEnd);
        this.valueLength = lengthInfo.valueLength;
    }


    /**
     * 解析长度字段
     *
     * @param data 输入的十六进制字符串数据
     * @return LengthInfo对象，包含解析出的长度值和占用的字节数；如果解析失败则返回null
     */
    private LengthInfo parseLength(String data) {
        if (data.length() < 2) return null;

        String firstByte = data.substring(0, 2);
        int firstLength = Integer.parseInt(firstByte, 16);

        // 处理单字节长度标识（长度小于等于0x7F）
        if (firstLength <= 0x7F) {
            return new LengthInfo(firstLength, 2);
        }
        // 处理多字节长度标识（第一个字节标识后续长度字节数）
        else if (firstLength == 0x81 && data.length() >= 4) {
            int length = Integer.parseInt(data.substring(2, 4), 16);
            return new LengthInfo(length, 4);
        } else if (firstLength == 0x82 && data.length() >= 6) {
            int length = Integer.parseInt(data.substring(2, 6), 16);
            return new LengthInfo(length, 6);
        } else if (firstLength == 0x83 && data.length() >= 8) {
            int length = Integer.parseInt(data.substring(2, 8), 16);
            return new LengthInfo(length, 8);
        }

        return null;
    }


    /**
     * 重置状态（保留data）
     * 将对象的所有状态属性重置为初始值，但保留data数据不变
     */
    private void resetState() {
        this.tag = "";
        this.value = "";
        this.remainingData = "";
        this.valueLength = 0;
        this.error = "";
        this.success = false;
    }


    /**
     * 设置错误信息并标记解析失败
     * <p>
     * 该方法用于在解析过程中遇到错误时设置错误信息，并将success状态置为false
     *
     * @param error 错误描述信息，用于记录解析失败的原因
     */
    private void setError(String error) {
        this.error = error;  // 设置错误信息
        this.success = false;  // 标记解析状态为失败
    }

    /**
     * 获取TLV结构中的标签字段
     * <p>
     * 返回当前解析结果中的标签部分，如果未设置标签或解析失败则返回空字符串
     *
     * @return 当前TLV对象的标签字符串
     */
    public String getTag() {
        return tag;
    }

    /**
     * 获取TLV结构中的值字段
     * <p>
     * 返回当前解析结果中的值部分，如果解析失败或未解析则返回空字符串
     *
     * @return 当前TLV对象的值字符串，十六进制格式
     */
    public String getValue() {
        return value;
    }

    /**
     * 获取剩余未解析的数据
     * <p>
     * 返回当前TLV解析完成后剩余的未处理数据部分，可用于链式解析下一个TLV结构
     *
     * @return 剩余未解析的十六进制字符串数据，如果没有剩余数据则返回空字符串
     */
    public String getRemainingData() {
        return remainingData;
    }

    /**
     * 获取TLV结构中值字段的字节长度
     * <p>
     * 返回当前解析结果中值部分的字节长度，如果解析失败或未解析则返回0
     *
     * @return 当前TLV对象值字段的字节长度
     */
    public int getValueLength() {
        return valueLength;
    }

    /**
     * 获取解析过程中的错误信息
     * <p>
     * 当解析失败时，返回具体的错误描述信息；如果解析成功或尚未解析，则返回空字符串
     *
     * @return 当前TLV解析过程中记录的错误信息字符串
     */
    public String getError() {
        return error;
    }

    /**
     * 获取解析操作是否成功
     * <p>
     * 该方法用于判断最近一次TLV解析操作是否成功完成
     *
     * @return 如果解析成功则返回true，否则返回false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取原始数据
     * <p>
     * 返回经过清理后的原始数据字符串（已移除空白字符和换行符，并转为大写格式）
     *
     * @return 清理后的原始数据字符串，如果原始数据为null则返回空字符串
     */
    public String getOriginalData() {
        return data;
    }

    /**
     * 打印TLV解析结果到标准输出
     * <p>
     * 该方法会格式化输出当前TLV对象的解析结果，包括：
     * 1. 解析状态（成功/失败）
     * 2. 成功时输出标签、值、值长度和剩余数据
     * 3. 失败时输出错误信息
     * 4. 最后输出分隔线
     * </p>
     *
     * @return 返回TLV对象本身，支持链式调用
     */
    // 打印结果
    public TLV print() {
        System.out.println("解析结果: " + (success ? "成功" : "失败"));

        if (success) {
            System.out.println("标签: " + (tag.isEmpty() ? "[LV结构]" : tag));
            System.out.println("值: " + value);
            System.out.println("值长度: " + valueLength + " 字节");
            System.out.println("剩余数据: " + (remainingData.isEmpty() ? "[无]" : remainingData));
        } else {
            System.out.println("错误: " + error);
        }
        System.out.println("---");
        return this;
    }

    /**
     * 存储TLV结构中长度字段的信息
     */
    public static class LengthInfo implements Serializable {
        final int valueLength;
        final int lengthFieldSize;

        LengthInfo(int valueLength, int lengthFieldSize) {
            this.valueLength = valueLength;
            this.lengthFieldSize = lengthFieldSize;
        }
    }
}