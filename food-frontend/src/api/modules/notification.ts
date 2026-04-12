import request from '../request';
import { BaseResponse, PageData, PageParams } from '../types';

// 通知类型
export type NotificationType = 'order' | 'system' | 'message' | 'payment';

// 通知状态
export type NotificationStatus = 'unread' | 'read';

// 通知
export interface Notification {
  id: number;
  userId: number;
  type: NotificationType;
  title: string;
  content: string;
  status: NotificationStatus;
  relatedType?: string;
  relatedId?: number;
  createTime: string;
  updateTime: string;
}

/**
 * 获取通知列表
 * @param userId 用户ID
 * @param status 通知状态（可选）
 * @param current 当前页码
 * @param size 每页大小
 */
export function listNotifications(
  userId: number,
  status?: NotificationStatus,
  current: number = 1,
  size: number = 10
): Promise<BaseResponse<PageData<Notification>>> {
  return request.get('/notification/list', {
    params: { userId, status, current, size },
  }) as Promise<BaseResponse<PageData<Notification>>>;
}

/**
 * 获取未读通知数量
 * @param userId 用户ID
 */
export function getUnreadCount(userId: number): Promise<BaseResponse<number>> {
  return request.get('/notification/unread-count', {
    params: { userId },
  }) as Promise<BaseResponse<number>>;
}

/**
 * 标记通知为已读
 * @param id 通知ID
 */
export function markNotificationAsRead(id: number): Promise<BaseResponse<string>> {
  return request.post(`/notification/read/${id}`) as Promise<BaseResponse<string>>;
}

/**
 * 标记所有通知为已读
 * @param userId 用户ID
 */
export function markAllAsRead(userId: number): Promise<BaseResponse<string>> {
  return request.post('/notification/read-all', null, {
    params: { userId },
  }) as Promise<BaseResponse<string>>;
}

/**
 * 创建通知
 * @param notification 通知内容
 */
export function createNotification(notification: Partial<Notification>): Promise<BaseResponse<Notification>> {
  return request.post('/notification/create', notification) as Promise<BaseResponse<Notification>>;
}
