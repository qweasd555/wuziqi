package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 额外回合技能处理器
 * 让玩家可以连续行动
 */
public class ExtraTurnHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "EXTRA_TURN";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        // 保持当前玩家不变，实现额外回合
        game.setCurrentPlayer(userId.intValue());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用了额外回合技能，可以继续行动！", userId);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        // 只有当前玩家可以使用额外回合技能
        return userId.longValue() == game.getCurrentPlayer();
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s - 获得额外一个回合的行动机会", skill.getName());
    }
}