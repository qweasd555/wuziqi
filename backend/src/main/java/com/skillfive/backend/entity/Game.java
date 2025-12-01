package com.skillfive.backend.entity;

import com.skillfive.backend.enums.GameMode;
import com.skillfive.backend.enums.GameType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Data
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private GameMode mode; // 游戏模式: REGULAR, SKILL

    @Enumerated(EnumType.STRING)
    private GameType type; // 游戏类型: VS_AI, LOCAL_PVP, ONLINE_PVP

    @ManyToOne
    @JoinColumn(name = "player1_id")
    private User player1;

    @ManyToOne
    @JoinColumn(name = "player2_id")
    private User player2;

    private String boardState; // 棋盘状态JSON
    private Integer currentPlayer = 1; // 当前玩家
    private String winner; // 获胜方: player1, player2, draw

    private LocalDateTime startTime = LocalDateTime.now();
    private LocalDateTime endTime;

    // 技能相关
    private String player1Skills; // 玩家1技能JSON
    private String player2Skills; // 玩家2技能JSON
    private String usedSkills;    // 已使用技能记录
    private String gameData;      // 游戏额外数据（如护盾、冻结状态等）
    private String lastMove;      // 最后一步操作描述
    
    /**
     * 游戏状态
     */
    private String status = "PENDING"; // PENDING, IN_PROGRESS, FINISHED
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        startTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
    
    // 辅助方法：获取玩家ID
    public Long getPlayer1Id() {
        return player1 != null ? player1.getId() : null;
    }
    
    public Long getPlayer2Id() {
        return player2 != null ? player2.getId() : null;
    }
    
    // 辅助方法：获取当前玩家ID
    public Long getCurrentPlayerId() {
        if (currentPlayer == 1) {
            return getPlayer1Id();
        } else {
            return getPlayer2Id();
        }
    }
    
    // 辅助方法：设置当前玩家ID
    public void setCurrentPlayerId(Long userId) {
        if (userId != null) {
            if (userId.equals(getPlayer1Id())) {
                currentPlayer = 1;
            } else if (userId.equals(getPlayer2Id())) {
                currentPlayer = 2;
            }
        }
    }
    
    // 手动添加缺失的setter方法
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
    
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
}