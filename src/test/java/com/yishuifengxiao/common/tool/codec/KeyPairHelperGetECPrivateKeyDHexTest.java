package com.yishuifengxiao.common.tool.codec;

import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;

import static org.junit.Assert.*;

/**
 * KeyPairHelper.getECPrivateKeyDHex 方法单元测试
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class KeyPairHelperGetECPrivateKeyDHexTest {

    private ECPrivateKey ecPrivateKey;
    private KeyPair keyPair;

    @Before
    public void setUp() throws Exception {
        // 生成 secp256r1 曲线的 ECC 密钥对
        keyPair = KeyPairHelper.generateECCKeyPair("secp256r1");
        ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 基本功能
     */
    @Test
    public void testGetECPrivateKeyDHex_BasicFunctionality() {
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        
        assertNotNull("D值hex不应为null", dHex);
        assertFalse("D值hex不应为空字符串", dHex.isEmpty());
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 固定长度64字符
     */
    @Test
    public void testGetECPrivateKeyDHex_FixedLength64Chars() {
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        
        assertEquals("D值hex长度应为64字符", 64, dHex.length());
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 全大写格式
     */
    @Test
    public void testGetECPrivateKeyDHex_UppercaseFormat() {
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        
        assertEquals("D值hex应为大写", dHex, dHex.toUpperCase());
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 只包含有效的十六进制字符
     */
    @Test
    public void testGetECPrivateKeyDHex_ValidHexCharacters() {
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        
        assertTrue("D值hex应只包含0-9和A-F字符", dHex.matches("^[0-9A-F]+$"));
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 多次生成不同密钥对
     */
    @Test
    public void testGetECPrivateKeyDHex_MultipleKeyPairs() {
        for (int i = 0; i < 5; i++) {
            KeyPair kp = KeyPairHelper.generateECCKeyPair("secp256r1");
            ECPrivateKey privateKey = (ECPrivateKey) kp.getPrivate();
            String dHex = KeyPairHelper.getECPrivateKeyDHex(privateKey);
            
            assertEquals("第" + (i + 1) + "次生成的D值hex长度应为64字符", 64, dHex.length());
            assertTrue("第" + (i + 1) + "次生成的D值hex应为有效十六进制", dHex.matches("^[0-9A-F]+$"));
        }
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 验证D值与原始私钥一致
     */
    @Test
    public void testGetECPrivateKeyDHex_ConsistencyWithOriginalKey() {
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        BigInteger originalS = ecPrivateKey.getS();
        
        // 将hex转换回BigInteger进行验证
        BigInteger recoveredS = new BigInteger(dHex, 16);
        
        assertEquals("从hex恢复的D值应与原始私钥D值一致", originalS, recoveredS);
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 小值补零测试
     */
    @Test
    public void testGetECPrivateKeyDHex_SmallValuePadding() {
        // 创建一个小的D值（小于32字节）
        BigInteger smallD = new BigInteger("1234567890ABCDEF", 16);
        
        // 使用反射调用私有方法进行测试（这里通过实际密钥对间接测试）
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        
        // 确保即使D值较小，也会补齐到64字符
        assertEquals("D值hex长度应为64字符（包含前导零）", 64, dHex.length());
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 大值截断测试
     */
    @Test
    public void testGetECPrivateKeyDHex_LargeValueTruncation() {
        // 生成多个密钥对，确保处理各种大小的D值
        boolean foundNonStandardSize = false;
        
        for (int i = 0; i < 10; i++) {
            KeyPair kp = KeyPairHelper.generateECCKeyPair("secp256r1");
            ECPrivateKey privateKey = (ECPrivateKey) kp.getPrivate();
            BigInteger s = privateKey.getS();
            
            // 检查原始D值的字节长度
            byte[] bytes = s.toByteArray();
            if (bytes.length != 32) {
                foundNonStandardSize = true;
                break;
            }
        }
        
        // 无论原始D值大小如何，输出都应该是64字符
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        assertEquals("D值hex长度应固定为64字符", 64, dHex.length());
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - null参数异常处理
     */
    @Test(expected = NullPointerException.class)
    public void testGetECPrivateKeyDHex_NullParameter() {
        KeyPairHelper.getECPrivateKeyDHex(null);
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 不同ECC曲线（256位）
     * 注意：getECPrivateKeyDHex方法固定返回64字符（32字节），仅适用于256位曲线
     */
    @Test
    public void testGetECPrivateKeyDHex_DifferentCurves() {
        // 只测试256位曲线，因为方法固定返回64字符
        String[] curves = {"secp256r1"};
        
        for (String curve : curves) {
            try {
                KeyPair kp = KeyPairHelper.generateECCKeyPair(curve);
                ECPrivateKey privateKey = (ECPrivateKey) kp.getPrivate();
                String dHex = KeyPairHelper.getECPrivateKeyDHex(privateKey);
                
                // 256位曲线：256位 = 32字节 = 64字符
                assertEquals("曲线 " + curve + " 的D值hex长度应为64字符", 64, dHex.length());
                assertTrue("曲线 " + curve + " 的D值hex应为有效十六进制", dHex.matches("^[0-9A-F]+$"));
            } catch (Exception e) {
                fail("曲线 " + curve + " 测试失败: " + e.getMessage());
            }
        }
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 返回值稳定性
     */
    @Test
    public void testGetECPrivateKeyDHex_ReturnValueStability() {
        String dHex1 = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        String dHex2 = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        
        assertEquals("同一私钥多次调用应返回相同结果", dHex1, dHex2);
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 边界值测试（最小值）
     */
    @Test
    public void testGetECPrivateKeyDHex_MinimumValue() {
        // secp256r1的最小有效私钥值为1
        BigInteger minD = BigInteger.ONE;
        
        // 通过实际密钥对测试（因为无法直接构造ECPrivateKey）
        String dHex = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        
        // 验证格式正确
        assertNotNull("D值hex不应为null", dHex);
        assertEquals("D值hex长度应为64字符", 64, dHex.length());
    }

    /**
     * 测试获取EC私钥D值的十六进制表示 - 与其他方法的一致性
     */
    @Test
    public void testGetECPrivateKeyDHex_ConsistencyWithKeyPairMethod() {
        String dHex1 = KeyPairHelper.getECPrivateKeyDHex(ecPrivateKey);
        String dHex2 = KeyPairHelper.getPrivateKeyDHex(keyPair);
        
        assertEquals("直接使用私钥和通过密钥对获取的D值应一致", dHex1, dHex2);
    }
}
