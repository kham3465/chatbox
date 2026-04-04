package com.vn.nhom2.service;

import com.vn.nhom2.dto.request.UserProfileUpdateRequest;
import com.vn.nhom2.dto.response.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    /**
     * Get user profile by user ID
     *
     * @param userId the user ID
     * @return UserProfileResponse containing profile information
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * Update user profile
     * Phone number cannot be updated once set
     *
     * @param userId the user ID
     * @param request the profile update request
     * @param imageFile optional profile image file
     * @return updated UserProfileResponse
     */
    UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request, MultipartFile imageFile);

    /**
     * Get current authenticated user profile
     *
     * @return UserProfileResponse of the current user
     */
    UserProfileResponse getCurrentUserProfile();
}
