package com.jcen.medpal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jcen.medpal.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天消息Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取未读消息列表
     */
    @Select("SELECT * FROM chat_message WHERE receiver_id = #{receiverId} AND is_read = 0 AND is_delete = 0 ORDER BY create_time DESC")
    List<ChatMessage> selectUnreadMessages(Long receiverId);

    /**
     * 获取两个用户之间的聊天记录
     */
    @Select("SELECT * FROM chat_message WHERE ((sender_id = #{userId1} AND receiver_id = #{userId2}) OR (sender_id = #{userId2} AND receiver_id = #{userId1})) AND is_delete = 0 ORDER BY create_time ASC LIMIT #{offset}, #{limit}")
    List<ChatMessage> selectChatHistory(Long userId1, Long userId2, Long offset, Long limit);

    /**
     * 获取离线消息（未送达的消息）
     */
    @Select("SELECT * FROM chat_message WHERE receiver_id = #{receiverId} AND is_delivered = 0 AND is_delete = 0 ORDER BY create_time ASC")
    List<ChatMessage> selectOfflineMessages(Long receiverId);
}
