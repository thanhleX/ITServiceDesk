package com.itservicedesk.modules.department.service;

import com.itservicedesk.modules.department.dto.DepartmentRequest;
import com.itservicedesk.modules.department.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> getAllDepartments();

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);

    void changeDepartmentStatus(Long id, String status);
}
