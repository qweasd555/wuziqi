package com.skillfive.backend.entity;

import com.skillfive.backend.enums.SkillType;
import com.skillfive.backend.enums.SkillEffectType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 技能实体类
 */
@Entity
@Data
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 技能名称
     */
    private String name;

    /**
     * 技能描述
     */
    private String description;

    /**
     * 技能类型
     */
    @Enumerated(EnumType.STRING)
    private SkillType type;

    /**
     * 技能效果类型
     */
    @Enumerated(EnumType.STRING)
    private SkillEffectType effectType;

    /**
     * 冷却时间（秒）
     */
    private Integer cooldown;

    /**
     * 消耗（如果有）
     */
    private Integer cost;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

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