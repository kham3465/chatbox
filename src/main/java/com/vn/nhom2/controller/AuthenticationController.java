package com.vn.nhom2.controller;

import com.vn.nhom2.dto.request.AuthenticationRequest;
import com.vn.nhom2.dto.response.AuthenticationResponse;
import com.vn.nhom2.dto.request.RegisterRequest;
import com.vn.nhom2.service.AuthenticationService;
import com.vn.nhom2.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Authentication", description = "Authentication and registration endpoints - No authentication required")
public class AuthenticationController {
	private final AuthenticationService service;

	@PostMapping("/register")
	@Operation(summary = "Register a new user", description = "Create a new user account with phone number, username, password")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
			@ApiResponse(responseCode = "400", description = "Invalid input - Username format invalid or user error"),
			@ApiResponse(responseCode = "409", description = "Conflict - Username or phone number already exists"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<Object> register(@RequestBody @Valid RegisterRequest request) {
		return new ResponseEntity<>(new StandardResponse("200", "Done", service.register(request)), HttpStatus.OK);
	}

	@PostMapping("/authenticate")
	@Operation(summary = "Login and get JWT token", description = "Authenticate user with username and password to receive JWT token for subsequent authenticated requests")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Authentication successful - JWT token returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid credentials"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody @Valid AuthenticationRequest request) {
		return new ResponseEntity(new StandardResponse("200", "Done", service.authenticate(request)), HttpStatus.OK);
	}

	// Dummy class for Swagger documentation
	public static class User {}
}
