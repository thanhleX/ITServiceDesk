package com.itservicedesk.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "Họ và tên không được để trống")
    @Size(max = 100, message = "Họ và tên không quá 100 ký tự")
    @Schema(example = "Nguyễn Văn Đức")
    private String fullName;

    @Schema(example = "0912345678")
    private String phone;

    @Schema(example = "1")
    private Long departmentId;

    @NotEmpty(message = "Phải chỉ định ít nhất một vai trò")
    @Schema(example = "[\"ROLE_IT_STAFF\"]")
    private Set<String> roleCodes;

    @Schema(example = "ACTIVE")
    private String status;
}
