package com.itservicedesk.modules.attachment.service;

import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.common.util.SecurityUtils;
import com.itservicedesk.modules.attachment.dto.AttachmentResponse;
import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import com.itservicedesk.modules.attachment.repository.TicketAttachmentRepository;
import com.itservicedesk.modules.history.entity.TicketHistory;
import com.itservicedesk.modules.history.repository.TicketHistoryRepository;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.repository.TicketRepository;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final TicketAttachmentRepository attachmentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketHistoryRepository historyRepository;
    private final FileStorageService fileStorageService;
    private final com.itservicedesk.modules.ticket.service.TicketAuthorizationService ticketAuthorizationService;

    @Override
    @Transactional
    public AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        // Kiểm tra quyền tải file lên tài nguyên (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanUploadAttachment(ticket, SecurityUtils.getCurrentUserDetails());

        // Lưu file vào ổ cứng local
        FileStorageService.StoredFileMetadata metadata = fileStorageService.storeFile(file);

        // Lưu metadata vào Database
        TicketAttachment attachment = TicketAttachment.builder()
                .ticket(ticket)
                .uploadedBy(currentUser)
                .fileName(metadata.originalFileName())
                .storedFileName(metadata.storedFileName())
                .filePath(metadata.filePath())
                .contentType(metadata.contentType())
                .fileSize(metadata.fileSize())
                .build();
        attachment = attachmentRepository.save(attachment);

        // Ghi Audit Log
        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(currentUser)
                .action("ATTACHMENT_UPLOADED")
                .description("Đính kèm tập tin: " + metadata.originalFileName())
                .build();
        historyRepository.save(history);

        return AttachmentResponse.fromEntity(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getTicketAttachments(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        // Kiểm tra quyền xem file của Ticket cha (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanViewTicket(ticket, SecurityUtils.getCurrentUserDetails());

        return attachmentRepository.findByTicketIdOrderByCreatedAtDesc(ticketId).stream()
                .map(AttachmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId) {
        TicketAttachment attachment = getAttachmentEntity(attachmentId);

        // Kiểm tra quyền tải file (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanDownloadAttachment(attachment, SecurityUtils.getCurrentUserDetails());

        return fileStorageService.loadFileAsResource(attachment.getStoredFileName());
    }

    @Override
    @Transactional(readOnly = true)
    public TicketAttachment getAttachmentEntity(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        TicketAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", "id", attachmentId));

        // Kiểm tra quyền xóa file (Resource-level Authorization - Anti IDOR)
        ticketAuthorizationService.checkCanDeleteAttachment(attachment, SecurityUtils.getCurrentUserDetails());

        // Xóa file trên đĩa
        fileStorageService.deleteFile(attachment.getStoredFileName());

        // Ghi Audit Log trước khi xóa bản ghi
        Ticket ticket = attachment.getTicket();
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUserId));

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .performedBy(currentUser)
                .action("ATTACHMENT_DELETED")
                .description("Xóa tập tin đính kèm: " + attachment.getFileName())
                .build();
        historyRepository.save(history);

        // Xóa bản ghi trong database
        attachmentRepository.delete(attachment);
    }
}
