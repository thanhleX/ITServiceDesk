package com.itservicedesk.modules.user.service;

import com.itservicedesk.common.dto.PagedResponse;
import com.itservicedesk.modules.user.dto.CreateUserRequest;
import com.itservicedesk.modules.user.dto.UpdateUserRequest;
import com.itservicedesk.modules.user.dto.UserFilterCriteria;
import com.itservicedesk.modules.user.dto.UserResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    PagedResponse<UserResponse> getUsers(UserFilterCriteria criteria, Pageable pageable);

    List<UserResponse> getActiveStaffs();

    UserResponse getUserById(Long id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    void changeUserStatus(Long id, String status);
}
