package com.yishuifengxiao.common.tool.collections;

import com.yishuifengxiao.common.tool.entity.BoolStat;
import com.yishuifengxiao.common.tool.lang.CompareUtil;
import com.yishuifengxiao.common.tool.lang.NumberUtil;
import org.junit.jupiter.api.Test;

/**
 * BoolUtil 测试类
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class BoolUtilTest {

    @Test
    public void boolTest() {

        final BoolStat bool = BoolStat.parse(0.02D);
        System.out.println(bool);
    }

    @Test
    public void test03() {
        System.out.println(CompareUtil.equals(0.1F, 0.1D));
        System.out.println(CompareUtil.equals(0.1F, 0.1F));
        System.out.println(CompareUtil.equals(NumberUtil.parse("0.1"), NumberUtil.parse("0.1")));
    }
}
