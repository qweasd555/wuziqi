package com.skillfive.backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 原生WebSocket配置类
 */
@Configuration
@EnableWebSocket
public class NativeWebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    
    public NativeWebSocketConfig(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册原生WebSocket处理器
        registry.addHandler(webSocketHandler, "/ws")
                .setAllowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:3001", "http://127.0.0.1:3001");
    }
}