package com.vn.nhom2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class AppChatBox {
    public static void main(String[] args) {
        SpringApplication.run(AppChatBox.class, args);
    }
}
