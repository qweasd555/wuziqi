package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 护盾技能处理器
 * 为玩家提供护盾保护，防止一次攻击
 */
public class ShieldHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "SHIELD";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        // 护盾技能不需要目标位置
        
        // 获取当前护盾状态
        String gameData = game.getGameData();
        int shieldCount = 0;
        
        if (gameData != null && !gameData.isEmpty()) {
            try {
                // 简单的护盾计数格式：shield:X
                if (gameData.contains("shield:")) {
                    String[] parts = gameData.split(";");
                    for (String part : parts) {
                        if (part.startsWith("shield:")) {
                            shieldCount = Integer.parseInt(part.split(":")[1]);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，重置为0
                shieldCount = 0;
            }
        }
        
        // 增加护盾层数（默认增加1层）
        int shieldLayers = 1;
        if (params != null && !params.isEmpty()) {
            try {
                shieldLayers = Integer.parseInt(params);
            } catch (NumberFormatException e) {
                shieldLayers = 1;
            }
        }
        
        shieldCount += shieldLayers;
        
        // 更新游戏数据
        StringBuilder newGameData = new StringBuilder();
        if (gameData != null && !gameData.isEmpty()) {
            // 移除旧的护盾数据
            String[] parts = gameData.split(";");
            for (String part : parts) {
                if (!part.startsWith("shield:")) {
                    newGameData.append(part).append(";");
                }
            }
        }
        newGameData.append("shield:").append(shieldCount);
        game.setGameData(newGameData.toString());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用护盾技能，获得 %d 层护盾保护！", userId, shieldLayers);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        // 护盾技能可以随时使用，没有特殊限制
        return true;
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s：%s 消耗：%d 冷却：%d秒", 
            skill.getName(), skill.getDescription(), skill.getCost(), skill.getCooldown());
    }
}