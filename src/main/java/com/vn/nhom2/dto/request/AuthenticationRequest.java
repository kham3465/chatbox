package com.vn.nhom2.dto.request;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String name;
    private String password;

}
