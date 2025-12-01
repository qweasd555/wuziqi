package com.skillfive.backend.dto.response;

import lombok.Data;

/**
 * 用户响应DTO
 */
@Data
public class UserResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 用户积分
     */
    private Integer score;
    
    /**
     * 胜场数
     */
    private Integer wins;
    
    /**
     * 总对局数
     */
    private Integer totalGames;
    
    /**
     * 段位等级
     */
    private String rankLevel;
    
    /**
     * 胜率
     */
    private Double winRate;
    
    /**
     * 是否在线
     */
    private Boolean isOnline;
    
    /**
     * 上次登录时间
     */
    private String lastLoginTime;
}