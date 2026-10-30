package com.itservicedesk.modules.auth.service;

import com.itservicedesk.common.exception.BadRequestException;
import com.itservicedesk.modules.auth.dto.ChangePasswordRequest;
import com.itservicedesk.modules.auth.dto.LoginRequest;
import com.itservicedesk.modules.auth.dto.LoginResponse;
import com.itservicedesk.modules.user.entity.Role;
import com.itservicedesk.modules.user.entity.User;
import com.itservicedesk.modules.user.repository.UserRepository;
import com.itservicedesk.security.jwt.JwtTokenProvider;
import com.itservicedesk.security.user.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        Role roleStaff = Role.builder().id(2L).code("ROLE_IT_STAFF").name("IT Staff").build();
        sampleUser = User.builder()
                .id(2L)
                .username("staff_nam")
                .email("nam.it@itservicedesk.com")
                .passwordHash("$2a$10$encodedPassword")
                .fullName("Tran Van Nam")
                .status("ACTIVE")
                .roles(Set.of(roleStaff))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Đăng nhập thành công trả về JWT Token và thông tin Roles/Permissions")
    void login_WhenCredentialsValid_ShouldReturnJwtTokenAndUserDetails() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("staff_nam")
                .password("Password@123")
                .build();

        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(2L)
                .username("staff_nam")
                .email("nam.it@itservicedesk.com")
                .fullName("Tran Van Nam")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_IT_STAFF")))
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("mock.jwt.token");
        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getAccessToken());
        assertEquals("staff_nam", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_IT_STAFF"));
    }

    @Test
    @DisplayName("Đổi mật khẩu thất bại khi mật khẩu hiện tại không đúng")
    void changePassword_WhenCurrentPasswordInvalid_ShouldThrowBadRequestException() {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                .id(2L)
                .username("staff_nam")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_IT_STAFF")))
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .currentPassword("WrongPassword")
                .newPassword("NewSecret@123")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword", sampleUser.getPasswordHash())).thenReturn(false);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.changePassword(request));
        assertTrue(ex.getMessage().contains("không chính xác"));
        verify(userRepository, never()).save(any());
    }
}
