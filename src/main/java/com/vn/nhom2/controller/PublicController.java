package com.vn.nhom2.controller;

import com.vn.nhom2.service.*;
import com.vn.nhom2.service.impl.GeminiServiceImpl;
import com.vn.nhom2.util.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
@Tag(name = "Public Api", description = "Public API")
public class PublicController {

    private final GeminiServiceImpl geminiService;
    @GetMapping("/ask")
    public String askGemini(@RequestParam String q) {
        return geminiService.generateContent(q);
    }
}
