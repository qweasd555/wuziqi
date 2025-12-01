package com.skillfive.backend.service.skill.handlers;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.Skill;
import com.skillfive.backend.service.skill.SkillEffectHandler;

/**
 * 传送技能处理器
 * 将一个棋子传送到另一个位置
 */
public class TeleportHandler implements SkillEffectHandler {
    
    @Override
    public String getSupportedEffectType() {
        return "TELEPORT";
    }
    
    @Override
    public Game executeEffect(Game game, Skill skill, Long userId, Integer targetPosition, String params) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            throw new IllegalArgumentException("无效的目标位置");
        }
        
        // 解析参数获取源位置
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("需要提供源位置参数");
        }
        
        int sourcePosition;
        try {
            sourcePosition = Integer.parseInt(params);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("位置参数格式错误");
        }
        
        if (sourcePosition < 0 || sourcePosition >= 225) {
            throw new IllegalArgumentException("源位置无效");
        }
        
        if (targetPosition == sourcePosition) {
            throw new IllegalArgumentException("不能传送到相同位置");
        }
        
        String boardState = game.getBoardState();
        char sourcePiece = boardState.charAt(sourcePosition);
        char targetPiece = boardState.charAt(targetPosition);
        
        if (sourcePiece == '0') {
            throw new IllegalStateException("源位置必须有棋子才能传送");
        }
        
        if (targetPiece != '0') {
            throw new IllegalStateException("目标位置必须为空才能传送");
        }
        
        // 传送棋子
        StringBuilder newBoard = new StringBuilder(boardState);
        newBoard.setCharAt(sourcePosition, '0'); // 源位置置空
        newBoard.setCharAt(targetPosition, sourcePiece); // 目标位置放置棋子
        game.setBoardState(newBoard.toString());
        
        // 添加技能使用记录
        String effectDescription = String.format("玩家 %d 使用传送技能，将位置 %d 的棋子传送到位置 %d！", userId, sourcePosition, targetPosition);
        game.setLastMove(effectDescription);
        
        return game;
    }
    
    @Override
    public boolean canUseSkill(Game game, Skill skill, Long userId, Integer targetPosition) {
        if (targetPosition == null || targetPosition < 0 || targetPosition >= 225) {
            return false;
        }
        
        // 需要源位置有棋子，目标位置为空
        String boardState = game.getBoardState();
        char targetPiece = boardState.charAt(targetPosition);
        
        return targetPiece == '0'; // 目标位置为空即可开始传送
    }
    
    @Override
    public String getSkillDescription(Skill skill) {
        return String.format("%s：%s 消耗：%d 冷却：%d秒", 
            skill.getName(), skill.getDescription(), skill.getCost(), skill.getCooldown());
    }
}