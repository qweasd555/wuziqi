package com.skillfive.backend.dto.request;

import com.skillfive.backend.enums.GameMode;
import lombok.Data;

/**
 * 游戏请求DTO
 */
@Data
public class GameRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 游戏模式
     */
    private GameMode mode;
    
    /**
     * 是否允许观看（可选）
     */
    private Boolean allowSpectators;
    
    /**
     * 游戏标题（可选）
     */
    private String title;
}