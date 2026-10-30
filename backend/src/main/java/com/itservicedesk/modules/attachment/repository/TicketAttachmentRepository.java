package com.itservicedesk.modules.attachment.repository;

import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {

    List<TicketAttachment> findByTicketIdOrderByCreatedAtDesc(Long ticketId);

    long countByTicketId(Long ticketId);
}
