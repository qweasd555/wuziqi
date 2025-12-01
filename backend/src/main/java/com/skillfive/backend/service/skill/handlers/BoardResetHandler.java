package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 重置棋盘技能处理器
 * 清空棋盘上的所有棋子
 */
public class BoardResetHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "BOARD_RESET";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        // 清空棋盘 - 将所有位置重置为0
        StringBuilder newBoard = new StringBuilder();
        for (int i = 0; i < 225; i++) { // 15x15棋盘
            newBoard.append("0");
        }
        game.setBoardState(newBoard.toString());
        
        // 重置当前玩家为玩家1
        game.setCurrentPlayer(1);
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用了重置棋盘技能，棋盘已清空！", userId);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        // 任何玩家都可以使用重置技能
        return game.getPlayer1Id().equals(userId) || game.getPlayer2Id().equals(userId);
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s - 清空整个棋盘，重新开始对局", skill.getName());
    }
}