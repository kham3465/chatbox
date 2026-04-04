package com.vn.nhom2.service.impl;

import com.vn.nhom2.dto.request.UserProfileUpdateRequest;
import com.vn.nhom2.dto.response.UserProfileResponse;
import com.vn.nhom2.entity.User;
import com.vn.nhom2.exception.ClientErrorException;
import com.vn.nhom2.exception.ResourceNotFoundException;
import com.vn.nhom2.exception.ServerErrorException;
import com.vn.nhom2.repo.UserRepository;
import com.vn.nhom2.service.UserService;
import com.vn.nhom2.util.FileUtil;
import com.vn.nhom2.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
        return mapUserToProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserProfile(Long userId, UserProfileUpdateRequest request,
            MultipartFile imageFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));

        // Update profile fields (phone number is excluded and cannot be updated)
        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setSex(request.getSex());
        user.setHeight(request.getHeight());
        user.setWeight(request.getWeight());
        user.setBloodGroup(request.getBloodGroup());

        // Handle image file upload if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String savedImagePath = FileUtil.saveFile(imageFile.getOriginalFilename(), imageFile);
                user.setImageProfile(savedImagePath);
            } catch (IOException e) {
                log.error("Failed to upload image: {}", e.getMessage(), e);
                throw new ServerErrorException("Không thể lưu hình ảnh: " + e.getMessage());
            }
        }

        User updatedUser = userRepository.save(user);
        return mapUserToProfileResponse(updatedUser);
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) {
            throw new ClientErrorException("Người dùng không được xác thực");
        }
        return getUserProfile(currentUser.getId());
    }

    private UserProfileResponse mapUserToProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getFullName(),
                user.getDateOfBirth(),
                user.getSex(),
                user.getHeight(),
                user.getWeight(),
                user.getBloodGroup(),
                user.getImageProfile());
    }
}
