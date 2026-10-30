package com.itservicedesk.config;

import com.itservicedesk.modules.category.entity.Category;
import com.itservicedesk.modules.category.repository.CategoryRepository;
import com.itservicedesk.modules.department.entity.Department;
import com.itservicedesk.modules.department.repository.DepartmentRepository;
import com.itservicedesk.modules.user.entity.Permission;
import com.itservicedesk.modules.user.entity.Role;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.PermissionRepository;
import com.itservicedesk.modules.user.repository.RoleRepository;
import com.itservicedesk.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            log.info("Dữ liệu ban đầu đã tồn tại trong database, bỏ qua bước khởi tạo mẫu.");
            return;
        }

        log.info("Bắt đầu khởi tạo dữ liệu mẫu cho hệ thống IT Service Desk...");

        // 1. Tạo Permissions
        Permission pTicketCreate = createPermissionIfNotFound("TICKET_CREATE", "Tạo yêu cầu hỗ trợ", "TICKET");
        Permission pTicketViewSelf = createPermissionIfNotFound("TICKET_VIEW_SELF", "Xem yêu cầu của chính mình", "TICKET");
        Permission pTicketViewAssigned = createPermissionIfNotFound("TICKET_VIEW_ASSIGNED", "Xem yêu cầu được phân công", "TICKET");
        Permission pTicketViewAll = createPermissionIfNotFound("TICKET_VIEW_ALL", "Xem tất cả yêu cầu trong hệ thống", "TICKET");
        Permission pTicketAssign = createPermissionIfNotFound("TICKET_ASSIGN", "Phân công kỹ thuật viên", "TICKET");
        Permission pTicketUpdateStatus = createPermissionIfNotFound("TICKET_UPDATE_STATUS", "Cập nhật trạng thái", "TICKET");
        Permission pTicketResolve = createPermissionIfNotFound("TICKET_RESOLVE", "Giải quyết yêu cầu", "TICKET");
        Permission pTicketClose = createPermissionIfNotFound("TICKET_CLOSE", "Đóng yêu cầu", "TICKET");
        Permission pTicketReopen = createPermissionIfNotFound("TICKET_REOPEN", "Mở lại yêu cầu", "TICKET");
        Permission pComment = createPermissionIfNotFound("COMMENT_CREATE", "Bình luận ticket", "COMMENT");
        Permission pAttachment = createPermissionIfNotFound("ATTACHMENT_UPLOAD", "Đính kèm file", "ATTACHMENT");
        Permission pUserManage = createPermissionIfNotFound("USER_MANAGE", "Quản lý tài khoản", "USER");
        Permission pCategoryManage = createPermissionIfNotFound("CATEGORY_MANAGE", "Quản trị danh mục", "CATEGORY");
        Permission pReportView = createPermissionIfNotFound("REPORT_VIEW", "Xem báo cáo", "REPORT");

        // 2. Tạo Roles
        Role roleEmployee = Role.builder()
                .code("ROLE_EMPLOYEE")
                .name("Nhân viên")
                .description("Nhân viên nội bộ tạo và theo dõi yêu cầu hỗ trợ")
                .permissions(new HashSet<>(List.of(pTicketCreate, pTicketViewSelf, pTicketClose, pTicketReopen, pComment, pAttachment)))
                .build();
        roleRepository.save(roleEmployee);

        Role roleStaff = Role.builder()
                .code("ROLE_IT_STAFF")
                .name("Kỹ thuật viên IT")
                .description("Kỹ thuật viên tiếp nhận và xử lý sự cố")
                .permissions(new HashSet<>(List.of(pTicketCreate, pTicketViewSelf, pTicketViewAssigned, pTicketUpdateStatus, pTicketResolve, pComment, pAttachment)))
                .build();
        roleRepository.save(roleStaff);

        Role roleManager = Role.builder()
                .code("ROLE_IT_MANAGER")
                .name("Trưởng phòng IT")
                .description("Quản trị phòng IT, điều phối ticket và giám sát SLA")
                .permissions(new HashSet<>(List.of(
                        pTicketCreate, pTicketViewSelf, pTicketViewAssigned, pTicketViewAll,
                        pTicketAssign, pTicketUpdateStatus, pTicketResolve, pTicketClose,
                        pTicketReopen, pComment, pAttachment, pUserManage, pCategoryManage, pReportView
                )))
                .build();
        roleRepository.save(roleManager);

        // 3. Tạo Departments
        Department deptIT = departmentRepository.save(Department.builder().code("IT").name("Phòng Công nghệ Thông tin").status("ACTIVE").build());
        Department deptHR = departmentRepository.save(Department.builder().code("HR").name("Phòng Nhân sự").status("ACTIVE").build());
        Department deptACC = departmentRepository.save(Department.builder().code("ACC").name("Phòng Kế toán - Tài chính").status("ACTIVE").build());
        departmentRepository.save(Department.builder().code("SALES").name("Phòng Kinh doanh & Tiếp thị").status("ACTIVE").build());

        // 4. Tạo Categories
        categoryRepository.save(Category.builder().code("HARDWARE").name("Phần cứng & Thiết bị").description("Sự cố máy tính, RAM, ổ cứng, màn hình").status("ACTIVE").build());
        categoryRepository.save(Category.builder().code("SOFTWARE").name("Cài đặt phần mềm").description("Yêu cầu phần mềm, OS, VPN, bản quyền").status("ACTIVE").build());
        categoryRepository.save(Category.builder().code("NETWORK").name("Mạng & Internet").description("Mất kết nối mạng, Wi-Fi chập chờn").status("ACTIVE").build());
        categoryRepository.save(Category.builder().code("ACCOUNT").name("Tài khoản & Phân quyền").description("Quên mật khẩu, cấp quyền thư mục").status("ACTIVE").build());
        categoryRepository.save(Category.builder().code("PRINTER").name("Máy in & Thiết bị ngoại vi").description("Máy in kẹt giấy, hết mực").status("ACTIVE").build());

        // 5. Tạo Users
        String defaultPasswordHash = passwordEncoder.encode("Password@123");

        // Manager
        userRepository.save(User.builder()
                .username("manager")
                .email("manager@itservicedesk.com")
                .passwordHash(defaultPasswordHash)
                .fullName("Nguyễn Văn Quản Lý")
                .phone("0901234567")
                .department(deptIT)
                .status("ACTIVE")
                .roles(Set.of(roleManager))
                .build());

        // IT Staff 1
        userRepository.save(User.builder()
                .username("staff_nam")
                .email("nam.it@itservicedesk.com")
                .passwordHash(defaultPasswordHash)
                .fullName("Trần Văn Nam")
                .phone("0902345678")
                .department(deptIT)
                .status("ACTIVE")
                .roles(Set.of(roleStaff))
                .build());

        // Employee HR
        userRepository.save(User.builder()
                .username("employee_lan")
                .email("lan.hr@itservicedesk.com")
                .passwordHash(defaultPasswordHash)
                .fullName("Phạm Hương Lan")
                .phone("0904567890")
                .department(deptHR)
                .status("ACTIVE")
                .roles(Set.of(roleEmployee))
                .build());

        log.info("Khởi tạo dữ liệu mẫu hoàn tất! Tài khoản mặc định: manager / Password@123");
    }

    private Permission createPermissionIfNotFound(String code, String name, String module) {
        return permissionRepository.findByCode(code)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder().code(code).name(name).module(module).build()
                ));
    }
}
