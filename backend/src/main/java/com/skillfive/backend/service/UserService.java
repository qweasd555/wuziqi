package com.skillfive.backend.service;

import com.skillfive.backend.entity.User;
import java.util.Optional;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     */
    User login(String openId, String nickname);

    /**
     * 根据ID查找用户
     */
    Optional<User> findById(Long id);

    /**
     * 根据openId查找用户
     */
    User findByOpenId(String openId);

    /**
     * 更新用户信息
     */
    User updateUser(User user);

    /**
     * 更新用户积分
     */
    User updateScore(Long userId, Integer score);

    /**
     * 记录游戏结果
     */
    void recordGameResult(Long userId, boolean isWin);
}
