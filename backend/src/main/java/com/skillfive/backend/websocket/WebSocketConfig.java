package com.skillfive.backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 */
@Configuration
@EnableWebSocketMessageBroker
@Profile("stomp")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，允许前端连接
        registry.addEndpoint("/ws-stomp")
                .setAllowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:8080") // 在生产环境中应该限制为特定域名
                .withSockJS(); // 启用SockJS作为备选
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单的消息代理，用于向客户端广播消息
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用程序目的地前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 可以在这里添加消息拦截器，用于认证等
        registration.interceptors(new WebSocketInterceptor(new WebSocketSessionManager()));
    }
}