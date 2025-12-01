package com.skillfive.backend.service.impl;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.User;
import com.skillfive.backend.enums.GameMode;
import com.skillfive.backend.enums.GameType;
import com.skillfive.backend.repository.GameRepository;
import com.skillfive.backend.repository.UserRepository;
import com.skillfive.backend.service.GameService;
import com.skillfive.backend.service.SkillService;
import com.skillfive.backend.utils.GameUtil;
import com.skillfive.backend.utils.JsonUtil;
import com.skillfive.backend.websocket.WebSocketSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏服务实现类
 */
@Service
public class GameServiceImpl implements GameService {

    private static final Logger logger = LoggerFactory.getLogger(GameServiceImpl.class);

    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final SkillService skillService;
    
    @Autowired
    private WebSocketSessionManager webSocketSessionManager;

    public GameServiceImpl(GameRepository gameRepository, UserRepository userRepository, SkillService skillService) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
        this.skillService = skillService;
    }

    @Override
    public Game createGame(Long player1Id, GameMode mode, GameType type) {
        User player1 = userRepository.findById(player1Id)
                .orElseThrow(() -> new RuntimeException("玩家不存在"));

        Game game = new Game();
        game.setPlayer1(player1);
        game.setMode(mode);
        game.setType(type);
        game.setStatus("PENDING");
        game.setBoardState(GameUtil.createEmptyBoard()); // 使用正确的空棋盘初始化
        game.setCurrentPlayer(1);
        
        return gameRepository.save(game);
    }

    @Override
    public Game joinGame(Long gameId, Long player2Id) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        if (game.getPlayer2() != null) {
            throw new RuntimeException("游戏已满");
        }

        // 如果是AI玩家（player2Id为null）
        if (player2Id == null) {
            game.setPlayer2(null); // AI玩家
        } else {
            User player2 = userRepository.findById(player2Id)
                    .orElseThrow(() -> new RuntimeException("玩家不存在"));
            game.setPlayer2(player2);
        }
        
        game.setStatus("IN_PROGRESS");
        
        // 保存游戏状态
        Game savedGame = gameRepository.save(game);
        
        // 广播游戏更新
        broadcastGameUpdate(savedGame);
        
        return savedGame;
    }
    
    /**
     * 广播游戏更新
     */
    public void broadcastGameUpdate(Game game) {
        try {
            Map<String, Object> gameData = new HashMap<>();
            gameData.put("gameId", game.getId());
            gameData.put("status", game.getStatus());
            gameData.put("currentPlayer", game.getCurrentPlayer());
            gameData.put("boardState", game.getBoardState());
            
            // 添加获胜者信息
            if (game.getWinner() != null) {
                gameData.put("winner", game.getWinner());
            }
            
            // 添加玩家信息
            if (game.getPlayer1() != null) {
                gameData.put("player1Id", game.getPlayer1().getId());
                gameData.put("player1Nickname", game.getPlayer1().getNickname());
                gameData.put("player1AvatarUrl", game.getPlayer1().getAvatarUrl());
            }
            
            if (game.getPlayer2() != null) {
                gameData.put("player2Id", game.getPlayer2().getId());
                gameData.put("player2Nickname", game.getPlayer2().getNickname());
                gameData.put("player2AvatarUrl", game.getPlayer2().getAvatarUrl());
            } else if (game.getType() == GameType.VS_AI) {
                gameData.put("player2Id", null);
                gameData.put("player2Nickname", "AI");
                gameData.put("player2AvatarUrl", "https://via.placeholder.com/40");
            }
            
            // 通过WebSocket广播游戏更新
            try {
                // 构建消息
                Map<String, Object> message = new ConcurrentHashMap<>();
                message.put("type", "game_update");
                message.put("data", gameData);
                
                // 发送消息
                webSocketSessionManager.sendMessageToGame(game.getId().toString(), JsonUtil.toJson(message));
            } catch (Exception e) {
                logger.error("构建游戏更新消息失败", e);
            }
        } catch (Exception e) {
            logger.error("广播游戏更新失败", e);
        }
    }

    @Override
    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    @Override
    public Game updateGame(Game game) {
        return gameRepository.save(game);
    }

    @Override
    public Game makeMove(Long gameId, Long userId, Integer x, Integer y) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        // 验证是否是当前玩家的回合
        if (!isCurrentPlayer(game, userId)) {
            throw new RuntimeException("不是您的回合");
        }

        // 获取当前玩家符号
        char currentSymbol = (game.getCurrentPlayer() == 1) ? GameUtil.PLAYER1 : GameUtil.PLAYER2;
        
        // 执行移动
        String newBoardState = GameUtil.makeMove(game.getBoardState(), x, y, currentSymbol);
        game.setBoardState(newBoardState);
        
        // 检查是否有获胜者
        if (GameUtil.hasWinner(newBoardState, currentSymbol)) {
            // 游戏结束，设置获胜者
            game.setStatus("FINISHED");
            game.setWinner(game.getCurrentPlayer() == 1 ? "player1" : "player2");
            game.setEndTime(java.time.LocalDateTime.now());
        } else if (GameUtil.isBoardFull(newBoardState)) {
            // 平局
            game.setStatus("FINISHED");
            game.setWinner("draw");
            game.setEndTime(java.time.LocalDateTime.now());
        } else {
            // 切换当前玩家
            game.setCurrentPlayer(game.getCurrentPlayer() == 1 ? 2 : 1);
        }
        
        // 保存游戏状态
        Game savedGame = gameRepository.save(game);
        
        // 广播游戏更新
        broadcastGameUpdate(savedGame);
        
        return savedGame;
    }

    @Override
    public Game useSkill(Long gameId, Long userId, Long skillId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        // 验证是否是当前玩家的回合
        if (!isCurrentPlayer(game, userId)) {
            throw new RuntimeException("不是您的回合");
        }

        // 验证技能是否可用
        if (!skillService.isSkillAvailable(skillId, userId, gameId)) {
            throw new RuntimeException("技能不可用或正在冷却中");
        }

        // 应用技能效果（这里实现一些简单的技能效果）
        String boardState = game.getBoardState();
        
        // 根据技能ID应用不同的效果
        switch (skillId.intValue()) {
            case 1: // 重置棋盘技能
                boardState = GameUtil.createEmptyBoard();
                game.setBoardState(boardState);
                break;
            case 2: // 额外回合技能 - 不改变当前玩家
                // 当前玩家保持不变
                break;
            case 3: // 随机清除一个棋子
                boardState = GameUtil.removeRandomPiece(boardState);
                game.setBoardState(boardState);
                break;
            default:
                // 其他技能效果可以在这里扩展
                break;
        }

        // 开始技能冷却（除了额外回合技能）
        if (skillId != 2) {
            ((SkillServiceImpl) skillService).startSkillCooldown(skillId, userId, gameId);
        }
        
        // 保存游戏状态
        Game savedGame = gameRepository.save(game);
        
        // 广播游戏更新
        broadcastGameUpdate(savedGame);
        
        return savedGame;
    }

    @Override
    public Game endGame(Long gameId, Long winnerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        game.setStatus("FINISHED");
        if (winnerId != null) {
            if (winnerId.equals(game.getPlayer1().getId())) {
                game.setWinner("player1");
            } else if (winnerId.equals(game.getPlayer2().getId())) {
                game.setWinner("player2");
            }
        } else {
            game.setWinner("draw");
        }
        
        // 保存游戏状态
        Game savedGame = gameRepository.save(game);
        
        // 广播游戏更新
        broadcastGameUpdate(savedGame);
        
        return savedGame;
    }

    @Override
    public List<Game> findGamesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return gameRepository.findByPlayer1OrPlayer2(user, user);
    }

    @Override
    public List<Game> findActiveGamesByUserId(Long userId) {
        return gameRepository.findActiveGamesByUserId(userId);
    }

    @Override
    public Optional<Game> findWaitingGame() {
        return gameRepository.findFirstByStatusAndPlayer2IsNullOrderByStartTime("PENDING");
    }

    @Override
    public List<Game> findAvailableGames(GameMode mode) {
        return gameRepository.findByStatusAndModeAndPlayer2IsNull("PENDING", mode);
    }

    @Override
    public Game createGame(Long player1Id, GameMode mode) {
        return createGame(player1Id, mode, GameType.GOMOKU);
    }

    @Override
    public Game makeMove(Long gameId, Long userId, Integer position) {
        // 将位置转换为坐标（假设是15x15棋盘）
        int x = position / 15;
        int y = position % 15;
        return makeMove(gameId, userId, x, y);
    }

    @Override
    public Game giveUpGame(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        // 确定胜者
        Long winnerId = null;
        if (userId.equals(game.getPlayer1().getId())) {
            winnerId = game.getPlayer2() != null ? game.getPlayer2().getId() : null;
        } else if (game.getPlayer2() != null && userId.equals(game.getPlayer2().getId())) {
            winnerId = game.getPlayer1().getId();
        }

        return endGame(gameId, winnerId);
    }

    /**
     * 验证是否是当前玩家的回合
     */
    private boolean isCurrentPlayer(Game game, Long userId) {
        if (game.getCurrentPlayer() == 1) {
            return game.getPlayer1() != null && userId.equals(game.getPlayer1().getId());
        } else {
            return game.getPlayer2() != null && userId.equals(game.getPlayer2().getId());
        }
    }
}