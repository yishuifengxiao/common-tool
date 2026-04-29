package com.yishuifengxiao.common.tool.validate;


import jakarta.validation.GroupSequence;
import jakarta.validation.groups.Default;

/**
 * <p>校验分组定义类</p>
 * <p>定义Jakarta Bean Validation的校验分组接口，用于在不同业务场景下执行不同的校验规则。</p>
 * <p>分组类型：</p>
 * <ul>
 * <li>Create：创建操作校验</li>
 * <li>Update：更新操作校验</li>
 * <li>Delete：删除操作校验</li>
 * <li>Query：查询操作校验</li>
 * <li>All：包含所有分组的序列校验</li>
 * </ul>
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
