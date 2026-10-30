package com.itservicedesk.modules.auth.service;

import com.itservicedesk.modules.auth.dto.ChangePasswordRequest;
import com.itservicedesk.modules.auth.dto.LoginRequest;
import com.itservicedesk.modules.auth.dto.LoginResponse;
import com.itservicedesk.modules.auth.dto.UserProfileResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserProfileResponse getCurrentUserProfile();

    void changePassword(ChangePasswordRequest request);
}
