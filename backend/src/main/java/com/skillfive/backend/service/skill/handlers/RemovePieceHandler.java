package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 移除棋子技能处理器
 * 移除指定位置的棋子
 */
public class RemovePieceHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "REMOVE_PIECE";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            throw new IllegalArgumentException("无效的目标位置");
        }
        
        String boardState = game.getBoardState();
        if (boardState.charAt(targetPosition) == '0') {
            throw new IllegalStateException("目标位置没有棋子");
        }
        
        // 移除指定位置的棋子
        StringBuilder newBoard = new StringBuilder(boardState);
        newBoard.setCharAt(targetPosition, '0');
        game.setBoardState(newBoard.toString());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用移除棋子技能，移除了位置 %d 的棋子！", userId, targetPosition);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            return false;
        }
        
        // 检查目标位置是否有棋子
        String boardState = game.getBoardState();
        return boardState.charAt(targetPosition) != '0';
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s - 移除棋盘上的一个棋子", skill.getName());
    }
}