package com.skillfive.backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * WebSocket会话管理器
 */
@Component
public class WebSocketSessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionManager.class);
    
    // 用户ID到WebSocket会话的映射
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // 游戏ID到用户ID列表的映射
    private final Map<String, List<String>> gameUsers = new ConcurrentHashMap<>();
    
    // 游戏ID到用户ID和角色的映射
    private final Map<String, Map<String, String>> gameUserRoles = new ConcurrentHashMap<>();
    
    /**
     * 添加用户会话
     */
    public void addUserSession(String userId, WebSocketSession session) {
        userSessions.put(userId, session);
        logger.info("用户 {} WebSocket会话已添加", userId);
    }
    
    /**
     * 移除用户会话
     */
    public void removeUserSession(String userId) {
        // 添加空值检查，防止NullPointerException
        if (userId == null) {
            logger.warn("尝试移除null用户的WebSocket会话，忽略此操作");
            return;
        }
        
        WebSocketSession session = userSessions.remove(userId);
        if (session != null) {
            logger.info("用户 {} WebSocket会话已移除", userId);
            
            // 从所有游戏中移除该用户
            gameUsers.forEach((gameId, users) -> {
                users.remove(userId);
                if (users.isEmpty()) {
                    gameUsers.remove(gameId);
                }
            });
            
            gameUserRoles.forEach((gameId, userRoles) -> {
                userRoles.remove(userId);
                if (userRoles.isEmpty()) {
                    gameUserRoles.remove(gameId);
                }
            });
        }
    }
    
    /**
     * 用户加入游戏
     */
    public void joinGame(String userId, String gameId) {
        gameUsers.computeIfAbsent(gameId, k -> new ArrayList<>()).add(userId);
        gameUserRoles.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>()).put(userId, "player");
        logger.info("用户 {} 已加入游戏 {}", userId, gameId);
    }
    
    /**
     * 用户加入游戏（带角色）
     */
    public void addUserToGame(String gameId, String userId, String role) {
        // 检查用户是否已经在游戏中
        if (!isUserInGame(userId, gameId)) {
            gameUsers.computeIfAbsent(gameId, k -> new ArrayList<>()).add(userId);
            gameUserRoles.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>()).put(userId, role);
            logger.info("用户 {} 已加入游戏 {}，角色: {}", userId, gameId, role);
        } else {
            logger.info("用户 {} 已经在游戏 {} 中，角色: {}", userId, gameId, role);
        }
    }
    
    /**
     * 用户离开游戏
     */
    public void leaveGame(String userId, String gameId) {
        List<String> users = gameUsers.get(gameId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                gameUsers.remove(gameId);
            }
        }
        
        Map<String, String> userRoles = gameUserRoles.get(gameId);
        if (userRoles != null) {
            userRoles.remove(userId);
            if (userRoles.isEmpty()) {
                gameUserRoles.remove(gameId);
            }
        }
        
        logger.info("用户 {} 已离开游戏 {}", userId, gameId);
    }
    
    /**
     * 获取游戏中的所有用户
     */
    public List<String> getGameUsers(String gameId) {
        return gameUsers.getOrDefault(gameId, new ArrayList<>());
    }
    
    /**
     * 获取游戏中的用户ID和角色映射
     */
    public Map<String, String> getGameUsersWithRoles(String gameId) {
        return gameUserRoles.getOrDefault(gameId, new ConcurrentHashMap<>());
    }
    
    /**
     * 发送消息给指定用户
     */
    public boolean sendMessageToUser(String userId, String message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                logger.debug("消息已发送给用户 {}: {}", userId, message);
                return true;
            } catch (IOException e) {
                logger.error("发送消息给用户 {} 失败", userId, e);
                userSessions.remove(userId);
                return false;
            }
        }
        return false;
    }
    
    /**
     * 发送消息给游戏中的所有用户
     */
    public void sendMessageToGame(String gameId, String message) {
        List<String> users = getGameUsers(gameId);
        users.forEach(userId -> sendMessageToUser(userId, message));
        logger.debug("消息已发送给游戏 {} 中的所有用户", gameId);
    }
    
    /**
     * 发送消息给游戏中的其他用户（排除指定用户）
     */
    public void sendMessageToGameOthers(String gameId, String excludeUserId, String message) {
        List<String> users = getGameUsers(gameId);
        users.stream()
            .filter(userId -> !userId.equals(excludeUserId))
            .forEach(userId -> sendMessageToUser(userId, message));
        logger.debug("消息已发送给游戏 {} 中的其他用户", gameId);
    }
    
    /**
     * 广播消息给所有在线用户
     */
    public void broadcastMessage(String message) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.error("广播消息给用户 {} 失败", userId, e);
                }
            }
        });
        logger.debug("消息已广播给所有在线用户");
    }
    
    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }
    
    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return (int) userSessions.values().stream()
            .filter(WebSocketSession::isOpen)
            .count();
    }
    
    /**
     * 获取所有在线用户ID
     */
    public List<String> getOnlineUserIds() {
        return userSessions.entrySet().stream()
            .filter(entry -> entry.getValue().isOpen())
            .map(Map.Entry::getKey)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * 检查用户是否在指定游戏中
     */
    public boolean isUserInGame(String userId, String gameId) {
        List<String> users = gameUsers.get(gameId);
        return users != null && users.contains(userId);
    }
}