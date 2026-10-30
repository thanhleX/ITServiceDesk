package com.itservicedesk.modules.ticket.service;

import com.itservicedesk.common.exception.BadRequestException;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import com.itservicedesk.security.user.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TicketAuthorizationServiceImpl implements TicketAuthorizationService {

    @Override
    public void checkCanViewTicket(Ticket ticket, CustomUserDetails user) {
        if (!hasRelationWithTicket(ticket, user)) {
            // Stealth Policy: Trả về 404 Not Found để chống tấn công rà quét (IDOR Scanning / Enumeration)
            throw new ResourceNotFoundException("Ticket", "id", ticket.getId());
        }
    }

    @Override
    public void checkCanUpdateTicket(Ticket ticket, CustomUserDetails user) {
        if (isManager(user)) {
            return;
        }

        if (isCreator(ticket, user)) {
            if (ticket.getStatus() != TicketStatus.OPEN) {
                throw new BadRequestException("Bạn chỉ có thể chỉnh sửa nội dung khi yêu cầu đang ở trạng thái Mới tạo (OPEN)");
            }
            return;
        }

        throw new AccessDeniedException("Bạn không có quyền chỉnh sửa thông tin Ticket này");
    }

    @Override
    public void checkCanAssignTicket(Ticket ticket, CustomUserDetails user) {
        if (!isManager(user)) {
            throw new AccessDeniedException("Chỉ Trưởng phòng IT (IT_MANAGER) mới có quyền phân công xử lý Ticket");
        }
    }

    @Override
    public void checkCanChangeStatus(Ticket ticket, TicketStatus targetStatus, CustomUserDetails user) {
        if (isManager(user)) {
            return; // IT Manager có toàn quyền điều phối các trạng thái hợp lệ
        }

        TicketStatus current = ticket.getStatus();

        // Kỹ thuật viên được giao xử lý (IT Staff)
        if (isAssignee(ticket, user)) {
            if (current == TicketStatus.ASSIGNED && targetStatus == TicketStatus.IN_PROGRESS) return;
            if (current == TicketStatus.IN_PROGRESS && targetStatus == TicketStatus.PENDING) return;
            if (current == TicketStatus.PENDING && targetStatus == TicketStatus.IN_PROGRESS) return;
            if (current == TicketStatus.IN_PROGRESS && targetStatus == TicketStatus.RESOLVED) return;
            if (current == TicketStatus.REOPENED && targetStatus == TicketStatus.IN_PROGRESS) return;

            throw new BadRequestException(
                    String.format("Kỹ thuật viên không được phép chuyển trạng thái từ %s sang %s", current, targetStatus)
            );
        }

        // Người tạo Ticket (Employee)
        if (isCreator(ticket, user)) {
            if (current == TicketStatus.RESOLVED && targetStatus == TicketStatus.CLOSED) {
                return;
            }
            throw new BadRequestException(
                    String.format("Người tạo không được phép chuyển trạng thái từ %s sang %s", current, targetStatus)
            );
        }

        throw new AccessDeniedException("Bạn không có quyền cập nhật trạng thái của Ticket này");
    }

    @Override
    public void checkCanComment(Ticket ticket, CustomUserDetails user) {
        if (!hasRelationWithTicket(ticket, user)) {
            throw new AccessDeniedException("Bạn không có quyền tham gia bình luận trong Ticket này");
        }
    }

    @Override
    public void checkCanUploadAttachment(Ticket ticket, CustomUserDetails user) {
        if (!hasRelationWithTicket(ticket, user)) {
            throw new AccessDeniedException("Bạn không có quyền tải file đính kèm vào Ticket này");
        }
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Không thể tải thêm file khi Ticket đã đóng");
        }
    }

    @Override
    public void checkCanDownloadAttachment(TicketAttachment attachment, CustomUserDetails user) {
        // Kiểm tra quyền xem trên Ticket cha chứa file này
        checkCanViewTicket(attachment.getTicket(), user);
    }

    @Override
    public void checkCanDeleteAttachment(TicketAttachment attachment, CustomUserDetails user) {
        if (isManager(user)) {
            return;
        }

        boolean isUploader = attachment.getUploadedBy() != null &&
                attachment.getUploadedBy().getId().equals(user.getId());

        if (isUploader) {
            if (attachment.getTicket().getStatus() == TicketStatus.CLOSED) {
                throw new BadRequestException("Không thể xóa file khi Ticket đã đóng");
            }
            return;
        }

        throw new AccessDeniedException("Chỉ người tải lên hoặc Quản lý mới có quyền xóa file đính kèm này");
    }

    @Override
    public void checkCanViewHistory(Ticket ticket, CustomUserDetails user) {
        if (!hasRelationWithTicket(ticket, user)) {
            throw new ResourceNotFoundException("Ticket", "id", ticket.getId());
        }
    }

    @Override
    public void checkCanReopen(Ticket ticket, CustomUserDetails user) {
        if (!isManager(user) && !isCreator(ticket, user)) {
            throw new AccessDeniedException("Chỉ người tạo yêu cầu (hoặc IT Manager) mới có quyền yêu cầu mở lại Ticket");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new BadRequestException("Chỉ có thể yêu cầu mở lại khi Ticket đang ở trạng thái RESOLVED");
        }

        if (ticket.getResolvedAt() == null || LocalDateTime.now().isAfter(ticket.getResolvedAt().plusDays(3))) {
            throw new BadRequestException("Đã quá thời hạn 3 ngày (72 giờ) kể từ khi sự cố được giải quyết. Vui lòng tạo Ticket mới");
        }
    }

    private boolean hasRelationWithTicket(Ticket ticket, CustomUserDetails user) {
        return isManager(user) || isCreator(ticket, user) || isAssignee(ticket, user);
    }

    private boolean isManager(CustomUserDetails user) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_IT_MANAGER"));
    }

    private boolean isCreator(Ticket ticket, CustomUserDetails user) {
        return ticket.getCreatedBy() != null && ticket.getCreatedBy().getId().equals(user.getId());
    }

    private boolean isAssignee(Ticket ticket, CustomUserDetails user) {
        return ticket.getAssignedTo() != null && ticket.getAssignedTo().getId().equals(user.getId());
    }
}
