package com.vn.nhom2.service;

import com.vn.nhom2.config.FileConfig;
import com.vn.nhom2.dto.request.UserProfileUpdateRequest;
import com.vn.nhom2.dto.response.UserProfileResponse;
import com.vn.nhom2.exception.ClientErrorException;
import com.vn.nhom2.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

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

    /**
     * Update FCM token for a user
     *
     * @param userId the user ID
     * @param fcmToken the new FCM token
     */
    void updateFcmToken(Long userId, String fcmToken);

    /**
     * Get FCM token for a user (cached)
     *
     * @param userId the user ID
     * @return the FCM token
     */
    String getFcmToken(Long userId);

    @Slf4j
    class AudioUtil {

        public static String AUDIO_UPLOAD_FOLDER = "uploads/audio";

        public static String saveAudio(MultipartFile file, FileConfig fileConfig) throws IOException {
            FileUtil.validateFiles(List.of(file), fileConfig);

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.startsWith("audio/") && !contentType.startsWith("video/"))) {
                throw new ClientErrorException("File tải lên phải là định dạng âm thanh hoặc video");
            }

            Path uploadPath = Paths.get(AUDIO_UPLOAD_FOLDER).normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String savedFileName = UUID.randomUUID().toString() + extension.toLowerCase();

            try (InputStream inputStream = file.getInputStream()) {
                Path filePath = uploadPath.resolve(savedFileName).normalize();
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                return savedFileName;
            } catch (IOException ex) {
                log.error("Could not save audio file", ex);
                throw new IOException("Could not save audio file", ex);
            }
        }

        public static String convertToBase64(MultipartFile file) throws IOException {
            return Base64.getEncoder().encodeToString(file.getBytes());
        }

        public static void deleteAudio(String fileName) {
            if (fileName == null || fileName.trim().isEmpty()) return;
            try {
                Path filePath = Paths.get(AUDIO_UPLOAD_FOLDER).resolve(fileName.trim()).normalize();
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Could not delete audio: {}", fileName);
            }
        }
    }
}
