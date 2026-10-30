package com.itservicedesk.modules.category.dto;

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
public class CategoryRequest {

    @NotBlank(message = "Mã danh mục không được để trống")
    @Size(max = 50, message = "Mã danh mục không được vượt quá 50 ký tự")
    @Schema(example = "NETWORK")
    private String code;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    @Schema(example = "Mạng nội bộ và Internet")
    private String name;

    @Size(max = 255, message = "Mô tả không được vượt quá 255 ký tự")
    @Schema(example = "Các sự cố liên quan đến Wi-Fi, LAN, đường truyền internet")
    private String description;

    @Schema(example = "ACTIVE")
    @Builder.Default
    private String status = "ACTIVE";
}
