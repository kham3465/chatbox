package com.vn.nhom2.service;

import com.vn.nhom2.dto.request.AuthenticationRequest;
import com.vn.nhom2.dto.request.RegisterRequest;
import com.vn.nhom2.dto.response.AuthenticationResponse;
import com.vn.nhom2.entity.User;

public interface AuthenticationService {

    public User register(RegisterRequest request);

    public AuthenticationResponse authenticate(AuthenticationRequest request);
}
