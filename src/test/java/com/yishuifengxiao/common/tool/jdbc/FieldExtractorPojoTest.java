package com.yishuifengxiao.common.tool.jdbc;


import com.yishuifengxiao.common.tool.bean.JsonUtil;
import org.junit.Test;

import java.util.List;

/**
 * @author yishui
 * @version v1.0.0
 * @Description:
 * @date 2026/5/9
 **/
public class FieldExtractorPojoTest {
    @Test
    public void test_extractFieldValue() {
        MockerData mockerData = new MockerData();
        List<FieldValue> fieldValues = FieldExtractor.extractFieldValue(mockerData);
        System.out.println(fieldValues);
    }

    @Test
    public void test_extractField() {
        MockerData mockerData = new MockerData();
        List<FieldValue> fieldValues = FieldExtractor.extractField(mockerData.getClass());
        System.out.println(fieldValues);
    }

    @Test
    public void test_extractTableName() {
        MockerData mockerData = new MockerData();
        String tableName = FieldExtractor.extractTableName(mockerData.getClass());
        assert tableName.equals("mocker_data");
    }

    @Test
    public void test_extractPrimaryField() {
        MockerData mockerData = new MockerData();
        FieldValue fieldValue = FieldExtractor.extractPrimaryField(mockerData.getClass());
        assert null != fieldValue;
        System.out.println(fieldValue);
    }


}

