package com.vn.nhom2.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@Data
public class FileConfig {
    @Value("${nhom2.file.max-file-size}")
    private String maxFileSizeConf;

    @Value("${nhom2.file.max-request-size}")
    private String maxRequestSizeConf;

    @Value("${nhom2.file.allow-extension}")
    private String allowExtensionConf;

    @Value("${nhom2.file.avatar-dir}")
    private String avatarDir;

    public long getMaxFileSize() {
        return getSize(maxFileSizeConf);
    }

    public long getMaxRequestSize() {
        return getSize(maxRequestSizeConf);
    }

    private long getSize(String sizeStr) {
        long size = Long.parseLong(sizeStr.replace("MB", ""));
        return size * 1024 * 1024;
    }

    public List<String> getAllowExtension() {
        return Arrays.asList(allowExtensionConf.split(","));
    }
}
