package com.itservicedesk.modules.ticket.dto;

import com.itservicedesk.modules.ticket.entity.TicketPriority;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketFilterCriteria {

    @Schema(description = "Từ khóa tìm kiếm theo mã Ticket hoặc tiêu đề", example = "TCK-2026")
    private String keyword;

    @Schema(description = "Lọc theo trạng thái", example = "OPEN")
    private TicketStatus status;

    @Schema(description = "Lọc theo mức độ ưu tiên", example = "HIGH")
    private TicketPriority priority;

    @Schema(description = "Lọc theo danh mục", example = "1")
    private Long categoryId;

    @Schema(description = "Lọc theo người tạo (Chỉ IT Manager dùng được)", example = "4")
    private Long createdById;

    @Schema(description = "Lọc theo kỹ thuật viên xử lý", example = "2")
    private Long assignedToId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Lọc từ ngày (YYYY-MM-DD)", example = "2026-10-01")
    private LocalDate fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Lọc đến ngày (YYYY-MM-DD)", example = "2026-10-31")
    private LocalDate toDate;
}
