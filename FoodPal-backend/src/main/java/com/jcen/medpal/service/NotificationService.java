package com.jcen.medpal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jcen.medpal.model.entity.Notification;

public interface NotificationService extends IService<Notification> {
    
    Notification createNotification(Long userId, String type, String title, String content, String relatedType, Long relatedId);
    
    IPage<Notification> getUserNotifications(Long userId, String status, long current, long size);
    
    boolean markAsRead(Long notificationId);
    
    boolean markAllAsRead(Long userId);
    
    long getUnreadCount(Long userId);
    
    void sendOrderNotification(Long userId, String orderStatus, Long orderId);
}
