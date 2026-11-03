package com.yishuifengxiao.common.tool.io;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link ExcelWriter} 的单元测试。
 */
class ExcelWriterTest {

    @TempDir
    Path tempDir;

    private static Workbook open(byte[] bytes) throws IOException {
        return WorkbookFactory.create(new ByteArrayInputStream(bytes));
    }

    private static long countEntries(Path dir) throws IOException {
        try (var stream = Files.list(dir)) {
            return stream.count();
        }
    }

    // ============================================================
    // 格式识别
    // ============================================================

    @Test
    @DisplayName("格式识别：detect() 大小写不敏感，只识别 .xls / .xlsx")
    void testDetect() {
        assertEquals(ExcelWriter.ExcelFormat.XLS, ExcelWriter.ExcelFormat.detect("a.xls"));
        assertEquals(ExcelWriter.ExcelFormat.XLS, ExcelWriter.ExcelFormat.detect("a.XLS"));
        assertEquals(ExcelWriter.ExcelFormat.XLSX, ExcelWriter.ExcelFormat.detect("a.xlsx"));
        assertEquals(ExcelWriter.ExcelFormat.XLSX, ExcelWriter.ExcelFormat.detect("A.XLSX"));
        assertEquals(ExcelWriter.ExcelFormat.XLS, ExcelWriter.ExcelFormat.detect("/path/to/a.xls"));
        assertEquals(ExcelWriter.ExcelFormat.XLSX, ExcelWriter.ExcelFormat.detect("/path/to/a.xlsx"));
        assertNull(ExcelWriter.ExcelFormat.detect("a.csv"));
        assertNull(ExcelWriter.ExcelFormat.detect("a"));
        assertNull(ExcelWriter.ExcelFormat.detect(null));
    }

    @Test
    @DisplayName("写 .xls 文件：生成 HSSFWorkbook")
    void testWriteXls() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        Path file = tempDir.resolve("out.xls");
        ExcelWriter.write(file.toFile(), cfg);

        try (InputStream in = Files.newInputStream(file);
             Workbook wb = WorkbookFactory.create(in)) {
            assertTrue(wb instanceof HSSFWorkbook, "应为 HSSFWorkbook");
            assertEquals("A", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
    }

    @Test
    @DisplayName("写 .xlsx 文件：生成 XSSFWorkbook")
    void testWriteXlsx() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        Path file = tempDir.resolve("out.xlsx");
        ExcelWriter.write(file.toFile(), cfg);

        try (InputStream in = Files.newInputStream(file);
             Workbook wb = WorkbookFactory.create(in)) {
            assertTrue(wb instanceof XSSFWorkbook, "应为 XSSFWorkbook");
        }
    }

    @Test
    @DisplayName("无法识别的文件后缀：抛 IllegalArgumentException")
    void testUnknownExtension() {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        Path file = tempDir.resolve("out.csv");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ExcelWriter.write(file.toFile(), cfg));
        assertTrue(ex.getMessage().contains(".xls"));
        assertTrue(ex.getMessage().contains(".xlsx"));
    }

    @Test
    @DisplayName("OutputStream：显式指定 xls / xlsx 都能正确写出")
    void testOutputStreamWithFormat() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        // xls
        ByteArrayOutputStream xlsOut = new ByteArrayOutputStream();
        ExcelWriter.write(xlsOut, cfg, ExcelWriter.ExcelFormat.XLS);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(xlsOut.toByteArray()))) {
            assertTrue(wb instanceof HSSFWorkbook);
        }

        // xlsx
        ByteArrayOutputStream xlsxOut = new ByteArrayOutputStream();
        ExcelWriter.write(xlsxOut, cfg, ExcelWriter.ExcelFormat.XLSX);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(xlsxOut.toByteArray()))) {
            assertTrue(wb instanceof XSSFWorkbook);
        }

        // OutputStream 默认 xlsx
        ByteArrayOutputStream defOut = new ByteArrayOutputStream();
        ExcelWriter.write(defOut, cfg);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(defOut.toByteArray()))) {
            assertTrue(wb instanceof XSSFWorkbook);
        }
    }

    @Test
    @DisplayName("writeToBytes：默认 xlsx / 显式 xls 都可用")
    void testWriteToBytesWithFormat() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        byte[] defaultBytes = ExcelWriter.writeToBytes(cfg);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(defaultBytes))) {
            assertTrue(wb instanceof XSSFWorkbook);
        }

        byte[] xlsBytes = ExcelWriter.writeToBytes(cfg, ExcelWriter.ExcelFormat.XLS);
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(xlsBytes))) {
            assertTrue(wb instanceof HSSFWorkbook);
        }
    }

    @Test
    @DisplayName("HSSF 边界：.xls 超过 65536 行或 256 列时提前抛异常")
    void testXlsLimits() throws IOException {
        // 257 列
        String[] headers = new String[257];
        Arrays.fill(headers, "h");
        ExcelWriter.WriteConfig tooManyCols = new ExcelWriter.WriteConfig()
                .setHeaders(Arrays.asList(headers))
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ExcelWriter.writeToBytes(tooManyCols, ExcelWriter.ExcelFormat.XLS));
        assertTrue(ex.getMessage().contains("256"));

        // 不超限时正常
        byte[] ok = ExcelWriter.writeToBytes(
                new ExcelWriter.WriteConfig().setHeaders("A")
                        .setListData(Collections.singletonList(Collections.singletonList("v"))),
                ExcelWriter.ExcelFormat.XLS);
        assertNotNull(ok);
    }

    // ============================================================
    // 以下为基础功能回归测试
    // ============================================================

    @Test
    @DisplayName("List 数据：表头与数据都正确写入")
    void testBasicListWrite() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("姓名", "年龄", "部门")
                .setListData(Arrays.asList(
                        Arrays.asList("张三", "28", "研发部"),
                        Arrays.asList("李四", "30", "市场部")));

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals("姓名", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("研发部", sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals("李四", sheet.getRow(2).getCell(0).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Map 数据：按表头顺序取值")
    void testMapWrite() throws Exception {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("姓名", "张三");
        row.put("部门", "研发部");

        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("部门", "姓名")
                .setMapData(Collections.singletonList(row));

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals("研发部", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("张三", sheet.getRow(1).getCell(1).getStringCellValue());
        }
    }

    @Test
    @DisplayName("truncateExtraData=true：忽略多余数据")
    void testTruncate() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A", "B")
                .setListData(Collections.singletonList(Arrays.asList("a1", "b1", "c1")))
                .setTruncateExtraData(true);

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            assertEquals(2, wb.getSheetAt(0).getRow(1).getLastCellNum());
        }
    }

    @Test
    @DisplayName("truncateExtraData=false：表头不足补空")
    void testExtendHeader() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Arrays.asList("a1", "b1", "c1")))
                .setTruncateExtraData(false);

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals(3, sheet.getRow(0).getLastCellNum());
            assertEquals("", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("c1", sheet.getRow(1).getCell(2).getStringCellValue());
        }
    }

    @Test
    @DisplayName("wrap 模式：wrapText=true；hide 模式：wrapText=false")
    void testTextOverflow() throws Exception {
        String longText = "很长很长的测试文本".repeat(20);

        byte[] wrap = ExcelWriter.writeToBytes(new ExcelWriter.WriteConfig()
                .setHeaders("内容")
                .setListData(Collections.singletonList(Collections.singletonList(longText)))
                .setTextOverflow(ExcelWriter.TextOverflow.WRAP));
        byte[] hide = ExcelWriter.writeToBytes(new ExcelWriter.WriteConfig()
                .setHeaders("内容")
                .setListData(Collections.singletonList(Collections.singletonList(longText)))
                .setTextOverflow(ExcelWriter.TextOverflow.HIDE));

        try (Workbook wbWrap = open(wrap); Workbook wbHide = open(hide)) {
            Cell wrapCell = wbWrap.getSheetAt(0).getRow(1).getCell(0);
            Cell hideCell = wbHide.getSheetAt(0).getRow(1).getCell(0);
            assertTrue(wrapCell.getCellStyle().getWrapText());
            assertFalse(hideCell.getCellStyle().getWrapText());
            assertTrue(wbWrap.getSheetAt(0).getRow(1).getHeightInPoints() > 15f);
        }
    }

    @Test
    @DisplayName("allAsText=true：数字按文本写入；false：识别为数字")
    void testAllAsText() throws Exception {
        byte[] asText = ExcelWriter.writeToBytes(new ExcelWriter.WriteConfig()
                .setHeaders("编号")
                .setListData(Collections.singletonList(Collections.singletonList("001234")))
                .setAllAsText(true));
        try (Workbook wb = open(asText)) {
            Cell cell = wb.getSheetAt(0).getRow(1).getCell(0);
            assertEquals(CellType.STRING, cell.getCellType());
            assertEquals("001234", cell.getStringCellValue());
        }

        byte[] asNumber = ExcelWriter.writeToBytes(new ExcelWriter.WriteConfig()
                .setHeaders("编号")
                .setListData(Collections.singletonList(Collections.singletonList("1234")))
                .setAllAsText(false));
        try (Workbook wb = open(asNumber)) {
            Cell cell = wb.getSheetAt(0).getRow(1).getCell(0);
            assertEquals(CellType.NUMERIC, cell.getCellType());
            assertEquals(1234.0, cell.getNumericCellValue(), 0.0001);
        }
    }

    @Test
    @DisplayName("列宽自适应：有上下限，长列宽于短列")
    void testColumnWidth() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("短", "长")
                .setListData(Collections.singletonList(
                        Arrays.asList("x", "很长很长".repeat(50))))
                .setMinColumnWidth(8)
                .setMaxColumnWidth(60);

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            Sheet sheet = wb.getSheetAt(0);
            int w0 = sheet.getColumnWidth(0);
            int w1 = sheet.getColumnWidth(1);
            assertTrue(w1 > w0);
            assertTrue(w0 >= 8 * 256);
            assertTrue(w1 <= 60 * 256);
        }
    }

    @Test
    @DisplayName("边界：headers 有、数据为空时只写表头")
    void testEmptyData() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A", "B", "C")
                .setListData(Collections.emptyList());

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals(0, sheet.getLastRowNum(), "只有表头一行");
            assertEquals(3, sheet.getRow(0).getLastCellNum());
        }
    }

    @Test
    @DisplayName("边界：OutputStream 不能被关闭（方法只写不 close）")
    void testOutputStreamNotClosed() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        // 用一个"写入后仍能继续用"的流来验证
        ByteArrayOutputStream baos = new ByteArrayOutputStream() {
            @Override
            public void close() {
                throw new IllegalStateException("不应被关闭");
            }
        };
        ExcelWriter.write(baos, cfg);           // 不应触发 close()
        assertTrue(baos.size() > 0);

        // 结束后继续写也没问题
        OutputStream out = baos;
        out.write(new byte[]{1, 2, 3});
    }

    // ============================================================
    // 写入安全：失败不破坏已有文件
    // ============================================================

    @Test
    @DisplayName("write(File)：config 非法时保留原文件内容，且不残留临时文件")
    void testWriteFileKeepsExistingContentWhenConfigInvalid() throws Exception {
        Path file = tempDir.resolve("existing.xlsx");
        Files.writeString(file, "ORIGINAL");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ExcelWriter.write(file.toFile(), null));
        assertTrue(ex.getMessage().contains("config"));

        assertEquals("ORIGINAL", Files.readString(file));
        assertEquals(1, countEntries(tempDir), "失败后只应剩下原文件，不应残留临时文件");
    }

    @Test
    @DisplayName("write(File)：xls 超限时保留原文件内容，且不残留临时文件")
    void testWriteFileKeepsExistingContentWhenXlsLimitExceeded() throws Exception {
        Path file = tempDir.resolve("existing.xls");
        Files.writeString(file, "ORIGINAL");

        String[] headers = new String[257];
        Arrays.fill(headers, "h");
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setHeaders(Arrays.asList(headers))
                .setListData(Collections.singletonList(Collections.singletonList("v")));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ExcelWriter.write(file.toFile(), cfg));
        assertTrue(ex.getMessage().contains("256"));

        assertEquals("ORIGINAL", Files.readString(file));
        assertEquals(1, countEntries(tempDir));
    }

    @Test
    @DisplayName("正常覆盖：write(File) 覆盖已有文件后内容完整")
    void testWriteFileReplacesExistingContent() throws Exception {
        Path file = tempDir.resolve("existing.xlsx");
        Files.writeString(file, "ORIGINAL");

        ExcelWriter.write(file.toFile(), new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v"))));

        try (InputStream in = Files.newInputStream(file);
             Workbook wb = WorkbookFactory.create(in)) {
            assertEquals("A", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
        assertEquals(1, countEntries(tempDir));
    }

    @Test
    @DisplayName("write(File)：通过符号链接写入时更新链接目标，链接本身保留")
    void testWriteFileThroughSymlink() throws Exception {
        Path real = tempDir.resolve("real.xlsx");
        Path link = tempDir.resolve("link.xlsx");
        try {
            Files.createSymbolicLink(link, real.getFileName());
        } catch (IOException | UnsupportedOperationException e) {
            Assumptions.assumeTrue(false, "当前环境不支持创建符号链接");
            return;
        }

        ExcelWriter.write(link.toFile(), new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v"))));

        assertTrue(Files.isSymbolicLink(link), "符号链接本身应保留");
        try (InputStream in = Files.newInputStream(real);
             Workbook wb = WorkbookFactory.create(in)) {
            assertEquals("A", wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue());
        }
        assertEquals(2, countEntries(tempDir), "应只有真实文件与链接，且无临时文件残留");
    }

    @Test
    @DisplayName("write(File)：POSIX 平台保留目标文件原有权限")
    void testWriteFilePreservesPosixPermissions() throws Exception {
        Assumptions.assumeTrue(
                Files.getFileStore(tempDir).supportsFileAttributeView(PosixFileAttributeView.class),
                "当前文件系统不支持 POSIX 权限");

        Path file = tempDir.resolve("perm.xlsx");
        Files.writeString(file, "ORIGINAL");
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-r--r--"));

        ExcelWriter.write(file.toFile(), new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v"))));

        assertEquals(PosixFilePermissions.fromString("rw-r--r--"),
                Files.getPosixFilePermissions(file));
    }

    @Test
    @DisplayName("无表头但有数据：不写空表头行，数据从第 0 行开始")
    void testNoHeadersWithData() throws Exception {
        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setListData(Arrays.asList(
                        Arrays.asList("a1", "b1"),
                        Arrays.asList("a2", "b2", "c2")));

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals(1, sheet.getLastRowNum(), "无表头时只有数据行");
            assertEquals(3, sheet.getRow(0).getLastCellNum());
            assertEquals(3, sheet.getRow(1).getLastCellNum());
            assertEquals("a1", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("c2", sheet.getRow(1).getCell(2).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Map 数据 + 无表头：以 Map key 作为表头，数据不丢失")
    void testMapDataWithoutHeaders() throws Exception {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("姓名", "张三");
        row.put("部门", "研发部");

        ExcelWriter.WriteConfig cfg = new ExcelWriter.WriteConfig()
                .setMapData(Collections.singletonList(row));

        try (Workbook wb = open(ExcelWriter.writeToBytes(cfg))) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals(1, sheet.getLastRowNum(), "表头 + 1 行数据");
            assertEquals("姓名", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("部门", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("张三", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("研发部", sheet.getRow(1).getCell(1).getStringCellValue());
        }
    }

    @Test
    @DisplayName("参数校验：sheetName 与列宽非法时提前报错")
    void testInvalidSheetNameAndColumnWidth() {
        ExcelWriter.WriteConfig tooLongName = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")))
                .setSheetName("x".repeat(32));
        assertThrows(IllegalArgumentException.class, () -> ExcelWriter.writeToBytes(tooLongName));

        ExcelWriter.WriteConfig tooWide = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")))
                .setMaxColumnWidth(300);
        assertThrows(IllegalArgumentException.class, () -> ExcelWriter.writeToBytes(tooWide));

        ExcelWriter.WriteConfig reversed = new ExcelWriter.WriteConfig()
                .setHeaders("A")
                .setListData(Collections.singletonList(Collections.singletonList("v")))
                .setMinColumnWidth(80)
                .setMaxColumnWidth(60);
        assertThrows(IllegalArgumentException.class, () -> ExcelWriter.writeToBytes(reversed));
    }
}