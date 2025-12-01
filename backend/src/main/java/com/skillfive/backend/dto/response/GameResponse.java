package com.skillfive.backend.dto.response;

import com.skillfive.backend.enums.GameMode;
import com.skillfive.backend.enums.GameType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏响应DTO
 */
@Data
public class GameResponse {
    
    /**
     * 游戏ID
     */
    private Long id;
    
    /**
     * 游戏模式
     */
    private GameMode mode;
    
    /**
     * 游戏类型
     */
    private GameType type;
    
    /**
     * 玩家1信息
     */
    private UserResponse player1;
    
    /**
     * 玩家2信息
     */
    private UserResponse player2;
    
    /**
     * 当前游戏状态
     */
    private String status;
    
    /**
     * 当前回合（1或2）
     */
    private Integer currentTurn;
    
    /**
     * 棋盘状态（用字符串表示，如"XOXOXOXOX"）
     */
    private String boardStatus;
    
    /**
     * 获胜者ID
     */
    private Long winnerId;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 剩余时间（秒）
     */
    private Integer remainingTime;
    
    /**
     * 玩家1的技能列表
     */
    private List<SkillInfo> player1Skills;
    
    /**
     * 玩家2的技能列表
     */
    private List<SkillInfo> player2Skills;
    
    /**
     * 技能冷却信息
     */
    @Data
    public static class SkillInfo {
        private Long skillId;
        private String skillName;
        private Integer cooldown;
        private Integer remainingCooldown;
    }
}