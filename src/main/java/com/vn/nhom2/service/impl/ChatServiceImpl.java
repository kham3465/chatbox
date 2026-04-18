package com.vn.nhom2.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vn.nhom2.config.FileConfig;
import com.vn.nhom2.dto.response.ChatConversationResponse;
import com.vn.nhom2.dto.response.ChatMessageResponse;
import com.vn.nhom2.entity.Conversation;
import com.vn.nhom2.entity.Message;
import com.vn.nhom2.entity.User;
import com.vn.nhom2.enums.MessageRole;
import com.vn.nhom2.exception.ClientErrorException;
import com.vn.nhom2.exception.ResourceNotFoundException;
import com.vn.nhom2.repo.ConversationRepository;
import com.vn.nhom2.repo.MessageRepository;
import com.vn.nhom2.service.ChatService;
import com.vn.nhom2.util.FileUtil;
import com.vn.nhom2.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final FileConfig fileConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api-key}")
    private String apiKey;

    @Override
    public List<ChatConversationResponse> getAllConversations() {
        User currentUser = getCurrentAuthenticatedUser();
        return conversationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToConversationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageResponse> getMessagesByConversation(Long conversationId) {
        User currentUser = getCurrentAuthenticatedUser();
        validateConversationOwnership(conversationId, currentUser.getId());
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(this::mapToMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatConversationResponse createConversation(String title) {
        User currentUser = getCurrentAuthenticatedUser();
        Conversation conversation = Conversation.builder()
                .user(currentUser)
                .title(title)
                .build();
        return mapToConversationResponse(conversationRepository.save(conversation));
    }

    @Override
    @Transactional
    public ChatMessageResponse sendUserMessage(Long conversationId, String content, MultipartFile file) {
        User currentUser = getCurrentAuthenticatedUser();
        Conversation conversation = validateConversationOwnership(conversationId, currentUser.getId());

        Message userMsg = Message.builder()
                .conversation(conversation)
                .role(MessageRole.USER)
                .content(content)
                .build();

        if (file != null && !file.isEmpty()) {
            FileUtil.validateFiles(List.of(file), fileConfig);
            try {
                String originalFileName = file.getOriginalFilename();
                if (originalFileName != null) {
                    originalFileName = originalFileName.replaceAll("\\s+", "");
                }
                String fileName = FileUtil.saveFile(originalFileName, file);
                String fileUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/api/v1/file/view/")
                        .path(fileName)
                        .toUriString();
                userMsg.setFilePath(fileUrl);
                userMsg.setFileType(file.getContentType());
            } catch (IOException e) {
                log.error("Error saving file: {}", e.getMessage());
                throw new ClientErrorException("Không thể lưu file đính kèm");
            }
        }
        messageRepository.save(userMsg);

        // Fetch history to provide context to Gemini
        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);

        String rawJson = callGeminiApi(history);
        String aiText = extractTextFromJson(rawJson);

        Message aiMsg = Message.builder()
                .conversation(conversation)
                .role(MessageRole.MODEL)
                .content(aiText)
                .build();

        return mapToMessageResponse(messageRepository.save(aiMsg));
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        User currentUser = getCurrentAuthenticatedUser();
        validateConversationOwnership(conversationId, currentUser.getId());
        conversationRepository.deleteById(conversationId);
    }

    private String callGeminiApi(List<Message> history) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-lite-preview:generateContent?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> contents = new ArrayList<>();

        for (Message msg : history) {
            List<Map<String, Object>> parts = new ArrayList<>();

            if (msg.getContent() != null && !msg.getContent().isEmpty()) {
                parts.add(Map.of("text", msg.getContent()));
            }

            if (msg.getFilePath() != null) {
                try {
                    String filePath = msg.getFilePath();
                    String fileName = filePath;
                    if (filePath != null && (filePath.contains("/api/v1/file/view/") || filePath.contains("/api/v1/file/download/"))) {
                        fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                    } else if (filePath != null && (filePath.startsWith("http://") || filePath.startsWith("https://"))) {
                        fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                    }
                    Path path = Paths.get(FileUtil.UPLOAD_FOLDER, fileName);
                    if (Files.exists(path)) {
                        byte[] fileData = Files.readAllBytes(path);
                        parts.add(Map.of(
                                "inline_data", Map.of(
                                        "mime_type", msg.getFileType(),
                                        "data", Base64.getEncoder().encodeToString(fileData)
                                )
                        ));
                    }
                } catch (IOException e) {
                    log.error("Error reading file for Gemini context: {}", msg.getFilePath(), e);
                }
            }

            if (!parts.isEmpty()) {
                contents.add(Map.of(
                        "role", msg.getRole().name().toLowerCase(),
                        "parts", parts
                ));
            }
        }

        if (contents.isEmpty()) {
            return "{\"error\": {\"message\": \"No content to send\"}}";
        }

        Map<String, Object> bodyMap = Map.of("contents", contents);

        try {
            String body = objectMapper.writeValueAsString(bodyMap);
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return "{\"error\": {\"message\": \"" + e.getMessage() + "\"}}";
        }
    }

    private Conversation validateConversationOwnership(Long conversationId, Long userId) {
        return conversationRepository.findById(conversationId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }

    private String extractTextFromJson(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            
            // Check for candidates
            JsonNode candidates = root.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }
            
            // Check for error field in the response
            if (root.has("error")) {
                return "AI Error: " + root.path("error").path("message").asText();
            }
            
            // Check for finishReason
            if (candidates.isArray() && candidates.size() > 0) {
                String finishReason = candidates.get(0).path("finishReason").asText();
                if ("SAFETY".equals(finishReason)) {
                    return "Nội dung bị chặn do vi phạm chính sách an toàn.";
                }
            }

            return "AI không thể phản hồi vào lúc này.";
        } catch (Exception e) {
            log.error("Error parsing response: {}", json, e);
            return "Lỗi xử lý phản hồi từ AI";
        }
    }

    private User getCurrentAuthenticatedUser() {
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser == null) throw new ClientErrorException("User not authenticated");
        return currentUser;
    }

    private ChatConversationResponse mapToConversationResponse(Conversation conv) {
        return ChatConversationResponse.builder()
                .id(conv.getId())
                .title(conv.getTitle())
                .createdAt(conv.getCreatedAt())
                .build();
    }

    private ChatMessageResponse mapToMessageResponse(Message msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .role(msg.getRole().name())
                .content(msg.getContent())
                .filePath(msg.getFilePath())
                .fileType(msg.getFileType())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}