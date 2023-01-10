package com.yishuifengxiao.common.tool.collections;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 集合比较工具测试类
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ArrayUtilTest {

    private final static List<Integer> srcList = Arrays.asList(1, 2, 3, 4, 5);
    private final static List<Integer> targetList = Arrays.asList(4, 5, 6, 7, 8);

    @Test
    public void intersectionTest1() {
        final Collection<Integer> integers = ArrayUtil.intersection(srcList, targetList, (v1, v2) -> v1 == v2);
        integers.stream().forEach(System.out::print);
    }

    @Test
    public void unionTest1() {
        final Collection<Integer> integers = ArrayUtil.union(srcList, targetList, (v1, v2) -> v1 == v2);
        integers.stream().forEach(System.out::print);
    }

    @Test
    public void differenceTest1() {
        final Collection<Integer> integers = ArrayUtil.difference(srcList, targetList, (v1, v2) -> v1 == v2);
        integers.stream().forEach(System.out::print);
    }
}
