package com.yishuifengxiao.common.tool.lang;

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
     * 将十六进制字符串转换为 BitSet。
     *
     * @param hex 十六进制字符串（支持 "0780"、"0x0780" 等，大小写不敏感）
     * @return 对应的 BitSet
     */
    public static BitSet hexToBitSet(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new BitSet();
        }
        hex = hex.trim();
        if (hex.startsWith("0x") || hex.startsWith("0X")) {
            hex = hex.substring(2);
        }
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        int byteLen = hex.length() / 2;
        byte[] bytes = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            String byteStr = hex.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(byteStr, 16);
        }

        BitSet bitSet = new BitSet();
        if (bytes.length > 0) {
            byte b = bytes[bytes.length - 1];
            for (int j = 0; j < 8; j++) {
                if ((b & (1 << (7 - j))) != 0) {
                    bitSet.set(7 - j);
                }
            }
        }
        return bitSet;
    }

    /**
     * 将 BitSet 转换为十六进制字符串（自动根据最高位确定字节长度）。
     * <p>
     * 特殊处理规则：
     * <ul>
     *   <li>当最高位在低字节（bit 0-7）时，返回两字节格式</li>
     *   <li>高字节的值为最高位的位置索引（例如 bit7=1 时高字节为 0x07）</li>
     * </ul>
     * </p>
     *
     * @param bitSet 待转换的 BitSet
     * @return 十六进制字符串，若 BitSet 为空则返回 "00"
     */
    public static String bitSetToHex(BitSet bitSet) {
        if (bitSet == null || bitSet.isEmpty()) {
            return "00";
        }
        int maxBit = bitSet.length() - 1;
        int byteCount = (maxBit / 8) + 1;

        if (maxBit < 8) {
            byteCount = 2;
        }

        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                int index = (byteCount - 1 - i) * 8 + j;
                if (bitSet.get(index)) {
                    b |= (1 << j);
                }
            }
            bytes[i] = b;
        }

        if (byteCount == 2 && bytes[0] == 0) {
            bytes[0] = (byte) maxBit;
        }
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /**
     * 将 BitSet 转换为固定字节数的十六进制字符串（高位补零）。
     * <p>
     * 如果实际长度超过目标长度，则截断高位；如果不足，则在高位补零。
     * </p>
     *
     * @param bitSet    待转换的 BitSet
     * @param byteCount 固定字节数（不足时高位补零）
     * @return 固定长度的十六进制字符串
     */
    public static String bitSetToHexFixed(BitSet bitSet, int byteCount) {
        String fullHex = bitSetToHex(bitSet);
        int fullLen = fullHex.length();
        int targetLen = byteCount * 2;
        if (fullLen >= targetLen) {
            return fullHex.substring(fullLen - targetLen);
        } else {
            return "0".repeat(targetLen - fullLen) + fullHex;
        }
    }

    // ==================== 二进制字符串 ↔ BitSet ====================

    /**
     * 将二进制字符串转换为 BitSet。
     * <p>
     * 转换约定：字符串最左字符对应最高位（索引 = len-1），最右字符对应最低位（索引 0）。
     * </p>
     * <p>
     * 示例："10000000" → BitSet 中只有索引 7 被设置。
     * </p>
     *
     * @param binary 二进制字符串（仅含 '0' 和 '1'）
     * @return 对应的 BitSet
     * @throws IllegalArgumentException 当字符串包含非 '0' 和 '1' 的字符时抛出
     */
    public static BitSet binaryToBitSet(String binary) {
        if (binary == null || binary.isEmpty()) {
            return new BitSet();
        }
        BitSet bitSet = new BitSet();
        int len = binary.length();
        for (int i = 0; i < len; i++) {
            char c = binary.charAt(i);
            if (c == '1') {
                bitSet.set(len - 1 - i);
            } else if (c != '0') {
                throw new IllegalArgumentException("二进制字符串只能包含 '0' 和 '1'");
            }
        }
        return bitSet;
    }

    /**
     * 将 BitSet 转换为二进制字符串（输出从最高设置位到 bit0，不足8位时高位补零）。
     * <p>
     * 输出规则：
     * <ul>
     *   <li>至少输出8位二进制字符串</li>
     *   <li>如果最高位索引小于7，则在高位补零至8位</li>
     *   <li>如果最高位索引大于等于7，则输出从最高位到bit0的所有位</li>
     * </ul>
     * </p>
     * <p>
     * 示例：
     * <ul>
     *   <li>BitSet 中只有 bit7 为 1 → 输出 "10000000"</li>
     *   <li>BitSet 中只有 bit4 为 1 → 输出 "00010000"</li>
     * </ul>
     * </p>
     *
     * @param bitSet 待转换的 BitSet
     * @return 二进制字符串，若 BitSet 为空则返回 "0"
     */
    public static String bitSetToBinary(BitSet bitSet) {
        if (bitSet == null || bitSet.isEmpty()) {
            return "0";
        }
        int maxBit = bitSet.length() - 1;
        StringBuilder sb = new StringBuilder();
        int startBit = Math.max(maxBit, 7);
        for (int i = startBit; i >= 0; i--) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }

    /**
     * 将 BitSet 转换为固定长度的二进制字符串（高位补零或截断高位）。
     * <p>
     * 转换规则：
     * <ul>
     *   <li>如果 BitSet 最高位索引小于 fixedLength-1，则在高位补零</li>
     *   <li>如果 BitSet 最高位索引大于等于 fixedLength，则截断超出部分</li>
     * </ul>
     * </p>
     *
     * @param bitSet      待转换的 BitSet
     * @param fixedLength 输出字符串长度（必须为正整数）
     * @return 固定长度的二进制字符串
     */
    public static String bitSetToBinaryFixed(BitSet bitSet, int fixedLength) {
        if (bitSet == null || bitSet.isEmpty()) {
            return "0".repeat(fixedLength);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = fixedLength - 1; i >= 0; i--) {
            sb.append(bitSet.get(i) ? '1' : '0');
        }
        return sb.toString();
    }


}