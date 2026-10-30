package com.itservicedesk.modules.notification.service;

import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.modules.notification.dto.NotificationResponse;
import com.itservicedesk.modules.user.entity.User;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void createNotification(User user, String title, String message, String type, Long referenceId);

    void notifyManagers(String title, String message, String type, Long referenceId);

    PagedResponse<NotificationResponse> getUserNotifications(Pageable pageable);

    void markAsRead(Long notificationId);

    void markAllAsRead();

    long getUnreadCount();
}
