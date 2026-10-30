package com.itservicedesk.modules.category.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.modules.category.dto.CategoryRequest;
import com.itservicedesk.modules.category.dto.CategoryResponse;
import com.itservicedesk.modules.category.service.CategoryService;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "3. Category Management", description = "Quản lý danh mục sự cố IT (Hardware, Software, Network, Account, Printer)")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Lấy danh sách danh mục sự cố", description = "Mặc định trả về các danh mục đang ACTIVE phục vụ form tạo ticket")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories(
            @RequestParam(required = false, defaultValue = "false") boolean all) {
        List<CategoryResponse> categories = all ? categoryService.getAllCategories() : categoryService.getActiveCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết danh mục theo ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Tạo mới danh mục sự cố (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo danh mục thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Cập nhật danh mục sự cố (Chỉ IT Manager)")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật danh mục thành công", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Đổi trạng thái hoạt động danh mục (ACTIVE/INACTIVE)")
    public ResponseEntity<ApiResponse<Void>> changeStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        categoryService.changeCategoryStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", null));
    }
}
