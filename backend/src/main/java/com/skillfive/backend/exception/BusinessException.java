package com.skillfive.backend.exception;

/**
 * 业务异常
 */
public class BusinessException extends BaseException {
    
    public BusinessException(String message) {
        super(400, message);
    }
    
    public BusinessException(Integer code, String message) {
        super(code, message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(400, message, cause);
    }
    
    public BusinessException(Integer code, String message, Throwable cause) {
        super(code, message, cause);
    }
}