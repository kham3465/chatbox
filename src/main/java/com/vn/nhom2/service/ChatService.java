package com.vn.nhom2.service;

import com.vn.nhom2.dto.response.ChatConversationResponse;
import com.vn.nhom2.dto.response.ChatMessageResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ChatService {
    ChatConversationResponse createConversation(String title);
    List<ChatConversationResponse> getAllConversations();
    List<ChatMessageResponse> getMessagesByConversation(Long conversationId);
    ChatMessageResponse sendUserMessage(Long conversationId, String content, MultipartFile file);
    void deleteConversation(Long conversationId);
}