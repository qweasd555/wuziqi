package com.skillfive.backend.service.impl;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.enums.GameStatus;
import com.skillfive.backend.repository.GameRepository;
import com.skillfive.backend.service.AiService;
import com.skillfive.backend.service.GameService;
import com.skillfive.backend.utils.GameUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * AI服务实现类
 */
@Service
public class AiServiceImpl implements AiService {

    private final GameRepository gameRepository;
    private final GameService gameService;
    private int difficulty = 2; // 默认中等难度
    private final Random random = new Random();

    public AiServiceImpl(GameRepository gameRepository, GameService gameService) {
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
            case 1: // 简单难度 - 随机下棋
                int pos = availablePositions[random.nextInt(availablePositions.length)];
                return new int[]{pos / GameUtil.BOARD_SIZE, pos % GameUtil.BOARD_SIZE};
            case 2: // 中等难度 - 基础攻防
                return getMediumMove(board, availablePositions, aiSymbol, humanSymbol);
            case 3: // 困难难度 - 进攻防策略
                return getHardMove(board, availablePositions, aiSymbol, humanSymbol);
            default:
                int randomPos = availablePositions[random.nextInt(availablePositions.length)];
                return new int[]{randomPos / GameUtil.BOARD_SIZE, randomPos % GameUtil.BOARD_SIZE};
        }
    }

    private int[] getMediumMove(String board, int[] availablePositions, char aiSymbol, char humanSymbol) {
        // 1. 检查是否能直接获胜
        for (int pos : availablePositions) {
            String testBoard = placeStone(board, pos, aiSymbol);
            if (checkWin(testBoard, aiSymbol)) {
                return new int[]{pos / GameUtil.BOARD_SIZE, pos % GameUtil.BOARD_SIZE};
            }
        }

        // 2. 检查是否需要阻止对手获胜
        for (int pos : availablePositions) {
            String testBoard = placeStone(board, pos, humanSymbol);
            if (checkWin(testBoard, humanSymbol)) {
                return new int[]{pos / GameUtil.BOARD_SIZE, pos % GameUtil.BOARD_SIZE};
            }
        }

        // 3. 选择中心位置或附近位置
        int centerPos = (GameUtil.BOARD_SIZE / 2) * GameUtil.BOARD_SIZE + (GameUtil.BOARD_SIZE / 2);
        if (board.charAt(centerPos) == GameUtil.EMPTY) {
            return new int[]{centerPos / GameUtil.BOARD_SIZE, centerPos % GameUtil.BOARD_SIZE};
        }

        // 4. 随机选择
        int randomPos = availablePositions[random.nextInt(availablePositions.length)];
        return new int[]{randomPos / GameUtil.BOARD_SIZE, randomPos % GameUtil.BOARD_SIZE};
    }

    private int[] getHardMove(String board, int[] availablePositions, char aiSymbol, char humanSymbol) {
        // 使用评估函数选择最佳位置
        int bestScore = Integer.MIN_VALUE;
        int bestPos = -1;

        for (int pos : availablePositions) {
            int score = evaluatePosition(board, pos, aiSymbol, humanSymbol);
            if (score > bestScore) {
                bestScore = score;
                bestPos = pos;
            }
        }

        return bestPos != -1 ? 
            new int[]{bestPos / GameUtil.BOARD_SIZE, bestPos % GameUtil.BOARD_SIZE} :
            new int[]{availablePositions[random.nextInt(availablePositions.length)] / GameUtil.BOARD_SIZE, availablePositions[random.nextInt(availablePositions.length)] % GameUtil.BOARD_SIZE};
    }

    private int evaluatePosition(String board, int pos, char aiSymbol, char humanSymbol) {
        int score = 0;

        // 评估AI在该位置的收益
        String aiBoard = placeStone(board, pos, aiSymbol);
        if (checkWin(aiBoard, aiSymbol)) {
            return 1000; // 直接获胜
        }

        // 评估阻止对手的收益
        String humanBoard = placeStone(board, pos, humanSymbol);
        if (checkWin(humanBoard, humanSymbol)) {
            return 900; // 阻止对手获胜
        }

        // 评估棋型
        score += evaluatePatterns(aiBoard, pos, aiSymbol) * 2;
        score += evaluatePatterns(humanBoard, pos, humanSymbol);

        // 中心位置加分
        int row = pos / GameUtil.BOARD_SIZE;
        int col = pos % GameUtil.BOARD_SIZE;
        int centerDistance = Math.abs(row - GameUtil.BOARD_SIZE / 2) + Math.abs(col - GameUtil.BOARD_SIZE / 2);
        score += (GameUtil.BOARD_SIZE - centerDistance);

        return score;
    }

    private int evaluatePatterns(String board, int pos, char symbol) {
        int score = 0;
        int row = pos / GameUtil.BOARD_SIZE;
        int col = pos % GameUtil.BOARD_SIZE;

        // 检查四个方向
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1; // 当前位置已经有一个棋子

            // 正方向
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

            // 反方向
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

    private boolean checkWin(String board, char symbol) {
        // 简化的获胜检查
        for (int i = 0; i < GameUtil.BOARD_SIZE; i++) {
            for (int j = 0; j < GameUtil.BOARD_SIZE; j++) {
                int pos = i * GameUtil.BOARD_SIZE + j;
                if (board.charAt(pos) == symbol) {
                    if (checkWinFromPosition(board, i, j, symbol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkWinFromPosition(String board, int row, int col, char symbol) {
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int[] dir : directions) {
            int count = 1;

            // 正方向
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

            // 反方向
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

            if (count >= 5) {
                return true;
            }
        }

        return false;
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

        if (!GameStatus.IN_PROGRESS.equals(game.getStatus()) || game.getCurrentPlayer() != 2) {
            return game;
        }

        String boardState = game.getBoardState();
        if (boardState == null || boardState.isEmpty()) {
            boardState = GameUtil.createEmptyBoard();
            game.setBoardState(boardState);
        }

        // 获取AI的最佳移动
        int[] move = getBestMove(boardState, GameUtil.PLAYER2, GameUtil.PLAYER1);
        if (move == null) {
            game.setWinner("draw");
            game.setStatus(GameStatus.FINISHED);
            Game savedGame = gameRepository.save(game);
            // 广播游戏结束
            gameService.broadcastGameUpdate(savedGame);
            return savedGame;
        }

        int position = move[0] * GameUtil.BOARD_SIZE + move[1];
        
        // 执行移动
        char[] board = boardState.toCharArray();
        board[position] = GameUtil.PLAYER2;
        game.setBoardState(new String(board));

        // 检查获胜
        if (checkWin(new String(board), GameUtil.PLAYER2)) {
            game.setWinner("player2");
            game.setStatus(GameStatus.FINISHED);
            game.setEndTime(java.time.LocalDateTime.now());
        } else if (checkDraw(new String(board))) {
            game.setWinner("draw");
            game.setStatus(GameStatus.FINISHED);
            game.setEndTime(java.time.LocalDateTime.now());
        } else {
            game.setCurrentPlayer(1);
        }

        Game savedGame = gameRepository.save(game);
        // 广播游戏状态更新
        gameService.broadcastGameUpdate(savedGame);
        return savedGame;
    }

    private boolean checkDraw(String board) {
        return board.indexOf(GameUtil.EMPTY) == -1;
    }

    @Override
    public boolean shouldAiMove(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("游戏不存在"));

        return game.getType() == com.skillfive.backend.enums.GameType.VS_AI &&
               GameStatus.IN_PROGRESS.equals(game.getStatus()) &&
               game.getCurrentPlayer() == 2;
    }

    @Override
    public void setDifficulty(int difficulty) {
        if (difficulty >= 1 && difficulty <= 3) {
            this.difficulty = difficulty;
        }
    }
}