package com.vn.nhom2.controller;

import com.vn.nhom2.config.FileConfig;
import com.vn.nhom2.util.FileUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
@Tag(name = "Files", description = "File management endpoints")
@Slf4j
public class FileController {

    private final FileConfig fileConfig;

    @GetMapping("/avatar/{filename}")
    @Operation(summary = "Get user avatar", description = "Retrieve a user's avatar image")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            Resource resource = FileUtil.getAvatarAsResource(filename, fileConfig);
            String contentType = "application/octet-stream";
            
            try {
                contentType = Files.probeContentType(resource.getFile().toPath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }
            
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Error retrieving avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/download/{filename}")
    @Operation(summary = "Download file", description = "Download a file by its filename")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Resource resource = FileUtil.getFileAsResource(filename);
            String contentType = "application/octet-stream";

            try {
                contentType = Files.probeContentType(resource.getFile().toPath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Error retrieving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/view/{filename}")
    @Operation(summary = "View file inline", description = "View a file or play audio inline without downloading")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) {
        try {
            Resource resource = FileUtil.getFileAsResource(filename);
            String contentType = "application/octet-stream";

            try {
                contentType = Files.probeContentType(resource.getFile().toPath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            log.error("Error retrieving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error retrieving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
