package com.skillfive.backend.repository;

import com.skillfive.backend.entity.GameRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 游戏记录Repository接口
 */
public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    /**
     * 根据游戏ID查找游戏记录
     */
    GameRecord findByGameId(Long gameId);

    /**
     * 查找用户参与的所有游戏记录
     */
    @Query("SELECT gr FROM GameRecord gr WHERE gr.player1Id = :userId OR gr.player2Id = :userId ORDER BY gr.endTime DESC")
    List<GameRecord> findByUserIdOrderByEndTimeDesc(@Param("userId") Long userId);

    /**
     * 查找用户的获胜记录
     */
    @Query("SELECT gr FROM GameRecord gr WHERE gr.winnerId = :userId ORDER BY gr.endTime DESC")
    List<GameRecord> findWinsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的游戏总数
     */
    @Query("SELECT COUNT(gr) FROM GameRecord gr WHERE gr.player1Id = :userId OR gr.player2Id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户的获胜次数
     */
    @Query("SELECT COUNT(gr) FROM GameRecord gr WHERE gr.winnerId = :userId")
    Long countWinsByUserId(@Param("userId") Long userId);
}