package com.skillfive.backend.exception;

import com.skillfive.backend.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理自定义基础异常
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Object>> handleBaseException(BaseException ex, HttpServletRequest request) {
        logger.warn("BaseException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = new ApiResponse<Object>(ex.getCode(), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.valueOf(ex.getCode()));
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        logger.warn("BusinessException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = new ApiResponse<Object>(ex.getCode(), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理资源不存在异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        logger.warn("ResourceNotFoundException: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<Object>(404, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    /**
     * 处理未授权异常
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request) {
        logger.warn("UnauthorizedException: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<Object>(401, ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * 处理请求参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        logger.warn("ValidationException: {}", ex.getMessage());
        
        // 获取所有验证错误
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
                ));
        
        ApiResponse<Object> response = new ApiResponse<Object>(400, "参数验证失败", errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        logger.error("RuntimeException: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = new ApiResponse<Object>(500, "服务器内部错误: " + ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {
        logger.error("Exception: {}", ex.getMessage(), ex);
        
        ApiResponse<Object> response = new ApiResponse<Object>(500, "服务器内部错误", null);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 获取请求信息
     */
    private Map<String, Object> getRequestInfo(HttpServletRequest request) {
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("uri", request.getRequestURI());
        requestInfo.put("method", request.getMethod());
        requestInfo.put("clientIp", request.getRemoteAddr());
        requestInfo.put("userAgent", request.getHeader("User-Agent"));
        return requestInfo;
    }
}