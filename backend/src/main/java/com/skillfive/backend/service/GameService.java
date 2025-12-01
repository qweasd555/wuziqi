package com.skillfive.backend.service;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.enums.GameMode;
import com.skillfive.backend.enums.GameType;

import java.util.List;
import java.util.Optional;

/**
 * 游戏服务接口
 */
public interface GameService {

    /**
     * 创建新游戏
     */
    Game createGame(Long player1Id, GameMode mode, GameType type);

    /**
     * 加入游戏
     */
    Game joinGame(Long gameId, Long player2Id);

    /**
     * 根据ID查找游戏
     */
    Optional<Game> findById(Long id);

    /**
     * 更新游戏状态
     */
    Game updateGame(Game game);

    /**
     * 执行玩家移动
     */
    Game makeMove(Long gameId, Long userId, Integer x, Integer y);

    /**
     * 使用技能
     */
    Game useSkill(Long gameId, Long userId, Long skillId);

    /**
     * 结束游戏
     */
    Game endGame(Long gameId, Long winnerId);

    /**
     * 查找用户的所有游戏
     */
    List<Game> findGamesByUserId(Long userId);

    /**
     * 查找进行中的游戏
     */
    List<Game> findActiveGamesByUserId(Long userId);

    /**
     * 查找等待中的游戏
     */
    Optional<Game> findWaitingGame();

    /**
     * 查找可加入的游戏
     */
    List<Game> findAvailableGames(GameMode mode);

    /**
     * 创建游戏（简化版）
     */
    Game createGame(Long player1Id, GameMode mode);

    /**
     * 执行移动（简化版）
     */
    Game makeMove(Long gameId, Long userId, Integer position);

    /**
     * 放弃游戏
     */
    Game giveUpGame(Long gameId, Long userId);
    
    /**
     * 广播游戏更新
     */
    void broadcastGameUpdate(Game game);
}