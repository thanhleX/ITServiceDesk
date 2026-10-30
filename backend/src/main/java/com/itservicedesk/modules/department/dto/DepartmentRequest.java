package com.itservicedesk.modules.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    @NotBlank(message = "Mã phòng ban không được để trống")
    @Size(max = 50, message = "Mã phòng ban không được vượt quá 50 ký tự")
    @Schema(example = "FIN")
    private String code;

    @NotBlank(message = "Tên phòng ban không được để trống")
    @Size(max = 150, message = "Tên phòng ban không được vượt quá 150 ký tự")
    @Schema(example = "Phòng Tài chính")
    private String name;

    @Schema(example = "ACTIVE")
    @Builder.Default
    private String status = "ACTIVE";
}
