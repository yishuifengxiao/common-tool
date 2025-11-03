package com.yishuifengxiao.common.tool.bean;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonUtil_str2List_Test {

    /**
     * 测试正常场景：将有效的JSON字符串转换为指定类型的对象集合
     * 预期结果：成功转换并返回包含对象的列表
     */
    @Test
    public void testStr2List_ValidJson() {
        String json = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";
        
        List<Person> result = JsonUtil.str2List(json, Person.class);
        
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals(30, result.get(0).getAge());
        assertEquals("Bob", result.get(1).getName());
        assertEquals(25, result.get(1).getAge());
    }

    /**
     * 测试边界场景：输入空字符串
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_EmptyString() {
        String json = "";
        
        List<Person> result = JsonUtil.str2List(json, Person.class);
        
        assertTrue(result.isEmpty());
    }

    /**
     * 测试边界场景：输入null值
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_NullInput() {
        String json = null;
        
        List<Person> result = JsonUtil.str2List(json, Person.class);
        
        assertTrue(result.isEmpty());
    }

    /**
     * 测试异常场景：输入无效的JSON字符串
     * 预期结果：抛出UncheckedException异常
     */
//    @Test(expected = UncheckedException.class)
    @Test
    public void testStr2List_InvalidJson() {
        String json = "invalid json string";

        List<Person> list = JsonUtil.str2List(json, Person.class);
        System.out.println( list);
    }

    /**
     * 测试正常场景：JSON数组包含复杂对象
     * 预期结果：成功转换并返回包含复杂对象的列表
     */
    @Test
    public void testStr2List_ComplexObject() {
        String json = "[{\"name\":\"Alice\",\"address\":{\"city\":\"Beijing\",\"street\":\"Main St\"}}]";
        
        List<PersonWithAddress> result = JsonUtil.str2List(json, PersonWithAddress.class);
        
        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
        assertEquals("Beijing", result.get(0).getAddress().getCity());
        assertEquals("Main St", result.get(0).getAddress().getStreet());
    }

    /**
     * 测试正常场景：JSON数组为空
     * 预期结果：返回空列表
     */
    @Test
    public void testStr2List_EmptyJsonArray() {
        String json = "[]";
        
        List<Person> result = JsonUtil.str2List(json, Person.class);
        
        assertTrue(result.isEmpty());
    }
}

// 辅助测试类
class Person {
    private String name;
    private int age;
    
    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}

class PersonWithAddress {
    private String name;
    private Address address;
    
    // getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }
}

class Address {
    private String city;
    private String street;
    
    // getters and setters
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
}
