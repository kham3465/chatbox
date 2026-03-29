package com.vn.nhom2.util;

import com.vn.nhom2.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static Long getCurrentUserId() {
        User userDetail = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetail.getId();
    }

    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static boolean isAnonymousUser() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser");
    }
}
