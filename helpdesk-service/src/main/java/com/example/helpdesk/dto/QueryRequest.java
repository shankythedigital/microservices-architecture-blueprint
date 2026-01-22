package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
public class QueryRequest {
    @NotBlank(message = "Question is required")
    private String question;

    @NotNull(message = "Related service is required")
    private RelatedService relatedService;

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
}

