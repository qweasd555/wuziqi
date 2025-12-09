package com.skillfive.backend.service;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.enums.GameStatus;
import com.skillfive.backend.entity.User;
import com.skillfive.backend.enums.GameType;
import com.skillfive.backend.repository.GameRepository;
import com.skillfive.backend.repository.UserRepository;
import com.skillfive.backend.utils.GameUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 游戏流程管理服务
 * 处理游戏创建、开始、移动等核心流程
 */
@Service
public class GameFlowService {
    private static final Logger log = LoggerFactory.getLogger(GameFlowService.class);

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private GameService gameService;

    /**
     * 创建新游戏
     */
    @Transactional
    public Game createGame(GameType type, Long player1Id, Long player2Id) {
        Game game = new Game();
        game.setType(type);
        
        // 设置玩家 - 需要先获取User对象
        User player1 = userRepository.findById(player1Id)
                .orElseThrow(() -> new RuntimeException("玩家1不存在"));
        game.setPlayer1(player1);
        
        if (player2Id != null) {
            User player2 = userRepository.findById(player2Id)
                    .orElseThrow(() -> new RuntimeException("玩家2不存在"));
            game.setPlayer2(player2);
        }
        
        game.setStatus(GameStatus.WAITING);
        game.setBoardState(GameUtil.createEmptyBoard());
        game.setCurrentPlayer(1);
        game.setCreatedTime(LocalDateTime.now());
        game.setUpdatedTime(LocalDateTime.now());

        return gameRepository.save(game);
    }

    /**
     * 开始游戏
     */
    @Transactional
    public Game startGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        if (game.getStatus() != GameStatus.WAITING) {
            throw new RuntimeException("游戏状态错误，无法开始");
        }

        game.setStatus(GameStatus.IN_PROGRESS);
        game.setStartTime(LocalDateTime.now());
        game.setUpdatedTime(LocalDateTime.now());

        Game savedGame = gameRepository.save(game);
        
        // 广播游戏开始事件
        gameService.broadcastGameUpdate(savedGame);
        
        return savedGame;
    }

    /**
     * 执行玩家移动
     */
    @Transactional
    public Game makeMove(Long gameId, int row, int col, Long playerId) {
        log.info("makeMove被调用 - 游戏ID: {}, 玩家ID: {}, 位置: ({}, {})", gameId, playerId, row, col);
        
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        // 验证游戏状态
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new RuntimeException("游戏不在进行中");
        }

        // 验证玩家权限
        if (!isPlayerTurn(game, playerId)) {
            throw new RuntimeException("不是你的回合");
        }

        // 验证移动合法性
        if (!isValidMove(game.getBoardState(), row, col)) {
            throw new RuntimeException("非法移动");
        }

        // 执行移动
        String boardState = game.getBoardState();
        char[] board = boardState.toCharArray();
        int position = row * GameUtil.BOARD_SIZE + col;
        char playerSymbol = getPlayerSymbol(game, playerId);
        
        board[position] = playerSymbol;
        String newBoardState = new String(board);
        game.setBoardState(newBoardState);

        // 检查获胜条件
        if (GameUtil.hasWinner(newBoardState, playerSymbol)) {
            game.setWinner(getWinnerKey(game, playerId));
            game.setStatus(GameStatus.FINISHED);
            game.setEndTime(LocalDateTime.now());
        } else if (isDraw(newBoardState)) {
            game.setWinner("draw");
            game.setStatus(GameStatus.FINISHED);
            game.setEndTime(LocalDateTime.now());
        } else {
            // 切换玩家
        int newPlayer = game.getCurrentPlayer() == 1 ? 2 : 1;
        log.info("切换玩家 - 从 {} 到 {}", game.getCurrentPlayer(), newPlayer);
        log.info("游戏类型: {}, VS_AI: {}", game.getType(), GameType.VS_AI);
        log.info("游戏状态: {}", game.getStatus());
        game.setCurrentPlayer(newPlayer);
        }

        game.setUpdatedTime(LocalDateTime.now());
        Game savedGame = gameRepository.save(game);

        // 广播游戏状态更新
        gameService.broadcastGameUpdate(savedGame);

        // 如果是人机对战且轮到AI，触发AI移动
        log.info("检查AI移动条件 - 游戏类型: {}, 状态: {}, 当前玩家: {}", 
                  savedGame.getType(), savedGame.getStatus(), savedGame.getCurrentPlayer());
        log.info("条件检查 - VS_AI: {}, IN_PROGRESS: {}, currentPlayer == 2: {}", 
                  savedGame.getType() == GameType.VS_AI, 
                  game.getStatus() == GameStatus.IN_PROGRESS,
                  savedGame.getCurrentPlayer() == 2);
        if (savedGame.getType() == GameType.VS_AI && 
           game.getStatus() == GameStatus.IN_PROGRESS && 
            savedGame.getCurrentPlayer() == 2) {
            
            log.info("触发AI移动 - 游戏ID: {}", savedGame.getId());
            try {
                // 同步执行AI移动，便于调试
                Game aiMovedGame = aiService.makeAiMove(savedGame.getId());
                log.info("AI移动执行完成 - 游戏ID: {}, 新状态: {}", aiMovedGame.getId(), aiMovedGame.getStatus());
                return aiMovedGame;
            } catch (Exception e) {
                log.error("AI移动执行失败 - 游戏ID: {}, 错误: {}", savedGame.getId(), e.getMessage(), e);
            }
        } else {
            log.info("不触发AI移动 - 条件不满足");
        }

        return savedGame;
    }

    /**
     * 重置游戏
     */
    @Transactional
    public Game resetGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        game.setBoardState(GameUtil.createEmptyBoard());
        game.setCurrentPlayer(1);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setWinner(null);
        game.setEndTime(null);
        game.setStartTime(LocalDateTime.now());
        game.setUpdatedTime(LocalDateTime.now());

        Game savedGame = gameRepository.save(game);
        
        // 广播游戏重置事件
        gameService.broadcastGameUpdate(savedGame);
        
        return savedGame;
    }

    /**
     * 获取游戏状态
     */
    public Map<String, Object> getGameState(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        Map<String, Object> state = new HashMap<>();
        state.put("gameId", game.getId());
        state.put("status", game.getStatus());
        state.put("currentPlayer", game.getCurrentPlayer());
        state.put("boardState", game.getBoardState());
        state.put("winner", game.getWinner());
        state.put("type", game.getType());
        state.put("player1Id", game.getPlayer1Id());
        state.put("player2Id", game.getPlayer2Id());
        state.put("createdTime", game.getCreatedTime());
        state.put("startTime", game.getStartTime());
        state.put("endTime", game.getEndTime());
        state.put("isAiTurn", isAiTurn(game));
        
        return state;
    }

    /**
     * 检查是否是玩家回合
     */
    private boolean isPlayerTurn(Game game, Long playerId) {
        if (game.getCurrentPlayer() == 1) {
            return game.getPlayer1Id().equals(playerId);
        } else {
            // 对于VS_AI游戏，player2为null，此时检查是否为AI回合
            if (game.getType() == GameType.VS_AI) {
                return false; // AI回合，人类玩家不能操作
            }
            return game.getPlayer2Id() != null && game.getPlayer2Id().equals(playerId);
        }
    }

    /**
     * 获取玩家符号
     */
    private char getPlayerSymbol(Game game, Long playerId) {
        if (game.getPlayer1Id().equals(playerId)) {
            return GameUtil.PLAYER1;
        } else {
            // 对于VS_AI游戏，检查player2是否为null
            if (game.getType() == GameType.VS_AI && game.getPlayer2Id() == null) {
                return GameUtil.PLAYER2; // AI玩家使用PLAYER2符号
            }
            return game.getPlayer2Id() != null && game.getPlayer2Id().equals(playerId) ? GameUtil.PLAYER2 : GameUtil.PLAYER2;
        }
    }

    /**
     * 验证移动合法性
     */
    private boolean isValidMove(String boardState, int row, int col) {
        if (row < 0 || row >= GameUtil.BOARD_SIZE || col < 0 || col >= GameUtil.BOARD_SIZE) {
            return false;
        }
        
        int position = row * GameUtil.BOARD_SIZE + col;
        return boardState.charAt(position) == GameUtil.EMPTY;
    }

    /**
     * 检查是否平局
     */
    private boolean isDraw(String boardState) {
        return boardState.indexOf(GameUtil.EMPTY) == -1;
    }

    /**
     * 获取获胜者标识
     */
    private String getWinnerKey(Game game, Long playerId) {
        if (game.getPlayer1Id().equals(playerId)) {
            return "player1";
        } else {
            // 对于VS_AI游戏，检查player2是否为null
            if (game.getType() == GameType.VS_AI && game.getPlayer2Id() == null) {
                return "player2"; // AI玩家获胜
            }
            return game.getPlayer2Id() != null && game.getPlayer2Id().equals(playerId) ? "player2" : "player2";
        }
    }

    /**
     * 检查是否是AI回合
     */
    private boolean isAiTurn(Game game) {
        return game.getType() == GameType.VS_AI && 
               game.getCurrentPlayer() == 2 && 
              game.getStatus() == GameStatus.IN_PROGRESS;

    }
}