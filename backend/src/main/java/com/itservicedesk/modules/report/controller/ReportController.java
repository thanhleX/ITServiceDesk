package com.itservicedesk.modules.report.controller;

import com.itservicedesk.common.dto.ApiResponse;
import com.itservicedesk.modules.report.dto.ChartDataResponse;
import com.itservicedesk.modules.report.dto.ReportFilterRequest;
import com.itservicedesk.modules.report.dto.ReportOverviewResponse;
import com.itservicedesk.modules.report.dto.StaffPerformanceResponse;
import com.itservicedesk.modules.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('IT_MANAGER')")
@Tag(name = "10. Dashboard & Reports", description = "Các chỉ số vận hành Service Desk, Báo cáo SLA và Đánh giá hiệu suất nhân viên (Chỉ IT Manager)")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/overview")
    @Operation(summary = "Báo cáo tổng quan Dashboard (Tổng số, trạng thái, quá hạn, tỷ lệ tuân thủ SLA)")
    public ResponseEntity<ApiResponse<ReportOverviewResponse>> getOverview(@ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getOverview(filter)));
    }

    @GetMapping("/by-status")
    @Operation(summary = "Thống kê phân bổ Ticket theo Trạng thái (Phục vụ biểu đồ tròn)")
    public ResponseEntity<ApiResponse<List<ChartDataResponse>>> getByStatus(@ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportByStatus(filter)));
    }

    @GetMapping("/by-priority")
    @Operation(summary = "Thống kê phân bổ Ticket theo Mức độ ưu tiên (Low, Medium, High, Critical)")
    public ResponseEntity<ApiResponse<List<ChartDataResponse>>> getByPriority(@ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportByPriority(filter)));
    }

    @GetMapping("/by-category")
    @Operation(summary = "Thống kê phân bổ Ticket theo Danh mục sự cố (Hardware, Software, Network...)")
    public ResponseEntity<ApiResponse<List<ChartDataResponse>>> getByCategory(@ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportByCategory(filter)));
    }

    @GetMapping("/by-staff")
    @Operation(summary = "Đánh giá hiệu suất làm việc của từng Kỹ thuật viên IT", description = "Đo lường số lượng nhận, đã giải quyết, đã đóng, thời gian xử lý trung bình và số lỗi SLA")
    public ResponseEntity<ApiResponse<List<StaffPerformanceResponse>>> getByStaff(@ModelAttribute ReportFilterRequest filter) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReportByStaff(filter)));
    }
}
