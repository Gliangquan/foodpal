package com.jcen.medpal.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * 自定义 WebSocket 握手拦截器
 * 用于从请求中提取用户信息并建立连接
 */
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // 从请求参数中获取用户ID和 token
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String userId = servletRequest.getServletRequest().getParameter("userId");
            String userType = servletRequest.getServletRequest().getParameter("userType");
            String token = servletRequest.getServletRequest().getParameter("token");
            
            if (userId != null) {
                attributes.put("userId", userId);
            }
            if (userType != null) {
                attributes.put("userType", userType);
            }
            if (token != null) {
                attributes.put("token", token);
            }
        }
        
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理
        if (exception != null) {
            System.err.println("WebSocket 握手失败: " + exception.getMessage());
        }
    }
}
