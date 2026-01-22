package com.example.helpdesk.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatbotMessageResponse {
    private String sessionId;
    private String response;
    private List<ChatMessage> conversationHistory;
    private LocalDateTime timestamp;

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public List<ChatMessage> getConversationHistory() { return conversationHistory; }
    public void setConversationHistory(List<ChatMessage> conversationHistory) { this.conversationHistory = conversationHistory; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public static class ChatMessage {
        private String role;
        private String content;
        private LocalDateTime timestamp;

        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}

