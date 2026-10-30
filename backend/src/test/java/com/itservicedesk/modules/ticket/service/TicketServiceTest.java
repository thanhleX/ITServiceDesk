package com.itservicedesk.modules.ticket.service;

import com.itservicedesk.common.exception.BadRequestException;
import com.itservicedesk.common.exception.ConflictException;
import com.itservicedesk.common.util.TicketCodeGenerator;
import com.itservicedesk.modules.assignment.repository.TicketAssignmentRepository;
import com.itservicedesk.modules.attachment.repository.TicketAttachmentRepository;
import com.itservicedesk.modules.category.entity.Category;
import com.itservicedesk.modules.category.repository.CategoryRepository;
import com.itservicedesk.modules.comment.repository.TicketCommentRepository;
import com.itservicedesk.modules.history.repository.TicketHistoryRepository;
import com.itservicedesk.modules.notification.service.NotificationService;
import com.itservicedesk.modules.ticket.dto.*;
import com.itservicedesk.modules.ticket.entity.Ticket;
import com.itservicedesk.modules.ticket.entity.TicketPriority;
import com.itservicedesk.modules.ticket.entity.TicketStatus;
import com.itservicedesk.modules.ticket.repository.TicketRepository;
import com.itservicedesk.modules.user.entity.Role;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import com.itservicedesk.security.user.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TicketAssignmentRepository assignmentRepository;
    @Mock
    private TicketHistoryRepository historyRepository;
    @Mock
    private TicketCommentRepository commentRepository;
    @Mock
    private TicketAttachmentRepository attachmentRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TicketCodeGenerator ticketCodeGenerator;
    @Mock
    private TicketAuthorizationService ticketAuthorizationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User managerUser;
    private User staffUser;
    private User employeeUser;
    private Category hardwareCategory;

    @BeforeEach
    void setUp() {
        Role roleManager = Role.builder().id(1L).code("ROLE_IT_MANAGER").name("Quản lý").build();
        Role roleStaff = Role.builder().id(2L).code("ROLE_IT_STAFF").name("Kỹ thuật viên").build();
        Role roleEmployee = Role.builder().id(3L).code("ROLE_EMPLOYEE").name("Nhân viên").build();

        managerUser = User.builder().id(1L).username("manager").fullName("Nguyen Manager").status("ACTIVE").roles(Set.of(roleManager)).build();
        staffUser = User.builder().id(2L).username("staff_nam").fullName("Tran Nam").status("ACTIVE").roles(Set.of(roleStaff)).build();
        employeeUser = User.builder().id(3L).username("employee_lan").fullName("Pham Lan").status("ACTIVE").roles(Set.of(roleEmployee)).build();

        hardwareCategory = Category.builder().id(1L).code("HARDWARE").name("Phần cứng").status("ACTIVE").build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user, String roleCode) {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .status("ACTIVE")
                .authorities(List.of(new SimpleGrantedAuthority(roleCode)))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("Tạo Ticket thành công: Tự động tính hạn SLA theo Priority và sinh mã TCK")
    void createTicket_WhenValidRequest_ShouldCalculateSlaAndReturnTicket() {
        authenticateAs(employeeUser, "ROLE_EMPLOYEE");

        CreateTicketRequest request = CreateTicketRequest.builder()
                .title("Màn hình không lên nguồn")
                .description("Bật case thì case sáng nhưng màn hình đen")
                .categoryId(1L)
                .priority(TicketPriority.CRITICAL) // SLA: 8 giờ
                .build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(employeeUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(hardwareCategory));
        when(ticketCodeGenerator.generateCode()).thenReturn("TCK-2026-00001");
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(100L);
            t.setCreatedAt(LocalDateTime.now());
            return t;
        });

        TicketResponse response = ticketService.createTicket(request);

        assertNotNull(response);
        assertEquals("TCK-2026-00001", response.getTicketCode());
        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertEquals(TicketPriority.CRITICAL, response.getPriority());
        // Kiểm tra due_at tính khoảng 8 giờ từ hiện tại
        assertTrue(response.getDueAt().isAfter(LocalDateTime.now().plusHours(7)));
        assertTrue(response.getDueAt().isBefore(LocalDateTime.now().plusHours(9)));

        verify(historyRepository, times(1)).save(any());
        verify(notificationService, times(1)).notifyManagers(anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    @DisplayName("Phân công Ticket: Cập nhật assignedTo, chuyển trạng thái sang ASSIGNED và lưu vết lịch sử")
    void assignTicket_WhenValidRequest_ShouldChangeStatusToAssignedAndRecordHistory() {
        authenticateAs(managerUser, "ROLE_IT_MANAGER");

        Ticket ticket = Ticket.builder()
                .id(100L)
                .ticketCode("TCK-2026-00001")
                .title("Sự cố mạng")
                .description("Không kết nối được Wi-Fi")
                .category(hardwareCategory)
                .createdBy(employeeUser)
                .priority(TicketPriority.HIGH)
                .status(TicketStatus.OPEN)
                .dueAt(LocalDateTime.now().plusHours(24))
                .version(0)
                .build();

        AssignTicketRequest request = AssignTicketRequest.builder()
                .assigneeId(2L)
                .note("Nhờ Nam ưu tiên xử lý")
                .version(0)
                .build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staffUser));
        when(userRepository.findById(1L)).thenReturn(Optional.of(managerUser));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.assignTicket(100L, request);

        assertNotNull(response);
        assertEquals(TicketStatus.ASSIGNED, response.getStatus());
        assertEquals(2L, response.getAssignedToId());

        verify(assignmentRepository, times(1)).save(any());
        verify(historyRepository, times(1)).save(any());
        verify(notificationService, times(1)).createNotification(eq(staffUser), anyString(), anyString(), eq("TICKET_ASSIGNED"), eq(100L));
    }

    @Test
    @DisplayName("Phân công Ticket thất bại khi Version không khớp (Optimistic Locking Conflict)")
    void assignTicket_WhenVersionMismatch_ShouldThrowOptimisticLockingConflict() {
        authenticateAs(managerUser, "ROLE_IT_MANAGER");

        Ticket ticket = Ticket.builder()
                .id(100L)
                .ticketCode("TCK-2026-00001")
                .category(hardwareCategory)
                .createdBy(employeeUser)
                .status(TicketStatus.OPEN)
                .version(1) // Version trong DB hiện là 1
                .build();

        AssignTicketRequest request = AssignTicketRequest.builder()
                .assigneeId(2L)
                .version(0) // Client gửi lên version cũ là 0
                .build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));

        assertThrows(ConflictException.class, () -> ticketService.assignTicket(100L, request));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cập nhật trạng thái sang RESOLVED nhưng thiếu Resolution -> Bị ném lỗi BadRequestException")
    void updateTicketStatus_WhenInProgressToResolvedWithoutResolution_ShouldThrowBadRequest() {
        authenticateAs(staffUser, "ROLE_IT_STAFF");

        Ticket ticket = Ticket.builder()
                .id(100L)
                .category(hardwareCategory)
                .createdBy(employeeUser)
                .assignedTo(staffUser)
                .status(TicketStatus.IN_PROGRESS)
                .version(1)
                .build();

        UpdateTicketStatusRequest request = UpdateTicketStatusRequest.builder()
                .targetStatus(TicketStatus.RESOLVED)
                .resolution("") // Resolution để rỗng
                .version(1)
                .build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staffUser));

        assertThrows(BadRequestException.class, () -> ticketService.updateTicketStatus(100L, request));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Yêu cầu mở lại Ticket trong thời hạn 3 ngày -> Thành công chuyển sang REOPENED")
    void reopenTicket_WhenWithin3Days_ShouldChangeStatusToReopened() {
        authenticateAs(employeeUser, "ROLE_EMPLOYEE");

        Ticket ticket = Ticket.builder()
                .id(100L)
                .ticketCode("TCK-2026-00001")
                .category(hardwareCategory)
                .createdBy(employeeUser)
                .assignedTo(staffUser)
                .priority(TicketPriority.MEDIUM)
                .status(TicketStatus.RESOLVED)
                .dueAt(LocalDateTime.now().plusDays(1))
                .resolvedAt(LocalDateTime.now().minusDays(1)) // Giải quyết cách đây 1 ngày (hợp lệ <= 3 ngày)
                .version(2)
                .build();

        ReopenTicketRequest request = ReopenTicketRequest.builder()
                .reason("Màn hình vẫn bị chập chờn khi cắm dây nguồn")
                .version(2)
                .build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(employeeUser));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.reopenTicket(100L, request);

        assertNotNull(response);
        assertEquals(TicketStatus.REOPENED, response.getStatus());
        verify(historyRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Yêu cầu mở lại Ticket sau quá 3 ngày kể từ khi Resolved -> Bị từ chối BadRequestException")
    void reopenTicket_WhenAfter3Days_ShouldThrowBadRequest() {
        authenticateAs(employeeUser, "ROLE_EMPLOYEE");

        Ticket ticket = Ticket.builder()
                .id(100L)
                .category(hardwareCategory)
                .createdBy(employeeUser)
                .status(TicketStatus.RESOLVED)
                .dueAt(LocalDateTime.now().plusDays(1))
                .resolvedAt(LocalDateTime.now().minusDays(4)) // Đã giải quyết cách đây 4 ngày (> 3 ngày)
                .version(2)
                .build();

        ReopenTicketRequest request = ReopenTicketRequest.builder()
                .reason("Muốn xử lý lại")
                .version(2)
                .build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(3L)).thenReturn(Optional.of(employeeUser));
        doThrow(new BadRequestException("Đã quá thời hạn 3 ngày (72 giờ) kể từ khi sự cố được giải quyết. Vui lòng tạo Ticket mới"))
                .when(ticketAuthorizationService).checkCanReopen(eq(ticket), any());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> ticketService.reopenTicket(100L, request));
        assertTrue(ex.getMessage().contains("quá thời hạn 3 ngày"));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Xem chi tiết Ticket thất bại khi người dùng không liên quan (Resource-level Authorization - Anti IDOR)")
    void getTicketDetail_WhenUserHasNoPermission_ShouldThrowAccessDeniedException() {
        User stranger = User.builder().id(99L).username("stranger").status("ACTIVE").roles(Set.of()).build();
        authenticateAs(stranger, "ROLE_EMPLOYEE");

        Ticket ticket = Ticket.builder()
                .id(100L)
                .category(hardwareCategory)
                .createdBy(employeeUser) // Creator là ID 3
                .assignedTo(staffUser)   // Assignee là ID 2
                .build();

        when(ticketRepository.findById(100L)).thenReturn(Optional.of(ticket));
        doThrow(new com.itservicedesk.common.exception.ResourceNotFoundException("Ticket", "id", 100L))
                .when(ticketAuthorizationService).checkCanViewTicket(eq(ticket), any());

        assertThrows(com.itservicedesk.common.exception.ResourceNotFoundException.class,
                () -> ticketService.getTicketDetail(100L));
    }
}
