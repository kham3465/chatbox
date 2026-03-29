package com.vn.nhom2.util;

public class StringUtil {
    public static boolean isNullOrEmpty(String obj) {
        return obj == null ||obj.isEmpty() || obj.isBlank();
    }
}
