package com.smartqueue.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {
    }

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUserEmail() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public static String getCurrentUserRole() {
        UserPrincipal user = getCurrentUser();
        if (user != null && !user.getAuthorities().isEmpty()) {
            return user.getAuthorities().iterator().next().getAuthority();
        }
        return null;
    }

    public static boolean isAdmin() {
        String role = getCurrentUserRole();
        return "ROLE_ADMIN".equals(role);
    }
}
