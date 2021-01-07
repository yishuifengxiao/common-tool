/**
 * 
 */
package com.yishuifengxiao.common.tool.validate.domain;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * 用于标记符合校验
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@GroupSequence({ Default.class, Create.class, Update.class })
public interface Group {

}
