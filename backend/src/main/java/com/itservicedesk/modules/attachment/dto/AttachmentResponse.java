package com.itservicedesk.modules.attachment.dto;

import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private Long id;
    private Long ticketId;
    private Long uploadedById;
    private String uploadedByName;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;

    public static AttachmentResponse fromEntity(TicketAttachment entity) {
        return AttachmentResponse.builder()
                .id(entity.getId())
                .ticketId(entity.getTicket().getId())
                .uploadedById(entity.getUploadedBy().getId())
                .uploadedByName(entity.getUploadedBy().getFullName())
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
