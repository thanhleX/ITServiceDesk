package com.itservicedesk.modules.department.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.modules.department.dto.DepartmentRequest;
import com.itservicedesk.modules.department.dto.DepartmentResponse;
import com.itservicedesk.modules.department.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@Tag(name = "2. Department Management", description = "Quản lý danh sách và trạng thái phòng ban doanh nghiệp")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả phòng ban")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getAllDepartments()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem thông tin phòng ban theo ID")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(departmentService.getDepartmentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Tạo phòng ban mới (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phòng ban thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Cập nhật thông tin phòng ban (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật phòng ban thành công", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Thay đổi trạng thái phòng ban (ACTIVE/INACTIVE)")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        departmentService.changeDepartmentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", null));
    }
}
