package com.vn.nhom2.controller;

import com.vn.nhom2.dto.response.ChatConversationResponse;
import com.vn.nhom2.dto.response.ChatMessageResponse;
import com.vn.nhom2.service.ChatService;
import com.vn.nhom2.util.StandardResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat API")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/create_conversation")
    public ResponseEntity<Object> createConversation(@RequestParam(required = false) String title) {
        ChatConversationResponse conversation = chatService.createConversation(title);
        return new ResponseEntity<>(new StandardResponse("200", "Done", conversation), HttpStatus.OK);
    }

    @GetMapping("/get_list_conversation")
    public ResponseEntity<Object> getAllConversations() {
        List<ChatConversationResponse> conversations = chatService.getAllConversations();
        return new ResponseEntity<>(new StandardResponse("200", "Done", conversations), HttpStatus.OK);
    }

    @GetMapping("/get_messages/{id}")
    public ResponseEntity<Object> getMessages(@PathVariable Long id) {
        List<ChatMessageResponse> messages = chatService.getMessagesByConversation(id);
        return new ResponseEntity<>(new StandardResponse("200", "Done", messages), HttpStatus.OK);
    }

    @PostMapping(value = {"/send_message"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> sendMessage(
            @RequestParam("conversationId") String conversationId,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        Long id = Long.parseLong(conversationId);
        ChatMessageResponse response = chatService.sendUserMessage(id, content, file);
        return new ResponseEntity<>(new StandardResponse("200", "Done", response), HttpStatus.OK);
    }

    @DeleteMapping("/delete_conversation/{id}")
    public ResponseEntity<Object> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        return new ResponseEntity<>(new StandardResponse("200", "Done", null), HttpStatus.OK);
    }
}
