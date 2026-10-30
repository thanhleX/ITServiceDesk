package com.itservicedesk.modules.notification.service;

import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.common.util.SecurityUtils;
import com.itservicedesk.modules.notification.dto.NotificationResponse;
import com.itservicedesk.modules.notification.entity.Notification;
import com.itservicedesk.modules.notification.repository.NotificationRepository;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void createNotification(User user, String title, String message, String type, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyManagers(String title, String message, String type, Long referenceId) {
        List<User> managers = userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_IT_MANAGER".equalsIgnoreCase(r.getCode())))
                .collect(Collectors.toList());

        for (User manager : managers) {
            createNotification(manager, title, message, type, referenceId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getUserNotifications(Pageable pageable) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Page<Notification> page = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable);

        List<NotificationResponse> content = page.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (notification.getUser().getId().equals(currentUserId)) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        notificationRepository.markAllAsRead(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(currentUserId);
    }
}
