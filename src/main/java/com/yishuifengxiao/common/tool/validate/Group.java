package com.yishuifengxiao.common.tool.validate;


import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

/**
 * Default Jakarta Bean Validation group.
 * Unless a list of groups is explicitly defined:
 * constraints belong to the Default group
 * validation applies to the Default group
 * Most structural constraints should belong to the default group.
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class Group {
    /**
     * 用于标记创建时的校验
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    public interface Create {

    }

    /**
     * 用于标记删除时的校验
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    public interface Delete {

    }

    /**
     * 用于标记查询时的校验
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    public interface Query {

    }

    /**
     * 用于标记符合校验
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    @GroupSequence({Default.class, Create.class, Update.class})
    public interface All {

    }

    /**
     * 用于标记更新时的校验
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    public interface Update {

    }
}
