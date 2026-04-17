package com.jcen.medpal.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收者ID
     */
    private Long receiverId;

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
}
