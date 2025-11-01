package com.yishuifengxiao.common.tool.codec;

import org.junit.Assert;
import org.junit.Test;

public class ECPrivateKeyUtilTest {

    @Test
    public void test_extractPrivateDValue_01() throws Exception {
        String val = "-----BEGIN EC PARAMETERS-----\n" +
                "BggqhkjOPQMBBw==\n" +
                "-----END EC PARAMETERS-----\n" +
                "-----BEGIN EC PRIVATE KEY-----\n" +
                "MHcCAQEEICY2nUQCPBvVEoSmkYWMIOVtKCFdglIm6MKyAZgfaQx9oAoGCCqGSM49\n" +
                "AwEHoUQDQgAEweVfV6RO21zC2AemcJNw49YCu7JkltjMJB5XplZh/oVSyQDatSNg\n" +
                "q66RVQtY/YcaIc4cQ1F96scD+bgMb9OqbQ==\n" +
                "-----END EC PRIVATE KEY-----";
        String dValue = ECC.extractPrivateDValue(val);
        Assert.assertTrue("26369D44023C1BD51284A691858C20E56D28215D825226E8C2B201981F690C7D".equalsIgnoreCase(dValue));
    }

    @Test
    public void test_extractPrivateDValue_02() throws Exception {
        String val = "-----BEGIN EC PRIVATE KEY-----\n" +
                "MHcCAQEEICY2nUQCPBvVEoSmkYWMIOVtKCFdglIm6MKyAZgfaQx9oAoGCCqGSM49\n" +
                "AwEHoUQDQgAEweVfV6RO21zC2AemcJNw49YCu7JkltjMJB5XplZh/oVSyQDatSNg\n" +
                "q66RVQtY/YcaIc4cQ1F96scD+bgMb9OqbQ==\n" +
                "-----END EC PRIVATE KEY-----";
        String dValue = ECC.extractPrivateDValue(val);
        Assert.assertTrue("26369D44023C1BD51284A691858C20E56D28215D825226E8C2B201981F690C7D".equalsIgnoreCase(dValue));
    }

    @Test
    public void test_extractPrivateDValue_03() throws Exception {
        String val = "MHcCAQEEICY2nUQCPBvVEoSmkYWMIOVtKCFdglIm6MKyAZgfaQx9oAoGCCqGSM49\n" +
                "AwEHoUQDQgAEweVfV6RO21zC2AemcJNw49YCu7JkltjMJB5XplZh/oVSyQDatSNg\n" +
                "q66RVQtY/YcaIc4cQ1F96scD+bgMb9OqbQ==";
        String dValue = ECC.extractPrivateDValue(val);
        Assert.assertTrue("26369D44023C1BD51284A691858C20E56D28215D825226E8C2B201981F690C7D".equalsIgnoreCase(dValue));
    }

    @Test
    public void test_extractPrivateDValue_04() throws Exception {
        String val = "MHcCAQEEICY2nUQCPBvVEoSmkYWMIOVtKCFdglIm6MKyAZgfaQx9oAoGCCqGSM49\n" +
                "AwEHoUQDQgAEweVfV6RO21zC2AemcJNw49YCu7JkltjMJB5XplZh/oVSyQDatSNg\n" +
                "q66RVQtY/YcaIc4cQ1F96scD+bgMb9OqbQ==";
        String dValue = ECC.extractPrivateDValue(val);
        Assert.assertTrue("26369D44023C1BD51284A691858C20E56D28215D825226E8C2B201981F690C7D".equalsIgnoreCase(dValue));
    }
}