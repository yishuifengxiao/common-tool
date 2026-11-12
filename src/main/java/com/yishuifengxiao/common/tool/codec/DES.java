package com.yishuifengxiao.common.tool.codec;

import com.yishuifengxiao.common.tool.lang.Hex;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * <p>
 * DES加密工具
 * </p>
 * <p>默认会使用DES/ECB/PKCS5Padding 填充模式</p>
 * <p>基于DES加解密实现的加密工具，该工具可以进行可逆加密，加密时的秘钥很重要，一定要自己改秘钥，打死也不要告诉其他人。</p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class DES {

    /**
     * 密钥，是加密解密的凭据，长度为8的倍数
     */
    private static final String PASSWORD_CRYPT_KEY = "yishui@#";
    /**
     * 加密方式
     */
    private static final String TYPE = "DES";

    /**
     * 加密转换方式，显式声明避免依赖JCE默认值
     */
    private static final String TRANSFORMATION = "DES/ECB/PKCS5Padding";

    /**
     * DES密钥的有效长度（字节）
     */
    private static final int LENGTH = 8;


    /**
     * 对输入的数据进行加密
     *
     * @param key  加密的密钥，如果为空则以默认密码进行加密；不足8个字符自动补0，超过8个字符截断为前8个字符
     * @param data 需要加密的数据
     * @return 加密后的数据, null表示加密失败
     */
    public static final String encrypt(String key, String data) {
        if (null == data) {
            return null;
        }
        try {
            return Hex.bytesToHex(encrypt(data.getBytes(StandardCharsets.UTF_8),
                    keyValidate(key).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            // 不记录明文数据，避免敏感信息进入日志
            if (log.isDebugEnabled()) {
                log.debug("数据加密失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 用默认的密码进行数据加密
     *
     * @param data 需要加密的数据
     * @return 加密后的数据, null表示加密失败
     */
    public static final String encrypt(String data) {
        return encrypt(null, data);
    }

    /**
     * 对输入的数据进行解密
     *
     * @param key  解密的密钥，如果为空则以默认密码进行解密；不足8个字符自动补0，超过8个字符截断为前8个字符
     * @param data 需要解密的数据
     * @return 解密后的数据, null表示解密失败
     */
    public static final String decrypt(String key, String data) {
        if (null == data) {
            return null;
        }
        try {
            return new String(decrypt(Hex.hexToBytes(data), keyValidate(key).getBytes(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 不记录密文数据，避免敏感信息进入日志
            if (log.isDebugEnabled()) {
                log.debug("数据解密失败: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 用默认的密码进行数据解密
     *
     * @param data 需要解密的数据
     * @return 解密后的数据, null表示解密失败
     */
    public static final String decrypt(String data) {
        return decrypt(null, data);
    }

    /**
     * 加密
     *
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回加密后的数据
     * @throws Exception
     */
    private static byte[] encrypt(byte[] src, byte[] key) throws Exception {

        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();

        // 从原始密匙数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密匙工厂，然后用它把DESKeySpec转换成
        // 一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(TYPE);

        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // 用密匙初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);

        // 现在，获取数据并加密
        // 正式执行加密操作
        return cipher.doFinal(src);

    }

    /**
     * 解密
     *
     * @param src 数据源
     * @param key 密钥，长度必须是8的倍数
     * @return 返回解密后的原始数据
     * @throws Exception
     */
    private static byte[] decrypt(byte[] src, byte[] key) throws Exception {

        // DES算法要求有一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
        // 从原始密匙数据创建一个DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);

        // 创建一个密匙工厂，然后用它把DESKeySpec对象转换成
        // 一个SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(TYPE);

        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        // 用密匙初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);

        // 现在，获取数据并解密
        // 正式执行解密操作
        return cipher.doFinal(src);

    }

    /**
     * 对key进行校验
     *
     * @param key 原始的key
     * @return 校验之后的可以
     */
    private static String keyValidate(String key) {
        if (StringUtils.isBlank(key)) {
            return PASSWORD_CRYPT_KEY;
        }
        String trimmed = key.trim();
        // DES 实际只使用密钥的前8个字节，这里统一为"不足补0、超长截断"，
        // 避免出现密钥长度看似合法、实际生效部分与使用者预期不一致的问题
        if (trimmed.length() >= LENGTH) {
            return trimmed.substring(0, LENGTH);
        }
        StringBuilder sb = new StringBuilder(trimmed);
        while (sb.length() < LENGTH) {
            sb.append('0');
        }
        return sb.toString();
    }


    /**
     * 解密数据
     *
     * @param data 待解密的数据，应为十六进制格式的字符串
     * @return 解密后的数据字符串
     * @throws Exception 解密过程中可能抛出的异常
     */
    public final static String decryptData(String data) throws Exception {
        return decryptData(null, data);
    }


    /**
     * 解密数据
     *
     * @param key  解密密钥，如果为空或空白字符则使用默认密钥PASSWORD_CRYPT_KEY
     * @param data 待解密的数据，应为十六进制格式的字符串
     * @return 解密后的明文字符串
     * @throws Exception 解密过程中可能抛出的异常
     */
    public final static String decryptData(String key, String data) throws Exception {
        // 将十六进制数据转换为字节数组并解密，然后转换为字符串返回
        return new String(decrypt(Hex.hexToBytes(data), keyValidate(key).getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }


    /**
     * 加密数据方法
     *
     * @param data 待加密的数据字符串
     * @return 加密后的数据字符串
     * @throws Exception 加密过程中可能抛出的异常
     */
    public final static String encryptData(String data) throws Exception {
        return encryptData(null, data);
    }


    /**
     * 加密数据
     *
     * @param key  加密密钥，如果为空则使用默认密钥PASSWORD_CRYPT_KEY
     * @param data 待加密的数据
     * @return 加密后的数据，以十六进制字符串形式返回
     * @throws Exception 加密过程中可能抛出的异常
     */
    public final static String encryptData(String key, String data) throws Exception {
        // 对数据进行加密并转换为十六进制字符串
        return Hex.bytesToHex(encrypt(data.getBytes(StandardCharsets.UTF_8),
                keyValidate(key).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * 计算数据的MAC（消息认证码）
     *
     * @param data 待计算MAC的数据字符串
     * @return 返回计算得到的MAC值
     * @throws Exception 当计算过程中发生错误时抛出异常
     */
    public static String mac(String data) throws Exception {

        return mac(null, data);
    }


    /**
     * 计算数据的MAC(Message Authentication Code)值
     *
     * @param key  用于加密的密钥字符串
     * @param data 待计算MAC的数据字符串(十六进制格式)
     * @return 返回计算得到的MAC值(16位十六进制字符串)
     * @throws Exception 当加密过程发生错误时抛出异常
     */
    public static String mac(String key, String data) throws Exception {
        // 将十六进制数据转换为字节数组
        byte[] planData = Hex.hexToBytes(data);
        final int dataLength = planData.length;
        final int lastLength = dataLength % 8;
        final int lastBlockLength = lastLength == 0 ? 8 : lastLength;
        final int blockCount = dataLength / 8 + (lastLength > 0 ? 1 : 0);

        // 将数据按8字节分组存储到二维数组中
        byte[][] dataBlock = new byte[blockCount][8];
        for (int i = 0; i < blockCount; i++) {
            int copyLength = i == blockCount - 1 ? lastBlockLength : 8;
            System.arraycopy(planData, i * 8, dataBlock[i], 0, copyLength);
        }

        // 执行MAC计算：异或后DES加密，循环处理所有数据块
        byte[] desXor = new byte[8];
        for (int i = 0; i < blockCount; i++) {
            byte[] tXor = Hex.xOr(desXor, dataBlock[i]);
            desXor = encrypt(tXor, keyValidate(key).getBytes(StandardCharsets.UTF_8));
        }

        // 将最终结果转换为十六进制字符串并截取前16位作为MAC值
        return Hex.bytesToHex(desXor).substring(0, 16);
    }


}
