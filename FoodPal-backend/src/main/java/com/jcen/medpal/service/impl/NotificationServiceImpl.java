package com.jcen.medpal.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jcen.medpal.mapper.NotificationMapper;
import com.jcen.medpal.model.entity.Notification;
import com.jcen.medpal.service.NotificationService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {
    
    @Override
    public Notification createNotification(Long userId, String type, String title, String content, String relatedType, Long relatedId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setStatus("unread");
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notification.setIsDelete(0);
        this.save(notification);
        return notification;
    }
    
    @Override
    public IPage<Notification> getUserNotifications(Long userId, String status, long current, long size) {
        var query = this.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsDelete, 0)
                .orderByDesc(Notification::getCreateTime);
        
        if (status != null && !status.isEmpty()) {
            query.eq(Notification::getStatus, status);
        }
        
        return query.page(new Page<>(current, size));
    }
    
    @Override
    public boolean markAsRead(Long notificationId) {
        Notification notification = this.getById(notificationId);
        if (notification == null) {
            return false;
        }
        notification.setStatus("read");
        notification.setReadTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        return this.updateById(notification);
    }
    
    @Override
    public boolean markAllAsRead(Long userId) {
        Notification update = new Notification();
        update.setStatus("read");
        update.setReadTime(LocalDateTime.now());
        update.setUpdateTime(LocalDateTime.now());
        
        return this.lambdaUpdate()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, "unread")
                .update(update);
    }
    
    @Override
    public long getUnreadCount(Long userId) {
        return this.lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getStatus, "unread")
                .eq(Notification::getIsDelete, 0)
                .count();
    }
    
    @Override
    public void sendOrderNotification(Long userId, String orderStatus, Long orderId) {
        String title;
        String content;
        
        switch (orderStatus) {
            case "confirmed":
                title = "订单已被接单";
                content = "您的陪诊订单已被接单，陪诊员将准时到达";
                break;
            case "service_started":
                title = "陪诊服务开始";
                content = "陪诊员已出发，请注意查收消息";
                break;
            case "completed":
                title = "服务完成";
                content = "陪诊服务已完成，请对服务进行评价";
                break;
            case "cancelled":
                title = "订单已取消";
                content = "您的订单已被取消，如有疑问请联系客服";
                break;
            default:
                return;
        }
        
        createNotification(userId, "order", title, content, "order", orderId);
    }
}
