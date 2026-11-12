package com.yishuifengxiao.common.tool;

import com.yishuifengxiao.common.tool.lang.NumberUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round17 收敛扫描修复回归测试
 * <p>
 * 覆盖 NumberUtil 比较方法对 Double.NaN / Infinity 的安全处理，不再抛未捕获异常。
 * </p>
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@DisplayName("Round17 收敛扫描修复回归测试")
class Round17ConvergentScanRegressionTest {

    @Test
    @DisplayName("Double.NaN 与任何值比较均返回 false（无数学意义）")
    void nanComparedToAnyAlwaysReturnsFalse() {
        // NaN 无大小关系：不大于、不小于、不等于任何值
        assertFalse(NumberUtil.gt(Double.NaN, 0));
        assertFalse(NumberUtil.gte(Double.NaN, 0));
        assertFalse(NumberUtil.lt(Double.NaN, 0));
        assertFalse(NumberUtil.lte(Double.NaN, 0));
        assertFalse(NumberUtil.equals(Double.NaN, 0));
        assertFalse(NumberUtil.equals(Double.NaN, Double.NaN));
        // Float NaN 同样处理
        assertFalse(NumberUtil.gt(Float.NaN, 0f));
        assertFalse(NumberUtil.equals(Float.NaN, Float.NaN));
    }

    @Test
    @DisplayName("Double.POSITIVE_INFINITY 大于任意有限值")
    void positiveInfinityIsGreaterThanFinite() {
        assertTrue(NumberUtil.gt(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
        assertTrue(NumberUtil.gte(Double.POSITIVE_INFINITY, Double.MAX_VALUE));
        assertTrue(NumberUtil.gt(Double.POSITIVE_INFINITY, 0));
        assertTrue(NumberUtil.gt(Float.POSITIVE_INFINITY, 0));
    }

    @Test
    @DisplayName("Double.NEGATIVE_INFINITY 小于任意有限值")
    void negativeInfinityIsLessThanFinite() {
        assertTrue(NumberUtil.lt(Double.NEGATIVE_INFINITY, Double.MIN_VALUE));
        assertTrue(NumberUtil.lte(Double.NEGATIVE_INFINITY, Double.MIN_VALUE));
        assertTrue(NumberUtil.lt(Double.NEGATIVE_INFINITY, 0));
    }

    @Test
    @DisplayName("有限值比较仍返回 BigDecimal 精确结果")
    void finiteValuesComparePrecisely() {
        assertTrue(NumberUtil.gt(3, 2));
        assertTrue(NumberUtil.gte(3, 3));
        assertTrue(NumberUtil.lt(2, 3));
        assertTrue(NumberUtil.lte(2, 2));
        assertTrue(NumberUtil.equals(3, 3));
    }
}
