package com.itservicedesk.modules.ticket.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.modules.ticket.dto.*;
import com.itservicedesk.modules.ticket.service.TicketService;
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

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "6. Ticket Management", description = "Vòng đời quản lý Ticket, Phân công, Chuyển trạng thái và Nghiệm thu")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @Operation(summary = "Tạo mới yêu cầu hỗ trợ (Ticket)", description = "Tự động sinh mã TCK-YYYY-XXXXX và tính due_at theo SLA mức độ ưu tiên")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketResponse response = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo yêu cầu hỗ trợ thành công", response));
    }

    @GetMapping
    @Operation(summary = "Xem danh sách Ticket (Có phân trang, bộ lọc và phân quyền dữ liệu theo vai trò)")
    public ResponseEntity<ApiResponse<PagedResponse<TicketResponse>>> getTickets(
            @ModelAttribute TicketFilterCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort sortOrder = Sort.by(
                sort.length > 1 && sort[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sort[0]
        );
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        return ResponseEntity.ok(ApiResponse.success(ticketService.getTickets(criteria, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết Ticket", description = "Kiểm tra quyền truy cập: Manager hoặc Người tạo hoặc Kỹ thuật viên được giao")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicketDetail(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Chỉnh sửa thông tin Ticket (Chỉ Creator khi OPEN hoặc Manager, chống IDOR)", description = "Ngăn chặn việc sửa Ticket của người khác hoặc sửa khi đã bắt đầu xử lý")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketRequest request) {
        TicketResponse response = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin Ticket thành công", response));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasRole('IT_MANAGER')")
    @Operation(summary = "Phân công Kỹ thuật viên xử lý Ticket (Chỉ IT Manager, có Optimistic Locking)", description = "Cập nhật assignedTo, chuyển trạng thái sang ASSIGNED, lưu lịch sử phân công và thông báo")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody AssignTicketRequest request) {
        TicketResponse response = ticketService.assignTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success("Phân công Ticket thành công", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái Ticket (Workflow State Machine)", description = "Kiểm tra chặt chẽ luồng trạng thái, yêu cầu resolution khi chuyển RESOLVED")
    public ResponseEntity<ApiResponse<TicketResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {
        TicketResponse response = ticketService.updateTicketStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", response));
    }

    @PostMapping("/{id}/reopen")
    @Operation(summary = "Yêu cầu mở lại Ticket đã giải quyết (Thời hạn trong 3 ngày)", description = "Chỉ cho phép người tạo mở lại khi Ticket đang RESOLVED trong vòng 72 giờ")
    public ResponseEntity<ApiResponse<TicketResponse>> reopenTicket(
            @PathVariable Long id,
            @Valid @RequestBody ReopenTicketRequest request) {
        TicketResponse response = ticketService.reopenTicket(id, request);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu mở lại Ticket thành công", response));
    }
}
