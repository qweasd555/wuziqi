package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 冻结技能处理器
 * 冻结指定位置的棋子，使其暂时无法使用
 */
public class FreezeHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "FREEZE";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            throw new IllegalArgumentException("无效的目标位置");
        }
        
        String boardState = game.getBoardState();
        char targetPiece = boardState.charAt(targetPosition);
        
        if (targetPiece == '0') {
            throw new IllegalStateException("目标位置没有棋子");
        }
        
        // 获取当前玩家的棋子类型
        char playerPiece = (userId == game.getPlayer1().getId()) ? '1' : '2';
        char opponentPiece = (playerPiece == '1') ? '2' : '1';
        
        // 只能冻结对手的棋子
        if (targetPiece != opponentPiece) {
            throw new IllegalStateException("只能冻结对手的棋子");
        }
        
        // 获取冻结持续时间（默认2回合）
        int freezeDuration = 2;
        if (params != null && !params.isEmpty()) {
            try {
                freezeDuration = Integer.parseInt(params);
            } catch (NumberFormatException e) {
                freezeDuration = 2;
            }
        }
        
        // 更新游戏数据中的冻结状态
        String gameData = game.getGameData();
        StringBuilder newGameData = new StringBuilder();
        
        if (gameData != null && !gameData.isEmpty()) {
            // 移除旧的冻结数据
            String[] parts = gameData.split(";");
            for (String part : parts) {
                if (!part.startsWith("freeze:" + targetPosition + ":")) {
                    newGameData.append(part).append(";");
                }
            }
        }
        
        // 添加新的冻结状态
        newGameData.append("freeze:").append(targetPosition).append(":").append(freezeDuration);
        game.setGameData(newGameData.toString());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用冻结技能，冻结了位置 %d 的棋子 %d 回合！", userId, targetPosition, freezeDuration);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            return false;
        }
        
        String boardState = game.getBoardState();
        char targetPiece = boardState.charAt(targetPosition);
        
        if (targetPiece == '0') {
            return false;
        }
        
        // 获取当前玩家的棋子类型
        char playerPiece = (userId == game.getPlayer1().getId()) ? '1' : '2';
        char opponentPiece = (playerPiece == '1') ? '2' : '1';
        
        // 只能冻结对手的棋子
        if (targetPiece != opponentPiece) {
            return false;
        }
        
        // 检查该位置是否已经被冻结
        String gameData = game.getGameData();
        if (gameData != null && gameData.contains("freeze:" + targetPosition + ":")) {
            return false; // 该位置已经被冻结
        }
        
        return true;
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s：%s 消耗：%d 冷却：%d秒", 
            skill.getName(), skill.getDescription(), skill.getCost(), skill.getCooldown());
    }
}