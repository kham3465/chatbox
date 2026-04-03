package com.vn.nhom2.controller;

import com.vn.nhom2.dto.request.UserProfileUpdateRequest;
import com.vn.nhom2.dto.response.UserProfileResponse;
import com.vn.nhom2.service.UserService;
import com.vn.nhom2.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {
    private final UserService userService;

    /**
     * Get current user profile
     * Requires authentication
     *
     * @return UserProfileResponse with user profile information
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieve the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated")
    })
    public ResponseEntity<StandardResponse> getCurrentUserProfile() {
        UserProfileResponse profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(
                new StandardResponse("200", "Thành công", profile)
        );
    }

    /**
     * Get user profile by ID
     * Requires authentication
     *
     * @param userId the user ID
     * @return UserProfileResponse with user profile information
     */
    @GetMapping("/profile/{userId}")
    @Operation(summary = "Get user profile by ID", description = "Retrieve profile information for a specific user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated")
    })
    public ResponseEntity<StandardResponse> getUserProfile(@PathVariable Long userId) {
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(
                new StandardResponse("200", "Thành công", profile)
        );
    }

    /**
     * Update current user profile
     * Requires authentication
     * Phone number cannot be updated once set
     *
     * @param request the profile update request
     * @param imageFile optional profile image file
     * @return updated UserProfileResponse
     */
    @PutMapping("/profile")
    @Operation(summary = "Update current user profile", description = "Update profile information for the currently authenticated user. Phone number cannot be updated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = StandardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data or user not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error - File upload failed")
    })
    public ResponseEntity<StandardResponse> updateUserProfile(
            @Valid @ModelAttribute UserProfileUpdateRequest request,
            @RequestParam(required = false) MultipartFile imageFile) {
        UserProfileResponse updatedProfile = userService.updateUserProfile(
                userService.getCurrentUserProfile().getId(), request, imageFile);
        return ResponseEntity.ok(
                new StandardResponse("200", "Cập nhật hồ sơ thành công", updatedProfile)
        );
    }
}
