package com.itservicedesk.modules.report.service;

import com.itservicedesk.modules.report.dto.ChartDataResponse;
import com.itservicedesk.modules.report.dto.ReportFilterRequest;
import com.itservicedesk.modules.report.dto.ReportOverviewResponse;
import com.itservicedesk.modules.report.dto.StaffPerformanceResponse;

import java.util.List;

public interface ReportService {

    ReportOverviewResponse getOverview(ReportFilterRequest filter);

    List<ChartDataResponse> getReportByStatus(ReportFilterRequest filter);

    List<ChartDataResponse> getReportByPriority(ReportFilterRequest filter);

    List<ChartDataResponse> getReportByCategory(ReportFilterRequest filter);

    List<StaffPerformanceResponse> getReportByStaff(ReportFilterRequest filter);
}
