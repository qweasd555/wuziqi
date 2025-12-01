package com.skillfive.backend.service;

/**
 * AI服务接口
 * 处理人机对战的AI逻辑
 */
public interface AiService {
    
    /**
     * 获取AI的最佳移动
     * 
     * @param board 当前棋盘状态
     * @param aiSymbol AI的棋子符号
     * @param humanSymbol 人类的棋子符号
     * @return 最佳移动坐标 [x, y]
     */
    int[] getBestMove(String board, char aiSymbol, char humanSymbol);
    
    /**
     * 执行AI移动
     * 
     * @param gameId 游戏ID
     * @return 更新后的游戏
     */
    com.skillfive.backend.entity.Game makeAiMove(Long gameId);
    
    /**
     * 检查是否需要AI移动
     * 
     * @param gameId 游戏ID
     * @return 是否需要AI移动
     */
    boolean shouldAiMove(Long gameId);
    
    /**
     * 设置AI难度
     * 
     * @param difficulty 难度等级 (1-3)
     */
    void setDifficulty(int difficulty);
}