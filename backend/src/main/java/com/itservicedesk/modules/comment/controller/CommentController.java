package com.itservicedesk.modules.comment.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.modules.comment.dto.CommentResponse;
import com.itservicedesk.modules.comment.dto.CreateCommentRequest;
import com.itservicedesk.modules.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
@Tag(name = "7. Comment Management", description = "Quản lý trao đổi, thảo luận trực tiếp trên từng Ticket")
public class CommentController {

    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "Xem danh sách bình luận của Ticket", description = "Chỉ cho phép Manager, Người tạo hoặc Kỹ thuật viên phụ trách")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getTicketComments(ticketId)));
    }

    @PostMapping
    @Operation(summary = "Đăng bình luận mới vào Ticket", description = "Tự động gửi thông báo cho các bên liên quan và lưu Audit Log")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateCommentRequest request) {
        CommentResponse response = commentService.addComment(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng bình luận thành công", response));
    }
}
