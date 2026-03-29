package com.vn.nhom2.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String licenseScore;
    private String password;
}
