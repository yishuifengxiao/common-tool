package com.yishuifengxiao.common.tool.io;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通用 Excel 读取工具类。
 *
 * <h2>API 一致性承诺</h2>
 * <ul>
 *   <li>返回的 {@link ParseResult} 结构、方法、行为在同步 / 异步模式下<b>完全相同</b>；</li>
 *   <li>表头校验失败时两种模式行为完全一致：<b>立即返回、不读正文、不回调、不提交任务</b>；</li>
 *   <li>正文读取均为<b>逐行流式回调</b>——每读一行 → {@link RowCallback#onRow} → 立即释放内存；</li>
 *   <li>正文行数由调用方在回调中自行统计（本工具类不内置行数统计）。</li>
 * </ul>
 *
 * <h2>async 参数说明</h2>
 * <table border="1">
 *   <caption>两种模式的关键差异</caption>
 *   <tr><th>项目</th><th>async = false（默认）</th><th>async = true</th></tr>
 *   <tr><td>表头校验</td><td>当前线程同步</td><td>当前线程同步</td></tr>
 *   <tr><td>表头失败</td><td>立即返回失败</td><td>立即返回失败</td></tr>
 *   <tr><td>正文读取线程</td><td>当前线程</td><td>executor 线程池</td></tr>
 *   <tr><td>回调方式</td><td>流式（逐行回调）</td><td>流式（逐行回调）</td></tr>
 *   <tr><td>parse(...) 返回时机</td>
 *       <td><b>回调全部执行完后</b>才返回</td>
 *       <td><b>表头通过后立即返回</b>，正文在后台进行</td></tr>
 *   <tr><td>parse 返回时数据是否处理完</td><td>是</td><td><b>可能还在处理中</b></td></tr>
 *   <tr><td>正文异常感知</td><td>通过 ParseResult.fail 返回</td>
 *       <td>通过 {@link HeaderConfig#getOnBodyComplete()} 回调（同时打印到 stderr）</td></tr>
 * </table>
 *
 * <h2>使用建议</h2>
 * <ul>
 *   <li><b>小型 / 中型文件</b>（万级以内）：用同步模式，最简单；</li>
 *   <li><b>大型文件</b>（百万行以内）：
 *     <ul>
 *       <li>UI 不阻塞 / 批量任务并行 → 用异步模式，回调中流式处理，主线程立即可继续；</li>
 *       <li>需要知道"何时处理完" → 用同步模式，或在异步模式下设置
 *           {@link HeaderConfig#setOnBodyComplete} 感知完成；</li>
 *       <li>需要感知正文异常 → 用同步模式，或在异步模式下设置 {@link HeaderConfig#setOnBodyComplete}；</li>
 *     </ul>
 *   </li>
 *   <li><b>超大文件</b>（千万行级）：当前基于 {@link WorkbookFactory} 的整表加载可能内存不足，
 *       建议改用 POI 的 {@code XSSFReader} 事件模型（本工具类暂不支持）。</li>
 * </ul>
 *
 * <h2>语言模式</h2>
 * <p>根据 {@link ColumnDef} 中用户实际提供的语言自动判定：</p>
 * <ul>
 *   <li>全双语 → 中英双套校验，任一通过即成功；</li>
 *   <li>全中文 → 只做中文校验；</li>
 *   <li>全英文 → 只做英文校验；</li>
 *   <li>混合 → 抛 {@link IllegalArgumentException}。</li>
 * </ul>
 *
 * <h2>关键约定</h2>
 * <ol>
 *   <li>{@code rowData} 为 {@link LinkedHashMap}，<b>key 为列在 columns 中的下标（0..N-1）</b>，
 *       迭代顺序与 columns 完全一致；</li>
 *   <li>{@link CellData#getExcelIdx()} 是该列在 Excel 文件中的<b>实际物理列索引</b>；</li>
 *   <li>{@link CellData#isValid()} / {@link CellData#getValidationErrorZh()} /
 *       {@link CellData#getValidationErrorEn()} 携带非空校验结果与中英双语错误消息；</li>
 *   <li>{@link RowCallback#onRow(int, LinkedHashMap)} 的 {@code rowIndex} 为 Excel
 *       <b>物理行号</b>（0 开始），从表头行下一行起按物理位置递增；
 *       空白行（含未创建的间隙行）<b>不跳过</b>，照常回调。</li>
 * </ol>
 *
 * <h2>Workbook 生命周期</h2>
 * <ul>
 *   <li>{@code parse(File/byte[], ...)}：内部创建的 Workbook 与文件流均由工具类负责关闭
 *       （同步模式在 {@code parse} 返回前，异步模式在正文读取完全结束时）；</li>
 *   <li>{@code parse(InputStream, ...)}：内部创建的 Workbook 由工具类负责关闭；
 *       传入的流由调用方负责关闭（异步模式下请等正文读取完成后再关闭）；</li>
 *   <li>{@code parse(Workbook, ...)}：传入的 Workbook 由调用方负责关闭。
 *       <b>异步模式下请等正文读取完成后再关闭</b>（可设置
 *       {@link HeaderConfig#setOnBodyComplete} 或在回调中控制）。</li>
 * </ul>
 */
public final class ExcelReader {

    /**
     * 私有构造：工具类禁止实例化。
     */
    private ExcelReader() {
    }

    // ============================================================
    // 对外类型
    // ============================================================

    /**
     * 表头语言。
     */
    @Getter
    public enum HeaderLanguage {

        /**
         * 中文表头。
         */
        ZH("中文"),

        /**
         * 英文表头。
         */
        EN("英文");

        private final String desc;

        HeaderLanguage(String desc) {
            this.desc = desc;
        }
    }

    /**
     * 列定义：中文名、英文名、是否允许为空。
     *
     * <p><b>语言填写规则：</b></p>
     * <ul>
     *   <li>只填中文 → 只参与中文校验；</li>
     *   <li>只填英文 → 只参与英文校验；</li>
     *   <li>中英都填 → 参与中英双套校验；</li>
     *   <li>都为空 → 构造抛异常；</li>
     *   <li>同一 columns 列表中<b>不允许</b>混合"只中文"与"只英文"。</li>
     * </ul>
     */
    @Getter
    public static final class ColumnDef {

        /**
         * 中文列名（已 trim；未提供时等于 englishName）。
         */
        private final String chineseName;

        /**
         * 英文列名（已 trim；未提供时等于 chineseName）。
         */
        private final String englishName;

        /**
         * 是否允许该列在数据行中为空。{@code false} 表示必填。
         */
        private final boolean nullable;

        /**
         * 用户是否实际提供了中文名。
         */
        private final boolean chineseProvided;

        /**
         * 用户是否实际提供了英文名。
         */
        private final boolean englishProvided;

        /**
         * 构造列定义。
         *
         * @throws IllegalArgumentException 中英文名都为空时抛出
         */
        public ColumnDef(String chineseName, String englishName, boolean nullable) {
            String zh = chineseName == null ? "" : chineseName.trim();
            String en = englishName == null ? "" : englishName.trim();
            if (zh.isEmpty() && en.isEmpty()) {
                throw new IllegalArgumentException(
                        "列的中英文名不能都为空 / Column chinese and english names cannot both be empty");
            }
            this.chineseProvided = !zh.isEmpty();
            this.englishProvided = !en.isEmpty();
            if (zh.isEmpty()) {
                zh = en;
            }
            if (en.isEmpty()) {
                en = zh;
            }
            this.chineseName = zh;
            this.englishName = en;
            this.nullable = nullable;
        }

        // -------------------- 中英双语 --------------------

        /**
         * 中英双语：通用工厂。
         */
        public static ColumnDef of(String chineseName, String englishName, boolean nullable) {
            return new ColumnDef(chineseName, englishName, nullable);
        }

        /**
         * 中英双语：必填。
         */
        public static ColumnDef required(String chineseName, String englishName) {
            return new ColumnDef(chineseName, englishName, false);
        }

        /**
         * 中英双语：可空。
         */
        public static ColumnDef nullable(String chineseName, String englishName) {
            return new ColumnDef(chineseName, englishName, true);
        }

        // -------------------- 仅中文 --------------------

        /**
         * 仅中文：通用工厂。
         */
        public static ColumnDef ofZh(String chineseName, boolean nullable) {
            return new ColumnDef(chineseName, null, nullable);
        }

        /**
         * 仅中文：必填。
         */
        public static ColumnDef requiredZh(String chineseName) {
            return new ColumnDef(chineseName, null, false);
        }

        /**
         * 仅中文：可空。
         */
        public static ColumnDef nullableZh(String chineseName) {
            return new ColumnDef(chineseName, null, true);
        }

        // -------------------- 仅英文 --------------------

        /**
         * 仅英文：通用工厂。
         */
        public static ColumnDef ofEn(String englishName, boolean nullable) {
            return new ColumnDef(null, englishName, nullable);
        }

        /**
         * 仅英文：必填。
         */
        public static ColumnDef requiredEn(String englishName) {
            return new ColumnDef(null, englishName, false);
        }

        /**
         * 仅英文：可空。
         */
        public static ColumnDef nullableEn(String englishName) {
            return new ColumnDef(null, englishName, true);
        }

        /**
         * 按语言取列名。
         */
        public String name(HeaderLanguage lang) {
            return lang == HeaderLanguage.EN ? englishName : chineseName;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(chineseName).append("/").append(englishName);
            if (chineseProvided && !englishProvided) {
                sb.append("(only zh)");
            } else if (!chineseProvided && englishProvided) {
                sb.append("(only en)");
            }
            sb.append(nullable ? "(nullable)" : "(required)");
            return sb.toString();
        }
    }

    /**
     * 单元格数据：文本值 + 列定义 + Excel 实际列索引 + 非空校验结果 + 中英双语错误消息。
     */
    @Getter
    public static final class CellData {

        /**
         * 单元格文本（已剔除首尾不可见字符）；空单元格返回 {@code ""}，永不 null。
         */
        private final String value;

        /**
         * 该单元格对应的列定义（不会为 null）。
         */
        private final ColumnDef column;

        /**
         * 该列在 Excel 文件中的实际物理列索引（0 开始）。
         */
        private final int excelIdx;

        /**
         * 数据校验结果：{@code true} 通过；{@code false} 必填列为空或单元格为 Excel 错误值。
         */
        private final boolean valid;

        /**
         * 校验失败时的中文错误消息；通过时为 null。
         */
        private final String validationErrorZh;

        /**
         * 校验失败时的英文错误消息；通过时为 null。
         */
        private final String validationErrorEn;

        CellData(String value, ColumnDef column, int excelIdx,
                 boolean valid, String validationErrorZh, String validationErrorEn) {
            this.value = value == null ? "" : value;
            this.column = column;
            this.excelIdx = excelIdx;
            this.valid = valid;
            this.validationErrorZh = validationErrorZh;
            this.validationErrorEn = validationErrorEn;
        }

        /**
         * 是否校验未通过（必填但为空，或单元格为 Excel 错误值）。等价于 {@code !isValid()}。
         */
        public boolean isMissingRequired() {
            return !valid;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * 正文行回调。
     *
     * <p><b>rowData 约定：</b>key = 列在 columns 中的下标（0..N-1），迭代顺序与 columns 完全一致；
     * value = {@link CellData}。</p>
     *
     * <p><b>触发时机：</b>仅在表头校验通过之后；表头失败时一次都不会调用。</p>
     *
     * <p><b>调用方式（关键）：</b></p>
     * <ul>
     *   <li><b>流式逐行回调</b>：每读一行 → 立即触发本方法 → 方法返回后 rowData 即可被 GC；</li>
     *   <li><b>同步模式</b>：在调用方线程执行，{@code parse(...)} 返回时已全部执行完；</li>
     *   <li><b>异步模式</b>：在 {@link HeaderConfig#getExecutor()} 指定的线程池执行，
     *       {@code parse(...)} <b>不等待</b>本方法执行完就返回。
     *       实现时需保证线程安全，且<b>不要假设调用顺序与 main 线程的时序</b>。</li>
     * </ul>
     */
    @FunctionalInterface
    public interface RowCallback {

        /**
         * 处理一行数据。
         *
         * @param rowIndex 当前行的 Excel 物理行号（0 开始）：从表头行下一行起按物理位置递增；
         *                 空白行（含从未创建的间隙行）同样触发本回调，不跳过
         * @param rowData  当前行数据（迭代顺序与 columns 完全一致）
         */
        void onRow(int rowIndex, LinkedHashMap<Integer, CellData> rowData);
    }

    /**
     * 异步正文读取完成回调。
     *
     * <p>仅在异步模式（{@link HeaderConfig#setAsync(boolean)}）下触发：
     * 正文全部读取完成或发生异常时调用一次，用于替代"只看 stderr"的异常感知方式。</p>
     *
     * <p><b>注意：</b>本回调在异步线程中执行，抛出异常不会影响主流程（仅打印到 stderr）。</p>
     */
    @FunctionalInterface
    public interface BodyCompleteListener {

        /**
         * 正文读取结束。
         *
         * @param success 正文是否全部成功读取
         * @param error   失败时的异常；成功时为 {@code null}
         */
        void onComplete(boolean success, Throwable error);
    }

    /**
     * 表头单元格信息：Excel 物理列索引 + 表头文本 + 匹配到的列定义。
     */
    @Getter
    public static final class HeaderCellInfo {

        /**
         * Excel 中的实际物理列索引（0 开始）。
         */
        private final int columnIndex;

        /**
         * 表头单元格文本（已剔除首尾不可见字符）。
         */
        private final String value;

        /**
         * 匹配到的列定义；未匹配上（失败场景）为 null。
         */
        private final ColumnDef column;

        HeaderCellInfo(int columnIndex, String value, ColumnDef column) {
            this.columnIndex = columnIndex;
            this.value = value;
            this.column = column;
        }

        @Override
        public String toString() {
            return columnIndex + ":" + value;
        }
    }

    /**
     * 解析结果。同步 / 异步模式下结构、方法、行为完全一致。
     *
     * <p>包含：表头校验结果（成功 / 失败）、命中语言、表头映射、中英双语简略/详细消息。</p>
     *
     * <p><b>不提供行数 / 异步 Future / body 结果</b>——行数由调用方在回调中自行统计；
     * 异步完成感知通过 {@link HeaderConfig#setOnBodyComplete} 回调。</p>
     */
    @Getter
    public static final class ParseResult {

        /**
         * 是否成功（表头 + 正文读取无异常）。异步模式下成功仅代表表头通过。
         */
        private final boolean success;

        /**
         * 详细版错误消息（中文）。
         */
        private final String messageZh;

        /**
         * 详细版错误消息（英文）。
         */
        private final String messageEn;

        /**
         * 简写版错误消息（中文）。
         */
        private final String messageZhShort;

        /**
         * 简写版错误消息（英文）。
         */
        private final String messageEnShort;

        /**
         * 命中的表头语言（任何失败场景均为 null，包括表头失败、提交失败、正文失败）。
         */
        private final HeaderLanguage language;

        /**
         * 按 columns 顺序排列的表头映射（索引 i 对应 columns.get(i)）。
         */
        private final List<HeaderCellInfo> headers;

        private ParseResult(boolean success,
                            String messageZh, String messageEn,
                            String messageZhShort, String messageEnShort,
                            HeaderLanguage language,
                            List<HeaderCellInfo> headers) {
            this.success = success;
            this.messageZh = messageZh;
            this.messageEn = messageEn;
            this.messageZhShort = messageZhShort;
            this.messageEnShort = messageEnShort;
            this.language = language;
            this.headers = headers == null
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<>(headers));
        }

        /**
         * 内部工厂：成功。
         */
        static ParseResult ok(HeaderLanguage language, List<HeaderCellInfo> headers) {
            return new ParseResult(true, "解析成功", "Parse succeeded",
                    "解析成功", "Parse succeeded",
                    language, headers);
        }

        /**
         * 内部工厂：失败（简写 ≠ 详细）。
         */
        static ParseResult fail(String zhDetail, String enDetail,
                                String zhShort, String enShort,
                                HeaderLanguage language, List<HeaderCellInfo> headers) {
            return new ParseResult(false, zhDetail, enDetail, zhShort, enShort,
                    language, headers);
        }

        /**
         * 内部工厂：失败（简写 = 详细）。
         */
        static ParseResult fail(String zhDetail, String enDetail,
                                HeaderLanguage language, List<HeaderCellInfo> headers) {
            return fail(zhDetail, enDetail, zhDetail, enDetail, language, headers);
        }

        /**
         * 是否失败。
         */
        public boolean isFailed() {
            return !success;
        }

        @Override
        public String toString() {
            return "ParseResult{success=" + success
                    + ", language=" + language
                    + ", messageZhShort='" + messageZhShort + '\''
                    + ", messageEnShort='" + messageEnShort + '\''
                    + ", messageZh='" + messageZh + '\''
                    + ", messageEn='" + messageEn + '\'' + '}';
        }
    }

    /**
     * 表头与解析配置。所有开关相互独立。
     *
     * <p><b>注意：</b>{@link #setAsync(boolean)} 只影响<b>正文的读取线程和 parse 返回时机</b>，
     * 不影响返回的 {@link ParseResult} 结构。详见类注释的"async 参数说明"。</p>
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class HeaderConfig {

        /**
         * 列定义列表（setter 做防御性拷贝，getter 返回不可变视图）。
         */
        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private List<ColumnDef> columns = Collections.emptyList();

        /**
         * 是否忽略表头行中的空白列（中间空列），默认 true。
         */
        private boolean ignoreBlankColumns = true;

        /**
         * 表头顺序是否必须与 columns 一致，默认 true。
         */
        private boolean orderRequired = true;

        /**
         * 是否允许 columns 之外的额外表头，默认 false。
         */
        private boolean allowExtraHeaders = false;

        /**
         * 表头比对是否忽略大小写，默认 true。
         */
        private boolean ignoreCase = true;

        /**
         * 表头比对是否忽略不可见字符，默认 true。
         */
        private boolean ignoreInvisibleChars = true;

        /**
         * 表头比对时额外忽略的字符串列表（整体子串匹配；setter 做防御性拷贝）。
         */
        @Setter(AccessLevel.NONE)
        private List<String> ignoredChars = Collections.emptyList();

        /**
         * 表头所在行（0 开始），setter 做 {@code Math.max(0, ...)} 兜底。
         */
        @Setter(AccessLevel.NONE)
        private int headerRowIndex = 0;

        /**
         * 工作表索引（0 开始），setter 做 {@code Math.max(0, ...)} 兜底。
         *
         * <p>超过文件实际工作表数量时解析直接失败（不再回退到最后一张表）。</p>
         */
        @Setter(AccessLevel.NONE)
        private int sheetIndex = 0;

        /**
         * 正文读取模式开关，默认 false。
         *
         * <ul>
         *   <li>{@code false}：同步模式——正文在调用方线程逐行流式回调，
         *       {@code parse(...)} 返回时回调已全部执行完；</li>
         *   <li>{@code true}：异步模式——表头通过后<b>{@code parse(...)} 立即返回</b>，
         *       正文在 {@link #getExecutor()} 指定的线程池<b>逐行流式回调</b>。
         *       适合大文件、UI 不阻塞场景。</li>
         * </ul>
         *
         * <p>两种模式下 {@link ParseResult} 结构相同；表头失败时行为也完全一致。</p>
         */
        private boolean async = false;

        /**
         * 异步模式使用的线程池；未设置时使用工具类内置的有界守护线程池
         * （线程数与队列容量均有上限，队列满时拒绝任务）。
         */
        private Executor executor;

        /**
         * 异步正文读取完成回调；未设置时不通知。仅异步模式生效。
         */
        @Setter(AccessLevel.NONE)
        private BodyCompleteListener onBodyComplete;

        /**
         * 列定义列表（不可变视图）。
         */
        public List<ColumnDef> getColumns() {
            return Collections.unmodifiableList(columns);
        }

        /**
         * 设置列定义列表（防御性拷贝）。
         */
        public HeaderConfig setColumns(List<ColumnDef> columns) {
            this.columns = columns == null ? Collections.emptyList() : new ArrayList<>(columns);
            return this;
        }

        /**
         * 设置列定义列表（可变参数，防御性拷贝）。
         */
        public HeaderConfig setColumns(ColumnDef... columns) {
            this.columns = columns == null
                    ? Collections.emptyList()
                    : new ArrayList<>(Arrays.asList(columns));
            return this;
        }

        /**
         * 设置异步正文读取完成回调（仅异步模式生效）。
         */
        public HeaderConfig setOnBodyComplete(BodyCompleteListener onBodyComplete) {
            this.onBodyComplete = onBodyComplete;
            return this;
        }

        /**
         * 设置表头比对时额外忽略的字符串列表。
         */
        public HeaderConfig setIgnoredChars(List<String> ignoredChars) {
            if (ignoredChars == null || ignoredChars.isEmpty()) {
                this.ignoredChars = Collections.emptyList();
                return this;
            }
            List<String> copy = new ArrayList<>(ignoredChars.size());
            Set<String> seen = new LinkedHashSet<>();
            for (String s : ignoredChars) {
                if (s != null && !s.isEmpty() && seen.add(s)) {
                    copy.add(s);
                }
            }
            this.ignoredChars = Collections.unmodifiableList(copy);
            return this;
        }

        /**
         * 设置表头行索引（0 开始），负数修正为 0。
         */
        public HeaderConfig setHeaderRowIndex(int headerRowIndex) {
            this.headerRowIndex = Math.max(0, headerRowIndex);
            return this;
        }

        /**
         * 设置工作表索引（0 开始），负数修正为 0。
         */
        public HeaderConfig setSheetIndex(int sheetIndex) {
            this.sheetIndex = Math.max(0, sheetIndex);
            return this;
        }

        /**
         * @deprecated 请使用 {@link #setAllowExtraHeaders(boolean)}。
         */
        @Deprecated
        public HeaderConfig setStrict(boolean strict) {
            this.allowExtraHeaders = !strict;
            return this;
        }

        /**
         * @deprecated 请使用 {@link #isAllowExtraHeaders()}。
         */
        @Deprecated
        public boolean isStrict() {
            return !allowExtraHeaders;
        }
    }

    // ============================================================
    // 解析入口
    // ============================================================

    /**
     * 从文件解析（自动识别 .xls / .xlsx）。
     *
     * <p>工具类内部打开的文件流会在解析完全结束后关闭：同步模式为 {@code parse} 返回前，
     * 异步模式为正文读取结束（或任务提交失败）之后。</p>
     */
    public static ParseResult parse(File file, HeaderConfig config, RowCallback callback) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file 不能为空 / file must not be null");
        }
        HeaderConfig cfg = config == null ? new HeaderConfig() : config;
        // 纯配置校验前移到打开文件之前：配置错误不应等到文件全量加载后才暴露
        LanguageMode mode = validateConfigAndDetectMode(cfg);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        return parseStream(in, cfg, callback, true, mode);
    }

    /**
     * 从字节数组解析。
     *
     * <p>内部创建的流在解析完全结束后关闭（语义同 {@link #parse(File, HeaderConfig, RowCallback)}）。</p>
     */
    public static ParseResult parse(byte[] bytes, HeaderConfig config, RowCallback callback) throws IOException {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes 不能为空 / bytes must not be null");
        }
        HeaderConfig cfg = config == null ? new HeaderConfig() : config;
        LanguageMode mode = validateConfigAndDetectMode(cfg);
        return parseStream(new ByteArrayInputStream(bytes), cfg, callback, true, mode);
    }

    /**
     * 从输入流解析（流由调用方负责关闭）。
     *
     * <p><b>异步模式</b>下请等正文读取完成（可设置
     * {@link HeaderConfig#setOnBodyComplete}）后再关闭流。</p>
     */
    public static ParseResult parse(InputStream in, HeaderConfig config, RowCallback callback) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("InputStream 不能为空 / InputStream must not be null");
        }
        HeaderConfig cfg = config == null ? new HeaderConfig() : config;
        LanguageMode mode = validateConfigAndDetectMode(cfg);
        return parseStream(in, cfg, callback, false, mode);
    }

    /**
     * 从输入流创建 Workbook 并进入核心流程。
     *
     * @param ownsStream 是否由工具类负责关闭该流（File / byte[] 输入为 true，调用方传入的流为 false）
     * @param mode       Phase 0 配置校验中已判定的语言模式
     */
    private static ParseResult parseStream(InputStream in, HeaderConfig cfg,
                                           RowCallback callback, boolean ownsStream,
                                           LanguageMode mode) throws IOException {
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(in);
        } catch (Exception e) {
            if (ownsStream) {
                closeQuietly(in);
            }
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return ParseResult.fail("文件解析失败：" + msg,
                    "Failed to parse file: " + msg, null, null);
        } catch (Error e) {
            if (ownsStream) {
                closeQuietly(in);
            }
            throw e;
        }
        return parseWorkbookInternal(workbook, cfg, callback, true, ownsStream ? in : null, mode);
    }

    /**
     * 从 Workbook 解析（不负责关闭）。
     *
     * <p><b>异步模式下</b>：调用方必须等正文读取完成后再关闭 Workbook，
     * 否则异步读取会失败。可设置 {@link HeaderConfig#setOnBodyComplete} 感知完成。</p>
     */
    public static ParseResult parse(Workbook workbook, HeaderConfig config, RowCallback callback) {
        if (workbook == null) {
            throw new IllegalArgumentException("Workbook 不能为空 / Workbook must not be null");
        }
        HeaderConfig cfg = config == null ? new HeaderConfig() : config;
        LanguageMode mode = validateConfigAndDetectMode(cfg);
        return parseWorkbookInternal(workbook, cfg, callback, false, null, mode);
    }

    // ============================================================
    // 核心流程
    // ============================================================

    /**
     * Phase 0：纯配置校验（columns 非空、无 null 元素），并判定语言模式。
     *
     * <p>不依赖任何文件内容，必须在打开 / 全量加载文件之前完成：
     * 配置错误快速失败，且异常类型与文件内容无关。</p>
     */
    private static LanguageMode validateConfigAndDetectMode(HeaderConfig cfg) {
        List<ColumnDef> columns = cfg.getColumns();
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException(
                    "必须至少提供一个列定义 / At least one column definition is required");
        }
        for (ColumnDef c : columns) {
            if (c == null) {
                throw new IllegalArgumentException(
                        "columns 中不允许包含 null 元素 / columns must not contain null elements");
            }
        }
        return detectLanguageMode(columns);
    }

    private static ParseResult parseWorkbookInternal(Workbook workbook, HeaderConfig cfg,
                                                     RowCallback callback, boolean ownsWorkbook,
                                                     InputStream ownedStream, LanguageMode mode) {
        // ---------- Phase 1：表头校验（始终同步） ----------
        HeaderCheckResult headerCheck;
        try {
            headerCheck = checkHeader(workbook, cfg, mode);
        } catch (RuntimeException | Error e) {
            release(ownsWorkbook, workbook, ownedStream);
            throw e;
        }

        // ---------- Phase 2：表头失败 → 立即返回 ----------
        if (!headerCheck.ok) {
            release(ownsWorkbook, workbook, ownedStream);
            return ParseResult.fail(
                    headerCheck.messageZhDetail, headerCheck.messageEnDetail,
                    headerCheck.messageZhShort, headerCheck.messageEnShort,
                    null, headerCheck.rawHeaders);
        }

        // ---------- Phase 3：正文读取 ----------
        Sheet sheet = headerCheck.sheet;
        List<HeaderCellInfo> finalHeaders = headerCheck.finalHeaders;
        HeaderLanguage language = headerCheck.language;
        int headerRowIndex = cfg.getHeaderRowIndex();
        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

        // 没有回调时只校验表头，不扫描正文
        if (callback == null) {
            release(ownsWorkbook, workbook, ownedStream);
            return ParseResult.ok(language, finalHeaders);
        }

        if (cfg.isAsync()) {
            // ---------- 异步：表头通过后立即返回，正文流式回调 ----------
            Executor executor = cfg.getExecutor() == null ? defaultExecutor() : cfg.getExecutor();
            BodyCompleteListener listener = cfg.getOnBodyComplete();
            try {
                CompletableFuture.runAsync(() -> {
                    Throwable bodyError = null;
                    try {
                        readDataRows(sheet, finalHeaders, headerRowIndex, formatter, evaluator, callback);
                    } catch (Throwable t) {
                        // 异步模式下异常无法通过返回值传播：写入 stderr，并通过 onBodyComplete 通知调用方
                        bodyError = t;
                        System.err.println("[ExcelReader] 异步正文读取异常: " + t.getMessage());
                        t.printStackTrace();
                    } finally {
                        // Sheet / Workbook / 文件流全部在正文读取完全结束后才释放
                        release(ownsWorkbook, workbook, ownedStream);
                        notifyBodyComplete(listener, bodyError);
                    }
                }, executor);
            } catch (RuntimeException e) {
                // 任务提交失败（线程池已关闭 / 队列已满）：立即释放 Workbook 与文件流、通知完成回调并返回失败结果
                release(ownsWorkbook, workbook, ownedStream);
                notifyBodyComplete(listener, e);
                String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                return ParseResult.fail("异步任务提交失败：" + msg,
                        "Failed to submit async task: " + msg, null, finalHeaders);
            }
            return ParseResult.ok(language, finalHeaders);
        }

        // ---------- 同步：当前线程流式回调，回调全部执行完后返回 ----------
        try {
            readDataRows(sheet, finalHeaders, headerRowIndex, formatter, evaluator, callback);
            return ParseResult.ok(language, finalHeaders);
        } catch (RuntimeException e) {
            String msg = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return ParseResult.fail("表体解析失败：" + msg,
                    "Body parsing failed: " + msg, null, finalHeaders);
        } finally {
            release(ownsWorkbook, workbook, ownedStream);
        }
    }

    /**
     * 释放工具类拥有的资源：先关闭 Workbook，再关闭文件流（均忽略关闭异常）。
     */
    private static void release(boolean ownsWorkbook, Workbook workbook, InputStream ownedStream) {
        if (ownsWorkbook) {
            closeQuietly(workbook);
        }
        if (ownedStream != null) {
            closeQuietly(ownedStream);
        }
    }

    /** 通知异步正文完成回调；回调自身异常不影响正文流程。 */
    private static void notifyBodyComplete(BodyCompleteListener listener, Throwable bodyError) {
        if (listener == null) {
            return;
        }
        try {
            listener.onComplete(bodyError == null, bodyError);
        } catch (RuntimeException e) {
            System.err.println("[ExcelReader] onBodyComplete 回调异常: " + e.getMessage());
        }
    }

    /**
     * 异步读取默认线程池：线程数与队列容量均有上限的守护线程池，
     * 避免阻塞式正文读取占用 JVM 公共池，也避免任务无限堆积。
     *
     * <p>库级别使用，不主动关闭；如需受控生命周期请通过
     * {@link HeaderConfig#setExecutor(Executor)} 显式传入线程池。</p>
     */
    private static final class DefaultExecutorHolder {

        /**
         * 排队任务上限：超过后拒绝任务，由调用方收到"提交失败"结果。
         *
         * <p>每个排队任务都持有已打开的 Workbook，容量过大会同时占用大量文件句柄与内存，
         * 因此保持较小值（线程数的数倍）。</p>
         */
        private static final int QUEUE_CAPACITY = 64;

        private static final ExecutorService INSTANCE = buildExecutor();

        private static ExecutorService buildExecutor() {
            int threads = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
            return new ThreadPoolExecutor(
                    threads, threads,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                    r -> {
                        Thread t = new Thread(r, "excel-reader-async");
                        t.setDaemon(true);
                        return t;
                    },
                    new ThreadPoolExecutor.AbortPolicy());
        }
    }

    private static Executor defaultExecutor() {
        return DefaultExecutorHolder.INSTANCE;
    }

    // ============================================================
    // 表头校验
    // ============================================================

    /**
     * 语言模式（内部使用）。
     */
    private enum LanguageMode {
        DUAL,
        ZH_ONLY,
        EN_ONLY
    }

    /**
     * 表头校验的中间结果。
     */
    private static final class HeaderCheckResult {
        boolean ok;
        String messageZhDetail;
        String messageEnDetail;
        String messageZhShort;
        String messageEnShort;
        Sheet sheet;
        List<HeaderCellInfo> rawHeaders;
        List<HeaderCellInfo> finalHeaders;
        HeaderLanguage language;

        void setMessage(String zh, String en) {
            this.messageZhDetail = zh;
            this.messageEnDetail = en;
            this.messageZhShort = zh;
            this.messageEnShort = en;
        }
    }

    /**
     * 判定语言模式；混合场景抛异常。
     */
    private static LanguageMode detectLanguageMode(List<ColumnDef> columns) {
        boolean allHaveChinese = true;
        boolean allHaveEnglish = true;
        for (ColumnDef c : columns) {
            if (!c.isChineseProvided()) {
                allHaveChinese = false;
            }
            if (!c.isEnglishProvided()) {
                allHaveEnglish = false;
            }
        }
        if (allHaveChinese && allHaveEnglish) {
            return LanguageMode.DUAL;
        }
        if (allHaveChinese) {
            return LanguageMode.ZH_ONLY;
        }
        if (allHaveEnglish) {
            return LanguageMode.EN_ONLY;
        }
        throw new IllegalArgumentException(
                "列定义语言不一致：要么所有列都同时提供中英文名，要么所有列都提供中文名，"
                        + "要么所有列都提供英文名；不支持混合 / "
                        + "Inconsistent column definition languages. Columns=" + columns);
    }

    /**
     * 表头校验（同步）。
     */
    private static HeaderCheckResult checkHeader(Workbook workbook, HeaderConfig cfg, LanguageMode mode) {
        HeaderCheckResult result = new HeaderCheckResult();

        if (workbook.getNumberOfSheets() == 0) {
            result.setMessage("文件中不存在任何工作表", "No worksheet found in the file");
            return result;
        }
        int sheetIdx = cfg.getSheetIndex();
        if (sheetIdx < 0 || sheetIdx >= workbook.getNumberOfSheets()) {
            result.setMessage("工作表索引超出范围：请求第 " + (sheetIdx + 1) + " 个工作表，实际共 "
                            + workbook.getNumberOfSheets() + " 个",
                    "Worksheet index out of range: requested #" + (sheetIdx + 1) + ", but the file has "
                            + workbook.getNumberOfSheets() + " worksheet(s)");
            return result;
        }
        Sheet sheet = workbook.getSheetAt(sheetIdx);
        if (sheet == null) {
            result.setMessage("工作表不存在", "Worksheet not found");
            return result;
        }
        result.sheet = sheet;

        DataFormatter formatter = new DataFormatter();
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        int headerRowIndex = cfg.getHeaderRowIndex();
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow == null) {
            result.setMessage("表头行不存在：第 " + (headerRowIndex + 1) + " 行为空",
                    "Header row is missing: row " + (headerRowIndex + 1) + " is empty");
            return result;
        }

        List<HeaderCellInfo> rawHeaders;
        try {
            rawHeaders = readHeaderCells(headerRow, formatter, evaluator, cfg.isIgnoreBlankColumns());
        } catch (RuntimeException e) {
            // 表头单元格读取/公式求值失败：返回失败结果而不是把异常抛给调用方
            result.setMessage("表头读取失败：" + e.getMessage(),
                    "Header parsing failed: " + e.getMessage());
            return result;
        }
        result.rawHeaders = rawHeaders;

        boolean anyValidHeader = false;
        for (HeaderCellInfo h : rawHeaders) {
            if (!h.getValue().isEmpty()) {
                anyValidHeader = true;
                break;
            }
        }
        if (!anyValidHeader) {
            result.setMessage("表头行没有任何有效内容", "Header row contains no valid content");
            return result;
        }

        Validation chosen;
        HeaderLanguage language;

        if (mode == LanguageMode.DUAL) {
            Validation zhV = validate(rawHeaders, cfg, true);
            Validation enV = validate(rawHeaders, cfg, false);
            if (zhV.ok && enV.ok) {
                chosen = zhV;
                language = HeaderLanguage.ZH;
            } else if (zhV.ok) {
                chosen = zhV;
                language = HeaderLanguage.ZH;
            } else if (enV.ok) {
                chosen = enV;
                language = HeaderLanguage.EN;
            } else {
                Validation primary = pickPrimary(zhV, enV, rawHeaders);
                fillFailureMessages(result, primary, cfg);
                return result;
            }
        } else if (mode == LanguageMode.ZH_ONLY) {
            Validation zhV = validate(rawHeaders, cfg, true);
            if (!zhV.ok) {
                fillFailureMessages(result, zhV, cfg);
                return result;
            }
            chosen = zhV;
            language = HeaderLanguage.ZH;
        } else {
            Validation enV = validate(rawHeaders, cfg, false);
            if (!enV.ok) {
                fillFailureMessages(result, enV, cfg);
                return result;
            }
            chosen = enV;
            language = HeaderLanguage.EN;
        }

        List<HeaderCellInfo> finalHeaders = new ArrayList<>(cfg.getColumns().size());
        for (int i = 0; i < cfg.getColumns().size(); i++) {
            Integer excelIdx = chosen.excelIndexByColumn.get(i);
            if (excelIdx == null) {
                result.setMessage("内部错误：列映射不完整", "Internal error: incomplete column mapping");
                return result;
            }
            String headerValue = "";
            for (HeaderCellInfo h : rawHeaders) {
                if (h.getColumnIndex() == excelIdx) {
                    headerValue = h.getValue();
                    break;
                }
            }
            finalHeaders.add(new HeaderCellInfo(excelIdx, headerValue, cfg.getColumns().get(i)));
        }

        result.ok = true;
        result.language = language;
        result.finalHeaders = finalHeaders;
        return result;
    }

    private static void fillFailureMessages(HeaderCheckResult result, Validation v, HeaderConfig cfg) {
        String prefixZh = "表头校验失败" + buildIgnoreInfoZh(cfg) + "：";
        String prefixEn = "Header validation failed" + buildIgnoreInfoEn(cfg) + ": ";
        result.messageZhDetail = prefixZh + (v.errorsZhDetail.isEmpty()
                ? "表头与要求不符" : String.join("；", v.errorsZhDetail));
        result.messageZhShort = prefixZh + (v.errorsZhShort.isEmpty()
                ? "表头与要求不符" : String.join("；", v.errorsZhShort));
        result.messageEnDetail = prefixEn + (v.errorsEnDetail.isEmpty()
                ? "headers do not match the required definition" : String.join("; ", v.errorsEnDetail));
        result.messageEnShort = prefixEn + (v.errorsEnShort.isEmpty()
                ? "headers do not match the required definition" : String.join("; ", v.errorsEnShort));
    }

    private static List<HeaderCellInfo> readHeaderCells(Row headerRow, DataFormatter formatter,
                                                        FormulaEvaluator evaluator,
                                                        boolean ignoreBlankColumns) {
        List<HeaderCellInfo> list = new ArrayList<>();
        int last = headerRow.getLastCellNum();
        if (last < 0) {
            return list;
        }
        for (int c = 0; c < last; c++) {
            String value = cellToString(headerRow.getCell(c), formatter, evaluator);
            if (value.isEmpty()) {
                if (!ignoreBlankColumns) {
                    list.add(new HeaderCellInfo(c, "", null));
                }
            } else {
                list.add(new HeaderCellInfo(c, value, null));
            }
        }
        return list;
    }

    // ============================================================
    // 表体读取（同步 / 异步共用的核心方法）
    // ============================================================

    /**
     * 逐行读取表体并流式调用 {@link RowCallback#onRow}。
     *
     * <p><b>流式特性：</b>每读一行立即回调 → 方法返回后 rowData 可被 GC，
     * 不缓存历史行数据。适合处理大型 Excel。</p>
     *
     * <p><b>callback == null 时</b>：直接返回，不读取正文（调用方只校验表头时使用）。</p>
     */
    private static void readDataRows(Sheet sheet, List<HeaderCellInfo> headers,
                                     int headerRowIndex, DataFormatter formatter,
                                     FormulaEvaluator evaluator, RowCallback callback) {
        if (callback == null) {
            return;
        }
        int n = headers.size();
        int[] excelCols = new int[n];
        ColumnDef[] defs = new ColumnDef[n];
        for (int i = 0; i < n; i++) {
            HeaderCellInfo h = headers.get(i);
            excelCols[i] = h.getColumnIndex();
            defs[i] = h.getColumn();
        }

        int start = headerRowIndex + 1;
        int last = sheet.getLastRowNum();
        for (int r = start; r <= last; r++) {
            // 空白行（包括整行不存在的情况）也必须回调，由非空校验标记必填列缺失
            Row row = sheet.getRow(r);

            // 每个单元格只求值一次：文本与错误标记都来自同一次求值结果
            String[] values = new String[n];
            boolean[] errorCells = new boolean[n];
            for (int i = 0; i < n; i++) {
                try {
                    Cell cell = row == null ? null : row.getCell(excelCols[i]);
                    values[i] = cellToString(cell, formatter, evaluator);
                    errorCells[i] = isErrorCell(cell);
                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            String.format("第 %d 行，Excel 列索引 %d 读取失败：%s",
                                    r + 1, excelCols[i], e.getMessage()), e);
                }
            }

            // 构造当前行的 rowData，逐行回调，回调完成后立即释放
            LinkedHashMap<Integer, CellData> rowData = new LinkedHashMap<>();
            for (int i = 0; i < n; i++) {
                String value = values[i];
                ColumnDef def = defs[i];
                int excelColIdx = excelCols[i];
                int displayRow = r + 1;

                boolean valid = true;
                String errZh = null;
                String errEn = null;
                if (errorCells[i]) {
                    // #DIV/0!、#VALUE! 等错误值不能当作有效数据
                    valid = false;
                    errZh = String.format(
                            "第 %d 行，列 [%s/%s]（Excel 列索引 %d）为 Excel 错误值（%s），请修正公式或原始数据",
                            displayRow, def.getChineseName(), def.getEnglishName(), excelColIdx, value);
                    errEn = String.format(
                            "Row %d, column [%s/%s] (Excel column index %d) contains an Excel error value (%s)",
                            displayRow, def.getChineseName(), def.getEnglishName(), excelColIdx, value);
                } else if (!def.isNullable() && value.isEmpty()) {
                    valid = false;
                    errZh = String.format(
                            "第 %d 行，列 [%s/%s]（Excel 列索引 %d）为必填列，但内容为空",
                            displayRow, def.getChineseName(), def.getEnglishName(), excelColIdx);
                    errEn = String.format(
                            "Row %d, column [%s/%s] (Excel column index %d) is required but empty",
                            displayRow, def.getChineseName(), def.getEnglishName(), excelColIdx);
                }

                rowData.put(i, new CellData(value, def, excelColIdx, valid, errZh, errEn));
            }
            try {
                callback.onRow(r, rowData);
            } catch (RuntimeException e) {
                throw new IllegalStateException(
                        String.format("第 %d 行回调处理失败：%s", r + 1, e.getMessage()), e);
            }
            // rowData 出作用域，若无外部引用即可被 GC
        }
    }

    // ============================================================
    // 表头校验细节
    // ============================================================

    private static final class Validation {
        final List<String> errorsZhShort = new ArrayList<>();
        final List<String> errorsZhDetail = new ArrayList<>();
        final List<String> errorsEnShort = new ArrayList<>();
        final List<String> errorsEnDetail = new ArrayList<>();
        boolean ok = false;
        final Map<Integer, Integer> excelIndexByColumn = new LinkedHashMap<>();

        void addError(String zhShort, String zhDetail, String enShort, String enDetail) {
            errorsZhShort.add(zhShort);
            errorsZhDetail.add(zhDetail);
            errorsEnShort.add(enShort);
            errorsEnDetail.add(enDetail);
        }
    }

    private static Validation validate(List<HeaderCellInfo> actual, HeaderConfig cfg, boolean isChinese) {
        Validation v = new Validation();

        List<ColumnDef> columns = cfg.getColumns();
        List<String> expected = new ArrayList<>(columns.size());
        for (ColumnDef c : columns) {
            expected.add(isChinese ? c.getChineseName() : c.getEnglishName());
        }

        if (cfg.isOrderRequired()) {
            boolean mismatch = false;
            int n = Math.min(actual.size(), expected.size());
            for (int i = 0; i < n; i++) {
                HeaderCellInfo info = actual.get(i);
                if (!headerEquals(info.getValue(), expected.get(i), cfg)) {
                    String actualDisplay = displayValue(info.getValue());
                    List<String> actualOrder = headerValues(actual);
                    v.addError(
                            String.format("第 %d 个表头（Excel 列索引 %d）期望为 [%s]，实际为 [%s]",
                                    i + 1, info.getColumnIndex(), expected.get(i), actualDisplay),
                            String.format("第 %d 个表头（Excel 列索引 %d）期望为 [%s]，实际为 [%s]；期望表头顺序为 %s，实际表头顺序为 %s",
                                    i + 1, info.getColumnIndex(), expected.get(i), actualDisplay, expected, actualOrder),
                            String.format("Header #%d (column index %d) expected [%s] but was [%s]",
                                    i + 1, info.getColumnIndex(), expected.get(i), actualDisplay),
                            String.format("Header #%d (column index %d) expected [%s] but was [%s]; expected order %s, actual order %s",
                                    i + 1, info.getColumnIndex(), expected.get(i), actualDisplay, expected, actualOrder));
                    mismatch = true;
                    break;
                }
                v.excelIndexByColumn.put(i, info.getColumnIndex());
            }
            if (!mismatch) {
                if (actual.size() < expected.size()) {
                    List<String> missing = expected.subList(actual.size(), expected.size());
                    List<String> actualOrder = headerValues(actual);
                    v.addError(
                            "缺少必需的表头: " + missing,
                            "缺少必需的表头: " + missing + "；期望表头顺序为 " + expected + "，实际表头顺序为 " + actualOrder,
                            "Missing required headers: " + missing,
                            "Missing required headers: " + missing + "; expected order " + expected + ", actual order " + actualOrder);
                }
                if (!cfg.isAllowExtraHeaders() && actual.size() > expected.size()) {
                    List<String> extra = new ArrayList<>();
                    for (int i = expected.size(); i < actual.size(); i++) {
                        extra.add(displayValue(actual.get(i).getValue()));
                    }
                    List<String> actualOrder = headerValues(actual);
                    v.addError(
                            "存在不允许的额外表头: " + extra,
                            "存在不允许的额外表头: " + extra + "；期望表头顺序为 " + expected + "，实际表头顺序为 " + actualOrder,
                            "Unexpected extra headers: " + extra,
                            "Unexpected extra headers: " + extra + "; expected order " + expected + ", actual order " + actualOrder);
                }
            }
        } else {
            Set<Integer> matched = new HashSet<>();
            for (HeaderCellInfo info : actual) {
                int foundIdx = -1;
                for (int i = 0; i < expected.size(); i++) {
                    if (matched.contains(i)) {
                        continue;
                    }
                    if (headerEquals(info.getValue(), expected.get(i), cfg)) {
                        foundIdx = i;
                        break;
                    }
                }
                if (foundIdx >= 0) {
                    matched.add(foundIdx);
                    v.excelIndexByColumn.put(foundIdx, info.getColumnIndex());
                } else if (!cfg.isAllowExtraHeaders()) {
                    String actualDisplay = displayValue(info.getValue());
                    v.addError(
                            "存在不允许的额外表头: " + actualDisplay,
                            "存在不允许的额外表头: " + actualDisplay + "；期望表头为 " + expected,
                            "Unexpected extra header: " + actualDisplay,
                            "Unexpected extra header: " + actualDisplay + "; expected headers: " + expected);
                }
            }
            List<String> missing = new ArrayList<>();
            for (int i = 0; i < expected.size(); i++) {
                if (!matched.contains(i)) {
                    missing.add(expected.get(i));
                }
            }
            if (!missing.isEmpty()) {
                v.addError(
                        "缺少必需的表头: " + missing,
                        "缺少必需的表头: " + missing + "；期望表头为 " + expected,
                        "Missing required headers: " + missing,
                        "Missing required headers: " + missing + "; expected headers: " + expected);
            }
        }

        v.ok = v.errorsZhDetail.isEmpty();
        return v;
    }

    /**
     * 双语表头均失败时，按实际表头语言选出一套校验结果作为错误消息来源。
     *
     * <p>正常情况下两套校验都会失败；仅当首选校验意外通过时，才改用另一套的失败信息。</p>
     */
    private static Validation pickPrimary(Validation zhV, Validation enV, List<HeaderCellInfo> actual) {
        boolean actualHasChinese = false;
        for (HeaderCellInfo h : actual) {
            if (containsChinese(h.getValue())) {
                actualHasChinese = true;
                break;
            }
        }
        Validation primary = actualHasChinese ? zhV : enV;
        if (primary.errorsZhDetail.isEmpty()) {
            Validation other = primary == zhV ? enV : zhV;
            if (!other.errorsZhDetail.isEmpty()) {
                return other;
            }
        }
        return primary;
    }

    private static String buildIgnoreInfoZh(HeaderConfig cfg) {
        List<String> parts = new ArrayList<>();
        if (cfg.isIgnoreInvisibleChars()) {
            parts.add("空白/不可见字符");
        }
        List<String> chars = cfg.getIgnoredChars();
        if (chars != null && !chars.isEmpty()) {
            parts.add("[" + String.join("", chars) + "]");
        }
        return parts.isEmpty() ? "" : "（比对时忽略：" + String.join("、", parts) + "）";
    }

    private static String buildIgnoreInfoEn(HeaderConfig cfg) {
        List<String> parts = new ArrayList<>();
        if (cfg.isIgnoreInvisibleChars()) {
            parts.add("whitespace/invisible chars");
        }
        List<String> chars = cfg.getIgnoredChars();
        if (chars != null && !chars.isEmpty()) {
            parts.add("[" + String.join("", chars) + "]");
        }
        return parts.isEmpty() ? "" : " (ignored during comparison: " + String.join(", ", parts) + ")";
    }

    // ============================================================
    // 表头比较 & 归一化
    // ============================================================

    private static boolean headerEquals(String a, String b, HeaderConfig cfg) {
        String x = normalizeHeader(a, cfg);
        String y = normalizeHeader(b, cfg);
        if (x.isEmpty() || y.isEmpty()) {
            return false;
        }
        return cfg.isIgnoreCase() ? x.equalsIgnoreCase(y) : x.equals(y);
    }

    private static String normalizeHeader(String s, HeaderConfig cfg) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        boolean ignoreInvisible = cfg.isIgnoreInvisibleChars();
        List<String> ignoredChars = cfg.getIgnoredChars();
        boolean hasIgnoredStrings = ignoredChars != null && !ignoredChars.isEmpty();
        if (!ignoreInvisible && !hasIgnoredStrings) {
            return s;
        }

        String step1;
        if (ignoreInvisible) {
            StringBuilder sb = new StringBuilder(s.length());
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (!isInvisible(c)) {
                    sb.append(c);
                }
            }
            step1 = sb.toString();
        } else {
            step1 = s;
        }

        return hasIgnoredStrings ? removeIgnoredStrings(step1, ignoredChars) : step1;
    }

    private static String removeIgnoredStrings(String s, List<String> ignored) {
        if (s == null || s.isEmpty() || ignored == null || ignored.isEmpty()) {
            return s == null ? "" : s;
        }
        List<String> sorted = new ArrayList<>(ignored.size());
        for (String t : ignored) {
            if (t != null && !t.isEmpty()) {
                sorted.add(t);
            }
        }
        if (sorted.isEmpty()) {
            return s;
        }
        sorted.sort((a, b) -> Integer.compare(b.length(), a.length()));

        StringBuilder sb = new StringBuilder(s);
        for (String t : sorted) {
            int idx;
            while ((idx = sb.indexOf(t)) >= 0) {
                sb.delete(idx, idx + t.length());
            }
        }
        return sb.toString();
    }

    private static boolean isInvisible(char c) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        switch (c) {
            case '\u00A0':
            case '\u2007':
            case '\u202F':
            case '\uFEFF':
            case '\u200B':
            case '\u200C':
            case '\u200D':
            case '\u2060':
                return true;
            default:
                return false;
        }
    }

    // ============================================================
    // 工具方法
    // ============================================================

    private static String displayValue(String value) {
        return value == null || value.isEmpty() ? "<空白列>" : value;
    }

    private static List<String> headerValues(List<HeaderCellInfo> actual) {
        List<String> list = new ArrayList<>(actual.size());
        for (HeaderCellInfo h : actual) {
            list.add(displayValue(h.getValue()));
        }
        return list;
    }

    private static boolean containsChinese(String s) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (Character.UnicodeScript.of(s.charAt(i)) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    /** 读取单元格文本；公式单元格按计算结果输出。读取异常向上抛出，由调用方补充行列信息。 */
    private static String cellToString(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) {
            return "";
        }
        String s = formatter.formatCellValue(cell, evaluator);
        return s == null ? "" : trimInvisible(s);
    }

    /**
     * 单元格是否为 Excel 错误值（#DIV/0!、#VALUE! 等）。
     *
     * <p>公式单元格使用 {@link Cell#getCachedFormulaResultType()} 判断：该值由
     * {@link #cellToString} 中的一次求值写入，避免对同一公式重复求值。</p>
     */
    private static boolean isErrorCell(Cell cell) {
        if (cell == null) {
            return false;
        }
        CellType type = cell.getCellType();
        if (type == CellType.ERROR) {
            return true;
        }
        if (type == CellType.FORMULA) {
            return cell.getCachedFormulaResultType() == CellType.ERROR;
        }
        return false;
    }

    private static String trimInvisible(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        int start = 0;
        int end = s.length();
        while (start < end && isInvisible(s.charAt(start))) {
            start++;
        }
        while (end > start && isInvisible(s.charAt(end - 1))) {
            end--;
        }
        if (start == 0 && end == s.length()) {
            return s;
        }
        return s.substring(start, end);
    }

    private static void closeQuietly(Workbook workbook) {
        if (workbook != null) {
            try {
                workbook.close();
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    private static void closeQuietly(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
                // ignore
            }
        }
    }
}