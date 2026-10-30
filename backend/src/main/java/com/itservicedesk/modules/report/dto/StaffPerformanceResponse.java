package com.itservicedesk.modules.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffPerformanceResponse {

    private Long staffId;
    private String staffName;
    private String staffEmail;
    private long totalAssigned;
    private long totalResolved;
    private long totalClosed;
    private double avgResolutionHours; // Thời gian xử lý trung bình tính bằng giờ
    private long slaBreaches;          // Số ticket bị trễ hạn SLA
}
