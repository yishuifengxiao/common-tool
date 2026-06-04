package com.yishuifengxiao.common.tool.lang;

import com.yishuifengxiao.common.tool.text.TextUtil;

import java.util.BitSet;

/**
 * 十六进制字符串 / 二进制字符串 与 BitSet 互相转换的工具类。
 * <p>
 * 支持以下转换：
 * <ul>
 *   <li>十六进制字符串 ↔ BitSet（仅处理最后一个字节，大端字节序）</li>
 *   <li>二进制字符串（'0'/'1'） ↔ BitSet（字符串最左为最高位）</li>
 * </ul>
 * </p>
 *
 * @author yishuifengxiao
 * @version 1.0
 * @since 1.0
 */
public class HexBitset {


    /**
     * 将BIT STRING格式的HEX字符串转换为BitSet对象
     * <p>
     * 该方法首先解析HEX字符串并转换为二进制字符串，然后根据指定的位数创建BitSet并设置对应的位值。
     * 如果指定的位数大于二进制字符串长度，则自动调整为实际长度。
     * </p>
     *
     * @param hexStr BIT STRING格式的HEX字符串，前两个字符表示未使用位数，后续为数据部分
     * @param nbits 需要提取的位数，不能超过二进制字符串的实际长度
     * @return 转换后的BitSet对象，包含指定位数的有效位信息
     */
    public static BitSet hexToBitSet(String hexStr, int nbits) {
        hexStr = TextUtil.removeWhitespaceAndInvisible(hexStr);
        String bitString = hexToBitString(hexStr);
        nbits = Math.min(nbits, bitString.length());
        BitSet result = new BitSet(nbits);
        for (int i = 0; i < nbits; i++) {
            result.set(i, bitString.charAt(i) == '1');
        }
        return result;
    }

    /**
     * 将BitSet对象转换为BIT STRING格式的HEX字符串
     * <p>
     * 该方法首先将BitSet中的每一位转换为对应的二进制字符串表示（'0'或'1'），
     * 然后调用bitStringToHex方法将其转换为BIT STRING格式的十六进制字符串。
     * </p>
     *
     * @param bitSet 待转换的BitSet对象
     * @return BIT STRING格式的HEX字符串，包含填充位信息和数据部分
     */
    public static String bitSetToHex(BitSet bitSet) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bitSet.length(); i++) {
            result.append(bitSet.get(i) ? '1' : '0');
        }
        return bitStringToHex(result.toString());
    }


    /**
     * 将01二进制字符串转换为BIT STRING格式的HEX字符串
     * <p>
     * 规则：
     * 1. 找到二进制字符串中第一个'1'的位置（从右往左数，从0开始）
     * 2. 第一个字节 = '1'的位置索引
     * 3. 输出格式: [位置索引(hex)] + [数据字节(hex)]
     *
     * @param binaryStr 01二进制字符串，例如 "10000000"
     * @return HEX格式的BIT STRING字符串，例如 "0780"
     */
    public static String bitStringToHex(String binaryStr) {
        binaryStr = TextUtil.removeWhitespaceAndInvisible(binaryStr);
        // 计算填充到8的倍数
        StringBuilder paddedBinary = new StringBuilder(binaryStr);
        int remainder = paddedBinary.length() % 8;
        if (remainder != 0) {
            int paddingNeeded = 8 - remainder;
            for (int i = 0; i < paddingNeeded; i++) {
                paddedBinary.append("0");
            }
        }
        binaryStr = paddedBinary.toString();
        // 找到第一个'1'的位置（从右往左数）
        int firstOnePosFromRight = -1;
        for (int i = binaryStr.length() - 1; i >= 0; i--) {
            if (binaryStr.charAt(i) == '1') {
                firstOnePosFromRight = binaryStr.length() - 1 - i;
                break;
            }
        }
        // 如果没有找到'1'，所有位都是0
        if (firstOnePosFromRight == -1) {
            return "0000";
        }

        // 将8位一组转换为字节
        StringBuilder hexResult = new StringBuilder();
        // 先添加第一个'1'的位置索引（从右往左数）
        hexResult.append(String.format("%02X", firstOnePosFromRight));
        // 逐字节转换
        for (int i = 0; i < paddedBinary.length(); i += 8) {
            String byteStr = paddedBinary.substring(i, i + 8);
            int byteValue = Integer.parseInt(byteStr, 2);
            hexResult.append(String.format("%02X", byteValue));
        }

        return hexResult.toString();
    }

    /**
     * 将BIT STRING格式的HEX字符串转换为01二进制字符串
     * <p>
     * 规则：
     * 1. 第一个字节表示第一个'1'出现的位置（从右往左数，从0开始）
     * 2. 后续字节为数据部分
     * 3. 返回完整的二进制字符串
     *
     * @param hexStr BIT STRING格式的HEX字符串，例如 "0780"
     * @return 01二进制字符串，例如 "10000000"
     */
    public static String hexToBitString(String hexStr) {
        hexStr = TextUtil.removeWhitespaceAndInvisible(hexStr);

        // HEX字符串长度必须是偶数
        if (!Hex.isHex(hexStr)) {
            throw new IllegalArgumentException("输入的数据不是Hex");
        }
        // 剩余部分为数据
        String dataHex = hexStr.substring(2);
        if (dataHex.isEmpty()) {
            return "";
        }

        // 将数据HEX转换为二进制字符串
        StringBuilder binaryResult = new StringBuilder();
        // 每2个hex字符对应1个字节
        for (int i = 0; i < dataHex.length(); i += 2) {
            String byteHex = dataHex.substring(i, i + 2);
            int byteValue = Integer.parseInt(byteHex, 16);
            // 转为二进制字符串，补全8位
            String binaryByte = String.format("%8s", Integer.toBinaryString(byteValue & 0xFF)).replace(' ', '0');
            binaryResult.append(binaryByte);
        }

        return binaryResult.toString();
    }


}