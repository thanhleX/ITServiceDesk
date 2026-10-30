package com.itservicedesk.modules.history.dto;

import com.itservicedesk.modules.history.entity.TicketHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {

    private Long id;
    private Long ticketId;
    private Long performedById;
    private String performedByName;
    private String action;
    private String oldStatus;
    private String newStatus;
    private String description;
    private LocalDateTime createdAt;

    public static HistoryResponse fromEntity(TicketHistory entity) {
        return HistoryResponse.builder()
                .id(entity.getId())
                .ticketId(entity.getTicket().getId())
                .performedById(entity.getPerformedBy().getId())
                .performedByName(entity.getPerformedBy().getFullName())
                .action(entity.getAction())
                .oldStatus(entity.getOldStatus())
                .newStatus(entity.getNewStatus())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
