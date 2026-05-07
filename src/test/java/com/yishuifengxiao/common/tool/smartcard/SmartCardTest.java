package com.yishuifengxiao.common.tool.smartcard;

import com.yishuifengxiao.common.tool.collections.CollUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.smartcardio.CardException;
import java.util.List;

/**
 * @author yishui
 * @version v1.0.0
 * @Description:
 * @date 2026/5/7
 **/
public class SmartCardTest {

    @Test
    public void test_1() throws CardException {
        SmartCard smartCard = new SmartCard();
        List<String> cardTerminalNames = smartCard.getCardTerminalNames();
        Assert.assertTrue(CollUtil.isNotEmpty(cardTerminalNames));
        System.out.println(cardTerminalNames);
        String name = cardTerminalNames.get(0);
        smartCard.connect(name);
        String eid = smartCard.getEid();
        System.out.println(eid);
    }
}
