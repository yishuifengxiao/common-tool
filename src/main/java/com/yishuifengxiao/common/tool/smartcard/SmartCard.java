package com.yishuifengxiao.common.tool.smartcard;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.Hex;
import com.yishuifengxiao.common.tool.lang.TLVUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.smartcardio.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 智能卡操作工具类
 * <p>
 * 提供智能卡的连接管理、APDU命令传输、逻辑通道管理等核心功能。
 * 支持自动拉取响应数据（SW1=0x61时）、分包传输等高级特性。
 * </p>
 *
 * @author yishui
 * @version v1.0.0
 * @date 2026/5/6
 */
@Slf4j
public class SmartCard {

    private static final String COMMAND_EID = "81E2910006BF3E035C015A";
    private static final String COMMAND_SELECT_ISR = "01A4040010A0000005591010FFFFFFFF8900000100";
    /**
     * APDU命令分包最大长度（字节数）
     */
    private static final int MAX_CHUNK_SIZE = 510;

    /**
     * 智能卡连接对象
     */
    private Card card;

    /**
     * 当前活动的逻辑通道
     */
    private CardChannel cardChannel;

    /**
     * 获取所有可用的智能卡终端列表
     *
     * @return 智能卡终端管理器
     */
    private synchronized CardTerminals getCardTerminals() {
        TerminalFactory factory = TerminalFactory.getDefault();
        return factory.terminals();
    }

    /**
     * 获取所有已连接的智能卡读卡器名称列表
     *
     * @return 读卡器名称列表
     */
    public synchronized List<String> getCardTerminalNames() {
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            return factory.terminals().list().stream().map(CardTerminal::getName).toList();
        } catch (Exception e) {
            log.warn("获取所有可访问的读卡器失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 根据状态过滤获取智能卡读卡器名称列表
     *
     * @param state 终端状态过滤器，如果为null则返回所有读卡器
     * @return 符合条件的读卡器名称列表
     */
    public List<String> getCardTerminals(CardTerminals.State state) {
        try {
            if (null == state) {
                return getCardTerminalNames();
            }
            return this.getCardTerminals().list(state).stream().map(CardTerminal::getName).toList();
        } catch (Exception e) {
            log.warn("获取所有可访问的读卡器失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 连接到指定的智能卡读卡器
     *
     * @param terminalName 读卡器名称
     * @return 当前SmartCard实例，支持链式调用
     * @throws CardException 当读卡器不存在或连接失败时抛出
     */
    public synchronized SmartCard connect(String terminalName) throws CardException {
        CardTerminal terminal = this.getCardTerminals().getTerminal(terminalName);
        if (null == terminal) {
            this.card = null;
            throw new UncheckedException("未找到名称为" + terminalName + "的终端");
        }
        if (null == this.card) {
            this.card = terminal.connect("*");
        }
        return this;
    }

    /**
     * 断开与智能卡的连接
     *
     * @return 当前SmartCard实例，支持链式调用
     * @throws CardException 当断开连接失败时抛出
     */
    public synchronized SmartCard disconnect() throws CardException {
        if (null != this.card) {
            this.card.disconnect(true);
            this.card = null;
        }
        return this;
    }

    /**
     * 打开一个新的逻辑通道
     * <p>
     * 如果当前没有活动的逻辑通道且卡片已连接，则创建新的逻辑通道。
     * 如果已存在逻辑通道，则不执行任何操作。
     * </p>
     *
     * @return 当前SmartCard实例，支持链式调用
     * @throws CardException 当打开逻辑通道失败时抛出
     */
    public synchronized SmartCard openLogicalChannel() throws CardException {
        if (null == this.card) {
            throw new UncheckedException("请先连接智能卡");
        }
        if (null == this.cardChannel) {
            this.cardChannel = this.card.openLogicalChannel();
        }
        return this;
    }

    private CardChannel cardChannel() throws CardException {
        if (null == this.cardChannel) {
            this.cardChannel = this.card.openLogicalChannel();
        }
        return this.cardChannel;
    }

    public SmartCard selectIsr() throws CardException {
        CommandAPDU commandAPDU = this.convertToCommandApdu(COMMAND_SELECT_ISR);
        this.cardChannel().transmit(commandAPDU);
        return this;
    }

    /**
     * 在当前活动的逻辑通道上发送APDU命令（默认启用自动拉取）
     *
     * @param hexCommand 十六进制格式的APDU命令字符串
     * @return APDU命令执行结果
     * @throws CardException 当命令传输失败时抛出
     */
    public synchronized ApduResult transmit(String hexCommand) throws CardException {
        return this.transmit(this.cardChannel(), hexCommand, true);
    }


    /**
     * 在新的逻辑通道上发送APDU命令（默认启用自动拉取）
     * <p>
     * 该方法会临时打开一个新的逻辑通道，在该通道上执行以下操作：
     * 1. 打开新的逻辑通道
     * 2. 选择ISR应用
     * 3. 传输APDU命令并启用自动拉取功能
     * 4. 关闭逻辑通道（在finally块中确保资源释放）
     * </p>
     * <p>
     * 适用于需要在独立通道上执行单次命令的场景，避免与当前活动通道产生干扰。
     * </p>
     *
     * @param hexCommand 十六进制格式的APDU命令字符串
     * @return APDU命令执行结果，包含响应数据和状态字信息
     * @throws CardException 当打开逻辑通道、选择ISR或命令传输失败时抛出
     */
    public synchronized ApduResult transmitWithNewLogicalChannel(String hexCommand) throws CardException {
        CardChannel channel = null;
        ApduResult result = null;
        try {
            channel = this.card.openLogicalChannel();
            channel.transmit(this.convertToCommandApdu(COMMAND_SELECT_ISR));
            result = this.transmit(channel, hexCommand, true);
        } finally {
            if (null != channel) {
                channel.close();
            }
        }
        return result;
    }


    /**
     * 将十六进制命令字符串转换为CommandAPDU对象
     *
     * @param hexCommand 十六进制格式的命令字符串
     * @return CommandAPDU对象
     */
    private CommandAPDU convertToCommandApdu(String hexCommand) {
        hexCommand = hexCommand.replaceAll("\\s+", "").trim();
        if (!Hex.isHex(hexCommand)) {
            throw new UncheckedException(String.format("输入的数据%s不是十六进制数据", hexCommand));
        }
        byte[] commandBytes = Hex.hexToBytes(hexCommand);
        return new CommandAPDU(commandBytes);
    }

    /**
     * 在指定的逻辑通道上发送APDU命令
     *
     * @param channel    目标逻辑通道
     * @param hexCommand 十六进制格式的APDU命令字符串
     * @param autoPull   是否启用自动拉取功能。当SW1=0x61时，自动发送GET RESPONSE命令获取剩余数据
     * @return APDU命令执行结果
     * @throws CardException 当命令传输失败时抛出
     */
    public synchronized ApduResult transmit(CardChannel channel, String hexCommand, boolean autoPull) throws CardException {
        this.validate();
        if (!autoPull) {
            return transmitWithoutAutoPull(channel, hexCommand);
        } else {
            ApduResult result = new ApduResult();
            return transmitWithAutoPull(result, channel, hexCommand);
        }
    }

    /**
     * 发送APDU命令但不启用自动拉取功能
     * <p>
     * 仅执行一次命令传输，即使返回SW1=0x61也不会自动拉取剩余数据。
     * </p>
     *
     * @param channel    目标逻辑通道
     * @param hexCommand 十六进制格式的APDU命令字符串
     * @return APDU命令执行结果，包含响应数据和状态字
     * @throws CardException 当命令传输失败时抛出
     */
    private ApduResult transmitWithoutAutoPull(CardChannel channel, String hexCommand) throws CardException {
        CommandAPDU commandApdu = this.convertToCommandApdu(hexCommand);
        ResponseAPDU responseApdu = channel.transmit(commandApdu);

        String responseData = Hex.bytesToHex(responseApdu.getData());
        String fullResponse = Hex.bytesToHex(responseApdu.getBytes());
        int sw1 = responseApdu.getSW1();
        int sw2 = responseApdu.getSW2();

        List<ExecuteRecord> records = new ArrayList<>();
        records.add(new ExecuteRecord(hexCommand, fullResponse));

        ApduResult result = new ApduResult().setData(responseData).setSw1(sw1).setSw2(sw2).setRecords(records);

        return result;
    }

    /**
     * 发送APDU命令并启用自动拉取功能
     * <p>
     * 当响应状态字SW1=0x61时，表示还有更多数据可用，会自动发送GET RESPONSE命令（01C00000XX）
     * 拉取剩余数据，直到SW1不为0x61为止。所有响应数据会拼接在一起返回。
     * </p>
     *
     * @param result     累积的结果对象，用于保存多次拉取的数据
     * @param channel    目标逻辑通道
     * @param hexCommand 十六进制格式的APDU命令字符串
     * @return APDU命令执行结果，包含完整的响应数据和最终状态字
     * @throws CardException 当命令传输失败时抛出
     */
    private ApduResult transmitWithAutoPull(ApduResult result, CardChannel channel, String hexCommand) throws CardException {
        StringBuilder responseData = new StringBuilder(result.getData() != null ? result.getData() : "");
        List<ExecuteRecord> records = new ArrayList<>(result.getRecords());

        CommandAPDU commandApdu = this.convertToCommandApdu(hexCommand);
        ResponseAPDU responseApdu = channel.transmit(commandApdu);

        String data = Hex.bytesToHex(responseApdu.getData());
        int sw1 = responseApdu.getSW1();
        int sw2 = responseApdu.getSW2();

        responseData.append(data);
        records.add(new ExecuteRecord(hexCommand, Hex.bytesToHex(responseApdu.getBytes())));

        if (sw1 != 0x61) {
            return result.setData(responseData.toString()).setSw1(sw1).setSw2(sw2).setRecords(records);
        } else {
            String getNextCommand = "01C00000" + Hex.numberToHexString(sw2);
            return transmitWithAutoPull(result, channel, getNextCommand);
        }
    }

    /**
     * 在新的逻辑通道上批量执行多个APDU命令（使用Supplier方式）
     * <p>
     * 为每个命令创建独立的逻辑通道，执行完成后自动关闭通道。
     * 适用于需要隔离执行的场景。
     * </p>
     *
     * @param suppliers 命令提供者数组，每个Supplier生成一个十六进制命令字符串
     * @return 每个命令的执行结果列表
     * @throws CardException 当命令传输失败时抛出
     */
    public synchronized List<ApduResult> transmitWithNewLogicalChannel(Supplier<String>... suppliers) throws CardException {
        List<String> commands = Arrays.stream(suppliers).map(Supplier::get).collect(Collectors.toList());
        return transmitWithNewLogicalChannel(commands);
    }

    /**
     * 在新的逻辑通道上发送81E2类型的请求命令
     * <p>
     * 自动创建临时逻辑通道，执行完成后关闭。专门用于处理81E2开头的特殊命令。
     * </p>
     *
     * @param hexCommand 十六进制格式的81E2命令字符串
     * @return APDU命令执行结果
     * @throws CardException 当命令传输失败时抛出
     */
    public synchronized ApduResult transmit81E2RequestWithNewLogicalChannel(String hexCommand) throws CardException {
        return this.transmit81E2RequestWithNewLogicalChannel(Arrays.asList(hexCommand)).get(0);
    }

    /**
     * 通过新逻辑通道传输81E2请求命令（Supplier版本）
     * <p>
     * 该方法接受Supplier函数式接口作为参数，支持延迟获取APDU命令。
     * 内部会通过Stream流处理将所有Supplier转换为实际的命令字符串列表，
     * 然后委托给主方法执行。适用于需要动态生成或延迟计算命令的场景。
     * </p>
     *
     * @param suppliers APDU命令的Supplier可变参数，每个Supplier在执行时返回一条十六进制格式的APDU命令字符串
     * @return APDU响应结果列表，按顺序包含每条命令的执行结果
     * @throws CardException 当卡片操作出现异常时抛出，例如通道打开失败、通信错误等
     */
    public synchronized List<ApduResult> transmit81E2RequestWithNewLogicalChannel(Supplier<String>... suppliers) throws CardException {
        // 将Supplier数组转换为命令字符串列表
        List<String> commands = Arrays.stream(suppliers).map(Supplier::get).collect(Collectors.toList());
        return this.transmit81E2RequestWithNewLogicalChannel(commands);
    }


    /**
     * 通过新逻辑通道传输81E2请求命令（可变参数版本）
     * <p>
     * 该方法是 {@link #transmit81E2RequestWithNewLogicalChannel(List)} 的便捷重载版本，
     * 支持使用可变参数的方式传入多个十六进制APDU命令。内部会将参数转换为List后委托给主方法执行。
     * </p>
     *
     * @param hexCommand 十六进制格式的APDU命令可变参数，每个参数代表一条完整的APDU命令字符串
     * @return APDU响应结果列表，按顺序包含每条命令的执行结果
     * @throws CardException 当卡片操作出现异常时抛出，例如通道打开失败、通信错误等
     */
    public synchronized List<ApduResult> transmit81E2RequestWithNewLogicalChannel(String... hexCommand) throws CardException {
        return this.transmit81E2RequestWithNewLogicalChannel(Arrays.asList(hexCommand));
    }


    /**
     * 通过新逻辑通道传输81E2请求命令
     * <p>
     * 该方法会打开一个新的逻辑通道，并在该通道上依次执行所有提供的APDU命令。
     * 每个命令都会按照81E2协议进行传输处理。方法执行完成后会自动关闭逻辑通道。
     * </p>
     *
     * @param hexCommand 十六进制格式的APDU命令列表，每个元素代表一条完整的APDU命令字符串
     * @return APDU响应结果列表，按顺序包含每条命令的执行结果
     * @throws CardException 当卡片操作出现异常时抛出，例如通道打开失败、通信错误等
     */
    public synchronized List<ApduResult> transmit81E2RequestWithNewLogicalChannel(List<String> hexCommand) throws CardException {
        List<ApduResult> results = new ArrayList<>();
        CardChannel channel = null;
        try {
            // 打开新的逻辑通道
            channel = this.card.openLogicalChannel();
            channel.transmit(this.convertToCommandApdu(COMMAND_SELECT_ISR));
            // 在逻辑通道上依次执行所有APDU命令
            for (String command : hexCommand) {
                results.add(transmit81E2Request(channel, command));
            }
        } finally {
            // 确保逻辑通道被正确关闭
            if (null != channel) {
                channel.close();
            }
        }
        return results;
    }


    /**
     * 在指定逻辑通道上发送81E2类型的请求命令（带自动分包功能）
     * <p>
     * 当命令数据超过MAX_CHUNK_SIZE（510字节）时，会自动将命令拆分为多个数据包，
     * 按顺序发送并在接收端拼接响应数据。每个分包都会添加81E2前缀和序列号。
     * </p>
     *
     * @param channel    目标逻辑通道
     * @param hexCommand 十六进制格式的81E2命令字符串（不含CLA头部的完整数据部分）
     * @return APDU命令执行结果，包含所有分包的拼接数据
     * @throws CardException 当命令传输失败时抛出
     */
    public synchronized ApduResult transmit81E2Request(CardChannel channel, String hexCommand) throws CardException {
        ApduResult result = new ApduResult();
        List<ExecuteRecord> records = new ArrayList<>();
        StringBuilder responseData = new StringBuilder();

        List<String> chunks = splitCommand(hexCommand);
        for (int i = 0; i < chunks.size(); i++) {
            String prefix = "81E291";
            String chunk = chunks.get(i);
            String command = prefix + Hex.numberToHexString(i) + Hex.numberToHexString(chunk.length() / 2) + chunk +
                    "00";

            ApduResult transmitResult = this.transmit(channel, command, true);
            records.addAll(transmitResult.getRecords());
            result.setSw1(transmitResult.getSw1());
            result.setSw2(transmitResult.getSw2());
            responseData.append(transmitResult.getData());

            if (!transmitResult.isSuccess()) {
                break;
            }
        }

        return result.setData(responseData.toString()).setRecords(records);
    }

    /**
     * 将长命令字符串按固定长度分割为多个数据包
     * <p>
     * 用于81E2命令的分包传输，每个数据包的最大长度为MAX_CHUNK_SIZE（510字节）。
     * </p>
     *
     * @param hexCommand 十六进制格式的长命令字符串
     * @return 分割后的命令片段列表
     */
    private List<String> splitCommand(String hexCommand) {
        if (hexCommand.length() <= MAX_CHUNK_SIZE) {
            return Collections.singletonList(hexCommand);
        }
        List<String> chunks = new ArrayList<>();
        int startPos = 0;
        int endPos = MAX_CHUNK_SIZE;

        while (endPos < hexCommand.length()) {
            String chunk = StringUtils.substring(hexCommand, startPos, endPos);
            startPos = endPos;
            endPos = startPos + MAX_CHUNK_SIZE;
            chunks.add(chunk);
        }

        return chunks;
    }

    /**
     * 在新的逻辑通道上批量执行多个APDU命令（可变参数版本）
     * <p>
     * 为每个命令创建独立的逻辑通道，执行完成后自动关闭通道。
     * </p>
     *
     * @param hexCommands 十六进制格式的命令字符串数组
     * @return 每个命令的执行结果列表
     * @throws CardException 当命令传输失败时抛出
     */
    public synchronized List<ApduResult> transmitWithNewLogicalChannel(String... hexCommands) throws CardException {
        return transmitWithNewLogicalChannel(Arrays.asList(hexCommands));
    }

    /**
     * 在新的逻辑通道上批量执行多个APDU命令（列表版本）
     * <p>
     * 为每个命令创建独立的逻辑通道，执行完成后自动关闭通道。
     * 适用于需要在隔离环境中执行多个命令的场景。
     * </p>
     *
     * @param hexCommands 十六进制格式的命令字符串列表
     * @return 每个命令的执行结果列表
     * @throws CardException 当命令传输失败时抛出
     */
    public synchronized List<ApduResult> transmitWithNewLogicalChannel(List<String> hexCommands) throws CardException {
        List<ApduResult> results = new ArrayList<>();
        CardChannel channel = null;
        try {
            channel = this.card.openLogicalChannel();
            for (String hexCommand : hexCommands) {
                ApduResult result = this.transmit(channel, hexCommand, true);
                results.add(result);
            }
        } finally {
            if (null != channel) {
                channel.close();
            }
        }
        return results;
    }


    /**
     * 验证智能卡连接状态和逻辑通道可用性
     * <p>
     * 该方法在执行APDU命令前进行前置检查：
     * 1. 检查是否已连接到智能卡，如果未连接则抛出异常
     * 2. 检查是否存在活动的逻辑通道，如果不存在则自动创建新的逻辑通道
     * </p>
     *
     * @throws CardException 当智能卡未连接或打开逻辑通道失败时抛出
     */
    private void validate() throws CardException {
        if (null == this.card) {
            throw new UncheckedException("请先连接智能卡");
        }
        if (null == this.cardChannel) {
            this.openLogicalChannel();
        }
    }


    /**
     * 获取智能卡的eID（嵌入式SIM标识符）
     * <p>
     * 该方法会在新的逻辑通道上发送EID查询命令，并从响应数据中提取BF38标签对应的值。
     * eID是eUICC设备的唯一标识符，用于识别和管理嵌入式SIM卡。
     * </p>
     *
     * @return eID字符串值，如果提取失败可能返回null或空字符串
     * @throws CardException 当卡片连接、命令传输或数据解析失败时抛出
     */
    public synchronized String getEid() throws CardException {
        ApduResult transmit = this.transmitWithNewLogicalChannel(COMMAND_EID);
        return TLVUtil.extractValsRecursive(transmit.getData(), "BF3E", "5A").getVal("5A");
    }


    /**
     * APDU命令执行结果
     * <p>
     * 包含命令执行的完整信息：响应数据、状态字、执行记录等。
     * 支持链式调用设置属性。
     * </p>
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ApduResult implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 命令执行历史记录列表
         */
        private List<ExecuteRecord> records = new ArrayList<>();

        /**
         * 响应数据（十六进制字符串）
         */
        private String data;

        /**
         * 状态字SW1
         */
        private int sw1;

        /**
         * 状态字SW2
         */
        private int sw2;

        /**
         * 添加单条执行记录
         *
         * @param record 执行记录对象
         * @return 当前ApduResult实例，支持链式调用
         */
        public ApduResult addRecord(ExecuteRecord record) {
            this.records.add(record);
            return this;
        }

        /**
         * 批量添加执行记录
         *
         * @param records 执行记录列表
         * @return 当前ApduResult实例，支持链式调用
         */
        public ApduResult addRecords(List<ExecuteRecord> records) {
            this.records.addAll(records);
            return this;
        }

        /**
         * 判断命令执行是否成功
         * <p>
         * SW1=0x90表示正常结束，SW1=0x61表示还有更多数据（需要进一步拉取）
         * </p>
         *
         * @return true表示成功或需要继续拉取，false表示执行失败
         */
        public boolean isSuccess() {
            return this.sw1 == 0x90 || this.sw1 == 0x61;
        }
    }

    /**
     * APDU命令执行记录
     * <p>
     * 记录单次APDU命令的请求和响应信息，用于审计和调试。
     * </p>
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ExecuteRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 请求命令（十六进制字符串）
         */
        private String request;

        /**
         * 响应数据（十六进制字符串，包含状态字）
         */
        private String response;
    }
}
