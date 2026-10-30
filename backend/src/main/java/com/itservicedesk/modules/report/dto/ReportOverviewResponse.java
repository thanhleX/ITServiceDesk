package com.itservicedesk.modules.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportOverviewResponse {

    private long totalTickets;
    private long openTickets;
    private long assignedTickets;
    private long inProgressTickets;
    private long pendingTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long overdueTickets;
    private double slaComplianceRate; // % tuân thủ SLA (Ticket hoàn thành đúng hạn / Tổng ticket đã giải quyết)
}
