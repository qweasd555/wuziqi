package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 交换棋子技能处理器
 * 交换棋盘上的两个棋子位置
 */
public class SwapPiecesHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "SWAP_PIECES";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            throw new IllegalArgumentException("无效的目标位置");
        }
        
        // 解析参数获取第二个位置
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("需要提供第二个位置参数");
        }
        
        int secondPosition;
        try {
            secondPosition = Integer.parseInt(params);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("位置参数格式错误");
        }
        
        if (secondPosition < 0 || secondPosition >= 225) {
            throw new IllegalArgumentException("第二个位置无效");
        }
        
        if (targetPosition == secondPosition) {
            throw new IllegalArgumentException("不能交换相同位置");
        }
        
        String boardState = game.getBoardState();
        char firstPiece = boardState.charAt(targetPosition);
        char secondPiece = boardState.charAt(secondPosition);
        
        if (firstPiece == '0' || secondPiece == '0') {
            throw new IllegalStateException("目标位置必须有棋子才能交换");
        }
        
        // 交换两个位置的棋子
        StringBuilder newBoard = new StringBuilder(boardState);
        newBoard.setCharAt(targetPosition, secondPiece);
        newBoard.setCharAt(secondPosition, firstPiece);
        game.setBoardState(newBoard.toString());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用交换棋子技能，交换了位置 %d 和 %d 的棋子！", userId, targetPosition, secondPosition);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            return false;
        }
        
        // 需要两个位置都有棋子才能交换
        String boardState = game.getBoardState();
        char firstPiece = boardState.charAt(targetPosition);
        
        return firstPiece != '0'; // 第一个位置有棋子即可开始交换
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s：%s 消耗：%d 冷却：%d秒", 
            skill.getName(), skill.getDescription(), skill.getCost(), skill.getCooldown());
    }
}