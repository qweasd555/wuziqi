package com.skillfive.backend.dto.response;

import lombok.Data;

/**
 * 通用API响应DTO
 */
@Data
public class ApiResponse<T> {
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 成功响应构造器
     */
    public ApiResponse(T data) {
        this.code = 200;
        this.message = "success";
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 自定义响应构造器
     */
    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功响应静态方法
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }
    
    /**
     * 成功响应静态方法（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(null);
    }
    
    /**
     * 错误响应静态方法
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 参数错误响应静态方法
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }
    
    /**
     * 未授权响应静态方法
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, null);
    }
    
    /**
     * 服务器错误响应静态方法
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500, message, null);
    }
}