package com.skillfive.backend.service.impl;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.repository.GameRepository;
import com.skillfive.backend.service.AiService;
import com.skillfive.backend.service.GameService;
import com.skillfive.backend.utils.GameUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * 增强版AI服务实现类
 * 提供智能的五子棋AI对战功能
 */
@Service
@Primary
public class EnhancedAiServiceImpl implements AiService {

    private final GameRepository gameRepository;
    private final GameService gameService;
    private int difficulty = 2; // 默认中等难度
    private final Random random = new Random();

    @Autowired
    public EnhancedAiServiceImpl(GameRepository gameRepository, GameService gameService) {
        this.gameRepository = gameRepository;
        this.gameService = gameService;
    }

    @Override
    public int[] getBestMove(String board, char aiSymbol, char humanSymbol) {
        int[] availablePositions = getAvailablePositions(board);
        if (availablePositions.length == 0) {
            return null;
        }

        // 根据难度选择策略
        switch (difficulty) {
            case 1: // 简单难度 - 随机下棋 + 基础防守
                return getEasyMove(board, availablePositions, aiSymbol, humanSymbol);
            case 2: // 中等难度 - 攻防平衡
                return getMediumMove(board, availablePositions, aiSymbol, humanSymbol);
            case 3: // 困难难度 - 高级攻防 + 策略优化
                return getHardMove(board, availablePositions, aiSymbol, humanSymbol);
            default:
                int randomPos = availablePositions[random.nextInt(availablePositions.length)];
                return new int[]{randomPos / GameUtil.BOARD_SIZE, randomPos % GameUtil.BOARD_SIZE};
        }
    }

    /**
     * 简单难度AI移动
     * 随机下棋，但会阻止明显的获胜机会
     */
    private int[] getEasyMove(String board, int[] availablePositions, char aiSymbol, char humanSymbol) {
        // 首先检查是否能直接获胜
        for (int pos : availablePositions) {
            String testBoard = placeStone(board, pos, aiSymbol);
            if (GameUtil.hasWinner(testBoard, aiSymbol)) {
                return new int[]{pos / GameUtil.BOARD_SIZE, pos % GameUtil.BOARD_SIZE};
            }
        }

        // 然后检查是否需要阻止对手获胜
        for (int pos : availablePositions) {
            String testBoard = placeStone(board, pos, humanSymbol);
            if (GameUtil.hasWinner(testBoard, humanSymbol)) {
                return new int[]{pos / GameUtil.BOARD_SIZE, pos % GameUtil.BOARD_SIZE};
            }
        }

        // 随机选择一个位置
        int pos = availablePositions[random.nextInt(availablePositions.length)];
        return new int[]{pos / GameUtil.BOARD_SIZE, pos % GameUtil.BOARD_SIZE};
    }

    /**
     * 中等难度AI移动
     * 平衡攻防策略
     */
    private int[] getMediumMove(String board, int[] availablePositions, char aiSymbol, char humanSymbol) {
        int bestScore = Integer.MIN_VALUE;
        int bestPos = availablePositions[0];

        for (int pos : availablePositions) {
            int score = evaluatePosition(board, pos, aiSymbol, humanSymbol);
            if (score > bestScore) {
                bestScore = score;
                bestPos = pos;
            }
        }

        return new int[]{bestPos / GameUtil.BOARD_SIZE, bestPos % GameUtil.BOARD_SIZE};
    }

    /**
     * 困难难度AI移动
     * 高级攻防策略 + 前瞻性思考
     */
    private int[] getHardMove(String board, int[] availablePositions, char aiSymbol, char humanSymbol) {
        int bestScore = Integer.MIN_VALUE;
        int bestPos = availablePositions[0];

        // 使用Minimax算法进行深度搜索
        for (int pos : availablePositions) {
            String newBoard = placeStone(board, pos, aiSymbol);
            
            // 检查是否能直接获胜
            if (GameUtil.hasWinner(newBoard, aiSymbol)) {
                return new int[]{pos / GameUtil.BOARD_SIZE, pos % GameUtil.BOARD_SIZE};
            }

            // 使用Minimax评估
            int score = minimax(newBoard, 2, false, aiSymbol, humanSymbol, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            // 加上位置评估
            score += evaluatePosition(board, pos, aiSymbol, humanSymbol);
            
            if (score > bestScore) {
                bestScore = score;
                bestPos = pos;
            }
        }

        return new int[]{bestPos / GameUtil.BOARD_SIZE, bestPos % GameUtil.BOARD_SIZE};
    }

    /**
     * Minimax算法实现
     */
    private int minimax(String board, int depth, boolean isMaximizing, char aiSymbol, char humanSymbol, int alpha, int beta) {
        if (depth == 0 || GameUtil.hasWinner(board, aiSymbol) || GameUtil.hasWinner(board, humanSymbol)) {
            return evaluateBoardState(board, aiSymbol, humanSymbol);
        }

        int[] availablePositions = getAvailablePositions(board);
        if (availablePositions.length == 0) {
            return 0; // 平局
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int pos : availablePositions) {
                String newBoard = placeStone(board, pos, aiSymbol);
                int eval = minimax(newBoard, depth - 1, false, aiSymbol, humanSymbol, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int pos : availablePositions) {
                String newBoard = placeStone(board, pos, humanSymbol);
                int eval = minimax(newBoard, depth - 1, true, aiSymbol, humanSymbol, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break; // Alpha-Beta剪枝
            }
            return minEval;
        }
    }

    /**
     * 评估棋盘状态
     */
    private int evaluateBoardState(String board, char aiSymbol, char humanSymbol) {
        if (GameUtil.hasWinner(board, aiSymbol)) {
            return 10000;
        }
        if (GameUtil.hasWinner(board, humanSymbol)) {
            return -10000;
        }
        
        // 使用GameUtil的评估函数
        return GameUtil.evaluateBoard(board, aiSymbol, humanSymbol);
    }

    /**
     * 评估位置的得分
     */
    private int evaluatePosition(String board, int pos, char aiSymbol, char humanSymbol) {
        int score = 0;

        // 评估AI在该位置的收益
        String aiBoard = placeStone(board, pos, aiSymbol);
        if (GameUtil.hasWinner(aiBoard, aiSymbol)) {
            return 1000; // 直接获胜
        }

        // 评估阻止对手的收益
        String humanBoard = placeStone(board, pos, humanSymbol);
        if (GameUtil.hasWinner(humanBoard, humanSymbol)) {
            return 900; // 阻止对手获胜
        }

        // 评估棋型得分
        score += evaluatePatterns(aiBoard, pos, aiSymbol) * 2;
        score += evaluatePatterns(humanBoard, pos, humanSymbol);

        // 中心位置加分
        int row = pos / GameUtil.BOARD_SIZE;
        int col = pos % GameUtil.BOARD_SIZE;
        int centerDistance = Math.abs(row - GameUtil.BOARD_SIZE / 2) + Math.abs(col - GameUtil.BOARD_SIZE / 2);
        score += (GameUtil.BOARD_SIZE - centerDistance);

        return score;
    }

    /**
     * 评估棋型模式
     */
    private int evaluatePatterns(String board, int pos, char symbol) {
        int score = 0;
        int row = pos / GameUtil.BOARD_SIZE;
        int col = pos % GameUtil.BOARD_SIZE;

        // 检查四个方向：水平、垂直、两条对角线
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1; // 当前位置

            // 正方向计数
            for (int i = 1; i < 5; i++) {
                int newRow = row + dir[0] * i;
                int newCol = col + dir[1] * i;
                if (isValidPosition(newRow, newCol) && 
                    board.charAt(newRow * GameUtil.BOARD_SIZE + newCol) == symbol) {
                    count++;
                } else {
                    break;
                }
            }

            // 反方向计数
            for (int i = 1; i < 5; i++) {
                int newRow = row - dir[0] * i;
                int newCol = col - dir[1] * i;
                if (isValidPosition(newRow, newCol) && 
                    board.charAt(newRow * GameUtil.BOARD_SIZE + newCol) == symbol) {
                    count++;
                } else {
                    break;
                }
            }

            // 根据连子数评分
            if (count >= 5) score += 10000;
            else if (count == 4) score += 1000;
            else if (count == 3) score += 100;
            else if (count == 2) score += 10;
        }

        return score;
    }

    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < GameUtil.BOARD_SIZE && col >= 0 && col < GameUtil.BOARD_SIZE;
    }

    private String placeStone(String board, int position, char stone) {
        char[] boardArray = board.toCharArray();
        boardArray[position] = stone;
        return new String(boardArray);
    }

    private int[] getAvailablePositions(String board) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < board.length(); i++) {
            if (board.charAt(i) == GameUtil.EMPTY) {
                positions.add(i);
            }
        }
        return positions.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public Game makeAiMove(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        if (!"IN_PROGRESS".equals(game.getStatus()) || game.getCurrentPlayer() != 2) {
            return game;
        }

        String boardState = game.getBoardState();
        if (boardState == null || boardState.isEmpty() || "{}".equals(boardState)) {
            boardState = GameUtil.createEmptyBoard();
            game.setBoardState(boardState);
        }

        // 添加思考时间，模拟真实AI思考
        try {
            Thread.sleep(500 + random.nextInt(1000)); // 0.5-1.5秒思考时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 获取AI的最佳移动
        int[] move = getBestMove(boardState, GameUtil.PLAYER2, GameUtil.PLAYER1);
        if (move == null) {
            game.setWinner("draw");
            game.setStatus("FINISHED");
            return gameRepository.save(game);
        }

        int position = move[0] * GameUtil.BOARD_SIZE + move[1];
        
        // 执行移动
        char[] board = boardState.toCharArray();
        board[position] = GameUtil.PLAYER2;
        String newBoardState = new String(board);
        game.setBoardState(newBoardState);

        // 检查获胜条件
        if (GameUtil.hasWinner(newBoardState, GameUtil.PLAYER2)) {
            game.setWinner("player2");
            game.setStatus("FINISHED");
            game.setEndTime(java.time.LocalDateTime.now());
        } else if (isDraw(newBoardState)) {
            game.setWinner("draw");
            game.setStatus("FINISHED");
            game.setEndTime(java.time.LocalDateTime.now());
        } else {
            game.setCurrentPlayer(1); // 切换回玩家1
        }

        Game savedGame = gameRepository.save(game);
        
        // 广播游戏状态更新
        gameService.broadcastGameUpdate(savedGame);
        
        return savedGame;
    }

    @Override
    public boolean shouldAiMove(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        return game.getType() == com.skillfive.backend.enums.GameType.VS_AI &&
               "IN_PROGRESS".equals(game.getStatus()) &&
               game.getCurrentPlayer() == 2;
    }

    @Override
    public void setDifficulty(int difficulty) {
        if (difficulty >= 1 && difficulty <= 3) {
            this.difficulty = difficulty;
        }
    }

    private boolean isDraw(String board) {
        return board.indexOf(GameUtil.EMPTY) == -1;
    }
}