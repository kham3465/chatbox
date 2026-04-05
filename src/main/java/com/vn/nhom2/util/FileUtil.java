package com.vn.nhom2.util;

import com.vn.nhom2.config.FileConfig;
import com.vn.nhom2.exception.ClientErrorException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@Slf4j
public class FileUtil {
    public static String UPLOAD_FOLDER = "files";

    public static String saveFile(String fileName, MultipartFile file) throws IOException {
        Path uploadpath = Paths.get(UPLOAD_FOLDER);
        if (!Files.exists(uploadpath)) {
            Files.createDirectories(uploadpath);
        }
        String fileCode = RandomStringUtils.randomAlphanumeric(8);
        try (InputStream inputStream = file.getInputStream()) {
            String savedFileName = fileCode + "-" + fileName;
            Path filePath = uploadpath.resolve(savedFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return savedFileName;
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new IOException("Could not save file: " + fileName, ex);
        }
    }

    public static void validateFiles(List<MultipartFile> files, FileConfig fileConfig) {

        long sumFileSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (sumFileSize > fileConfig.getMaxRequestSize()) {
            throw new ClientErrorException("Tổng dung lượng file không được vượt quá " + fileConfig.getMaxRequestSize() + "MB");
        }

        files.forEach(x -> {
            if (x.getSize() > fileConfig.getMaxFileSize()) {
                throw new ClientErrorException("File không được vượt quá " + fileConfig.getMaxFileSize() + "MB");
            }
            String extension = Objects.requireNonNull(x.getOriginalFilename()).substring(x.getOriginalFilename().lastIndexOf(".") + 1);
            if (!fileConfig.getAllowExtension().contains(extension.toLowerCase())) {
                throw new ClientErrorException("Chỉ hỗ trợ tải lên các loại file sau: " + fileConfig.getAllowExtensionConf());
            }
        });
    }

    public static Resource getFileAsResource(String fileName) throws IOException {
        Path uploadpath = Paths.get(UPLOAD_FOLDER + File.separator + fileName.trim());
        return new UrlResource(uploadpath.toUri());
    }

    public static String saveAvatar(MultipartFile file, FileConfig fileConfig) throws IOException {
        validateFiles(List.of(file), fileConfig);

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ClientErrorException("File tải lên phải là hình ảnh");
        }

        Path uploadPath = Paths.get(fileConfig.getAvatarDir()).normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFileName = java.util.UUID.randomUUID().toString() + extension.toLowerCase();

        try (InputStream inputStream = file.getInputStream()) {
            Path filePath = uploadPath.resolve(savedFileName).normalize();
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return savedFileName;
        } catch (IOException ex) {
            log.error("Could not save avatar file", ex);
            throw new IOException("Could not save avatar file", ex);
        }
    }

    public static Resource getAvatarAsResource(String fileName, FileConfig fileConfig) throws IOException {
        Path uploadPath = Paths.get(fileConfig.getAvatarDir()).normalize();
        Path filePath = uploadPath.resolve(fileName.trim()).normalize();
        
        // Prevent path traversal
        if (!filePath.startsWith(uploadPath)) {
            throw new ClientErrorException("Đường dẫn file không hợp lệ");
        }
        
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ClientErrorException("File không tồn tại hoặc không thể đọc");
        }
        return resource;
    }

    public static void deleteAvatar(String fileName, FileConfig fileConfig) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }
        try {
            Path uploadPath = Paths.get(fileConfig.getAvatarDir()).normalize();
            Path filePath = uploadPath.resolve(fileName.trim()).normalize();
            
            // Prevent path traversal
            if (filePath.startsWith(uploadPath)) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.warn("Could not delete old avatar: {}", fileName, e);
        }
    }
}
