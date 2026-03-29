package com.vn.nhom2.dto.response;

import com.vn.nhom2.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationResponse {
    private User user;
    private String accessToken;
}
