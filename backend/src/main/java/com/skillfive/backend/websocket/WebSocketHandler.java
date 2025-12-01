package com.skillfive.backend.websocket;

import com.skillfive.backend.utils.JsonUtil;
import com.skillfive.backend.dto.SkillEffectResponse;
import com.skillfive.backend.dto.SkillUseRequest;
import com.skillfive.backend.controller.SkillController;
import com.skillfive.backend.service.GameService;
import com.skillfive.backend.entity.User;
import com.skillfive.backend.entity.Game;
import com.skillfive.backend.enums.GameType;
import com.skillfive.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket连接处理器
 */
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    
    @Autowired
    private SkillController skillController;
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WebSocketSessionManager webSocketSessionManager;
    
    // 存储会话ID与用户ID的映射
    private static final Map<String, String> SESSION_USER_MAPPING = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("WebSocket连接已建立 - sessionId: {}", session.getId());
        
        // 从URL参数或其他方式获取用户ID
        String userId = getUserIdFromSession(session);
        
        if (userId != null) {
            // 存储会话信息
            webSocketSessionManager.addUserSession(userId, session);
            SESSION_USER_MAPPING.put(session.getId(), userId);
            
            // 发送连接成功消息
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", "connection_established");
            message.put("message", "连接成功");
            message.put("userId", userId);
            message.put("timestamp", System.currentTimeMillis());
            
            try {
                session.sendMessage(new TextMessage(JsonUtil.toJson(message)));
            } catch (Exception e) {
                logger.error("序列化消息失败", e);
            }
            
            logger.info("用户已连接 - userId: {}, sessionId: {}", userId, session.getId());
        } else {
            // 如果没有用户ID，关闭连接
            logger.warn("无法获取用户ID，关闭连接 - sessionId: {}", session.getId());
            session.close(CloseStatus.BAD_DATA.withReason("Missing userId"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String userId = SESSION_USER_MAPPING.get(session.getId());
        String payload = message.getPayload();
        
        logger.info("收到消息 - userId: {}, payload: {}", userId, payload);
        
        // 检查userId是否为null
        if (userId == null) {
            logger.warn("收到消息但userId为null，关闭连接 - sessionId: {}", session.getId());
            sendErrorMessage(session, "用户未认证");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("用户未认证"));
            return;
        }
        
        try {
            // 解析消息
            Map<String, Object> msgData = JsonUtil.fromJson(payload, Map.class);
            String type = (String) msgData.get("type");
            
            // 处理不同类型的消息
            switch (type) {
                case "ping":
                    // 响应心跳消息
                    handlePingMessage(session);
                    break;
                case "join_game":
                    // 加入游戏
                    handleJoinGame(userId, msgData);
                    break;
                case "leave_game":
                    // 离开游戏
                    handleLeaveGame(userId, msgData);
                    break;
                case "move":
                    // 移动操作
                    handleMove(userId, msgData);
                    break;
                case "skill":
                    // 技能操作
                    handleSkill(userId, msgData);
                    break;
                case "chat":
                    // 聊天消息
                    handleChat(userId, msgData);
                    break;
                default:
                    logger.warn("未知消息类型 - type: {}", type);
                    sendErrorMessage(session, "未知消息类型");
            }
        } catch (Exception e) {
            logger.error("处理消息失败 - userId: {}, 错误详情: {}", userId, e.getMessage(), e);
            sendErrorMessage(session, "处理消息失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        String userId = SESSION_USER_MAPPING.remove(sessionId);
        
        logger.info("WebSocket连接已关闭 - sessionId: {}, userId: {}, status: {}", 
                sessionId, userId, status);
        
        // 移除用户会话，添加null检查
        if (userId != null) {
            webSocketSessionManager.removeUserSession(userId);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        String userId = SESSION_USER_MAPPING.get(sessionId);
        
        logger.error("WebSocket传输错误 - sessionId: {}, userId: {}", sessionId, userId, exception);
        
        // 如果会话仍然打开，可以发送错误消息
        if (session.isOpen()) {
            sendErrorMessage(session, "传输错误: " + exception.getMessage());
        }
    }

    /**
     * 从会话中获取用户ID
     */
    private String getUserIdFromSession(WebSocketSession session) {
        // 从URL参数获取
        String query = session.getUri().getQuery();
        if (query != null && query.contains("userId=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("userId=")) {
                    return param.substring(7);
                }
            }
        }
        
        // 从会话属性获取（如果在拦截器中设置）
        Object userIdObj = session.getAttributes().get("userId");
        if (userIdObj != null) {
            return userIdObj.toString();
        }
        
        return null;
    }

    /**
     * 处理心跳消息
     */
    private void handlePingMessage(WebSocketSession session) throws IOException {
        Map<String, Object> pongMessage = new ConcurrentHashMap<>();
        pongMessage.put("type", "pong");
        pongMessage.put("timestamp", System.currentTimeMillis());
        
        session.sendMessage(new TextMessage(JsonUtil.toJson(pongMessage)));
    }

    /**
     * 处理加入游戏
     */
    private void handleJoinGame(String userId, Map<String, Object> data) {
        String gameId = (String) data.get("gameId");
        String role = (String) dataOrDefault(data, "role", "player");
        
        if (gameId != null) {
            webSocketSessionManager.addUserToGame(gameId, userId, role);
            logger.info("用户加入游戏 - userId: {}, gameId: {}, role: {}", userId, gameId, role);
            
            // 构建用户加入游戏的消息
            Map<String, Object> joinData = new ConcurrentHashMap<>();
            joinData.put("userId", userId);
            joinData.put("role", role);
            
            // 获取用户详细信息
            try {
                Long userLongId = Long.parseLong(userId);
                Optional<User> userOpt = userRepository.findById(userLongId);
                
                // 如果找到用户信息，添加到消息中
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    joinData.put("nickname", user.getNickname());
                    joinData.put("avatarUrl", user.getAvatarUrl());
                }
            } catch (Exception e) {
                logger.error("获取用户信息失败 - userId: {}", userId, e);
            }
            
            // 无论如何都发送用户加入游戏的消息
            broadcastToGame(gameId, "user_joined", joinData);
            
            // 获取游戏信息进行后续处理
            try {
                Long gameLongId = Long.parseLong(gameId);
                Optional<Game> gameOpt = gameService.findById(gameLongId);
                
                if (gameOpt.isPresent()) {
                    Game game = gameOpt.get();
                    
                    // 如果游戏是PENDING状态且只有一个玩家，尝试加入游戏
                    if ("PENDING".equals(game.getStatus()) && game.getPlayer2() == null) {
                        try {
                            // 调用joinGame方法加入游戏
                            gameService.joinGame(gameLongId, Long.parseLong(userId));
                            // joinGame方法已经调用了broadcastGameUpdate，所以不需要在这里再次广播
                        } catch (Exception e) {
                            logger.error("加入游戏失败 - userId: {}, gameId: {}", userId, gameId, e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("获取游戏信息失败 - gameId: {}", gameId, e);
            }
        }
    }
    
    /**
     * 获取Map中的值，如果不存在则返回默认值
     */
    private Object dataOrDefault(Map<String, Object> data, String key, Object defaultValue) {
        return data.containsKey(key) ? data.get(key) : defaultValue;
    }

    /**
     * 处理离开游戏
     */
    private void handleLeaveGame(String userId, Map<String, Object> data) {
        String gameId = (String) data.get("gameId");
        
        if (gameId != null) {
            webSocketSessionManager.leaveGame(userId, gameId);
            logger.info("用户离开游戏 - userId: {}, gameId: {}", userId, gameId);
            
            // 广播用户离开消息
            Map<String, Object> leaveData = new ConcurrentHashMap<>();
            leaveData.put("userId", userId);
            broadcastToGame(gameId, "user_left", leaveData);
        }
    }

    /**
     * 处理移动操作
     */
    private void handleMove(String userId, Map<String, Object> data) {
        try {
            Object gameIdObj = data.get("gameId");
            Long gameId;
            if (gameIdObj instanceof String) {
                gameId = Long.parseLong((String) gameIdObj);
            } else if (gameIdObj instanceof Number) {
                gameId = ((Number) gameIdObj).longValue();
            } else {
                logger.warn("无效的游戏ID类型 - userId: {}, gameId: {}", userId, gameIdObj);
                return;
            }
            
            Integer position = (Integer) data.get("position");
            
            if (gameId == null || position == null) {
                logger.warn("游戏ID和位置不能为空 - userId: {}", userId);
                return;
            }
            
            // 验证用户是否在游戏中
            if (!webSocketSessionManager.getGameUsers(gameId.toString()).contains(userId)) {
                logger.warn("用户不在此游戏中 - userId: {}, gameId: {}", userId, gameId);
                return;
            }
            
            // 调用游戏服务执行移动
            com.skillfive.backend.entity.Game game = gameService.makeMove(
                gameId, Long.parseLong(userId), position);
            
            // 广播游戏状态更新
            Map<String, Object> gameData = new ConcurrentHashMap<>();
            gameData.put("gameId", game.getId());
            gameData.put("boardState", game.getBoardState());
            gameData.put("currentPlayer", game.getCurrentPlayer());
            gameData.put("currentPlayerId", game.getCurrentPlayerId());
            gameData.put("status", game.getStatus());
            gameData.put("winner", game.getWinner());
            gameData.put("moveBy", userId);
            gameData.put("position", position);
            
            // 添加玩家信息
            if (game.getPlayer1() != null) {
                gameData.put("player1Id", game.getPlayer1().getId());
                gameData.put("player1Nickname", game.getPlayer1().getNickname());
                gameData.put("player1AvatarUrl", game.getPlayer1().getAvatarUrl());
            }
            
            if (game.getPlayer2() != null) {
                gameData.put("player2Id", game.getPlayer2().getId());
                gameData.put("player2Nickname", game.getPlayer2().getNickname());
                gameData.put("player2AvatarUrl", game.getPlayer2().getAvatarUrl());
            } else if (game.getType() == GameType.VS_AI) {
                gameData.put("player2Id", null);
                gameData.put("player2Nickname", "AI");
                gameData.put("player2AvatarUrl", "https://via.placeholder.com/40");
            }
            
            broadcastToGame(gameId.toString(), "game_update", gameData);
            
        } catch (Exception e) {
            logger.error("处理移动操作失败 - userId: {}", userId, e);
        }
    }

    /**
     * 处理技能使用
     */
    private void handleSkill(String userId, Map<String, Object> data) {
        try {
            // 处理gameId类型转换
            Object gameIdObj = data.get("gameId");
            Long gameId;
            if (gameIdObj instanceof String) {
                gameId = Long.parseLong((String) gameIdObj);
            } else if (gameIdObj instanceof Number) {
                gameId = ((Number) gameIdObj).longValue();
            } else {
                logger.warn("无效的游戏ID类型 - userId: {}, gameId: {}", userId, gameIdObj);
                return;
            }
            
            // 处理skillId类型转换
            Object skillIdObj = data.get("skillId");
            Long skillId;
            if (skillIdObj instanceof String) {
                skillId = Long.parseLong((String) skillIdObj);
            } else if (skillIdObj instanceof Number) {
                skillId = ((Number) skillIdObj).longValue();
            } else {
                logger.warn("无效的技能ID类型 - userId: {}, skillId: {}", userId, skillIdObj);
                return;
            }
            
            if (gameId == null || skillId == null) {
                logger.warn("游戏ID和技能ID不能为空 - userId: {}", userId);
                return;
            }
            
            // 验证用户是否在游戏中
            if (!webSocketSessionManager.getGameUsers(gameId.toString()).contains(userId)) {
                logger.warn("用户不在此游戏中 - userId: {}, gameId: {}", userId, gameId);
                return;
            }
            
            // 调用技能服务使用技能
            SkillUseRequest request = new SkillUseRequest();
            request.setUserId(Long.parseLong(userId));
            request.setGameId(gameId);
            request.setSkillId(skillId);
            
            // 如果有目标位置，设置目标位置
            if (data.containsKey("targetPosition")) {
                request.setTargetPosition((Integer) data.get("targetPosition"));
            }
            
            // 如果有目标用户，设置目标用户（如果SkillUseRequest支持）
            // 注意：当前SkillUseRequest没有targetUserId字段，如果需要可以添加
            
            // 如果有额外参数，设置额外参数
            if (data.containsKey("params")) {
                // 将Map转换为JSON字符串
                try {
                    String paramsJson = JsonUtil.toJson(data.get("params"));
                    request.setParams(paramsJson);
                } catch (Exception e) {
                    logger.warn("转换技能参数失败 - userId: {}", userId, e);
                }
            }
            
            ResponseEntity<SkillEffectResponse> response = skillController.useSkill(request);
            
            // 广播技能使用结果
            Map<String, Object> skillData = new ConcurrentHashMap<>();
            skillData.put("userId", userId);
            skillData.put("skillId", skillId);
            skillData.put("success", response.getBody().isSuccess());
            skillData.put("message", response.getBody().getMessage());
            skillData.put("timestamp", System.currentTimeMillis());
            
            // 如果有游戏状态更新，包含在响应中
            if (response.getBody().getGameState() != null) {
                skillData.put("gameState", response.getBody().getGameState());
                
                // 添加玩家信息
                Game game = gameService.findById(gameId).orElse(null);
                if (game != null) {
                    if (game.getPlayer1() != null) {
                        skillData.put("player1Id", game.getPlayer1().getId());
                        skillData.put("player1Nickname", game.getPlayer1().getNickname());
                        skillData.put("player1AvatarUrl", game.getPlayer1().getAvatarUrl());
                    }
                    
                    if (game.getPlayer2() != null) {
                        skillData.put("player2Id", game.getPlayer2().getId());
                        skillData.put("player2Nickname", game.getPlayer2().getNickname());
                        skillData.put("player2AvatarUrl", game.getPlayer2().getAvatarUrl());
                    } else if (game.getType() == com.skillfive.backend.enums.GameType.VS_AI) {
                        skillData.put("player2Id", null);
                        skillData.put("player2Nickname", "AI");
                        skillData.put("player2AvatarUrl", "https://via.placeholder.com/40");
                    }
                }
            }
            
            // 如果有效果描述，包含在响应中
            if (response.getBody().getEffectDescription() != null) {
                skillData.put("effectDescription", response.getBody().getEffectDescription());
            }
            
            broadcastToGame(gameId.toString(), "skill_used", skillData);
            
        } catch (Exception e) {
            logger.error("处理技能使用失败 - userId: {}", userId, e);
        }
    }

    /**
     * 处理聊天消息
     */
    private void handleChat(String userId, Map<String, Object> data) {
        try {
            String gameId = (String) data.get("gameId");
            String content = (String) data.get("content");
            
            if (gameId == null || content == null || content.trim().isEmpty()) {
                logger.warn("游戏ID和消息内容不能为空 - userId: {}", userId);
                return;
            }
            
            // 验证用户是否在游戏中
            if (!webSocketSessionManager.isUserInGame(userId, gameId)) {
                logger.warn("用户不在此游戏中 - userId: {}, gameId: {}", userId, gameId);
                return;
            }
            
            // 广播聊天消息
            Map<String, Object> chatData = new ConcurrentHashMap<>();
            chatData.put("userId", userId);
            chatData.put("content", content);
            chatData.put("timestamp", System.currentTimeMillis());
            
            broadcastToGame(gameId, "chat", chatData);
            
        } catch (Exception e) {
            logger.error("处理聊天消息失败 - userId: {}", userId, e);
        }
    }

    /**
     * 广播消息到游戏内所有用户
     */
    private void broadcastToGame(String gameId, String eventType, Map<String, Object> eventData) {
        Map<String, Object> message = new ConcurrentHashMap<>();
        message.put("type", eventType);
        message.put("gameId", gameId);
        message.put("data", eventData);
        message.put("timestamp", System.currentTimeMillis());
        
        String jsonMessage = JsonUtil.toJson(message);
        webSocketSessionManager.sendMessageToGame(gameId, jsonMessage);
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String message) {
        try {
            Map<String, Object> errorMessage = new ConcurrentHashMap<>();
            errorMessage.put("type", "error");
            errorMessage.put("message", message);
            errorMessage.put("timestamp", System.currentTimeMillis());
            
            String jsonMessage = JsonUtil.toJson(errorMessage);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (Exception e) {
            logger.error("发送错误消息失败", e);
        }
    }

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(String userId, String messageType, Object data) {
        try {
            Map<String, Object> message = new ConcurrentHashMap<>();
            message.put("type", messageType);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());
            
            String jsonMessage = JsonUtil.toJson(message);
            webSocketSessionManager.sendMessageToUser(userId, jsonMessage);
        } catch (Exception e) {
            logger.error("向用户 {} 发送消息失败", userId, e);
        }
    }
}