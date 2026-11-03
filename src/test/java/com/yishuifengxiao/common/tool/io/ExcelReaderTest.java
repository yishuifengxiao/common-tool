package com.yishuifengxiao.common.tool.io;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ExcelReader} 单元测试。
 *
 * <p>核心验证：</p>
 * <ul>
 *   <li><b>同步模式</b>：parse 返回时回调已全部执行完毕；</li>
 *   <li><b>异步模式</b>：parse 表头通过后<b>立即返回</b>，正文在后台流式回调；</li>
 *   <li>两种模式都返回结构相同的 {@link ExcelReader.ParseResult}；</li>
 *   <li>表头失败时两种模式都不读正文、不回调、不提交任务。</li>
 * </ul>
 */
class ExcelReaderTest {

    @TempDir
    Path tempDir;

    // ============================================================
    // 辅助方法
    // ============================================================

    private Path createExcel(String sheetName, int headerRowIndex,
                             String[] headers, Object[][] data) throws IOException {
        Path file = tempDir.resolve("test-" + UUID.randomUUID() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook();
             OutputStream out = Files.newOutputStream(file)) {
            Sheet sheet = wb.createSheet(sheetName);
            for (int i = 0; i < headerRowIndex; i++) {
                Row r = sheet.createRow(i);
                r.createCell(0).setCellValue("ignore-" + i);
            }
            Row headerRow = sheet.createRow(headerRowIndex);
            for (int c = 0; c < headers.length; c++) {
                if (headers[c] != null) {
                    headerRow.createCell(c).setCellValue(headers[c]);
                } else {
                    headerRow.createCell(c);
                }
            }
            for (int r = 0; r < data.length; r++) {
                Row row = sheet.createRow(headerRowIndex + 1 + r);
                for (int c = 0; c < data[r].length; c++) {
                    Object v = data[r][c];
                    if (v == null) continue;
                    if (v instanceof Number) {
                        row.createCell(c).setCellValue(((Number) v).doubleValue());
                    } else if (v instanceof Boolean) {
                        row.createCell(c).setCellValue((Boolean) v);
                    } else {
                        row.createCell(c).setCellValue(v.toString());
                    }
                }
            }
            wb.write(out);
        }
        return file;
    }

    private Path createMultiSheetExcel() throws IOException {
        Path file = tempDir.resolve("multi-" + UUID.randomUUID() + ".xlsx");
        try (Workbook wb = new XSSFWorkbook();
             OutputStream out = Files.newOutputStream(file)) {
            Sheet s0 = wb.createSheet("Sheet0");
            s0.createRow(0).createCell(0).setCellValue("WrongHeader");
            s0.createRow(1).createCell(0).setCellValue("dummy");

            Sheet s1 = wb.createSheet("Sheet1");
            Row h = s1.createRow(0);
            h.createCell(0).setCellValue("姓名");
            h.createCell(1).setCellValue("年龄");
            Row d = s1.createRow(1);
            d.createCell(0).setCellValue("张三");
            d.createCell(1).setCellValue(28);

            wb.write(out);
        }
        return file;
    }

    /** 回调数据收集器。 */
    private static final class Collector {
        final List<Integer> rowIndices = Collections.synchronizedList(new ArrayList<>());
        final List<LinkedHashMap<Integer, ExcelReader.CellData>> rows =
                Collections.synchronizedList(new ArrayList<>());

        ExcelReader.RowCallback callback() {
            return (rowIndex, rowData) -> {
                rowIndices.add(rowIndex);
                rows.add(rowData);
            };
        }

        int count() {
            return rows.size();
        }
    }

    // ============================================================
    // 1. 基础读取
    // ============================================================

    @Test
    @DisplayName("基础：双语表头，顺序严格，同步模式")
    void testBasicBilingualOrdered() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", "年龄", "部门"},
                new Object[][]{
                        {"张三", 28, "研发部"},
                        {"李四", 30, "市场部"},
                });

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.required("姓名", "Name"),
                ExcelReader.ColumnDef.required("年龄", "Age"),
                ExcelReader.ColumnDef.nullable("部门", "Department"));

        Collector collector = new Collector();
        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns), collector.callback());

        assertAll("基础解析",
                () -> assertTrue(r.isSuccess()),
                () -> assertEquals(ExcelReader.HeaderLanguage.ZH, r.getLanguage()),
                () -> assertEquals(2, collector.count()),
                () -> assertEquals("张三", collector.rows.get(0).get(0).getValue()),
                () -> assertEquals("研发部", collector.rows.get(0).get(2).getValue()));
    }

    // ============================================================
    // 2. rowData 契约
    // ============================================================

    @Test
    @DisplayName("乱序：rowData.keySet 按 columns 顺序；excelIdx 指向 Excel 实际列")
    void testUnorderedHeadersAndRowDataContract() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"部门", "姓名", "年龄"},
                new Object[][]{{"研发部", "张三", 28}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.required("姓名", "Name"),
                ExcelReader.ColumnDef.required("年龄", "Age"),
                ExcelReader.ColumnDef.nullable("部门", "Department"));

        AtomicReference<LinkedHashMap<Integer, ExcelReader.CellData>> captured = new AtomicReference<>();
        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(columns)
                        .setOrderRequired(false),
                (idx, rowData) -> captured.set(rowData));

        assertTrue(r.isSuccess());
        LinkedHashMap<Integer, ExcelReader.CellData> row = captured.get();
        assertAll("rowData 契约",
                () -> assertEquals(Arrays.asList(0, 1, 2), new ArrayList<>(row.keySet())),
                () -> assertEquals("姓名", row.get(0).getColumn().getChineseName()),
                () -> assertEquals(1, row.get(0).getExcelIdx()),
                () -> assertEquals("张三", row.get(0).getValue()),
                () -> assertEquals("年龄", row.get(1).getColumn().getChineseName()),
                () -> assertEquals(2, row.get(1).getExcelIdx()),
                () -> assertEquals("部门", row.get(2).getColumn().getChineseName()),
                () -> assertEquals(0, row.get(2).getExcelIdx()));
    }

    // ============================================================
    // 3. 表头校验
    // ============================================================

    @Test
    @DisplayName("额外列：allowExtraHeaders=true 通过；false 失败")
    void testExtraHeaders() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", "年龄", "部门", "备注"},
                new Object[][]{{"张三", 28, "研发部", "新入职"}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.required("姓名", "Name"),
                ExcelReader.ColumnDef.required("年龄", "Age"),
                ExcelReader.ColumnDef.nullable("部门", "Department"));

        ExcelReader.ParseResult allow = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAllowExtraHeaders(true),
                (idx, rowData) -> assertEquals(3, rowData.size()));

        ExcelReader.ParseResult strict = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAllowExtraHeaders(false),
                null);

        assertAll("额外列",
                () -> assertTrue(allow.isSuccess()),
                () -> assertFalse(strict.isSuccess()),
                () -> assertTrue(strict.getMessageZhShort().contains("额外表头")),
                () -> assertTrue(strict.getMessageEnShort().contains("Unexpected extra")));
    }

    @Test
    @DisplayName("缺列：少列失败；简写与详细区分")
    void testMissingHeader() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", "年龄"},
                new Object[][]{{"张三", 28}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.required("姓名", "Name"),
                ExcelReader.ColumnDef.required("年龄", "Age"),
                ExcelReader.ColumnDef.nullable("部门", "Department"));

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns), null);

        assertAll("缺列",
                () -> assertFalse(r.isSuccess()),
                () -> assertTrue(r.getMessageZh().contains("缺少必需的表头")),
                () -> assertTrue(r.getMessageEn().contains("Missing required")),
                () -> assertFalse(r.getMessageZhShort().contains("期望表头顺序")));
    }

    @Test
    @DisplayName("顺序错误：orderRequired=true 但顺序不同，失败")
    void testOrderMismatch() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"年龄", "姓名"},
                new Object[][]{{28, "张三"}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.required("姓名", "Name"),
                ExcelReader.ColumnDef.required("年龄", "Age"));

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(columns)
                        .setOrderRequired(true), null);

        assertAll("顺序错误",
                () -> assertFalse(r.isSuccess()),
                () -> assertTrue(r.getMessageZhShort().contains("第 1 个表头")),
                () -> assertTrue(r.getMessageZh().contains("期望表头顺序为")),
                () -> assertTrue(r.getMessageEn().contains("expected order")));
    }

    // ============================================================
    // 4. 空白列 / 归一化
    // ============================================================

    @Test
    @DisplayName("空白列：ignoreBlankColumns 控制")
    void testBlankColumns() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", null, "年龄"},
                new Object[][]{{"张三", null, 28}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.required("姓名", "Name"),
                ExcelReader.ColumnDef.required("年龄", "Age"));

        ExcelReader.ParseResult ignore = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setIgnoreBlankColumns(true),
                (idx, rowData) -> assertEquals(2, rowData.size()));

        ExcelReader.ParseResult keep = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setIgnoreBlankColumns(false),
                null);

        assertAll("空白列",
                () -> assertTrue(ignore.isSuccess()),
                () -> assertFalse(keep.isSuccess()),
                () -> assertTrue(keep.getMessageZhShort().contains("<空白列>")));
    }

    @Test
    @DisplayName("归一化：忽略不可见字符 + 自定义字符串")
    void testNormalization() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名*", "年龄（必填）"},
                new Object[][]{{"张三", 28}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.requiredZh("姓名"),
                ExcelReader.ColumnDef.requiredZh("年龄"));

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(columns)
                        .setIgnoreInvisibleChars(true)
                        .setIgnoredChars(Arrays.asList("*", "（必填）")),
                null);

        assertTrue(r.isSuccess(), r.getMessageZhShort());
    }

    // ============================================================
    // 5. 语言模式
    // ============================================================

    @Test
    @DisplayName("语言模式：只中文 / 只英文 / 部分双语 —— 自动正确判定")
    void testLanguageModes() throws Exception {
        Path zhFile = createExcel("Employees", 0,
                new String[]{"姓名", "年龄"}, new Object[][]{{"张三", 28}});
        Path enFile = createExcel("Employees", 0,
                new String[]{"Name", "Age"}, new Object[][]{{"张三", 28}});

        ExcelReader.ParseResult zhOnly = ExcelReader.parse(zhFile.toFile(),
                new ExcelReader.HeaderConfig().setColumns(
                        ExcelReader.ColumnDef.requiredZh("姓名"),
                        ExcelReader.ColumnDef.requiredZh("年龄")), null);

        ExcelReader.ParseResult enOnly = ExcelReader.parse(enFile.toFile(),
                new ExcelReader.HeaderConfig().setColumns(
                        ExcelReader.ColumnDef.requiredEn("Name"),
                        ExcelReader.ColumnDef.requiredEn("Age")), null);

        ExcelReader.ParseResult partial = ExcelReader.parse(zhFile.toFile(),
                new ExcelReader.HeaderConfig().setColumns(
                        ExcelReader.ColumnDef.required("姓名", "Name"),
                        ExcelReader.ColumnDef.requiredZh("年龄")), null);

        assertAll("语言模式",
                () -> assertTrue(zhOnly.isSuccess()),
                () -> assertSame(ExcelReader.HeaderLanguage.ZH, zhOnly.getLanguage()),
                () -> assertTrue(enOnly.isSuccess()),
                () -> assertSame(ExcelReader.HeaderLanguage.EN, enOnly.getLanguage()),
                () -> assertTrue(partial.isSuccess()),
                () -> assertSame(ExcelReader.HeaderLanguage.ZH, partial.getLanguage()));
    }

    @Test
    @DisplayName("语言混合：抛 IllegalArgumentException")
    void testMixedLanguageThrows() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", "Age"}, new Object[][]{{"张三", 28}});

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ExcelReader.parse(file.toFile(),
                        new ExcelReader.HeaderConfig().setColumns(
                                ExcelReader.ColumnDef.requiredZh("姓名"),
                                ExcelReader.ColumnDef.requiredEn("Age")), null));
        assertTrue(ex.getMessage().contains("语言不一致"));
    }

    // ============================================================
    // 6. 非空校验
    // ============================================================

    @Test
    @DisplayName("非空校验：CellData.valid + 中英双语错误")
    void testNonNullValidation() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", "年龄", "部门"},
                new Object[][]{
                        {"张三", 28, "研发部"},
                        {"李四", null, "市场部"},
                        {"王五", 25, null},
                });

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.required("姓名", "Name"),
                ExcelReader.ColumnDef.required("年龄", "Age"),
                ExcelReader.ColumnDef.nullable("部门", "Department"));

        List<List<Boolean>> rowValidity = new ArrayList<>();
        List<List<String>> rowErrorsZh = new ArrayList<>();
        List<List<String>> rowErrorsEn = new ArrayList<>();

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns),
                (rowIndex, rowData) -> {
                    rowValidity.add(rowData.values().stream()
                            .map(ExcelReader.CellData::isValid).collect(Collectors.toList()));
                    rowErrorsZh.add(rowData.values().stream()
                            .map(ExcelReader.CellData::getValidationErrorZh).collect(Collectors.toList()));
                    rowErrorsEn.add(rowData.values().stream()
                            .map(ExcelReader.CellData::getValidationErrorEn).collect(Collectors.toList()));
                });

        assertAll("非空校验",
                () -> assertTrue(r.isSuccess()),
                () -> assertEquals(3, rowValidity.size()),
                () -> assertEquals(Arrays.asList(true, true, true), rowValidity.get(0)),
                () -> assertEquals(Arrays.asList(true, false, true), rowValidity.get(1)),
                () -> assertNotNull(rowErrorsZh.get(1).get(1)),
                () -> assertTrue(rowErrorsZh.get(1).get(1).contains("年龄")),
                () -> assertTrue(rowErrorsZh.get(1).get(1).contains("必填")),
                () -> assertTrue(rowErrorsEn.get(1).get(1).contains("required but empty")),
                () -> assertEquals(Arrays.asList(true, true, true), rowValidity.get(2)),
                () -> assertNull(rowErrorsZh.get(2).get(2)));
    }

    // ============================================================
    // 7. 多输入源
    // ============================================================

    @Test
    @DisplayName("输入源：File / byte[] / InputStream / Workbook")
    void testAllInputSources() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名"}, new Object[][]{{"张三"}});

        List<ExcelReader.ColumnDef> columns =
                Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名"));
        ExcelReader.HeaderConfig config = new ExcelReader.HeaderConfig().setColumns(columns);

        Collector c1 = new Collector();
        ExcelReader.ParseResult fromFile = ExcelReader.parse(file.toFile(), config, c1.callback());

        Collector c2 = new Collector();
        ExcelReader.ParseResult fromBytes = ExcelReader.parse(Files.readAllBytes(file), config, c2.callback());

        Collector c3 = new Collector();
        ExcelReader.ParseResult fromStream;
        try (InputStream in = Files.newInputStream(file)) {
            fromStream = ExcelReader.parse(in, config, c3.callback());
        }

        Collector c4 = new Collector();
        ExcelReader.ParseResult fromWorkbook;
        try (Workbook wb = new XSSFWorkbook(Files.newInputStream(file))) {
            fromWorkbook = ExcelReader.parse(wb, config, c4.callback());
        }

        assertAll("多输入源",
                () -> assertTrue(fromFile.isSuccess()),
                () -> assertTrue(fromBytes.isSuccess()),
                () -> assertTrue(fromStream.isSuccess()),
                () -> assertTrue(fromWorkbook.isSuccess()),
                () -> assertEquals(1, c1.count()),
                () -> assertEquals(1, c2.count()),
                () -> assertEquals(1, c3.count()),
                () -> assertEquals(1, c4.count()));
    }

    // ============================================================
    // 8. sheetIndex / headerRowIndex
    // ============================================================

    @Test
    @DisplayName("定位：可指定工作表索引和表头行索引")
    void testSheetAndHeaderRowIndex() throws Exception {
        Path file = createMultiSheetExcel();

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.requiredZh("姓名"),
                ExcelReader.ColumnDef.requiredZh("年龄"));

        ExcelReader.ParseResult s0 = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns), null);
        ExcelReader.ParseResult s1 = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setSheetIndex(1), null);

        Path headerRow3 = createExcel("Employees", 2,
                new String[]{"姓名", "年龄"}, new Object[][]{{"张三", 28}});
        Collector c3 = new Collector();
        ExcelReader.ParseResult r3 = ExcelReader.parse(headerRow3.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setHeaderRowIndex(2),
                c3.callback());

        assertAll("定位",
                () -> assertFalse(s0.isSuccess()),
                () -> assertTrue(s1.isSuccess()),
                () -> assertTrue(r3.isSuccess()),
                () -> assertEquals(1, c3.count()));
    }

    @Test
    @DisplayName("定位：工作表索引越界时直接失败，不回退到最后一张表")
    void testSheetIndexOutOfRangeFails() throws Exception {
        Path file = createMultiSheetExcel();
        Collector c = new Collector();

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(Arrays.asList(
                                ExcelReader.ColumnDef.requiredZh("姓名"),
                                ExcelReader.ColumnDef.requiredZh("年龄")))
                        .setSheetIndex(5),
                c.callback());

        assertAll("越界",
                () -> assertTrue(r.isFailed()),
                () -> assertTrue(r.getMessageZhShort().contains("超出范围")),
                () -> assertEquals(0, c.count()));
    }

    // ============================================================
    // 9. 异步核心：parse 立即返回 + 后台流式回调
    // ============================================================

    @Test
    @DisplayName("异步核心：parse 表头通过后立即返回，正文在后台流式回调")
    void testAsyncReturnsImmediately() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名"},
                new Object[][]{{"张三"}, {"李四"}, {"王五"}});

        // 用 CountDownLatch 阻塞第一行回调，人为制造"后台未完成"状态
        CountDownLatch blockFirst = new CountDownLatch(1);
        CountDownLatch allDone = new CountDownLatch(3);
        AtomicInteger callCount = new AtomicInteger(0);

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名")))
                        .setAsync(true),
                (idx, rowData) -> {
                    int c = callCount.incrementAndGet();
                    if (c == 1) {
                        try {
                            blockFirst.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    allDone.countDown();
                });

        // 关键断言：parse 已返回，但正文还没处理完
        assertAll("parse 立即返回",
                () -> assertTrue(r.isSuccess(), "表头应已通过"),
                () -> assertTrue(allDone.getCount() > 0, "parse 返回时正文不应全部完成"),
                () -> assertTrue(callCount.get() <= 1, "回调尚未执行完毕"));

        // 放行第一行，等待所有行完成
        blockFirst.countDown();
        assertTrue(allDone.await(5, TimeUnit.SECONDS), "异步正文应在 5 秒内完成");
        assertEquals(3, callCount.get());
    }

    @Test
    @DisplayName("异步：线程池拒绝任务时返回失败结果，不抛出异常")
    void testAsyncRejectedExecutorReturnsFailure() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名"}, new Object[][]{{"张三"}});

        ExecutorService rejected = Executors.newSingleThreadExecutor();
        rejected.shutdown();

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名")))
                        .setAsync(true)
                        .setExecutor(rejected),
                (idx, rowData) -> {
                });

        assertAll("提交失败",
                () -> assertTrue(r.isFailed()),
                () -> assertTrue(r.getMessageZhShort().contains("提交失败")),
                () -> assertTrue(r.getMessageEnShort().contains("submit")));
    }

    @Test
    @DisplayName("异步：最终所有数据都通过流式回调处理完毕")
    void testAsyncAllRowsDelivered() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", "年龄"},
                new Object[][]{{"张三", 28}, {"李四", 30}, {"王五", 32}});

        CountDownLatch allDone = new CountDownLatch(3);
        List<String> names = Collections.synchronizedList(new ArrayList<>());

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(Arrays.asList(
                                ExcelReader.ColumnDef.requiredZh("姓名"),
                                ExcelReader.ColumnDef.requiredZh("年龄")))
                        .setAsync(true),
                (idx, rowData) -> {
                    names.add(rowData.get(0).getValue());
                    allDone.countDown();
                });

        assertTrue(r.isSuccess());
        assertTrue(allDone.await(5, TimeUnit.SECONDS), "所有回调应在 5 秒内完成");
        assertEquals(3, names.size());
        assertTrue(names.contains("张三"));
        assertTrue(names.contains("李四"));
        assertTrue(names.contains("王五"));
    }

    @Test
    @DisplayName("异步：表头失败立即返回，不回调、不提交任务")
    void testAsyncHeaderFailureNoCallback() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"错误表头"}, new Object[][]{{"数据"}});

        AtomicInteger callCount = new AtomicInteger(0);
        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig()
                        .setColumns(Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名")))
                        .setAsync(true),
                (idx, rowData) -> callCount.incrementAndGet());

        // 给后台任务一点时间（如果有的话）
        Thread.sleep(100);

        assertAll("异步表头失败",
                () -> assertFalse(r.isSuccess()),
                () -> assertEquals(0, callCount.get(), "回调一次都不应被调用"),
                () -> assertTrue(r.getMessageZhShort().contains("表头校验失败")),
                () -> assertNull(r.getLanguage()));
    }

    @Test
    @DisplayName("异步：自定义 Executor 生效，回调在指定线程执行")
    void testAsyncCustomExecutor() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名"}, new Object[][]{{"张三"}});

        AtomicReference<String> workerThread = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        java.util.concurrent.ExecutorService pool =
                java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "excel-body-thread");
                    t.setDaemon(true);
                    return t;
                });
        try {
            ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                    new ExcelReader.HeaderConfig()
                            .setColumns(Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名")))
                            .setAsync(true)
                            .setExecutor(pool),
                    (idx, rowData) -> {
                        workerThread.compareAndSet(null, Thread.currentThread().getName());
                        done.countDown();
                    });

            assertTrue(r.isSuccess());
            assertTrue(done.await(5, TimeUnit.SECONDS));
            assertEquals("excel-body-thread", workerThread.get());
        } finally {
            pool.shutdown();
        }
    }

    // ============================================================
    // 10. API 一致性（ParseResult 结构）
    // ============================================================

    @Test
    @DisplayName("API 一致性：同步 / 异步返回的 ParseResult 结构、方法、行为完全一致")
    void testSyncAsyncParseResultConsistency() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名", "年龄"},
                new Object[][]{{"张三", 28}, {"李四", 30}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.requiredZh("姓名"),
                ExcelReader.ColumnDef.requiredZh("年龄"));

        // 同步
        Collector syncCollector = new Collector();
        ExcelReader.ParseResult syncR = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAsync(false),
                syncCollector.callback());

        // 异步
        CountDownLatch asyncDone = new CountDownLatch(2);
        List<LinkedHashMap<Integer, ExcelReader.CellData>> asyncRows =
                Collections.synchronizedList(new ArrayList<>());
        ExcelReader.ParseResult asyncR = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAsync(true),
                (idx, rowData) -> {
                    asyncRows.add(rowData);
                    asyncDone.countDown();
                });
        assertTrue(asyncDone.await(5, TimeUnit.SECONDS));

        // ParseResult 结构、方法、行为完全一致
        assertAll("ParseResult 一致",
                () -> assertEquals(syncR.isSuccess(), asyncR.isSuccess()),
                () -> assertSame(syncR.getLanguage(), asyncR.getLanguage()),
                () -> assertEquals(syncR.getMessageZhShort(), asyncR.getMessageZhShort()),
                () -> assertEquals(syncR.getMessageEnShort(), asyncR.getMessageEnShort()),
                () -> assertEquals(syncR.getMessageZh(), asyncR.getMessageZh()),
                () -> assertEquals(syncR.getMessageEn(), asyncR.getMessageEn()),
                () -> assertEquals(syncR.getHeaders().size(), asyncR.getHeaders().size()),
                () -> assertEquals(syncR.getHeaders().get(0).getValue(),
                        asyncR.getHeaders().get(0).getValue()),
                // 数据内容一致
                () -> assertEquals(2, syncCollector.count()),
                () -> assertEquals(2, asyncRows.size()),
                () -> assertEquals(syncCollector.rows.get(0).get(0).getValue(),
                        asyncRows.get(0).get(0).getValue()),
                () -> assertEquals(syncCollector.rows.get(1).get(0).getValue(),
                        asyncRows.get(1).get(0).getValue()));
    }

    // ============================================================
    // 11. 异常 / 边界
    // ============================================================

    @Test
    @DisplayName("异常：表头行全空 / 表头行不存在")
    void testErrorPaths() throws Exception {
        List<ExcelReader.ColumnDef> columns =
                Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名"));

        Path emptyHeader = createExcel("Employees", 0, new String[]{}, new Object[][]{});
        ExcelReader.ParseResult r1 = ExcelReader.parse(emptyHeader.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns), null);

        Path noHeader = createExcel("Employees", 0, new String[]{"姓名"},
                new Object[][]{{"张三"}});
        ExcelReader.ParseResult r2 = ExcelReader.parse(noHeader.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setHeaderRowIndex(100), null);

        assertAll("异常路径",
                () -> assertFalse(r1.isSuccess()),
                () -> assertTrue(r1.getMessageZh().contains("没有任何有效内容")),
                () -> assertFalse(r2.isSuccess()),
                () -> assertTrue(r2.getMessageZh().contains("表头行不存在")));
    }

    @Test
    @DisplayName("异常：columns 为空或含 null 元素")
    void testInvalidColumns() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名"}, new Object[][]{{"张三"}});

        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class,
                () -> ExcelReader.parse(file.toFile(), new ExcelReader.HeaderConfig(), null));

        List<ExcelReader.ColumnDef> withNull = new ArrayList<>();
        withNull.add(ExcelReader.ColumnDef.requiredZh("姓名"));
        withNull.add(null);
        IllegalArgumentException e2 = assertThrows(IllegalArgumentException.class,
                () -> ExcelReader.parse(file.toFile(),
                        new ExcelReader.HeaderConfig().setColumns(withNull), null));

        assertAll("非法 columns",
                () -> assertTrue(e1.getMessage().contains("至少提供一个列定义")),
                () -> assertTrue(e2.getMessage().contains("不允许包含 null")));
    }

    @Test
    @DisplayName("异常：ColumnDef 中英文名都为空时构造抛 IllegalArgumentException")
    void testColumnDefValidation() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new ExcelReader.ColumnDef(null, "", false));
        assertTrue(ex.getMessage().contains("中英文名不能都为空"));
    }

    // ============================================================
    // 12. headers 回传 / null callback / 空数据
    // ============================================================

    @Test
    @DisplayName("headers：按 columns 顺序回传，含 Excel 实际列索引")
    void testHeadersReported() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"部门", "姓名"},
                new Object[][]{{"研发部", "张三"}});

        List<ExcelReader.ColumnDef> columns = Arrays.asList(
                ExcelReader.ColumnDef.requiredZh("姓名"),
                ExcelReader.ColumnDef.nullableZh("部门"));

        ExcelReader.ParseResult r = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setOrderRequired(false), null);

        assertTrue(r.isSuccess());
        List<ExcelReader.HeaderCellInfo> headers = r.getHeaders();
        assertAll("headers 回传",
                () -> assertEquals(2, headers.size()),
                () -> assertEquals("姓名", headers.get(0).getValue()),
                () -> assertEquals(1, headers.get(0).getColumnIndex()),
                () -> assertEquals("部门", headers.get(1).getValue()),
                () -> assertEquals(0, headers.get(1).getColumnIndex()));
    }

    @Test
    @DisplayName("callback=null：同步 / 异步模式都能成功解析")
    void testNullCallback() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名"}, new Object[][]{{"张三"}});

        List<ExcelReader.ColumnDef> columns =
                Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名"));

        ExcelReader.ParseResult sync = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAsync(false), null);
        ExcelReader.ParseResult async = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAsync(true), null);

        assertAll("null callback",
                () -> assertTrue(sync.isSuccess()),
                () -> assertTrue(async.isSuccess()),
                () -> assertSame(sync.getLanguage(), async.getLanguage()));
    }

    @Test
    @DisplayName("空数据：只有表头，两种模式都成功")
    void testEmptyData() throws Exception {
        Path file = createExcel("Employees", 0,
                new String[]{"姓名"}, new Object[][]{});

        List<ExcelReader.ColumnDef> columns =
                Collections.singletonList(ExcelReader.ColumnDef.requiredZh("姓名"));

        AtomicBoolean anyCallback = new AtomicBoolean(false);
        ExcelReader.ParseResult sync = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAsync(false),
                (idx, rowData) -> anyCallback.set(true));

        CountDownLatch asyncDone = new CountDownLatch(1);
        AtomicInteger asyncCount = new AtomicInteger(0);
        ExcelReader.ParseResult async = ExcelReader.parse(file.toFile(),
                new ExcelReader.HeaderConfig().setColumns(columns).setAsync(true),
                (idx, rowData) -> asyncCount.incrementAndGet());

        // 给异步任务一点时间
        Thread.sleep(100);

        assertAll("空数据",
                () -> assertTrue(sync.isSuccess()),
                () -> assertTrue(async.isSuccess()),
                () -> assertFalse(anyCallback.get(), "同步：无数据不应回调"),
                () -> assertEquals(0, asyncCount.get(), "异步：无数据不应回调"));
    }
}