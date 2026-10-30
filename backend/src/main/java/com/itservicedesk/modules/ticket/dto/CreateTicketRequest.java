package com.itservicedesk.modules.ticket.dto;

import com.itservicedesk.modules.ticket.entity.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @NotBlank(message = "Tiêu đề yêu cầu không được để trống")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    @Schema(example = "Màn hình máy tính bàn không lên nguồn")
    private String title;

    @NotBlank(message = "Mô tả chi tiết không được để trống")
    @Schema(example = "Sáng nay bật máy thì case vẫn sáng đèn nhưng màn hình Dell báo No Signal. Đã thử cắm lại dây nguồn và HDMI.")
    private String description;

    @NotNull(message = "Vui lòng chọn danh mục sự cố")
    @Schema(example = "1")
    private Long categoryId;

    @NotNull(message = "Vui lòng chọn mức độ ưu tiên")
    @Schema(example = "MEDIUM")
    private TicketPriority priority;
}
