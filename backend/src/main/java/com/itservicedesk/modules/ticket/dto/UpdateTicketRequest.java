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
public class UpdateTicketRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 200, message = "Tiêu đề không quá 200 ký tự")
    @Schema(example = "Màn hình Dell không lên nguồn sau khi cắm lại dây")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    @Schema(example = "Mô tả chi tiết cập nhật lại hiện tượng...")
    private String description;

    @NotNull(message = "Danh mục sự cố không được để trống")
    @Schema(example = "1")
    private Long categoryId;

    @Schema(example = "HIGH")
    private TicketPriority priority;

    @NotNull(message = "Version không được để trống")
    @Schema(description = "Phiên bản ticket phục vụ Optimistic Locking", example = "0")
    private Integer version;
}
