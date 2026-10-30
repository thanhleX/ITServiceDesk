package com.itservicedesk.modules.auth.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.modules.auth.dto.ChangePasswordRequest;
import com.itservicedesk.modules.auth.dto.LoginRequest;
import com.itservicedesk.modules.auth.dto.LoginResponse;
import com.itservicedesk.modules.auth.dto.UserProfileResponse;
import com.itservicedesk.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "Các API xác thực, đăng nhập và quản lý tài khoản cá nhân")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Xác thực bằng username hoặc email kết hợp password, trả về Bearer JWT Token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Đăng xuất khỏi hệ thống", description = "Hủy phiên làm việc phía client (Client xóa Bearer Token khỏi Storage)")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy thông tin người dùng hiện tại", description = "Lấy thông tin profile, danh sách Roles và Permissions của người dùng đang đăng nhập")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser() {
        UserProfileResponse response = authService.getCurrentUserProfile();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Người dùng tự cập nhật mật khẩu mới")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Đổi mật khẩu thành công", null));
    }
}
