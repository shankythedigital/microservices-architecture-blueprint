package com.example.helpdesk.controller;

import com.example.helpdesk.dto.ChatbotMessageRequest;
import com.example.helpdesk.dto.ChatbotMessageResponse;
import com.example.helpdesk.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/helpdesk/chatbot")
@Tag(name = "Chatbot", description = "APIs for chatbot interactions")
public class ChatbotController {
    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/message")
    @Operation(summary = "Send message to chatbot", description = "Send a message to the chatbot and get a response")
    public ResponseEntity<ChatbotMessageResponse> sendMessage(@Valid @RequestBody ChatbotMessageRequest request) {
        ChatbotMessageResponse response = chatbotService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get session history", description = "Retrieve conversation history for a chatbot session")
    public ResponseEntity<List<ChatbotMessageResponse>> getSessionHistory(@PathVariable String sessionId) {
        List<ChatbotMessageResponse> history = chatbotService.getSessionHistory(sessionId);
        return ResponseEntity.ok(history);
    }
}

