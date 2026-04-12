package com.jcen.medpal.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 接收者名称
     */
    private String receiverName;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：text-文本, image-图片, file-文件
     */
    private String messageType;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 是否已送达
     */
    private Integer isDelivered;

    /**
     * 送达时间
     */
    private LocalDateTime deliveredTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
