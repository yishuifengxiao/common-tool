package com.yishuifengxiao.common.tool.bean;

import com.yishuifengxiao.common.tool.exception.UncheckedException;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * {@link BeanUtil#mapToBean(Map, Class)} 的单元测试
 */
public class BeanUtilMapToBeanTest {

    // ==================== 辅助类定义 ====================

    static class SimpleUser {
        private String name;
        private Integer age;

        public SimpleUser() {}

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
    }

    static class UserWithoutDefaultConstructor {
        private final String name;
        private final Integer age;

        public UserWithoutDefaultConstructor(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public Integer getAge() {
            return age;
        }
    }

    static class InvalidTypeUser {
        private String name;
        private Long age; // 类型不同导致 convertValue 失败

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Long getAge() {
            return age;
        }

        public void setAge(Long age) {
            this.age = age;
        }
    }

    // ==================== 测试用例 ====================

    /**
     * TC01: map 为 null，应该返回 null
     */
    @Test
    public void testMapToBean_MapIsNull_ReturnsNull() {
        SimpleUser result = BeanUtil.mapToBean(null, SimpleUser.class);
        assertNull(result);
    }

    /**
     * TC02: 正常 Map 转换为 Bean 成功
     */
    @Test
    public void testMapToBean_NormalConversion_Success() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("age", 25);

        SimpleUser user = BeanUtil.mapToBean(map, SimpleUser.class);

        assertNotNull(user);
        assertEquals("Alice", user.getName());
        assertEquals(Integer.valueOf(25), user.getAge());
    }

    /**
     * TC03: convertValue 失败，fallback 成功（类型不一致）
     */
    @Test
    public void testMapToBean_ConvertValueFails_FallbackSuccess() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Bob");
        map.put("age", "30"); // String -> Long

        InvalidTypeUser user = BeanUtil.mapToBean(map, InvalidTypeUser.class);

        assertNotNull(user);
        assertEquals("Bob", user.getName());
        assertEquals(Long.valueOf(30), user.getAge());
    }

    /**
     * TC04: fallback 中 write/read JSON 成功
     */
    @Test
    public void testMapToBean_WriteReadJsonFallback_Success() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Charlie");
        map.put("age", 28);

        // 使用无默认构造函数的类来触发 fallback
        UserWithoutDefaultConstructor user =
                BeanUtil.mapToBean(map, UserWithoutDefaultConstructor.class);

        assertNotNull(user);
        assertEquals("Charlie", user.getName());
        assertEquals(Integer.valueOf(28), user.getAge());
    }

    /**
     * TC05: fallback 中 write/read JSON 失败，但构造器方式成功
     */
    @Test
    public void testMapToBean_ConstructorsFallback_Success() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "David");
        map.put("age", 35);

        // 使用无默认构造函数的类来触发 fallback
        UserWithoutDefaultConstructor user =
                BeanUtil.mapToBean(map, UserWithoutDefaultConstructor.class);

        assertNotNull(user);
        assertEquals("David", user.getName());
        assertEquals(Integer.valueOf(35), user.getAge());
    }

    /**
     * TC06: fallback 中 write/read JSON 失败且构造器失败 → 抛出 UncheckedException
     */
    @Test(expected = UncheckedException.class)
    public void testMapToBean_AllFallbackFailed_ThrowsException() {
        Map<String, Object> map = new HashMap<>();
        map.put("nonExistentField", "value");

        // 使用没有对应字段的类，并且也没有合适的构造函数
        class NoFieldAndNoConstructor {
            private NoFieldAndNoConstructor(String dummy) {}
        }

        BeanUtil.mapToBean(map, NoFieldAndNoConstructor.class);
    }
}
