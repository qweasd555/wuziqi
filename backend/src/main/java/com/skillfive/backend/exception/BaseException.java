package com.skillfive.backend.exception;

/**
 * 基础异常类
 */
public class BaseException extends RuntimeException {
    
    private Integer code;
    private String message;
    
    public BaseException(String message) {
        super(message);
        this.code = 500; // 默认服务器错误
        this.message = message;
    }
    
    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BaseException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }
    
    public BaseException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public void setCode(Integer code) {
        this.code = code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}