package com.jcen.medpal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket STOMP 消息处理器
 */
@Slf4j
@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 处理客户端发送的消息
     */
    @MessageMapping("/chat/send")
    @SendTo("/topic/messages")
    public Map<String, Object> handleChatMessage(Map<String, Object> message) {
        log.info("收到消息: {}", message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", "chat");
        response.put("data", message);
        response.put("timestamp", System.currentTimeMillis());
        
        return response;
    }

    /**
     * 处理心跳消息
     */
    @MessageMapping("/ping")
    public void handlePing(Map<String, Object> message) {
        log.debug("收到心跳消息");
        
        // 发送 pong 响应
        messagingTemplate.convertAndSend("/topic/pong", new HashMap<String, Object>() {{
            put("type", "pong");
            put("timestamp", System.currentTimeMillis());
        }});
    }

    /**
     * 处理用户上线消息
     */
    @MessageMapping("/online")
    public void handleOnline(Map<String, Object> message) {
        Long userId = ((Number) message.get("userId")).longValue();
        log.info("用户 {} 上线", userId);
        
        // 广播用户上线消息
        messagingTemplate.convertAndSend("/topic/online", new HashMap<String, Object>() {{
            put("type", "online");
            put("userId", userId);
            put("timestamp", System.currentTimeMillis());
        }});
    }

    /**
     * 处理用户离线消息
     */
    @MessageMapping("/offline")
    public void handleOffline(Map<String, Object> message) {
        Long userId = ((Number) message.get("userId")).longValue();
        log.info("用户 {} 离线", userId);
        
        // 广播用户离线消息
        messagingTemplate.convertAndSend("/topic/offline", new HashMap<String, Object>() {{
            put("type", "offline");
            put("userId", userId);
            put("timestamp", System.currentTimeMillis());
        }});
    }
}
