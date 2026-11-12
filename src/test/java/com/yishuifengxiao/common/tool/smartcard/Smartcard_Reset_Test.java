package com.yishuifengxiao.common.tool.smartcard;

import org.junit.Assume;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author yishui
 * @version v1.0.0
 * @Description:
 * @date 2026/9/11
 **/
public class Smartcard_Reset_Test {


    @Test
    public void test_reset() {
        SmartCard smartCard = new SmartCard();
        List<String> terminalNames = smartCard.getCardTerminalNames();
        // 该用例依赖真实读卡器与卡片，无硬件环境时跳过而不是失败
        Assume.assumeFalse("未检测到读卡器，跳过真实硬件测试", terminalNames.isEmpty());
        smartCard.connect(terminalNames.get(0));
        try {
            String eid = smartCard.getEid();
            smartCard.reset();
            String eid2 = smartCard.getEid();
            assertEquals(eid, eid2);
            SmartCard.ApduResult result = smartCard.transmitWithNewLogicalChannel("81E2910006BF3E035C015A");
            assertTrue(result.isSuccess());
        } finally {
            smartCard.disconnect();
        }
    }
}
