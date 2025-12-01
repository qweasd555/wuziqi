package com.skillfive.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.Map;

/**
 * JSON工具类
 */
public class JsonUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        // 设置时间格式为ISO格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }
    
    /**
     * 将对象转换为JSON字符串
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON序列化失败", e);
        }
    }
    
    /**
     * 将JSON字符串转换为对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            // 记录详细的错误信息
            System.err.println("JSON反序列化失败: " + e.getMessage());
            System.err.println("失败的JSON内容: " + json);
            System.err.println("目标类型: " + clazz.getName());
            // 抛出更详细的异常信息
            throw new RuntimeException("JSON反序列化失败: " + e.getMessage() + ", JSON: " + json, e);
        }
    }
    
    /**
     * 将JSON字符串转换为Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (IOException e) {
            throw new RuntimeException("JSON转Map失败", e);
        }
    }
    
    /**
     * 将Map转换为对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }
    
    /**
     * 将对象转换为Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> objectToMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }
    
    /**
     * 格式化JSON字符串
     */
    public static String formatJson(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("JSON格式化失败", e);
        }
    }
    
    /**
     * 验证JSON字符串是否有效
     */
    public static boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}