package com.itservicedesk.modules.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReopenTicketRequest {

    @NotBlank(message = "Lý do mở lại không được để trống")
    @Schema(description = "Lý do người dùng chưa nghiệm thu kết quả", example = "Màn hình lại bị chập chờn sau 1 tiếng sử dụng")
    private String reason;

    @NotNull(message = "Version không được để trống")
    @Schema(description = "Phiên bản ticket phục vụ Optimistic Locking", example = "2")
    private Integer version;
}
