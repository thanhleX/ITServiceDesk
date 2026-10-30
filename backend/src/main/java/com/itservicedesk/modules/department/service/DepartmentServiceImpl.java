package com.itservicedesk.modules.department.service;

import com.itservicedesk.common.exception.ConflictException;
import com.itservicedesk.common.exception.ResourceNotFoundException;
import com.itservicedesk.modules.department.dto.DepartmentRequest;
import com.itservicedesk.modules.department.dto.DepartmentResponse;
import com.itservicedesk.modules.department.entity.Department;
import com.itservicedesk.modules.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return DepartmentResponse.fromEntity(dept);
    }

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCode(request.getCode().trim().toUpperCase())) {
            throw new ConflictException("Mã phòng ban đã tồn tại: " + request.getCode());
        }

        Department department = Department.builder()
                .code(request.getCode().trim().toUpperCase())
                .name(request.getName().trim())
                .status(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE")
                .build();

        return DepartmentResponse.fromEntity(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        String newCode = request.getCode().trim().toUpperCase();
        if (!department.getCode().equalsIgnoreCase(newCode) && departmentRepository.existsByCode(newCode)) {
            throw new ConflictException("Mã phòng ban đã tồn tại: " + newCode);
        }

        department.setCode(newCode);
        department.setName(request.getName().trim());
        if (request.getStatus() != null) {
            department.setStatus(request.getStatus().toUpperCase());
        }

        return DepartmentResponse.fromEntity(departmentRepository.save(department));
    }

    @Override
    @Transactional
    public void changeDepartmentStatus(Long id, String status) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        department.setStatus(status.toUpperCase());
        departmentRepository.save(department);
    }
}
