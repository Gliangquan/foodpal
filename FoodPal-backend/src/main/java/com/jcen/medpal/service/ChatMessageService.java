package com.jcen.medpal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.dto.chat.ChatMessageRequest;
import com.jcen.medpal.model.entity.ChatMessage;
import com.jcen.medpal.model.vo.ChatMessageVO;

import java.util.List;

/**
 * 聊天消息服务接口
 */
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 发送消息
     */
    ChatMessageVO sendMessage(Long senderId, ChatMessageRequest request);

    /**
     * 获取未读消息列表
     */
    List<ChatMessageVO> getUnreadMessages(Long receiverId);

    /**
     * 获取聊天历史
     */
    List<ChatMessageVO> getChatHistory(Long userId1, Long userId2, Integer pageNum, Integer pageSize);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId);

    /**
     * 标记消息为已送达
     */
    void markAsDelivered(Long messageId);

    /**
     * 获取离线消息
     */
    List<ChatMessageVO> getOfflineMessages(Long receiverId);

    /**
     * 批量标记消息为已送达
     */
    void markMessagesAsDelivered(List<Long> messageIds);
}
