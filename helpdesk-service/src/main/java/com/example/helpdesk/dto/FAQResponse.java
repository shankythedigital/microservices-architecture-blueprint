package com.example.helpdesk.dto;

import com.example.helpdesk.enums.RelatedService;
import java.time.LocalDateTime;

public class FAQResponse {
    private Long id;
    private String question;
    private String answer;
    private RelatedService relatedService;
    private String category;
    private Integer viewCount;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    public Integer getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(Integer helpfulCount) { this.helpfulCount = helpfulCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

