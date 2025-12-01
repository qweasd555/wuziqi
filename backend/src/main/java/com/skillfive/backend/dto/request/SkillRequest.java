package com.skillfive.backend.dto.request;

import lombok.Data;

/**
 * 技能请求DTO
 */
@Data
public class SkillRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 游戏ID
     */
    private Long gameId;
    
    /**
     * 技能ID
     */
    private Long skillId;
    
    /**
     * 技能目标位置（可选，某些技能可能需要指定目标）
     */
    private Integer targetPosition;
    
    /**
     * 技能参数（可选，用于传递额外的技能参数）
     */
    private String skillParams;
}