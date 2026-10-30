package com.itservicedesk.modules.report.service;

import com.itservicedesk.modules.category.entity.Category;
import com.itservicedesk.modules.category.repository.CategoryRepository;
import com.itservicedesk.modules.report.dto.ChartDataResponse;
import com.itservicedesk.modules.report.dto.ReportFilterRequest;
import com.itservicedesk.modules.report.dto.ReportOverviewResponse;
import com.itservicedesk.modules.report.dto.StaffPerformanceResponse;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketPriority;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import com.itservicedesk.modules.ticket.repository.TicketRepository;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TicketRepository ticketRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportOverviewResponse getOverview(ReportFilterRequest filter) {
        List<Ticket> tickets = getFilteredTickets(filter);
        LocalDateTime now = LocalDateTime.now();

        long total = tickets.size();
        long open = tickets.stream().filter(t -> t.getStatus() == TicketStatus.OPEN).count();
        long assigned = tickets.stream().filter(t -> t.getStatus() == TicketStatus.ASSIGNED).count();
        long inProgress = tickets.stream().filter(t -> t.getStatus() == TicketStatus.IN_PROGRESS).count();
        long pending = tickets.stream().filter(t -> t.getStatus() == TicketStatus.PENDING).count();
        long resolved = tickets.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED).count();
        long closed = tickets.stream().filter(t -> t.getStatus() == TicketStatus.CLOSED).count();

        long overdue = tickets.stream()
                .filter(t -> t.getStatus() != TicketStatus.RESOLVED && t.getStatus() != TicketStatus.CLOSED)
                .filter(t -> t.getDueAt() != null && now.isAfter(t.getDueAt()))
                .count();

        // Đo lường % tuân thủ SLA trong các ticket đã hoàn thành
        List<Ticket> completedTickets = tickets.stream()
                .filter(t -> t.getResolvedAt() != null)
                .collect(Collectors.toList());

        double complianceRate = 100.0;
        if (!completedTickets.isEmpty()) {
            long onTimeCount = completedTickets.stream()
                    .filter(t -> !t.getResolvedAt().isAfter(t.getDueAt()))
                    .count();
            complianceRate = Math.round((onTimeCount * 100.0 / completedTickets.size()) * 10.0) / 10.0;
        }

        return ReportOverviewResponse.builder()
                .totalTickets(total)
                .openTickets(open)
                .assignedTickets(assigned)
                .inProgressTickets(inProgress)
                .pendingTickets(pending)
                .resolvedTickets(resolved)
                .closedTickets(closed)
                .overdueTickets(overdue)
                .slaComplianceRate(complianceRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChartDataResponse> getReportByStatus(ReportFilterRequest filter) {
        List<Ticket> tickets = getFilteredTickets(filter);
        long total = tickets.size();

        Map<TicketStatus, Long> counts = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getStatus, Collectors.counting()));

        List<ChartDataResponse> results = new ArrayList<>();
        for (TicketStatus status : TicketStatus.values()) {
            long count = counts.getOrDefault(status, 0L);
            double percentage = total > 0 ? Math.round((count * 100.0 / total) * 10.0) / 10.0 : 0.0;
            results.add(ChartDataResponse.builder()
                    .key(status.name())
                    .label(status.getDescription())
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChartDataResponse> getReportByPriority(ReportFilterRequest filter) {
        List<Ticket> tickets = getFilteredTickets(filter);
        long total = tickets.size();

        Map<TicketPriority, Long> counts = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getPriority, Collectors.counting()));

        List<ChartDataResponse> results = new ArrayList<>();
        for (TicketPriority priority : TicketPriority.values()) {
            long count = counts.getOrDefault(priority, 0L);
            double percentage = total > 0 ? Math.round((count * 100.0 / total) * 10.0) / 10.0 : 0.0;
            results.add(ChartDataResponse.builder()
                    .key(priority.name())
                    .label(priority.getDescription())
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChartDataResponse> getReportByCategory(ReportFilterRequest filter) {
        List<Ticket> tickets = getFilteredTickets(filter);
        long total = tickets.size();

        Map<Long, Long> counts = tickets.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory().getId(), Collectors.counting()));

        List<Category> categories = categoryRepository.findAll();
        List<ChartDataResponse> results = new ArrayList<>();

        for (Category cat : categories) {
            long count = counts.getOrDefault(cat.getId(), 0L);
            double percentage = total > 0 ? Math.round((count * 100.0 / total) * 10.0) / 10.0 : 0.0;
            results.add(ChartDataResponse.builder()
                    .key(cat.getCode())
                    .label(cat.getName())
                    .count(count)
                    .percentage(percentage)
                    .build());
        }
        return results;
    }

    @Override
    @Transactional(readOnly = true)
    public List<StaffPerformanceResponse> getReportByStaff(ReportFilterRequest filter) {
        List<Ticket> tickets = getFilteredTickets(filter);
        LocalDateTime now = LocalDateTime.now();

        // Lấy danh sách toàn bộ Kỹ thuật viên IT
        List<User> staffs = userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_IT_STAFF".equalsIgnoreCase(r.getCode())))
                .collect(Collectors.toList());

        List<StaffPerformanceResponse> results = new ArrayList<>();

        for (User staff : staffs) {
            List<Ticket> staffTickets = tickets.stream()
                    .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(staff.getId()))
                    .collect(Collectors.toList());

            long totalAssigned = staffTickets.size();
            long totalResolved = staffTickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.RESOLVED || t.getStatus() == TicketStatus.CLOSED)
                    .count();
            long totalClosed = staffTickets.stream()
                    .filter(t -> t.getStatus() == TicketStatus.CLOSED)
                    .count();

            // Tính thời gian xử lý trung bình theo giờ
            List<Ticket> resolvedList = staffTickets.stream()
                    .filter(t -> t.getResolvedAt() != null)
                    .collect(Collectors.toList());

            double avgHours = 0.0;
            if (!resolvedList.isEmpty()) {
                double totalHours = resolvedList.stream()
                        .mapToDouble(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes() / 60.0)
                        .sum();
                avgHours = Math.round((totalHours / resolvedList.size()) * 10.0) / 10.0;
            }

            // Tính số ticket bị quá hạn SLA
            long slaBreaches = staffTickets.stream()
                    .filter(t -> {
                        if (t.getResolvedAt() != null) {
                            return t.getResolvedAt().isAfter(t.getDueAt());
                        } else {
                            return now.isAfter(t.getDueAt());
                        }
                    })
                    .count();

            results.add(StaffPerformanceResponse.builder()
                    .staffId(staff.getId())
                    .staffName(staff.getFullName())
                    .staffEmail(staff.getEmail())
                    .totalAssigned(totalAssigned)
                    .totalResolved(totalResolved)
                    .totalClosed(totalClosed)
                    .avgResolutionHours(avgHours)
                    .slaBreaches(slaBreaches)
                    .build());
        }

        return results;
    }

    private List<Ticket> getFilteredTickets(ReportFilterRequest filter) {
        if (filter == null || (filter.getFromDate() == null && filter.getToDate() == null)) {
            return ticketRepository.findAll();
        }

        LocalDateTime start = filter.getFromDate() != null ? filter.getFromDate().atStartOfDay() : LocalDateTime.MIN;
        LocalDateTime end = filter.getToDate() != null ? filter.getToDate().atTime(LocalTime.MAX) : LocalDateTime.MAX;

        return ticketRepository.findAll().stream()
                .filter(t -> !t.getCreatedAt().isBefore(start) && !t.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());
    }
}
