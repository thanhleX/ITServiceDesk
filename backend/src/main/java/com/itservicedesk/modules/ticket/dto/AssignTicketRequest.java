package com.itservicedesk.modules.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTicketRequest {

    @NotNull(message = "Vui lòng chọn nhân viên IT phụ trách")
    @Schema(description = "ID của Kỹ thuật viên IT", example = "2")
    private Long assigneeId;

    @Schema(description = "Ghi chú phân công", example = "Ưu tiên xử lý trong buổi sáng nay do nhân sự cần máy làm việc")
    private String note;

    @NotNull(message = "Version không được để trống (Optimistic Locking)")
    @Schema(description = "Phiên bản hiện tại của Ticket để phòng tránh xung đột dữ liệu", example = "0")
    private Integer version;
}
