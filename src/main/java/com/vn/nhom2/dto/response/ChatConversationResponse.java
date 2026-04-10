package com.vn.nhom2.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
}