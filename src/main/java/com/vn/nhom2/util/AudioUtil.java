package com.vn.nhom2.util;

import com.vn.nhom2.config.FileConfig;
import com.vn.nhom2.exception.ClientErrorException;
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

@Slf4j
public class AudioUtil {

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