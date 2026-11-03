package com.yishuifengxiao.common.tool.io;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.*;

/**
 * 通用 Excel 写入工具类。
 *
 * <h2>输出格式</h2>
 * <ul>
 *   <li><b>自动识别</b>：{@link #write(File, WriteConfig)} 根据文件名后缀判断（大小写不敏感）；</li>
 *   <li><b>显式指定</b>：{@code write(OutputStream, config, format)} / {@code writeToBytes(config, format)}；</li>
 *   <li><b>便利重载</b>：{@code write(OutputStream, config)} / {@code writeToBytes(config)} 默认 {@link ExcelFormat#XLSX}。</li>
 * </ul>
 *
 * <h2>行高与列宽处理（重要）</h2>
 * <ul>
 *   <li>列宽：取"表头 + 所有数据单元格"的显示宽度最大值，再 clamp 到
 *       [{@code minColumnWidth}, {@code maxColumnWidth}]；</li>
 *   <li>行高：{@link TextOverflow#WRAP} 模式下按 <b>最终实际列宽</b> 估算需要多少行，
 *       每行 15pt；上限 409pt（POI 单行限制）；</li>
 *   <li><b>顺序保证</b>：先算实际列宽并 setColumnWidth，<b>再</b>用同一组列宽调行高，
 *       避免"内容长但被 maxColumnWidth 截断后行高算少了"的问题。</li>
 * </ul>
 */
public final class ExcelWriter {

    /** 私有构造：工具类禁止实例化。 */
    private ExcelWriter() {
    }

    // ============================================================
    // 对外类型
    // ============================================================

    /** Excel 输出格式。 */
    public enum ExcelFormat {

        /** 老格式 .xls（HSSF），最大 65536 行 × 256 列。 */
        XLS,

        /** 新格式 .xlsx（XSSF）。 */
        XLSX;

        /** 根据文件名自动识别格式（大小写不敏感）。 */
        public static ExcelFormat detect(String fileName) {
            if (fileName == null || fileName.isEmpty()) {
                return null;
            }
            String lower = fileName.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".xlsx")) {
                return XLSX;
            }
            if (lower.endsWith(".xls")) {
                return XLS;
            }
            return null;
        }
    }

    /** 文本溢出处理方式。 */
    public enum TextOverflow {
        /** 自动换行：超出列宽的内容在单元格内换行显示。 */
        WRAP,
        /** 隐藏：超出列宽的内容不显示（不换行）。 */
        HIDE
    }

    /**
     * 写入配置。所有 setter 均链式返回。
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    public static final class WriteConfig {

        /** 工作表名称，默认 "Sheet1"。 */
        private String sheetName = "Sheet1";

        /** 表头（列名列表，顺序即列顺序）。 */
        @Setter(AccessLevel.NONE)
        private List<String> headers = new ArrayList<>();

        /** 正文数据（List 形式）。与 mapData 互斥。 */
        @Setter(AccessLevel.NONE)
        private List<List<String>> listData = new ArrayList<>();

        /** 正文数据（Map 形式）。与 listData 互斥。 */
        @Setter(AccessLevel.NONE)
        private List<Map<String, String>> mapData = new ArrayList<>();

        /**
         * 数据列数超过表头列数时的处理：
         * <ul>
         *   <li>true：以表头列数为准，数据超出部分忽略；</li>
         *   <li>false：以数据最长列为准，表头不足位置补空。</li>
         * </ul>
         */
        private boolean truncateExtraData = true;

        /** 文本溢出处理方式，默认自动换行。 */
        private TextOverflow textOverflow = TextOverflow.WRAP;

        /**
         * 是否所有单元格都以字符串格式写入，默认 true。
         *
         * <p>{@code false} 时自动识别数字/布尔；超过 2^53 的整数仍按文本写入，避免 double 精度丢失。</p>
         */
        private boolean allAsText = true;

        /** 是否自动计算列宽，默认 true。 */
        private boolean autoColumnWidth = true;

        /** 列宽下限（字符数），默认 8。 */
        private int minColumnWidth = 8;

        /** 列宽上限（字符数），默认 60。 */
        private int maxColumnWidth = 60;

        /** 是否冻结首行（表头），默认 true。 */
        private boolean freezeHeader = true;

        /** 设置表头（List 形式，防御性拷贝）。 */
        public WriteConfig setHeaders(List<String> headers) {
            this.headers = headers == null ? new ArrayList<>() : new ArrayList<>(headers);
            return this;
        }

        /** 设置表头（可变参数形式，null 元素自动转为 ""）。 */
        public WriteConfig setHeaders(String... headers) {
            if (headers == null) {
                this.headers = new ArrayList<>();
            } else {
                List<String> list = new ArrayList<>(headers.length);
                for (String h : headers) {
                    list.add(h == null ? "" : h);
                }
                this.headers = list;
            }
            return this;
        }

        /** 设置 List 形式数据。会清空 mapData。 */
        public WriteConfig setListData(List<List<String>> listData) {
            this.listData = listData == null ? new ArrayList<>() : new ArrayList<>(listData);
            this.mapData = new ArrayList<>();
            return this;
        }

        /** 设置 Map 形式数据。会清空 listData。 */
        public WriteConfig setMapData(List<Map<String, String>> mapData) {
            this.mapData = mapData == null ? new ArrayList<>() : new ArrayList<>(mapData);
            this.listData = new ArrayList<>();
            return this;
        }
    }

    // ============================================================
    // 写出入口
    // ============================================================

    /**
     * 写出到文件，根据后缀自动识别格式（.xls / .xlsx）。
     *
     * <p>写入采用"同目录临时文件 + 原子替换"：失败时不会破坏已存在的目标文件。</p>
     * <ul>
     *   <li>目标为符号链接时写入链接指向的真实文件，链接本身保留；层级超过 8 层或成环时报错；</li>
     *   <li>格式按<b>真实目标文件</b>的后缀判断，避免链接名与目标后缀不一致；</li>
     *   <li>目标已存在且不可写时拒绝覆盖（抛 {@link IOException}）。该判断为<b>尽力而为</b>：
     *       root/管理员身份下可能仍可写入只读文件，需要强保证时应依赖文件系统权限；</li>
     *   <li>POSIX 平台保留目标文件原有权限；新建文件不强制覆盖权限，沿用平台默认权限（受 umask 影响）；</li>
     *   <li>替换前对临时文件执行 {@code fsync}；但<b>不对父目录 fsync</b>，
     *       因此"原子替换"只保证旧文件或新文件之一可见，不保证断电后更新一定持久化。</li>
     * </ul>
     */
    public static void write(File file, WriteConfig config) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file 不能为空 / file must not be null");
        }
        // 纯配置校验放在任何路径/文件操作之前：配置错误不应被软链解析等 I/O 错误掩盖
        validateConfig(config);

        Path target = resolveRealTarget(file.toPath().toAbsolutePath());
        if (target.getFileName() == null) {
            throw new IllegalArgumentException(
                    "目标路径是文件系统根目录，无法写出文件：" + target
                            + " / Target path is a filesystem root, cannot write file: " + target);
        }
        String targetName = target.getFileName().toString();
        ExcelFormat format = ExcelFormat.detect(targetName);
        if (format == null) {
            throw new IllegalArgumentException(
                    "无法从文件名判断 Excel 格式，文件名须以 .xls 或 .xlsx 结尾："
                            + file.getName()
                            + (targetName.equals(file.getName()) ? "" : "（实际写入 " + targetName + "）")
                            + " / Cannot determine format from file name, must end with .xls or .xlsx: "
                            + targetName);
        }

        // 先在内存中完成可预知的配置校验与布局计算，不触碰任何文件
        SheetLayout sheetLayout = layout(config, format);

        Path parent = target.getParent();
        if (parent == null || !Files.isDirectory(parent)) {
            throw new IllegalArgumentException(
                    "目标文件的父目录不存在：" + target
                            + " / Parent directory of the target file does not exist: " + target);
        }
        if (Files.isDirectory(target)) {
            throw new IllegalArgumentException(
                    "目标路径是目录，无法写出文件：" + target
                            + " / Target path is a directory, cannot write file: " + target);
        }
        // 只读文件在 POSIX 下仍可被 rename 覆盖，这里显式拒绝，避免绕过只读保护
        if (Files.exists(target) && !Files.isWritable(target)) {
            throw new IOException(
                    "目标文件不可写，已拒绝覆盖：" + target
                            + " / Target file is not writable, refusing to overwrite: " + target);
        }

        // 写同目录临时文件，成功后再原子替换目标文件：任何失败都不会破坏已有文件
        // 临时文件使用 .tmp 后缀，进程崩溃残留时不会被误认为正常 Excel
        // 用 Files.createFile 创建（遵循平台 umask），而不是硬编码权限
        String fileName = target.getFileName().toString();
        String prefix = fileName.length() >= 3 ? fileName : fileName + "-tmp";
        Path temp = createTempFile(parent, prefix);
        try {
            try (Workbook wb = buildWorkbook(sheetLayout, config, format);
                 OutputStream out = new BufferedOutputStream(Files.newOutputStream(temp))) {
                wb.write(out);
            }
            // 先让临时文件内容落盘，再替换目标文件；原子替换只保证"看到旧文件或新文件"
            forceFile(temp);
            applyTargetPermissions(target, temp);
            moveReplacing(temp, target);
        } finally {
            // 清理失败不覆盖真实异常；残留的 .tmp 可安全删除
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // ignore：优先保留写入/替换阶段的主异常
            }
        }
    }

    /** 解析符号链接到真实路径（最多 8 层），保证写入更新链接目标而不是替换链接本身。 */
    private static Path resolveRealTarget(Path target) throws IOException {
        Path current = target;
        for (int i = 0; i < 8 && Files.isSymbolicLink(current); i++) {
            Path link = Files.readSymbolicLink(current);
            current = link.isAbsolute() ? link : current.getParent().resolve(link);
            current = current.normalize();
        }
        // 超过上限或存在环时，继续替换会破坏链接本身，直接报错
        if (Files.isSymbolicLink(current)) {
            throw new IOException(
                    "符号链接层级超过上限或存在环：" + target
                            + " / Too many levels of symbolic links or a link cycle: " + target);
        }
        return current;
    }

    /** 将临时文件内容强制刷入磁盘（POSIX 下 fsync，Windows 下同语义尽力而为）。 */
    private static void forceFile(Path path) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
            channel.force(true);
        }
    }

    /**
     * 在目标目录创建唯一临时文件（后缀固定为 {@code .tmp}）。
     *
     * <p>使用 {@link Files#createFile} 而非 {@link Files#createTempFile}，让文件权限由平台默认策略
     * （umask）决定，避免新建目标文件时被硬编码权限覆盖。</p>
     */
    private static Path createTempFile(Path parent, String prefix) throws IOException {
        for (int i = 0; i < 10; i++) {
            Path candidate = parent.resolve(
                    prefix + "-" + UUID.randomUUID().toString().replace("-", "") + ".tmp");
            try {
                return Files.createFile(candidate);
            } catch (FileAlreadyExistsException e) {
                // 名字冲突，重新生成
            }
        }
        throw new IOException("无法创建临时文件，目录：" + parent
                + " / Failed to create temporary file in directory: " + parent);
    }

    /**
     * 让替换后的目标文件保持原有权限。
     *
     * <p>POSIX 平台临时文件默认权限较严格，直接替换会改变目标文件权限；
     * 目标已存在时沿用其权限；目标不存在时不覆盖权限，沿用平台/JVM 创建临时文件时的默认权限
     * （受进程 umask 影响），避免硬编码权限。非 POSIX 平台（如 Windows）不做处理。</p>
     */
    private static void applyTargetPermissions(Path target, Path temp) throws IOException {
        if (!Files.getFileStore(temp).supportsFileAttributeView(PosixFileAttributeView.class)) {
            return;
        }
        if (!Files.exists(target)) {
            // 新建文件：保留临时文件的平台默认权限，不做硬编码覆盖
            return;
        }
        Files.setPosixFilePermissions(temp, Files.getPosixFilePermissions(target));
    }

    /** 用临时文件替换目标文件；文件系统不支持原子移动时退化为普通替换。 */
    private static void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** 写出到输出流，默认 xlsx。 */
    public static void write(OutputStream out, WriteConfig config) throws IOException {
        write(out, config, ExcelFormat.XLSX);
    }

    /** 写出到输出流，显式指定格式。 */
    public static void write(OutputStream out, WriteConfig config, ExcelFormat format) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("OutputStream 不能为空 / OutputStream must not be null");
        }
        if (format == null) {
            throw new IllegalArgumentException("ExcelFormat 不能为空 / ExcelFormat must not be null");
        }
        try (Workbook wb = buildWorkbook(config, format)) {
            wb.write(out);
        }
    }

    /** 写出为字节数组，默认 xlsx。 */
    public static byte[] writeToBytes(WriteConfig config) throws IOException {
        return writeToBytes(config, ExcelFormat.XLSX);
    }

    /** 写出为字节数组，显式指定格式。 */
    public static byte[] writeToBytes(WriteConfig config, ExcelFormat format) throws IOException {
        if (format == null) {
            throw new IllegalArgumentException("ExcelFormat 不能为空 / ExcelFormat must not be null");
        }
        try (Workbook wb = buildWorkbook(config, format);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    /** 向已有 Workbook 追加一个 Sheet（由调用方管理生命周期）。 */
    public static void write(Workbook workbook, WriteConfig config) {
        if (workbook == null) {
            throw new IllegalArgumentException("Workbook 不能为空 / Workbook must not be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("config 不能为空 / config must not be null");
        }
        String sheetName = config.getSheetName();
        if (workbook.getSheet(sheetName) != null) {
            throw new IllegalArgumentException(
                    "工作表已存在，无法重复创建：" + sheetName
                            + " / Sheet already exists: " + sheetName);
        }
        int before = workbook.getNumberOfSheets();
        try {
            buildSheet(workbook, config);
        } catch (RuntimeException e) {
            // 渲染中途失败（如样式/行列资源上限）时回滚新建的 Sheet，避免留下半成品
            try {
                if (workbook.getNumberOfSheets() > before) {
                    workbook.removeSheetAt(workbook.getNumberOfSheets() - 1);
                }
            } catch (RuntimeException rollbackError) {
                e.addSuppressed(rollbackError);
            }
            throw e;
        }
    }

    // ============================================================
    // 内部：Workbook 创建
    // ============================================================

    private static Workbook buildWorkbook(WriteConfig config, ExcelFormat format) {
        return buildWorkbook(layout(config, format), config, format);
    }

    private static Workbook buildWorkbook(SheetLayout sheetLayout, WriteConfig config, ExcelFormat format) {
        Workbook wb = format == ExcelFormat.XLS ? new HSSFWorkbook() : new XSSFWorkbook();
        renderSheet(wb, sheetLayout, config);
        return wb;
    }

    // ============================================================
    // 核心逻辑
    // ============================================================

    /** 构建 Sheet（布局计算 + 渲染），供已有 Workbook 追加 Sheet 使用。 */
    private static void buildSheet(Workbook wb, WriteConfig config) {
        ExcelFormat format = wb instanceof HSSFWorkbook ? ExcelFormat.XLS : ExcelFormat.XLSX;
        renderSheet(wb, layout(config, format), config);
    }

    /** Sheet 布局：已完成全部可预知的参数与边界校验，不包含任何文件操作。 */
    private static final class SheetLayout {
        final List<String> headers;
        final List<List<String>> rows;
        final int maxCols;

        SheetLayout(List<String> headers, List<List<String>> rows, int maxCols) {
            this.headers = headers;
            this.rows = rows;
            this.maxCols = maxCols;
        }
    }

    /**
     * 计算 Sheet 布局并完成可预知的校验（config 非空、sheetName、列宽参数、HSSF 行列上限）。
     *
     * <p>此方法不创建任何文件，因此可以在打开输出目标之前安全调用。</p>
     */
    private static SheetLayout layout(WriteConfig config, ExcelFormat format) {
        validateConfig(config);

        // 1. 解析有效表头
        List<String> effectiveHeaders = resolveHeaders(config);
        int headerCols = effectiveHeaders.size();

        // 2. 解析数据行
        List<List<String>> rows = toRowList(config, effectiveHeaders);

        // 3. 计算最终列数
        //    headers 为空但存在数据时，按数据列数展开：truncateExtraData 只用于"有表头时丢弃多余列"，
        //    不能因为没写表头就把数据整体丢弃
        int dataCols = 0;
        for (List<String> row : rows) {
            if (row != null && row.size() > dataCols) {
                dataCols = row.size();
            }
        }
        int maxCols = headerCols > 0 && config.isTruncateExtraData()
                ? headerCols
                : Math.max(headerCols, dataCols);

        // 4. HSSF 边界保护（未设置表头时没有表头行，行数只算数据行）
        if (format == ExcelFormat.XLS) {
            int totalRows = (headerCols > 0 ? 1 : 0) + rows.size();
            if (totalRows > 65536) {
                throw new IllegalArgumentException(
                        "xls 最多支持 65536 行，当前 " + totalRows + " 行，请改用 .xlsx / "
                                + "xls supports at most 65536 rows, current " + totalRows + ", please use .xlsx");
            }
            if (maxCols > 256) {
                throw new IllegalArgumentException(
                        "xls 最多支持 256 列，当前 " + maxCols + " 列，请改用 .xlsx / "
                                + "xls supports at most 256 columns, current " + maxCols + ", please use .xlsx");
            }
        }
        return new SheetLayout(effectiveHeaders, rows, maxCols);
    }

    /**
     * 纯配置校验（config 非空、sheetName、列宽参数）：不触碰任何文件，
     * 可在路径解析之前安全调用。
     */
    private static void validateConfig(WriteConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config 不能为空 / config must not be null");
        }
        validateSheetName(config.getSheetName());
        if (config.isAutoColumnWidth()) {
            validateColumnWidth(config.getMinColumnWidth(), config.getMaxColumnWidth());
        }
    }

    /** 校验工作表名称（POI 要求非空、不超过 31 个字符且不含 Excel 禁用字符）。 */
    private static void validateSheetName(String sheetName) {
        if (sheetName == null || sheetName.isEmpty()) {
            throw new IllegalArgumentException("sheetName 不能为空 / sheetName must not be empty");
        }
        if (sheetName.length() > 31) {
            throw new IllegalArgumentException(
                    "sheetName 长度不能超过 31 个字符，当前 " + sheetName.length()
                            + " / sheetName must not exceed 31 characters, current " + sheetName.length());
        }
        // Excel 禁用的工作表名字符：: \ / ? * [ ]
        for (int i = 0; i < sheetName.length(); i++) {
            char ch = sheetName.charAt(i);
            if (ch == ':' || ch == '\\' || ch == '/' || ch == '?' || ch == '*'
                    || ch == '[' || ch == ']') {
                throw new IllegalArgumentException(
                        "sheetName 包含 Excel 不允许的字符 [" + ch + "]：" + sheetName
                                + " / sheetName contains an invalid character [" + ch + "]: " + sheetName);
            }
        }
    }

    /** 校验列宽参数（POI 单列宽度上限为 255 个字符）。 */
    private static void validateColumnWidth(int minColumnWidth, int maxColumnWidth) {
        if (minColumnWidth < 1) {
            throw new IllegalArgumentException(
                    "minColumnWidth 不能小于 1，当前 " + minColumnWidth
                            + " / minColumnWidth must be at least 1, current " + minColumnWidth);
        }
        if (maxColumnWidth > 255) {
            throw new IllegalArgumentException(
                    "maxColumnWidth 不能大于 255，当前 " + maxColumnWidth
                            + " / maxColumnWidth must not exceed 255, current " + maxColumnWidth);
        }
        if (minColumnWidth > maxColumnWidth) {
            throw new IllegalArgumentException(
                    "minColumnWidth 不能大于 maxColumnWidth，当前 " + minColumnWidth + " > " + maxColumnWidth
                            + " / minColumnWidth must not exceed maxColumnWidth, current "
                            + minColumnWidth + " > " + maxColumnWidth);
        }
    }

    /**
     * 按布局渲染 Sheet。
     *
     * <p><b>关键顺序：</b></p>
     * <ol>
     *   <li>写表头 + 数据，同时统计每列的内容最大显示宽度 {@code columnWidths[]}；</li>
     *   <li>把 {@code columnWidths[]} clamp 成 {@code actualWidths[]} 并 {@code setColumnWidth}；</li>
     *   <li><b>再</b>用 {@code actualWidths[]} 遍历调行高（wrap 模式）。
     *       必须使用 clamp 后的实际列宽，否则当内容超长、列宽被截断时会算少行数，行高不变。</li>
     * </ol>
     */
    private static void renderSheet(Workbook wb, SheetLayout sheetLayout, WriteConfig config) {
        List<String> effectiveHeaders = sheetLayout.headers;
        int headerCols = effectiveHeaders.size();
        List<List<String>> rows = sheetLayout.rows;
        int maxCols = sheetLayout.maxCols;

        if (maxCols == 0) {
            wb.createSheet(config.getSheetName());
            return;
        }

        // 5. 创建 Sheet + 样式
        Sheet sheet = wb.createSheet(config.getSheetName());
        CellStyle dataStyle = createDataStyle(wb, config);

        // 6. 写表头行；未设置表头时不写空表头行，数据从第 0 行开始
        int dataStartRow = 0;
        if (headerCols > 0) {
            dataStartRow = 1;
            CellStyle headerStyle = createHeaderStyle(wb);
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < maxCols; c++) {
                String headerValue = c < headerCols ? effectiveHeaders.get(c) : "";
                Cell cell = headerRow.createCell(c);
                cell.setCellValue(headerValue == null ? "" : headerValue);
                cell.setCellStyle(headerStyle);
            }
        }

        // 7. 写数据行 + 统计每列内容最大显示宽度
        int[] columnWidths = new int[maxCols];
        if (headerCols > 0) {
            for (int c = 0; c < maxCols; c++) {
                columnWidths[c] = displayLength(c < headerCols ? effectiveHeaders.get(c) : "");
            }
        }
        for (int r = 0; r < rows.size(); r++) {
            List<String> values = rows.get(r);
            Row row = sheet.createRow(r + dataStartRow);
            int rowCols = values == null ? 0 : values.size();
            for (int c = 0; c < maxCols; c++) {
                String value = c < rowCols ? values.get(c) : "";
                if (value == null) value = "";
                Cell cell = row.createCell(c);
                writeCellValue(cell, value, config);
                cell.setCellStyle(dataStyle);
                int len = displayLength(value);
                if (len > columnWidths[c]) {
                    columnWidths[c] = len;
                }
            }
        }

        // 8. 计算"最终实际列宽"并 setColumnWidth
        //    注意：这一组 actualWidths 是 clamp 之后的真实列宽，行高估算必须用它
        int[] actualWidths = new int[maxCols];
        for (int c = 0; c < maxCols; c++) {
            int w;
            if (config.isAutoColumnWidth()) {
                // +2 padding
                w = columnWidths[c] + 2;
                if (w < config.getMinColumnWidth()) {
                    w = config.getMinColumnWidth();
                }
                if (w > config.getMaxColumnWidth()) {
                    w = config.getMaxColumnWidth();
                }
                sheet.setColumnWidth(c, w * 256);
            } else {
                // 不自动计算时使用 Excel 默认列宽（约 8.43 字符，这里取 8）
                w = 8;
            }
            actualWidths[c] = w;
        }

        // 9. wrap 模式下，用"实际列宽"调整表头行与数据行行高
        if (config.getTextOverflow() == TextOverflow.WRAP) {
            if (headerCols > 0) {
                Row headerRow = sheet.getRow(0);
                if (headerRow != null) {
                    adjustRowHeight(headerRow, maxCols, actualWidths);
                }
            }
            for (int r = 0; r < rows.size(); r++) {
                Row row = sheet.getRow(r + dataStartRow);
                if (row == null) {
                    continue;
                }
                adjustRowHeight(row, maxCols, actualWidths);
            }
        }

        // 10. 冻结首行（只在存在表头行时冻结，否则会冻结第一行数据）
        if (headerCols > 0 && config.isFreezeHeader()) {
            sheet.createFreezePane(0, 1);
        }
    }

    // ============================================================
    // 表头 / 数据解析
    // ============================================================

    /**
     * 解析有效表头。
     *
     * <p>Map 模式下，{@code truncateExtraData=false} 或 <b>未设置表头</b>时追加额外 key，
     * 避免无表头场景把 Map 数据整体丢弃。</p>
     */
    private static List<String> resolveHeaders(WriteConfig config) {
        List<String> headers = new ArrayList<>(config.getHeaders());
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i) == null) {
                headers.set(i, "");
            }
        }
        if ((!config.isTruncateExtraData() || headers.isEmpty()) && !config.getMapData().isEmpty()) {
            Set<String> existing = new HashSet<>(headers);
            LinkedHashSet<String> extraKeys = new LinkedHashSet<>();
            for (Map<String, String> row : config.getMapData()) {
                if (row == null) continue;
                for (String key : row.keySet()) {
                    if (key == null) continue;
                    if (!existing.contains(key) && !extraKeys.contains(key)) {
                        extraKeys.add(key);
                    }
                }
            }
            headers.addAll(extraKeys);
        }
        return headers;
    }

    /** 统一转为 {@code List<List<String>>}；Map 数据中的 null 行直接跳过，不产生空白行。 */
    private static List<List<String>> toRowList(WriteConfig config, List<String> effectiveHeaders) {
        if (!config.getMapData().isEmpty()) {
            List<List<String>> result = new ArrayList<>(config.getMapData().size());
            for (Map<String, String> mapRow : config.getMapData()) {
                if (mapRow == null) {
                    continue;
                }
                List<String> line = new ArrayList<>(effectiveHeaders.size());
                for (String header : effectiveHeaders) {
                    String v = mapRow.get(header);
                    line.add(v == null ? "" : v);
                }
                result.add(line);
            }
            return result;
        }
        if (!config.getListData().isEmpty()) {
            List<List<String>> result = new ArrayList<>(config.getListData().size());
            for (List<String> row : config.getListData()) {
                result.add(row == null ? new ArrayList<>() : new ArrayList<>(row));
            }
            return result;
        }
        return new ArrayList<>();
    }

    // ============================================================
    // 样式
    // ============================================================

    /** 表头样式：加粗、居中、灰底、边框、自动换行。 */
    private static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    /** 数据样式：边框、顶部对齐、wrapText 由 TextOverflow 决定。 */
    private static CellStyle createDataStyle(Workbook wb, WriteConfig config) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(config.getTextOverflow() == TextOverflow.WRAP);
        return style;
    }

    // ============================================================
    // 单元格值写入
    // ============================================================

    /** double 可精确表示的最大连续整数（2^53），超出后按文本写入。 */
    private static final long MAX_SAFE_LONG = 1L << 53;

    /** 写入单元格值：按 allAsText 决定统一文本还是自动识别类型。 */
    private static void writeCellValue(Cell cell, String value, WriteConfig config) {
        if (value == null) value = "";
        if (config.isAllAsText()) {
            cell.setCellValue(value);
            return;
        }
        if (isLong(value)) {
            long v = Long.parseLong(value);
            if (v > MAX_SAFE_LONG || v < -MAX_SAFE_LONG) {
                // 超过 2^53 的整数无法用 double 精确表示，按文本写入保留原始精度
                cell.setCellValue(value);
            } else {
                cell.setCellValue((double) v);
            }
            return;
        }
        if (isDouble(value)) {
            cell.setCellValue(Double.parseDouble(value));
            return;
        }
        if (isBoolean(value)) {
            cell.setCellValue(Boolean.parseBoolean(value));
            return;
        }
        cell.setCellValue(value);
    }

    private static boolean isLong(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            double d = Double.parseDouble(s);
            return !Double.isNaN(d) && !Double.isInfinite(d);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBoolean(String s) {
        return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
    }

    // ============================================================
    // 行高 / 列宽
    // ============================================================

    /**
     * wrap 模式下按内容估算行高。
     *
     * <p><b>关键点：</b>使用 {@code actualWidths[c]}（clamp 后的实际列宽）而不是原始内容长度，
     * 否则内容超长、列宽被 maxColumnWidth 截断时会把行数算少，行高不变。</p>
     *
     * <p>每行 15pt；POI 单行上限约 409pt，超过时取 409pt 避免溢出。</p>
     *
     * @param row          行
     * @param maxCols      总列数
     * @param actualWidths 每列的<b>实际显示宽度</b>（字符数）
     */
    private static void adjustRowHeight(Row row, int maxCols, int[] actualWidths) {
        int maxLines = 1;
        for (int c = 0; c < maxCols; c++) {
            Cell cell = row.getCell(c);
            if (cell == null) {
                continue;
            }
            // 只有文本才需要换行；数字 / 布尔 / 公式单元格按单行处理
            if (cell.getCellType() != CellType.STRING) {
                continue;
            }
            String text = cell.getStringCellValue();
            if (text == null || text.isEmpty()) {
                continue;
            }
            int colWidth = actualWidths[c] > 0 ? actualWidths[c] : 8;
            int lines = estimateLines(text, colWidth);
            if (lines > maxLines) {
                maxLines = lines;
            }
        }
        if (maxLines > 1) {
            float height = maxLines * 15f;
            // POI 单行高度上限约 409 磅，超出时钳制
            if (height > 409f) {
                height = 409f;
            }
            row.setHeightInPoints(height);
        }
    }

    /** 估算字符串在指定宽度（字符数）下会占据多少行。 */
    private static int estimateLines(String text, int colWidth) {
        if (colWidth <= 0) colWidth = 8;
        int lines = 0;
        // 按 \n 拆段，逐段估算
        String[] paragraphs = text.split("\n", -1);
        for (String p : paragraphs) {
            int len = displayLength(p);
            int sub = Math.max(1, (len + colWidth - 1) / colWidth);
            lines += sub;
        }
        return Math.max(1, lines);
    }

    /** 计算显示宽度：ASCII 字符按 1，其余（中文等）按 2。 */
    private static int displayLength(String s) {
        if (s == null) return 0;
        int len = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            len += (c < 128) ? 1 : 2;
        }
        return len;
    }
}