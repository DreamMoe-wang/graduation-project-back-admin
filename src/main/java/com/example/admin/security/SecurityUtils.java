package com.example.admin.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

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

    public static Long requireCurrentUserId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未获取到当前登录用户");
        }
        return userId;
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

    public static boolean hasRole(String roleCode) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        String targetRole = roleCode != null && roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
        return authorities.stream().anyMatch(item -> targetRole.equals(item.getAuthority()));
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }
}
