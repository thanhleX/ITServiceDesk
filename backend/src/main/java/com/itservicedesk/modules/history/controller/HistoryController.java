package com.itservicedesk.modules.history.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.common.util.SecurityUtils;
import com.itservicedesk.modules.history.dto.HistoryResponse;
import com.itservicedesk.modules.history.repository.TicketHistoryRepository;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.repository.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@RequiredArgsConstructor
@Tag(name = "9. Ticket History Management", description = "Truy xuất lịch sử Audit Log chi tiết của từng Ticket")
public class HistoryController {

    private final TicketHistoryRepository historyRepository;
    private final TicketRepository ticketRepository;
    private final com.itservicedesk.modules.ticket.service.TicketAuthorizationService ticketAuthorizationService;

    @GetMapping
    @Operation(summary = "Xem lịch sử thay đổi của Ticket (Audit Log)", description = "Danh sách được sắp xếp theo thời gian mới nhất trước")
    public ResponseEntity<ApiResponse<List<HistoryResponse>>> getHistory(@PathVariable Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        // Kiểm tra quyền xem lịch sử (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanViewHistory(ticket, SecurityUtils.getCurrentUserDetails());

        List<HistoryResponse> histories = historyRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(HistoryResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(histories));
    }
}
