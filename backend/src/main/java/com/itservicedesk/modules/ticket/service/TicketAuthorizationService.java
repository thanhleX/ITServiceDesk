package com.itservicedesk.modules.ticket.service;

import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import com.itservicedesk.security.user.CustomUserDetails;

public interface TicketAuthorizationService {

    /**
     * Kiểm tra quyền xem chi tiết Ticket.
     * Áp dụng chính sách Stealth Security (ném ResourceNotFoundException - 404)
     * nếu người dùng không có quan hệ với Ticket nhằm chống tấn công dò quét (Enumeration).
     */
    void checkCanViewTicket(Ticket ticket, CustomUserDetails user);

    /**
     * Kiểm tra quyền chỉnh sửa nội dung Ticket (Chỉ Creator khi ticket còn OPEN, hoặc Manager).
     */
    void checkCanUpdateTicket(Ticket ticket, CustomUserDetails user);

    /**
     * Kiểm tra quyền phân công Ticket (Chỉ IT_MANAGER).
     */
    void checkCanAssignTicket(Ticket ticket, CustomUserDetails user);

    /**
     * Kiểm tra quyền chuyển trạng thái Ticket theo quan hệ người dùng với Ticket.
     */
    void checkCanChangeStatus(Ticket ticket, TicketStatus targetStatus, CustomUserDetails user);

    /**
     * Kiểm tra quyền gửi bình luận vào Ticket.
     */
    void checkCanComment(Ticket ticket, CustomUserDetails user);

    /**
     * Kiểm tra quyền tải file đính kèm lên Ticket.
     */
    void checkCanUploadAttachment(Ticket ticket, CustomUserDetails user);

    /**
     * Kiểm tra quyền tải xuống file đính kèm (bao gồm quyền truy cập Ticket cha).
     */
    void checkCanDownloadAttachment(TicketAttachment attachment, CustomUserDetails user);

    /**
     * Kiểm tra quyền xóa file đính kèm (Người tải lên hoặc Manager).
     */
    void checkCanDeleteAttachment(TicketAttachment attachment, CustomUserDetails user);

    /**
     * Kiểm tra quyền xem lịch sử xử lý (Audit History) của Ticket.
     */
    void checkCanViewHistory(Ticket ticket, CustomUserDetails user);

    /**
     * Kiểm tra quyền yêu cầu mở lại Ticket (Chỉ Creator khi ticket đang RESOLVED trong vòng 3 ngày, hoặc Manager).
     */
    void checkCanReopen(Ticket ticket, CustomUserDetails user);
}
