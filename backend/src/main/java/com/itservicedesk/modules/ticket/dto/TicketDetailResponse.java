package com.itservicedesk.modules.ticket.dto;

import com.itservicedesk.modules.assignment.dto.AssignmentResponse;
import com.itservicedesk.modules.history.dto.HistoryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailResponse {

    private TicketResponse ticket;
    private List<AssignmentResponse> assignments;
    private List<HistoryResponse> histories;
    private long totalComments;
    private long totalAttachments;
}
