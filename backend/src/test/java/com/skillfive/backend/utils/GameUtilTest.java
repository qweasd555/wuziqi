package com.skillfive.backend.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * GameUtil测试类
 */
public class GameUtilTest {

    @Test
    public void testCreateEmptyBoard() {
        String board = GameUtil.createEmptyBoard();
        assertEquals(225, board.length()); // 15x15 = 225
        assertTrue(board.chars().allMatch(c -> c == GameUtil.EMPTY));
    }

    @Test
    public void testIsValidMove() {
        String board = GameUtil.createEmptyBoard();
        assertTrue(GameUtil.isValidMove(board, 7, 7));
        
        String boardWithMove = GameUtil.makeMove(board, 7, 7, GameUtil.PLAYER1);
        assertFalse(GameUtil.isValidMove(boardWithMove, 7, 7));
        assertTrue(GameUtil.isValidMove(boardWithMove, 7, 8));
    }

    @Test
    public void testHasWinnerHorizontal() {
        String board = GameUtil.createEmptyBoard();
        // 创建水平五连
        board = GameUtil.makeMove(board, 7, 7, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 7, 8, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 7, 9, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 7, 10, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 7, 11, GameUtil.PLAYER1);
        
        assertTrue(GameUtil.hasWinner(board, GameUtil.PLAYER1));
        assertEquals(GameUtil.PLAYER1, GameUtil.getWinner(board));
    }

    @Test
    public void testHasWinnerVertical() {
        String board = GameUtil.createEmptyBoard();
        // 创建垂直五连
        board = GameUtil.makeMove(board, 7, 7, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 8, 7, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 9, 7, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 10, 7, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 11, 7, GameUtil.PLAYER1);
        
        assertTrue(GameUtil.hasWinner(board, GameUtil.PLAYER1));
        assertEquals(GameUtil.PLAYER1, GameUtil.getWinner(board));
    }

    @Test
    public void testHasWinnerDiagonal() {
        String board = GameUtil.createEmptyBoard();
        // 创建对角线五连
        board = GameUtil.makeMove(board, 7, 7, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 8, 8, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 9, 9, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 10, 10, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 11, 11, GameUtil.PLAYER1);
        
        assertTrue(GameUtil.hasWinner(board, GameUtil.PLAYER1));
        assertEquals(GameUtil.PLAYER1, GameUtil.getWinner(board));
    }

    @Test
    public void testNoWinner() {
        String board = GameUtil.createEmptyBoard();
        // 创建四连（不是五连）
        board = GameUtil.makeMove(board, 7, 7, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 7, 8, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 7, 9, GameUtil.PLAYER1);
        board = GameUtil.makeMove(board, 7, 10, GameUtil.PLAYER1);
        
        assertFalse(GameUtil.hasWinner(board, GameUtil.PLAYER1));
        assertEquals(GameUtil.EMPTY, GameUtil.getWinner(board));
    }

    @Test
    public void testIsBoardFull() {
        String board = GameUtil.createEmptyBoard();
        assertFalse(GameUtil.isBoardFull(board));
        
        // 填满棋盘
        StringBuilder fullBoard = new StringBuilder();
        for (int i = 0; i < 225; i++) {
            fullBoard.append(GameUtil.PLAYER1);
        }
        assertTrue(GameUtil.isBoardFull(fullBoard.toString()));
    }

    @Test
    public void testGetBestMove() {
        String board = GameUtil.createEmptyBoard();
        int[] bestMove = GameUtil.getBestMove(board, GameUtil.PLAYER1, GameUtil.PLAYER2);
        
        assertNotNull(bestMove);
        assertEquals(2, bestMove.length);
        assertTrue(bestMove[0] >= 0 && bestMove[0] < 15);
        assertTrue(bestMove[1] >= 0 && bestMove[1] < 15);
    }

    @Test
    public void testPositionConversion() {
        int position = 120; // 第120个位置
        int expectedX = position / 15; // 8
        int expectedY = position % 15; // 0
        
        assertEquals(expectedX, 8);
        assertEquals(expectedY, 0);
    }
}