import axiosClient from './axiosClient';
import { ApiResponse, NotificationItem, PagedResponse } from '../types';

export const notificationApi = {
  getMyNotifications: (page = 0, size = 10) =>
    axiosClient.get<any, ApiResponse<PagedResponse<NotificationItem>>>('/notifications', {
      params: { page, size },
    }),

  getUnreadCount: () =>
    axiosClient.get<any, ApiResponse<number>>('/notifications/unread-count'),

  markAsRead: (id: number) =>
    axiosClient.patch<any, ApiResponse<void>>(`/notifications/${id}/read`),

  markAllAsRead: () =>
    axiosClient.patch<any, ApiResponse<void>>('/notifications/read-all'),
};
