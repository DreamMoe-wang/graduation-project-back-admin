package com.example.admin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            return ((SecurityUser) principal).getId();
        }
        return null;
    }

    public static Long getCurrentUserIdOrDefault(Long defaultUserId) {
        Long userId = getCurrentUserId();
        return userId == null ? defaultUserId : userId;
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser) {
            return ((SecurityUser) principal).getUsername();
        }
        return null;
    }
}
