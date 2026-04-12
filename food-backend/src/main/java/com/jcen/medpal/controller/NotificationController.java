package com.jcen.medpal.controller;

import com.jcen.medpal.annotation.AuthCheck;
import com.jcen.medpal.common.ErrorCode;
import com.jcen.medpal.common.ResultUtils;
import com.jcen.medpal.constant.UserConstant;
import com.jcen.medpal.exception.BusinessException;
import com.jcen.medpal.model.entity.Notification;
import com.jcen.medpal.model.entity.User;
import com.jcen.medpal.service.NotificationService;
import com.jcen.medpal.service.UserService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    @Resource
    private UserService userService;

    @GetMapping("/list")
    public Object listNotifications(@RequestParam(required = false) Long userId,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size,
                                   HttpServletRequest request) {
        try {
            Long targetUserId = resolveTargetUserId(userId, request);
            var result = notificationService.getUserNotifications(targetUserId, status, current, size);
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @GetMapping("/unread-count")
    public Object getUnreadCount(@RequestParam(required = false) Long userId, HttpServletRequest request) {
        try {
            Long targetUserId = resolveTargetUserId(userId, request);
            long count = notificationService.getUnreadCount(targetUserId);
            return ResultUtils.success(count);
        } catch (Exception e) {
            return ResultUtils.error(40000, "查询失败");
        }
    }

    @PostMapping("/read/{id}")
    public Object markAsRead(@PathVariable Long id, HttpServletRequest request) {
        try {
            User loginUser = userService.getLoginUser(request);
            Notification notification = notificationService.getById(id);
            if (notification == null || notification.getIsDelete() != null && notification.getIsDelete() == 1) {
                return ResultUtils.error(40400, "通知不存在");
            }
            if (!isOwnerOrAdmin(loginUser, notification.getUserId())) {
                return ResultUtils.error(ErrorCode.NO_AUTH_ERROR.getCode(), "无权操作该通知");
            }
            boolean result = notificationService.markAsRead(id);
            return result ? ResultUtils.success("已标记为已读") : ResultUtils.error(40000, "操作失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "操作失败");
        }
    }

    @PostMapping("/read-all")
    public Object markAllAsRead(@RequestParam(required = false) Long userId, HttpServletRequest request) {
        try {
            Long targetUserId = resolveTargetUserId(userId, request);
            boolean result = notificationService.markAllAsRead(targetUserId);
            return result ? ResultUtils.success("全部已读") : ResultUtils.error(40000, "操作失败");
        } catch (Exception e) {
            return ResultUtils.error(40000, "操作失败");
        }
    }

    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Object createNotification(@RequestBody Notification notification) {
        try {
            Notification result = notificationService.createNotification(
                notification.getUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedType(),
                notification.getRelatedId()
            );
            return ResultUtils.success(result);
        } catch (Exception e) {
            return ResultUtils.error(40000, "创建失败");
        }
    }

    private Long resolveTargetUserId(Long userId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (userId == null) {
            return loginUser.getId();
        }
        if (!isOwnerOrAdmin(loginUser, userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权访问其他用户通知");
        }
        return userId;
    }

    private boolean isOwnerOrAdmin(User loginUser, Long targetUserId) {
        return loginUser != null
                && (loginUser.getId().equals(targetUserId) || userService.isAdmin(loginUser));
    }
}
