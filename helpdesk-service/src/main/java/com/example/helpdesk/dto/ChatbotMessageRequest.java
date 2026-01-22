package com.example.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
public class ChatbotMessageRequest {
    @NotBlank(message = "Message is required")
    private String message;

    private String sessionId; // Optional, for continuing conversation

    // Getters and Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}

