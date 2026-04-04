package com.vn.nhom2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String name;
    private String phoneNumber;
    private String fullName;
    private LocalDate dateOfBirth;
    private String sex;
    private Double height;
    private Double weight;
    private String bloodGroup;
    private String imageProfile;
}
