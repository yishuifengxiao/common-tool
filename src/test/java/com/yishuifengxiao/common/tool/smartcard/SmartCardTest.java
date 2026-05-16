package com.yishuifengxiao.common.tool.smartcard;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.Hex;
import org.junit.Before;
import org.junit.Test;

import javax.smartcardio.CommandAPDU;
import java.util.List;

import static org.junit.Assert.*;

/**
 * SmartCard单元测试类
 * 主要测试不依赖智能卡硬件的核心逻辑功能
 */
public class SmartCardTest {

    private SmartCard smartCard;

    @Before
    public void setUp() {
        smartCard = new SmartCard();
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - 有效输入
     */
    @Test
    public void testConvertToCommandApdu_ValidHex() {
        String hexCommand = "00A4040010A0000005591010FFFFFFFF8900000100";

        CommandAPDU result = smartCard.convertToCommandApdu(hexCommand);

        assertNotNull(result);
        assertEquals(hexCommand.length() / 2, result.getBytes().length);
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - 带空格
     */
    @Test
    public void testConvertToCommandApdu_WithSpaces() {
        String hexCommand = "00 A4 04 00 10 A0 00 00 05 59 10 10 FF FF FF FF 89 00 00 01 00";

        CommandAPDU result = smartCard.convertToCommandApdu(hexCommand);

        assertNotNull(result);
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - 非十六进制数据
     */
    @Test(expected = UncheckedException.class)
    public void testConvertToCommandApdu_NonHex() {
        smartCard.convertToCommandApdu("GGHHIJKL");
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - 奇数长度
     */
    @Test(expected = UncheckedException.class)
    public void testConvertToCommandApdu_OddLength() {
        smartCard.convertToCommandApdu("00A404001");
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - null输入
     */
    @Test(expected = UncheckedException.class)
    public void testConvertToCommandApdu_NullInput() {
        smartCard.convertToCommandApdu(null);
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - 空字符串
     */
    @Test(expected = UncheckedException.class)
    public void testConvertToCommandApdu_EmptyString() {
        smartCard.convertToCommandApdu("");
    }

    /**
     * 测试分割命令 - 短命令不分包
     */
    @Test
    public void testSplitCommand_ShortCommand() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        String shortCommand = "00A4040010";
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, shortCommand);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(shortCommand, result.get(0));
    }

    /**
     * 测试分割命令 - 长命令分包
     * 注意：splitCommand使用十六进制字符串长度与MAX_CHUNK_SIZE(510)比较
     * 因此超过510个十六进制字符（255字节）就会分包
     */
    @Test
    public void testSplitCommand_LongCommand() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        // 构造一个超过510个十六进制字符的命令（600个十六进制字符 = 300字节）
        StringBuilder longCommand = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            longCommand.append("FF");
        }

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, longCommand.toString());

        assertNotNull(result);
        assertTrue(result.size() > 1);
        // 验证每个分包的长度不超过限制（510个十六进制字符）
        for (String chunk : result) {
            assertTrue("分包长度不应超过510个十六进制字符", 
                    chunk.length() <= 510);
        }
    }

    /**
     * 测试分割命令 - 空命令
     */
    @Test
    public void testSplitCommand_EmptyCommand() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, "");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    /**
     * 测试分割命令 - null命令
     */
    @Test
    public void testSplitCommand_NullCommand() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, (String) null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.get(0));
    }

    /**
     * 测试分割命令 - 正好等于最大长度的命令
     * 注意：MAX_CHUNK_SIZE=510是字节数，对应1020个十六进制字符
     * splitCommand方法中使用的是十六进制字符串长度与MAX_CHUNK_SIZE比较
     * 因此这里应该构造长度为510的十六进制字符串（255字节）
     */
    @Test
    public void testSplitCommand_ExactlyMaxSize() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        // 构造正好510个十六进制字符的命令（255字节）
        StringBuilder command = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            command.append("FF");
        }

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, command.toString());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(command.toString(), result.get(0));
    }

    /**
     * 测试分割命令 - 超过最大长度一个字符的命令
     * 注意：splitCommand使用十六进制字符串长度与MAX_CHUNK_SIZE(510)比较
     */
    @Test
    public void testSplitCommand_OneByteOverMaxSize() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        // 构造511个十六进制字符的命令（255.5字节，实际会按字符串处理）
        StringBuilder command = new StringBuilder();
        for (int i = 0; i < 255; i++) {
            command.append("FF");
        }
        command.append("0"); // 再加一个字符，总共511个十六进制字符

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, command.toString());

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(510, result.get(0).length()); // 第一个分包应该是510个十六进制字符
        assertEquals(1, result.get(1).length()); // 第二个分包应该是1个十六进制字符
    }

    /**
     * 测试ApduResult的isSuccess方法 - SW1=0x90成功
     */
    @Test
    public void testApduResult_IsSuccess_SW1_90() {
        SmartCard.ApduResult result = new SmartCard.ApduResult();
        result.setSw1(0x90);
        result.setSw2(0x00);
        assertTrue("SW1=0x90应该被认为是成功的", result.isSuccess());
    }

    /**
     * 测试ApduResult的isSuccess方法 - SW1=0x61需要更多数据
     */
    @Test
    public void testApduResult_IsSuccess_SW1_61() {
        SmartCard.ApduResult result = new SmartCard.ApduResult();
        result.setSw1(0x61);
        result.setSw2(0x05);
        assertTrue("SW1=0x61应该被认为是成功的（需要继续拉取）", result.isSuccess());
    }

    /**
     * 测试ApduResult的isSuccess方法 - SW1=0x6A失败
     */
    @Test
    public void testApduResult_IsSuccess_SW1_6A() {
        SmartCard.ApduResult result = new SmartCard.ApduResult();
        result.setSw1(0x6A);
        result.setSw2(0x82);
        assertFalse("SW1=0x6A不应该被认为是成功的", result.isSuccess());
    }

    /**
     * 测试ApduResult的isSuccess方法 - SW1=0x6F失败
     */
    @Test
    public void testApduResult_IsSuccess_SW1_6F() {
        SmartCard.ApduResult result = new SmartCard.ApduResult();
        result.setSw1(0x6F);
        result.setSw2(0x00);
        assertFalse("SW1=0x6F不应该被认为是成功的", result.isSuccess());
    }

    /**
     * 测试ApduResult的链式调用
     */
    @Test
    public void testApduResult_ChainCall() {
        SmartCard.ExecuteRecord record = new SmartCard.ExecuteRecord("00A4040010", "9000");

        SmartCard.ApduResult result = new SmartCard.ApduResult()
                .setData("ABCD")
                .setSw1(0x90)
                .setSw2(0x00)
                .addRecord(record);

        assertEquals("ABCD", result.getData());
        assertEquals(0x90, result.getSw1());
        assertEquals(0x00, result.getSw2());
        assertEquals(1, result.getRecords().size());
    }

    /**
     * 测试ApduResult批量添加记录
     */
    @Test
    public void testApduResult_AddRecords() {
        SmartCard.ExecuteRecord record1 = new SmartCard.ExecuteRecord("00A4040010", "9000");
        SmartCard.ExecuteRecord record2 = new SmartCard.ExecuteRecord("00B0000010", "6A82");

        SmartCard.ApduResult result = new SmartCard.ApduResult()
                .addRecords(java.util.Arrays.asList(record1, record2));

        assertEquals(2, result.getRecords().size());
    }

    /**
     * 测试ExecuteRecord的构造函数和getter/setter
     */
    @Test
    public void testExecuteRecord() {
        SmartCard.ExecuteRecord record = new SmartCard.ExecuteRecord("00A4040010", "9000");

        assertEquals("00A4040010", record.getRequest());
        assertEquals("9000", record.getResponse());

        // 测试setter
        record.setRequest("00B0000010");
        record.setResponse("6A82");
        assertEquals("00B0000010", record.getRequest());
        assertEquals("6A82", record.getResponse());
    }

    /**
     * 测试ExecuteRecord的链式调用
     */
    @Test
    public void testExecuteRecord_ChainCall() {
        SmartCard.ExecuteRecord record = new SmartCard.ExecuteRecord()
                .setRequest("00A4040010")
                .setResponse("9000");

        assertEquals("00A4040010", record.getRequest());
        assertEquals("9000", record.getResponse());
    }

    /**
     * 测试ApduResult默认初始化records列表
     */
    @Test
    public void testApduResult_DefaultRecords() {
        SmartCard.ApduResult result = new SmartCard.ApduResult();

        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().isEmpty());
    }

    /**
     * 测试未连接时获取读卡器名称返回空列表
     */
    @Test
    public void testGetCardTerminalNames_NoConnection() {
        // 在没有真实读卡器的环境中，应该返回空列表而不是抛出异常
        java.util.List<String> names = smartCard.getCardTerminalNames();

        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    /**
     * 测试未连接时根据状态过滤获取读卡器返回空列表
     */
    @Test
    public void testGetCardTerminals_NoConnection() {
        // 在没有真实读卡器的环境中，应该返回空列表
        java.util.List<String> names = smartCard.getCardTerminals(
                javax.smartcardio.CardTerminals.State.CARD_INSERTION);

        assertNotNull(names);
        assertTrue(names.isEmpty());
    }

    /**
     * 测试未连接时尝试断开连接不会抛出异常
     */
    @Test
    public void testDisconnect_NotConnected() {
        SmartCard result = smartCard.disconnect();

        assertNotNull(result);
        assertSame(smartCard, result);
    }

    /**
     * 测试尝试连接不存在的终端抛出异常
     */
    @Test(expected = UncheckedException.class)
    public void testConnect_TerminalNotFound() {
        // 在没有真实读卡器的环境中，尝试连接不存在的终端会抛出异常
        smartCard.connect("NonExistentReader");
    }

    /**
     * 测试未连接时打开逻辑通道抛出异常
     */
    @Test(expected = UncheckedException.class)
    public void testOpenLogicalChannel_NotConnected() {
        smartCard.openLogicalChannel();
    }

    /**
     * 测试未连接时在新逻辑通道上执行命令抛出异常
     */
    @Test(expected = UncheckedException.class)
    public void testTransmitWithNewLogicalChannel_NotConnected() {
        smartCard.transmitWithNewLogicalChannel("00A4040010");
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - 小写十六进制
     */
    @Test
    public void testConvertToCommandApdu_LowercaseHex() {
        String hexCommand = "00a4040010a0000005591010ffffffff8900000100";

        CommandAPDU result = smartCard.convertToCommandApdu(hexCommand);

        assertNotNull(result);
    }

    /**
     * 测试转换十六进制命令为CommandAPDU - 混合大小写
     */
    @Test
    public void testConvertToCommandApdu_MixedCaseHex() {
        String hexCommand = "00A4040010a0000005591010fFfFfFfF8900000100";

        CommandAPDU result = smartCard.convertToCommandApdu(hexCommand);

        assertNotNull(result);
    }

    /**
     * 测试分割命令 - 非常长的命令（多个分包）
     * 注意：splitCommand使用十六进制字符串长度与MAX_CHUNK_SIZE(510)比较
     */
    @Test
    public void testSplitCommand_VeryLongCommand() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        // 构造2000个十六进制字符的命令（1000字节）
        StringBuilder longCommand = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longCommand.append("FF");
        }

        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, longCommand.toString());

        assertNotNull(result);
        assertTrue("应该分成多个分包", result.size() > 3);
        
        // 验证所有分包拼接后等于原命令
        StringBuilder reconstructed = new StringBuilder();
        for (String chunk : result) {
            reconstructed.append(chunk);
        }
        assertEquals(longCommand.toString(), reconstructed.toString());
    }

    /**
     * 测试ApduResult的swHex方法（通过反射）
     */
    @Test
    public void testApduResult_SwHex() throws Exception {
        SmartCard.ApduResult result = new SmartCard.ApduResult();
        result.setSw1(0x90);
        result.setSw2(0x00);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.ApduResult.class.getDeclaredMethod("swHex");
        method.setAccessible(true);
        String swHex = (String) method.invoke(result);

        assertEquals("9000", swHex);
    }

    /**
     * 测试ApduResult的swHex方法 - 不同状态字
     */
    @Test
    public void testApduResult_SwHex_DifferentStatus() throws Exception {
        SmartCard.ApduResult result = new SmartCard.ApduResult();
        result.setSw1(0x6A);
        result.setSw2(0x82);

        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.ApduResult.class.getDeclaredMethod("swHex");
        method.setAccessible(true);
        String swHex = (String) method.invoke(result);

        assertEquals("6A82", swHex);
    }

    /**
     * 测试分割命令边界情况 - 长度为1的命令
     */
    @Test
    public void testSplitCommand_SingleByte() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        String command = "FF"; // 1字节
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, command);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("FF", result.get(0));
    }

    /**
     * 测试分割命令边界情况 - 长度为2的命令
     */
    @Test
    public void testSplitCommand_TwoBytes() throws Exception {
        // 使用反射调用私有方法
        java.lang.reflect.Method method = SmartCard.class.getDeclaredMethod("splitCommand", String.class);
        method.setAccessible(true);

        String command = "FFFF"; // 2字节
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) method.invoke(smartCard, command);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("FFFF", result.get(0));
    }

    /**
     * 测试ApduResult序列化兼容性
     */
    @Test
    public void testApduResult_Serializable() {
        SmartCard.ApduResult result = new SmartCard.ApduResult();
        result.setData("ABCD");
        result.setSw1(0x90);
        result.setSw2(0x00);

        // 验证可以实现Serializable接口
        assertTrue(result instanceof java.io.Serializable);
    }

    /**
     * 测试ExecuteRecord序列化兼容性
     */
    @Test
    public void testExecuteRecord_Serializable() {
        SmartCard.ExecuteRecord record = new SmartCard.ExecuteRecord("00A4040010", "9000");

        // 验证可以实现Serializable接口
        assertTrue(record instanceof java.io.Serializable);
    }
}
