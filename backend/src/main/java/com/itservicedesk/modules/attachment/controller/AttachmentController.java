package com.itservicedesk.modules.attachment.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.modules.attachment.dto.AttachmentResponse;
import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import com.itservicedesk.modules.attachment.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "8. Attachment Management", description = "Quản lý upload, tải về và xóa file đính kèm trong Ticket")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/api/tickets/{ticketId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Tải file đính kèm lên Ticket", description = "Lưu file vào local server, hỗ trợ các định dạng ảnh, pdf, office, txt, zip/rar tối đa 10MB")
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadAttachment(
            @PathVariable Long ticketId,
            @RequestParam("file") MultipartFile file) {
        AttachmentResponse response = attachmentService.uploadAttachment(ticketId, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tải file lên thành công", response));
    }

    @GetMapping("/api/tickets/{ticketId}/attachments")
    @Operation(summary = "Xem danh sách file đính kèm của Ticket")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(@PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(attachmentService.getTicketAttachments(ticketId)));
    }

    @GetMapping("/api/attachments/{id}")
    @Operation(summary = "Tải về hoặc xem trực tiếp file đính kèm theo ID", description = "Kiểm tra quyền truy cập tài nguyên trước khi cho phép tải file")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long id) {
        TicketAttachment attachment = attachmentService.getAttachmentEntity(id);
        Resource resource = attachmentService.downloadAttachment(id);

        String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @DeleteMapping("/api/attachments/{id}")
    @Operation(summary = "Xóa file đính kèm (Người tải lên hoặc Manager)")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa file thành công", null));
    }
}
