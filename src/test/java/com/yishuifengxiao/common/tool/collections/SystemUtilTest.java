package com.yishuifengxiao.common.tool.collections;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import com.yishuifengxiao.common.tool.utils.SystemUtil;

/**
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class SystemUtilTest {

    public static void main(String[] args) {
        final UncheckedException exception = UncheckedException.of("用户信息不能为空");
        final String msg = SystemUtil.extractError(exception);
        System.out.println(msg);
        System.out.println("---------------------------------");
        System.out.println(exception.getMessage());
        System.out.println("---------------------------------");
        exception.printStackTrace();
    }
}
