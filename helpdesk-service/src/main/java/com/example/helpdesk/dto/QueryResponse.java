package com.example.helpdesk.dto;

import com.example.helpdesk.enums.QueryStatus;
import com.example.helpdesk.enums.RelatedService;
import java.time.LocalDateTime;

public class QueryResponse {
    private Long id;
    private String question;
    private String answer;
    private QueryStatus status;
    private RelatedService relatedService;
    private String askedBy;
    private String answeredBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime answeredAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public QueryStatus getStatus() { return status; }
    public void setStatus(QueryStatus status) { this.status = status; }
    public RelatedService getRelatedService() { return relatedService; }
    public void setRelatedService(RelatedService relatedService) { this.relatedService = relatedService; }
    public String getAskedBy() { return askedBy; }
    public void setAskedBy(String askedBy) { this.askedBy = askedBy; }
    public String getAnsweredBy() { return answeredBy; }
    public void setAnsweredBy(String answeredBy) { this.answeredBy = answeredBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
}

