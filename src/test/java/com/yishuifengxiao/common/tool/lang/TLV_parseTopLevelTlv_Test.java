package com.yishuifengxiao.common.tool.lang;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TLV_parseTopLevelTlv_Test {

    @Test
    public void test_ok() {
        String tlv = "810302030182030205008303190400840D81010082040003EB80830241F58503017F328603090200870302030088020480A92C041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F860AA2C041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F8609902064004030100000C1653415341636372656469746174696F6E4E756D6265729001FFB40BA005040302000C81008200";
        TLV.TlvResult result = TLV.parseTopLevelTlv(tlv);
        assertTrue(null != result);
        assertTrue(result.isSuccess());
        assertTrue(StringUtils.isBlank(result.getRemain()));
        assertTrue(null == result.getException());
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("81"), "020301"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("82"), "020500"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("83"), "190400"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("84"), "81010082040003EB80830241F5"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("A9"), "041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F860"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("90"), "FF"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("99"), "0640"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("88"), "0480"));
    }

    @Test
    public void test_ok_1() {
        String tlv = "810302030182030205008303190400840D81010082040003EB80830241F58503017F328603090200870302030088020480A92C041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F860AA2C041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F8609902064004030100000C1653415341636372656469746174696F6E4E756D6265729001FFB40BA005040302000C810082001111";
        TLV.TlvResult result = TLV.parseTopLevelTlv(tlv);
        assertTrue(null != result);
        assertTrue(result.isSuccess());
        assertTrue(StringUtils.equalsIgnoreCase(result.getRemain(), "1111"));
        assertTrue(null == result.getException());
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("81"), "020301"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("82"), "020500"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("83"), "190400"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("84"), "81010082040003EB80830241F5"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("A9"), "041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F860"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("90"), "FF"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("99"), "0640"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("88"), "0480"));
    }

    @Test
    public void test_ok_2() {
        String tlv = "810302030182030205008303190400840D81010082040003EB80830241F58503017F328603090200870302030088020480A92C041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F860AA2C041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F8609902064004030100000C1653415341636372656469746174696F6E4E756D6265729001FFB40BA005040302000C81008200123";
        TLV.TlvResult result = TLV.parseTopLevelTlv(tlv);
        assertTrue(null != result);
        assertTrue(result.isSuccess());
        assertTrue(StringUtils.equalsIgnoreCase(result.getRemain(), "123"));
        assertTrue(null == result.getException());
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("81"), "020301"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("82"), "020500"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("83"), "190400"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("84"), "81010082040003EB80830241F5"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("A9"), "041481370F5125D0B1D408D4C3B232E6D25E795BEBFB041461F709B006684D94C2BC594B7D02EAE8B599F860"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("90"), "FF"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("99"), "0640"));
        assertTrue(StringUtils.equalsIgnoreCase(result.getVal("88"), "0480"));
    }
}
