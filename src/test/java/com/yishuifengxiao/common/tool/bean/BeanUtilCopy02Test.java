package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * {@link BeanUtil#copy(Object, Object)} 的单元测试（续）
 */
public class BeanUtilCopy02Test {

    // ==================== 辅助类定义 ====================

    static class PrimitiveSource {
        private int age = 25;
        private boolean active = true;
    }

    static class WrapperTarget {
        private Integer age;
        private Boolean active;
    }

    enum TestEnum {
        VALUE1, VALUE2
    }

    static class EnumSource {
        private String testEnum = "VALUE1";
    }

    static class EnumTarget {
        private TestEnum testEnum;
    }

    static class ArraySource {
        private String[] names = {"Alice", "Bob"};
    }

    static class ArrayTarget {
        private String[] names;
    }

    static class DateTimeSource {
        private String date = "2023-01-01";
    }

    static class DateTimeTarget {
        private LocalDate date;
    }

    static class ComplexInner {
        private String innerName = "inner";

        public String getInnerName() {
            return innerName;
        }

        public void setInnerName(String innerName) {
            this.innerName = innerName;
        }
    }

    static class ComplexSource {
        private ComplexInner inner = new ComplexInner();
    }

    static class ComplexTarget {
        private ComplexInner inner;
    }

    static class IncompatibleSource {
        private String name = "Alice";
    }

    static class IncompatibleTarget {
        private int name; // 类型不兼容
    }

    static class AccessRestoreSource {
        private String name = "Alice";
    }

    static class AccessRestoreTarget {
        private String name;
    }

    // ==================== 测试用例 ====================

    /**
     * TC06: 基本类型与包装类型之间的转换
     */
    @Test
    public void testCopy_PrimitiveToWrapperConversion() {
        PrimitiveSource source = new PrimitiveSource();
        WrapperTarget target = new WrapperTarget();

        WrapperTarget result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertEquals(Integer.valueOf(25), result.age);
        assertEquals(Boolean.TRUE, result.active);
    }

    /**
     * TC07: 枚举类型转换
     */
    @Test
    public void testCopy_EnumConversion() {
        EnumSource source = new EnumSource();
        EnumTarget target = new EnumTarget();

        EnumTarget result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertEquals(TestEnum.VALUE1, result.testEnum);
    }

    /**
     * TC08: 数组类型转换
     */
    @Test
    public void testCopy_ArrayConversion() {
        ArraySource source = new ArraySource();
        ArrayTarget target = new ArrayTarget();

        ArrayTarget result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertArrayEquals(new String[]{"Alice", "Bob"}, result.names);
    }

    /**
     * TC09: 日期时间类型转换
     */
    @Test
    public void testCopy_DateTimeConversion() {
        DateTimeSource source = new DateTimeSource();
        DateTimeTarget target = new DateTimeTarget();

        DateTimeTarget result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertEquals(LocalDate.of(2023, 1, 1), result.date);
    }

    /**
     * TC10: 复杂对象递归拷贝
     */
    @Test
    public void testCopy_ComplexObjectRecursiveCopy() {
        ComplexSource source = new ComplexSource();
        ComplexTarget target = new ComplexTarget();

        ComplexTarget result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertNotNull(result.inner);
        assertEquals("inner", result.inner.getInnerName());
    }

    /**
     * TC11: 类型不兼容跳过拷贝
     */
    @Test
    public void testCopy_TypeIncompatibleSkipCopy() {
        IncompatibleSource source = new IncompatibleSource();
        IncompatibleTarget target = new IncompatibleTarget();

        IncompatibleTarget result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertEquals(0, result.name); // 默认值
    }

    /**
     * TC12: 字段访问权限还原验证
     */
    @Test
    public void testCopy_FieldAccessibilityRestored() throws Exception {
        AccessRestoreSource source = new AccessRestoreSource();
        AccessRestoreTarget target = new AccessRestoreTarget();

        Field targetField = target.getClass().getDeclaredField("name");
        boolean originalAccessible = targetField.isAccessible();

        BeanUtil.copy(source, target);

        // 验证字段访问权限是否还原
        assertEquals(originalAccessible, targetField.isAccessible());
        assertEquals("Alice", target.name);
    }
}
