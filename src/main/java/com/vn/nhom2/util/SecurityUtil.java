package com.vn.nhom2.util;

import com.vn.nhom2.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static User getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return null;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }
        return null;
    }

    public static boolean isAnonymousUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return true;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal.equals("anonymousUser");
    }
}

