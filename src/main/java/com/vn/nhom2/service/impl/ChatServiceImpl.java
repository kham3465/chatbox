package com.vn.nhom2.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            try {
                String originalFileName = file.getOriginalFilename();
                String fileName = FileUtil.saveFile(originalFileName, file);
                userMsg.setFilePath(fileName);
                userMsg.setFileType(file.getContentType());
            } catch (IOException e) {
                log.error("Error saving file: {}", e.getMessage());
                throw new ClientErrorException("Không thể lưu file đính kèm");
            }
        }
        messageRepository.save(userMsg);

        String rawJson = callGeminiApi(content, file);
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

    private String callGeminiApi(String prompt, MultipartFile file) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", prompt));

        if (file != null && !file.isEmpty()) {
            try {
                String base64Data = Base64.getEncoder().encodeToString(file.getBytes());
                parts.add(Map.of(
                        "inline_data", Map.of(
                                "mime_type", file.getContentType(),
                                "data", base64Data
                        )
                ));
            } catch (IOException e) {
                log.error("Error encoding file", e);
            }
        }

        Map<String, Object> bodyMap = Map.of(
                "contents", List.of(Map.of("parts", parts))
        );

        try {
            String body = objectMapper.writeValueAsString(bodyMap);
            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return "Error: " + response.getStatusCode();
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error building request body", e);
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
            return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "Error parsing response from AI";
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