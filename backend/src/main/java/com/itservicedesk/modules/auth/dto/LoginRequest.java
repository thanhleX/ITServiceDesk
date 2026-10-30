package com.itservicedesk.modules.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Tên đăng nhập hoặc email không được để trống")
    @Schema(example = "manager")
    private String usernameOrEmail;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Schema(example = "Password@123")
    private String password;
}
