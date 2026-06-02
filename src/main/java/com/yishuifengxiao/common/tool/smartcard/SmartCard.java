package com.yishuifengxiao.common.tool.smartcard;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.Hex;
import com.yishuifengxiao.common.tool.lang.TLV;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.smartcardio.*;
import java.io.Serializable;
import java.util.*;
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
     * 最大自动拉取次数，防止无限递归
     */
    private static final int MAX_AUTO_PULL_COUNT = 100;

    /**
     * 成功状态字SW1
     */
    private static final int SW1_SUCCESS = 0x90;

    /**
     * 需要拉取更多数据的状态字SW1
     */
    private static final int SW1_MORE_DATA = 0x61;

    /**
     * GET RESPONSE命令前缀
     */
    private static final String GET_RESPONSE_PREFIX = "01C00000";

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
        }
        return Collections.emptyList();
    }

    /**
     * 连接到指定的智能卡读卡器
     *
     * @param terminalName 读卡器名称
     * @return 当前SmartCard实例，支持链式调用
     */
    public synchronized SmartCard connect(String terminalName) {
        CardTerminal terminal = this.getCardTerminals().getTerminal(terminalName);
        if (null == terminal) {
            this.card = null;
            throw new UncheckedException("未找到名称为" + terminalName + "的终端");
        }
        if (null == this.card) {
            try {
                this.card = terminal.connect("*");
            } catch (CardException e) {
                throw new UncheckedException("建立与卡片的连接失败", e);
            }
            log.debug("成功连接到读卡器: {}", terminalName);
        }
        return this;
    }

    /**
     * 断开与智能卡的连接
     *
     * @return 当前SmartCard实例，支持链式调用
     */
    public synchronized SmartCard disconnect() {
        if (null != this.card) {
            try {
                this.card.disconnect(true);
                log.debug("已断开智能卡连接");
            } catch (CardException e) {
                throw new UncheckedException("断开与该卡的连接失败", e);
            } finally {
                this.card = null;
                this.cardChannel = null;
            }
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
     */
    public synchronized SmartCard openLogicalChannel() {
        if (null == this.card) {
            throw new UncheckedException("请先连接智能卡");
        }
        if (null == this.cardChannel) {
            this.cardChannel();
        }
        return this;
    }

    /**
     * 获取或自动创建逻辑通道
     * <p>
     * 该方法采用懒加载模式，如果当前没有活动的逻辑通道且卡片已连接，
     * 则自动打开一个新的逻辑通道。如果已存在逻辑通道，则直接返回。
     * </p>
     *
     * @return 当前活动的CardChannel对象，保证非null
     * @throws UncheckedException 当打开逻辑通道失败时抛出异常
     */
    private synchronized CardChannel cardChannel() {
        if (null == this.cardChannel) {
            try {
                this.cardChannel = this.card.openLogicalChannel();
            } catch (CardException e) {
                throw new UncheckedException(e);
            }
        }
        return this.cardChannel;
    }

    /**
     * 在当前逻辑通道上选择ISR应用
     * <p>
     * 该方法会将预定义的ISR选择命令（COMMAND_SELECT_ISR）转换为CommandAPDU对象，
     * 并在当前活动的逻辑通道上传输执行。如果当前没有活动的逻辑通道，会自动创建一个。
     * </p>
     *
     * @return 当前SmartCard实例，支持链式调用
     */
    public synchronized SmartCard selectIsr() {

        /*
         * 将预定义的ISR选择命令字符串转换为CommandAPDU对象
         */
        CommandAPDU commandAPDU = this.convertToCommandApdu(COMMAND_SELECT_ISR);
        try {

            /*
             * 获取或创建逻辑通道，并传输ISR选择命令以激活ISR应用
             */
            this.cardChannel().transmit(commandAPDU);
        } catch (CardException e) {

            /*
             * 卡片通信异常时抛出非检查型异常，中断执行流程
             */
            throw new UncheckedException(e);
        }
        return this;
    }

    /**
     * 在当前活动的逻辑通道上发送APDU命令
     * <p>
     * 该方法委托给三参数的transmit方法执行，支持自动拉取功能。
     * 当autoPull为true且响应SW1=0x61时，会自动发送GET RESPONSE命令获取剩余数据；
     * 当autoPull为false时，仅执行单次命令传输，不进行自动拉取。
     * </p>
     *
     * @param hexCommand 十六进制格式的APDU命令字符串
     * @param autoPull   是否启用自动拉取功能。当SW1=0x61时，自动发送GET RESPONSE命令获取剩余数据
     * @return APDU命令执行结果，包含响应数据和状态字信息
     */
    public synchronized ApduResult transmit(String hexCommand, boolean autoPull) {
        return this.transmit(this.cardChannel(), hexCommand, autoPull);
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
     */
    public synchronized ApduResult transmitWithNewLogicalChannel(String hexCommand) {
        if (null == this.card) {
            throw new UncheckedException("请先连接智能卡");
        }
        CardChannel channel = null;
        try {
            channel = this.card.openLogicalChannel();
            ApduResult selectResult = this.transmit(channel, COMMAND_SELECT_ISR, true);
            if (!selectResult.isSuccess()) {
                throw new UncheckedException("在81E2专用逻辑通道上执行命令失败:" + selectResult.getData() + ",sw:" + selectResult.swHex());
            }
            return this.transmit(channel, hexCommand, true);
        } catch (CardException e) {
            log.error("在新逻辑通道上执行命令失败: {}", hexCommand, e);
            throw new UncheckedException("在新逻辑通道上执行命令失败", e);
        } finally {
            closeChannelQuietly(channel);
        }
    }


    /**
     * 将十六进制命令字符串转换为CommandAPDU对象
     *
     * @param hexCommand 十六进制格式的命令字符串
     * @return CommandAPDU对象
     */
    public CommandAPDU convertToCommandApdu(String hexCommand) {
        try {
            hexCommand = hexCommand.replaceAll("\\s+", "").trim();
            if (!Hex.isHex(hexCommand)) {
                throw new UncheckedException(String.format("输入的数据%s不是十六进制数据", hexCommand));
            }
            if (hexCommand.length() % 2 != 0) {
                throw new UncheckedException(String.format("输入的十六进制数据%s长度不是偶数", hexCommand));
            }
            byte[] commandBytes = Hex.hexToBytes(hexCommand);
            return new CommandAPDU(commandBytes);
        } catch (UncheckedException e) {
            throw e;
        } catch (Exception e) {
            log.error("将Hex数据{}转换成CommandAPDU时出现问题", hexCommand, e);
            throw new UncheckedException("将Hex数据" + hexCommand + "转换成CommandAPDU时出现问题", e);
        }
    }

    /**
     * 在指定的逻辑通道上发送APDU命令
     *
     * @param channel    目标逻辑通道
     * @param hexCommand 十六进制格式的APDU命令字符串
     * @param autoPull   是否启用自动拉取功能。当SW1=0x61时，自动发送GET RESPONSE命令获取剩余数据
     * @return APDU命令执行结果
     */
    public synchronized ApduResult transmit(CardChannel channel, String hexCommand, boolean autoPull) {
        this.validate();
        if (!autoPull) {
            return transmitWithoutAutoPull(channel, hexCommand);
        } else {
            ApduResult result = new ApduResult();
            return transmitWithAutoPull(result, channel, hexCommand, 0);
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
     */
    private ApduResult transmitWithoutAutoPull(CardChannel channel, String hexCommand) {
        CommandAPDU commandApdu = this.convertToCommandApdu(hexCommand);
        ResponseAPDU responseApdu = null;

        try {
            responseApdu = channel.transmit(commandApdu);
        } catch (CardException e) {
            throw new UncheckedException("命令" + commandApdu + "传输失败", e);
        }

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
     * @param pullCount  当前拉取次数，用于防止无限循环
     * @return APDU命令执行结果，包含完整的响应数据和最终状态字
     */
    private ApduResult transmitWithAutoPull(ApduResult result, CardChannel channel, String hexCommand, int pullCount) {
        if (pullCount >= MAX_AUTO_PULL_COUNT) {
            throw new UncheckedException("自动拉取次数超过限制(" + MAX_AUTO_PULL_COUNT + ")，可能存在异常");
        }

        StringBuilder responseData = new StringBuilder(result.getData() != null ? result.getData() : "");
        List<ExecuteRecord> records = result.getRecords() != null ? result.getRecords() : new ArrayList<>();

        CommandAPDU commandApdu = this.convertToCommandApdu(hexCommand);
        ResponseAPDU responseApdu = null;

        try {
            responseApdu = channel.transmit(commandApdu);
        } catch (CardException e) {
            throw new UncheckedException("命令" + commandApdu + "传输失败", e);
        }

        String data = Hex.bytesToHex(responseApdu.getData());
        int sw1 = responseApdu.getSW1();
        int sw2 = responseApdu.getSW2();

        responseData.append(data);
        records.add(new ExecuteRecord(hexCommand, Hex.bytesToHex(responseApdu.getBytes())));

        if (sw1 != SW1_MORE_DATA) {
            log.debug("命令执行完成，SW1=0x{}, SW2=0x{}", Integer.toHexString(sw1), Integer.toHexString(sw2));
            return result.setData(responseData.toString()).setSw1(sw1).setSw2(sw2).setRecords(records);
        } else {
            log.debug("检测到SW1=0x61，执行第{}次自动拉取", pullCount + 1);
            String getNextCommand = GET_RESPONSE_PREFIX + Hex.numberToHexString(sw2);
            return transmitWithAutoPull(result, channel, getNextCommand, pullCount + 1);
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
     */
    public synchronized List<ApduResult> transmitWithNewLogicalChannel(Supplier<String>... suppliers) {
        List<String> commands =
                Arrays.stream(suppliers).filter(Objects::nonNull).map(Supplier::get).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        return transmitWithNewLogicalChannel(commands);
    }

    /**
     * 在新的逻辑通道上发送81E2类型的请求命令（单命令版本）
     * <p>
     * 该方法会自动创建临时逻辑通道，在该通道上执行ISR应用选择后，
     * 调用 {@link #transmit81E2Request(CardChannel, String)} 方法处理81E2命令。
     * 此方法专门用于处理以81E2开头的特殊命令，支持自动分包和响应数据拼接。
     * </p>
     * <p>
     * 执行流程：
     * 1. 打开新的逻辑通道
     * 2. 选择ISR应用（81E2协议的前置必要步骤）
     * 3. 执行81E2命令（自动处理分包和响应拼接）
     * 4. 关闭逻辑通道（在finally块中确保资源释放）
     * </p>
     *
     * @param hexCommand 十六进制格式的81E2命令字符串（不含CLA头部的完整数据部分）
     * @return APDU命令执行结果，包含响应数据、状态字和执行记录；如果执行失败则返回null
     */
    public synchronized ApduResult transmit81E2RequestWithNewLogicalChannel(String hexCommand) {
        ApduResult result = null;
        CardChannel channel = null;
        try {

            /*
             * 为81E2命令创建专用的临时逻辑通道，确保与当前活动通道隔离
             */
            channel = this.card.openLogicalChannel();

            /*
             * 在新通道上执行ISR应用选择命令，这是81E2协议通信的前置必要步骤
             */
            channel.transmit(this.convertToCommandApdu(COMMAND_SELECT_ISR));

            /*
             * 调用单命令版本的81E2传输方法，该方法会自动处理命令分包、
             * 添加81E2协议头部、依次执行分包并拼接响应数据
             */
            result = this.transmit81E2Request(channel, hexCommand);
        } catch (CardException e) {
            log.error("在81E2专用逻辑通道上执行命令失败", e);
            throw new UncheckedException("在81E2专用逻辑通道上执行命令失败", e);
        } finally {

            /*
             * 无论执行成功与否，都必须确保逻辑通道被正确关闭以释放资源
             */
            closeChannelQuietly(channel);
        }
        return result;
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
     */
    public synchronized List<ApduResult> transmit81E2RequestWithNewLogicalChannel(Supplier<String>... suppliers) {
        // 将Supplier数组转换为命令字符串列表
        List<String> commands =
                Arrays.stream(suppliers).filter(Objects::nonNull).map(Supplier::get).filter(StringUtils::isNotBlank).collect(Collectors.toList());
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
     */
    public synchronized List<ApduResult> transmit81E2RequestWithNewLogicalChannel(String... hexCommand) {
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
     * @return APDU响应结果列表，按顺序包含每条命令的执行结果；如果执行结果为null则返回空列表
     */
    public synchronized List<ApduResult> transmit81E2RequestWithNewLogicalChannel(List<String> hexCommand) {
        List<ApduResult> results = new ArrayList<>();
        CardChannel channel = null;
        try {
            channel = this.card.openLogicalChannel();
            ApduResult selectResult = this.transmit(channel, COMMAND_SELECT_ISR, true);
            if (!selectResult.isSuccess()) {
                throw new UncheckedException("在81E2专用逻辑通道上执行命令失败:" + selectResult.getData() + ",sw:" + selectResult.swHex());
            }
            for (String cmd : hexCommand) {
                ApduResult apduResult = this.transmit81E2Request(channel, cmd);
                results.add(apduResult);
                if (!apduResult.isSuccess()) {
                    break;
                }
            }
        } catch (CardException e) {
            log.error("在81E2专用逻辑通道上执行命令失败", e);
            throw new UncheckedException("在81E2专用逻辑通道上执行命令失败", e);
        } finally {

            /*
             * 无论执行成功与否，都必须确保逻辑通道被正确关闭以释放资源
             */
            closeChannelQuietly(channel);
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
     */
    public synchronized ApduResult transmit81E2Request(CardChannel channel, String hexCommand) {
        ApduResult result = new ApduResult();
        List<ExecuteRecord> records = new ArrayList<>();
        StringBuilder responseData = new StringBuilder();

        List<String> chunks = splitCommand(hexCommand);
        for (int i = 0; i < chunks.size(); i++) {
            String prefix = (chunks.size() - 1 == i) ? "81E291" : "81E211";
            String chunk = chunks.get(i);
            String command = prefix + Hex.numberToHexString(i) + Hex.numberToHexString(chunk.length() / 2) + chunk +
                    "00";

            ApduResult transmitResult = this.transmit(channel, command, true);
            records.addAll(transmitResult.getRecords());
            result.setSw1(transmitResult.getSw1());
            result.setSw2(transmitResult.getSw2());
            responseData.append(transmitResult.getData());

            if (!transmitResult.isSuccess()) {
                log.warn("81E2命令{}第{}个分包{}执行命令{}失败，SW1=0x{}", hexCommand, i + 1, chunk, command,
                        Integer.toHexString(transmitResult.getSw1()).toUpperCase());
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
        if (hexCommand == null || hexCommand.isEmpty()) {
            return Collections.singletonList("");
        }

        if (hexCommand.length() <= MAX_CHUNK_SIZE) {
            return Collections.singletonList(hexCommand);
        }

        List<String> chunks = new ArrayList<>();
        int startPos = 0;

        while (startPos < hexCommand.length()) {
            int endPos = Math.min(startPos + MAX_CHUNK_SIZE, hexCommand.length());
            String chunk = hexCommand.substring(startPos, endPos);
            startPos = endPos;
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
     */
    public synchronized List<ApduResult> transmitWithNewLogicalChannel(String... hexCommands) {
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
     */
    public synchronized List<ApduResult> transmitWithNewLogicalChannel(List<String> hexCommands) {
        List<ApduResult> results = new ArrayList<>();
        CardChannel channel = null;
        try {
            channel = this.card.openLogicalChannel();
            for (String hexCommand : hexCommands) {
                ApduResult result = this.transmit(channel, hexCommand, true);
                results.add(result);
                if (!result.isSuccess()) {
                    break;
                }
            }
        } catch (CardException e) {
            log.error("在新逻辑通道上批量执行命令失败", e);
            throw new UncheckedException("在新逻辑通道上批量执行命令失败", e);
        } finally {
            closeChannelQuietly(channel);
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
     */
    private void validate() {
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
     */
    public synchronized String getEid() {
        ApduResult transmit = this.transmitWithNewLogicalChannel(COMMAND_EID);
        return TLV.extractValsRecursive(transmit.getData(), "BF3E", "5A").getVal("5A");
    }

    /**
     * 安静地关闭逻辑通道，不抛出异常
     *
     * @param channel 要关闭的逻辑通道
     */
    private void closeChannelQuietly(CardChannel channel) {
        if (null != channel) {
            try {
                channel.close();
                log.debug("逻辑通道已关闭");
            } catch (CardException e) {
                log.warn("关闭逻辑通道时发生异常", e);
            }
        }
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
            return this.sw1 == SW1_SUCCESS || this.sw1 == SW1_MORE_DATA;
        }


        /**
         * 将状态字SW1和SW2转换为十六进制字符串
         * <p>
         * 将两个状态字合并为一个4位十六进制字符串，便于日志记录和调试。
         * 例如：SW1=0x90, SW2=0x00 转换为 "9000"
         * </p>
         *
         * @return 格式化后的状态字十六进制字符串（4位大写）
         */
        public String swHex() {
            return String.format("%02X%02X", this.sw1, this.sw2);
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
