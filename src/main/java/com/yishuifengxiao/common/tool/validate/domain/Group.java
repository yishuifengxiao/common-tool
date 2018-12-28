/**
 * 
 */
package com.yishuifengxiao.common.tool.validate.domain;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * 用于标记符合校验
 * @author yishui
 * @date 2018年9月5日
 * @Version 0.0.1
 */
@GroupSequence({ Default.class, Create.class, Update.class })
public interface Group {

}
