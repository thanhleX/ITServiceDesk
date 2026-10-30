package com.itservicedesk.common.util;

import com.itservicedesk.common.exception.UnauthorizedException;
import com.itservicedesk.security.user.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new UnauthorizedException("Người dùng chưa được xác thực trong hệ thống");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    public static String getCurrentUsername() {
        return getCurrentUserDetails().getUsername();
    }

    public static boolean hasRole(String role) {
        return getCurrentUserDetails().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase(role) || a.getAuthority().equalsIgnoreCase("ROLE_" + role));
    }

    public static boolean isManager() {
        return hasRole("ROLE_IT_MANAGER");
    }

    public static boolean isStaff() {
        return hasRole("ROLE_IT_STAFF");
    }

    public static boolean isEmployee() {
        return hasRole("ROLE_EMPLOYEE");
    }
}
