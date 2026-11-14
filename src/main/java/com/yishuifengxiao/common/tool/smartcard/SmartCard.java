package com.yishuifengxiao.common.tool.smartcard;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.lang.Hex;
import com.yishuifengxiao.common.tool.lang.TLV;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.smartcardio.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 智能卡操作工具类（v6.15）
 *
 * <p>基于 Java Smart Card IO（{@code javax.smartcardio}）封装智能卡操作能力，
 * 覆盖连接管理、逻辑通道、APDU 传输、自动拉取、81E2 分包协议等场景。</p>
 *
 * <h3>连接状态管理</h3>
 * <p><b>重要：</b>{@link javax.smartcardio.Card} 接口<b>没有</b> {@code isConnected()} 方法，
 * 无法直接查询底层连接状态。本类通过以下方式管理连接生命周期：</p>
 * <ul>
 *     <li>实例字段 {@link #connected} 手动跟踪逻辑连接状态；</li>
 *     <li>{@code connect()} 成功置 {@code true}；</li>
 *     <li>{@code disconnect()} / {@code clearConnectionState()} / 捕获 {@link IllegalStateException} 时置 {@code false}；</li>
 *     <li>公共方法 {@link #isConnected()} 返回该标志位，语义为"本实例是否持有有效连接"，
 *     <b>不代表</b>物理卡片一定在位（驱动缓存可能滞后）。</li>
 * </ul>
 *
 * <h3>物理设备限制</h3>
 * <ul>
 *     <li>Windows 在最后一个读卡器断开后停止 Smart Card 服务，所有 PC/SC 句柄失效，
 *     需重启 JVM 或反射清理 {@code PCSCTerminals} 缓存才能恢复。
 *     若列表操作抛出 {@code SCARD_E_NO_SERVICE / SCARD_E_SERVICE_STOPPED}，
 *     本类会包装为带明确指引的异常。</li>
 *     <li>逻辑通道数量有限（通常 3~19），{@link #openNewLogicalChannel()} 返回的通道必须由调用方关闭。
 *     {@code javax.smartcardio} 的 {@code ChannelImpl} 会忽略手工发送的 MANAGE CHANNEL 命令，
 *     必须使用 {@code openLogicalChannel()} / {@code close()} 管理通道。</li>
 *     <li>基本通道（通道 0）不可调用 {@code close()}，否则抛 {@link IllegalStateException}。</li>
 *     <li>物理拔卡后，PC/SC 层可能在首次 APDU 时才暴露失效，此时捕获 {@link IllegalStateException}
 *     后将 {@link #connected} 置 {@code false} 并清理 {@link #card} 引用。</li>
 * </ul>
 *
 * <h3>逻辑通道约束（ISO/IEC 7816-4）</h3>
 * <p>APDU 的 CLA 字节低位编码逻辑通道号：通道 0~3 对应 CLA 低 4 位 {@code 0x0~0x3}
 * （{@code CLA & 0xF0 | N}）；通道 4~19 对应 {@code CLA & 0xB0 | 0x40 | (N-4)}。
 * 通道有效范围 0~19，通道 0 为基本通道且不可关闭。
 * <b>注意：</b>CLA=0xFF 是协议类型选择保留值，因此通过 {@code 0xFx} 形式无法编码通道 19，
 * 若需要严格使用通道 19，应由调用方确保 CLA 高位不落入保留区。</p>
 * <ul>
 *     <li>{@link #selectIsr()} 与自动拉取命令均通过 {@link #applyChannelToApdu(String, int)}
 *     动态重写 CLA，确保与<b>实际使用的通道</b>匹配。</li>
 *     <li><b>81E2 协议（Aadhaar eID）设计上要求逻辑通道 1</b>：{@link #COMMAND_EID} 与
 *     {@link #transmit81E2On(CardChannel, String)} 生成的命令 CLA 固定为 {@code 0x81}。
 *     本类在发送前会检查通道号：若目标通道不是 1，仅记录 <b>WARN</b> 日志，<b>不再抛异常</b>，
 *     仍继续发送命令。请调用方自行确认 PC/SC 层不会把命令静默路由到通道 1 而导致排查困难。</li>
 * </ul>
 *
 * <h3>状态一致性契约</h3>
 * <ul>
 *     <li>{@link #disconnect()} 清空 {@code card / cardChannel / cardTerminal}，<b>保留</b> {@link #terminalName}；</li>
 *     <li>{@link #connect(String)} 失败时调用 {@link #clearConnectionState()}，断开物理连接并清空所有状态；</li>
 *     <li>{@link #reset()} 失败时恢复 {@link #terminalName}，允许重试。</li>
 * </ul>
 *
 * <h3>自动拉取与状态字处理</h3>
 * <ul>
 *     <li>SW1=0x61 → GET RESPONSE；SW1=0x91 → RETRIEVE RESPONSE；</li>
 *     <li><b>6C XX</b>：Le 错误。本类在自动拉取循环中自动将 Le 修正为 XX 并重发原命令，不再把 6C 当作失败；</li>
 *     <li><b>63 00</b>：认证状态未满足。本类将其视为失败，但不再自动重试。</li>
 * </ul>
 *
 * <p><b>注意</b>：{@code new SmartCard(true, null)} 有编译歧义，无绑定实例请用 {@code new SmartCard(true)}。</p>
 *
 * @author yishui
 * @version v6.15.0
 * @date 2026/5/8
 */
@Slf4j
public class SmartCard {


    /**
     * 查询 eID 的 APDU 命令（81E2 协议，读取 BF3E/5A 标签）
     *
     * <p><b>通道约定</b>：CLA=0x81 按 ISO/IEC 7816-4 表示<b>逻辑通道 1</b>。
     * 81E2 协议（Aadhaar eID）设计上要求命令在通道 1 上执行。
     * 本类在发送前会检查通道号，若目标通道不是 1，仅记录 <b>WARN</b> 日志并继续发送，
     * <b>不再抛异常</b>；请调用方确认 PC/SC 层不会把命令静默路由到通道 1。</p>
     */
    private static final String COMMAND_EID = "81E2910006BF3E035C015A";

    /**
     * 选择 ISR 应用的 APDU 命令（AID：A0000005591010FFFFFFFF8900000100）
     *
     * <p><b>通道约束</b>：原始 CLA=0x01 表示逻辑通道 1。本类在发送前会通过
     * {@link #applyChannelToApdu(String, int)} 根据当前绑定通道号重写 CLA 低位，
     * 以符合"在当前绑定通道上选择 ISR 应用"的语义。</p>
     */
    private static final String COMMAND_SELECT_ISR = "01A4040010A0000005591010FFFFFFFF8900000100";

    /**
     * 81E2 分包时单包最大字节数
     */
    private static final int MAX_CHUNK_SIZE = 255;

    /**
     * 81E2 分包序号上限（单字节，0~255）
     */
    private static final int MAX_CHUNK_COUNT = 256;

    /**
     * 自动拉取最大次数。
     *
     * <p>短格式 APDU 单次响应上限 256 字节，扩展格式可达 65536 字节。
     * 取 256 次 × 256 字节 ≈ 65536 字节，覆盖扩展格式的最大响应，同时作为防御性终止条件。</p>
     */
    private static final int MAX_AUTO_PULL_COUNT = 256;

    /**
     * 状态字 SW1：正常结束
     */
    private static final int SW1_SUCCESS = 0x90;

    /**
     * 状态字 SW1：还有更多数据可拉取（配合 GET RESPONSE）
     */
    private static final int SW1_MORE_DATA = 0x61;

    /**
     * 状态字 SW1：81E2 协议下的"还有更多数据"（配合 RETRIEVE RESPONSE）
     */
    private static final int SW1_RETRIEVE = 0x91;

    /**
     * 状态字 SW1：Le 错误，SW2 指示正确的 Le（00 表示 256）
     */
    private static final int SW1_WRONG_LE = 0x6C;

    /**
     * 状态字 SW1：认证状态未满足（如 63 00）
     */
    private static final int SW1_AUTH_REQUIRED = 0x63;

    /**
     * GET RESPONSE 命令前缀（CLA=01, INS=C0, P1=00, P2=00），后接 Le 字节。
     * 发送前 CLA 会被 {@link #applyChannelToApdu(String, int)} 按实际通道重写。
     */
    private static final String GET_RESPONSE_PREFIX = "01C00000";

    /**
     * RETRIEVE RESPONSE 命令前缀（CLA=80, INS=12, P1=00, P2=00），后接 Le 字节。
     * 发送前 CLA 会被 {@link #applyChannelToApdu(String, int)} 按实际通道重写。
     */
    private static final String RETRIEVE_RESPONSE_PREFIX = "80120000";


    /**
     * 是否启用自动拉取策略，构造时确定，生命周期内不变
     */
    private final boolean autoPull;

    /**
     * 逻辑连接状态标志。
     * <p>{@code connect()} 成功置 {@code true}；{@code disconnect()} / {@code clearConnectionState()} /
     * 捕获 {@link IllegalStateException} 时置 {@code false}。由实例方法在 {@code synchronized} 块内读写。</p>
     */
    private boolean connected = false;

    /**
     * 当前连接到的终端实际名称。
     * <p>记录的是模糊匹配命中后的真实名称（非用户原始入参）。</p>
     * <p><b>生命周期策略</b>：</p>
     * <ul>
     *     <li>{@code connect(String)} 成功时更新为命中终端名；</li>
     *     <li>{@code connect(String)} 失败时被 {@link #clearConnectionState()} 清空；</li>
     *     <li>{@code disconnect()} <b>保留</b>该字段，供 {@link #reset()} 与懒加载重连使用；</li>
     *     <li>{@code reset()} 失败时恢复旧值，允许用户重试。</li>
     * </ul>
     */
    private String terminalName;

    /**
     * 当前 {@link CardTerminal} 引用；无终端关联时为 {@code null}
     */
    private CardTerminal cardTerminal;

    /**
     * 当前底层 {@link Card} 实例；未连接或已断开时为 {@code null}
     */
    private Card card;

    /**
     * 当前绑定的逻辑通道；构造注入或懒加载，断开时置 {@code null}
     */
    private CardChannel cardChannel;


    /**
     * 默认构造：启用自动拉取，未绑定连接（需后续 {@link #connect(String)}）。
     * <p>等价于 {@code new SmartCard(true)}。</p>
     */
    public SmartCard() {
        this(true);
    }

    /**
     * 构造客户端实例（未绑定连接，需后续 {@link #connect(String)}）。
     *
     * @param autoPull 是否启用自动拉取
     */
    public SmartCard(boolean autoPull) {
        this.autoPull = autoPull;
    }

    /**
     * 通过终端名称构造：立即按模糊匹配规则连接。
     *
     * <p><b>注意</b>：若 {@code terminalName} 为 {@code null}，构造函数会抛异常；
     * 如需创建未连接实例，请使用 {@code new SmartCard(autoPull)}。</p>
     *
     * @param autoPull     是否启用自动拉取
     * @param terminalName 终端名称（支持模糊匹配），不能为 {@code null} 或纯空白（含 Unicode 空白）
     * @throws UncheckedException 名称非法、无匹配终端、卡不在位、终端失效或连接失败
     */
    public SmartCard(boolean autoPull, String terminalName) {
        this.autoPull = autoPull;
        this.connect(terminalName);
    }

    /**
     * 通过 {@link CardTerminal} 构造：立即建立连接。
     *
     * @param autoPull 是否启用自动拉取
     * @param terminal 目标读卡器；为 {@code null} 时等价于 {@link #SmartCard(boolean)}
     * @throws UncheckedException 卡不在位、终端失效或连接失败
     */
    public SmartCard(boolean autoPull, CardTerminal terminal) {
        this.autoPull = autoPull;
        if (null == terminal) {
            return;
        }
        ensureCardPresent(terminal);
        try {
            this.card = terminal.connect("*");
            this.cardTerminal = terminal;
            this.terminalName = terminal.getName();
            this.connected = true;
            log.debug("通过 CardTerminal 成功连接到读卡器: {}", this.terminalName);
        } catch (IllegalStateException e) {
            throw new UncheckedException("终端已失效（可能读卡器已被物理移除）: " + terminal.getName(), e);
        } catch (CardException e) {
            throw new UncheckedException("建立与卡片的连接失败（终端：" + terminal.getName() + "）", e);
        }
    }

    /**
     * 通过已打开的 {@link CardChannel} 构造：直接使用该通道，{@link #card()} 由通道推导。
     *
     * <p><b>所有权提示</b>：{@link #disconnect()} / {@link #reset()} 会断开此通道及其 Card，
     * 请勿在外部继续使用。</p>
     *
     * <p><b>通道提示</b>：若注入通道不是通道 1，81E2 相关方法（{@code COMMAND_EID} 与
     * {@code transmit81E2*}）在发送前会记录 <b>WARN</b> 日志，但仍继续发送，
     * 由调用方自行确认 PC/SC 层不会把命令静默路由到通道 1。</p>
     *
     * @param autoPull    是否启用自动拉取
     * @param cardChannel 外部逻辑通道；为 {@code null} 时等价于 {@link #SmartCard(boolean)}
     * @throws UncheckedException 通道已关闭或未关联有效 Card
     */
    public SmartCard(boolean autoPull, CardChannel cardChannel) {
        this.autoPull = autoPull;
        if (null == cardChannel) {
            return;
        }
        try {
            this.cardChannel = cardChannel;
            this.card = cardChannel.getCard();
            this.connected = true;
        } catch (IllegalStateException e) {
            throw new UncheckedException("提供的 CardChannel 已关闭或对应卡片已断开", e);
        }
        if (null == this.card) {
            this.connected = false;
            throw new UncheckedException("提供的 CardChannel 未关联有效的 Card");
        }
    }


    /**
     * 当前实例是否启用自动拉取。
     *
     * @return 是否自动拉取
     */
    public synchronized boolean isAutoPull() {
        return autoPull;
    }

    /**
     * 当前连接到的终端实际名称。
     *
     * @return 终端名；未连接或无终端关联时返回 {@code null}
     */
    public synchronized String terminalName() {
        return this.terminalName;
    }

    /**
     * 当前 {@link CardTerminal} 引用。
     *
     * @return 终端引用；无终端关联时返回 {@code null}
     */
    public synchronized CardTerminal cardTerminal() {
        return this.cardTerminal;
    }

    /**
     * 当前底层 {@link Card} 实例。
     *
     * @return Card 实例；未连接时返回 {@code null}
     */
    public synchronized Card card() {
        return this.card;
    }

    /**
     * 当前是否持有有效连接。
     *
     * <p>返回 {@code true} 表示本实例认为连接有效；返回 {@code false} 表示已确认断开。
     * 注意 {@code true} 不代表物理卡片一定在位（驱动缓存可能滞后）。</p>
     *
     * @return 逻辑连接状态
     */
    public synchronized boolean isConnected() {
        return this.connected;
    }

    /**
     * 获取当前卡片的 ATR（Answer To Reset）。
     *
     * <p>ATR 用于识别卡片类型，建议在 {@link #connect(String)} 成功后立即读取并缓存。</p>
     *
     * @return ATR 对象
     * @throws UncheckedException 未连接或卡片已断开
     */
    public synchronized ATR atr() {
        ensureCardConnected();
        try {
            return this.card.getATR();
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("卡片已失效（可能已被物理移除），无法获取 ATR", e);
        }
    }


    /**
     * 获取默认 {@link CardTerminals} 终端管理器。
     *
     * @return 终端管理器实例
     */
    private synchronized CardTerminals getCardTerminals() {
        return TerminalFactory.getDefault().terminals();
    }

    /**
     * 获取所有已连接读卡器名称。
     *
     * @return 读卡器名称列表；异常或无读卡器时返回空列表
     */
    public synchronized List<String> getCardTerminalNames() {
        try {
            return getCardTerminals().list().stream()
                    .map(CardTerminal::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取智能卡读卡器名称列表失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 按状态过滤读卡器名称。
     *
     * @param state 终端状态过滤器；{@code null} 等价于 {@link #getCardTerminalNames()}
     * @return 读卡器名称列表；异常时返回空列表
     */
    public synchronized List<String> getCardTerminals(CardTerminals.State state) {
        try {
            if (null == state) {
                return getCardTerminalNames();
            }
            return getCardTerminals().list(state).stream()
                    .map(CardTerminal::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取指定状态的智能卡读卡器列表失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 按终端名称连接（支持模糊匹配）。
     *
     * <p>匹配规则：入参 {@code null}/纯空白抛异常 → 精确优先 → {@code contains} 模糊；
     * 多个命中取第一个并 WARN；无匹配抛异常并列出所有可用终端。</p>
     *
     * <p><b>失败清理契约</b>：若最终连接失败（无读卡器、无匹配、卡不在位、终端失效、
     * 底层 {@code CardException}），本方法会调用 {@link #clearConnectionState()}
     * <b>断开物理连接</b>并清空全部状态（含 {@link #terminalName}）。</p>
     *
     * <p><b>幂等契约</b>：若已连接到目标终端且 {@link #connected} 为 true，直接返回。</p>
     *
     * @param terminalName 终端名称（支持模糊匹配），不能为 {@code null} 或纯空白
     * @return 当前实例
     * @throws UncheckedException 名称非法、无匹配、卡不在位、终端失效或连接失败
     */
    public synchronized SmartCard connect(String terminalName) {
        String stripped = stripBlank(terminalName);
        if (null == stripped || stripped.isEmpty()) {
            throw new UncheckedException("terminalName 不能为空或纯空白");
        }
        final String keyword = stripped;

        // 获取终端列表（失败不清空状态：PC/SC 服务临时异常时保留原状态供用户重试）
        List<CardTerminal> terminals;
        try {
            terminals = getCardTerminals().list();
        } catch (Exception e) {
            log.error("获取智能卡读卡器列表失败", e);
            if (isPcscServiceStopped(e)) {
                throw new UncheckedException(
                        "Windows Smart Card 服务已停止（SCARD_E_NO_SERVICE / SCARD_E_SERVICE_STOPPED）。" +
                                "请以管理员身份执行 `net start SCardSvr`，或重启 JVM 以清理 PCSCTerminals 缓存后重试。", e);
            }
            throw new UncheckedException("获取智能卡读卡器列表失败", e);
        }
        if (terminals == null || terminals.isEmpty()) {
            // 无读卡器：清空全部状态并断开物理连接
            clearConnectionState();
            throw new UncheckedException("未发现任何可用的智能卡读卡器，请检查读卡器是否连接");
        }

        // 精确匹配优先
        CardTerminal target = null;
        for (CardTerminal t : terminals) {
            if (keyword.equals(t.getName())) {
                target = t;
                break;
            }
        }

        // 模糊匹配（contains），多个命中取第一个
        if (null == target) {
            CardTerminal firstMatch = null;
            int matchCount = 0;
            for (CardTerminal t : terminals) {
                if (t.getName().contains(keyword)) {
                    if (null == firstMatch) {
                        firstMatch = t;
                    }
                    matchCount++;
                }
            }
            if (null == firstMatch) {
                String available = terminals.stream()
                        .map(CardTerminal::getName)
                        .collect(Collectors.joining(", "));
                // 无匹配：清空全部状态并断开物理连接
                clearConnectionState();
                throw new UncheckedException(
                        "未找到与'" + keyword + "'匹配的终端。可用终端: [" + available + "]");
            }
            if (matchCount > 1) {
                log.warn("terminalName '{}' 模糊匹配到 {} 个终端，将使用第一个: {}",
                        keyword, matchCount, firstMatch.getName());
            }
            target = firstMatch;
        }

        // 幂等：已连接目标终端且逻辑状态有效，直接返回
        if (this.connected && null != this.card && Objects.equals(this.terminalName, target.getName())) {
            return this;
        }

        // 断开旧连接（保留 terminalName，由后续步骤决定更新或清空）
        try {
            disconnect();
        } catch (UncheckedException e) {
            log.warn("切换终端前清理旧连接异常，已忽略", e);
        }

        // 建立新连接；失败则清空全部状态，避免静默回退到旧终端
        try {
            ensureCardPresent(target);
            this.card = target.connect("*");
            this.cardTerminal = target;
            this.terminalName = target.getName();
            this.connected = true;
            log.debug("成功连接到读卡器: {} (请求: {})", this.terminalName, keyword);
            return this;
        } catch (UncheckedException e) {
            clearConnectionState();
            throw e;
        } catch (Exception e) {
            clearConnectionState();
            throw new UncheckedException("连接终端失败（终端：" + target.getName() + "）", e);
        }
    }

    /**
     * 断开连接并清空 {@code card / cardChannel / cardTerminal}，<b>保留 terminalName</b>。
     *
     * <p><b>所有权提示</b>：若通过 {@link #SmartCard(boolean, CardChannel)} 注入通道，
     * 本方法会断开注入的通道及其 Card，外部不应再使用。</p>
     *
     * <p><b>物理拔卡兼容</b>：{@code card.disconnect(true)} 可能抛 {@link IllegalStateException}
     * （卡已被物理断开）。由于断开目的已实际达成，本方法<b>不抛此异常</b>，仅记录 WARN。</p>
     *
     * <p><b>{@link CardException} 处理</b>：包装为 {@link UncheckedException} 抛出，
     * 但 {@code finally} 已保证状态清理，用户收到异常时实例已处于"未连接"状态。</p>
     *
     * @return 当前实例
     * @throws UncheckedException 底层驱动报告断开失败（{@link CardException}），此时状态已清理
     */
    public synchronized SmartCard disconnect() {
        if (null == this.card) {
            if (null != this.cardChannel) {
                closeChannelQuietly(this.cardChannel);
            }
            this.cardChannel = null;
            this.cardTerminal = null;
            this.connected = false;
            return this;
        }
        try {
            this.card.disconnect(true);
            log.debug("已断开智能卡连接");
        } catch (IllegalStateException e) {
            // 物理拔卡后断开目的已达成，不抛异常
            log.warn("断开连接时卡片已失效（可能已被物理移除），视为断开成功", e);
        } catch (CardException e) {
            throw new UncheckedException("断开与该卡的连接失败（实例状态已清理）", e);
        } finally {
            this.card = null;
            this.cardChannel = null;
            this.cardTerminal = null;
            this.connected = false;
        }
        return this;
    }

    /**
     * 重置智能卡：关闭绑定通道 → 断开物理连接 → 用已记录的实际终端名重连。
     *
     * <p><b>失败恢复契约</b>：若重连失败，本方法会<b>恢复</b>进入方法时的 {@link #terminalName}，
     * 使用户可以在同一实例上重试 {@code reset()}。</p>
     *
     * @return 当前实例
     * @throws UncheckedException 无终端名（无法重连）或重连失败
     */
    public synchronized SmartCard reset() {
        if (null == this.terminalName) {
            throw new UncheckedException(
                    "当前实例未关联终端（可能通过 CardChannel 直接构造），无法 reset。" +
                            "请改用 new SmartCard(autoPull, terminalName) 或 connect(String) 方式构造");
        }
        final String savedTerminalName = this.terminalName;
        if (null != this.cardChannel) {
            closeChannelQuietly(this.cardChannel);
            this.cardChannel = null;
        }
        try {
            disconnect();
        } catch (UncheckedException e) {
            log.warn("重置时断开连接失败，仍将尝试重连", e);
        }
        try {
            return connect(savedTerminalName);
        } catch (RuntimeException e) {
            // connect 失败会清空 terminalName，此处恢复以便用户重试 reset
            this.terminalName = savedTerminalName;
            throw e;
        }
    }

    /**
     * 获取当前绑定的逻辑通道；不存在或已失效时，会确保连接并重新打开。
     *
     * <p>物理拔卡后已缓存的 {@code cardChannel} 引用仍非 null 但实际已失效；
     * 本方法会主动关闭失效通道，确保底层连接有效后重新打开新通道。</p>
     *
     * @return 当前 {@link CardChannel}，保证非 {@code null}
     * @throws UncheckedException 无连接且无终端信息、卡已失效且重连失败，或打开通道失败
     */
    public synchronized CardChannel cardChannel() {
        // 已有绑定通道且逻辑状态有效：直接返回；但需验证通道本身未被外部关闭
        if (null != this.cardChannel && this.connected) {
            try {
                this.cardChannel.getChannelNumber();
                return this.cardChannel;
            } catch (IllegalStateException e) {
                log.warn("已缓存的逻辑通道已失效，将重新打开", e);
                this.cardChannel = null;
            }
        }
        // 已失效：主动释放失效通道引用
        if (null != this.cardChannel) {
            closeChannelQuietly(this.cardChannel);
            this.cardChannel = null;
        }
        // 确保底层连接有效（必要时自动重连）
        ensureConnectedOrReconnect();
        try {
            this.cardChannel = this.card.openLogicalChannel();
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("卡片已失效（可能已被物理移除），无法打开逻辑通道", e);
        } catch (CardException e) {
            throw new UncheckedException(e);
        }
        return this.cardChannel;
    }

    /**
     * 打开一个新的逻辑通道（不改变当前绑定通道）。
     *
     * <p>若底层连接已失效但存在终端名，会自动重连后再打开新通道。</p>
     *
     * <p><b>物理限制</b>：逻辑通道数量有限（通常 3~19）。
     * 调用方必须最终使用 {@link #closeChannelQuietly(CardChannel)} 关闭，否则会导致通道耗尽。</p>
     *
     * @return 新建的逻辑通道
     * @throws UncheckedException 无连接且无终端信息、卡已失效且重连失败，或打开失败
     */
    public synchronized CardChannel openNewLogicalChannel() {
        ensureConnectedOrReconnect();
        try {
            return this.card.openLogicalChannel();
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("卡片已失效（可能已被物理移除），无法打开新逻辑通道", e);
        } catch (CardException e) {
            throw new UncheckedException(e);
        }
    }

    /**
     * 获取基础通道（通道号 0）。
     *
     * <p>若底层连接已失效但存在终端名，会自动重连后再获取基础通道。</p>
     *
     * <p><b>注意</b>：ISO/IEC 7816-4 规定基本通道永久可用，<b>不可调用 {@code close()}</b>，
     * 否则抛 {@link IllegalStateException}。</p>
     *
     * @return 基础 {@link CardChannel}
     * @throws UncheckedException 无连接且无终端信息、卡已失效且重连失败，或获取失败
     */
    public synchronized CardChannel getBasicChannel() {
        ensureConnectedOrReconnect();
        try {
            return this.card.getBasicChannel();
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("卡片已失效（可能已被物理移除），无法获取基础通道", e);
        } catch (Exception e) {
            throw new UncheckedException(e);
        }
    }

    /**
     * 当前绑定通道号。
     *
     * @return 通道号（0~19）
     * @throws UncheckedException 通道已关闭或卡片已失效
     */
    public synchronized Integer getChannelNumber() {
        try {
            return cardChannel().getChannelNumber();
        } catch (IllegalStateException e) {
            throw new UncheckedException("逻辑通道已关闭或卡片已失效，无法获取通道号", e);
        }
    }

    /**
     * 在当前绑定通道上选择 ISR 应用。
     *
     * <p>选择 ISR 时<b>强制自动拉取</b>（不受实例 {@link #autoPull} 影响），
     * 确保完整读取选择结果；失败（无响应或 SW1/SW2 非 0x90）时抛
     * {@link UncheckedException}，避免后续 APDU 因未激活而返回 6A82 级联失败。</p>
     *
     * <p><b>通道语义</b>：本方法通过 {@link #applyChannelToApdu(String, int)} 按当前绑定通道号
     * 重写 {@code COMMAND_SELECT_ISR} 的 CLA 低 4 位，确保命令<b>发送到当前绑定通道</b>。</p>
     *
     * @return 当前实例
     * @throws UncheckedException 未连接、传输失败或选择失败
     */
    public synchronized SmartCard selectIsr() {
        CardChannel channel = this.cardChannel();
        final int channelNumber;
        try {
            channelNumber = channel.getChannelNumber();
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("通道已失效，无法获取通道号以选择 ISR 应用", e);
        }
        String command = applyChannelToApdu(COMMAND_SELECT_ISR, channelNumber);
        ApduResult r = transmitOn(channel, command, true);
        if (!r.isSuccess()) {
            throw new UncheckedException(
                    "选择 ISR 应用失败，SW=" + r.swHex() + "，data=" + r.getData());
        }
        return this;
    }

    /**
     * 获取 eID（新通道 + ISR + 解析 BF3E/5A）。
     *
     * <p>EID 响应通常跨越多次 GET/RETRIEVE RESPONSE，本方法<b>强制自动拉取</b>
     * （不受实例 {@link #autoPull} 影响），确保拿到完整数据后再解析。</p>
     *
     * <p><b>通道提示</b>：81E2 协议（{@code COMMAND_EID}）设计上要求 CLA 低 4 位表示通道 1。
     * 若执行通道不是 1，本方法仅记录 <b>WARN</b> 日志并继续发送（<b>不再抛异常</b>），
     * 请调用方确认 PC/SC 层不会把命令静默路由到通道 1。</p>
     *
     * <p>失败语义：本方法<b>不抛异常</b>，失败时返回 {@code null} 并记录 ERROR 日志。</p>
     *
     * @return eID 字符串；获取或解析失败返回 {@code null}
     */
    public synchronized String getEid() {
        try {
            ApduResults rs = executeWithNewLogicalChannelAndSelectIsr(
                    channel -> {
                        int chNum = channel.getChannelNumber();
                        if (chNum != 1) {
                            log.warn("81E2 eID 命令设计上要求逻辑通道 1，但当前通道为 {}。" +
                                    "仍将继续发送，请确认 PC/SC 层不会把命令静默路由到通道 1", chNum);
                        }
                        return transmitInternal(channel, Collections.singletonList(COMMAND_EID), true);
                    },
                    "获取 eID 失败",
                    "获取 eID 失败");
            ApduResult first = rs.first();
            if (first == null || first.getData() == null || first.getData().isEmpty()) {
                log.error("获取eID失败：响应数据为空");
                return null;
            }
            return TLV.extractValsRecursive(first.getData(), "BF3E", "5A").getVal("5A");
        } catch (UncheckedException e) {
            log.error("获取eID失败", e);
            return null;
        } catch (RuntimeException e) {
            log.error("获取eID时发生异常（可能响应数据格式不合法）", e);
            return null;
        }
    }


    /**
     * 在实例绑定通道上依次传输多条命令（varargs / 数组）。
     * <p>自动拉取策略由实例 {@link #autoPull} 决定；任一条失败即中断。</p>
     *
     * @param hexCommands 十六进制命令列表
     * @return 按顺序累积的执行结果；空数组 / null 返回空结果
     */
    public synchronized ApduResults transmit(String... hexCommands) {
        if (null == hexCommands || hexCommands.length == 0) {
            return new ApduResults();
        }
        return transmitInternal(this.cardChannel(), Arrays.asList(hexCommands));
    }

    /**
     * 在实例绑定通道上依次传输多条命令（List）。
     *
     * @param hexCommands 十六进制命令列表
     * @return 执行结果集合；空集合 / null 返回空结果
     * @see #transmit(String...)
     */
    public synchronized ApduResults transmit(List<String> hexCommands) {
        if (null == hexCommands || hexCommands.isEmpty()) {
            return new ApduResults();
        }
        return transmitInternal(this.cardChannel(), hexCommands);
    }


    /**
     * 新建临时通道 → 依次执行 → 关闭（varargs）。
     * <p>不影响实例绑定通道。</p>
     *
     * @param hexCommands 十六进制命令列表
     * @return 执行结果集合；空数组 / null 返回空结果
     */
    public synchronized ApduResults transmitInNewChannel(String... hexCommands) {
        if (null == hexCommands || hexCommands.length == 0) {
            return new ApduResults();
        }
        return transmitInNewChannel(Arrays.asList(hexCommands));
    }

    /**
     * 新建临时通道 → 依次执行 → 关闭（List）。
     *
     * <p>空集合 / null 直接返回空结果，不要求已连接（与 {@link #transmit(List)} 语义一致）。</p>
     *
     * @param hexCommands 十六进制命令列表
     * @return 执行结果集合；空集合 / null 返回空结果
     * @see #transmitInNewChannel(String...)
     */
    public synchronized ApduResults transmitInNewChannel(List<String> hexCommands) {
        if (null == hexCommands || hexCommands.isEmpty()) {
            return new ApduResults();
        }
        return executeWithNewLogicalChannel(
                channel -> transmitInternal(channel, hexCommands),
                "在新逻辑通道上执行命令失败",
                "在新逻辑通道上执行命令失败");
    }


    /**
     * 新建临时通道 → 选 ISR → 依次执行 → 关闭（varargs）。
     *
     * @param hexCommands 十六进制命令列表
     * @return 执行结果集合；空数组 / null 返回空结果
     * @throws UncheckedException 选 ISR 失败或命令传输失败
     */
    public synchronized ApduResults transmitInNewChannelWithIsr(String... hexCommands) {
        if (null == hexCommands || hexCommands.length == 0) {
            return new ApduResults();
        }
        return transmitInNewChannelWithIsr(Arrays.asList(hexCommands));
    }

    /**
     * 新建临时通道 → 选 ISR → 依次执行 → 关闭（List）。
     *
     * <p>空集合 / null 直接返回空结果，不要求已连接（与 {@link #transmit(List)} 语义一致）。</p>
     *
     * @param hexCommands 十六进制命令列表
     * @return 执行结果集合；空集合 / null 返回空结果
     * @see #transmitInNewChannelWithIsr(String...)
     */
    public synchronized ApduResults transmitInNewChannelWithIsr(List<String> hexCommands) {
        if (null == hexCommands || hexCommands.isEmpty()) {
            return new ApduResults();
        }
        return executeWithNewLogicalChannelAndSelectIsr(
                channel -> transmitInternal(channel, hexCommands),
                "在新逻辑通道(含ISR)上执行命令失败",
                "在新逻辑通道(含ISR)上执行命令失败");
    }


    /**
     * 在实例绑定通道上执行 81E2 命令（varargs）。
     *
     * <p><b>通道提示</b>：81E2 命令 CLA 固定为 {@code 0x81}（逻辑通道 1）。
     * 若目标通道号不是 1，本方法仅记录 <b>WARN</b> 日志并继续发送，<b>不再抛异常</b>。</p>
     *
     * <p><b>注意</b>：81E2 分包响应跨越多帧，内部<b>强制自动拉取</b>，不受实例 {@link #autoPull} 影响。</p>
     *
     * @param hexCommands 81E2 命令数据（不含 CLA 头）
     * @return 执行结果集合；空数组 / null 返回空结果
     */
    public synchronized ApduResults transmit81E2(String... hexCommands) {
        if (null == hexCommands || hexCommands.length == 0) {
            return new ApduResults();
        }
        return transmit81E2Internal(this.cardChannel(), Arrays.asList(hexCommands));
    }

    /**
     * 在实例绑定通道上执行 81E2 命令（List）。
     *
     * @param hexCommands 81E2 命令数据
     * @return 执行结果集合；空集合 / null 返回空结果
     * @see #transmit81E2(String...)
     */
    public synchronized ApduResults transmit81E2(List<String> hexCommands) {
        if (null == hexCommands || hexCommands.isEmpty()) {
            return new ApduResults();
        }
        return transmit81E2Internal(this.cardChannel(), hexCommands);
    }

    /**
     * 新建临时通道 → 选 ISR → 执行 81E2 命令（varargs）。
     *
     * @param hexCommands 81E2 命令数据
     * @return 执行结果集合；空数组 / null 返回空结果
     */
    public synchronized ApduResults transmit81E2InNewChannelWithIsr(String... hexCommands) {
        if (null == hexCommands || hexCommands.length == 0) {
            return new ApduResults();
        }
        return transmit81E2InNewChannelWithIsr(Arrays.asList(hexCommands));
    }

    /**
     * 新建临时通道 → 选 ISR → 执行 81E2 命令（List）。
     *
     * <p>空集合 / null 直接返回空结果，不要求已连接（与 {@link #transmit(List)} 语义一致）。</p>
     *
     * <p><b>通道提示</b>：81E2 命令 CLA 固定为 {@code 0x81}（逻辑通道 1）。
     * {@link #executeWithNewLogicalChannelAndSelectIsr} 打开的新通道通常为通道 1，
     * 但若通道 1 已被占用，新通道可能为其他值。此时 {@link #transmit81E2On} 仅记录 <b>WARN</b>
     * 日志并继续发送，<b>不再抛异常</b>。</p>
     *
     * @param hexCommands 81E2 命令数据
     * @return 执行结果集合；空集合 / null 返回空结果
     * @see #transmit81E2InNewChannelWithIsr(String...)
     */
    public synchronized ApduResults transmit81E2InNewChannelWithIsr(List<String> hexCommands) {
        if (null == hexCommands || hexCommands.isEmpty()) {
            return new ApduResults();
        }
        return executeWithNewLogicalChannelAndSelectIsr(
                channel -> transmit81E2Internal(channel, hexCommands),
                "在81E2专用逻辑通道上执行命令失败",
                "在81E2专用逻辑通道上执行命令失败");
    }


    /**
     * 十六进制字符串 → {@link CommandAPDU}。
     *
     * <p>入参先经过 {@code normalizeHex} 去除所有空白字符（含 ASCII 空白、NBSP、全角空格等），
     * 再校验格式与长度。</p>
     *
     * @param hexCommand 十六进制命令（允许含空白），不能为 {@code null} 或空串
     * @return CommandAPDU
     * @throws UncheckedException 入参为空、格式非法、字节数不合规或构造失败
     */
    public synchronized CommandAPDU convertToCommandApdu(String hexCommand) {
        String normalized = normalizeHex(hexCommand);
        if (null == normalized || normalized.isEmpty()) {
            throw new UncheckedException("APDU 命令不能为空");
        }
        try {
            if (!Hex.isHex(normalized)) {
                throw new UncheckedException(String.format("输入的数据%s不是十六进制数据", normalized));
            }
            if (normalized.length() % 2 != 0) {
                throw new UncheckedException(String.format("输入的十六进制数据%s长度不是偶数", normalized));
            }
            return new CommandAPDU(Hex.hexToBytes(normalized));
        } catch (UncheckedException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new UncheckedException(
                    "APDU 命令字节数不符合 ISO/IEC 7816-4 要求（4~261 短格式，或扩展格式）: "
                            + normalized + "，有效字节数=" + (normalized.length() / 2), e);
        } catch (Exception e) {
            log.error("将Hex数据{}转换成CommandAPDU时出现问题", normalized, e);
            throw new UncheckedException("将Hex数据" + normalized + "转换成CommandAPDU时出现问题", e);
        }
    }

    /**
     * 按 ISO/IEC 7816-4 规则根据通道号重写 APDU 的 CLA 低 4 位。
     *
     * <p>编码规则：</p>
     * <ul>
     *     <li>通道 0~3：{@code CLA = (CLA & 0xF0) | N}；</li>
     *     <li>通道 4~19：{@code CLA = (CLA & 0xB0) | 0x40 | (N - 4)}；</li>
     *     <li>通道号超出 0~19 或入参非法时原样返回，由调用方自行校验。</li>
     * </ul>
     *
     * <p><b>保留值提示</b>：CLA=0xFF 为协议类型选择保留值，因此通过 {@code 0xFx} 形式
     * 无法编码通道 19。若需要严格使用通道 19，应由调用方确保 CLA 高位不落入保留区。</p>
     *
     * <p>本方法为纯函数，不涉及共享状态，无需同步。</p>
     *
     * @param hexCommand    十六进制 APDU 命令（至少 2 个 hex 字符）
     * @param channelNumber 目标逻辑通道号（0~19）
     * @return 重写 CLA 后的十六进制 APDU 命令；入参非法时返回原值
     */
    private static String applyChannelToApdu(String hexCommand, int channelNumber) {
        if (null == hexCommand || hexCommand.length() < 2) {
            return hexCommand;
        }
        if (channelNumber < 0 || channelNumber > 19) {
            return hexCommand;
        }
        final int cla;
        try {
            cla = Integer.parseInt(hexCommand.substring(0, 2), 16);
        } catch (NumberFormatException e) {
            return hexCommand;
        }
        final int newCla;
        if (channelNumber <= 3) {
            newCla = (cla & 0xF0) | channelNumber;
        } else {
            newCla = (cla & 0xB0) | 0x40 | (channelNumber - 4);
        }
        return String.format("%02X", newCla) + hexCommand.substring(2);
    }

    /**
     * 按 {@link #MAX_CHUNK_SIZE}（255 字节 = 510 个 hex 字符）切分命令。
     *
     * <p>入参先经过 {@code normalizeHex} 去除空白，避免 chunk 中混入空白导致长度字节计算错误。</p>
     *
     * @param hexCommand 十六进制命令
     * @return 切分片段；{@code null}/{@code ""} 返回只含一个空串的列表
     */
    public synchronized List<String> splitCommand(String hexCommand) {
        String normalized = normalizeHex(hexCommand);
        if (null == normalized || normalized.isEmpty()) {
            return Collections.singletonList("");
        }
        final int maxLen = MAX_CHUNK_SIZE * 2;
        if (normalized.length() <= maxLen) {
            return Collections.singletonList(normalized);
        }
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + maxLen, normalized.length());
            chunks.add(normalized.substring(start, end));
            start = end;
        }
        return chunks;
    }

    /**
     * 安静关闭通道；{@code null} 直接返回。
     *
     * <p>物理拔卡后 {@code channel.close()} 可能抛 {@link IllegalStateException}
     * （通道已被底层回收），本方法捕获后仅记录 WARN，不向外抛出。</p>
     *
     * @param channel 待关闭通道
     */
    public static synchronized void closeChannelQuietly(CardChannel channel) {
        if (null == channel) {
            return;
        }
        try {
            channel.close();
            log.debug("逻辑通道已关闭");
        } catch (IllegalStateException e) {
            log.warn("关闭逻辑通道时通道已失效（可能已被底层回收或卡片已拔出）", e);
        } catch (CardException e) {
            log.warn("关闭逻辑通道时发生异常", e);
        }
    }

    /**
     * 统一 hex 规范化：去除所有空白字符（含 ASCII 空白、NBSP、全角空格等不可见字符）。
     *
     * <p>本方法为纯函数，不涉及共享状态，无需同步。</p>
     *
     * @param hex 原始 hex 字符串；{@code null} 时返回 {@code null}
     * @return 去除所有空白后的 hex 字符串
     */
    private static String normalizeHex(String hex) {
        if (null == hex) {
            return null;
        }
        StringBuilder sb = new StringBuilder(hex.length());
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (!isBlankChar(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 去除字符串首尾所有空白字符（含 ASCII 空白、NBSP、全角空格等）。
     *
     * <p>本方法为纯函数，不涉及共享状态，无需同步。</p>
     *
     * @param s 原始字符串；{@code null} 时返回 {@code null}
     * @return 去除首尾所有空白后的字符串
     */
    private static String stripBlank(String s) {
        if (null == s) {
            return null;
        }
        int start = 0;
        int end = s.length();
        while (start < end && isBlankChar(s.charAt(start))) {
            start++;
        }
        while (end > start && isBlankChar(s.charAt(end - 1))) {
            end--;
        }
        return s.substring(start, end);
    }

    /**
     * 判断字符是否为空白/不可见分隔符。
     *
     * @param c 待判定字符
     * @return 是否为空白字符
     */
    private static boolean isBlankChar(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    /**
     * 将 0~255 的整数格式化为两位大写十六进制字符串（补零）。
     *
     * <p>用于 81E2 分包序号、分包长度、自动拉取 Le 等单字节字段，保证 APDU 长度为偶数。</p>
     *
     * @param value 整数值（低 8 位有效）
     * @return 两位十六进制字符串
     */
    private static String toHexByte(int value) {
        return String.format("%02X", value & 0xFF);
    }

    /**
     * 判断异常是否为 Windows PC/SC 服务停止。
     */
    private static boolean isPcscServiceStopped(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            String msg = cur.getMessage();
            if (msg != null && (msg.contains("SCARD_E_NO_SERVICE")
                    || msg.contains("SCARD_E_SERVICE_STOPPED"))) {
                return true;
            }
            cur = cur.getCause();
        }
        return false;
    }

    /**
     * 标记为已断开并清理失效引用（不抛异常，供 catch 块调用）。
     *
     * <p>物理拔卡后 {@code cardChannel} 可能已失效，但仍尝试安静关闭以释放 PC/SC 层资源；
     * 保留 {@link #terminalName} 以便后续自动重连。</p>
     */
    private synchronized void markDisconnected() {
        if (null != this.cardChannel) {
            closeChannelQuietly(this.cardChannel);
        }
        this.cardChannel = null;
        this.card = null;
        this.connected = false;
    }

    /**
     * 清空全部连接状态（含 {@link #terminalName}），并主动断开旧物理连接。
     *
     * <p><b>为什么必须主动断开</b>：仅清空 {@code this.card} 引用不会断开物理连接，
     * 旧 Card 被 GC 前 PC/SC 层仍占用终端，导致后续对新终端的连接被拒。</p>
     */
    private synchronized void clearConnectionState() {
        if (null != this.card) {
            try {
                this.card.disconnect(true);
            } catch (IllegalStateException e) {
                log.warn("清空状态时卡片已失效（可能已被物理移除），视为断开成功", e);
            } catch (CardException e) {
                log.warn("清空状态时断开卡片发生异常，已忽略", e);
            }
        }
        this.card = null;
        this.cardChannel = null;
        this.cardTerminal = null;
        this.terminalName = null;
        this.connected = false;
    }

    /**
     * 前置校验：必须已连接到智能卡。
     *
     * @throws UncheckedException 未连接时抛出
     */
    private synchronized void requireConnected() {
        if (!this.connected) {
            throw new UncheckedException(
                    "请先连接智能卡（或通过构造注入 CardChannel / CardTerminal / terminalName）");
        }
    }

    /**
     * 前置校验：底层卡片连接是否仍然有效。
     *
     * <p>由于 {@link Card} 接口无 {@code isConnected()} 方法，本方法仅依赖
     * {@link #connected} 标志位判断，不能发现驱动缓存滞后导致的"看似连接实际失效"。
     * 真正的失效必须通过 APDU 通讯异常（{@link IllegalStateException}）兜底发现。</p>
     *
     * @throws UncheckedException 卡片已断开
     */
    private synchronized void ensureCardConnected() {
        requireConnected();
    }

    /**
     * 确保底层连接有效，必要时自动重连。
     *
     * <p>处理"物理拔卡后引用非 null 但已失效"的场景：</p>
     * <ul>
     *     <li>{@link #connected} 为 true：直接返回；</li>
     *     <li>失效但有 {@link #terminalName}：断开清理后重连；</li>
     *     <li>失效且无 {@link #terminalName}（如通过 CardChannel 注入）：断开残留物理连接，
     *     清空状态后抛异常。</li>
     * </ul>
     *
     * @throws UncheckedException 无连接且无终端信息，或重连失败
     */
    private synchronized void ensureConnectedOrReconnect() {
        if (this.connected) {
            return;
        }
        // 连接已失效（或从未连接）
        if (null == this.terminalName) {
            // 无终端信息，无法自动重连；先断开残留物理连接，再清空并抛异常
            if (null != this.card) {
                try {
                    this.card.disconnect(true);
                } catch (IllegalStateException e) {
                    log.warn("清理失效连接时卡片已失效（可能已被物理移除），视为断开成功", e);
                } catch (CardException e) {
                    log.warn("清理失效连接时断开卡片发生异常，已忽略", e);
                }
            }
            this.card = null;
            this.cardChannel = null;
            this.cardTerminal = null;
            throw new UncheckedException(
                    "智能卡未连接且无终端信息，无法执行后续操作。请先 connect(...) 或通过构造注入 CardChannel");
        }
        // 有终端名：清理失效连接并重连
        if (null != this.card) {
            try {
                disconnect();
            } catch (UncheckedException e) {
                log.warn("重连前清理失效连接异常，已忽略", e);
            }
        }
        connect(this.terminalName);
    }

    /**
     * 前置校验：终端中是否有卡片在位。
     *
     * <p>如果 {@link CardTerminal#isCardPresent()} 抛 {@link CardException}，
     * 说明终端或驱动异常，抛出带终端名的明确错误。</p>
     *
     * <p>本方法为纯函数（只操作入参 terminal），不涉及实例状态，无需 synchronized。</p>
     *
     * @param terminal 目标终端
     * @throws UncheckedException 卡片不在位或检查失败
     */
    private static void ensureCardPresent(CardTerminal terminal) {
        boolean present;
        try {
            present = terminal.isCardPresent();
        } catch (IllegalStateException e) {
            throw new UncheckedException(
                    "终端已失效（可能读卡器已被物理移除）: " + terminal.getName(), e);
        } catch (CardException e) {
            throw new UncheckedException(
                    "检查终端卡片在位状态失败: " + terminal.getName(), e);
        }
        if (!present) {
            throw new UncheckedException(
                    "终端 '" + terminal.getName() + "' 中未检测到卡片，请插入卡片后重试");
        }
    }

    /**
     * 批量传输核心实现（使用实例 {@link #autoPull}）。
     *
     * @param channel     目标通道
     * @param hexCommands 命令列表
     * @return 执行结果集合
     */
    public synchronized ApduResults transmitInternal(CardChannel channel, List<String> hexCommands) {
        return transmitInternal(channel, hexCommands, this.autoPull);
    }

    /**
     * 批量传输核心实现（显式指定 autoPull）。
     *
     * <p>供"语义上必须完整拉取"的场景（如 {@link #getEid()}）覆盖实例配置。</p>
     *
     * @param channel     目标通道
     * @param hexCommands 命令列表
     * @param autoPull    本次是否启用自动拉取
     * @return 执行结果集合
     * @throws UncheckedException 通道为 null、未连接、命令非法或传输失败
     */
    public synchronized ApduResults transmitInternal(CardChannel channel, List<String> hexCommands,
                                                     boolean autoPull) {
        if (null == channel) {
            throw new UncheckedException("逻辑通道不能为空");
        }
        ApduResults results = new ApduResults();
        if (hexCommands == null) {
            return results;
        }
        for (String cmd : hexCommands) {
            ApduResult r = transmitOn(channel, cmd, autoPull);
            results.add(r);
            if (!r.isSuccess()) {
                break;
            }
        }
        return results;
    }

    /**
     * 单条原子传输：在指定通道上执行一条命令。
     *
     * <p>执行顺序：hex 规范化 → 判空 → 连接检查 → autoPull 分支。</p>
     *
     * @param channel    目标通道
     * @param hexCommand 十六进制命令
     * @param autoPull   是否启用自动拉取
     * @return 单条执行结果
     * @throws UncheckedException 通道为 null、命令为空、未连接或传输失败
     */
    public synchronized ApduResult transmitOn(CardChannel channel, String hexCommand, boolean autoPull) {
        if (null == channel) {
            throw new UncheckedException("逻辑通道不能为空");
        }
        String normalized = normalizeHex(hexCommand);
        if (null == normalized || normalized.isEmpty()) {
            throw new UncheckedException("APDU 命令不能为空");
        }
        ensureCardConnected();
        return autoPull
                ? transmitWithAutoPull(new ApduResult(), channel, normalized)
                : transmitWithoutAutoPull(channel, normalized);
    }

    /**
     * 81E2 批量传输核心实现：遍历命令列表，逐条执行，失败即中断。
     *
     * @param channel     目标通道
     * @param hexCommands 81E2 命令数据列表
     * @return 执行结果集合
     * @throws UncheckedException 通道为 null
     */
    public synchronized ApduResults transmit81E2Internal(CardChannel channel, List<String> hexCommands) {
        if (null == channel) {
            throw new UncheckedException("逻辑通道不能为空");
        }
        ApduResults results = new ApduResults();
        if (hexCommands == null) {
            return results;
        }
        for (String cmd : hexCommands) {
            ApduResult r = transmit81E2On(channel, cmd);
            results.add(r);
            if (!r.isSuccess()) {
                break;
            }
        }
        return results;
    }

    /**
     * 81E2 单条原子传输：含分包与强制自动拉取。
     *
     * <p><b>通道提示</b>：本方法生成的 APDU 前缀 CLA 固定为 {@code 0x81}（或 {@code 0x91}），
     * 按 ISO/IEC 7816-4 表示<b>逻辑通道 1</b>。方法入口会检查
     * {@code channel.getChannelNumber()}：若不为 1，<b>仅记录 WARN 日志并继续发送</b>，
     * <b>不再抛异常</b>。请调用方确认 PC/SC 层不会把命令静默路由到通道 1。</p>
     *
     * <p>入参校验前置：{@code null} / 空串 / 非 hex / 奇数长度均在第一物理 APDU 前抛出，
     * 避免错误延迟到分包中途，规避卡片遗留不完整 81E2 状态。</p>
     *
     * <p><b>分包序号语义</b>：81E2 分包序号从 0 开始，最后一包前缀为 {@code 81E291}，
     * 中间包为 {@code 81E211}。本方法使用 {@link #toHexByte(int)} 将序号补零为两位十六进制，
     * 避免 {@code Hex.numberToHexString} 返回单字符导致 APDU 长度为奇数。</p>
     *
     * @param channel    目标通道（设计上应为通道 1，非 1 仅 WARN）
     * @param hexCommand 81E2 命令数据（不含 CLA 头）
     * @return 合并后的执行结果
     * @throws UncheckedException 通道为 null、命令为空、非 hex、奇数长度或未连接
     */
    public synchronized ApduResult transmit81E2On(CardChannel channel, String hexCommand) {
        if (null == channel) {
            throw new UncheckedException("逻辑通道不能为空");
        }
        final int channelNumber;
        try {
            channelNumber = channel.getChannelNumber();
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("通道已失效，无法获取通道号以校验 81E2 通道约束", e);
        }
        if (channelNumber != 1) {
            log.warn("81E2 命令设计上要求逻辑通道 1，但当前通道为 {}。" +
                    "仍将继续发送，请确认 PC/SC 层不会把命令静默路由到通道 1", channelNumber);
        }

        String normalized = normalizeHex(hexCommand);
        if (null == normalized || normalized.isEmpty()) {
            throw new UncheckedException("命令不能为空");
        }
        if (!Hex.isHex(normalized)) {
            throw new UncheckedException("81E2 命令不是十六进制数据: " + normalized);
        }
        if (normalized.length() % 2 != 0) {
            throw new UncheckedException("81E2 命令长度不是偶数（hex 字符数需为 2 的倍数）: " + normalized);
        }
        ensureCardConnected();

        ApduResult result = new ApduResult();
        List<ExecuteRecord> records = new ArrayList<>();
        StringBuilder responseData = new StringBuilder();

        List<String> chunks = splitCommand(normalized);
        // 防御：分包数超出单字节序号范围
        if (chunks.size() > MAX_CHUNK_COUNT) {
            throw new UncheckedException(
                    "81E2 命令分包数量超过单字节上限(" + MAX_CHUNK_COUNT + ")，实际=" + chunks.size()
                            + "，请拆分后分批发送");
        }
        for (int i = 0; i < chunks.size(); i++) {
            String prefix = (chunks.size() - 1 == i) ? "81E291" : "81E211";
            String chunk = chunks.get(i);
            String command = prefix
                    + toHexByte(i)
                    + toHexByte(chunk.length() / 2)
                    + chunk + "00";

            ApduResult transmitResult = transmitOn(channel, command, true);
            // 防御：transmitResult.getRecords() 可能为 null（全参构造注入 null 等极端场景）
            if (null != transmitResult.getRecords()) {
                records.addAll(transmitResult.getRecords());
            }
            result.setSw1(transmitResult.getSw1()).setSw2(transmitResult.getSw2());
            if (null != transmitResult.getData()) {
                responseData.append(transmitResult.getData());
            }

            if (!transmitResult.isSuccess()) {
                log.warn("第{}个分包时执行命令{}失败，SW1=0x{},SW2=0x{}",
                        i + 1, command,
                        String.format("%02X", transmitResult.getSw1()),
                        String.format("%02X", transmitResult.getSw2()));
                break;
            }
        }
        return result.setData(responseData.toString()).setRecords(records);
    }

    /**
     * 单条传输、不启用自动拉取。
     *
     * <p>若收到 {@code 6CXX}，说明 Le 错误，本方法会按 SW2 修正 Le 后重发一次原命令，
     * 而不是直接返回失败。</p>
     *
     * @param channel    目标通道
     * @param hexCommand 规范化后的十六进制命令
     * @return 单次响应结果
     * @throws UncheckedException 通道失效或传输失败
     */
    public synchronized ApduResult transmitWithoutAutoPull(CardChannel channel, String hexCommand) {
        CommandAPDU commandApdu = convertToCommandApdu(hexCommand);
        ResponseAPDU responseApdu;
        try {
            responseApdu = channel.transmit(commandApdu);
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("通道已失效（可能卡片已被物理移除），命令" + commandApdu + "传输失败", e);
        } catch (CardException e) {
            if (isPcscServiceStopped(e)) {
                markDisconnected();
                throw new UncheckedException(
                        "Windows Smart Card 服务已停止，请以管理员身份执行 `net start SCardSvr` 后重试", e);
            }
            throw new UncheckedException("命令" + commandApdu + "传输失败", e);
        }

        // 6CXX 修正 Le 后重发一次
        if (responseApdu.getSW1() == SW1_WRONG_LE) {
            String corrected = replaceLe(hexCommand, responseApdu.getSW2());
            if (corrected != null) {
                log.debug("收到 6C{}，使用修正 Le 重发原命令: {} → {}", responseApdu.getSW2(), hexCommand, corrected);
                try {
                    responseApdu = channel.transmit(convertToCommandApdu(corrected));
                } catch (IllegalStateException e) {
                    markDisconnected();
                    throw new UncheckedException("6C 修正 Le 后通道已失效", e);
                } catch (CardException e) {
                    throw new UncheckedException("6C 修正 Le 后重发命令失败", e);
                }
                hexCommand = corrected;
            }
        }

        List<ExecuteRecord> records = new ArrayList<>();
        records.add(new ExecuteRecord(hexCommand, Hex.bytesToHex(responseApdu.getBytes())));

        return new ApduResult()
                .setData(Hex.bytesToHex(responseApdu.getData()))
                .setSw1(responseApdu.getSW1())
                .setSw2(responseApdu.getSW2())
                .setRecords(records);
    }

    /**
     * 单条传输、启用自动拉取（SW1=0x61 走 GET RESPONSE；SW1=0x91 走 RETRIEVE RESPONSE；
     * SW1=0x6C 修正 Le 后重发原命令）。
     *
     * <p><b>修复点</b>：拉取命令的 CLA 不再硬编码，而是通过 {@link #applyChannelToApdu(String, int)}
     * 按 {@code channel.getChannelNumber()} 动态重写，确保拉取命令与实际通道一致。</p>
     *
     * @param result     已有结果（可携带初始 data/records）
     * @param channel    目标通道
     * @param hexCommand 初始命令
     * @return 累积结果
     * @throws UncheckedException 通道失效、传输失败或超过 {@link #MAX_AUTO_PULL_COUNT}
     */
    public synchronized ApduResult transmitWithAutoPull(ApduResult result, CardChannel channel, String hexCommand) {
        StringBuilder responseData = new StringBuilder(result.getData() != null ? result.getData() : "");
        List<ExecuteRecord> records = result.getRecords();
        if (null == records) {
            records = new ArrayList<>();
            result.setRecords(records);
        }

        final int channelNumber;
        try {
            channelNumber = channel.getChannelNumber();
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("通道已失效，无法获取通道号以构造自动拉取命令", e);
        }

        String currentCommand = hexCommand;
        boolean isOriginalCommand = true;

        for (int pullCount = 0; pullCount < MAX_AUTO_PULL_COUNT; pullCount++) {
            CommandAPDU commandApdu = convertToCommandApdu(currentCommand);
            ResponseAPDU responseApdu;
            try {
                responseApdu = channel.transmit(commandApdu);
            } catch (IllegalStateException e) {
                markDisconnected();
                throw new UncheckedException(
                        "通道已失效（可能卡片已被物理移除），命令" + commandApdu
                                + "传输失败（已自动拉取" + pullCount + "次）", e);
            } catch (CardException e) {
                if (isPcscServiceStopped(e)) {
                    markDisconnected();
                    throw new UncheckedException(
                            "Windows Smart Card 服务已停止，请以管理员身份执行 `net start SCardSvr` 后重试", e);
                }
                throw new UncheckedException("命令" + commandApdu + "传输失败", e);
            }

            responseData.append(Hex.bytesToHex(responseApdu.getData()));
            records.add(new ExecuteRecord(currentCommand, Hex.bytesToHex(responseApdu.getBytes())));

            int sw1 = responseApdu.getSW1();
            int sw2 = responseApdu.getSW2();
            result.setData(responseData.toString()).setSw1(sw1).setSw2(sw2);

            if (sw1 == SW1_WRONG_LE) {
                // 6CXX：Le 错误。仅对原始命令修正 Le 后重发；对拉取命令的 6C 直接视为失败，避免无限循环。
                if (isOriginalCommand) {
                    String corrected = replaceLe(currentCommand, sw2);
                    if (corrected != null) {
                        log.debug("收到 6C{}，使用修正 Le 重发原命令: {} → {}", sw2, currentCommand, corrected);
                        currentCommand = corrected;
                        // 仍然标记为原始命令，下一次迭代发送修正后的原命令
                        continue;
                    }
                }
                log.debug("收到 6C{}，但无法修正 Le（命令结构不支持），视为完成", sw2);
                return result;
            }

            if (sw1 == SW1_AUTH_REQUIRED) {
                log.debug("收到 63XX（认证状态未满足），停止自动拉取，SW={}",
                        String.format("%02X%02X", sw1, sw2));
                return result;
            }

            if (sw1 == SW1_RETRIEVE) {
                log.debug("检测到SW1=0x91，执行第{}次自动拉取（通道{}）", pullCount + 1, channelNumber);
                currentCommand = applyChannelToApdu(
                        RETRIEVE_RESPONSE_PREFIX + toHexByte(sw2), channelNumber);
                isOriginalCommand = false;
            } else if (sw1 == SW1_MORE_DATA) {
                log.debug("检测到SW1=0x61，执行第{}次自动拉取（通道{}）", pullCount + 1, channelNumber);
                currentCommand = applyChannelToApdu(
                        GET_RESPONSE_PREFIX + toHexByte(sw2), channelNumber);
                isOriginalCommand = false;
            } else {
                log.debug("命令执行完成，SW1=0x{}, SW2=0x{}",
                        Integer.toHexString(sw1), Integer.toHexString(sw2));
                return result;
            }
        }
        throw new UncheckedException("自动拉取次数超过限制(" + MAX_AUTO_PULL_COUNT + ")，可能存在异常");
    }

    /**
     * 将 APDU 命令的 Le 替换为指定值（短格式、单字节 Le）。
     *
     * <p>若命令长度不足以包含 Le，或结构不是短格式 case 2/case 4，返回 {@code null}，
     * 表示无法安全修正。</p>
     */
    private static String replaceLe(String hexCommand, int newLe) {
        if (null == hexCommand || hexCommand.length() < 10) {
            // 至少 CLA INS P1 P2 Le = 5 字节 = 10 个 hex 字符
            return null;
        }
        String normalized = normalizeHex(hexCommand);
        if (normalized == null || normalized.length() < 10 || normalized.length() % 2 != 0) {
            return null;
        }
        // 短格式下 Le 为最后一个字节；将最后两个 hex 字符替换为 newLe
        return normalized.substring(0, normalized.length() - 2) + toHexByte(newLe);
    }

    /**
     * 新建通道 → 选 ISR（强制拉取）→ 执行动作 → 关闭。
     *
     * <p>前置通过 {@link #ensureConnectedOrReconnect()} 确保连接有效；
     * 无连接且无终端信息时抛异常，不会尝试 {@code connect(null)}。</p>
     *
     * <p><b>通道语义</b>：本方法打开的新通道通常为通道 1。ISR 选择命令通过
     * {@link #applyChannelToApdu(String, int)} 按新通道号动态重写 CLA，
     * 保证与后续命令在同一通道上执行。</p>
     *
     * @param action            通道上的具体操作
     * @param errorLogMsg       异常日志
     * @param errorExceptionMsg 异常消息
     * @param <T>               返回值类型
     * @return 动作返回值
     * @throws UncheckedException 无连接且无终端信息、选 ISR 失败、通道操作失败或卡已失效
     */
    public synchronized <T> T executeWithNewLogicalChannelAndSelectIsr(ChannelAction<T> action,
                                                                       String errorLogMsg, String errorExceptionMsg) {
        CardChannel channel = null;
        try {
            ensureConnectedOrReconnect();
            channel = this.card.openLogicalChannel();

            final int channelNumber = channel.getChannelNumber();
            String selectCommand = applyChannelToApdu(COMMAND_SELECT_ISR, channelNumber);
            ApduResult selectResult = transmitOn(channel, selectCommand, true);
            if (!selectResult.isSuccess()) {
                throw new UncheckedException(
                        "在新逻辑通道上选择ISR应用失败:" + selectResult.getData() + ",sw:" + selectResult.swHex());
            }
            return action.execute(channel);
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("卡片已失效（可能已被物理移除），无法执行新通道操作", e);
        } catch (CardException e) {
            log.error(errorLogMsg, e);
            throw new UncheckedException(errorExceptionMsg, e);
        } finally {
            closeChannelQuietly(channel);
        }
    }

    /**
     * 新建通道 → 执行动作（不选 ISR）→ 关闭。
     *
     * <p>前置通过 {@link #ensureConnectedOrReconnect()} 确保连接有效；
     * 无连接且无终端信息时抛异常，不会尝试 {@code connect(null)}。</p>
     *
     * @param action            通道上的具体操作
     * @param errorLogMsg       异常日志
     * @param errorExceptionMsg 异常消息
     * @param <T>               返回值类型
     * @return 动作返回值
     * @throws UncheckedException 无连接且无终端信息、通道操作失败或卡已失效
     */
    private synchronized <T> T executeWithNewLogicalChannel(ChannelAction<T> action,
                                                            String errorLogMsg, String errorExceptionMsg) {
        CardChannel channel = null;
        try {
            ensureConnectedOrReconnect();
            channel = this.card.openLogicalChannel();
            return action.execute(channel);
        } catch (IllegalStateException e) {
            markDisconnected();
            throw new UncheckedException("卡片已失效（可能已被物理移除），无法执行新通道操作", e);
        } catch (CardException e) {
            log.error(errorLogMsg, e);
            throw new UncheckedException(errorExceptionMsg, e);
        } finally {
            closeChannelQuietly(channel);
        }
    }


    /**
     * 通道操作的函数式接口（允许抛 {@link CardException}）。
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    private interface ChannelAction<T> {
        /**
         * 在给定通道上执行操作。
         *
         * @param channel 目标通道
         * @return 操作返回值
         * @throws CardException 底层通讯异常
         */
        T execute(CardChannel channel) throws CardException;
    }

    /**
     * APDU 批量结果容器，继承 {@link ArrayList}{@code <ApduResult>}。
     *
     * <p>关键读写方法均加 {@code synchronized}；如用户在多线程中遍历，
     * 建议配合 {@code Collections.synchronizedList(...)} 使用。</p>
     */
    public static class ApduResults extends ArrayList<ApduResult> {

        /**
         * 序列化版本号
         */
        private static final long serialVersionUID = 1L;

        /**
         * 追加一条结果。
         *
         * @param apduResult 结果对象
         * @return 是否添加成功
         */
        @Override
        public synchronized boolean add(ApduResult apduResult) {
            return super.add(apduResult);
        }

        /**
         * 批量追加结果。
         *
         * @param c 结果集合
         * @return 是否添加成功
         */
        @Override
        public synchronized boolean addAll(Collection<? extends ApduResult> c) {
            return super.addAll(c);
        }

        /**
         * 第一条结果。
         *
         * @return 第一条；空集合返回 {@code null}
         */
        public synchronized ApduResult first() {
            return isEmpty() ? null : get(0);
        }

        /**
         * 最后一条结果。
         *
         * @return 最后一条；空集合返回 {@code null}
         */
        public synchronized ApduResult last() {
            return isEmpty() ? null : get(size() - 1);
        }

        /**
         * 是否全部成功。
         *
         * @return 全部成功返回 {@code true}；空集合返回 {@code true}
         */
        public synchronized boolean isAllSuccess() {
            return stream().allMatch(ApduResult::isSuccess);
        }

        /**
         * 是否存在失败项。
         *
         * @return 任一项失败返回 {@code true}；空集合返回 {@code false}
         */
        public synchronized boolean isAnyFailure() {
            return stream().anyMatch(r -> !r.isSuccess());
        }
    }

    /**
     * 单条 APDU 执行结果。
     *
     * <p>包含响应数据、状态字（SW1/SW2）、以及本次命令链上的所有请求/响应记录。</p>
     *
     * <p>注意：Lombok 生成的 getter/setter 未加 {@code synchronized}，
     * 并发访问请在外层保护；本类只对 {@code addRecord / addRecords / isSuccess / swHex / toString}
     * 等关键方法提供同步。</p>
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ApduResult implements Serializable {

        /**
         * 序列化版本号
         */
        private static final long serialVersionUID = 1L;

        /**
         * 执行历史记录（分包/多轮拉取时有多条）
         */
        private List<ExecuteRecord> records = new ArrayList<>();

        /**
         * 拼接后的响应数据（十六进制字符串，不含状态字）
         */
        private String data;

        /**
         * 状态字 SW1
         */
        private int sw1;

        /**
         * 状态字 SW2
         */
        private int sw2;

        /**
         * 追加一条执行记录。
         *
         * <p>若 {@code records} 字段被全参构造传入 {@code null}，本方法会懒初始化，避免 NPE。</p>
         *
         * @param record 记录对象
         * @return 当前实例（链式）
         */
        public synchronized ApduResult addRecord(ExecuteRecord record) {
            if (null == this.records) {
                this.records = new ArrayList<>();
            }
            this.records.add(record);
            return this;
        }

        /**
         * 批量追加执行记录。
         *
         * <p>入参为 {@code null} 或空集合时直接返回，避免对 {@code null} 调用 {@code addAll} 抛 NPE。</p>
         *
         * @param records 记录列表
         * @return 当前实例（链式）
         */
        public synchronized ApduResult addRecords(List<ExecuteRecord> records) {
            if (null == records || records.isEmpty()) {
                return this;
            }
            if (null == this.records) {
                this.records = new ArrayList<>();
            }
            this.records.addAll(records);
            return this;
        }

        /**
         * 判断命令是否成功。
         *
         * <p>SW1=0x90 正常结束；SW1=0x61/0x91 表示后续需继续拉取，仍视为成功。</p>
         *
         * @return 是否成功
         */
        public synchronized boolean isSuccess() {
            return this.sw1 == SW1_SUCCESS
                    || this.sw1 == SW1_MORE_DATA
                    || this.sw1 == SW1_RETRIEVE;
        }

        /**
         * 状态字格式化为 4 位大写十六进制字符串。
         *
         * @return 形如 {@code "9000"} 的字符串
         */
        public synchronized String swHex() {
            return String.format("%02X%02X", this.sw1, this.sw2);
        }

        /**
         * 序列化友好的字符串表示。
         *
         * @return 包含 records、data、sw 的字符串
         */
        @Override
        public synchronized String toString() {
            return "ApduResult{" + "records=" + records + ", data=" + data + ", sw=0x" + swHex() + "}";
        }
    }

    /**
     * 单次 APDU 请求 / 响应记录，用于审计与排错。
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ExecuteRecord implements Serializable {

        /**
         * 序列化版本号
         */
        private static final long serialVersionUID = 1L;

        /**
         * 请求命令（十六进制，含 CLA/INS/P1/P2/LC/DATA/LE）
         */
        private String request;

        /**
         * 响应字节（十六进制，含状态字）
         */
        private String response;
    }
}