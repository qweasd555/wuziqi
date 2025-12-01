package com.skillfive.backend.exception;

/**
 * 资源不存在异常
 */
public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String message) {
        super(404, message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(404, message, cause);
    }
    
    /**
     * 创建资源不存在异常
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(404, String.format("资源 %s (ID: %s) 不存在", resourceType, resourceId));
    }
    
    /**
     * 创建资源不存在异常
     * @param resourceType 资源类型
     * @param fieldName 字段名
     * @param fieldValue 字段值
     */
    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super(404, String.format("资源 %s (条件: %s=%s) 不存在", resourceType, fieldName, fieldValue));
    }
}