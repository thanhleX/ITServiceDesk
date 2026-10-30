package com.itservicedesk.modules.user.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.common.exception.UnauthorizedException;
import com.itservicedesk.common.util.SecurityUtils;
import com.itservicedesk.modules.user.dto.CreateUserRequest;
import com.itservicedesk.modules.user.dto.UpdateUserRequest;
import com.itservicedesk.modules.user.dto.UserFilterCriteria;
import com.itservicedesk.modules.user.dto.UserResponse;
import com.itservicedesk.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "4. User Management", description = "Quản lý tài khoản nhân viên, phân quyền vai trò và phòng ban (Dành cho IT Manager)")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Tìm kiếm và phân trang danh sách người dùng (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @ModelAttribute UserFilterCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort) {

        Sort sortOrder = Sort.by(
                sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sort[0]
        );
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        return ResponseEntity.ok(ApiResponse.success(userService.getUsers(criteria, pageable)));
    }

    @GetMapping("/staffs")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Lấy danh sách Kỹ thuật viên IT active để phân công (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getActiveStaffs() {
        return ResponseEntity.ok(ApiResponse.success(userService.getActiveStaffs()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết người dùng theo ID (Manager hoặc chính chủ)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!SecurityUtils.isManager() && !currentUserId.equals(id)) {
            throw new UnauthorizedException("Bạn không có quyền xem thông tin tài khoản người dùng khác");
        }
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Tạo mới tài khoản người dùng (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo tài khoản thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Cập nhật tài khoản người dùng (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tài khoản thành công", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Khóa hoặc mở khóa tài khoản người dùng (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        userService.changeUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái tài khoản thành công", null));
    }
}
