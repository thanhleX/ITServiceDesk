package com.itservicedesk.modules.ticket.dto;

import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketPriority;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String ticketCode;
    private String title;
    private String description;
    private Long categoryId;
    private String categoryName;

    // Creator Info
    private Long createdById;
    private String createdByName;
    private String createdByEmail;
    private String departmentName;

    // Assignee Info
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;

    private TicketPriority priority;
    private TicketStatus status;
    private String resolution;

    private LocalDateTime dueAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;

    private boolean isOverdue;
    private Integer version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TicketResponse fromEntity(Ticket ticket) {
        boolean overdue = ticket.getStatus() != TicketStatus.RESOLVED 
                && ticket.getStatus() != TicketStatus.CLOSED 
                && ticket.getDueAt() != null
                && LocalDateTime.now().isAfter(ticket.getDueAt());

        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .categoryId(ticket.getCategory().getId())
                .categoryName(ticket.getCategory().getName())
                .createdById(ticket.getCreatedBy().getId())
                .createdByName(ticket.getCreatedBy().getFullName())
                .createdByEmail(ticket.getCreatedBy().getEmail())
                .departmentName(ticket.getCreatedBy().getDepartment() != null ? ticket.getCreatedBy().getDepartment().getName() : null)
                .assignedToId(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getId() : null)
                .assignedToName(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getFullName() : null)
                .assignedToEmail(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getEmail() : null)
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .resolution(ticket.getResolution())
                .dueAt(ticket.getDueAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .isOverdue(overdue)
                .version(ticket.getVersion())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
