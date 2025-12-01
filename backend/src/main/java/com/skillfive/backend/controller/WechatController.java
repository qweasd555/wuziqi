package com.skillfive.backend.controller;

import com.skillfive.backend.entity.User;
import com.skillfive.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 微信登录控制器
 */
@RestController
@RequestMapping("/api/wechat")
public class WechatController {

    private final UserService userService;

    public WechatController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public ResponseEntity<User> wechatLogin(@RequestBody Map<String, String> request) {
        String openId = request.get("openId");
        String nickname = request.get("nickname");
        String avatarUrl = request.get("avatarUrl");
        
        if (openId == null || openId.isEmpty()) {
            throw new RuntimeException("微信openId不能为空");
        }
        
        // 调用用户服务进行登录
        User user = userService.login(openId, nickname);
        
        // 如果提供了头像URL，更新用户头像
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
            userService.updateUser(user);
        }
        
        return ResponseEntity.ok(user);
    }

    /**
     * 微信小程序登录（获取openId）
     */
    @PostMapping("/miniapp/login")
    public ResponseEntity<Map<String, Object>> miniAppLogin(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        if (code == null || code.isEmpty()) {
            throw new RuntimeException("登录code不能为空");
        }
        
        // 这里应该调用微信官方接口获取openId
        // 暂时返回模拟数据
        Map<String, Object> result = Map.of(
                "openId", "mock_openid_" + System.currentTimeMillis(),
                "sessionKey", "mock_session_key",
                "expiresIn", 7200
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户会话信息
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, String>> getSessionInfo(@RequestHeader("Authorization") String token) {
        // 这里可以实现基于token的会话验证
        // 暂时返回模拟数据
        return ResponseEntity.ok(Map.of(
                "valid", "true",
                "message", "会话有效"
        ));
    }

    /**
     * 刷新登录状态
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        // 这里可以实现token刷新逻辑
        // 暂时返回模拟数据
        return ResponseEntity.ok(Map.of(
                "accessToken", "new_mock_token_" + System.currentTimeMillis(),
                "refreshToken", "new_mock_refresh_token"
        ));
    }
}