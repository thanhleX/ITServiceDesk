package com.itservicedesk.modules.ticket.service;

import com.itservicedesk.modules.notification.entity.Notification;
import com.itservicedesk.modules.notification.repository.NotificationRepository;
import com.itservicedesk.modules.notification.service.NotificationService;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import com.itservicedesk.modules.ticket.repository.TicketRepository;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlaSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SlaSchedulerService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    /**
     * Chạy định kỳ mỗi 15 phút để quét và cảnh báo SLA tự động
     */
    @Scheduled(cron = "0 */15 * * * ?")
    @Transactional
    public void scanAndAlertSla() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Bắt đầu quét giám sát SLA định kỳ tại: {}", now.format(TIME_FORMATTER));

        // Lấy tất cả các Ticket chưa hoàn tất (Khác RESOLVED và CLOSED)
        List<Ticket> activeTickets = ticketRepository.findAll().stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED)
                .collect(Collectors.toList());

        LocalDateTime warningThreshold = now.minusHours(12); // Không gửi thông báo lặp lại trong vòng 12 giờ

        for (Ticket ticket : activeTickets) {
            if (ticket.getDueAt() == null) continue;

            if (now.isAfter(ticket.getDueAt())) {
                // 1. Trường hợp Ticket đã QUÁ HẠN (SLA Breached)
                handleSlaBreached(ticket, warningThreshold);
            } else if (ticket.getDueAt().isBefore(now.plusHours(2))) {
                // 2. Trường hợp Ticket SẮP QUÁ HẠN trong vòng 2 giờ (SLA Warning)
                handleSlaApproaching(ticket, warningThreshold);
            }
        }
    }

    private void handleSlaBreached(Ticket ticket, LocalDateTime warningThreshold) {
        String type = "SLA_BREACH";
        String title = "CẢNH BÁO QUÁ HẠN SLA: " + ticket.getTicketCode();
        String message = String.format("Ticket '%s' đã quá hạn xử lý (Hạn chót: %s).",
                ticket.getTitle(), ticket.getDueAt().format(TIME_FORMATTER));

        // Cảnh báo Kỹ thuật viên phụ trách (nếu có)
        if (ticket.getAssignedTo() != null) {
            boolean alreadySent = notificationRepository.existsByUserIdAndReferenceIdAndTypeAndCreatedAtAfter(
                    ticket.getAssignedTo().getId(), ticket.getId(), type, warningThreshold
            );
            if (!alreadySent) {
                notificationService.createNotification(ticket.getAssignedTo(), title, message, type, ticket.getId());
            }
        }

        // Cảnh báo Trưởng phòng IT
        List<User> managers = getActiveManagers();
        for (User manager : managers) {
            boolean alreadySent = notificationRepository.existsByUserIdAndReferenceIdAndTypeAndCreatedAtAfter(
                    manager.getId(), ticket.getId(), type, warningThreshold
            );
            if (!alreadySent) {
                notificationService.createNotification(manager, title, message, type, ticket.getId());
            }
        }
    }

    private void handleSlaApproaching(Ticket ticket, LocalDateTime warningThreshold) {
        String type = "SLA_WARNING";
        String title = "NHẮC NHỞ SẮP ĐẾN HẠN SLA: " + ticket.getTicketCode();
        String message = String.format("Ticket '%s' sắp hết hạn xử lý vào lúc %s (còn dưới 2 giờ).",
                ticket.getTitle(), ticket.getDueAt().format(TIME_FORMATTER));

        if (ticket.getAssignedTo() != null) {
            boolean alreadySent = notificationRepository.existsByUserIdAndReferenceIdAndTypeAndCreatedAtAfter(
                    ticket.getAssignedTo().getId(), ticket.getId(), type, warningThreshold
            );
            if (!alreadySent) {
                notificationService.createNotification(ticket.getAssignedTo(), title, message, type, ticket.getId());
            }
        }
    }

    private List<User> getActiveManagers() {
        return userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_IT_MANAGER".equalsIgnoreCase(r.getCode())))
                .collect(Collectors.toList());
    }
}
