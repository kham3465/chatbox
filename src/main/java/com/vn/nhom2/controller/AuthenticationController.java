package com.vn.nhom2.controller;

import com.vn.nhom2.dto.request.AuthenticationRequest;
import com.vn.nhom2.dto.response.AuthenticationResponse;
import com.vn.nhom2.dto.request.RegisterRequest;
import com.vn.nhom2.service.AuthenticationService;
import com.vn.nhom2.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth management APIs")
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("/register")
    @Operation(description = "API đăng ký user")
    public ResponseEntity<Object> register(@RequestBody @Valid RegisterRequest request) {
        return new ResponseEntity<>(new StandardResponse("200", "Done", service.register(request)), HttpStatus.OK);
    }
    @PostMapping("/authenticate")
    @Operation(description = "API đăng nhập")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
        return new ResponseEntity(new StandardResponse("200", "Done", service.authenticate(request)), HttpStatus.OK);
    }
}
