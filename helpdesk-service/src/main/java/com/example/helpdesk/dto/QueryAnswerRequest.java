package com.example.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryAnswerRequest {
    @NotBlank(message = "Answer is required")
    private String answer;
}

