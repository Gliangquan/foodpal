package com.jcen.medpal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 * 支持 STOMP 协议的 WebSocket 实时通信
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * 用于实现消息的订阅和广播
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理，消息将以 /topic 开头（用于群发）
        // 客户端可以订阅 /topic/xxx 来接收消息
        config.enableSimpleBroker("/topic", "/queue");
        
        // 配置应用程序目的地前缀（用于点对点消息）
        // 客户端发送消息时，目的以 /app 开头
        config.setApplicationDestinationPrefixes("/app");
        
        // 配置用户目的地前缀（用于特定用户的消息）
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 配置 WebSocket 端点
     * 客户端连接时需要访问此端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 WebSocket 端点，客户端通过 /ws 进行连接
        // withSockJS() 启用 SockJS 作为备选传输方案
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // 允许跨域
                .withSockJS()
                .setInterceptors(new CustomHandshakeInterceptor());
        
        // 同时注册不使用 SockJS 的端点（原生 WebSocket）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new org.springframework.web.socket.server.support.DefaultHandshakeHandler())
                .addInterceptors(new CustomHandshakeInterceptor());
    }
}
