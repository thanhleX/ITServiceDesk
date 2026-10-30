package com.itservicedesk.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private Long departmentId;
    private String departmentName;
    private String status;
    private List<String> roles;
    private List<String> permissions;
    private LocalDateTime createdAt;
}
