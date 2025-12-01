package com.skillfive.backend.dto.request;

import lombok.Data;

/**
 * 移动请求DTO
 */
@Data
public class MoveRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 落子位置（0-8，表示3x3棋盘位置）
     */
    private Integer position;
    
    /**
     * 游戏ID
     */
    private Long gameId;
    
    /**
     * 移动类型（可选）
     * - normal: 普通移动
     * - skill: 技能移动
     */
    private String moveType;
}