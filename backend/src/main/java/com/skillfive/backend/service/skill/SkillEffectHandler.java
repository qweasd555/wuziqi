package com.skillfive.backend.service.skill;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;

/**
 * 技能效果处理器接口
 * 定义技能效果的处理逻辑
 */
public interface SkillEffectHandler {
    
    /**
     * 获取处理器支持的技能效果类型
     * 
     * @return 技能效果类型
     */
    String getSupportedEffectType();
    
    /**
     * 执行技能效果
     * 
     * @param game 当前游戏
     * @param skill 使用的技能
     * @param userId 使用技能的用户ID
     * @param targetPosition 目标位置（可选）
     * @param params 额外参数（可选）
     * @return 更新后的游戏
     */
    Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params);
    
    /**
     * 验证技能是否可以使用
     * 
     * @param game 当前游戏
     * @param skill 要使用的技能
     * @param userId 使用技能的用户ID
     * @param targetPosition 目标位置（可选）
     * @return 是否可以使用
     */
    boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition);
    
    /**
     * 获取技能的描述信息
     * 
     * @param skill 技能
     * @return 描述信息
     */
    String getSkillDescription(Skill skill);
}