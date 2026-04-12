package com.jcen.medpal.controller;

import com.jcen.medpal.annotation.AuthCheck;
import com.jcen.medpal.common.BaseResponse;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.model.dto.chat.ChatMessageRequest;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.model.vo.ChatMessageVO;
import com.jcen.medpal.service.ChatMessageService;
import com.jcen.medpal.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 聊天消息控制器
 */
@Slf4j
@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private UserService userService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    @AuthCheck(mustRole = "")
    public BaseResponse<ChatMessageVO> sendMessage(
            @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser == null) {
            return ResultUtils.error(40100, "未登录");
        }

        ChatMessageVO message = chatMessageService.sendMessage(loginUser.getId(), request);
        return ResultUtils.success(message);
    }

    /**
     * 获取未读消息
     */
    @GetMapping("/unread")
    @AuthCheck(mustRole = "")
    public BaseResponse<List<ChatMessageVO>> getUnreadMessages(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser == null) {
            return ResultUtils.error(40100, "未登录");
        }

        List<ChatMessageVO> messages = chatMessageService.getUnreadMessages(loginUser.getId());
        return ResultUtils.success(messages);
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/history")
    @AuthCheck(mustRole = "")
    public BaseResponse<List<ChatMessageVO>> getChatHistory(
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser == null) {
            return ResultUtils.error(40100, "未登录");
        }

        List<ChatMessageVO> messages = chatMessageService.getChatHistory(loginUser.getId(), targetUserId, pageNum, pageSize);
        return ResultUtils.success(messages);
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/read/{messageId}")
    @AuthCheck(mustRole = "")
    public BaseResponse<Boolean> markAsRead(@PathVariable Long messageId) {
        chatMessageService.markAsRead(messageId);
        return ResultUtils.success(true);
    }

    /**
     * 获取离线消息
     */
    @GetMapping("/offline")
    @AuthCheck(mustRole = "")
    public BaseResponse<List<ChatMessageVO>> getOfflineMessages(HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser == null) {
            return ResultUtils.error(40100, "未登录");
        }

        List<ChatMessageVO> messages = chatMessageService.getOfflineMessages(loginUser.getId());
        return ResultUtils.success(messages);
    }

    /**
     * 批量标记消息为已送达
     */
    @PostMapping("/delivered")
    @AuthCheck(mustRole = "")
    public BaseResponse<Boolean> markMessagesAsDelivered(@RequestBody List<Long> messageIds) {
        chatMessageService.markMessagesAsDelivered(messageIds);
        return ResultUtils.success(true);
    }
}
