package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

/**
 * 严格按照RFC 4493标准的CMAC实现
 * <p>CMAC（Cipher-based Message Authentication Code）是一种基于密码学的消息认证码，用于验证数据的完整性和真实性。
 * 该实现严格按照RFC 4493标准，确保在不同环境下的一致性和安全性。</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class CMAC {

    private static final int BLOCK_SIZE = 16;
    private static final byte CONST_RB = (byte) 0x87;

    /**
     * 计算数据的CMAC值（严格按照RFC 4493标准实现）
     * <p>该方法实现了RFC 4493标准中定义的CMAC算法，通过对数据进行AES加密来生成消息认证码。
     * 算法主要包含以下步骤：
     * 1. 生成子密钥K1和K2
     * 2. 准备数据块（根据完整性规则处理最后一个块）
     * 3. 使用CBC模式进行加密处理
     * </p>
     *
     * @param dataHex 十六进制格式的数据
     * @param keyHex  十六进制格式的密钥（必须是16字节的AES密钥）
     * @return 十六进制格式的CMAC值
     * @throws Exception 加密过程中可能抛出的异常
     */
    public static String calculateCMAC(String dataHex, String keyHex) throws Exception {
        byte[] data = Hex.hexToBytes(dataHex);
        byte[] key = Hex.hexToBytes(keyHex);
        // 步骤1: 计算L = AES(K, 0)
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        byte[] zero = new byte[BLOCK_SIZE];
        byte[] L = cipher.doFinal(zero);

        // 步骤2: 生成子密钥K1和K2
        byte[] K1 = leftShiftOneBit(L);
        if ((L[0] & 0x80) != 0) {
            K1[BLOCK_SIZE - 1] ^= CONST_RB;
        }

        byte[] K2 = leftShiftOneBit(K1);
        if ((K1[0] & 0x80) != 0) {
            K2[BLOCK_SIZE - 1] ^= CONST_RB;
        }

        // 步骤3: 计算块数
        int n = (data.length + BLOCK_SIZE - 1) / BLOCK_SIZE;
        if (n == 0) {
            n = 1;
        }

        // 步骤4: 准备最后一个块
        byte[] lastBlock = new byte[BLOCK_SIZE];
        boolean lastBlockComplete = (data.length % BLOCK_SIZE == 0);

        if (data.length == 0) {
            // 空数据
            lastBlock[0] = (byte) 0x80;
            xor(lastBlock, K2);
        } else if (lastBlockComplete) {
            // 完整块
            System.arraycopy(data, (n - 1) * BLOCK_SIZE, lastBlock, 0, BLOCK_SIZE);
            xor(lastBlock, K1);
        } else {
            // 不完整块
            int remaining = data.length % BLOCK_SIZE;
            System.arraycopy(data, (n - 1) * BLOCK_SIZE, lastBlock, 0, remaining);
            lastBlock[remaining] = (byte) 0x80;
            xor(lastBlock, K2);
        }

        // 步骤5: CBC-MAC计算
        byte[] cbcState = new byte[BLOCK_SIZE];
        Arrays.fill(cbcState, (byte) 0);

        // 处理前n-1个块
        for (int i = 0; i < n - 1; i++) {
            byte[] block = new byte[BLOCK_SIZE];
            System.arraycopy(data, i * BLOCK_SIZE, block, 0, BLOCK_SIZE);

            xor(cbcState, block);
            cbcState = cipher.doFinal(cbcState);
        }

        // 处理最后一个块
        xor(cbcState, lastBlock);
        cbcState = cipher.doFinal(cbcState);

        return Hex.bytesToHex(cbcState);
    }

    /**
     * 左移一位
     * <p>将输入的字节数组按位左移一位，最高位溢出丢弃，最低位补0。
     * 该操作在CMAC算法中用于生成子密钥K1和K2。</p>
     *
     * @param input 输入字节数组
     * @return 左移一位后的字节数组
     */
    private static byte[] leftShiftOneBit(byte[] input) {
        byte[] output = new byte[BLOCK_SIZE];
        int carry = 0;

        // 从最低字节到最高字节处理
        for (int i = BLOCK_SIZE - 1; i >= 0; i--) {
            int value = input[i] & 0xFF;
            output[i] = (byte) ((value << 1) | carry);
            carry = (value >>> 7) & 0x01;
        }

        return output;
    }

    /**
     * 异或操作
     * <p>对两个字节数组进行逐字节异或操作，结果存储在第一个数组中。
     * 该操作在CMAC算法中用于数据块处理。</p>
     *
     * @param a 第一个字节数组，同时也是结果存储数组
     * @param b 第二个字节数组
     */
    private static void xor(byte[] a, byte[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] ^= b[i];
        }
    }


    public static void main(String[] args) {
        try {
            // 测试用例1
            System.out.println("=== 测试用例1 ===");
            String key1 = "631DC24731C5272FB0887987580F38AC";
            String data1 = "24F7002D1128496A4403D675557590EB8718FD396C88D14DCCBBB64E664D330EA597";
            String expected1 = "22EAE95FEC0ECEAAB8634DF7DF64703E";

            String result1 = calculateCMAC(data1, key1);
            System.out.println("\n测试结果: " + result1.equalsIgnoreCase(expected1));
            System.out.println("计算值: " + result1);
            System.out.println("预期值: " + expected1);
            System.out.println();

            // 测试用例2
            System.out.println("=== 测试用例2 ===");
            String key2 = "DF44B25E2C89CD872FF19C48D2815D21";
            String data2 = "C0C7D92977793D22CC684858866AEF1487186DE97254E3E5AA420199A4416295362F";
            String expected2 = "087BE81559A5AACE22D6CE64204A504C";

            String result2 = calculateCMAC(data2, key2);
            System.out.println("\n测试结果: " + result2.equalsIgnoreCase(expected2));
            System.out.println("计算值: " + result2);
            System.out.println("预期值: " + expected2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}