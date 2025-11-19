package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * {@link BeanUtil#beanToMap(Object)} 的单元测试
 */
public class BeanUtilBeanToMapTest {

    // ==================== 辅助类定义 ====================

    static class TestBean {
        private String name = "test";
        private int age = 25;
        private boolean active = true;
        private Date birthDate = new Date();

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

    static class PrivateFieldBean {
        private String secret = "hidden";

        // Getter only for testing access via reflection or JSON conversion
        public String getSecret() {
            return secret;
        }
    }

    // ==================== 测试用例 ====================

    /**
     * TC01: data 为 null 时应返回 EMPTY_MAP
     */
    @Test
    public void testBeanToMap_DataIsNull_ReturnsEmptyMap() {
        Map<?, ?> result = BeanUtil.beanToMap(null);
        assertSame(Collections.EMPTY_MAP, result);
    }

    /**
     * TC02: 正常对象转换为 Map
     */
    @Test
    public void testBeanToMap_NormalObject_ConvertedSuccessfully() {
        TestBean bean = new TestBean();
        Map<String, Object> result = BeanUtil.beanToMap(bean);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals("test", result.get("name"));
        assertEquals(25, result.get("age"));
        assertEquals(true, result.get("active"));
        assertNotNull(result.get("birthDate"));
    }

    /**
     * TC03: 包含私有字段的对象也应当能被正确转换
     */
    @Test
    public void testBeanToMap_PrivateFields_AreIncludedInResult() {
        PrivateFieldBean bean = new PrivateFieldBean();
        Map<String, Object> result = BeanUtil.beanToMap(bean);

        assertNotNull(result);
        assertEquals("hidden", result.get("secret"));
    }


}
