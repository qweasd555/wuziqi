package com.skillfive.backend.service;

import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.enums.SkillType;

import java.util.List;
import java.util.Optional;

/**
 * 技能服务接口
 */
public interface SkillService {

    /**
     * 创建新技能
     */
    Skill createSkill(Skill skill);

    /**
     * 根据ID查找技能
     */
    Optional<Skill> findById(Long id);

    /**
     * 更新技能
     */
    Skill updateSkill(Skill skill);

    /**
     * 删除技能（软删除）
     */
    void deleteSkill(Long id);

    /**
     * 根据技能类型查找技能
     */
    List<Skill> findByType(SkillType type);

    /**
     * 获取所有启用的技能
     */
    List<Skill> findAllEnabled();

    /**
     * 根据类型获取启用的技能
     */
    List<Skill> findEnabledByType(SkillType type);

    /**
     * 检查技能是否可用
     */
    boolean isSkillAvailable(Long skillId, Long userId, Long gameId);
    
    /**
     * 记录技能使用时间，开始冷却
     */
    void startSkillCooldown(Long skillId, Long userId, Long gameId);
    
    /**
     * 获取技能剩余冷却时间（秒）
     */
    int getRemainingCooldown(Long skillId, Long userId, Long gameId);
}