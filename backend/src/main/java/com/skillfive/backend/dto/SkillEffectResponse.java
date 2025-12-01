package com.skillfive.backend.dto;

import lombok.Data;

/**
 * 技能效果响应DTO
 */
@Data
public class SkillEffectResponse {
    
    /**
     * 是否成功
     */
    private boolean success;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 更新后的游戏状态
     */
    private GameStateResponse gameState;
    
    /**
     * 技能效果描述
     */
    private String effectDescription;
    
    /**
     * 游戏状态响应DTO
     */
    @Data
    public static class GameStateResponse {
        private Long gameId;
        private String boardState;
        private Long currentPlayerId;
        private String lastMove;
        private String gameStatus;
    }
    
    public static SkillEffectResponse success(String message, GameStateResponse gameState, String effectDescription) {
        SkillEffectResponse response = new SkillEffectResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setGameState(gameState);
        response.setEffectDescription(effectDescription);
        return response;
    }
    
    public static SkillEffectResponse failure(String message) {
        SkillEffectResponse response = new SkillEffectResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}