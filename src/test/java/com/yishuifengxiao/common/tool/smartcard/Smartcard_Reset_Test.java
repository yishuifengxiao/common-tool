package com.yishuifengxiao.common.tool.smartcard;

import org.junit.Test;

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
        smartCard.connect(smartCard.getCardTerminalNames().get(0));
        String eid = smartCard.getEid();
        System.out.println("eid: " + eid);
        smartCard.reset();
        String eid2 = smartCard.getEid();
        System.out.println("eid2: " + eid2);
        assertEquals(eid, eid2);
        SmartCard.ApduResult result = smartCard.transmitWithNewLogicalChannel("81E2910006BF3E035C015A");
        assertTrue(result.isSuccess());
    }
}
