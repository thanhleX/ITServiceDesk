package com.itservicedesk.modules.ticket.service;

import com.itservicedesk.common.exception.BadRequestException;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.modules.attachment.entity.TicketAttachment;
import com.itservicedesk.modules.category.entity.Category;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketPriority;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import com.itservicedesk.modules.user.entity.Role;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.security.user.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Kiểm thử Bảo mật Resource-level Authorization và Chống tấn công IDOR")
class TicketAuthorizationSecurityTest {

    private TicketAuthorizationService authService;

    private User employeeA;
    private User employeeB;
    private User staffA;
    private User staffB;
    private User manager;

    private CustomUserDetails userDetailsEmployeeA;
    private CustomUserDetails userDetailsEmployeeB;
    private CustomUserDetails userDetailsStaffA;
    private CustomUserDetails userDetailsStaffB;
    private CustomUserDetails userDetailsManager;

    private Ticket ticketOfEmployeeA;
    private Ticket ticketOfEmployeeB;
    private Ticket ticketAssignedToStaffA;
    private Ticket ticketAssignedToStaffB;

    @BeforeEach
    void setUp() {
        authService = new TicketAuthorizationServiceImpl();

        Role roleEmployee = Role.builder().id(1L).code("ROLE_EMPLOYEE").name("Nhân viên").build();
        Role roleStaff = Role.builder().id(2L).code("ROLE_IT_STAFF").name("Kỹ thuật viên").build();
        Role roleManager = Role.builder().id(3L).code("ROLE_IT_MANAGER").name("Quản lý").build();

        employeeA = User.builder().id(101L).username("emp_a").fullName("Employee A").roles(Set.of(roleEmployee)).build();
        employeeB = User.builder().id(102L).username("emp_b").fullName("Employee B").roles(Set.of(roleEmployee)).build();
        staffA = User.builder().id(201L).username("staff_a").fullName("IT Staff A").roles(Set.of(roleStaff)).build();
        staffB = User.builder().id(202L).username("staff_b").fullName("IT Staff B").roles(Set.of(roleStaff)).build();
        manager = User.builder().id(301L).username("manager").fullName("IT Manager").roles(Set.of(roleManager)).build();

        userDetailsEmployeeA = buildUserDetails(employeeA, "ROLE_EMPLOYEE");
        userDetailsEmployeeB = buildUserDetails(employeeB, "ROLE_EMPLOYEE");
        userDetailsStaffA = buildUserDetails(staffA, "ROLE_IT_STAFF");
        userDetailsStaffB = buildUserDetails(staffB, "ROLE_IT_STAFF");
        userDetailsManager = buildUserDetails(manager, "ROLE_IT_MANAGER");

        Category category = Category.builder().id(1L).code("HARDWARE").name("Phần cứng").build();

        ticketOfEmployeeA = Ticket.builder()
                .id(1L)
                .ticketCode("TCK-2026-00001")
                .title("Ticket của Employee A")
                .description("Mô tả A")
                .category(category)
                .createdBy(employeeA)
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .dueAt(LocalDateTime.now().plusDays(2))
                .build();

        ticketOfEmployeeB = Ticket.builder()
                .id(2L)
                .ticketCode("TCK-2026-00002")
                .title("Ticket của Employee B")
                .description("Mô tả B")
                .category(category)
                .createdBy(employeeB)
                .status(TicketStatus.OPEN)
                .priority(TicketPriority.MEDIUM)
                .dueAt(LocalDateTime.now().plusDays(2))
                .build();

        ticketAssignedToStaffA = Ticket.builder()
                .id(3L)
                .ticketCode("TCK-2026-00003")
                .title("Ticket phân công cho Staff A")
                .description("Mô tả 3")
                .category(category)
                .createdBy(employeeA)
                .assignedTo(staffA)
                .status(TicketStatus.ASSIGNED)
                .priority(TicketPriority.HIGH)
                .dueAt(LocalDateTime.now().plusDays(1))
                .build();

        ticketAssignedToStaffB = Ticket.builder()
                .id(4L)
                .ticketCode("TCK-2026-00004")
                .title("Ticket phân công cho Staff B")
                .description("Mô tả 4")
                .category(category)
                .createdBy(employeeB)
                .assignedTo(staffB)
                .status(TicketStatus.ASSIGNED)
                .priority(TicketPriority.HIGH)
                .dueAt(LocalDateTime.now().plusDays(1))
                .build();
    }

    private CustomUserDetails buildUserDetails(User user, String role) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .status("ACTIVE")
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }

    @Test
    @DisplayName("Kịch bản 1: EMPLOYEE A xem Ticket do chính A tạo -> Cho phép thành công")
    void scenario1_EmployeeA_ViewOwnTicket_ShouldSuccess() {
        assertDoesNotThrow(() -> authService.checkCanViewTicket(ticketOfEmployeeA, userDetailsEmployeeA));
    }

    @Test
    @DisplayName("Kịch bản 2: EMPLOYEE A xem Ticket do B tạo (IDOR) -> Bị từ chối với 404 Not Found (Stealth)")
    void scenario2_EmployeeA_ViewTicketOfB_ShouldThrowNotFound() {
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.checkCanViewTicket(ticketOfEmployeeB, userDetailsEmployeeA)
        );
        assertTrue(ex.getMessage().contains("Ticket không tồn tại với id"));
    }

    @Test
    @DisplayName("Kịch bản 3: EMPLOYEE A sửa Ticket do B tạo -> Bị từ chối với 403 Forbidden")
    void scenario3_EmployeeA_UpdateTicketOfB_ShouldThrowAccessDenied() {
        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> authService.checkCanUpdateTicket(ticketOfEmployeeB, userDetailsEmployeeA)
        );
        assertTrue(ex.getMessage().contains("không có quyền chỉnh sửa"));
    }

    @Test
    @DisplayName("Kịch bản 4: IT_STAFF A xem Ticket được giao cho A -> Cho phép thành công")
    void scenario4_StaffA_ViewAssignedTicket_ShouldSuccess() {
        assertDoesNotThrow(() -> authService.checkCanViewTicket(ticketAssignedToStaffA, userDetailsStaffA));
    }

    @Test
    @DisplayName("Kịch bản 5: IT_STAFF A xem Ticket được giao cho B (IDOR) -> Bị từ chối với 404 Not Found")
    void scenario5_StaffA_ViewTicketAssignedToStaffB_ShouldThrowNotFound() {
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> authService.checkCanViewTicket(ticketAssignedToStaffB, userDetailsStaffA)
        );
        assertTrue(ex.getMessage().contains("Ticket không tồn tại"));
    }

    @Test
    @DisplayName("Kịch bản 6: IT_STAFF A cập nhật trạng thái Ticket không được giao cho mình -> Bị từ chối 403")
    void scenario6_StaffA_UpdateStatusTicketOfStaffB_ShouldThrowAccessDenied() {
        AccessDeniedException ex = assertThrows(
                AccessDeniedException.class,
                () -> authService.checkCanChangeStatus(ticketAssignedToStaffB, TicketStatus.IN_PROGRESS, userDetailsStaffA)
        );
        assertTrue(ex.getMessage().contains("không có quyền cập nhật trạng thái"));
    }

    @Test
    @DisplayName("Kịch bản 7: IT_MANAGER xem Ticket bất kỳ trong hệ thống -> Luôn thành công")
    void scenario7_Manager_ViewAnyTicket_ShouldSuccess() {
        assertDoesNotThrow(() -> authService.checkCanViewTicket(ticketOfEmployeeA, userDetailsManager));
        assertDoesNotThrow(() -> authService.checkCanViewTicket(ticketOfEmployeeB, userDetailsManager));
        assertDoesNotThrow(() -> authService.checkCanViewTicket(ticketAssignedToStaffA, userDetailsManager));
        assertDoesNotThrow(() -> authService.checkCanViewTicket(ticketAssignedToStaffB, userDetailsManager));
    }

    @Test
    @DisplayName("Kịch bản 8: EMPLOYEE A tải Attachment thuộc Ticket của B -> Bị từ chối 404 Not Found")
    void scenario8_EmployeeA_DownloadAttachmentOfTicketB_ShouldThrowNotFound() {
        TicketAttachment attachmentB = TicketAttachment.builder()
                .id(50L)
                .ticket(ticketOfEmployeeB)
                .uploadedBy(employeeB)
                .fileName("secret_document.pdf")
                .storedFileName("uuid-secret.pdf")
                .build();

        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.checkCanDownloadAttachment(attachmentB, userDetailsEmployeeA)
        );
    }

    @Test
    @DisplayName("Kịch bản 9: Người dùng đổi ID trên URL để bình luận/xem lịch sử -> Bị chặn triệt để")
    void scenario9_TamperIdOnUrl_ShouldEnforceResourceLevelAuthorization() {
        // Employee A cố tình gửi comment vào Ticket 2 (của B)
        assertThrows(
                AccessDeniedException.class,
                () -> authService.checkCanComment(ticketOfEmployeeB, userDetailsEmployeeA)
        );

        // Employee A cố tình xem lịch sử Ticket 2 (của B)
        assertThrows(
                ResourceNotFoundException.class,
                () -> authService.checkCanViewHistory(ticketOfEmployeeB, userDetailsEmployeeA)
        );

        // Employee A cố tình tải file lên Ticket 2 (của B)
        assertThrows(
                AccessDeniedException.class,
                () -> authService.checkCanUploadAttachment(ticketOfEmployeeB, userDetailsEmployeeA)
        );
    }

    @Test
    @DisplayName("Kịch bản 10: Quy tắc chuyển trạng thái của IT Staff và Employee tuân thủ State Machine")
    void scenario10_WorkflowRules_ShouldBeEnforcedStrictly() {
        // Staff A được giao ticket 3 (đang ASSIGNED) -> Chuyển sang IN_PROGRESS: Hợp lệ
        assertDoesNotThrow(() -> authService.checkCanChangeStatus(ticketAssignedToStaffA, TicketStatus.IN_PROGRESS, userDetailsStaffA));

        // Staff A cố tình nhảy cóc từ ASSIGNED thẳng lên RESOLVED -> Bị từ chối BadRequestException
        assertThrows(
                BadRequestException.class,
                () -> authService.checkCanChangeStatus(ticketAssignedToStaffA, TicketStatus.RESOLVED, userDetailsStaffA)
        );

        // Employee A (người tạo) cố tình chuyển trạng thái khi ticket chưa RESOLVED -> Bị từ chối
        assertThrows(
                BadRequestException.class,
                () -> authService.checkCanChangeStatus(ticketAssignedToStaffA, TicketStatus.CLOSED, userDetailsEmployeeA)
        );
    }
}
