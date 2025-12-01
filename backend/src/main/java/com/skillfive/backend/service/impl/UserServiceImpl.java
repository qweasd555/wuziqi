package com.skillfive.backend.service.impl;

import com.skillfive.backend.entity.User;
import com.skillfive.backend.repository.UserRepository;
import com.skillfive.backend.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User login(String openId, String nickname) {
        User user = userRepository.findByOpenId(openId);
        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname(nickname);
            user.setScore(1000);
            userRepository.save(user);
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User findByOpenId(String openId) {
        return userRepository.findByOpenId(openId);
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User updateScore(Long userId, Integer score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setScore(user.getScore() + score);
        return userRepository.save(user);
    }

    @Override
    public void recordGameResult(Long userId, boolean isWin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setTotalCount(user.getTotalCount() + 1);
        if (isWin) {
            user.setWinCount(user.getWinCount() + 1);
        }
        
        userRepository.save(user);
    }
}