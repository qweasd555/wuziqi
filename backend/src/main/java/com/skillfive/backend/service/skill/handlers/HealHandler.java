package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 治疗技能处理器
 * 为玩家恢复生命值
 */
public class HealHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "HEAL";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        // 治疗技能不需要目标位置
        
        // 获取当前生命值
        String gameData = game.getGameData();
        int currentHealth = 100; // 默认生命值
        int maxHealth = 100; // 最大生命值
        
        if (gameData != null && !gameData.isEmpty()) {
            try {
                // 解析生命值格式：health:current/max
                if (gameData.contains("health:")) {
                    String[] parts = gameData.split(";");
                    for (String part : parts) {
                        if (part.startsWith("health:")) {
                            String healthData = part.split(":")[1];
                            String[] healthParts = healthData.split("/");
                            currentHealth = Integer.parseInt(healthParts[0]);
                            maxHealth = Integer.parseInt(healthParts[1]);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，使用默认值
                currentHealth = 100;
                maxHealth = 100;
            }
        }
        
        // 计算治疗量（默认治疗20点）
        int healAmount = 20;
        if (params != null && !params.isEmpty()) {
            try {
                healAmount = Integer.parseInt(params);
            } catch (NumberFormatException e) {
                healAmount = 20;
            }
        }
        
        // 应用治疗，但不能超过最大生命值
        int newHealth = Math.min(currentHealth + healAmount, maxHealth);
        int actualHeal = newHealth - currentHealth;
        
        // 更新游戏数据
        StringBuilder newGameData = new StringBuilder();
        if (gameData != null && !gameData.isEmpty()) {
            // 移除旧的生命值数据
            String[] parts = gameData.split(";");
            for (String part : parts) {
                if (!part.startsWith("health:")) {
                    newGameData.append(part).append(";");
                }
            }
        }
        newGameData.append("health:").append(newHealth).append("/").append(maxHealth);
        game.setGameData(newGameData.toString());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用治疗技能，恢复了 %d 点生命值！当前生命：%d/%d", 
            userId, actualHeal, newHealth, maxHealth);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        // 检查游戏数据中的生命值
        String gameData = game.getGameData();
        if (gameData != null && !gameData.isEmpty()) {
            try {
                if (gameData.contains("health:")) {
                    String[] parts = gameData.split(";");
                    for (String part : parts) {
                        if (part.startsWith("health:")) {
                            String healthData = part.split(":")[1];
                            String[] healthParts = healthData.split("/");
                            int currentHealth = Integer.parseInt(healthParts[0]);
                            int maxHealth = Integer.parseInt(healthParts[1]);
                            
                            // 只有当前生命值小于最大生命值时才能使用治疗
                            return currentHealth < maxHealth;
                        }
                    }
                }
            } catch (Exception e) {
                // 如果解析失败，允许使用治疗
                return true;
            }
        }
        
        // 如果没有生命值数据，允许使用治疗
        return true;
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s：%s 消耗：%d 冷却：%d秒", 
            skill.getName(), skill.getDescription(), skill.getCost(), skill.getCooldown());
    }
}