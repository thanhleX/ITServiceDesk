package com.itservicedesk.modules.assignment.dto;

import com.itservicedesk.modules.assignment.entity.TicketAssignment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private Long id;
    private Long ticketId;
    private Long assignedById;
    private String assignedByName;
    private Long assignedToId;
    private String assignedToName;
    private String note;
    private LocalDateTime assignedAt;

    public static AssignmentResponse fromEntity(TicketAssignment entity) {
        return AssignmentResponse.builder()
                .id(entity.getId())
                .ticketId(entity.getTicket().getId())
                .assignedById(entity.getAssignedBy().getId())
                .assignedByName(entity.getAssignedBy().getFullName())
                .assignedToId(entity.getAssignedTo().getId())
                .assignedToName(entity.getAssignedTo().getFullName())
                .note(entity.getNote())
                .assignedAt(entity.getAssignedAt())
                .build();
    }
}
