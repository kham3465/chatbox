package com.vn.nhom2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateRequest {
    @NotBlank(message = "Full name cannot be blank")
    private String fullName;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    
    private String sex;
    
    private Double height;
    
    private Double weight;
    
    private String bloodGroup;
    
    private String imageProfile;
}
