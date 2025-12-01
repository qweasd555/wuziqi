package com.skillfive.backend.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;

import java.util.List;

/**
 * WebSocket消息拦截器
 */
public class WebSocketInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketInterceptor.class);
    
    private final WebSocketSessionManager sessionManager;
    
    public WebSocketInterceptor(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null) {
            // 处理连接请求
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                // 从headers中获取token或userId
                List<String> tokenValues = accessor.getNativeHeader("Authorization");
                List<String> userIdValues = accessor.getNativeHeader("userId");
                
                String token = tokenValues != null && !tokenValues.isEmpty() ? tokenValues.get(0) : null;
                String userId = userIdValues != null && !userIdValues.isEmpty() ? userIdValues.get(0) : null;
                
                // 可以在这里进行认证验证
                logger.info("WebSocket连接请求 - userId: {}, token: {}", userId, token != null ? "[已提供]" : "[未提供]");
                
                // 如果验证成功，可以将用户信息存储在sessionAttributes中
                if (userId != null) {
                    accessor.getSessionAttributes().put("userId", userId);
                }
            } 
            // 处理订阅请求
            else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                String destination = accessor.getDestination();
                String userId = (String) accessor.getSessionAttributes().get("userId");
                logger.info("WebSocket订阅请求 - userId: {}, destination: {}", userId, destination);
            }
            // 处理断开连接请求
            else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                String userId = (String) accessor.getSessionAttributes().get("userId");
                logger.info("WebSocket断开连接 - userId: {}", userId);
                
                // 可以在这里清理用户相关资源
                sessionManager.removeUserSession(userId);
            }
        }
        
        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        // 消息发送后处理
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        // 发送完成后处理
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        return true;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        return message;
    }

    @Override
    public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        // 接收完成后处理
    }
}