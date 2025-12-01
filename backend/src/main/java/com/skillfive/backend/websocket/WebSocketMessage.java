package com.skillfive.backend.websocket;

import lombok.Data;
import java.util.Map;

/**
 * WebSocket消息类
 */
@Data
public class WebSocketMessage {
    
    /**
     * 消息类型
     * - ping/pong: 心跳
     * - move: 移动操作
     * - skill: 技能使用
     * - chat: 聊天消息
     * - status: 状态更新
     * - notification: 通知
     * - error: 错误消息
     */
    private String type;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 游戏ID
     */
    private String gameId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 附加数据
     */
    private Map<String, Object> data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 创建消息实例
     */
    public static WebSocketMessage create(String type, String userId, String gameId, Object data) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType(type);
        message.setUserId(userId);
        message.setGameId(gameId);
        if (data instanceof Map) {
            message.setData((Map<String, Object>) data);
        }
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    /**
     * 创建错误消息
     */
    public static WebSocketMessage error(String userId, String errorMessage) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("error");
        message.setUserId(userId);
        message.setContent(errorMessage);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
    
    /**
     * 创建通知消息
     */
    public static WebSocketMessage notification(String userId, String content) {
        WebSocketMessage message = new WebSocketMessage();
        message.setType("notification");
        message.setUserId(userId);
        message.setContent(content);
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }
}