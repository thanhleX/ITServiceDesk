package com.itservicedesk.modules.ticket.dto;

import com.itservicedesk.modules.ticket.entity.TicketStatus;
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
public class UpdateTicketStatusRequest {

    @NotNull(message = "Trạng thái đích không được để trống")
    @Schema(description = "Trạng thái muốn chuyển sang", example = "IN_PROGRESS")
    private TicketStatus targetStatus;

    @Schema(description = "Nội dung giải pháp xử lý (Bắt buộc khi chuyển RESOLVED)", example = "Đã thay cáp HDMI mới và kiểm tra nguồn cấp, màn hình hiển thị bình thường")
    private String resolution;

    @Schema(description = "Ghi chú bổ sung", example = "Đã liên hệ người dùng kiểm tra trực tiếp tại bàn làm việc")
    private String note;

    @NotNull(message = "Version không được để trống")
    @Schema(description = "Phiên bản ticket phục vụ Optimistic Locking", example = "1")
    private Integer version;
}
