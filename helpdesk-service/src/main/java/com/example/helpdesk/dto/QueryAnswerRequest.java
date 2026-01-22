package com.example.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
public class QueryAnswerRequest {
    @NotBlank(message = "Answer is required")
    private String answer;

    // Getters and Setters
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}

