package com.itservicedesk.modules.ticket.service;

import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.common.exception.BadRequestException;
import com.itservicedesk.common.exception.ConflictException;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.common.util.SecurityUtils;
import com.itservicedesk.common.util.TicketCodeGenerator;
import com.itservicedesk.modules.assignment.dto.AssignmentResponse;
import com.itservicedesk.modules.assignment.entity.TicketAssignment;
import com.itservicedesk.modules.assignment.repository.TicketAssignmentRepository;
import com.itservicedesk.modules.attachment.repository.TicketAttachmentRepository;
import com.itservicedesk.modules.category.entity.Category;
import com.itservicedesk.modules.category.repository.CategoryRepository;
import com.itservicedesk.modules.comment.repository.TicketCommentRepository;
import com.itservicedesk.modules.history.dto.HistoryResponse;
import com.itservicedesk.modules.history.entity.TicketHistory;
import com.itservicedesk.modules.history.repository.TicketHistoryRepository;
import com.itservicedesk.modules.notification.service.NotificationService;
import com.itservicedesk.modules.ticket.dto.*;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import com.itservicedesk.modules.ticket.repository.TicketRepository;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TicketAssignmentRepository assignmentRepository;
    private final TicketHistoryRepository historyRepository;
    private final TicketCommentRepository commentRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final NotificationService notificationService;
    private final TicketCodeGenerator ticketCodeGenerator;
    private final TicketAuthorizationService ticketAuthorizationService;

    @Override
    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User creator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (!"ACTIVE".equalsIgnoreCase(category.getStatus())) {
            throw new BadRequestException("Danh mục sự cố này hiện đang ngưng hoạt động");
        }

        // 1. Tự động sinh Ticket Code chuẩn (VD: TCK-2026-00001)
        String code = ticketCodeGenerator.generateCode();

        // 2. Tính toán Due Date theo SLA của Priority
        LocalDateTime dueAt = LocalDateTime.now().plusHours(request.getPriority().getSlaHours());

        // 3. Khởi tạo Ticket với trạng thái mặc định OPEN
        Ticket ticket = Ticket.builder()
                .ticketCode(code)
                .title(request.getTitle().trim())
                .description(request.getDescription().trim())
                .category(category)
                .createdBy(creator)
                .priority(request.getPriority())
                .status(TicketStatus.OPEN)
                .dueAt(dueAt)
                .version(0)
                .build();

        ticket = ticketRepository.save(ticket);

        // 4. Lưu Audit Log đầu tiên
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(creator)
                .action("CREATED")
                .newStatus(TicketStatus.OPEN.name())
                .description("Tạo mới yêu cầu hỗ trợ")
                .build();
        historyRepository.save(history);

        // 5. Gửi thông báo đến toàn bộ IT Manager để tiếp nhận và phân công
        notificationService.notifyManagers(
                "Ticket mới: " + ticket.getTicketCode(),
                "Nhân viên " + creator.getFullName() + " vừa tạo yêu cầu: " + ticket.getTitle(),
                "TICKET_CREATED",
                ticket.getId()
        );

        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TicketResponse> getTickets(TicketFilterCriteria criteria, Pageable pageable) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isManager = SecurityUtils.isManager();
        boolean isStaff = SecurityUtils.isStaff();

        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Phân quyền phạm vi dữ liệu theo Role (Data Scope Isolation)
            if (!isManager) {
                if (isStaff) {
                    // IT Staff xem các Ticket được giao hoặc do chính mình tạo
                    Predicate assignedToMe = cb.equal(root.get("assignedTo").get("id"), currentUserId);
                    Predicate createdByMe = cb.equal(root.get("createdBy").get("id"), currentUserId);
                    predicates.add(cb.or(assignedToMe, createdByMe));
                } else {
                    // Employee chỉ xem Ticket do chính mình tạo
                    predicates.add(cb.equal(root.get("createdBy").get("id"), currentUserId));
                }
            }

            // 2. Các bộ lọc tìm kiếm
            if (criteria != null) {
                if (StringUtils.hasText(criteria.getKeyword())) {
                    String pattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
                    Predicate matchCode = cb.like(cb.lower(root.get("ticketCode")), pattern);
                    Predicate matchTitle = cb.like(cb.lower(root.get("title")), pattern);
                    predicates.add(cb.or(matchCode, matchTitle));
                }

                if (criteria.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
                }

                if (criteria.getPriority() != null) {
                    predicates.add(cb.equal(root.get("priority"), criteria.getPriority()));
                }

                if (criteria.getCategoryId() != null) {
                    predicates.add(cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
                }

                if (isManager && criteria.getCreatedById() != null) {
                    predicates.add(cb.equal(root.get("createdBy").get("id"), criteria.getCreatedById()));
                }

                if (criteria.getAssignedToId() != null) {
                    predicates.add(cb.equal(root.get("assignedTo").get("id"), criteria.getAssignedToId()));
                }

                if (criteria.getFromDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getFromDate().atStartOfDay()));
                }

                if (criteria.getToDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getToDate().atTime(LocalTime.MAX)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Ticket> page = ticketRepository.findAll(spec, pageable);
        List<TicketResponse> content = page.getContent().stream()
                .map(TicketResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.<TicketResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketDetail(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        // Kiểm tra quyền truy cập tài nguyên (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanViewTicket(ticket, SecurityUtils.getCurrentUserDetails());

        List<AssignmentResponse> assignments = assignmentRepository.findByTicketIdOrderByAssignedAtDesc(id).stream()
                .map(AssignmentResponse::fromEntity)
                .collect(Collectors.toList());

        List<HistoryResponse> histories = historyRepository.findByTicketIdOrderByCreatedAtDesc(id).stream()
                .map(HistoryResponse::fromEntity)
                .collect(Collectors.toList());

        long commentCount = commentRepository.countByTicketId(id);
        long attachmentCount = attachmentRepository.countByTicketId(id);

        return TicketDetailResponse.builder()
                .ticket(TicketResponse.fromEntity(ticket))
                .assignments(assignments)
                .histories(histories)
                .totalComments(commentCount)
                .totalAttachments(attachmentCount)
                .build();
    }

    @Override
    @Transactional
    public TicketResponse updateTicket(Long id, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        // Kiểm tra Khóa lạc quan (Optimistic Locking)
        if (!Objects.equals(ticket.getVersion(), request.getVersion())) {
            throw new ConflictException("Ticket vừa được cập nhật bởi một người khác. Vui lòng tải lại trang và thực hiện lại.");
        }

        // Kiểm tra quyền chỉnh sửa tài nguyên (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanUpdateTicket(ticket, SecurityUtils.getCurrentUserDetails());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        ticket.setTitle(request.getTitle().trim());
        ticket.setDescription(request.getDescription().trim());
        ticket.setCategory(category);

        if (SecurityUtils.isManager() && request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
            ticket.setDueAt(ticket.getCreatedAt().plusHours(request.getPriority().getSlaHours()));
        }

        ticket = ticketRepository.save(ticket);

        // Ghi Audit Log
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(currentUser)
                .action("UPDATED")
                .description("Cập nhật thông tin tiêu đề/mô tả sự cố")
                .build();
        historyRepository.save(history);

        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional
    public TicketResponse assignTicket(Long id, AssignTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        // Kiểm tra quyền phân công (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanAssignTicket(ticket, SecurityUtils.getCurrentUserDetails());

        // Kiểm tra Khóa lạc quan (Optimistic Locking)
        if (!Objects.equals(ticket.getVersion(), request.getVersion())) {
            throw new ConflictException("Ticket vừa được cập nhật bởi một người khác. Vui lòng tải lại trang và thực hiện lại.");
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Không thể phân công cho Ticket đã đóng");
        }

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));

        if (!"ACTIVE".equalsIgnoreCase(assignee.getStatus())) {
            throw new BadRequestException("Kỹ thuật viên này hiện đang không hoạt động (Inactive/Locked)");
        }

        boolean hasStaffRole = assignee.getRoles().stream()
                .anyMatch(r -> "ROLE_IT_STAFF".equalsIgnoreCase(r.getCode()));
        if (!hasStaffRole) {
            throw new BadRequestException("Người nhận phân công phải có vai trò Kỹ thuật viên (ROLE_IT_STAFF)");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User manager = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        TicketStatus oldStatus = ticket.getStatus();
        // Cập nhật người phụ trách và chuyển trạng thái
        ticket.setAssignedTo(assignee);
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.ASSIGNED);
        }

        ticket = ticketRepository.save(ticket);

        // Lưu vết lịch sử phân công
        TicketAssignment assignment = TicketAssignment.builder()
                .ticket(ticket)
                .assignedBy(manager)
                .assignedTo(assignee)
                .note(request.getNote())
                .build();
        assignmentRepository.save(assignment);

        // Lưu Audit Log
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(manager)
                .action("ASSIGNED")
                .oldStatus(oldStatus.name())
                .newStatus(ticket.getStatus().name())
                .description("Phân công cho " + assignee.getFullName() + (StringUtils.hasText(request.getNote()) ? " (" + request.getNote() + ")" : ""))
                .build();
        historyRepository.save(history);

        // Gửi thông báo đến kỹ thuật viên được giao
        notificationService.createNotification(
                assignee,
                "Bạn được phân công Ticket: " + ticket.getTicketCode(),
                "Trưởng phòng IT đã giao Ticket '" + ticket.getTitle() + "' cho bạn xử lý.",
                "TICKET_ASSIGNED",
                ticket.getId()
        );

        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional
    public TicketResponse updateTicketStatus(Long id, UpdateTicketStatusRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        // Kiểm tra Khóa lạc quan (Optimistic Locking)
        if (!Objects.equals(ticket.getVersion(), request.getVersion())) {
            throw new ConflictException("Ticket vừa được cập nhật bởi một người khác. Vui lòng tải lại trang và thực hiện lại.");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        boolean isManager = SecurityUtils.isManager();
        boolean isCreator = ticket.getCreatedBy().getId().equals(currentUserId);
        boolean isAssignee = ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(currentUserId);

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus targetStatus = request.getTargetStatus();

        if (currentStatus == targetStatus) {
            throw new BadRequestException("Ticket hiện tại đã ở trạng thái: " + currentStatus);
        }

        if (currentStatus == TicketStatus.CLOSED) {
            throw new BadRequestException("Ticket đã đóng, không thể thay đổi trạng thái");
        }

        // Kiểm tra State Machine & Resource-level Authorization
        ticketAuthorizationService.checkCanChangeStatus(ticket, targetStatus, SecurityUtils.getCurrentUserDetails());

        // Quy tắc khi chuyển sang RESOLVED: Bắt buộc phải có Resolution
        if (targetStatus == TicketStatus.RESOLVED) {
            if (!StringUtils.hasText(request.getResolution())) {
                throw new BadRequestException("Nội dung giải pháp (resolution) là bắt buộc khi đánh dấu Ticket đã giải quyết");
            }
            ticket.setResolution(request.getResolution().trim());
            ticket.setResolvedAt(LocalDateTime.now());

            // Gửi thông báo cho người tạo nghiệm thu
            notificationService.createNotification(
                    ticket.getCreatedBy(),
                    "Ticket đã giải quyết: " + ticket.getTicketCode(),
                    "Kỹ thuật viên đã xử lý xong yêu cầu của bạn. Vui lòng kiểm tra và xác nhận đóng ticket.",
                    "TICKET_RESOLVED",
                    ticket.getId()
            );
        }

        // Quy tắc khi chuyển sang CLOSED: Cập nhật thời điểm đóng
        if (targetStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(LocalDateTime.now());
            if (ticket.getAssignedTo() != null) {
                notificationService.createNotification(
                        ticket.getAssignedTo(),
                        "Ticket đã đóng: " + ticket.getTicketCode(),
                        "Người tạo đã xác nhận nghiệm thu và đóng yêu cầu.",
                        "TICKET_CLOSED",
                        ticket.getId()
                );
            }
        }

        ticket.setStatus(targetStatus);
        ticket = ticketRepository.save(ticket);

        // Lưu Audit Log
        String desc = StringUtils.hasText(request.getNote()) 
                ? request.getNote() 
                : "Chuyển trạng thái từ " + currentStatus + " sang " + targetStatus;
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(currentUser)
                .action("STATUS_CHANGED")
                .oldStatus(currentStatus.name())
                .newStatus(targetStatus.name())
                .description(desc)
                .build();
        historyRepository.save(history);

        return TicketResponse.fromEntity(ticket);
    }

    @Override
    @Transactional
    public TicketResponse reopenTicket(Long id, ReopenTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        // Kiểm tra Khóa lạc quan (Optimistic Locking)
        if (!Objects.equals(ticket.getVersion(), request.getVersion())) {
            throw new ConflictException("Ticket vừa được cập nhật bởi một người khác. Vui lòng tải lại trang và thực hiện lại.");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        // Kiểm tra Resource-level Authorization & điều kiện 3 ngày cho Reopen
        ticketAuthorizationService.checkCanReopen(ticket, SecurityUtils.getCurrentUserDetails());

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(TicketStatus.REOPENED);
        ticket = ticketRepository.save(ticket);

        // Lưu Audit Log
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(currentUser)
                .action("REOPENED")
                .oldStatus(oldStatus.name())
                .newStatus(TicketStatus.REOPENED.name())
                .description("Yêu cầu mở lại: " + request.getReason())
                .build();
        historyRepository.save(history);

        // Gửi thông báo cho Kỹ thuật viên phụ trách & Quản lý
        if (ticket.getAssignedTo() != null) {
            notificationService.createNotification(
                    ticket.getAssignedTo(),
                    "Ticket mở lại: " + ticket.getTicketCode(),
                    "Người dùng yêu cầu xử lý lại với lý do: " + request.getReason(),
                    "TICKET_REOPENED",
                    ticket.getId()
            );
        }
        notificationService.notifyManagers(
                "Ticket mở lại: " + ticket.getTicketCode(),
                "Ticket " + ticket.getTicketCode() + " đã được người dùng mở lại.",
                "TICKET_REOPENED",
                ticket.getId()
        );

        return TicketResponse.fromEntity(ticket);
    }
}
