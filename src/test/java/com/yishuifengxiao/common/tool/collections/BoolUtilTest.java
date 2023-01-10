package com.yishuifengxiao.common.tool.collections;

import com.yishuifengxiao.common.tool.entity.BoolStat;
import com.yishuifengxiao.common.tool.lang.BoolUtil;
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

        final BoolStat bool = BoolUtil.bool(0.02D);
        System.out.println(bool);
    }
}
