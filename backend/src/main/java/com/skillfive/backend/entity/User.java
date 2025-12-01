package com.skillfive.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String openId;      // 微信openId

    private String nickname;    // 用户昵称
    private String avatarUrl;   // 头像URL
    private Integer score = 0;  // 积分，默认0
    private Integer winCount = 0; // 胜场
    private Integer totalCount = 0; // 总对局
    private Integer rankLevel = 1; // 段位等级

    /**
     * 创建时间
     */
    @Column(updatable = false)
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

    // 计算胜率
    public Double getWinRate() {
        return totalCount == 0 ? 0.0 : (double) winCount / totalCount * 100;
    }
}