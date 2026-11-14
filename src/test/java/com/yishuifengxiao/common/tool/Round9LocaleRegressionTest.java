package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.http.UrlUtil;
import com.yishuifengxiao.common.tool.io.IoUtil;
import com.yishuifengxiao.common.tool.lang.OID;
import com.yishuifengxiao.common.tool.utils.LogLevelUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 第 9 轮回归测试：消除隐式默认 Locale 依赖
 *
 * <p>背景：工具类中用于协议、标识符、类型名匹配的 {@code toLowerCase()} / {@code toUpperCase()}
 * 此前未指定 Locale。在土耳其语（tr-TR）等默认 Locale 下，ASCII 字母 {@code I/i} 的大小写
 * 映射会发生变化（{@code "I" -> "ı"}、{@code "i" -> "İ"}），导致字符串匹配失败。本测试在
 * 土耳其语环境下验证相关公共 API 的行为与默认 Locale 下保持一致。</p>
 *
 * <p>注意：每个用例都会临时修改 JVM 默认 Locale，必须在 {@code finally} 中恢复，
 * 避免污染同 JVM 中的其他测试。</p>
 *
 * @author qingteng
 * @version 1.0.0
 */
@DisplayName("第9轮：消除隐式默认 Locale 依赖回归测试")
public class Round9LocaleRegressionTest {

    /**
     * 土耳其语 Locale，用于触发 ASCII I/i 大小写映射差异
     */
    private static final Locale TURKISH = Locale.forLanguageTag("tr-TR");

    /**
     * 在土耳其语默认 Locale 下执行给定逻辑，执行完毕后恢复原 Locale
     *
     * @param supplier 待执行的逻辑
     * @param <T>      返回类型
     * @return 逻辑执行结果
     */
    private <T> T underTurkishLocale(Supplier<T> supplier) {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(TURKISH);
            return supplier.get();
        } finally {
            Locale.setDefault(original);
        }
    }

    /**
     * 文件后缀归一化应固定使用 Locale.ROOT
     */
    @Test
    @DisplayName("土耳其语环境下 IoUtil.suffix 仍返回 ASCII 小写后缀")
    void ioUtilSuffixIsLocaleIndependent() {
        // 修复前："INI".toLowerCase(tr-TR) 会得到 "ını"，与 "ini" 不相等
        assertEquals("ini", underTurkishLocale(() -> IoUtil.suffix("FILE.INI")));
    }

    /**
     * 域名归一化应固定使用 Locale.ROOT
     */
    @Test
    @DisplayName("土耳其语环境下 UrlUtil.keyword 提取的域名关键字不被改变")
    void urlUtilKeywordIsLocaleIndependent() {
        // 修复前："INVOICE".toLowerCase(tr-TR) 会得到 "ınvoıce"
        assertEquals("invoice", underTurkishLocale(() -> UrlUtil.keyword("https://www.INVOICE.com/path")));
    }

    /**
     * 日志级别解析应固定使用 Locale.ROOT
     */
    @Test
    @DisplayName("土耳其语环境下 LogLevelUtil.setLevel 可正确解析小写级别")
    void logLevelUtilIsLocaleIndependent() {
        // 修复前："info".toUpperCase(tr-TR) 会得到 "İNFO"，Level.valueOf 解析失败
        assertTrue(underTurkishLocale(() -> LogLevelUtil.setLevel("round9.locale.test", "info")));
    }

    /**
     * OID 十六进制解析结果不应随默认 Locale 变化
     */
    @Test
    @DisplayName("土耳其语环境下 OID 十六进制解析结果不变")
    void oidParsingIsLocaleIndependent() {
        String baseline = OID.hexToDotNotation("2A03");
        assertEquals(baseline, underTurkishLocale(() -> OID.hexToDotNotation("2A03")));
    }


}
