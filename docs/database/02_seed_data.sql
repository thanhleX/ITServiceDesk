-- =====================================================================
-- IT SERVICE DESK MANAGEMENT SYSTEM - INITIAL SEED DATA
-- =====================================================================

USE it_service_desk_db;

-- 1. Seed Roles
INSERT INTO roles (id, code, name, description) VALUES
(1, 'ROLE_EMPLOYEE', 'Nhân viên', 'Nhân viên nội bộ tạo và theo dõi yêu cầu hỗ trợ'),
(2, 'ROLE_IT_STAFF', 'Kỹ thuật viên IT', 'Kỹ thuật viên tiếp nhận và xử lý sự cố'),
(3, 'ROLE_IT_MANAGER', 'Trưởng phòng IT', 'Quản lý phòng IT, điều phối ticket và giám sát SLA')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2. Seed Permissions
INSERT INTO permissions (id, code, name, module) VALUES
(1, 'TICKET_CREATE', 'Tạo yêu cầu hỗ trợ', 'TICKET'),
(2, 'TICKET_VIEW_SELF', 'Xem yêu cầu của chính mình', 'TICKET'),
(3, 'TICKET_VIEW_ASSIGNED', 'Xem yêu cầu được phân công', 'TICKET'),
(4, 'TICKET_VIEW_ALL', 'Xem tất cả yêu cầu trong hệ thống', 'TICKET'),
(5, 'TICKET_ASSIGN', 'Phân công kỹ thuật viên xử lý', 'TICKET'),
(6, 'TICKET_UPDATE_STATUS', 'Cập nhật trạng thái yêu cầu', 'TICKET'),
(7, 'TICKET_RESOLVE', 'Giải quyết và hoàn thành yêu cầu', 'TICKET'),
(8, 'TICKET_CLOSE', 'Nghiệm thu và đóng yêu cầu', 'TICKET'),
(9, 'TICKET_REOPEN', 'Mở lại yêu cầu chưa giải quyết', 'TICKET'),
(10, 'COMMENT_CREATE', 'Bình luận trên ticket', 'COMMENT'),
(11, 'ATTACHMENT_UPLOAD', 'Tải lên đính kèm', 'ATTACHMENT'),
(12, 'USER_MANAGE', 'Quản trị tài khoản người dùng', 'USER'),
(13, 'CATEGORY_MANAGE', 'Quản trị danh mục sự cố', 'CATEGORY'),
(14, 'REPORT_VIEW', 'Xem báo cáo và dashboard SLA', 'REPORT')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. Seed Role Permissions
-- Employee permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 8), (1, 9), (1, 10), (1, 11);

-- IT Staff permissions
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), (2, 6), (2, 7), (2, 10), (2, 11);

-- IT Manager permissions (Toàn quyền)
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9), (3, 10), (3, 11), (3, 12), (3, 13), (3, 14);

-- 4. Seed Departments
INSERT INTO departments (id, code, name, status) VALUES
(1, 'IT', 'Phòng Công nghệ Thông tin', 'ACTIVE'),
(2, 'HR', 'Phòng Nhân sự', 'ACTIVE'),
(3, 'ACC', 'Phòng Kế toán - Tài chính', 'ACTIVE'),
(4, 'SALES', 'Phòng Kinh doanh & Tiếp thị', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5. Seed Categories
INSERT INTO categories (id, code, name, description, status) VALUES
(1, 'HARDWARE', 'Phần cứng & Thiết bị văn phòng', 'Sự cố máy tính, bàn phím, chuột, màn hình, hỏng RAM, ổ cứng', 'ACTIVE'),
(2, 'SOFTWARE', 'Cài đặt & Bản quyền phần mềm', 'Yêu cầu cài đặt Windows, Office, VPN, phần mềm chuyên dụng', 'ACTIVE'),
(3, 'NETWORK', 'Mạng nội bộ, Wi-Fi & Internet', 'Không kết nối được Wi-Fi, mất mạng LAN, chậm đường truyền', 'ACTIVE'),
(4, 'ACCOUNT', 'Tài khoản & Phân quyền', 'Mở khóa tài khoản nội bộ, reset mật khẩu, cấp quyền thư mục', 'ACTIVE'),
(5, 'PRINTER', 'Máy in, Photocopy & Ngoại vi', 'Máy in kẹt giấy, hết mực, không kết nối được máy in mạng', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 6. Seed Initial Users (Default Password: Password@123)
-- Hash BCrypt: $2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.
INSERT INTO users (id, username, email, password_hash, full_name, phone, department_id, status) VALUES
(1, 'manager', 'manager@itservicedesk.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Nguyễn Văn Quản Lý', '0901234567', 1, 'ACTIVE'),
(2, 'staff_nam', 'nam.it@itservicedesk.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Trần Văn Nam (IT Staff)', '0902345678', 1, 'ACTIVE'),
(3, 'staff_hoa', 'hoa.it@itservicedesk.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Lê Thị Hoa (IT Staff)', '0903456789', 1, 'ACTIVE'),
(4, 'employee_lan', 'lan.hr@itservicedesk.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Phạm Hương Lan (HR)', '0904567890', 2, 'ACTIVE'),
(5, 'employee_tuan', 'tuan.acc@itservicedesk.com', '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzZOI7khzdyGPBr08PpIi0na624b8.', 'Hoàng Anh Tuấn (Accounting)', '0905678901', 3, 'ACTIVE')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- 7. Assign User Roles
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 3), -- manager -> ROLE_IT_MANAGER
(2, 2), -- staff_nam -> ROLE_IT_STAFF
(3, 2), -- staff_hoa -> ROLE_IT_STAFF
(4, 1), -- employee_lan -> ROLE_EMPLOYEE
(5, 1); -- employee_tuan -> ROLE_EMPLOYEE
