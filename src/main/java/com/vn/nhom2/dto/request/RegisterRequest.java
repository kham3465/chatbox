package com.vn.nhom2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    @NotBlank(message = "Password cannot be blank")
    private String password;
    @NotBlank(message = "Phone number cannot be blank")
    @Pattern(regexp = "^\\d{10,11}$", message = "Phone number must be 10-11 digits")
    private String phoneNumber;
}
