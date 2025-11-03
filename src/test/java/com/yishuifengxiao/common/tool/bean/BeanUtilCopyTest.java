package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * {@link BeanUtil#copy(Object, Object)} 的单元测试
 */
public class BeanUtilCopyTest {

    // ==================== 辅助类定义 ====================

    static class SourceObject {
        private String name;
        private int age;
        private boolean active;
        private Date birthDate;

        public SourceObject() {
        }

        public SourceObject(String name, int age, boolean active, Date birthDate) {
            this.name = name;
            this.age = age;
            this.active = active;
            this.birthDate = birthDate;
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
        }
    }

    static class TargetObject {
        private String name;
        private Integer age;
        private Boolean active;
        private Date birthDate;

        public TargetObject() {
        }

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }

        public Date getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDate = birthDate;
        }
    }

    static class NoMatchFieldObject {
        private String differentName;
        private Double salary;

        public String getDifferentName() {
            return differentName;
        }

        public void setDifferentName(String differentName) {
            this.differentName = differentName;
        }

        public Double getSalary() {
            return salary;
        }

        public void setSalary(Double salary) {
            this.salary = salary;
        }
    }

    static class PrivateFieldSource {
        private String secret = "secret";
    }

    static class PrivateFieldTarget {
        private String secret;
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

    // ==================== 测试用例 ====================

    /**
     * TC01: source为null的情况
     */
    @Test
    public void testCopy_SourceIsNull() {
        TargetObject target = new TargetObject();
        TargetObject result = BeanUtil.copy(null, target);
        assertSame(target, result);
    }

    /**
     * TC02: target为null的情况
     */
    @Test
    public void testCopy_TargetIsNull() {
        SourceObject source = new SourceObject();
        TargetObject result = BeanUtil.copy(source, null);
        assertNull(result);
    }

    /**
     * TC03: source和target都为null的情况
     */
    @Test
    public void testCopy_BothAreNull() {
        TargetObject result = BeanUtil.copy(null, null);
        assertNull(result);
    }

    /**
     * TC04: source和target没有公共字段的情况
     */
    @Test
    public void testCopy_NoCommonFields() {
        SourceObject source = new SourceObject("Alice", 25, true, new Date());
        NoMatchFieldObject target = new NoMatchFieldObject();
        target.setDifferentName("Bob");
        target.setSalary(5000.0);

        NoMatchFieldObject result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertEquals("Bob", result.getDifferentName());
        assertEquals(Double.valueOf(5000.0), result.getSalary());
    }

    /**
     * TC05: 私有字段拷贝成功
     */
    @Test
    public void testCopy_PrivateFieldsCopiedSuccessfully() throws Exception {
        PrivateFieldSource source = new PrivateFieldSource();
        PrivateFieldTarget target = new PrivateFieldTarget();

        PrivateFieldTarget result = BeanUtil.copy(source, target);

        assertNotNull(result);
        assertEquals("secret", result.secret);
    }

    /**
     * TC06: 基本类型与包装类型转换
     */
    @Test
    public void testCopy_PrimitiveToWrapperType() {
        SourceObject source = new SourceObject("Alice", 25, true, new Date());
        TargetObject target = new TargetObject();

        BeanUtil.copy(source, target);

        assertEquals("Alice", target.getName());
        assertEquals(Integer.valueOf(25), target.getAge());
        assertTrue(target.getActive());
        assertEquals(source.getBirthDate(), target.getBirthDate());
    }

    /**
     * TC07: 枚举类型转换
     */
    @Test
    public void testCopy_StringToEnum() {
        EnumSource source = new EnumSource();
        EnumTarget target = new EnumTarget();

        BeanUtil.copy(source, target);

        assertEquals(TestEnum.VALUE1, target.testEnum);
    }

    /**
     * TC08: 数组类型转换
     */
    @Test
    public void testCopy_ArrayType() {
        ArraySource source = new ArraySource();
        ArrayTarget target = new ArrayTarget();

        BeanUtil.copy(source, target);

        assertArrayEquals(source.names, target.names);
    }

    /**
     * TC09: 时间类型转换
     */
    @Test
    public void testCopy_DateTimeConversion() {
        DateTimeSource source = new DateTimeSource();
        DateTimeTarget target = new DateTimeTarget();

        BeanUtil.copy(source, target);

        assertEquals(LocalDate.of(2023, 1, 1), target.date);
    }


}
