package com.yishuifengxiao.common.tool.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 执行工具
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ExecuteUtil {

    /**
     * <p>流程串行执行</p>
     * <p>当前一个流程的输出结果不满足要求时继续执行下一个流程，直到执行完成,若流程的输出结果满足要求则停止后续流程的执行，直接返回结果</p>
     *
     * @param match     匹配条件，若满足此条件则直接返回结果
     * @param suppliers 流程执行
     * @param <T>       输出数据类型
     * @return 输出数据
     */
    @SuppressWarnings("unchecked")
    public synchronized static <T> T execute(Predicate<T> match, Supplier<T>... suppliers) {
        if (null == suppliers) {
            return null;
        }
        return Arrays.stream(suppliers).filter(Objects::nonNull).map(v -> v.get()).filter(v -> match.test(v)).findFirst().orElse(null);
    }
}
