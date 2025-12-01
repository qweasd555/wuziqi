package com.skillfive.backend.websocket;

import com.skillfive.backend.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket消息控制器
 */
@Controller
@Profile("stomp")
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private WebSocketSessionManager sessionManager;

    /**
     * 游戏消息处理
     * 客户端发送到 /app/game/message
     */
    @MessageMapping("/game/message")
    @SendTo("/topic/game/public")
    public ApiResponse<?> handleGameMessage(GameMessage message) {
        logger.info("收到游戏消息 - type: {}, userId: {}, gameId: {}", 
                message.getType(), message.getUserId(), message.getGameId());
        
        // 处理不同类型的游戏消息
        switch (message.getType()) {
            case "move":
                // 处理移动消息
                return handleMoveMessage(message);
            case "skill":
                // 处理技能消息
                return handleSkillMessage(message);
            case "chat":
                // 处理聊天消息
                return handleChatMessage(message);
            case "status":
                // 处理状态更新消息
                return handleStatusMessage(message);
            default:
                return new ApiResponse<>(200, "收到消息", message);
        }
    }

    /**
     * 处理移动消息
     */
    private ApiResponse<?> handleMoveMessage(GameMessage message) {
        // 广播移动消息给游戏内所有用户
        broadcastToGame(message.getGameId(), "move", message);
        return new ApiResponse<>(200, "移动成功", message);
    }

    /**
     * 处理技能消息
     */
    private ApiResponse<?> handleSkillMessage(GameMessage message) {
        // 广播技能消息给游戏内所有用户
        broadcastToGame(message.getGameId(), "skill", message);
        return new ApiResponse<>(200, "技能使用成功", message);
    }

    /**
     * 处理聊天消息
     */
    private ApiResponse<?> handleChatMessage(GameMessage message) {
        // 广播聊天消息给游戏内所有用户
        broadcastToGame(message.getGameId(), "chat", message);
        return new ApiResponse<>(200, "消息发送成功", message);
    }

    /**
     * 处理状态更新消息
     */
    private ApiResponse<?> handleStatusMessage(GameMessage message) {
        // 广播状态更新给游戏内所有用户
        broadcastToGame(message.getGameId(), "status", message);
        return new ApiResponse<>(200, "状态更新成功", message);
    }

    /**
     * 广播消息到指定游戏
     */
    private void broadcastToGame(String gameId, String eventType, Object data) {
        if (gameId != null) {
            Map<String, String> gameUsers = sessionManager.getGameUsersWithRoles(gameId);
            
            // 创建广播消息
            Map<String, Object> broadcastMessage = new ConcurrentHashMap<>();
            broadcastMessage.put("event", eventType);
            broadcastMessage.put("gameId", gameId);
            broadcastMessage.put("data", data);
            broadcastMessage.put("timestamp", System.currentTimeMillis());
            
            // 发送给游戏内的所有用户
            gameUsers.forEach((userId, role) -> {
                try {
                    messagingTemplate.convertAndSendToUser(
                            userId, 
                            "/queue/game", 
                            broadcastMessage
                    );
                } catch (Exception e) {
                    logger.error("向用户 {} 发送消息失败", userId, e);
                }
            });
        }
    }

    /**
     * 发送私人消息给指定用户
     */
    public void sendPrivateMessage(String userId, String message) {
        if (userId != null) {
            try {
                Map<String, Object> privateMessage = new ConcurrentHashMap<>();
                privateMessage.put("event", "private_message");
                privateMessage.put("message", message);
                privateMessage.put("timestamp", System.currentTimeMillis());
                
                messagingTemplate.convertAndSendToUser(
                        userId, 
                        "/queue/notification", 
                        privateMessage
                );
                
                logger.info("私人消息已发送 - userId: {}", userId);
            } catch (Exception e) {
                logger.error("发送私人消息失败 - userId: {}", userId, e);
            }
        }
    }

    /**
     * 发送系统通知
     */
    public void sendSystemNotification(String content) {
        Map<String, Object> notification = new ConcurrentHashMap<>();
        notification.put("event", "system_notification");
        notification.put("content", content);
        notification.put("timestamp", System.currentTimeMillis());
        
        messagingTemplate.convertAndSend("/topic/notification", notification);
        logger.info("系统通知已发送: {}", content);
    }

    /**
     * 游戏消息数据类
     */
    public static class GameMessage {
        private String type; // move, skill, chat, status, etc.
        private String userId;
        private String gameId;
        private Map<String, Object> data;
        private Long timestamp;
        
        // Getters and Setters
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getGameId() {
            return gameId;
        }
        
        public void setGameId(String gameId) {
            this.gameId = gameId;
        }
        
        public Map<String, Object> getData() {
            return data;
        }
        
        public void setData(Map<String, Object> data) {
            this.data = data;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}