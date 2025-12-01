package com.skillfive.backend.dto;

import lombok.Data;

/**
 * 技能使用请求DTO
 */
@Data
public class SkillUseRequest {
    
    /**
     * 技能ID
     */
    private Long skillId;
    
    /**
     * 目标位置（可选，某些技能需要）
     */
    private Integer targetPosition;
    
    /**
     * 额外参数（可选）
     */
    private String params;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 游戏ID
     */
    private Long gameId;
}