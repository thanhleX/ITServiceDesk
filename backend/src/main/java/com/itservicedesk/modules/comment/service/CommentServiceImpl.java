package com.itservicedesk.modules.comment.service;

import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.common.util.SecurityUtils;
import com.itservicedesk.modules.comment.dto.CommentResponse;
import com.itservicedesk.modules.comment.dto.CreateCommentRequest;
import com.itservicedesk.modules.comment.entity.TicketComment;
import com.itservicedesk.modules.comment.repository.TicketCommentRepository;
import com.itservicedesk.modules.history.entity.TicketHistory;
import com.itservicedesk.modules.history.repository.TicketHistoryRepository;
import com.itservicedesk.modules.notification.service.NotificationService;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.repository.TicketRepository;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository historyRepository;
    private final NotificationService notificationService;
    private final com.itservicedesk.modules.ticket.service.TicketAuthorizationService ticketAuthorizationService;

    @Override
    @Transactional
    public CommentResponse addComment(Long ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        // Kiểm tra quyền bình luận tài nguyên (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanComment(ticket, SecurityUtils.getCurrentUserDetails());

        TicketComment comment = TicketComment.builder()
                .ticket(ticket)
                .user(currentUser)
                .content(request.getContent().trim())
                .build();
        comment = commentRepository.save(comment);

        // Lưu Audit Log
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(currentUser)
                .action("COMMENT_ADDED")
                .description("Thêm bình luận mới bởi " + currentUser.getFullName())
                .build();
        historyRepository.save(history);

        // Gửi thông báo đến các bên liên quan trong Ticket
        sendCommentNotifications(ticket, currentUser, request.getContent());

        return CommentResponse.fromEntity(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getTicketComments(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        // Kiểm tra quyền xem bình luận (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanViewTicket(ticket, SecurityUtils.getCurrentUserDetails());

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private void sendCommentNotifications(Ticket ticket, User commenter, String content) {
        String snippet = content.length() > 60 ? content.substring(0, 60) + "..." : content;
        String title = "Bình luận mới: " + ticket.getTicketCode();
        String message = commenter.getFullName() + ": \"" + snippet + "\"";

        // Nếu người bình luận là Creator -> thông báo cho Assignee
        if (ticket.getCreatedBy().getId().equals(commenter.getId())) {
            if (ticket.getAssignedTo() != null) {
                notificationService.createNotification(ticket.getAssignedTo(), title, message, "COMMENT_ADDED", ticket.getId());
            }
        } else {
            // Nếu người bình luận là Assignee hoặc Manager -> thông báo cho Creator
            notificationService.createNotification(ticket.getCreatedBy(), title, message, "COMMENT_ADDED", ticket.getId());

            // Và nếu Manager bình luận thì thông báo luôn cho Assignee (nếu có và không phải chính Manager)
            if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(commenter.getId())) {
                notificationService.createNotification(ticket.getAssignedTo(), title, message, "COMMENT_ADDED", ticket.getId());
            }
        }
    }
}
