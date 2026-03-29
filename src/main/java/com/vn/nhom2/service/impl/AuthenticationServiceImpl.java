package com.vn.nhom2.service.impl;

import com.vn.nhom2.config.JwtService;
import com.vn.nhom2.dto.request.AuthenticationRequest;
import com.vn.nhom2.dto.request.RegisterRequest;
import com.vn.nhom2.dto.response.AuthenticationResponse;
import com.vn.nhom2.entity.User;
import com.vn.nhom2.enums.Role;
import com.vn.nhom2.exception.ClientErrorException;
import com.vn.nhom2.repo.UserRepository;
import com.vn.nhom2.service.AuthenticationService;
import com.vn.nhom2.util.TimeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByName(request.getName())) {
            throw new ClientErrorException("Tên người dùng đã được đăng ký");
        }
        User user = new User();
        user.setLicenseScore(request.getLicenseScore());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(true);
        user.setName(request.getName());
        user.setRole(Role.USER);
        user.setCreatedTime(TimeUtil.getCurrentDateTime());
        return userRepository.save(user);
    }
    @Override
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getName(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .user(user)
                .accessToken(jwtToken)
                .build();
    }
}
