package com.skillfive.backend.exception;

/**
 * 未授权异常
 */
public class UnauthorizedException extends BaseException {
    
    public UnauthorizedException(String message) {
        super(401, message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(401, message, cause);
    }
    
    /**
     * 创建登录失败异常
     */
    public static UnauthorizedException loginFailed() {
        return new UnauthorizedException("用户名或密码错误");
    }
    
    /**
     * 创建令牌无效异常
     */
    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException("令牌无效或已过期");
    }
    
    /**
     * 创建会话过期异常
     */
    public static UnauthorizedException sessionExpired() {
        return new UnauthorizedException("会话已过期，请重新登录");
    }
    
    /**
     * 创建权限不足异常
     */
    public static UnauthorizedException insufficientPermission() {
        return new UnauthorizedException("权限不足，无法执行此操作");
    }
}