package com.itservicedesk.modules.attachment.service;

import com.itservicedesk.modules.attachment.dto.AttachmentResponse;
import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentResponse uploadAttachment(Long ticketId, MultipartFile file);

    List<AttachmentResponse> getTicketAttachments(Long ticketId);

    Resource downloadAttachment(Long attachmentId);

    TicketAttachment getAttachmentEntity(Long attachmentId);

    void deleteAttachment(Long attachmentId);
}
