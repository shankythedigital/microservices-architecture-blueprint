package com.example.helpdesk.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatbotMessageResponse {
    private String sessionId;
    private String response;
    private List<ChatMessage> conversationHistory;
    private LocalDateTime timestamp;

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
        private LocalDateTime timestamp;
    }
}

