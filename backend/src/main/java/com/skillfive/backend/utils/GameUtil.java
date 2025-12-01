package com.skillfive.backend.utils;

import com.skillfive.backend.enums.GameMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏工具类
 * 提供五子棋游戏的核心逻辑
 */
public class GameUtil {
    
    // 棋盘大小
    public static final int BOARD_SIZE = 15;
    
    // 获胜条件（五子棋需要5个连续棋子）
    private static final int WIN_COUNT = 5;
    
    // 棋子符号
    public static final char PLAYER1 = 'X'; // 玩家1（黑棋）
    public static final char PLAYER2 = 'O'; // 玩家2（白棋）
    public static final char EMPTY = '-'; // 空位置
    
    private static final Random random = new Random();
    
    /**
     * 创建空的15x15棋盘
     */
    public static String createEmptyBoard() {
        StringBuilder board = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; i++) {
            board.append(EMPTY);
        }
        return board.toString();
    }
    
    /**
     * 获取随机可用位置
     */
    public static int[] getRandomAvailablePosition(String boardState) {
        List<int[]> availablePositions = getAvailablePositions(boardState);
        if (availablePositions.isEmpty()) {
            return null;
        }
        
        Random random = new Random();
        int index = random.nextInt(availablePositions.size());
        return availablePositions.get(index);
    }
    
    /**
     * 获取所有可用位置
     */
    private static List<int[]> getAvailablePositions(String boardState) {
        List<int[]> positions = new ArrayList<>();
        
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (isValidMove(boardState, i, j)) {
                    positions.add(new int[]{i, j});
                }
            }
        }
        
        return positions;
    }
    
    /**
     * 检查位置是否有效且为空
     */
    public static boolean isValidMove(String board, int x, int y) {
        if (x < 0 || x >= BOARD_SIZE || y < 0 || y >= BOARD_SIZE) {
            return false;
        }
        int position = x * BOARD_SIZE + y;
        return position >= 0 && position < board.length() && board.charAt(position) == EMPTY;
    }
    
    /**
     * 执行移动
     */
    public static String makeMove(String board, int x, int y, char symbol) {
        if (!isValidMove(board, x, y)) {
            throw new RuntimeException("无效的移动位置: (" + x + ", " + y + ")");
        }
        int position = x * BOARD_SIZE + y;
        char[] boardArray = board.toCharArray();
        boardArray[position] = symbol;
        return new String(boardArray);
    }
    
    /**
     * 检查是否有玩家获胜（五子棋规则）
     */
    public static boolean hasWinner(String board, char playerSymbol) {
        char[][] board2D = boardTo2DArray(board);
        
        // 检查所有方向：水平、垂直、对角线
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board2D[i][j] == playerSymbol) {
                    // 检查水平方向
                    if (checkDirection(board2D, i, j, 0, 1, playerSymbol)) {
                        return true;
                    }
                    // 检查垂直方向
                    if (checkDirection(board2D, i, j, 1, 0, playerSymbol)) {
                        return true;
                    }
                    // 检查对角线方向（左上到右下）
                    if (checkDirection(board2D, i, j, 1, 1, playerSymbol)) {
                        return true;
                    }
                    // 检查对角线方向（右上到左下）
                    if (checkDirection(board2D, i, j, 1, -1, playerSymbol)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * 检查指定方向是否有5子连线
     */
    private static boolean checkDirection(char[][] board, int startX, int startY, 
                                        int deltaX, int deltaY, char playerSymbol) {
        int count = 1;
        
        // 向正方向检查
        int x = startX + deltaX;
        int y = startY + deltaY;
        while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && 
               board[x][y] == playerSymbol && count < WIN_COUNT) {
            count++;
            x += deltaX;
            y += deltaY;
        }
        
        // 向负方向检查
        x = startX - deltaX;
        y = startY - deltaY;
        while (x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE && 
               board[x][y] == playerSymbol && count < WIN_COUNT) {
            count++;
            x -= deltaX;
            y -= deltaY;
        }
        
        return count >= WIN_COUNT;
    }
    
    /**
     * 获取获胜的玩家符号，如果没有获胜者返回'-'
     */
    public static char getWinner(String board) {
        if (hasWinner(board, PLAYER1)) {
            return PLAYER1;
        }
        if (hasWinner(board, PLAYER2)) {
            return PLAYER2;
        }
        return EMPTY;
    }
    
    /**
     * 检查棋盘是否已满
     */
    public static boolean isBoardFull(String board) {
        return !board.contains(String.valueOf(EMPTY));
    }
    
    /**
     * 随机移除棋盘上的一个棋子（用于技能效果）
     */
    public static String removeRandomPiece(String board) {
        List<Integer> piecePositions = new ArrayList<>();
        
        // 找到所有有棋子的位置
        for (int i = 0; i < board.length(); i++) {
            if (board.charAt(i) == PLAYER1 || board.charAt(i) == PLAYER2) {
                piecePositions.add(i);
            }
        }
        
        // 如果没有棋子，返回原棋盘
        if (piecePositions.isEmpty()) {
            return board;
        }
        
        // 随机选择一个位置移除棋子
        int randomIndex = piecePositions.get(random.nextInt(piecePositions.size()));
        char[] boardArray = board.toCharArray();
        boardArray[randomIndex] = EMPTY;
        
        return new String(boardArray);
    }
    
    /**
     * 评估棋盘得分（用于AI决策）
     * 考虑连子数量、阻挡对手、控制中心等因素
     */
    public static int evaluateBoard(String board, char aiSymbol, char humanSymbol) {
        char[][] board2D = boardTo2DArray(board);
        int score = 0;
        
        // 评估每个位置
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board2D[i][j] == aiSymbol) {
                    score += evaluatePosition(board2D, i, j, aiSymbol, humanSymbol);
                } else if (board2D[i][j] == humanSymbol) {
                    score -= evaluatePosition(board2D, i, j, humanSymbol, aiSymbol);
                }
            }
        }
        
        return score;
    }
    
    /**
     * 评估单个位置的得分
     */
    private static int evaluatePosition(char[][] board, int x, int y, char player, char opponent) {
        int score = 0;
        
        // 中心位置加分
        if (x >= 5 && x < 10 && y >= 5 && y < 10) {
            score += 10;
        }
        
        // 检查四个方向
        int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};
        
        for (int[] dir : directions) {
            int count = 1;
            int block = 0;
            
            // 正方向
            int nx = x + dir[0];
            int ny = y + dir[1];
            while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE) {
                if (board[nx][ny] == player) {
                    count++;
                } else if (board[nx][ny] == opponent) {
                    block++;
                    break;
                } else {
                    break;
                }
                nx += dir[0];
                ny += dir[1];
            }
            
            // 负方向
            nx = x - dir[0];
            ny = y - dir[1];
            while (nx >= 0 && nx < BOARD_SIZE && ny >= 0 && ny < BOARD_SIZE) {
                if (board[nx][ny] == player) {
                    count++;
                } else if (board[nx][ny] == opponent) {
                    block++;
                    break;
                } else {
                    break;
                }
                nx -= dir[0];
                ny -= dir[1];
            }
            
            // 根据连子数量和阻挡情况评分
            if (count >= 5) {
                score += 10000; // 获胜
            } else if (count == 4 && block == 0) {
                score += 1000; // 活四
            } else if (count == 4 && block == 1) {
                score += 100; // 冲四
            } else if (count == 3 && block == 0) {
                score += 50; // 活三
            } else if (count == 3 && block == 1) {
                score += 10; // 眠三
            } else if (count == 2 && block == 0) {
                score += 5; // 活二
            } else if (count == 2 && block == 1) {
                score += 2; // 眠二
            }
        }
        
        return score;
    }
    
    /**
     * 获取AI的最佳移动（简化版Minimax算法）
     */
    public static int[] getBestMove(String board, char aiSymbol, char humanSymbol) {
        List<int[]> availablePositions = getAvailablePositions(board);
        if (availablePositions.isEmpty()) {
            return null;
        }
        
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = availablePositions.get(0);
        
        // 遍历所有可能的移动
        for (int[] move : availablePositions) {
            String newBoard = makeMove(board, move[0], move[1], aiSymbol);
            int score = evaluateBoard(newBoard, aiSymbol, humanSymbol);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
    
    /**
     * 将一维棋盘转换为二维数组
     */
    public static char[][] boardTo2DArray(String board) {
        char[][] board2D = new char[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board2D[i][j] = board.charAt(i * BOARD_SIZE + j);
            }
        }
        return board2D;
    }
    
    /**
     * 将二维数组转换为一维棋盘字符串
     */
    public static String boardToString(char[][] board2D) {
        StringBuilder board = new StringBuilder();
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board.append(board2D[i][j]);
            }
        }
        return board.toString();
    }
    
    /**
     * 将位置转换为坐标
     */
    public static int[] positionToCoordinate(int position) {
        return new int[]{position / BOARD_SIZE, position % BOARD_SIZE};
    }
    
    /**
     * 将坐标转换为位置
     */
    public static int coordinateToPosition(int x, int y) {
        return x * BOARD_SIZE + y;
    }
    
    /**
     * 检查游戏是否结束
     */
    public static boolean isGameOver(String board) {
        return hasWinner(board, PLAYER1) || hasWinner(board, PLAYER2) || isBoardFull(board);
    }
    
    /**
     * 获取游戏状态
     */
    public static String getGameStatus(String board) {
        if (hasWinner(board, PLAYER1)) {
            return "PLAYER1_WINS";
        } else if (hasWinner(board, PLAYER2)) {
            return "PLAYER2_WINS";
        } else if (isBoardFull(board)) {
            return "DRAW";
        } else {
            return "IN_PROGRESS";
        }
    }
}