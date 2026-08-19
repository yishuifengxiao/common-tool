package com.yishuifengxiao.common.tool.bean;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证移除 @class 属性后的序列化结果是否符合预期
 *
 * <p>背景：JsonUtil 曾通过 {@code activateDefaultTyping(NON_FINAL, PROPERTY)} 在
 * 序列化非 final 类型时注入 {@code @class} 属性以保留多态类型信息。
 * 该配置已移除，本测试验证：</p>
 * <ul>
 *   <li>简单 POJO 序列化不含 {@code @class}</li>
 *   <li>多态子类实例序列化不含 {@code @class}</li>
 *   <li>基类引用指向子类实例时不含 {@code @class}</li>
 *   <li>混合子类集合序列化不含 {@code @class}</li>
 *   <li>{@code deepClone} 仍能正确往返，且克隆结果不含 {@code @class}</li>
 *   <li>{@code prettyPrinter} 输出不含 {@code @class}</li>
 *   <li>无 {@code @class} 的 JSON 可被正确反序列化回同类型对象</li>
 * </ul>
 *
 * @author qingteng
 * @version 1.0.0
 */
@Slf4j
@DisplayName("JsonUtil移除@class属性验证测试")
public class JsonUtil_NoClassAttribute_Test {

    // ==================== 测试数据类 ====================

    /** 基础类型（非 final，旧配置下会被注入 @class） */
    @Data
    public static class Animal {
        private String name;

        public Animal() {
        }

        public Animal(String name) {
            this.name = name;
        }
    }

    /** 子类：Dog，新增 breed 字段 */
    @Data
    public static class Dog extends Animal {
        private String breed;

        public Dog() {
        }

        public Dog(String name, String breed) {
            super(name);
            this.breed = breed;
        }
    }

    /** 子类：Cat，新增 color 字段 */
    @Data
    public static class Cat extends Animal {
        private String color;

        public Cat() {
        }

        public Cat(String name, String color) {
            super(name);
            this.color = color;
        }
    }

    /** 简单 POJO（非 final） */
    @Data
    public static class Person {
        private String name;
        private Integer age;

        public Person() {
        }

        public Person(String name, Integer age) {
            this.name = name;
            this.age = age;
        }
    }

    // ==================== 测试用例 ====================

    /**
     * 简单 POJO 序列化不应包含 @class
     */
    @Test
    @DisplayName("简单POJO序列化不包含@class属性")
    public void testSimplePojo_NoClassAttribute() {
        Person person = new Person("张三", 30);
        String json = JsonUtil.toJSONString(person);

        log.info("简单POJO序列化结果: {}", json);

        assertNotNull(json, "序列化结果不应为null");
        assertFalse(json.contains("@class"),
                "序列化结果不应包含@class属性，实际: " + json);
        assertTrue(json.contains("\"name\""), "应包含name字段");
        assertTrue(json.contains("张三"), "name值应正确");
        assertTrue(json.contains("\"age\""), "应包含age字段");
    }

    /**
     * 多态子类实例序列化不应包含 @class
     *
     * <p>这是移除 activateDefaultTyping 的核心验证点：
     * 旧配置下 Dog 实例会被序列化为
     * {@code {"@class":"...Dog","name":"...","breed":"..."}}，
     * 移除后应只有 {@code {"name":"...","breed":"..."}}</p>
     */
    @Test
    @DisplayName("多态子类序列化不包含@class属性")
    public void testPolymorphicSubclass_NoClassAttribute() {
        Dog dog = new Dog("旺财", "金毛");
        String json = JsonUtil.toJSONString(dog);

        log.info("Dog子类序列化结果: {}", json);

        assertNotNull(json, "序列化结果不应为null");
        assertFalse(json.contains("@class"),
                "子类序列化结果不应包含@class属性，实际: " + json);
        assertTrue(json.contains("\"breed\""), "应包含子类字段breed");
        assertTrue(json.contains("金毛"), "breed值应正确");
        assertTrue(json.contains("\"name\""), "应包含父类字段name");
    }

    /**
     * 将子类当作基类引用序列化也不应包含 @class
     *
     * <p>旧配置下，声明类型为 Animal 但实际为 Cat 时会注入
     * {@code @class} 以便反序列化时还原为 Cat；
     * 移除后不再保留类型信息。</p>
     */
    @Test
    @DisplayName("基类引用指向子类实例时不包含@class属性")
    public void testBaseReferenceToSubclass_NoClassAttribute() {
        Animal animal = new Cat("咪咪", "白色");
        String json = JsonUtil.toJSONString(animal);

        log.info("基类引用(指向Cat)序列化结果: {}", json);

        assertNotNull(json, "序列化结果不应为null");
        assertFalse(json.contains("@class"),
                "基类引用序列化不应包含@class属性，实际: " + json);
        assertTrue(json.contains("\"color\""), "应包含子类字段color");
        assertTrue(json.contains("白色"), "color值应正确");
    }

    /**
     * 包含混合子类实例的集合序列化不应包含 @class
     *
     * <p>旧配置下 List{@literal <Animal>} 中混合 Dog 与 Cat 时，
     * 每个元素都会带 {@code @class} 以区分类型；
     * 移除后集合元素不再携带类型标识。</p>
     */
    @Test
    @DisplayName("混合子类集合序列化不包含@class属性")
    public void testMixedSubclassCollection_NoClassAttribute() {
        List<Animal> animals = new ArrayList<>();
        animals.add(new Dog("旺财", "金毛"));
        animals.add(new Cat("咪咪", "白色"));

        String json = JsonUtil.toJSONString(animals);

        log.info("混合子类集合序列化结果: {}", json);

        assertNotNull(json, "序列化结果不应为null");
        assertFalse(json.contains("@class"),
                "集合序列化不应包含@class属性，实际: " + json);
        assertTrue(json.contains("金毛"), "应包含Dog的breed值");
        assertTrue(json.contains("白色"), "应包含Cat的color值");
    }

    /**
     * deepClone 后的对象序列化也不应包含 @class
     *
     * <p>deepClone 内部使用 JSON 往返实现，移除 @class 后
     * 仍能通过 {@code val.getClass()} 正确还原同类型实例。</p>
     */
    @Test
    @DisplayName("deepClone结果不包含@class属性且字段值一致")
    public void testDeepClone_NoClassAttribute() {
        Person original = new Person("李四", 25);
        Object cloned = JsonUtil.deepClone(original);

        assertNotNull(cloned, "克隆结果不应为null");
        assertTrue(cloned instanceof Person, "克隆对象应为Person类型");

        Person clonedPerson = (Person) cloned;
        assertEquals("李四", clonedPerson.getName(), "name应一致");
        assertEquals(25, clonedPerson.getAge(), "age应一致");

        // 验证克隆对象的序列化也不包含 @class
        String json = JsonUtil.toJSONString(cloned);
        log.info("deepClone结果序列化: {}", json);
        assertFalse(json.contains("@class"),
                "deepClone结果序列化不应包含@class属性，实际: " + json);
    }

    /**
     * deepClone 对子类实例同样有效
     */
    @Test
    @DisplayName("deepClone对子类实例同样有效且不含@class")
    public void testDeepClone_Subclass() {
        Dog original = new Dog("大黄", "中华田园犬");
        Object cloned = JsonUtil.deepClone(original);

        assertNotNull(cloned, "克隆结果不应为null");
        assertTrue(cloned instanceof Dog, "克隆对象应为Dog类型");

        Dog clonedDog = (Dog) cloned;
        assertEquals("大黄", clonedDog.getName(), "name应一致");
        assertEquals("中华田园犬", clonedDog.getBreed(), "breed应一致");

        String json = JsonUtil.toJSONString(cloned);
        log.info("子类deepClone结果序列化: {}", json);
        assertFalse(json.contains("@class"),
                "子类deepClone结果序列化不应包含@class属性，实际: " + json);
    }

    /**
     * prettyPrinter 输出也不应包含 @class
     */
    @Test
    @DisplayName("prettyPrinter输出不包含@class属性")
    public void testPrettyPrinter_NoClassAttribute() {
        Dog dog = new Dog("大黄", "中华田园犬");
        String json = JsonUtil.prettyPrinter(dog);

        log.info("prettyPrinter输出:\n{}", json);

        assertNotNull(json, "prettyPrinter结果不应为null");
        assertFalse(json.contains("@class"),
                "prettyPrinter输出不应包含@class属性，实际: " + json);
        assertTrue(json.contains("中华田园犬"), "应包含breed值");
    }

    /**
     * 验证无 @class 的序列化结果可被正确反序列化回同类型对象
     */
    @Test
    @DisplayName("无@class的序列化结果可正确反序列化回同类型对象")
    public void testRoundTripSerialization() {
        Person original = new Person("王五", 40);
        String json = JsonUtil.toJSONString(original);

        Person deserialized = JsonUtil.strToBean(json, Person.class);

        assertNotNull(deserialized, "反序列化结果不应为null");
        assertEquals("王五", deserialized.getName(), "name应一致");
        assertEquals(40, deserialized.getAge(), "age应一致");
    }

    /**
     * 验证 @class 属性确实不在 JSON 的任何位置出现
     *
     * <p>用各种对象组合测试，确保 @class 字面量在任何
     * 序列化路径下都不会出现。</p>
     */
    @Test
    @DisplayName("@class字面量在任何序列化路径下都不出现")
    public void testNoClassAnywhere() {
        // 简单对象
        assertFalse(JsonUtil.toJSONString(new Person("A", 1)).contains("@class"));

        // 子类对象
        assertFalse(JsonUtil.toJSONString(new Dog("B", "C")).contains("@class"));
        assertFalse(JsonUtil.toJSONString(new Cat("D", "E")).contains("@class"));

        // 基类引用指向子类
        Animal a1 = new Dog("F", "G");
        Animal a2 = new Cat("H", "I");
        assertFalse(JsonUtil.toJSONString(a1).contains("@class"));
        assertFalse(JsonUtil.toJSONString(a2).contains("@class"));

        // 集合
        List<Animal> list = new ArrayList<>();
        list.add(new Dog("J", "K"));
        list.add(new Cat("L", "M"));
        assertFalse(JsonUtil.toJSONString(list).contains("@class"));

        // prettyPrinter
        assertFalse(JsonUtil.prettyPrinter(new Dog("N", "O")).contains("@class"));

        // 包含 null 值的对象
        assertFalse(JsonUtil.toJSONString(true, new Person("P", null)).contains("@class"));

        log.info("所有序列化路径均不含@class属性");
    }
}
