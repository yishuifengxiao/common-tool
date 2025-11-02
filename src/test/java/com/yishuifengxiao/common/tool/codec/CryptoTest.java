package com.yishuifengxiao.common.tool.codec;

public class CryptoTest {
    public static void main(String[] args) {
        try {


            String originalText = "这是一段需要加密的敏感数据！";


            // 测试3DES加解密
            System.out.println("\n=== 3DES加解密测试 ===");
            String desKey = TripleDESUtil.generate3DESKey();
            System.out.println("3DES密钥: " + desKey);

            String desEncrypted = TripleDESUtil.encrypt(originalText, desKey);
            System.out.println("3DES加密后: " + desEncrypted);

            String desDecrypted = TripleDESUtil.decrypt(desEncrypted, desKey);
            System.out.println("3DES解密后: " + desDecrypted);

            System.out.println("3DES加解密验证: " + originalText.equals(desDecrypted));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}