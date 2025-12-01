package com.skillfive.backend.controller;

import com.skillfive.backend.entity.User;
import com.skillfive.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public User login(@RequestParam String openId,
                      @RequestParam String nickname) {
        return userService.login(openId, nickname);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return ResponseEntity.ok(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updatedUser = userService.updateUser(user);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 获取用户游戏统计信息
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 使用HashMap来避免Map.of()的类型限制问题
        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", user.getId());
        stats.put("username", user.getNickname());
        stats.put("avatar", user.getAvatarUrl());
        stats.put("score", user.getScore());
        stats.put("wins", user.getWinCount());
        stats.put("totalGames", user.getTotalCount());
        stats.put("rankLevel", user.getRankLevel());
        stats.put("winRate", user.getWinRate());
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 更新用户积分
     */
    @PostMapping("/{id}/score")
    public ResponseEntity<User> updateScore(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer scoreChange = request.get("scoreChange");
        if (scoreChange == null) {
            throw new RuntimeException("积分变化值不能为空");
        }
        User updatedUser = userService.updateScore(id, scoreChange);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * 获取用户排名
     */
    @GetMapping("/ranking")
    public ResponseEntity<Object> getRanking(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "20") Integer size) {
        // 这里可以实现排行榜逻辑
        return ResponseEntity.ok("排行榜功能待实现");
    }
}
