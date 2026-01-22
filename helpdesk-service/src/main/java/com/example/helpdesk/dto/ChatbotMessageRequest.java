package com.example.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatbotMessageRequest {
    @NotBlank(message = "Message is required")
    private String message;

    private String sessionId; // Optional, for continuing conversation
}

