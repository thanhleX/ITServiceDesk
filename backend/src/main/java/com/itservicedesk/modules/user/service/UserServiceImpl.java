package com.itservicedesk.modules.user.service;

import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.common.exception.BadRequestException;
import com.itservicedesk.common.exception.ConflictException;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.modules.department.entity.Department;
import com.itservicedesk.modules.department.repository.DepartmentRepository;
import com.itservicedesk.modules.user.dto.CreateUserRequest;
import com.itservicedesk.modules.user.dto.UpdateUserRequest;
import com.itservicedesk.modules.user.dto.UserFilterCriteria;
import com.itservicedesk.modules.user.dto.UserResponse;
import com.itservicedesk.modules.user.entity.Role;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.RoleRepository;
import com.itservicedesk.modules.user.repository.UserRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<UserResponse> getUsers(UserFilterCriteria criteria, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria != null) {
                if (StringUtils.hasText(criteria.getKeyword())) {
                    String pattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
                    Predicate matchUsername = cb.like(cb.lower(root.get("username")), pattern);
                    Predicate matchEmail = cb.like(cb.lower(root.get("email")), pattern);
                    Predicate matchFullName = cb.like(cb.lower(root.get("fullName")), pattern);
                    predicates.add(cb.or(matchUsername, matchEmail, matchFullName));
                }

                if (criteria.getDepartmentId() != null) {
                    predicates.add(cb.equal(root.get("department").get("id"), criteria.getDepartmentId()));
                }

                if (StringUtils.hasText(criteria.getStatus())) {
                    predicates.add(cb.equal(root.get("status"), criteria.getStatus().toUpperCase()));
                }

                if (StringUtils.hasText(criteria.getRoleCode())) {
                    Join<User, Role> rolesJoin = root.join("roles");
                    predicates.add(cb.equal(rolesJoin.get("code"), criteria.getRoleCode().toUpperCase()));
                }
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> page = userRepository.findAll(spec, pageable);
        List<UserResponse> content = page.getContent().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.<UserResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getActiveStaffs() {
        return userRepository.findAll().stream()
                .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                .filter(u -> u.getRoles().stream().anyMatch(r -> "ROLE_IT_STAFF".equalsIgnoreCase(r.getCode())))
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Tên đăng nhập đã tồn tại: " + username);
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email đã tồn tại: " + email);
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        }

        Set<Role> roles = new HashSet<>();
        for (String roleCode : request.getRoleCodes()) {
            Role role = roleRepository.findByCode(roleCode.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "code", roleCode));
            roles.add(role);
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .department(department)
                .roles(roles)
                .status("ACTIVE")
                .build();

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
        }

        Set<Role> roles = new HashSet<>();
        for (String roleCode : request.getRoleCodes()) {
            Role role = roleRepository.findByCode(roleCode.toUpperCase())
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "code", roleCode));
            roles.add(role);
        }

        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setDepartment(department);
        user.setRoles(roles);

        if (StringUtils.hasText(request.getStatus())) {
            user.setStatus(request.getStatus().toUpperCase());
        }

        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changeUserStatus(Long id, String status) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        String upperStatus = status.toUpperCase();
        if (!List.of("ACTIVE", "LOCKED", "INACTIVE").contains(upperStatus)) {
            throw new BadRequestException("Trạng thái không hợp lệ. Chỉ chấp nhận ACTIVE, LOCKED, INACTIVE");
        }

        user.setStatus(upperStatus);
        userRepository.save(user);
    }
}
