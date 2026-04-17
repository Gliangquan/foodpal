package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.ChatMessageMapper;
import com.jcen.medpal.mapper.UserMapper;
import com.jcen.medpal.model.dto.chat.ChatMessageRequest;
import com.jcen.medpal.model.entity.ChatMessage;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.vo.ChatMessageVO;
import com.jcen.medpal.service.ChatMessageService;
import com.jcen.medpal.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天消息服务实现
 */
@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long senderId, ChatMessageRequest request) {
        // 创建消息
        ChatMessage message = ChatMessage.builder()
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .content(request.getContent())
                .messageType(request.getMessageType() != null ? request.getMessageType() : "text")
                .imageUrl(request.getImageUrl())
                .fileUrl(request.getFileUrl())
                .isRead(0)
                .isDelivered(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDelete(0)
                .build();

        // 保存消息到数据库
        chatMessageMapper.insert(message);

        // 尝试通过 WebSocket 发送消息
        try {
            ChatMessageVO vo = convertToVO(message);
            messagingTemplate.convertAndSendToUser(
                    request.getReceiverId().toString(),
                    "/queue/messages",
                    vo
            );
            // 标记为已送达
            message.setIsDelivered(1);
            message.setDeliveredTime(LocalDateTime.now());
            chatMessageMapper.updateById(message);
        } catch (Exception e) {
            log.warn("WebSocket 发送失败，消息将作为离线消息存储: {}", e.getMessage());
        }

        // 创建系统通知
        notificationService.createNotification(
                request.getReceiverId(),
                "chat",
                "新消息",
                "来自用户的新消息",
                "chat",
                message.getId()
        );

        return convertToVO(message);
    }

    @Override
    public List<ChatMessageVO> getUnreadMessages(Long receiverId) {
        List<ChatMessage> messages = chatMessageMapper.selectUnreadMessages(receiverId);
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVO> getChatHistory(Long userId1, Long userId2, Integer pageNum, Integer pageSize) {
        Long offset = (long) (pageNum - 1) * pageSize;
        List<ChatMessage> messages = chatMessageMapper.selectChatHistory(userId1, userId2, offset, (long) pageSize);
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message != null) {
            message.setIsRead(1);
            message.setReadTime(LocalDateTime.now());
            chatMessageMapper.updateById(message);
        }
    }

    @Override
    @Transactional
    public void markAsDelivered(Long messageId) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        if (message != null) {
            message.setIsDelivered(1);
            message.setDeliveredTime(LocalDateTime.now());
            chatMessageMapper.updateById(message);
        }
    }

    @Override
    public List<ChatMessageVO> getOfflineMessages(Long receiverId) {
        List<ChatMessage> messages = chatMessageMapper.selectOfflineMessages(receiverId);
        return messages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markMessagesAsDelivered(List<Long> messageIds) {
        for (Long messageId : messageIds) {
            markAsDelivered(messageId);
        }
    }

    /**
     * 将 ChatMessage 转换为 ChatMessageVO
     */
    private ChatMessageVO convertToVO(ChatMessage message) {
        User sender = userMapper.selectById(message.getSenderId());
        User receiver = userMapper.selectById(message.getReceiverId());

        return ChatMessageVO.builder()
                .id(message.getId())
                .senderId(message.getSenderId())
                .senderName(sender != null ? sender.getUserName() : "未知用户")
                .receiverId(message.getReceiverId())
                .receiverName(receiver != null ? receiver.getUserName() : "未知用户")
                .content(message.getContent())
                .messageType(message.getMessageType())
                .imageUrl(message.getImageUrl())
                .fileUrl(message.getFileUrl())
                .isRead(message.getIsRead())
                .readTime(message.getReadTime())
                .isDelivered(message.getIsDelivered())
                .deliveredTime(message.getDeliveredTime())
                .createTime(message.getCreateTime())
                .build();
    }
}
