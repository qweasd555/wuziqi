package com.skillfive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 游戏记录实体类
 */
@Entity
@Data
@Table(name = "game_records")
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 游戏ID
     */
    @Column(name = "game_id")
    private Long gameId;

    /**
     * 玩家1ID
     */
    @Column(name = "player1_id")
    private Long player1Id;

    /**
     * 玩家2ID
     */
    @Column(name = "player2_id")
    private Long player2Id;

    /**
     * 获胜者ID
     */
    @Column(name = "winner_id")
    private Long winnerId;

    /**
     * 移动记录（JSON格式存储）
     */
    private String moveRecords;

    /**
     * 技能使用记录（JSON格式存储）
     */
    private String skillRecords;

    /**
     * 游戏开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 游戏结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 游戏持续时间（秒）
     */
    private Integer duration;

    /**
     * 游戏状态
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}