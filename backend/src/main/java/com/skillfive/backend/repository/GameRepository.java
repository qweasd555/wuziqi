package com.skillfive.backend.repository;

import com.skillfive.backend.entity.Game;
import com.skillfive.backend.entity.User;
import com.skillfive.backend.enums.GameMode;
import com.skillfive.backend.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 游戏Repository接口
 */
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * 根据状态查找游戏
     */
    List<Game> findByStatus(GameStatus status);

    /**
     * 查找用户参与的所有游戏
     */
    List<Game> findByPlayer1OrPlayer2(User player1, User player2);

    /**
     * 根据用户ID查找进行中的游戏
     */
    @Query("SELECT g FROM Game g WHERE (g.player1.id = :userId OR g.player2.id = :userId) AND g.status = 'IN_PROGRESS'")
    List<Game> findActiveGamesByUserId(@Param("userId") Long userId);

    /**
     * 查找等待中的游戏（玩家2为空）
     */
    Optional<Game> findFirstByStatusAndPlayer2IsNullOrderByStartTime(GameStatus status);

    /**
     * 根据状态和模式查找可加入的游戏（玩家2为空）
     */
    List<Game> findByStatusAndModeAndPlayer2IsNull(GameStatus status, GameMode mode);
}
