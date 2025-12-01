package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 强制移动技能处理器
 * 强制移动一个棋子到指定位置
 */
public class ForceMoveHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "FORCE_MOVE";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            throw new IllegalArgumentException("无效的目标位置");
        }
        
        // 解析参数获取目标位置
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("需要提供目标位置参数");
        }
        
        int destinationPosition;
        try {
            destinationPosition = Integer.parseInt(params);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("位置参数格式错误");
        }
        
        if (destinationPosition < 0 || destinationPosition >= 225) {
            throw new IllegalArgumentException("目标位置无效");
        }
        
        if (targetPosition == destinationPosition) {
            throw new IllegalArgumentException("不能移动到相同位置");
        }
        
        String boardState = game.getBoardState();
        char pieceToMove = boardState.charAt(targetPosition);
        char destinationPiece = boardState.charAt(destinationPosition);
        
        if (pieceToMove == '0') {
            throw new IllegalStateException("源位置没有棋子");
        }
        
        if (destinationPiece != '0') {
            throw new IllegalStateException("目标位置已有棋子");
        }
        
        // 执行强制移动
        StringBuilder newBoard = new StringBuilder(boardState);
        newBoard.setCharAt(targetPosition, '0'); // 清空源位置
        newBoard.setCharAt(destinationPosition, pieceToMove); // 移动到目标位置
        game.setBoardState(newBoard.toString());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用强制移动技能，将位置 %d 的棋子移动到位置 %d！", userId, targetPosition, destinationPosition);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            return false;
        }
        
        // 检查源位置是否有棋子
        String boardState = game.getBoardState();
        return boardState.charAt(targetPosition) != '0';
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s：%s 消耗：%d 冷却：%d秒", 
            skill.getName(), skill.getDescription(), skill.getCost(), skill.getCooldown());
    }
}